package com.Aga.Agali.entity;

import com.Aga.Agali.enums.UserState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long chatId;

    private String firstName;
    private String lastName;
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserState state;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.state == null) {
            this.state = UserState.IDLE;
        }
    }
}