package kr.co.growlog.growlog_project.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.growlog.growlog_project.entity.Member;
import kr.co.growlog.growlog_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * Spring Security 로그인 성공 직후 실행되는 Handler
 *
 * 현재 프로젝트의 Controller들은 로그인 회원을 다음 세션 속성에서 조회한다.
 * "session.getAttribute("loginMember")"
 * 그러나 Spring Security는 인증 정보를 "loginMember"라는 이름으로 저장하지 않고
 * SecurityContext 내부에 Authentication 객체로 저장한다.
 *
 * 따라서 Spring Security는 로그인으로 전환하자마자 기존 Controller들은
 * 사용자가 로그인하지 않았다고 판단할 수 있다.
 *
 * 이 Handler는 Controller를 Principal 방식으로 모두 변경하기 전까지
 * 기존 세션 인증 코드가 계속 동작하도록 만들어주는 임시 호환 코드
 *
 * 모든 Controller에서 loginMember 세션 사용을 제거한 후에는
 * 이 클래스와 SecurityConfig의 successHandler 설정도 제거해야 한다.
 */
@RequiredArgsConstructor
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    /*
     * 인증 사용자 번호를 이용해 최신 Member 정보를 조회하기 위해 사용
     */
    private final MemberService memberService;

    /*
     * Spring Security 인증에 성공한 직후 호출
     *
     * @param request 현재 로그인 HTTP 요청
     * @param response 로그인 성공 후 응답
     * @param authentication 인증에 성공한 사용자 정보
     */

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        /*
         * CustomUserDetailService가 반환했던 LoginMemberPrincipal은
         * 인증 성공 후 Authentication의 principal에 저장
         */
        LoginMemberPrincipal principal = (LoginMemberPrincipal) authentication.getPrincipal();

        /*
         * LoginMemberPrincipal에는 인증에 필요한 최소 정보만 들어 있다.
         *
         * 기존 Controller는 세션에서 Member Entity를 꺼내 사용하고 있으므로
         * Principal의 회원 번호를 이용해 최신 Member 정보를 DB에서 조회
         *
         * 이 조회와 세션 저장은 기존 Controller를
         * @AuthenticationPrincipal 방식으로 변경하기 전까지만 사용하는
         * 임시 호환 처리다
         */
        Member loginMember = memberService.findById(principal.getMemberNo());

        /*
         * 기존 Controller들이 아직 이 세션 속성을 사용하기 때문에
         * 마이그레이션이 끝날 때까지만 임시로 저장
         */
        request.getSession().setAttribute("loginMember", loginMember);

        /*
         * 로그인 성공 후 홈 화면으로 이동
         *
         * sendRedirect는 새로운 GET /home 요청을 발생시킨다
         */
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
