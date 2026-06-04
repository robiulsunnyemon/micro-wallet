import asyncio
import json
import logging
import aio_pika
from aio_pika.abc import AbstractIncomingMessage
from app.verification.service import run_kyc_verification

logger = logging.getLogger(__name__)

# ─── Configuration ───────────────────────────────────────────────────────────
RABBITMQ_HOST     = "localhost"
RABBITMQ_PORT     = 5672
RABBITMQ_USER     = "guest"
RABBITMQ_PASSWORD = "guest"
RABBITMQ_VHOST    = "/"

# consume
EXCHANGE_NAME = "kyc.exchange"
QUEUE_NAME    = "kyc.verification.requested.queue"
ROUTING_KEY   = "profile.kyc.verification.requested"

# result
RESULT_EXCHANGE    = "kyc.exchange"
RESULT_QUEUE       = "profile.kyc.verification.completed.queue"
RESULT_ROUTING_KEY = "kyc.verification.completed"


# ─── Result Publisher ─────────────────────────────────────────────────────────

async def _publish_result(user_id: int, is_verified: bool, distance: float, threshold: float) -> None:

    try:
        connection = await aio_pika.connect_robust(
            host=RABBITMQ_HOST,
            port=RABBITMQ_PORT,
            login=RABBITMQ_USER,
            password=RABBITMQ_PASSWORD,
            virtualhost=RABBITMQ_VHOST,
            heartbeat=60,
        )
        async with connection:
            channel = await connection.channel()

            exchange = await channel.declare_exchange(
                RESULT_EXCHANGE,
                type=aio_pika.ExchangeType.DIRECT,
                durable=True,
            )


            queue = await channel.declare_queue(RESULT_QUEUE, durable=True)
            await queue.bind(exchange, routing_key=RESULT_ROUTING_KEY)

            payload = json.dumps({
                "userId":     user_id,
                "verified":   is_verified,
                "distance":   distance,
                "threshold":  threshold,
            }).encode("utf-8")

            await exchange.publish(
                aio_pika.Message(
                    body=payload,
                    delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
                    content_type="application/json",
                ),
                routing_key=RESULT_ROUTING_KEY,
            )

            logger.info(f"[Publisher] ✅ Result published → userId={user_id}, verified={is_verified}")

    except Exception as exc:
        logger.error(f"[Publisher] ❌ Failed to publish result for userId={user_id}: {exc}")


# ─── Business Logic ──────────────────────────────────────────────────────────

async def _process_message(payload: dict) -> None:

    user_id       = payload.get("userId")
    selfie_url    = payload.get("selfieUrl")
    nid_front_url = payload.get("nidFrontUrl")

    if not all([user_id, selfie_url, nid_front_url]):
        logger.error(f"[Consumer] Missing fields in payload: {payload}")
        return

    try:

        result = await run_kyc_verification(user_id, selfie_url, nid_front_url)

        is_verified = result["verified"]
        distance    = result["distance"]
        threshold   = result["threshold"]

        logger.info(
            f"[Consumer] userId={user_id} → "
            f"{'✅ VERIFIED' if is_verified else '❌ NOT VERIFIED'} "
            f"| distance={distance:.4f}"
        )


        await _publish_result(user_id, is_verified, distance, threshold)

    except Exception as exc:
        logger.error(f"[Consumer] Processing error for userId={user_id}: {exc}")


# ─── Message Handler ─────────────────────────────────────────────────────────

async def _on_message(message: AbstractIncomingMessage) -> None:
    async with message.process(requeue=True):
        try:
            payload: dict = json.loads(message.body.decode("utf-8"))
            logger.info(f"[Consumer] Message received ✓ → userId={payload.get('userId')}")
            asyncio.create_task(_process_message(payload))

        except json.JSONDecodeError as exc:
            logger.error(f"[Consumer] Invalid JSON, discarding: {exc}")
            await message.reject(requeue=False)

        except Exception as exc:
            logger.error(f"[Consumer] Handler error: {exc}")
            raise


# ─── Consumer Entry Point ────────────────────────────────────────────────────

async def start_rabbitmq_consumer() -> None:
    while True:
        try:
            logger.info("[Consumer] Connecting to RabbitMQ...")

            connection = await aio_pika.connect_robust(
                host=RABBITMQ_HOST,
                port=RABBITMQ_PORT,
                login=RABBITMQ_USER,
                password=RABBITMQ_PASSWORD,
                virtualhost=RABBITMQ_VHOST,
                heartbeat=60,
                connection_timeout=30,
            )

            async with connection:
                logger.info("[Consumer] RabbitMQ connected ✓")

                channel = await connection.channel()
                await channel.set_qos(prefetch_count=1)

                exchange = await channel.declare_exchange(
                    EXCHANGE_NAME,
                    type=aio_pika.ExchangeType.DIRECT,
                    durable=True,
                )

                queue = await channel.declare_queue(QUEUE_NAME, durable=True)
                await queue.bind(exchange, routing_key=ROUTING_KEY)

                logger.info(f"[Consumer] Waiting for messages on '{QUEUE_NAME}' ✓")

                await queue.consume(_on_message)
                await asyncio.Future()

        except asyncio.CancelledError:
            logger.info("[Consumer] Shutdown signal received. Exiting cleanly.")
            break

        except Exception as exc:
            logger.error(f"[Consumer] Connection error: {exc}. Retrying in 5s...")
            await asyncio.sleep(5)