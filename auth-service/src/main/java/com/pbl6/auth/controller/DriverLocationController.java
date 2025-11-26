package com.pbl6.auth.controller;

import com.pbl6.auth.dto.DriverLocationRequest;
import com.pbl6.auth.dto.DriverLocationResponse;
import com.pbl6.auth.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService locationService;

    @PutMapping("/{driverId}/location")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<DriverLocationResponse> updateLocation(
            @RequestBody DriverLocationRequest req,
            Authentication auth, @PathVariable UUID driverId) {

        UUID actor = UUID.fromString(auth.getName());
        var resp = locationService.updateLocation(actor, driverId, req.latitude(), req.longitude());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{driverId}/location")
    public ResponseEntity<DriverLocationResponse> getLatest(@PathVariable UUID driverId, Authentication auth) {
        Optional<DriverLocationResponse> opt = locationService.getLatest(driverId);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
