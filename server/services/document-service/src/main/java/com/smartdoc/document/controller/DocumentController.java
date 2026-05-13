package com.smartdoc.document.controller;

import com.smartdoc.document.dto.*;
import com.smartdoc.document.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<DocumentListResponseDto> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader("X-User-Email") String ownerEmail) {
        return ResponseEntity.ok(documentService.listDocuments(page, pageSize, ownerEmail));
    }
}