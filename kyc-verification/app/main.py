from fastapi import FastAPI
import py_eureka_client.eureka_client as eureka_client
from contextlib import asynccontextmanager
from app.verification.router import router as verification_router
from app.config import settings


EUREKA_SERVER = settings.EUREKA_SERVER
APP_NAME = settings.APP_NAME
APP_PORT = settings.APP_PORT

@asynccontextmanager
async def lifespan(_: FastAPI):
    await eureka_client.init_async(
        eureka_server=EUREKA_SERVER,
        app_name=APP_NAME,
        instance_port=APP_PORT,
        instance_host="localhost"
    )
    yield
    await eureka_client.stop_async()

app = FastAPI(lifespan=lifespan)
app.include_router(verification_router)
@app.get("/kyc-verification/test")
def read_root():
    return {"message": "Hello from FastAPI Profile Service via Eureka!"}