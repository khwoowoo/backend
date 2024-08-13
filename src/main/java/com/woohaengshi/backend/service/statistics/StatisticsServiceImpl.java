package com.woohaengshi.backend.service.statistics;

import com.woohaengshi.backend.domain.StudyRecord;
import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.statistics.Statistics;
import com.woohaengshi.backend.domain.statistics.StatisticsType;
import com.woohaengshi.backend.dto.response.statistics.RankDataResponse;
import com.woohaengshi.backend.dto.response.statistics.ShowRankSnapshotResponse;
import com.woohaengshi.backend.exception.ErrorCode;
import com.woohaengshi.backend.exception.WoohaengshiException;
import com.woohaengshi.backend.repository.StatisticsRepository;

import com.woohaengshi.backend.repository.StudyRecordRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final StudyRecordRepository studyRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public ShowRankSnapshotResponse showRankData(Long memberId, StatisticsType statisticsType, Pageable pageable) {
        Statistics statistics = findStatisticsByMemberId(memberId);
        return statisticsType == StatisticsType.DAILY ? handleDailyStatistics(memberId, pageable, statistics)
                : handlePeriodicStatistics(statisticsType, pageable, statistics);
    }

    private ShowRankSnapshotResponse handleDailyStatistics(Long memberId, Pageable pageable, Statistics statistics) {
        StudyRecord studyRecord = findStudyRecordByMemberId(memberId);
        Slice<StudyRecord> rankSlice = getRankDataSlice(LocalDate.now(), pageable);
        return ShowRankSnapshotResponse.of(
                statistics.getMember(),
                studyRecordRepository.findRankByDateAndMemberId(LocalDate.now(), studyRecord.getTime()),
                studyRecord.getTime(),
                statistics.getTotalTime(),
                rankSlice.hasNext(),
                calculationRank(rankSlice, pageable, statistics));
    }

    private ShowRankSnapshotResponse handlePeriodicStatistics(StatisticsType statisticsType, Pageable pageable, Statistics statistics) {
        Slice<Statistics> rankSlice = getRankDataSlice(statisticsType, pageable);
        return ShowRankSnapshotResponse.of(
                statistics.getMember(),
                getMemberRank(statisticsType, statistics),
                getTimeByStatisticsType(statisticsType, statistics),
                statistics.getTotalTime(),
                rankSlice.hasNext(),
                calculationRank(rankSlice, pageable, statisticsType));
    }
    private int getMemberRank(StatisticsType statisticsType, Statistics statistics) {
        int time = getTimeByStatisticsType(statisticsType, statistics);

        Specification<Statistics> specification =
                (Root<Statistics> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                    if (statisticsType == StatisticsType.WEEKLY)
                        return cb.greaterThan(root.get("weeklyTime"), time);
                    if (statisticsType == StatisticsType.MONTHLY)
                        return cb.greaterThan(root.get("monthlyTime"), time);

                    throw new WoohaengshiException(ErrorCode.STATISTICS_TYPE_NOT_FOUND);
                };
        return (int) statisticsRepository.count(specification) + 1;
    }

    private Slice<Statistics> getRankDataSlice(StatisticsType statisticsType, Pageable pageable) {
        Specification<Statistics> specification =
                (Root<Statistics> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
                    if (statisticsType == StatisticsType.WEEKLY)
                        query.orderBy(cb.desc(root.get("weeklyTime")));
                    if (statisticsType == StatisticsType.MONTHLY)
                        query.orderBy(cb.desc(root.get("monthlyTime")));
                    return query.getRestriction();
                };
        return statisticsRepository.findAll(specification, pageable);
    }

    private List<RankDataResponse> calculationRank(
            Slice<Statistics> statisticsSlice, Pageable pageable, StatisticsType statisticsType) {
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        return IntStream.range(0, statisticsSlice.getNumberOfElements())
                .mapToObj(
                        index -> {
                            Statistics statistics = statisticsSlice.getContent().get(index);
                            Member member = statistics.getMember();

                            return RankDataResponse.of(
                                    member,
                                    startRank + index,
                                    getTimeByStatisticsType(statisticsType, statistics),
                                    statistics.getTotalTime());
                        })
                .toList();
    }

    private Slice<StudyRecord> getRankDataSlice(LocalDate targetDate, Pageable pageable) {
        Specification<StudyRecord> specification = (root, query, cb) -> {
            Predicate datePredicate = cb.equal(root.get("date"), targetDate);
            query.orderBy(cb.desc(root.get("time")));
            return cb.and(datePredicate);
        };
        return studyRecordRepository.findAll(specification, pageable);
    }

    private List<RankDataResponse> calculationRank(
            Slice<StudyRecord> studyRecordSlice, Pageable pageable, Statistics statistics) {
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        return IntStream.range(0, studyRecordSlice.getNumberOfElements())
                .mapToObj(
                        index -> {
                            StudyRecord studyRecord= studyRecordSlice.getContent().get(index);
                            Member member = studyRecord.getMember();

                            return RankDataResponse.of(
                                    member,
                                    startRank + index,
                                    studyRecord.getTime(),
                                    statistics.getTotalTime());
                        })
                .toList();
    }


    private int getTimeByStatisticsType(StatisticsType statisticsType, Statistics statistics) {
        if (statisticsType == StatisticsType.DAILY) return statistics.getDailyTime();
        if (statisticsType == StatisticsType.WEEKLY) return statistics.getWeeklyTime();
        if (statisticsType == StatisticsType.MONTHLY) return statistics.getMonthlyTime();

        throw new WoohaengshiException(ErrorCode.STATISTICS_TYPE_NOT_FOUND);
    }

    private Statistics findStatisticsByMemberId(Long memberId) {
        return statisticsRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new WoohaengshiException(ErrorCode.STATISTICS_NOT_FOUND));
    }

    private StudyRecord findStudyRecordByMemberId(Long memberId) {
        return studyRecordRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new WoohaengshiException(ErrorCode.INVALID_INPUT));
    }

    @Override
    public void updateDailyStatistics() {

    }

    @Override
    public void updateWeeklyStatistics() {

    }

    @Override
    public void updateMonthlyStatistics() {

    }

    @Override
    public void updateTotalStatistics() {

    }
}
