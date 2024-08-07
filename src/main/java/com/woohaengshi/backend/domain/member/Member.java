package com.woohaengshi.backend.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "image")
    private String image;

    @Column(name = "group", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Group group;

    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private State state;

    @Column(name = "sleep_date", nullable = false)
    private LocalDate sleepDate;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Member() {}

    public Member(
            Long id,
            String name,
            String email,
            String password,
            String image,
            Group group,
            State state,
            LocalDate sleepDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.image = image;
        this.group = group;
        this.state = state;
        this.sleepDate = sleepDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
