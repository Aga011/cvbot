package com.Aga.Agali.entity;

import com.Aga.Agali.enums.CvTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "cv_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private CvTemplate template;

    private String fullName;
    private String profession;
    private String phone;
    private String email;
    private String address;

    @Column(columnDefinition = "BYTEA")
    private byte[] photo;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String languages;

    private String githubLink;
    private String linkedinLink;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(columnDefinition = "TEXT")
    private String computerSkills;

    @Column(columnDefinition = "TEXT")
    private String personalSkills;

    private boolean itProfession;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}