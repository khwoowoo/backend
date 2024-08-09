package com.woohaengshi.backend.service.subject;

import com.woohaengshi.backend.domain.StudyRecord;
import com.woohaengshi.backend.domain.Subject;
import com.woohaengshi.backend.dto.response.studyrecord.FindTimerResponse;
import com.woohaengshi.backend.dto.response.subject.FindSubjectsResponse;
import com.woohaengshi.backend.exception.ErrorCode;
import com.woohaengshi.backend.exception.WoohaengshiException;
import com.woohaengshi.backend.repository.MemberRepository;
import com.woohaengshi.backend.repository.StudyRecordRepository;
import com.woohaengshi.backend.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final StudyRecordRepository studyRecordRepository;
    private final SubjectRepository subjectRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public FindTimerResponse getTimer(Long memberId) {
        validateExistMember(memberId);
        List<FindSubjectsResponse> subjectsResponses = getSubjectsResponses(memberId);
        int todayStudyTime = getTodayStudyTime(memberId, getTodayDate());
        return new FindTimerResponse(todayStudyTime, subjectsResponses);
    }

    private void validateExistMember(Long memberId) {
        if (!memberRepository.existsById(memberId))
            throw new WoohaengshiException(ErrorCode.MEMBER_NOT_FOUND);
    }

    private List<FindSubjectsResponse> getSubjectsResponses(Long memberId) {
        Stream<Subject> subjectStream = subjectRepository.findAllByMemberIdOrderByNameAsc(memberId);
        return subjectStream
                .map(subject -> new FindSubjectsResponse(subject.getId(), subject.getName()))
                .collect(Collectors.toList());
    }

    private int getTodayStudyTime(Long memberId, LocalDate date) {
        Optional<StudyRecord> studyRecordOptional =
                studyRecordRepository.findByDateAndMemberId(date, memberId);
        if (studyRecordOptional.isPresent()) return studyRecordOptional.get().getTime();
        else return 0;
    }

    private LocalDate getTodayDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime standardTime = LocalTime.of(5, 0);

        if (now.toLocalTime().isBefore(standardTime)) return now.toLocalDate().minusDays(1);
        else return now.toLocalDate();
    }
}
