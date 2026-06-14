package com.smartdoc.document.dto;

/** Result of a question-answering request — returned to the client and parsed from the GenAI service. */
public record AnswerResponseDto(
    String answer,
    String backend,
    String model
) {}
