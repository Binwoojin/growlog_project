package kr.co.growlog.growlog_project.service;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import kr.co.growlog.growlog_project.dto.JoinRequest;
import kr.co.growlog.growlog_project.dto.LoginRequest;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;


    @Transactional
    public void join(JoinRequest request, HttpSession session) {



        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 인증 확인
        if (!emailVerificationService.isVerified(request.getEmail(), session)) {
            throw new IllegalArgumentException("이메일 인증을 완료해주세요.");
        }

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = Member.builder().email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUserName()).nickname(request.getNickname()).profileImage(null).build();

        memberRepository.save(member);

        // 회원가입 성공 후 인증 정보 삭제
        emailVerificationService.clearVerification(session);
    }

    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public Member login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow( () -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        boolean passwordMatched = passwordEncoder.matches(request.getPassword(), member.getPassword());

        if (!passwordMatched) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }


    public Member findById(Long memberNo) {
        return memberRepository.findById(memberNo).orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }
}
