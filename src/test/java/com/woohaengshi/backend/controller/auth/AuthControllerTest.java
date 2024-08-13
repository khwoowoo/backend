package com.woohaengshi.backend.controller.auth;

import com.woohaengshi.backend.domain.member.Course;
import com.woohaengshi.backend.dto.request.auth.SignUpRequest;
import com.woohaengshi.backend.dto.request.studyrecord.auth.SignInRequest;
import com.woohaengshi.backend.support.ControllerTest;
import org.junit.jupiter.api.Test;

import static com.woohaengshi.backend.controller.auth.RefreshCookieProvider.REFRESH_TOKEN;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

class AuthControllerTest extends ControllerTest {

    @Test
    void 로그인이_가능하다() {
        SignInRequest request = new SignInRequest("rlfrkdms1@naver.com", "password12!@");
        baseRestAssured()
                .body(request)
                .when()
                .post("/api/v1/sign-in")
                .then()
                .log()
                .all()
                .cookie(REFRESH_TOKEN)
                .statusCode(OK.value());
    }

    @Test
    void 회원가입이_가능하다() {
        SignUpRequest request = new SignUpRequest("김혜빈", Course.CLOUD_SERVICE, "rlagpqls@naver.com", "password12!@");
        baseRestAssured()
                .body(request)
                .when()
                .post("/api/v1/sign-up")
                .then()
                .log()
                .all()
                .statusCode(CREATED.value());
    }
}
