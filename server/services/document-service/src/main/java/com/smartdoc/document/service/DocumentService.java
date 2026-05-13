package com.smartdoc.document.service;

import com.smartdoc.document.dto.*;

public interface DocumentService {
    DocumentListResponseDto listDocuments(int page, int pageSize, String ownerEmail);
}