package com.smartdoc.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Outgoing request body sent to the GenAI service POST /ask. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenAiAskRequest(
    String text,
    String question,
    String title
) {}
