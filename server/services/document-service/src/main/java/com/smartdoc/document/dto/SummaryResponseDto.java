package com.smartdoc.document.dto;

/** Result of a summarization request — returned to the client and parsed from the GenAI service. */
public record SummaryResponseDto(
    String summary,
    String backend,
    String model
) {}
