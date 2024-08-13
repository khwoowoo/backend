package com.woohaengshi.backend.dto.request.auth;

import com.woohaengshi.backend.domain.member.Course;
import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.domain.member.State;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class SignUpRequest {

    @NotBlank(message = "이름은 필수 입니다.")
    private String name;

    @NotNull(message = "과정은 필수 입니다.")
    private Course course;

    @Email(message = "잘못된 이메일 형식입니다.")
    private String email;

    @Length(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다. ")
    @Pattern(regexp = "^[a-zA-Z]+\\d+[!@#$%^&*]+$", message = "비밀번호는 영어, 숫자, 특수문자를 포함해야합니다.")
    private String password;

    public SignUpRequest(String name, Course course, String email, String password) {
        this.name = name;
        this.course = course;
        this.email = email;
        this.password = password;
    }

    private SignUpRequest() {}

    public Member toMember(String password) {
        return Member.builder()
                .state(State.ACTIVE)
                .name(name)
                .email(email)
                .password(password)
                .course(course)
                .build();
    }
}
