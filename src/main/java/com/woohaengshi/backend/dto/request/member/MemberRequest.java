package com.woohaengshi.backend.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberRequest {

    @NotBlank(message = "이름은 필수 입니다.")
    private String name;

    @NotBlank(message = "과정은 필수 입니다.")
    private String course;

    public MemberRequest(String name, String course) {
        this.name = name;
        this.course = course;
    }

    private MemberRequest() {
    }
}
