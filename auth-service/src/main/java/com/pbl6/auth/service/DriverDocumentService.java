package com.pbl6.auth.service;

import com.pbl6.auth.dto.DriverDocumentResponse;
import com.pbl6.auth.dto.UploadDocumentRequest;
import com.pbl6.auth.entity.*;
import com.pbl6.auth.exception.AppException;
import com.pbl6.auth.repository.DriverDocumentRepository;
import com.pbl6.auth.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverDocumentService {

  private final DriverDocumentRepository docRepo;
  private final DriverRepository driverRepo;

  @Transactional
  public DriverDocumentResponse uploadDocument(
      UUID driverId, UploadDocumentRequest req, UUID actorId, Set<String> roles) {

    var driver =
        driverRepo.findByUserId(driverId).orElseThrow(() -> AppException.notFound("Driver not found"));

    boolean isAdmin = roles != null && roles.contains("ADMIN");
    boolean isOwner = driver.getUserId().equals(actorId);

    if (!isAdmin && !isOwner) {
      throw AppException.forbidden("Not allowed to upload documents for this driver");
    }

    if (req == null || req.docType() == null) {
      throw AppException.badRequest("docType required");
    }
    if (req.fileUrl() == null || req.fileUrl().isBlank()) {
      throw AppException.badRequest("fileUrl required");
    }

    DocumentType docType;
    try {
      docType = DocumentType.valueOf(req.docType());
    } catch (Exception ex) {
      throw AppException.badRequest("Invalid docType");
    }

    // Check existing doc for replacement
    var existing = docRepo.findByDriverIdAndDocType(driverId, docType).orElse(null);

    if (existing != null) {
      existing.setFileUrl(req.fileUrl());
      existing.setUploadedBy(actorId);
      existing.setUploadedAt(LocalDateTime.now());
      existing.setStatus(DocumentStatus.PENDING);
      existing.setReviewedAt(null);
      existing.setReviewedBy(null);
      existing.setReviewNote(null);

      existing = docRepo.save(existing);
      return toDto(existing);
    }

    // Create new document
    DriverDocument d = new DriverDocument();
    d.setDriverId(driverId);
    d.setDocType(docType);
    d.setFileUrl(req.fileUrl());
    d.setUploadedBy(actorId);
    d.setUploadedAt(LocalDateTime.now());
    d.setStatus(DocumentStatus.PENDING);

    d = docRepo.save(d);

    return toDto(d);
  }

  public List<DriverDocumentResponse> listDocuments(
      UUID currentUserId, Set<String> roles, UUID driverId) {
    var driver =
        driverRepo.findByUserId(driverId).orElseThrow(() -> AppException.notFound("Driver not found"));

    boolean isAdmin = roles != null && roles.contains("ADMIN");
    if (!isAdmin && !driver.getUserId().equals(currentUserId))
      throw AppException.forbidden("Access denied");

    return docRepo.findByDriverIdOrderByUploadedAtAsc(driverId).stream().map(this::toDto).toList();
  }

  @Transactional
  public DriverDocumentResponse review(
      UUID docId, boolean approve, String note, UUID reviewerId, Set<String> roles) {
    if (!roles.contains("ADMIN")) {
      throw AppException.forbidden("Only admin can review documents");
    }

    DriverDocument d =
        docRepo.findById(docId).orElseThrow(() -> AppException.notFound("Document not found"));

    d.setStatus(approve ? DocumentStatus.APPROVED : DocumentStatus.REJECTED);
    d.setReviewedBy(reviewerId);
    d.setReviewedAt(LocalDateTime.now());
    d.setReviewNote(note);

    d = docRepo.save(d);

    maybeAutoApproveDriver(d.getDriverId());

    return toDto(d);
  }

  private void maybeAutoApproveDriver(UUID driverId) {
    var required =
        Set.of(
            DocumentType.ID_FRONT,
            DocumentType.ID_BACK,
            DocumentType.DRIVER_LICENSE,
            DocumentType.VEHICLE_REGISTRATION,
            DocumentType.PLATE_PHOTO);

    var docs = docRepo.findByDriverIdOrderByUploadedAtAsc(driverId);

    var approvedTypes =
        docs.stream()
            .filter(d -> d.getStatus() == DocumentStatus.APPROVED)
            .map(DriverDocument::getDocType)
            .collect(Collectors.toSet());

    if (approvedTypes.containsAll(required)) {
      var driver = driverRepo.findByUserId(driverId).orElseThrow();
      driver.setStatus(DriverStatus.APPROVED);
      driver.setApprovedAt(LocalDateTime.now());
      driverRepo.save(driver);
    }
  }

  private DriverDocumentResponse toDto(DriverDocument d) {
    return new DriverDocumentResponse(
        d.getId(),
        d.getDriverId(),
        d.getDocType(),
        d.getFileUrl(),
        d.getMimeType(),
        d.getSize(),
        d.getUploadedBy(),
        d.getUploadedAt(),
        d.getStatus(),
        d.getReviewedBy(),
        d.getReviewedAt(),
        d.getReviewNote());
  }
}
