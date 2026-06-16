"""Cloud backend — OpenAI (or any OpenAI-compatible) Chat Completions API."""

from ..config import Settings
from .base import Backend


class OpenAIBackend(Backend):
    def __init__(self, settings: Settings) -> None:
        from openai import OpenAI  # lazy import: only needed for this backend

        if not settings.openai_api_key:
            raise RuntimeError(
                "OPENAI_API_KEY is not set but GENAI_BACKEND=openai. "
                "Supply it via the environment / a Kubernetes Secret — never bake it into the image."
            )
        self._model = settings.genai_model
        self._temperature = settings.genai_temperature
        self._client = OpenAI(
            api_key=settings.openai_api_key,
            base_url=settings.openai_base_url or None,
            timeout=settings.genai_timeout_seconds,
        )

    @property
    def name(self) -> str:
        return "openai"

    @property
    def model(self) -> str:
        return self._model

    def generate(self, prompt: str, max_tokens: int) -> str:
        completion = self._client.chat.completions.create(
            model=self._model,
            messages=[{"role": "user", "content": prompt}],
            max_tokens=max_tokens,
            temperature=self._temperature,
        )
        return (completion.choices[0].message.content or "").strip()
