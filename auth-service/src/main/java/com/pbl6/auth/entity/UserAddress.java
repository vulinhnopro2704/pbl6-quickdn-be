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
  private String addressLine; // đường, số nhà

  @Column(length = 150)
  private String ward; // phường/xã

  @Column(length = 150)
  private String district; // quận/huyện

  @Column(length = 150)
  private String province; // tỉnh/thành phố

  @Column(length = 100)
  private String country = "VN";

  // Use DECIMAL(10,6)
  @Column(name = "latitude", precision = 10, scale = 6)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 10, scale = 6)
  private BigDecimal longitude;

  @Column(name = "note", length = 300)
  private String note;

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
