package com.pbl6.auth.dto;

import com.pbl6.auth.entity.DocumentStatus;
import com.pbl6.auth.entity.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record DriverDocumentResponse(
    UUID id,
    UUID driverId,
    DocumentType docType,
    String fileUrl,
    String mimeType,
    Long size,
    UUID uploadedBy,
    LocalDateTime uploadedAt,
    DocumentStatus status,
    UUID reviewedBy,
    LocalDateTime reviewedAt,
    String reviewNote) {}
