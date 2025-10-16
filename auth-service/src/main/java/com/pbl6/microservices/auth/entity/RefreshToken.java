package com.pbl6.microservices.auth.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=512)
    private String token;

    @OneToOne
    private User user;

    @Column(nullable=false)
    private Instant expiryDate;

}
