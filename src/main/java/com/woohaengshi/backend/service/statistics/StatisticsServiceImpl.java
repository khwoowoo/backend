package com.woohaengshi.backend.service.statistics;

import com.woohaengshi.backend.constant.StandardTimeConstant;
import com.woohaengshi.backend.domain.StudyRecord;
import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.statistics.Statistics;
import com.woohaengshi.backend.domain.statistics.StatisticsType;
import com.woohaengshi.backend.dto.response.statistics.RankDataResponse;
import com.woohaengshi.backend.dto.response.statistics.ShowRankSnapshotResponse;
import com.woohaengshi.backend.exception.ErrorCode;
import com.woohaengshi.backend.exception.WoohaengshiException;
import com.woohaengshi.backend.repository.statistics.StatisticsRepository;
import com.woohaengshi.backend.repository.studyrecord.StudyRecordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final StudyRecordRepository studyRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public ShowRankSnapshotResponse showRankData(
            Long memberId, StatisticsType statisticsType, Pageable pageable) {
        return statisticsType == StatisticsType.DAILY
                ? handleDailyStatistics(memberId, pageable)
                : handlePeriodicStatistics(memberId, statisticsType, pageable);
    }

    private ShowRankSnapshotResponse handleDailyStatistics(Long memberId, Pageable pageable) {
        LocalDate today = getShowDate();
        Slice<StudyRecord> rankSlice = getDailyRankDataSlice(today, pageable);

        if (pageable.getPageNumber() > 0) {
            return ShowRankSnapshotResponse.of(
                    rankSlice.hasNext(), calculateDailyRank(rankSlice, pageable));
        }

        Optional<StudyRecord> studyRecord =
                studyRecordRepository.findByDateAndMemberId(today, memberId);
        int rank =
                studyRecord
                        .map(
                                record ->
                                        studyRecordRepository.findRankByDate(
                                                today, record.getTime()))
                        .orElse(0);
        int time = studyRecord.map(StudyRecord::getTime).orElse(0);

        return buildRankSnapshotResponse(
                findStatisticsByMemberId(memberId), rank, time, rankSlice, pageable);
    }

    private LocalDate getShowDate() {
        LocalTime nowTime = LocalTime.now();
        LocalDate today = LocalDate.now();

        if (nowTime.isBefore(StandardTimeConstant.STANDARD_TIME)) {
            return today.minusDays(1);
        }

        return today;
    }

    private ShowRankSnapshotResponse handlePeriodicStatistics(
            Long memberId, StatisticsType statisticsType, Pageable pageable) {
        Slice<Statistics> rankSlice = getPeriodicRankDataSlice(statisticsType, pageable);

        if (pageable.getPageNumber() > 0) {
            return ShowRankSnapshotResponse.of(
                    rankSlice.hasNext(),
                    calculatePeriodicRank(rankSlice, pageable, statisticsType));
        }

        Statistics statistics = findStatisticsByMemberId(memberId);
        int studyTime = getTimeByStatisticsType(statisticsType, statistics);
        int rank =
                studyTime > 0
                        ? (int) statisticsRepository.getMemberRank(statisticsType, statistics)
                        : 0;

        return buildRankSnapshotResponse(
                statistics, rank, studyTime, rankSlice, pageable, statisticsType);
    }

    private ShowRankSnapshotResponse buildRankSnapshotResponse(
            Statistics statistics,
            int rank,
            int time,
            Slice<StudyRecord> rankSlice,
            Pageable pageable) {
        return ShowRankSnapshotResponse.of(
                statistics.getMember(),
                rank,
                time,
                statistics.getTotalTime(),
                rankSlice.hasNext(),
                calculateDailyRank(rankSlice, pageable));
    }

    private ShowRankSnapshotResponse buildRankSnapshotResponse(
            Statistics statistics,
            int rank,
            int time,
            Slice<Statistics> rankSlice,
            Pageable pageable,
            StatisticsType statisticsType) {
        return ShowRankSnapshotResponse.of(
                statistics.getMember(),
                rank,
                time,
                statistics.getTotalTime(),
                rankSlice.hasNext(),
                calculatePeriodicRank(rankSlice, pageable, statisticsType));
    }

    public Slice<Statistics> getPeriodicRankDataSlice(
            StatisticsType statisticsType, Pageable pageable) {
        return statisticsRepository.findStatisticsByTypeSortedByTimeDesc(statisticsType, pageable);
    }

    public Slice<StudyRecord> getDailyRankDataSlice(LocalDate targetDate, Pageable pageable) {
        return studyRecordRepository.findStudyRecordsByDateSortedByTimeDesc(targetDate, pageable);
    }

    private List<RankDataResponse> calculateDailyRank(
            Slice<StudyRecord> rankSlice, Pageable pageable) {
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        return IntStream.range(0, rankSlice.getNumberOfElements())
                .mapToObj(
                        index -> {
                            StudyRecord studyRecord = rankSlice.getContent().get(index);
                            Member member = studyRecord.getMember();
                            Statistics statistics = findStatisticsByMemberId(member.getId());

                            return RankDataResponse.of(
                                    member,
                                    startRank + index,
                                    studyRecord.getTime(),
                                    statistics.getTotalTime());
                        })
                .toList();
    }

    private List<RankDataResponse> calculatePeriodicRank(
            Slice<Statistics> rankSlice, Pageable pageable, StatisticsType statisticsType) {
        int startRank = pageable.getPageNumber() * pageable.getPageSize() + 1;

        return IntStream.range(0, rankSlice.getNumberOfElements())
                .mapToObj(
                        index -> {
                            Statistics statistics = rankSlice.getContent().get(index);
                            Member member = statistics.getMember();

                            return RankDataResponse.of(
                                    member,
                                    startRank + index,
                                    getTimeByStatisticsType(statisticsType, statistics),
                                    statistics.getTotalTime());
                        })
                .toList();
    }

    private int getTimeByStatisticsType(StatisticsType statisticsType, Statistics statistics) {
        if (statisticsType == StatisticsType.WEEKLY) return statistics.getWeeklyTime();
        if (statisticsType == StatisticsType.MONTHLY) return statistics.getMonthlyTime();
        throw new WoohaengshiException(ErrorCode.STATISTICS_TYPE_NOT_FOUND);
    }

    private Statistics findStatisticsByMemberId(Long memberId) {
        return statisticsRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new WoohaengshiException(ErrorCode.STATISTICS_NOT_FOUND));
    }
}
