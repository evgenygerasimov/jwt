package com.jwt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Entity
@Table(name = "authorities")
public class Role {

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "authority")
    private String authority;
}