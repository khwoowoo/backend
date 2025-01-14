package com.woohaengshi.backend.repository.statistics;

import static com.woohaengshi.backend.domain.statistics.QStatistics.*;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woohaengshi.backend.domain.statistics.Statistics;
import com.woohaengshi.backend.domain.statistics.StatisticsType;
import com.woohaengshi.backend.exception.ErrorCode;
import com.woohaengshi.backend.exception.WoohaengshiException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class StatisticsCustomRepositoryImpl implements StatisticsCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public long getMemberRank(StatisticsType statisticsType, Statistics memberStatistics) {
        NumberPath<Integer> timePath =
                Expressions.numberPath(Integer.class, statistics, statisticsType.getFieldName());
        int time = getStatisticsTime(statisticsType, memberStatistics);

        return jpaQueryFactory
                        .selectFrom(statistics)
                        .where(timePath.gt(time).and(timePath.ne(0)))
                        .fetch()
                        .size()
                + 1;
    }

    private int getStatisticsTime(StatisticsType statisticsType, Statistics statistics) {
        if (statisticsType == StatisticsType.WEEKLY) return statistics.getWeeklyTime();
        if (statisticsType == StatisticsType.MONTHLY) return statistics.getMonthlyTime();

        throw new WoohaengshiException(ErrorCode.STATISTICS_TYPE_NOT_FOUND);
    }

    public Slice<Statistics> findStatisticsByTypeSortedByTimeDesc(
            StatisticsType statisticsType, Pageable pageable) {
        NumberPath<Integer> timePath =
                Expressions.numberPath(Integer.class, statistics, statisticsType.getFieldName());

        List<Statistics> content =
                jpaQueryFactory
                        .selectFrom(statistics)
                        .where(timePath.ne(0))
                        .orderBy(timePath.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        long total =
                jpaQueryFactory
                        .select(timePath.count())
                        .from(statistics)
                        .where(timePath.ne(0))
                        .fetchOne();
        boolean hasNext = pageable.getOffset() + pageable.getPageSize() < total;
        return new SliceImpl<>(content, pageable, hasNext);
    }
}
