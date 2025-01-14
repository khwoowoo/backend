package com.woohaengshi.backend.service.password;

import static com.woohaengshi.backend.exception.ErrorCode.AUTHENTICATE_CODE_NOT_FOUND;
import static com.woohaengshi.backend.exception.ErrorCode.CREATE_MAIL_EXCEPTION;
import static com.woohaengshi.backend.exception.ErrorCode.INCORRECT_MEMBER_INFO;
import static com.woohaengshi.backend.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.woohaengshi.backend.exception.ErrorCode.QUIT_MEMBER;

import com.woohaengshi.backend.domain.member.Course;
import com.woohaengshi.backend.domain.member.Member;
import com.woohaengshi.backend.dto.request.password.ChangePasswordRequest;
import com.woohaengshi.backend.dto.request.password.SendMailRequest;
import com.woohaengshi.backend.exception.WoohaengshiException;
import com.woohaengshi.backend.repository.AuthenticationCode;
import com.woohaengshi.backend.repository.AuthenticationCodeRepository;
import com.woohaengshi.backend.repository.MemberRepository;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final JavaMailSender mailSender;
    private final MemberRepository memberRepository;
    private final AuthenticationCodeRepository authenticationCodeRepository;
    private static final String WOOHAENGSHI_MAIL = "woohaengshi@gmail.com";
    private static final String CONTENT_FORMAT =
            "[우행시] 비밀번호 재설정을 위한 임시 번호 발급 안내\n"
                    + "\n"
                    + "안녕하세요, %s님.\n"
                    + "\n"
                    + "비밀번호를 잊으셨나요? 아래의 임시 번호를 사용하여 비밀번호를 재설정할 수 있습니다.\n"
                    + "\n"
                    + "임시 번호: [%s]\n"
                    + "\n"
                    + "이 번호는 30분 동안만 유효하며, 타인과 공유하지 않도록 주의해 주세요. 새로운 비밀번호로 변경하려면 아래의 링크를 클릭하세요.\n"
                    + "\n"
                    + "비밀번호 재설정 링크: [%s]\n"
                    + "\n"
                    + "만약 이 요청을 본인이 하지 않았다면, 즉시 저희에게 알려주세요.\n"
                    + "\n"
                    + "감사합니다.\n"
                    + "우행시 팀 드림\n";
    private static final String TITLE = "[우행시] 비밀번호 재설정을 위한 임시 번호 발급 안내";
    private static final String REISSUE_FORMAT = "https://woohangshi.vercel.app/password/%s";

    @Override
    public void sendMail(SendMailRequest request) {
        Member member = findMemberByEmail(request.getEmail());
        validateMemberInformation(request, member);
        validQuitMember(member);
        AuthenticationCode authenticationCode = new AuthenticationCode(member.getId());
        authenticationCodeRepository.save(authenticationCode);
        MimeMessage mimeMessage = createMail(member, authenticationCode);
        mailSender.send(mimeMessage);
    }

    private void validateMemberInformation(SendMailRequest request, Member member) {
        if (!member.getName().equals(request.getName())
                || !member.getCourse().equals(Course.from(request.getCourse()))) {
            throw new WoohaengshiException(INCORRECT_MEMBER_INFO);
        }
    }

    private MimeMessage createMail(Member member, AuthenticationCode authenticationCode) {
        String reissueLink = String.format(REISSUE_FORMAT, authenticationCode.getCode());
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            mimeMessage.setFrom(WOOHAENGSHI_MAIL);
            mimeMessage.setSubject(TITLE);
            mimeMessage.setText(
                    String.format(
                            CONTENT_FORMAT,
                            member.getName(),
                            authenticationCode.getCode(),
                            reissueLink));
            mimeMessage.setRecipients(Message.RecipientType.TO, member.getEmail());
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
            throw new WoohaengshiException(CREATE_MAIL_EXCEPTION);
        }
        return mimeMessage;
    }

    private void validQuitMember(Member member) {
        if (!member.isActive()) {
            throw new WoohaengshiException(QUIT_MEMBER);
        }
    }

    private Member findMemberByEmail(String email) {
        return memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new WoohaengshiException(MEMBER_NOT_FOUND));
    }

    @Override
    public void changePassword(String code, ChangePasswordRequest request) {
        AuthenticationCode authenticationCode = findAuthenticationCode(code);
        Member member = findMemberById(authenticationCode.getMemberId());
        member.changePassword(request.getPassword());
    }

    private Member findMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new WoohaengshiException(MEMBER_NOT_FOUND));
    }

    private AuthenticationCode findAuthenticationCode(String authenticationNumber) {
        return authenticationCodeRepository
                .findById(authenticationNumber)
                .orElseThrow(() -> new WoohaengshiException(AUTHENTICATE_CODE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCode(String authenticationCode) {
        if (!authenticationCodeRepository.existsById(authenticationCode)) {
            throw new WoohaengshiException(AUTHENTICATE_CODE_NOT_FOUND);
        }
    }
}
