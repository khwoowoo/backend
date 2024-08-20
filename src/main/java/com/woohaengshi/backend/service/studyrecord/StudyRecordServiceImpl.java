package com.woohaengshi.backend.service.studyrecord;

import static com.woohaengshi.backend.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.woohaengshi.backend.exception.ErrorCode.STATISTICS_NOT_FOUND;
import static com.woohaengshi.backend.exception.ErrorCode.SUBJECT_NOT_FOUND;
import static com.woohaengshi.backend.exception.ErrorCode.TIME_HAVE_TO_GREATER_THAN_EXIST;

import com.woohaengshi.backend.domain.StudyRecord;
import com.woohaengshi.backend.domain.StudySubject;
import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.statistics.Statistics;
import com.woohaengshi.backend.domain.subject.Subject;
import com.woohaengshi.backend.dto.request.studyrecord.SaveRecordRequest;
import com.woohaengshi.backend.dto.response.studyrecord.ShowMonthlyRecordResponse;
import com.woohaengshi.backend.dto.response.studyrecord.ShowYearlyRecordResponse;
import com.woohaengshi.backend.dto.result.ShowCalendarResult;
import com.woohaengshi.backend.exception.WoohaengshiException;
import com.woohaengshi.backend.repository.MemberRepository;
import com.woohaengshi.backend.repository.StatisticsRepository;
import com.woohaengshi.backend.repository.StudySubjectRepository;
import com.woohaengshi.backend.repository.SubjectRepository;
import com.woohaengshi.backend.repository.studyrecord.StudyRecordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRecordServiceImpl implements StudyRecordService {

    private final MemberRepository memberRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final SubjectRepository subjectRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final StatisticsRepository statisticsRepository;

    @Override
    public void save(SaveRecordRequest request, Long memberId) {
        validateExistMember(memberId);
        Statistics statistics = findStatisticsByMemberId(memberId);
        StudyRecord studyRecord = saveStudyRecordAndUpdateStatistics(request, memberId, statistics);
        saveSubjects(request.getSubjects(), studyRecord);
    }

    private Statistics findStatisticsByMemberId(Long memberId) {
        return statisticsRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new WoohaengshiException(STATISTICS_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public ShowMonthlyRecordResponse getMonthlyRecord(YearMonth date, Long memberId) {
        validateExistMember(memberId);

        List<ShowCalendarResult> studyRecordInCalendar =
                studyRecordRepository.findStudyRecordInCalendar(
                        date.getYear(), date.getMonthValue(), memberId);

        return ShowMonthlyRecordResponse.of(date, createCalendar(date, studyRecordInCalendar));
    }

    private Map<Integer, ShowCalendarResult> createCalendar(
            YearMonth date, List<ShowCalendarResult> studyRecordInCalendar) {
        Map<Integer, ShowCalendarResult> calendar =
                studyRecordInCalendar.stream()
                        .collect(Collectors.toMap(ShowCalendarResult::getDay, Function.identity()));

        return IntStream.rangeClosed(1, date.atEndOfMonth().getDayOfMonth())
                .boxed()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                day ->
                                        calendar.containsKey(day)
                                                ? calendar.get(day)
                                                : ShowCalendarResult.init(day)));
    }

    @Override
    @Transactional(readOnly = true)
    public ShowYearlyRecordResponse showYearlyRecord(int year, Long memberId) {
        validateExistMember(memberId);

        return ShowYearlyRecordResponse.of(
                year, studyRecordRepository.findMonthlyTotalByYearAndMemberId(year, memberId));
    }

    private void validateExistMember(Long memberId) {
        if (!memberRepository.existsById(memberId))
            throw new WoohaengshiException(MEMBER_NOT_FOUND);
    }

    private StudyRecord saveStudyRecordAndUpdateStatistics(
            SaveRecordRequest request, Long memberId, Statistics statistics) {
        Optional<StudyRecord> optionalStudyRecord =
                studyRecordRepository.findByDateAndMemberId(request.getDate(), memberId);
        if (optionalStudyRecord.isPresent()) {
            return updateStudyRecordAndStatistics(request, statistics, optionalStudyRecord);
        }
        statistics.update(request.getTime());
        return studyRecordRepository.save(request.toStudyRecord(findMemberById(memberId)));
    }

    private StudyRecord updateStudyRecordAndStatistics(
            SaveRecordRequest request,
            Statistics statistics,
            Optional<StudyRecord> optionalStudyRecord) {
        StudyRecord studyRecord = optionalStudyRecord.get();
        validateTimeIsGreaterThanExistTime(request, studyRecord);
        statistics.update(request.getTime() - studyRecord.getTime());
        studyRecord.updateTime(request.getTime());
        return studyRecord;
    }

    private void validateTimeIsGreaterThanExistTime(
            SaveRecordRequest request, StudyRecord studyRecord) {
        if (request.getTime() <= studyRecord.getTime()) {
            throw new WoohaengshiException(TIME_HAVE_TO_GREATER_THAN_EXIST);
        }
    }

    private void saveSubjects(List<Long> subjects, StudyRecord studyRecord) {
        for (Long subjectId : subjects) {
            if (!studySubjectRepository.existsBySubjectIdAndStudyRecordId(
                    subjectId, studyRecord.getId())) {
                Subject subject = findSubjectById(subjectId);
                studySubjectRepository.save(createStudySubject(studyRecord, subject));
            }
        }
    }

    private Subject findSubjectById(Long subjectId) {
        return subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new WoohaengshiException(SUBJECT_NOT_FOUND));
    }

    private StudySubject createStudySubject(StudyRecord studyRecord, Subject subject) {
        return StudySubject.builder().subject(subject).studyRecord(studyRecord).build();
    }

    private Member findMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new WoohaengshiException(MEMBER_NOT_FOUND));
    }
}
