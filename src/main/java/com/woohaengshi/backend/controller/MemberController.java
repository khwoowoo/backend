package com.woohaengshi.backend.controller;

import com.woohaengshi.backend.controller.auth.MemberId;
import com.woohaengshi.backend.dto.request.member.ChangePasswordRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.woohaengshi.backend.dto.response.member.ShowMemberResponse;
import com.woohaengshi.backend.service.member.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request, @MemberId Long memberId) {
        memberService.changePassword(request, memberId);
        return ResponseEntity.ok().build();

   @GetMapping
    public ShowMemberResponse showMemberInfo(@MemberId Long memberId) {
        return memberService.getMemberInfo(memberId);
    }
}
