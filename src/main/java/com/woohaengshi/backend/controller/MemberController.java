package com.woohaengshi.backend.controller;

import com.woohaengshi.backend.controller.auth.CookieProvider;
import com.woohaengshi.backend.controller.auth.MemberId;
import com.woohaengshi.backend.dto.request.member.ChangePasswordRequest;
import com.woohaengshi.backend.dto.response.member.ShowMemberResponse;
import com.woohaengshi.backend.service.member.MemberService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.woohaengshi.backend.controller.auth.CookieProvider.REFRESH_TOKEN;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final CookieProvider cookieProvider;

    @PostMapping
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request, @MemberId Long memberId) {
        memberService.changePassword(request, memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ShowMemberResponse showMemberInfo(@MemberId Long memberId) {
        return memberService.getMemberInfo(memberId);
    }

    @DeleteMapping
    public ResponseEntity<Void> quit(
            @MemberId Long memberId,
            @CookieValue(name = REFRESH_TOKEN, required = false) String refreshToken) {
        memberService.quit(memberId, refreshToken);
        return ResponseEntity.noContent()
                .header(SET_COOKIE, cookieProvider.createSignOutCookie().toString())
                .build();
    }
}
