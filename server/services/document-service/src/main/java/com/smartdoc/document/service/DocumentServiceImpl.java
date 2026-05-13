package com.smartdoc.document.service;

import com.smartdoc.document.dto.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Override
    public DocumentListResponseDto listDocuments(int page, int pageSize, String ownerEmail) {

        List<DocumentResponseDto> dtos = List.of(
            new DocumentResponseDto(
                UUID.randomUUID(),
                "research-paper.pdf",
                "application/pdf",
                245760L,
                DocumentStatusDto.DONE,
                12,
                "EN",
                OffsetDateTime.now().minusDays(1),
                null
            ),
            new DocumentResponseDto(
                UUID.randomUUID(),
                "lease-agreement.pdf",
                "application/pdf",
                512000L,
                DocumentStatusDto.PROCESSING,
                25,
                "EN",
                OffsetDateTime.now().minusHours(3),
                null
            )
        );

        return new DocumentListResponseDto(
            dtos,
            dtos.size(),
            page,
            pageSize
        );
    }
}