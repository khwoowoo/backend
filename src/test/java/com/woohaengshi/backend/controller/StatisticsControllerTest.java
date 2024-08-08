package com.woohaengshi.backend.controller;

import com.woohaengshi.backend.domain.member.Course;
import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.member.State;
import com.woohaengshi.backend.domain.statistics.Statistics;
import com.woohaengshi.backend.repository.MemberRepository;
import com.woohaengshi.backend.repository.StatisticsRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StatisticsControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    private Member member1;
    private Member member2;
    private Statistics statistics1;
    private Statistics statistics2;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void getRanking_정상_요청() {
        RestAssured.given()
                .log().all()
                .param("page", 0)
                .param("type", "DAILY")
                .param("size", 10)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/rank")
                .then()
                .log().all().statusCode(200);
    }
}
