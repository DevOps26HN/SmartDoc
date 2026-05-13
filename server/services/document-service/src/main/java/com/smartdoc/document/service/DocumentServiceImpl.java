package com.smartdoc.document.service;

import com.smartdoc.document.dto.*;
import com.smartdoc.document.model.Document;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final List<Document> documents = new ArrayList<>();

    public DocumentServiceImpl() {
        // Mock data for initial milestone, just to test
        documents.add(new Document("1", "Project Proposal", "This is the initial project proposal for SmartDoc.", "PROCESSED"));
        documents.add(new Document("2", "Architecture Overview", "Detailed architecture of the SmartDoc microservices.", "PENDING"));
    }

    @Override
    public DocumentListResponseDto listDocuments(int page, int pageSize, String ownerEmail) {
        List<DocumentResponseDto> dtos = documents.stream()
            .map(d -> new DocumentResponseDto(
                UUID.randomUUID(),
                d.getTitle(),
                "text/plain",
                (long) d.getContent().length(),
                d.getStatus().equals("PROCESSED") ? DocumentStatusDto.DONE : DocumentStatusDto.PENDING,
                1,
                "EN",
                OffsetDateTime.now(),
                null
            ))
            .collect(Collectors.toList());

        return new DocumentListResponseDto(
            dtos,
            dtos.size(),
            page,
            pageSize
        );
    }

    @Override
    public List<Document> getAllDocuments() {
        return documents;
    }

    @Override
    public Optional<Document> getDocumentById(String id) {
        return documents.stream().filter(d -> d.getId().equals(id)).findFirst();
    }
}
