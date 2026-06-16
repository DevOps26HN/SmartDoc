package com.smartdoc.document.dto;

/** Request body from the client to ask a question about arbitrary pasted text. */
public record FreeTextAskRequest(
    String text,
    String question,
    String title
) {}
