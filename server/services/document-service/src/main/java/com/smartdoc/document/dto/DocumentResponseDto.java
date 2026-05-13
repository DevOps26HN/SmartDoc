package com.smartdoc.document.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponseDto(
    UUID id,
    String originalFilename,
    String mimeType,
    Long fileSizeBytes,
    DocumentStatusDto status,
    Integer pageCount,
    String language,
    OffsetDateTime uploadedAt,
    OffsetDateTime processedAt
) {}