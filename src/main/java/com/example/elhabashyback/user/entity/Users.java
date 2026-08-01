package com.example.elhabashyback.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false,columnDefinition = "varchar(255)")
    private String firstName;


    @Column(name = "last_name", nullable = false,columnDefinition = "varchar(255)")
    private String lastName;

    @Column(name = "email",nullable = false,unique = true,columnDefinition = "varchar(255)")
    private String email;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "enabled",columnDefinition = "boolean",nullable = false)
    private Boolean enabled;

    @Column(name = "role",columnDefinition = "varchar(20)",nullable = false)
    private Role role;

    @CreationTimestamp
    private Instant createsAt;

    @PrePersist
    public void prePersist(){
        this.createsAt = Instant.now();
        if (this.enabled == null) {
            this.enabled = true;
        }
    }

}
