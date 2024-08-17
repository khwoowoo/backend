package com.woohaengshi.backend.service;

import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.dto.response.member.ShowMemberResponse;
import com.woohaengshi.backend.repository.MemberRepository;
import com.woohaengshi.backend.service.member.MemberServiceImpl;
import com.woohaengshi.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
    @Mock private MemberRepository memberRepository;
    @InjectMocks private MemberServiceImpl memberService;

    @Test
    void 회원_정보를_조회한다() {
        Member member = MemberFixture.builder().build();

        given(memberRepository.existsById(member.getId())).willReturn(true);
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        ShowMemberResponse response = memberService.getMemberInfo(member.getId());

        assertAll(
                () -> verify(memberRepository, times(1)).findById(member.getId()),
                () -> assertThat(response.getName()).isEqualTo(member.getName()),
                () -> assertThat(response.getPassword()).isEqualTo(member.getPassword()),
                () -> assertThat(response.getEmail()).isEqualTo(member.getEmail()),
                () -> assertThat(response.getImage()).isEqualTo(member.getImage()),
                () -> assertThat(response.getCourse()).isEqualTo(member.getCourse().getName())
        );
    }


}
