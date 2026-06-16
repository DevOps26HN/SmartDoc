import { useEffect, useState, type FormEvent } from "react";

type DocumentStatus = "PENDING" | "PROCESSING" | "DONE" | "FAILED";

type DocumentItem = {
  id: string;
  originalFilename: string;
  mimeType: string;
  fileSizeBytes: number;
  status: DocumentStatus;
  pageCount: number;
  language: string;
  uploadedAt: string;
  processedAt: string | null;
};

type DocumentListResponse = {
  documents: DocumentItem[];
  total: number;
  page: number;
  pageSize: number;
};

type SummaryResponse = { summary: string; backend: string; model: string };
type AnswerResponse = { answer: string; backend: string; model: string };
type HealthResponse = { status: string; backend: string; model: string };

const USER_EMAIL = "dev@example.com";
const LIST_URL = "/api/v1/documents?page=0&pageSize=20";

const headers = { "Content-Type": "application/json", "X-User-Email": USER_EMAIL };

async function postJson<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, { method: "POST", headers, body: JSON.stringify(body) });
  if (!res.ok) {
    let detail = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      if (data?.message) detail = data.message;
      else if (data?.detail) detail = data.detail;
    } catch {
      /* keep default */
    }
    throw new Error(detail);
  }
  return (await res.json()) as T;
}

function Attribution({ backend, model }: { backend: string; model: string }) {
  return (
    <span className="attribution">
      via {backend} · {model}
    </span>
  );
}

function DocumentCard({ doc }: { doc: DocumentItem }) {
  const [summary, setSummary] = useState<SummaryResponse | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryError, setSummaryError] = useState<string | null>(null);

  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState<AnswerResponse | null>(null);
  const [askLoading, setAskLoading] = useState(false);
  const [askError, setAskError] = useState<string | null>(null);

  async function onSummarize() {
    setSummaryLoading(true);
    setSummaryError(null);
    try {
      setSummary(await postJson<SummaryResponse>(`/api/v1/documents/${doc.id}/summarize`, {}));
    } catch (e) {
      setSummaryError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setSummaryLoading(false);
    }
  }

  async function onAsk(e: FormEvent) {
    e.preventDefault();
    if (!question.trim()) return;
    setAskLoading(true);
    setAskError(null);
    try {
      setAnswer(await postJson<AnswerResponse>(`/api/v1/documents/${doc.id}/ask`, { question }));
    } catch (e) {
      setAskError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setAskLoading(false);
    }
  }

  return (
    <li className="card">
      <div className="card-head">
        <strong>{doc.originalFilename}</strong>
        <span className={`chip chip-${doc.status.toLowerCase()}`}>{doc.status}</span>
      </div>
      <div className="muted small">
        {doc.mimeType} · {doc.fileSizeBytes} bytes
      </div>

      <div className="actions">
        <button onClick={onSummarize} disabled={summaryLoading}>
          {summaryLoading ? "Summarizing…" : "Summarize"}
        </button>
      </div>

      {summaryError && <p className="error">⚠ {summaryError}</p>}
      {summary && (
        <div className="result">
          <div className="result-label">Summary <Attribution backend={summary.backend} model={summary.model} /></div>
          <p>{summary.summary}</p>
        </div>
      )}

      <form className="ask" onSubmit={onAsk}>
        <input
          type="text"
          value={question}
          placeholder="Ask a question about this document…"
          onChange={(e) => setQuestion(e.target.value)}
        />
        <button type="submit" disabled={askLoading || !question.trim()}>
          {askLoading ? "Asking…" : "Ask"}
        </button>
      </form>

      {askError && <p className="error">⚠ {askError}</p>}
      {answer && (
        <div className="result">
          <div className="result-label">Answer <Attribution backend={answer.backend} model={answer.model} /></div>
          <p>{answer.answer}</p>
        </div>
      )}
    </li>
  );
}

function FreeTextPanel() {
  const [text, setText] = useState("");
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState<SummaryResponse | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [summaryError, setSummaryError] = useState<string | null>(null);

  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState<AnswerResponse | null>(null);
  const [askLoading, setAskLoading] = useState(false);
  const [askError, setAskError] = useState<string | null>(null);

  async function onSummarize() {
    if (!text.trim()) return;
    setSummaryLoading(true);
    setSummaryError(null);
    try {
      setSummary(
        await postJson<SummaryResponse>("/api/v1/genai/summarize", { text, title: title || null })
      );
    } catch (e) {
      setSummaryError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setSummaryLoading(false);
    }
  }

  async function onAsk(e: FormEvent) {
    e.preventDefault();
    if (!text.trim() || !question.trim()) return;
    setAskLoading(true);
    setAskError(null);
    try {
      setAnswer(
        await postJson<AnswerResponse>("/api/v1/genai/ask", { text, question, title: title || null })
      );
    } catch (e) {
      setAskError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setAskLoading(false);
    }
  }

  return (
    <section className="panel">
      <h2>Analyze your own text</h2>
      <p className="muted small">
        Paste any document text below and let the GenAI service summarize it or answer a question about it.
      </p>
      <input
        className="title-input"
        type="text"
        value={title}
        placeholder="Optional title"
        onChange={(e) => setTitle(e.target.value)}
      />
      <textarea
        value={text}
        rows={7}
        placeholder="Paste document text here…"
        onChange={(e) => setText(e.target.value)}
      />
      <div className="actions">
        <button onClick={onSummarize} disabled={summaryLoading || !text.trim()}>
          {summaryLoading ? "Summarizing…" : "Summarize"}
        </button>
      </div>
      {summaryError && <p className="error">⚠ {summaryError}</p>}
      {summary && (
        <div className="result">
          <div className="result-label">Summary <Attribution backend={summary.backend} model={summary.model} /></div>
          <p>{summary.summary}</p>
        </div>
      )}

      <form className="ask" onSubmit={onAsk}>
        <input
          type="text"
          value={question}
          placeholder="Ask a question about the text above…"
          onChange={(e) => setQuestion(e.target.value)}
        />
        <button type="submit" disabled={askLoading || !text.trim() || !question.trim()}>
          {askLoading ? "Asking…" : "Ask"}
        </button>
      </form>
      {askError && <p className="error">⚠ {askError}</p>}
      {answer && (
        <div className="result">
          <div className="result-label">Answer <Attribution backend={answer.backend} model={answer.model} /></div>
          <p>{answer.answer}</p>
        </div>
      )}
    </section>
  );
}

function App() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<DocumentListResponse | null>(null);
  const [health, setHealth] = useState<HealthResponse | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(LIST_URL, { headers: { "X-User-Email": USER_EMAIL } });
        if (!res.ok) throw new Error(`Request failed (${res.status})`);
        const json = (await res.json()) as DocumentListResponse;
        if (!cancelled) setData(json);
      } catch (e) {
        if (!cancelled) {
          setData(null);
          setError(e instanceof Error ? e.message : "Unknown error");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    void load();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    let cancelled = false;
    fetch("/api/v1/genai/health", { headers: { "X-User-Email": USER_EMAIL } })
      .then((r) => (r.ok ? r.json() : Promise.reject()))
      .then((h: HealthResponse) => !cancelled && setHealth(h))
      .catch(() => !cancelled && setHealth(null));
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main>
      <header className="app-header">
        <h1>SmartDoc</h1>
        {health ? (
          <span className="badge" title="Active GenAI inference backend">
            AI backend: {health.backend} · {health.model}
          </span>
        ) : (
          <span className="badge badge-off" title="GenAI service health">
            AI backend: unavailable
          </span>
        )}
      </header>
      <p className="muted">
        AI-powered document management. Summarize documents and ask questions about their content —
        powered by an internal GenAI microservice (cloud or self-hosted local model).
      </p>

      <section>
        <h2>Your documents</h2>
        {loading && <p>Loading…</p>}
        {error && (
          <p role="alert" className="error">
            Error: {error} — start the server on port 8080, then refresh.
          </p>
        )}
        {!loading && !error && data && (
          <ul className="cards">
            {data.documents.map((doc) => (
              <DocumentCard key={doc.id} doc={doc} />
            ))}
          </ul>
        )}
      </section>

      <FreeTextPanel />
    </main>
  );
}

export default App;