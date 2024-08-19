package com.woohaengshi.backend.controller;

import static org.springframework.http.HttpStatus.OK;

import com.woohaengshi.backend.support.ControllerTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemberControllerTest extends ControllerTest {
    @LocalServerPort private int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Test
    void 회원_정보를_조회한다() {
        baseRestAssuredWithAuth()
                .when()
                .get("/api/v1/members")
                .then()
                .log()
                .all()
                .statusCode(OK.value());
    }
}
