package com.woohaengshi.backend.repository.studyrecord;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static com.woohaengshi.backend.domain.QStudyRecord.studyRecord;
import static com.woohaengshi.backend.domain.QStudySubject.studySubject;
import static com.woohaengshi.backend.domain.subject.QSubject.subject;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woohaengshi.backend.domain.StudyRecord;
import com.woohaengshi.backend.dto.result.ShowCalendarResult;
import com.woohaengshi.backend.dto.result.SubjectResult;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class StudyRecordCustomRepositoryImpl implements StudyRecordCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<ShowCalendarResult> findStudyRecordInCalendar(int year, int month, Long memberId) {
        return jpaQueryFactory
                .selectFrom(studyRecord)
                .leftJoin(studySubject)
                .on(studySubject.studyRecord.id.eq(studyRecord.id))
                .leftJoin(subject)
                .on(subject.id.eq(studySubject.subject.id))
                .where(
                        studyRecord.date.month().eq(month),
                        studyRecord.date.year().eq(year),
                        studyRecord.member.id.eq(memberId))
                .transform(
                        groupBy(studyRecord.id)
                                .list(
                                        Projections.constructor(
                                                ShowCalendarResult.class,
                                                studyRecord.date.dayOfMonth(),
                                                studyRecord.time,
                                                studyRecord.comment,
                                                list(
                                                        Projections.constructor(
                                                                        SubjectResult.class,
                                                                        subject.id,
                                                                        subject.name)
                                                                .skipNulls()))));
    }

    public Slice<StudyRecord> findStudyRecordsByDateSortedByTimeDesc(
            LocalDate date, Pageable pageable) {
        List<StudyRecord> content =
                jpaQueryFactory
                        .selectFrom(studyRecord)
                        .where(studyRecord.date.eq(date))
                        .orderBy(studyRecord.time.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        long total =
                jpaQueryFactory
                        .select(studyRecord.count())
                        .from(studyRecord)
                        .where(studyRecord.date.eq(date))
                        .fetchOne();

        boolean hasNext = pageable.getOffset() + pageable.getPageSize() < total;
        return new SliceImpl<>(content, pageable, hasNext);
    }

    public long getCountStudyRecordsByDate(LocalDate date) {
        return jpaQueryFactory
                .select(studyRecord.count())
                .from(studyRecord)
                .where(studyRecord.date.eq(date))
                .fetchOne();
    }
}
