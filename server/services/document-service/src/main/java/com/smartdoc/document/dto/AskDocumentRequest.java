package com.smartdoc.document.dto;

/** Request body from the client when asking a question about a stored document. */
public record AskDocumentRequest(
    String question
) {}
