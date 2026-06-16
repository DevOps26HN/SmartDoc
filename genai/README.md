# SmartDoc GenAI Service

An **independent Python (FastAPI) microservice** that provides SmartDoc's
Generative-AI capabilities. It is an **internal** component — only the SmartDoc
server calls it, over the REST interface below. It is never exposed to the
public internet.

## REST interface

| Method & path | Purpose | Request body | Response body |
|---------------|---------|--------------|---------------|
| `GET  /health`    | Liveness + which backend/model is active | – | `{ "status", "backend", "model" }` |
| `POST /summarize` | Summarize document text | `{ "text", "title?", "max_words?" }` | `{ "summary", "backend", "model" }` |
| `POST /ask`       | Answer a question grounded in document text | `{ "text", "question", "title?" }` | `{ "answer", "backend", "model" }` |

Example:

```bash
curl -s http://localhost:8000/summarize \
  -H 'Content-Type: application/json' \
  -d '{"text":"<document text>","max_words":60}'
```

## Pluggable inference backends

The active backend is chosen through the `GENAI_BACKEND` environment variable —
**no code change and no rebuild** required, and an API key is **never** baked
into the image.

| `GENAI_BACKEND` | Backend | Notes |
|-----------------|---------|-------|
| `openai` (default) | Cloud — OpenAI / any OpenAI-compatible API | Needs `OPENAI_API_KEY` |
| `local`            | Local / self-hosted — [Ollama](https://ollama.com) | Keeps data on-prem; needs a reachable Ollama server |

### Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `GENAI_BACKEND` | `openai` | `openai` (cloud) or `local` (Ollama) |
| `OPENAI_API_KEY` | – | **Secret.** Required when `GENAI_BACKEND=openai` |
| `OPENAI_BASE_URL` | – | Optional override for OpenAI-compatible endpoints |
| `GENAI_MODEL` | `gpt-4o-mini` | Cloud model name |
| `OLLAMA_BASE_URL` | `http://ollama:11434` | Ollama server URL (local backend) |
| `GENAI_LOCAL_MODEL` | `llama3.2:1b` | Local model name |
| `GENAI_LOCAL_AUTO_PULL` | `true` | Pull the local model on first use if missing |
| `GENAI_MAX_TOKENS` | `384` | Max tokens generated |
| `GENAI_DEFAULT_MAX_WORDS` | `80` | Default summary length |
| `GENAI_TEMPERATURE` | `0.2` | Sampling temperature |
| `GENAI_TIMEOUT_SECONDS` | `120` | Upstream model call timeout |

## Run locally (without Docker)

```bash
cd genai
pip install -r requirements.txt
export GENAI_BACKEND=openai
export OPENAI_API_KEY=sk-...        # never commit this
uvicorn app.main:app --port 8000
```

For the local backend, start Ollama (`ollama serve`) and set
`GENAI_BACKEND=local`, `OLLAMA_BASE_URL=http://localhost:11434`.

## Layout

```
genai/
├── Dockerfile
├── requirements.txt
└── app/
    ├── main.py            # FastAPI app + routes (/health, /summarize, /ask)
    ├── config.py          # env-driven settings (no secrets in code)
    ├── schemas.py         # request/response contract
    ├── prompts.py         # prompt construction
    └── backends/
        ├── base.py        # Backend interface
        ├── factory.py     # GENAI_BACKEND -> backend selection
        ├── openai_backend.py
        └── ollama_backend.py
```
