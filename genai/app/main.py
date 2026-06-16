"""SmartDoc GenAI microservice (FastAPI).

An independent Python microservice that turns document text into real model
output. It is an internal component: only the SmartDoc server calls it, over the
REST interface defined below. The active inference backend (cloud OpenAI vs.
local/self-hosted Ollama) is chosen entirely through configuration.
"""

import logging

from fastapi import FastAPI, HTTPException

from .backends.base import Backend
from .backends.factory import create_backend
from .config import get_settings
from .prompts import build_ask_prompt, build_summarize_prompt
from .schemas import (
    AskRequest,
    AskResponse,
    HealthResponse,
    SummarizeRequest,
    SummarizeResponse,
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("genai")

app = FastAPI(
    title="SmartDoc GenAI Service",
    description=(
        "Independent GenAI microservice for SmartDoc. Provides document "
        "summarization and grounded question answering, backed by a pluggable "
        "cloud (OpenAI) or local self-hosted (Ollama) model."
    ),
    version="1.0.0",
)

settings = get_settings()
_backend: Backend | None = None


def get_backend() -> Backend:
    """Lazily build the configured backend (kept out of /health so probes stay cheap)."""
    global _backend
    if _backend is None:
        _backend = create_backend(settings)
        logger.info("Initialised GenAI backend '%s' (model=%s)", _backend.name, _backend.model)
    return _backend


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    # Reports the configured backend/model without forcing a (possibly heavy) load.
    model = settings.genai_model if settings.genai_backend.lower() in ("openai", "cloud") else settings.genai_local_model
    return HealthResponse(status="ok", backend=settings.genai_backend, model=model)


@app.post("/summarize", response_model=SummarizeResponse)
def summarize(request: SummarizeRequest) -> SummarizeResponse:
    max_words = request.max_words or settings.genai_default_max_words
    prompt = build_summarize_prompt(request.text, request.title, max_words)
    backend, output = _run(prompt)
    return SummarizeResponse(summary=output, backend=backend.name, model=backend.model)


@app.post("/ask", response_model=AskResponse)
def ask(request: AskRequest) -> AskResponse:
    prompt = build_ask_prompt(request.text, request.question, request.title)
    backend, output = _run(prompt)
    return AskResponse(answer=output, backend=backend.name, model=backend.model)


def _run(prompt: str) -> tuple[Backend, str]:
    try:
        backend = get_backend()
        output = backend.generate(prompt, max_tokens=settings.genai_max_tokens)
    except Exception as exc:  # config errors + upstream model failures
        logger.exception("GenAI generation failed")
        raise HTTPException(status_code=502, detail=f"GenAI backend error: {exc}") from exc
    if not output:
        raise HTTPException(status_code=502, detail="GenAI backend returned an empty response.")
    return backend, output


if __name__ == "__main__":  # local dev convenience: `python -m app.main`
    import uvicorn

    uvicorn.run("app.main:app", host=settings.genai_host, port=settings.genai_port)
