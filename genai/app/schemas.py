"""Request/response schemas — the documented contract of the REST interface."""

from typing import Optional

from pydantic import BaseModel, Field


class SummarizeRequest(BaseModel):
    text: str = Field(..., min_length=1, description="The document text to summarize.")
    title: Optional[str] = Field(None, description="Optional document title for context.")
    max_words: Optional[int] = Field(
        None, ge=10, le=500, description="Approximate maximum length of the summary, in words."
    )


class SummarizeResponse(BaseModel):
    summary: str = Field(..., description="Model-generated summary of the document.")
    backend: str = Field(..., description="Which backend produced the result: 'openai' or 'local'.")
    model: str = Field(..., description="The concrete model used.")


class AskRequest(BaseModel):
    text: str = Field(..., min_length=1, description="The document text to answer questions about.")
    question: str = Field(..., min_length=1, description="The user's question about the document.")
    title: Optional[str] = Field(None, description="Optional document title for context.")


class AskResponse(BaseModel):
    answer: str = Field(..., description="Model-generated answer grounded in the document text.")
    backend: str = Field(..., description="Which backend produced the result: 'openai' or 'local'.")
    model: str = Field(..., description="The concrete model used.")


class HealthResponse(BaseModel):
    status: str
    backend: str
    model: str
