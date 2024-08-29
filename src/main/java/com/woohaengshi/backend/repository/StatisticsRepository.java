package com.woohaengshi.backend.repository;

import com.woohaengshi.backend.domain.statistics.Statistics;

import com.woohaengshi.backend.repository.statistics.StatisticsCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StatisticsRepository
        extends JpaRepository<Statistics, Long>, StatisticsCustomRepository {

    @Query("SELECT s FROM Statistics s JOIN FETCH s.member WHERE s.member.id = :memberId")
    Optional<Statistics> findByMemberId(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Statistics s SET s.weeklyTime = 0")
    void initWeeklyTime();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Statistics s SET s.monthlyTime = 0")
    void initMonthlyTime();
}
