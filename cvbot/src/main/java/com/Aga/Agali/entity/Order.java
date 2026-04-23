package com.Aga.Agali.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cv_data_id", nullable = false)
    private CvData cvData;

    private String stripeSessionId;
    private boolean paid;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.paid = false;
    }
}