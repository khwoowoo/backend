package com.woohaengshi.backend.domain.subject;

import com.woohaengshi.backend.domain.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    protected Subject() {}

    @Builder
    public Subject(Long id, String name, boolean isActive, Member member) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.member = member;
    }

    public Subject(String name, Member member) {
        this.isActive = true;
        this.name = name;
        this.member = member;
    }

    public boolean isActive() {
        return isActive;
    }

    public void inActive() {
        this.isActive = false;
    }

    public void active() {
        this.isActive = true;
    }
}
