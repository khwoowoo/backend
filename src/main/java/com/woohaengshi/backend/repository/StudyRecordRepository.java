package com.woohaengshi.backend.repository;

import com.woohaengshi.backend.domain.StudyRecord;

import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.statistics.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {
    Optional<StudyRecord> findByDateAndMemberId(LocalDate date, Long memberId);
    @Query("SELECT s FROM StudyRecord s JOIN FETCH s.member WHERE s.member.id = :memberId")
    Optional<StudyRecord> findByMemberId(Long memberId);

    @Query(
            value =
                    "SELECT DAY(r.date), r.time, sb.id, sb.name\n"
                            + "FROM study_record r\n"
                            + "INNER JOIN study_subject st ON r.id = st.study_record_id\n"
                            + "INNER JOIN subject sb ON st.subject_id = sb.id\n"
                            + "WHERE YEAR(r.date) = :year\n"
                            + "  AND MONTH(r.date) = :month\n"
                            + "  AND r.member_id = :memberId\n"
                            + "ORDER BY r.date ASC;",
            nativeQuery = true)
    List<Object[]> findByYearAndMonthAndMemberId(
            @Param(value = "year") int year,
            @Param(value = "month") int month,
            @Param(value = "memberId") Long memberId);

    @Query("SELECT COUNT(s) + 1 FROM StudyRecord s WHERE s.date = :date AND s.time > :time")
    Integer findRankByDateAndMemberId(LocalDate date, int time);

    @Query("SELECT SUM(sr.time) FROM StudyRecord sr WHERE sr.member = :member AND sr.date BETWEEN :startDate AND :endDate")
    Integer sumTimeByMemberAndDateBetween(@Param("member") Member member, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
