package com.woohaengshi.backend.controller;

import static com.woohaengshi.backend.exception.ErrorCode.INVALID_INPUT;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import com.woohaengshi.backend.dto.request.member.ChangePasswordRequest;
import com.woohaengshi.backend.dto.request.member.MemberRequest;
import com.woohaengshi.backend.support.ControllerTest;

import org.junit.jupiter.api.Test;

class MemberControllerTest extends ControllerTest {

    @Test
    void 비밀번호를_변경_할_수_있다() {
        ChangePasswordRequest request =
                new ChangePasswordRequest("password12!@", "newPassword12!@");
        baseRestAssuredWithAuth()
                .body(request)
                .when()
                .post("/api/v1/members")
                .then()
                .log()
                .all()
                .statusCode(OK.value());
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

    @Test
    void 비밀번호_형식이_다른_경우_예외() {
        ChangePasswordRequest request = new ChangePasswordRequest("password12", "newPassword!@");
        baseRestAssuredWithAuth()
                .body(request)
                .when()
                .post("/api/v1/members")
                .then()
                .log()
                .all()
                .statusCode(INVALID_INPUT.getStatus().value());
    }

    @Test
    void 회원은_탈퇴할_수_있다() {
        baseRestAssuredWithAuth()
                .when()
                .delete("/api/v1/members")
                .then()
                .log()
                .all()
                .statusCode(NO_CONTENT.value());
    }

    @Test
    void 회원_정보를_수정할_수_있다() {
        MemberRequest request = new MemberRequest("김수정", "클라우드 엔지니어링");
        baseRestAssuredWithAuth()
                .body(request)
                .when()
                .patch("/api/v1/members")
                .then()
                .log()
                .all()
                .statusCode(OK.value());
    }
}
