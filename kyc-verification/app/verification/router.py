from fastapi import APIRouter, HTTPException, status, UploadFile, File
import py_eureka_client.eureka_client as eureka_client
import httpx
from deepface import DeepFace
import os
import shutil

router = APIRouter(prefix="/verification", tags=["Verification"])


@router.post("/{userId}")
async def verify_user_kyc(userId: str, file: UploadFile = File(...)):
    async def call_profile_service(url: str):
        async with httpx.AsyncClient() as http:
            response = await http.get(url, timeout=5.0)
            if response.status_code != 200:
                raise HTTPException(status_code=response.status_code, detail="Profile service returned error")
            return response.json()

    try:
        # ১. ইউরেকা দিয়ে প্রোফাইল ডাটা ফেচ করা
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

        # ২. প্রোফাইল থেকে NID ফ্রন্ট সাইডের ইউআরএল নেওয়া
        nid_front_url = profile_data.get("nidFrontSide")
        if not nid_front_url:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="NID front side image not found in user profile"
            )

        # ৩. ক্লায়েন্ট থেকে পাঠানো লাইভ ছবি (Selfie) লোকালি সেভ করা
        temp_selfie_path = f"temp_selfie_{userId}.jpg"
        with open(temp_selfie_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # ৪. DeepFace দিয়ে NID Image URL এবং Live Selfie ম্যাচ করা
        try:
            result = DeepFace.verify(
                img1_path=nid_front_url,
                img2_path=temp_selfie_path,
                model_name="VGG-Face",
                enforce_detection=False
            )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"DeepFace processing failed: {str(e)}"
            )
        finally:
            # টেম্পোরারি ফাইলটি ডিলিট করা
            if os.path.exists(temp_selfie_path):
                os.remove(temp_selfie_path)

        # ৫. ভেরিফিকেশন রেজাল্ট চেক করা
        is_matched = result.get("verified", False)
        distance = result.get("distance", 1.0)

        if not is_matched:
            return {
                "status": "Verification Failed",
                "userId": userId,
                "message": "Face does not match with NID image",
                "score": {"distance": distance, "threshold": result.get("threshold")}
            }

        return {
            "status": "Verified Successfully",
            "userId": userId,
            "score": {"distance": distance, "threshold": result.get("threshold")},
            "profile": profile_data
        }

    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(exc))