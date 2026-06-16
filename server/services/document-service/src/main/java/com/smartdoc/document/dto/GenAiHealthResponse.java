package com.smartdoc.document.dto;

/** GenAI service health payload, surfaced to the client so the UI can show the active backend. */
public record GenAiHealthResponse(
    String status,
    String backend,
    String model
) {}
