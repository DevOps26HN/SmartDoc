"""Prompt construction for the GenAI use cases.

Keeping prompts here (separate from transport and backends) makes the actual
GenAI behaviour easy to read and tune without touching the service wiring.
"""

from typing import Optional


def build_summarize_prompt(text: str, title: Optional[str], max_words: int) -> str:
    header = f'Document title: "{title}"\n\n' if title else ""
    return (
        "You are a precise assistant for a document-management platform. "
        f"Summarize the document below in clear English, in at most {max_words} words. "
        "Capture only the key points and do not invent information that is not present.\n\n"
        f'{header}Document:\n"""\n{text}\n"""\n\nSummary:'
    )


def build_ask_prompt(text: str, question: str, title: Optional[str]) -> str:
    header = f'Document title: "{title}"\n\n' if title else ""
    return (
        "You are a precise assistant for a document-management platform. "
        "Answer the question using ONLY the information in the document below. "
        "If the answer is not contained in the document, say you cannot find it in the document. "
        "Quote the relevant wording when helpful.\n\n"
        f'{header}Document:\n"""\n{text}\n"""\n\n'
        f"Question: {question}\n\nAnswer:"
    )
