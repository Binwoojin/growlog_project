package kr.co.growlog.growlog_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        /*
         * URL별 접근 권한을 설정
         *
         * 인증되지 않은 사용자가 접근하면 Controller를 실행하지 않고
         * Spring Security가 로그인 페이지로 이동시킨다
         *
         * 아직 전환하지 않은 Controller는 기존 세션 방식을
         * 사용하고 있으므로 나머지 요청은 임시로 모두 허용
         */

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/goal/**", "/record/**", "/home", "/timeline", "/mypage/**", "/api/members/check-nickname-update").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        /*
                         * 로그인 화면을 표시하는 주소
                         * GET /login은 기존 HomeController가 처리
                         */
                        .loginPage("/login")

                        /*
                         * 로그인 폼이 제출되는 주소
                         * POST /login 요청은 Controller가 아닌
                         * Spring Security의 로그인 필터가 처리
                         */
                        .loginProcessingUrl("/login")
                        /*
                         * Spring Security의 기본 사용자명 파라미터는 username이다
                         * 현재 login.jsp는 <input name="email">을 사용하므로
                         * 이메일 파라미터를 사용자 식별자로 사용하도록 변경한다
                         */
                        .usernameParameter("email")
                        /*
                         * 현재 login.jsp의 비밀번호 필드 이름
                         * 기본값도 password지만 코드의 이미를 명확히 하기 위해 작성
                         */
                        .passwordParameter("password")
                        /*
                         * 로그인 성공 시 임시 호환 Handler를 실행
                         */
                        .defaultSuccessUrl("/home", true)
                        /*
                         * 인증에 실패하면 쿼리 파라미터를 붙여 로그인 화면으로 다시 이동
                         */
                        .failureUrl("/login?error")
                        /*
                         * 비로그인 사용자도 로그인 화면과 로그인 처리 주소에
                         * 접근할 수 있어야 한다.
                         */
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/logout-complete")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll());

        /*
         * authorizeHttpRequest : URL마다 접근 권한을 설정하는 영역
         * anyRequest().permitAll() : 모든 요청을 허용
         * Logout
         * - logoutUrl("/logout") : 헤더의 POST /logout 요청을 Spring Security가 처리
         * - logoutSuccessUrl("/logout-complete") : 로그아웃 처리가 끝나면 /logout-complete로 리다이렉트한다
         * - invalidateHttpSession(true) : 기존 세션을 무효화, 현재 세션에 들어있는 loginMember도 제거된다
         * - clearAuthentication(true) : Spring Security의 인증정보도 제거
         *
         * 아직 Spring Security 로그인 기능과 프로젝트 회원 정보를 연결하지 않았기 때문에
         * 기존 기능을 유지하면서 Security 설정이 정상 등록되었는지만 확인하기 위한 설정
         */

        /*
         * CSRF 임시 비활성화
         * Security를 설치하면 POST, PUT, DELETE 요청에 CSRF 검사가 기본 적용된다.
         * 현재 JSP 폼에는 CSRF 토큰이 없기 때문에
         * 활성화할 경우 로그인, 회원가입, 목표 등록 등이 "403 Forbidden"으로 실패함
         * 인증 전환이 끝난 후 다시 활성화 예정
         */
        return http.build();
    }
}
