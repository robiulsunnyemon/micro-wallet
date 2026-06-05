FROM python:3.11-slim

WORKDIR /app

# Install system dependencies for OpenCV
RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    libxcb1 \
    libxext6 \
    libxrender1 \
    libsm6 \
    && rm -rf /var/lib/apt/lists/*

# Install poetry
RUN pip install --no-cache-dir poetry

# Copy dependency definition files
COPY pyproject.toml poetry.lock ./

# Install dependencies (disable virtualenv creation so it installs globally in the container)
RUN poetry config virtualenvs.create false \
    && poetry install --no-root --no-interaction --no-ansi

# Copy application code
COPY app ./app
COPY .env ./

EXPOSE 8000

# Run uvicorn server
CMD ["poetry", "run", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
