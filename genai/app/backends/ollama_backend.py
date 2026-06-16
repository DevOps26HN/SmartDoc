"""Local / self-hosted backend — Ollama serving a LLaMA-family model.

This backend keeps inference fully on-premises (no data leaves the cluster),
which is the privacy selling point of SmartDoc. The model is served by a
separate Ollama process reached over HTTP; this class is a thin client for it.
"""

import logging
import threading

from ..config import Settings
from .base import Backend

logger = logging.getLogger("genai.ollama")


class OllamaBackend(Backend):
    def __init__(self, settings: Settings) -> None:
        import ollama  # lazy import: only needed for this backend

        self._ollama = ollama
        self._model = settings.genai_local_model
        self._temperature = settings.genai_temperature
        self._auto_pull = settings.genai_local_auto_pull
        self._client = ollama.Client(
            host=settings.ollama_base_url,
            timeout=settings.genai_timeout_seconds,
        )
        self._pull_lock = threading.Lock()
        self._pulled = False

    @property
    def name(self) -> str:
        return "local"

    @property
    def model(self) -> str:
        return self._model

    def _ensure_model(self) -> None:
        """Pull the model once if it is not present on the Ollama server."""
        if self._pulled or not self._auto_pull:
            return
        with self._pull_lock:
            if self._pulled:
                return
            logger.info("Ensuring local model '%s' is available (pulling if missing)...", self._model)
            self._client.pull(self._model)
            self._pulled = True

    def _chat(self, prompt: str, max_tokens: int) -> str:
        response = self._client.chat(
            model=self._model,
            messages=[{"role": "user", "content": prompt}],
            options={"num_predict": max_tokens, "temperature": self._temperature},
        )
        return (response["message"]["content"] or "").strip()

    def generate(self, prompt: str, max_tokens: int) -> str:
        try:
            self._ensure_model()
            return self._chat(prompt, max_tokens)
        except self._ollama.ResponseError as exc:
            # Model missing and auto-pull disabled (or a transient miss): try once more after a pull.
            if self._auto_pull and getattr(exc, "status_code", None) == 404:
                self._client.pull(self._model)
                self._pulled = True
                return self._chat(prompt, max_tokens)
            raise
