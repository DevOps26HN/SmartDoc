package com.smartdoc.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Outgoing request body sent to the GenAI service POST /summarize. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenAiSummarizeRequest(
    String text,
    String title,
    @JsonProperty("max_words") Integer maxWords
) {}
