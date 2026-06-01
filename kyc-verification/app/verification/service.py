import asyncio
import logging
import os
import uuid
import httpx
from deepface import DeepFace

logger = logging.getLogger(__name__)


async def download_image(url: str, save_path: str) -> None:

    async with httpx.AsyncClient(timeout=20.0) as client:
        response = await client.get(url)
        response.raise_for_status()
        with open(save_path, "wb") as f:
            f.write(response.content)


def _deepface_verify(nid_path: str, selfie_path: str) -> dict:

    return DeepFace.verify(
        img1_path=nid_path,
        img2_path=selfie_path,
        model_name="VGG-Face",
        enforce_detection=False,
    )


async def run_kyc_verification(user_id: int, selfie_url: str, nid_front_url: str) -> dict:

    selfie_path = f"temp_selfie_{uuid.uuid4().hex}.jpg"

    try:
        logger.info(f"[Service] Downloading selfie for userId={user_id}...")
        await download_image(selfie_url, selfie_path)

        logger.info(f"[Service] Running DeepFace for userId={user_id}...")
        loop = asyncio.get_event_loop()
        result = await loop.run_in_executor(
            None,
            _deepface_verify,
            nid_front_url,
            selfie_path,
        )

        is_verified: bool  = result.get("verified", False)
        distance: float    = result.get("distance", 1.0)
        threshold: float   = result.get("threshold", 0.4)

        logger.info(
            f"[Service] userId={user_id} | verified={is_verified} "
            f"| distance={distance:.4f} | threshold={threshold}"
        )

        return {
            "userId":    user_id,
            "verified":  is_verified,
            "distance":  distance,
            "threshold": threshold,
        }

    except Exception as exc:
        logger.error(f"[Service] KYC failed for userId={user_id}: {exc}")
        raise

    finally:
        if os.path.exists(selfie_path):
            os.remove(selfie_path)
            logger.debug(f"[Service] Temp file removed: {selfie_path}")