from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    # .env ফাইলের ভেরিয়েবলের নামের সাথে হুবহু মিল থাকতে হবে
    EUREKA_SERVER: str
    APP_NAME: str
    APP_PORT: int # Pydantic অটোমেটিক স্ট্রিং থেকে ইন্টিজারে কনভার্ট করে নেবে

    # .env ফাইলটি ট্র্যাক করার জন্য কনফিগারেশন
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

# একটি গ্লোবাল ইনস্ট্যান্স তৈরি করে রাখা, যা অন্য ফাইলে ইম্পোর্ট করা যাবে
settings = Settings()