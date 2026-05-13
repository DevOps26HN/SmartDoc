package com.smartdoc.document.dto;

import java.util.List;

public record DocumentListResponseDto(
    List<DocumentResponseDto> documents,
    Integer total,
    Integer page,
    Integer pageSize
) {}