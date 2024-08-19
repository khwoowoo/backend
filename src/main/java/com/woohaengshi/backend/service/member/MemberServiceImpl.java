package com.woohaengshi.backend.service.member;

import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.dto.response.member.ShowMemberResponse;
import com.woohaengshi.backend.exception.ErrorCode;
import com.woohaengshi.backend.exception.WoohaengshiException;
import com.woohaengshi.backend.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public ShowMemberResponse getMemberInfo(Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new WoohaengshiException(ErrorCode.MEMBER_NOT_FOUND));

        return ShowMemberResponse.of(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getImage(),
                member.getCourse().getName());
    }
}
