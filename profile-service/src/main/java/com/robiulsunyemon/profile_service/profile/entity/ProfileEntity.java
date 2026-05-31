package com.robiulsunyemon.profile_service.profile.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "profiles")
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long walletId;
    private String nameEn;
    private String nameBn;
    private String nidFrontSide;
    private String nidBackSide;
    private LocalDateTime dateOfBirth;
    private String address;
    private String nidNumber;
    private KycStatus kycStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void setCreatedDate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void setUpdatedDate(){
        updatedAt=LocalDateTime.now();
    }
}
