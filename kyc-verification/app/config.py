from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    EUREKA_SERVER: str
    APP_NAME: str
    APP_PORT: int
    APP_HOST: str = "localhost"


    RABBITMQ_HOST:str
    RABBITMQ_PORT:str
    RABBITMQ_USER :str
    RABBITMQ_PASSWORD:str
    RABBITMQ_VHOST:str

    # consume
    EXCHANGE_NAME:str
    QUEUE_NAME:str
    ROUTING_KEY:str

    # result
    RESULT_EXCHANGE:str
    RESULT_QUEUE:str
    RESULT_ROUTING_KEY :str

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

settings = Settings()