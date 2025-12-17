package com.pbl6.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "user_addresses",
    indexes = {
      @Index(name = "idx_user_addresses_user_id", columnList = "user_id"),
      @Index(name = "idx_user_addresses_is_primary", columnList = "is_primary")
    })
public class UserAddress {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "address_line", length = 500)
  private String addressLine; // Optional: street address

  @Column(name = "description", length = 500)
  private String description; // Optional: description of address

  @Column(name = "detail", nullable = false, length = 500)
  private String detail; // Required: Full address with all info

  @Column(name = "name", length = 150)
  private String name; // Optional: name of person at this address (if null, use User's fullName)

  @Column(name = "phone", length = 20)
  private String phone; // Optional: phone of person at this address (if null, use User's phone)

  @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
  private BigDecimal latitude; // Required

  @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
  private BigDecimal longitude; // Required

  @Column(name = "ward_code")
  private Integer wardCode; // Optional: ward code

  @Column(name = "district_code")
  private Integer districtCode; // Optional: district code

  @Column(name = "note", length = 300)
  private String note; // Optional

  @Column(name = "is_primary", nullable = false)
  private boolean isPrimary = false;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
