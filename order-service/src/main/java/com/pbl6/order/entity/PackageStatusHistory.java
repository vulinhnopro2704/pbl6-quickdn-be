package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "package_status_histories")
@Getter
@Setter
@NoArgsConstructor
public class PackageStatusHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "package_id", nullable = false)
    private UUID packageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    private PackageStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 50, nullable = false)
    private PackageStatus newStatus;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "changed_by", length = 100)
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    public PackageStatusHistory(UUID packageId, PackageStatus oldStatus, PackageStatus newStatus, String note, UUID changedBy) {
        this.packageId = packageId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.note = note;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}
