package com.woohaengshi.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.statistics.Statistics;
import com.woohaengshi.backend.domain.statistics.StatisticsType;
import com.woohaengshi.backend.repository.statistics.StatisticsRepository;
import com.woohaengshi.backend.support.RepositoryTest;
import com.woohaengshi.backend.support.fixture.MemberFixture;
import com.woohaengshi.backend.support.fixture.StatisticsFixture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@RepositoryTest
class StatisticsRepositoryTest {

    @Autowired private StatisticsRepository statisticsRepository;
    @Autowired private MemberRepository memberRepository;

    private Member 저장(Member member) {
        return memberRepository.save(member);
    }

    private Statistics 저장(Statistics statistics) {
        return statisticsRepository.save(statistics);
    }

    @Test
    void 주단위_통계를_초기화_할_수_있다() {
        Member member = 저장(MemberFixture.builder().build());
        저장(StatisticsFixture.builder().member(member).weeklyTime(10).build());
        저장(StatisticsFixture.builder().member(member).weeklyTime(10).build());

        statisticsRepository.initWeeklyTime();

        List<Statistics> allStatistics = statisticsRepository.findAll();
        allStatistics.forEach(statistics -> assertThat(statistics.getWeeklyTime()).isEqualTo(0));
    }

    @Test
    void 월단위_통계를_초기화_할_수_있다() {
        Member member = 저장(MemberFixture.builder().build());
        저장(StatisticsFixture.builder().member(member).monthlyTime(10).build());
        저장(StatisticsFixture.builder().member(member).monthlyTime(10).build());

        statisticsRepository.initMonthlyTime();

        List<Statistics> allStatistics = statisticsRepository.findAll();
        allStatistics.forEach(statistics -> assertThat(statistics.getMonthlyTime()).isEqualTo(0));
    }


    @Test
    void 주간_월간_시간의_멤버의_등수를_조회할_수_있다(){
        Member member = 저장(MemberFixture.builder().build());

        Statistics statistics = 저장(StatisticsFixture.builder().member(member).monthlyTime(11).build());
        저장(StatisticsFixture.builder().member(member).monthlyTime(10).build());
        저장(StatisticsFixture.builder().member(member).monthlyTime(12).build());

        int memberRank = statisticsRepository.getMemberRank(StatisticsType.MONTHLY, statistics);

        assertThat(memberRank).isEqualTo(2);
    }
}
