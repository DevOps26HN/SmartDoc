package com.smartdoc.document.controller;

import com.smartdoc.document.client.GenAiClient;
import com.smartdoc.document.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GenAI endpoints that operate on arbitrary text the user provides in the client
 * (the "analyse your own document" panel), plus a health passthrough so the UI
 * can show which backend (cloud vs. local) is active.
 *
 * <p>All of these are served by the server and proxied to the internal GenAI
 * microservice — the client never reaches GenAI directly.</p>
 */
@RestController
@RequestMapping("/api/v1/genai")
public class GenAiController {

    private final GenAiClient genAiClient;

    public GenAiController(GenAiClient genAiClient) {
        this.genAiClient = genAiClient;
    }

    @GetMapping("/health")
    public ResponseEntity<GenAiHealthResponse> health() {
        return ResponseEntity.ok(genAiClient.health());
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummaryResponseDto> summarize(@RequestBody FreeTextSummarizeRequest request) {
        return ResponseEntity.ok(
                genAiClient.summarize(request.text(), request.title(), request.maxWords()));
    }

    @PostMapping("/ask")
    public ResponseEntity<AnswerResponseDto> ask(@RequestBody FreeTextAskRequest request) {
        return ResponseEntity.ok(
                genAiClient.ask(request.text(), request.question(), request.title()));
    }
}
