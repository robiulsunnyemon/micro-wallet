from fastapi import APIRouter, HTTPException, status
import py_eureka_client.eureka_client as eureka_client
import httpx
from app.config import settings

EUREKA_SERVER = settings.EUREKA_SERVER

router = APIRouter(prefix="/verification", tags=["Verification"])


@router.get("/{userId}")
async def get_user_verification_with_profile(userId: str):

    async def call_profile_service(url: str):

        async with httpx.AsyncClient() as http:
            response = await http.get(url, timeout=5.0)
            if response.status_code != 200:
                import urllib.request
                raise urllib.request.HTTPError(
                    url, response.status_code,
                    f"Profile service returned {response.status_code}",
                    {}, None
                )
            return response.json()

    try:
        profile_data = await eureka_client.walk_nodes_async(
            "PROFILE-SERVICE",
            f"/profiles/user/kyc/verification/{userId}",
            walker=call_profile_service
        )

        if profile_data is None:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="PROFILE-SERVICE could not be resolved via Eureka"
            )






        return {
            "status": "Verified Successfully",
            "userId": userId,
            "profile": profile_data
        }

    except HTTPException:
        raise

    except httpx.TimeoutException:
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="Request to Profile Service timed out"
        )

    except httpx.RequestError as exc:
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail=f"Network error contacting Profile Service: {exc}"
        )

    except Exception as exc:
        if "not inited" in str(exc).lower() or "EurekaClientException" in type(exc).__name__:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Eureka client is not initialized or PROFILE-SERVICE is down"
            )
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(exc)
        )