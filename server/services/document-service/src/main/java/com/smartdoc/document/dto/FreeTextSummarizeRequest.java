package com.smartdoc.document.dto;

/** Request body from the client to summarize arbitrary pasted text. */
public record FreeTextSummarizeRequest(
    String text,
    String title,
    Integer maxWords
) {}
