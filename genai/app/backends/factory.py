"""Backend selection — driven entirely by configuration (GENAI_BACKEND)."""

from ..config import Settings
from .base import Backend


def create_backend(settings: Settings) -> Backend:
    backend = (settings.genai_backend or "").strip().lower()
    if backend in ("openai", "cloud"):
        from .openai_backend import OpenAIBackend

        return OpenAIBackend(settings)
    if backend in ("local", "ollama", "llama"):
        from .ollama_backend import OllamaBackend

        return OllamaBackend(settings)
    raise ValueError(
        f"Unknown GENAI_BACKEND '{settings.genai_backend}'. Use 'openai' (cloud) or 'local' (Ollama)."
    )
