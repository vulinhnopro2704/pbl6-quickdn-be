package com.pbl6.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "size_config")
public class SizeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_config_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_code", nullable = false, unique = true)
    private PackageSize sizeCode;

    @Column(name = "surcharge", nullable = false)
    private long surcharge;
}
