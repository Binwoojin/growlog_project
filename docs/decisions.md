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

---

## Day 2 (2026-09-14) — 인증 최소 흐름 완성

Login/Logout/현재 사용자 조회/Router Guard는 Day 1에서 이미 구현해뒀기 때문에,
Day 2에 실제로 필요했던 건 "세션이 화면을 쓰는 도중에 끊기는 경우"에 대한
처리 하나였다.

### 401은 두 가지 상황에서 발생하고, 처리 위치가 다르다

1. **앱을 처음 열었을 때** — 로그인 여부를 몰라서 확인차 보내는 `GET /api/me`.
   이건 로그인 안 한 사용자에게는 항상, 정상적으로 401이 난다. 이 경우는
   `authStore.fetchCurrentUser()`가 자체적으로 `catch`해서 `user = null`로만
   처리하고 끝낸다 — Router Guard가 그 결과를 보고 어차피 `/login`으로
   보내줄 것이기 때문에 별도 리다이렉트가 필요 없다.
2. **이미 로그인된 화면을 쓰던 도중** — 세션 만료, 서버 재시작, 쿠키 삭제
   등으로 API 호출이 갑자기 401을 반환하는 경우. 이건 Router Guard가 잡을
   수 없다(네비게이션이 발생하지 않으니까). Axios 응답 인터셉터
   (`frontend/src/api/interceptors.ts`)를 추가해서, 현재 라우트가 `/login`이
   아닐 때만 로그인 화면으로 강제 이동시키도록 했다.

이 인터셉터를 `axios.ts` 안에 바로 넣지 않고 별도 `interceptors.ts` 파일로
분리해서 `main.ts`에서 pinia/router가 준비된 뒤에 등록한 이유: 인터셉터가
Router와 Pinia store를 둘 다 참조해야 하는데, `axios.ts <- auth.api.ts <-
auth.store.ts <- router <- axios.ts` 순으로 순환 참조가 생긴다. 인터셉터
콜백 안에서만 `router`/`useAuthStore`를 쓰면(모듈 로드 시점이 아니라 호출
시점에 접근하면) 순환 참조 자체는 동작하지만, 굳이 그 구조를 만들지 않고
`axios.ts`는 인스턴스 생성만 책임지게 하는 게 더 명확하다고 판단했다.

### 로그인 후 원래 가려던 화면으로 돌아가기

Router Guard와 401 인터셉터 둘 다 `?redirect=원래경로`를 붙여서 로그인
화면으로 보낸다. `LoginView.vue`가 로그인 성공 시 이 쿼리 파라미터를 읽어서
무조건 Dashboard로 보내지 않고 원래 가려던 화면으로 돌려보내도록 했다.

---

## Day 4 (2026-09-14) — Dashboard 실제 데이터 연결 (+ 브랜치/보안 점검)

### master와의 관계: 지금은 병합하지 않기로 결정

Day 4 시작 전 `origin/master`와의 차이를 확인했다. 공통 조상(2026-07-30)
이후 master가 7개, 이 브랜치가 18개 커밋을 각자 진행해서 갈라져 있었고,
공통 조상 이후 양쪽에서 모두 손댄 파일이 67개였다. master는 우리가 모르는
사이에 레거시 JSP 백엔드 작업이 별도로 계속된 것으로 보인다 — 특히
`SecurityConfig.java`를 독자적으로 다시 작성했는데(CORS 없음, CSRF는
`disable()`로 완전히 꺼둠, `LoginSuccessHandler` 신규 추가), 이건 Day 1에서
공들여 맞춘 CORS/CSRF/SameSite 구조와 정면으로 충돌한다. 뱃지/타임라인
관련 파일 삭제 등 다른 실질적 충돌도 많았다.

지금 병합하면 (1) 67개 파일의 실제 충돌을 수작업으로 풀어야 하고 (2) 잘못
풀면 Day 1~3 인증 구조가 깨질 위험이 있고 (3) Day 4 작업 자체엔 master의
변경사항이 전혀 필요 없다. 그래서 지금은 병합을 미루고, 이 브랜치만으로
Day 4를 진행하기로 했다. 병합은 사람이 파일 단위로 리뷰할 시간이 있을 때
(예: master의 `DB_URL` 환경변수화, `open-in-view: false`, multipart 업로드
제한처럼 리뉴얼과 무관하고 안전한 개선사항만 선별해서) 별도로 진행한다.

### DB_URL도 환경변수로 분리

기존엔 `DB_USERNAME`/`DB_PASSWORD`만 환경변수였고 RDS 호스트가 포함된
`url`은 하드코딩되어 있었다. master가 독립적으로 같은 결론(`${DB_URL}`)에
도달한 걸 보고, 우리도 반영했다. 기본값을 기존 RDS 주소로 그대로 둬서
이미 만들어둔 `env.sh`/`env.bat`는 수정 없이 계속 동작한다 — `DB_URL`을
새로 설정해야만 다른 값으로 바뀐다.

### Dashboard API는 새 로직 없이 기존 Service 재사용만으로 구성

`HomeController.homePage()`가 기존 JSP `/home` 화면에 넘기던 값들
(`goalService.countThisWeekInProgressGoals`, `growthRecordService.countThisMonthRecords`,
`attendanceService.getAttendanceSummary().currentStreak`, `timelineService.getTimeline()`)을
그대로 호출해서 `GET /api/dashboard`로 JSON 재포장만 했다. Service/Repository
변경은 전혀 없다.

`countThisWeekInProgressGoals`는 이름상 "이번 주 등록된 진행중" 목표
개수라 로드맵 문구("진행 중 목표 수")와 완전히 같은 의미는 아니지만,
기존 JSP `/home` 화면도 같은 메서드를 쓰고 있어서 그대로 재사용했다 —
그래야 JSP 홈 화면과 Vue Dashboard가 같은 숫자를 보여준다. 통계 정의를
새로 내리는 건 이번 리뉴얼 범위(백엔드 재설계 금지) 밖이라고 판단했다.

Recent Timeline Preview는 `TimelineItem` DTO(GOAL/RECORD를 합쳐서
최신순 정렬해주는 기존 DTO)를 그대로 쓰고 상위 3개만 잘라서 내려준다.
Day 5에서 전체 Timeline 화면을 만들 때 프론트엔드 타입도 그대로 확장해서
쓸 수 있도록 `frontend/src/types/dashboard.ts`의 `DashboardTimelineItem`을
백엔드 DTO 필드명과 1:1로 맞춰뒀다.

### 새 공통 컴포넌트는 추가하지 않음

Day 3에서 만든 `BaseCard`/`BaseButton`/`BaseBadge`만으로 Summary Card,
Quick Action, Timeline Preview를 전부 조립할 수 있어서 새 컴포넌트를
만들지 않았다. Loading/Error 상태는 아직 전용 컴포넌트 없이 텍스트로만
처리했다 — Skeleton 등 제대로 된 UX는 Timeline을 만드는 Day 6에서 함께
다듬을 계획이다.
