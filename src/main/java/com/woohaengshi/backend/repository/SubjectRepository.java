package com.woohaengshi.backend.repository;

import com.woohaengshi.backend.domain.subject.Subject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findAllByMemberIdOrderByNameAsc(Long memberId);

    boolean existsByMemberIdAndName(Long memberId, String name);
}
