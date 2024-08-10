package com.woohaengshi.backend.service;

import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.dto.request.subject.SubjectRequest;
import com.woohaengshi.backend.repository.MemberRepository;
import com.woohaengshi.backend.repository.SubjectRepository;
import com.woohaengshi.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private SubjectRepository subjectRepository;
  @InjectMocks private SubjectService subjectService;

  @Test
  void 과목을_저장한다() {
    // Given
    Member member = MemberFixture.builder().build();
    SubjectRequest request = new SubjectRequest();
    request.setSubjectsForAddition(List.of("Java", "Spring"));
    request.setSubjectsForDeletion(List.of());


    given(memberRepository.existsById(member.getId())).willReturn(true);
    given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
    given(subjectRepository.existsByMemberIdAndName(member.getId(), "Java")).willReturn(false);
    given(subjectRepository.existsByMemberIdAndName(member.getId(), "Spring")).willReturn(false);

    // When
    subjectService.editSubjects(member.getId(), request);

    // Then
    assertAll(
            () -> verify(subjectRepository).save(argThat(subject -> subject.getName().equals("Java") && subject.getMember().equals(member))),
            () -> verify(subjectRepository).save(argThat(subject -> subject.getName().equals("Spring") && subject.getMember().equals(member)))
    );
  }

  @Test
  void 과목을_삭제한다() {
    // Given
    Member member = MemberFixture.builder().build();

    given(memberRepository.existsById(member.getId())).willReturn(true);
    given(subjectRepository.existsById(1L)).willReturn(true);
    given(subjectRepository.existsById(2L)).willReturn(true);
    given(subjectRepository.existsById(3L)).willReturn(true);

    SubjectRequest request = new SubjectRequest();
    request.setSubjectsForAddition(List.of());
    request.setSubjectsForDeletion(List.of(1L, 3L));

    // When
    subjectService.editSubjects(member.getId(), request);

    // Then
    assertAll(
            () -> verify(subjectRepository).deleteById(1L),
            () -> verify(subjectRepository).deleteById(3L),
            () -> verify(subjectRepository, never()).deleteById(2L),
            () -> assertTrue(subjectRepository.existsById(2L))
    );
  }
}
