from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    EUREKA_SERVER: str
    APP_NAME: str
    APP_PORT: int

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

settings = Settings()