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

### 6. (실제 브라우저 검증에서 발견) Axios의 `withXSRFToken: true`가 빠지면 로그인이 403으로 막힌다

로컬에서 실제 브라우저로 로그인을 처음 테스트했을 때 `POST /login`이 403으로
거부됐다. 원인은 Axios 1.6부터 추가된 보안 정책 때문이었다 — Axios는 기본적으로
XSRF 쿠키→헤더 자동 변환을 **같은 Origin 요청에만** 적용하고, Cross-Origin
요청(Vue `:5173` → Spring Boot `:8080`)에는 적용하지 않는다. `withXSRFToken: true`를
명시적으로 켜야 우리 SPA 구조에서도 CSRF 토큰이 실제로 헤더에 실린다.

MockMvc 통합 테스트만으로는 이 문제를 잡을 수 없었다 — MockMvc는 백엔드
필터 체인만 검증할 뿐, Axios가 실제로 어떤 헤더를 보내는지는 검증 범위 밖이기
때문이다. 실제 두 서버를 띄우고 브라우저로 로그인해본 뒤에야 발견했다. 이후
로드맵에서도 인증/CORS/CSRF처럼 "여러 계층이 맞물리는" 기능은 로컬 실기동
검증을 반드시 한 번은 거쳐야 한다는 교훈으로 남긴다.

### 7. (실제 브라우저 검증에서 발견) 세션 쿠키에 SameSite=None을 명시해야 한다

`withXSRFToken` 문제를 고친 뒤에도 로그인 후 `GET /api/me`가 계속 401이었다.
`POST /login`은 성공(세션 생성)했는데도 그랬다. 원인은 세션 쿠키
(`JSESSIONID`)의 기본 `SameSite` 값이 `Lax`였기 때문이다.

`SameSite=Lax`는 "같은 사이트로의 최상위 이동(링크 클릭 등)"에만 쿠키를
허용하고, XHR/fetch 같은 하위 요청(sub-request)에는 GET이든 POST든 무조건
차단한다. CSRF 토큰(XSRF-TOKEN)은 JS가 `document.cookie`로 직접 읽어서
헤더로 수동 전달하기 때문에 이 제약을 안 받지만(브라우저가 자동으로
Cookie 헤더에 붙이는 게 아니라 우리 코드가 값을 직접 옮겨 심는 것이라
SameSite 검사 대상이 아니다), 세션 쿠키는 브라우저가 요청마다 자동으로
붙여주는 방식이라 이 정책의 영향을 그대로 받는다.

`server.servlet.session.cookie.same-site=none` (+ `secure=true`)로
명시해서 해결했다. `SameSite=None`은 `Secure` 속성을 반드시 요구하는데,
`http://localhost`는 최신 브라우저에서 예외적으로 "안전한 컨텍스트"로
취급되어 HTTPS 없이도 정상 동작한다. 실제 배포 환경은 어차피 HTTPS를
쓰게 되므로, 이 설정은 로컬 개발용 임시방편이 아니라 배포 환경에서도
그대로 유효한 설정이다.

### 부수적으로 고친 것: 백엔드 빌드가 애초에 깨져 있었음

`SecurityConfig`/`LoginMemberPrincipal`/`S3Config`가 참조하는
`spring-boot-starter-security`, AWS S3 SDK(`software.amazon.awssdk:s3`)가
`pom.xml`에 없어서 `mvn compile`이 실패하는 상태였다. Day 1 작업을
검증하려면 애초에 빌드가 되어야 하므로 함께 추가했다. 테스트용 H2
드라이버도 `application-test.yaml`은 참조하지만 의존성이 없어서 같이 추가했다.

### 최종 결과

로컬에서 IntelliJ(실제 RDS 연결) + `npm run dev`로 실제 브라우저 로그인까지
전부 성공 확인. 6~7번 항목에서 정리한 CSRF/SameSite 버그를 순서대로
고친 뒤, 마지막으로 남은 원인은 단순 비밀번호 오타였다 — 즉 인증/CORS/CSRF
연동 자체는 완전히 정상 동작한다는 뜻이다. Day 1 목표 달성.
