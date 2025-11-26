package com.pbl6.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "drivers",
        indexes = {
                @Index(name = "idx_drivers_user_id", columnList = "user_id"),
                @Index(name = "idx_drivers_status", columnList = "status")
        },
        uniqueConstraints = {@UniqueConstraint(name = "uc_drivers_driver_code", columnNames = {"driver_code"})})
@Getter
@Setter
@NoArgsConstructor
public class DriverEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "vehicle_plate_number", nullable = false, length = 50, unique = true)
    private String vehiclePlateNumber;

    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "identity_full_name", length = 255)
    private String identityFullName;       // Họ tên

    @Column(name = "identity_number", length = 64)
    private String identityNumber;         // Số CCCD

    @Column(name = "identity_issue_date")
    private LocalDate identityIssueDate;   // Ngày cấp

    @Column(name = "identity_issue_place", length = 255)
    private String identityIssuePlace;     // Nơi cấp

    @Column(name = "identity_address", columnDefinition = "text")
    private String identityAddress;        // Nơi thường trú

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_gender", length = 16)
    private Gender identityGender;         // Giới tính: MALE / FEMALE / OTHER

    @Column(name = "identity_birthdate")
    private LocalDate identityBirthdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DriverStatus status = DriverStatus.PENDING;

    @Column(name = "rating_avg", precision = 5, scale = 2, nullable = false)
    private BigDecimal ratingAvg = BigDecimal.valueOf(0.00);

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
