package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shipping_config")
public class ShippingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long id;

    // số km được tính base_km_fee
    @Column(name = "base_km", nullable = false)
    private double baseKm;

    // phí cho base_km
    @Column(name = "base_km_fee", nullable = false)
    private long baseKmFee;

    // phí mỗi km thêm sau base_km
    @Column(name = "rate_per_km", nullable = false)
    private long ratePerKm;

    // giá mỗi kg
    @Column(name = "rate_per_kg", nullable = false)
    private long ratePerKg;

    // phí tối thiểu
    @Column(name = "min_fee", nullable = false)
    private long minFee;

    // % surcharge nhiên liệu (optional)
    @Column(name = "fuel_surcharge_percent")
    private Double fuelSurchargePercent;

}

