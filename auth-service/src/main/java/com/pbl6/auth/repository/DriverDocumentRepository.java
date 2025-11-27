package com.pbl6.auth.repository;

import com.pbl6.auth.entity.DriverDocument;
import com.pbl6.auth.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {
    List<DriverDocument> findByDriverIdOrderByUploadedAtAsc(UUID driverId);
    Optional<DriverDocument> findByDriverIdAndDocType(UUID driverId, DocumentType docType);
}
