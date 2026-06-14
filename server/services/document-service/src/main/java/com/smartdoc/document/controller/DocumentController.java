package com.smartdoc.document.controller;

import com.smartdoc.document.client.GenAiClient;
import com.smartdoc.document.dto.*;
import com.smartdoc.document.model.Document;
import com.smartdoc.document.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final GenAiClient genAiClient;

    public DocumentController(DocumentService documentService, GenAiClient genAiClient) {
        this.documentService = documentService;
        this.genAiClient = genAiClient;
    }

    @GetMapping
    public ResponseEntity<DocumentListResponseDto> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader(value = "X-User-Email", required = false) String ownerEmail) {
        return ResponseEntity.ok(documentService.listDocuments(page, pageSize, ownerEmail));
    }

    @GetMapping("/all")
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable String id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GenAI use case #1 — summarize a stored document.
     * client -> server -> GenAI service -> summary returned to the client.
     */
    @PostMapping("/{id}/summarize")
    public ResponseEntity<SummaryResponseDto> summarizeDocument(@PathVariable UUID id) {
        return documentService.getDocumentById(id.toString())
                .map(doc -> ResponseEntity.ok(
                        genAiClient.summarize(doc.getContent(), doc.getTitle(), null)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GenAI use case #2 — ask a question grounded in a stored document.
     */
    @PostMapping("/{id}/ask")
    public ResponseEntity<AnswerResponseDto> askDocument(
            @PathVariable UUID id,
            @RequestBody AskDocumentRequest request) {
        return documentService.getDocumentById(id.toString())
                .map(doc -> ResponseEntity.ok(
                        genAiClient.ask(doc.getContent(), request.question(), doc.getTitle())))
                .orElse(ResponseEntity.notFound().build());
    }
}
