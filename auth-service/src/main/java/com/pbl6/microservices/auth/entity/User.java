package com.pbl6.microservices.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    private String phone;

    @Column(nullable=false)
    private String password;

    private String roles; // e.g. "ROLE_USER,ROLE_ADMIN"

    private boolean enabled = true;
}
