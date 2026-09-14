# 기술적 의사결정 기록

리뉴얼 진행 중 내린 기술적 판단과 그 이유를 하루 단위로 남긴다.
목적은 README 작성과 면접에서 "왜 이렇게 했나요?"에 바로 답할 수 있도록 하는 것.

---

## Day 1 (2026-09-14) — 인증/CORS 기술 검증

### 1. CookieCsrfTokenRepository + 커스텀 필터로 CSRF 쿠키를 강제 로드

기존 프로젝트는 JSP가 `${_csrf.token}`을 직접 렌더링해서 CSRF 토큰을 세션에
저장하는 방식(`HttpSessionCsrfTokenRepository`, 기본값)을 썼다. Vue SPA는
서버가 렌더링하는 화면이 없으므로 이 방식으로는 토큰을 읽을 방법이 없다.

`CookieCsrfTokenRepository.withHttpOnlyFalse()`로 바꿔 XSRF-TOKEN 쿠키로
토큰을 내려주도록 했다. Axios는 기본적으로 이 쿠키 이름(XSRF-TOKEN)과 헤더
이름(X-XSRF-TOKEN)을 그대로 사용하므로 프론트엔드에서 별도 코드 없이 CSRF
토큰이 자동으로 요청에 실린다.

다만 Spring Security의 CsrfToken은 기본적으로 "지연 로딩"된다 — 누군가
`csrfToken.getToken()`을 실제로 호출해야 그제서야 쿠키가 응답에 실린다.
JSP는 `${_csrf.token}`을 렌더링하면서 자연스럽게 이 호출이 일어나지만,
REST API만 호출하는 SPA 흐름에는 그런 코드가 없다. 그래서 `CsrfFilter`
바로 뒤에 토큰을 강제로 한 번 읽는 작은 필터(`csrfCookieFilter`)를
추가해서 매 요청마다 쿠키가 항상 내려가도록 했다. (Spring Security 공식
"CSRF for a SPA using cookies" 가이드에서 제시하는 표준 패턴이다.)

### 2. /api/** 는 인증 실패 시 401을 반환하도록 별도 EntryPoint 등록

formLogin의 기본 동작은 미인증 요청을 `/login` 페이지로 302 리다이렉트하는
것이다. Axios로 `GET /api/me`를 호출하는 SPA 입장에서는 이 리다이렉트를
그대로 따라가면 HTML 응답을 받게 되어 로그인 여부를 코드로 구분하기 어렵다.
그래서 `/api/**` 경로에는 `HttpStatusEntryPoint(401)`을 등록해 JSON API와
서버 렌더링 페이지의 미인증 처리 방식을 분리했다.

구현 중 발견한 함정: `ExceptionHandlingConfigurer.defaultAuthenticationEntryPointFor()`로
등록한 매핑 중 "가장 먼저 등록된 것"이 전체 fallback이 된다. formLogin이
등록하는 로그인 리다이렉트 매핑은 `http.build()` 시점에 뒤늦게 추가되므로,
내가 등록한 `/api/**` 매핑이 항상 먼저 들어가 fallback이 되어버려
`/home` 같은 일반 페이지까지 401을 받는 문제가 있었다. `/api/**` 매핑과
나머지 전체를 위한 로그인 리다이렉트 매핑을 직접, 이 순서로 함께 등록해서
해결했다.

### 3. CORS는 명시적 Origin 목록 + credentials 허용

Vue 개발 서버(`localhost:5173`)와 Spring Boot(`localhost:8080`)가 다른
Origin이므로 세션 쿠키를 주고받으려면 CORS에서 `allowCredentials(true)`가
필요하다. 이 옵션을 쓰려면 `allowedOrigins`에 `"*"`를 쓸 수 없어서 허용할
Origin을 `app.cors.allowed-origins` 설정값(환경변수 `CORS_ALLOWED_ORIGINS`로
배포 시 교체 가능)으로 명시했다.

또한 CORS preflight(OPTIONS) 요청은 자격 증명 없이 오기 때문에
`authorizeHttpRequests`에서 `OPTIONS` 메서드는 인증 규칙보다 먼저
무조건 허용하도록 별도 규칙을 추가했다. 이게 없으면 모든 Cross-Origin
요청이 preflight 단계에서부터 막힌다.

### 4. 로그인 성공 여부는 POST /login 응답이 아니라 GET /api/me로 판단

Spring Security formLogin은 성공/실패 모두 302로 응답한다(성공 → `/home`,
실패 → `/login?error`). SPA에서 이 리다이렉트 자체를 파싱해서 성공 여부를
판단하는 건 취약하므로, `POST /login` 이후 항상 `GET /api/me`를 호출해서
실제로 세션이 인증된 상태인지로 로그인 성공 여부를 판단하도록 설계했다.

### 5. 검증 방법: MockMvc 기반 통합 테스트로 자동화

브라우저로 직접 CORS/쿠키 흐름을 확인하는 대신, 실제 `SecurityFilterChain`
Bean이 동작하는 MockMvc 통합 테스트(`AuthControllerTest`)로 아래 5가지를
자동 검증했다.

- 미인증 `GET /api/me` → 401 (로그인 페이지로 리다이렉트되지 않음)
- 인증된 `GET /api/me` → 200 + 로그인 회원 JSON
- 응답에 `XSRF-TOKEN` 쿠키가 실제로 내려가는지
- 허용된 Origin(`localhost:5173`)의 CORS preflight → 200 + `Access-Control-Allow-Origin` 헤더
- 허용되지 않은 Origin의 CORS preflight → `Access-Control-Allow-Origin` 헤더 없음

### 부수적으로 고친 것: 백엔드 빌드가 애초에 깨져 있었음

`SecurityConfig`/`LoginMemberPrincipal`/`S3Config`가 참조하는
`spring-boot-starter-security`, AWS S3 SDK(`software.amazon.awssdk:s3`)가
`pom.xml`에 없어서 `mvn compile`이 실패하는 상태였다. Day 1 작업을
검증하려면 애초에 빌드가 되어야 하므로 함께 추가했다. 테스트용 H2
드라이버도 `application-test.yaml`은 참조하지만 의존성이 없어서 같이 추가했다.
