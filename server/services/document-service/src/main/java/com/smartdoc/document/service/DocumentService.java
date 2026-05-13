package com.smartdoc.document.service;

import com.smartdoc.document.dto.*;
import com.smartdoc.document.model.Document;

import java.util.List;
import java.util.Optional;

public interface DocumentService {
    DocumentListResponseDto listDocuments(int page, int pageSize, String ownerEmail);
    List<Document> getAllDocuments();
    Optional<Document> getDocumentById(String id);
}
