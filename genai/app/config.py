"""Runtime configuration for the GenAI service.

Every value is read from the environment so that nothing environment-specific
(and, crucially, no secret) is baked into the image. Backend selection, model
names, endpoints and timeouts are all externalised — consistent with how the
rest of SmartDoc is configured.
"""

from functools import lru_cache
from typing import Optional

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Unknown env vars (PATH, etc.) are ignored; only declared fields are read.
    model_config = SettingsConfigDict(case_sensitive=False, extra="ignore")

    # Which inference backend to use: "openai" (cloud) or "local" (Ollama).
    # Providers are switched purely through configuration — never by rebuilding
    # the image and never by baking a key into it.
    genai_backend: str = "openai"

    # --- Cloud backend (OpenAI / any OpenAI-compatible API) -------------------
    # The key is a SECRET supplied by the environment / a Kubernetes Secret.
    # It is never committed and has no default.
    openai_api_key: Optional[str] = None
    # Optional override for self-hosted or proxied OpenAI-compatible endpoints.
    openai_base_url: Optional[str] = None
    # Cloud model name.
    genai_model: str = "gpt-4o-mini"

    # --- Local / self-hosted backend (Ollama, LLaMA-family models) ------------
    ollama_base_url: str = "http://ollama:11434"
    genai_local_model: str = "llama3.2:1b"
    # Pull the local model automatically on first use if it is missing.
    genai_local_auto_pull: bool = True

    # --- Generation tuning ----------------------------------------------------
    genai_max_tokens: int = 384
    genai_default_max_words: int = 80
    genai_temperature: float = 0.2
    # Upstream request timeout (seconds) for the model call.
    genai_timeout_seconds: float = 120.0

    # --- Server ---------------------------------------------------------------
    genai_host: str = "0.0.0.0"
    genai_port: int = 8000


@lru_cache
def get_settings() -> Settings:
    return Settings()
