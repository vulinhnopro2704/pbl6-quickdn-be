package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "package_addresses")
@Getter
@Setter
@NoArgsConstructor
public class PackageAddressEntity {
  @Id @GeneratedValue private UUID id;

  @Column(name = "detail", length = 500, nullable = false)
  private String detail;

  @Column(length = 200)
  private String name;

  @Column(length = 30)
  private String phone;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

//  @Column(length = 150)
//  private int wardCode; // phường/xã code
//
//  @Column(length = 150)
//  private int districtCode; // quận/huyện code

  @Column(name = "note", length = 500)
  private String note;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
