# GenAI microservice — independent Python image, containerised separately from
# the client and the server.
FROM python:3.11-slim

# Predictable, quiet, unbuffered Python suitable for container logs.
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# Install dependencies first for better layer caching.
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Application code.
COPY app ./app

# Run as a non-root user.
RUN useradd --create-home --uid 10001 appuser
USER appuser

# Internal-only service: reachable by the SmartDoc server, never by the public
# Ingress. The port is the documented REST interface.
EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
