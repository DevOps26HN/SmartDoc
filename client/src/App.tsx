import { useEffect, useState } from "react";

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

const LIST_URL = "/api/v1/documents?page=0&pageSize=20";

function App() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<DocumentListResponse | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(LIST_URL, {
          headers: { "X-User-Email": "dev@example.com" },
        });
        if (!res.ok) {
          throw new Error(`Request failed (${res.status})`);
        }
        const json = (await res.json()) as DocumentListResponse;
        if (!cancelled) {
          setData(json);
        }
      } catch (e) {
        if (!cancelled) {
          setData(null);
          setError(e instanceof Error ? e.message : "Unknown error");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void load();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main>
      <h1>SmartDoc</h1>
      <p>Documents from the API (dev server proxies /api to Spring).</p>
      {loading && <p>Loading…</p>}
      {error && (
        <p role="alert">
          Error: {error} — start document-service on port 8080, then refresh.
        </p>
      )}
      {!loading && !error && data && (
        <>
          <p>
            Page {data.page}, size {data.pageSize}, total {data.total}, showing{" "}
            {data.documents.length} rows.
          </p>
          <ul>
            {data.documents.map((doc) => (
              <li key={doc.id}>
                <strong>{doc.originalFilename}</strong> — {doc.status} — {doc.mimeType} —{" "}
                {doc.fileSizeBytes} bytes
              </li>
            ))}
          </ul>
        </>
      )}
    </main>
  );
}

export default App;
