package com.pbl6.auth.controller;

import com.pbl6.auth.dto.DriverDocumentResponse;
import com.pbl6.auth.dto.UploadDocumentRequest;
import com.pbl6.auth.service.DriverDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverDocumentController {

  private final DriverDocumentService driverDocumentService;

  @PostMapping("/{driverId}/documents")
  @Operation(summary = "Upload driver document (by URL)")
  public ResponseEntity<DriverDocumentResponse> uploadDocument(
      @PathVariable UUID driverId, @RequestBody UploadDocumentRequest req, Authentication auth) {

    UUID actorId = UUID.fromString(auth.getName());
    Set<String> roles = extractRoles(auth);

    DriverDocumentResponse resp =
        driverDocumentService.uploadDocument(driverId, req, actorId, roles);

    return ResponseEntity.ok(resp);
  }

  private Set<String> extractRoles(Authentication auth) {
    if (auth == null) return Set.of();
    return auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  @GetMapping("/{driverId}/documents")
  public ResponseEntity<List<DriverDocumentResponse>> list(
      @PathVariable UUID driverId, Authentication auth) {

    UUID actor = UUID.fromString(auth.getName());
    Set<String> roles =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    var list = driverDocumentService.listDocuments(actor, roles, driverId);
    return ResponseEntity.ok(list);
  }

  @PostMapping("/documents/{docId}/review")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<DriverDocumentResponse> review(
      @PathVariable UUID docId,
      @RequestParam boolean approve,
      @RequestParam(required = false) String note,
      Authentication auth) {

    UUID reviewer = UUID.fromString(auth.getName());
    Set<String> roles = extractRoles(auth);
    var resp = driverDocumentService.review(docId, approve, note, reviewer, roles);

    return ResponseEntity.ok(resp);
  }
}
