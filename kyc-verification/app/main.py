import asyncio
import logging
from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client
from fastapi import FastAPI
from app.config import settings
from app.verification.rabbitmq_consumer import start_rabbitmq_consumer

# ─── Logging Configuration ───────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(name)s → %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)


# ─── Background Task Reference ───────────────────────────────────────────────
_background_tasks: set = set()



# ─── Lifespan ────────────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(_: FastAPI):
    logger.info("[Lifespan] Registering with Eureka...")
    await eureka_client.init_async(
        eureka_server=settings.EUREKA_SERVER,
        app_name=settings.APP_NAME,
        instance_port=settings.APP_PORT,
        instance_host=settings.APP_HOST,
    )
    logger.info("[Lifespan] Eureka registration complete ✓")
    logger.info("[Lifespan] Starting RabbitMQ consumer...")
    task = asyncio.create_task(start_rabbitmq_consumer())
    _background_tasks.add(task)
    task.add_done_callback(_background_tasks.discard)
    logger.info("[Lifespan] RabbitMQ consumer task created ✓")

    yield  #
    # ─── Shutdown ────────────────────────────────────────────────────────────
    logger.info("[Lifespan] Shutdown initiated...")
    task.cancel()

    try:
        await task
    except asyncio.CancelledError:
        logger.info("[Lifespan] RabbitMQ consumer stopped ✓")

    await eureka_client.stop_async()
    logger.info("[Lifespan] Eureka client stopped ✓")


# ─── App ─────────────────────────────────────────────────────────────────────
app = FastAPI(
    title="KYC Verification Service",
    lifespan=lifespan,
)

@app.get("/kyc-verification/test")
def health_check():
    return {"status": "ok", "service": settings.APP_NAME}