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

### 실제 RDS 데이터 기준 검증 (사용자가 DBeaver로 직접 확인)

이 환경에서는 RDS에 접속이 안 돼서 Playwright로 API 응답을 모킹한 화면만
확인할 수 있었다. 실제 데이터와 일치하는지는 사용자가 로컬에서 DBeaver로
RDS에 직접 붙어 검증했다.

방법: `GET /api/dashboard`가 호출하는 4개 값 각각에 대응하는 SQL을 Service
로직과 동일한 조건(이번 주 월요일 기준, 이번 달 1일 기준 등)으로 작성해서
전달하고, 화면에 뜨는 값과 SQL 결과를 하나씩 대조했다.

검증 결과 (member_no=1, 테스트 계정):
- 이번 달 기록 수: SQL 0 = 화면 0
- 진행 중 목표 수: SQL 0 = 화면 0
- 연속 기록: 출석일이 7/28, 7/30뿐이라 오늘/어제와 이어지지 않아 계산상
  0일이 나와야 하는데, 화면도 0일로 일치
- Recent Timeline: 이번 달 데이터가 없어 SQL도 0건, 화면은 카드 없이
  Empty State 문구("아직 이번 달 기록이 없어요...")를 정상 표시

4개 항목 모두 일치 확인. 다만 테스트 계정에 최근 데이터가 없어서 전부
0/빈 상태로만 검증됐다 — "값이 있을 때도 정확히 맞는지"는 아직 실데이터로
확인 못 했다. Goal/Record 작성 화면이 아직 JSP에만 있어서(Vue 전환은
Day 8~9, 13), 실제로 값이 있는 케이스를 보려면 JSP 화면에서 데이터를
등록해보거나 Day 8 이후에 재검증이 필요하다.

---

## master 병합 (2026-09-14) — 저위험 항목만 선별 반영

`claude/keen-darwin-2f77lc`와 `master`가 공통 조상(2026-07-30) 이후 각자
7개/20개 커밋으로 갈라져 있어서, 전체 병합 전에 파일 단위로 위험도를
분류했다. 이번엔 그중 **가장 안전한 항목만** 먼저 반영하고, 나머지는
의도적으로 손대지 않았다.

### 가져온 것 (master → 이 브랜치)

- **`pom.xml`**: AWS S3 SDK 버전을 `2.29.52` → `2.31.67`로 올림. master가
  독립적으로 이 버전을 채택했고, 우리 쪽엔 이 버전을 고정해야 할 이유가
  없어서 최신 쪽에 맞췄다. `spring-security-test`(CSRF 테스트용, master엔
  없음)는 그대로 유지.
- **`application.yaml` — `spring.jpa.open-in-view: false`**: Open Session
  In View 안티패턴을 끈다. master가 독립적으로 같은 결론에 도달한 걸
  보고 반영. 기존 `database-platform` 설정과 무관한 설정이라 그대로 뒀다.
- **`application.yaml` — `spring.servlet.multipart`**: 이미지 업로드 요청
  크기 제한(`max-file-size: 5MB`, `max-request-size: 25MB`). 리뉴얼과
  무관하고 순수하게 누락되어 있던 설정이라 반영.

### 의도적으로 제외한 것

- **`SecurityConfig.java` 전체** — master는 CORS 설정이 없고 CSRF를
  `disable()`로 완전히 꺼둔, 우리와 근본적으로 다른 구조다. Day 1~4에서
  맞춘 CORS/CSRF-쿠키/SameSite 구조를 그대로 유지해야 하므로 손대지 않음.
- **`application.yaml`의 CORS/세션 쿠키 관련 설정** — 위와 동일한 이유.
- **`server.port`/`server.address`** — 이번 선별 반영 범위(S3, multipart,
  open-in-view)에 포함되지 않아서 제외. 필요해지면 별도로 검토.
- **`MemberController.java`, `HomeController.java`, `MyPageController.java`**
  — master가 `@AuthenticationPrincipal` 대신 `HttpSession.getAttribute
  ("loginMember")` 레거시 패턴으로 되돌아가 있다. 우리 쪽엔 그 세션
  속성을 채워주는 `LoginSuccessHandler`가 없어서, 그대로 가져오면 로그인
  후에도 로그인 안 된 것처럼 동작하는 회귀가 생긴다. `LoginSuccessHandler`
  도입 여부를 포함해서 별도로 검토해야 할 대상으로 남겨둔다.
- **뱃지/타임라인 삭제, Goal/Record/MyPage 관련 JSP·CSS·JS 다수** — 아직
  세부 검토 전. 이번 작업 범위 밖.

빌드 및 전체 테스트(28개) 통과 확인. 변경 파일은 `pom.xml`,
`application.yaml` 단 2개뿐이고, `SecurityConfig.java`는 diff 없음.

---

## activeGoalCount 라벨 정합성 (2026-09-14)

Dashboard의 "진행 중 목표" 카드가 실제로는 `GoalService.
countThisWeekInProgressGoals()`를 재사용한 값이라 "전체 진행중 목표"가
아니라 "이번 주에 등록한 진행중 목표"였다. 라벨과 실제 집계 의미가
어긋나 있었다.

새 집계 로직을 만들지 않고 고치는 방법을 먼저 검토했다. 기존 JSP
`home.jsp`를 다시 보니 **같은 값을 같은 이름("진행 중인 목표")으로
보여주면서, 숫자 아래 작은 문구("이번 주 목표를 이어가고 있어요.")로
범위를 명시**하는 방식을 이미 쓰고 있었다 — 즉 이 애매함은 새로 생긴
문제가 아니라 원래 JSP 설계에도 있던 것이고, JSP는 라벨이 아니라
부연 문구로 풀어낸 것이었다.

그래서 Vue Dashboard도 같은 패턴을 그대로 가져왔다:
- 라벨을 "진행 중 목표" → "진행 중인 목표"로 JSP와 동일하게 맞춤
- 숫자 아래에 JSP와 완전히 같은 문구("이번 주 목표를 이어가고 있어요.")를
  `summary-card__hint`로 추가
- "이번 달 기록" 카드도 JSP의 "꾸준히 기록하고 있어요." 문구를 그대로 가져와
  톤을 통일

백엔드/Service 변경 없음 — `DashboardView.vue` 텍스트만 수정했다.
"연속 기록" 카드는 JSP가 `attendedToday` 여부에 따라 다른 문구를
보여주는데, 이 값은 아직 `DashboardResponse`에 없어서(순수 라벨 수정
범위를 벗어나므로) 이번엔 손대지 않았다 — 필요해지면 별도로 검토.

---

## Day 5 (2026-09-15) — Timeline + TypeScript 설계

### normalizeTimeline()을 프론트에서 새로 만들지 않았다

로드맵 원안은 "Goal/Record를 각각 조회 → 프론트에서 normalizeTimeline()으로
병합"하는 흐름을 가정하고 있었다. 그런데 백엔드 `TimelineService.
getTimeline(memberNo, yearMonth)`가 이미 정확히 그 일을 하고 있었다
(Goal/Record를 `TimelineItem` DTO로 변환 후 `createdAt` 기준 병합·정렬).
JSP `PageController.timeline()`이 이미 이 Service를 그대로 쓰고 있어서,
새 `GET /api/timeline`도 같은 Service를 재사용해 JSON으로만 다시
포장했다 — 월 선택/보정 로직(미래 달 요청 시 이번 달로 clamp 등)까지
JSP와 동일하게 맞췄다.

결과적으로 "Goal/Record → TimelineItem[]" 변환은 프론트가 아니라
**백엔드에서 이미 끝난 상태로 내려온다.** 프론트의 `TimelineItem`
Discriminated Union 타입(`frontend/src/types/timeline.ts`)은 그 JSON을
그대로 받아서 타입 안전하게 `type` 필드로 분기하는 역할만 한다. 병합
로직을 새로 만들지 않고 기존 Service를 재사용한다는 이번 리뉴얼의
원칙과도 맞는 선택이라고 판단했다.

### Discriminated Union을 실제로 쓰는 이유

지금은 `GoalTimelineItem`과 `RecordTimelineItem`의 필드가 완전히
동일하다(둘 다 title/content/createdAt/detailUrl). 그래서 얼핏 Union을
쓸 이유가 없어 보일 수 있는데, 굳이 유지한 이유는:
1. 백엔드 `TimelineItem` DTO 자체가 두 도메인을 하나의 구조로 뭉뚱그린
   것이라, 이후 목표에만 `progress`, 기록에만 `mood` 같은 필드가 추가될
   가능성이 높다(로드맵 12번 항목의 원래 예시가 그렇다). 그때 Union이면
   `item.type === 'GOAL'`로 분기한 블록 안에서 TS가 자동으로
   `GoalTimelineItem`으로 좁혀줘서 안전하게 확장할 수 있다.
2. `types/dashboard.ts`의 `DashboardSummary.recentTimeline`도 이 타입을
   그대로 재사용하도록 정리해서, Dashboard 미리보기와 Timeline 전체
   화면이 같은 타입을 공유한다.

### Day 6로 미룬 것

Loading/Error/Empty 상태는 Day 4 Dashboard와 동일하게 최소 텍스트로만
처리했고, 필터(전체/목표/기록)는 아직 없다. 로드맵상 Day 6이 "Timeline
UX 완성" 담당이라 그쪽에서 함께 다듬을 계획이다.

---

## Day 6 (2026-09-15) — Timeline UX 완성

### Skeleton을 새 공통 컴포넌트로 뺀 이유

로드맵 13번 섹션(권장 폴더 구조)에 원래 `components/common/
LoadingSkeleton.vue`가 명시되어 있었고, Day 6 체크리스트에도 명시적으로
Skeleton이 들어있어서 "필요성이 명확한 경우"로 판단해 새로 만들었다.
Dashboard도 같은 로딩 스켈레톤을 그대로 재사용하도록 바꿔서(기존엔
"불러오는 중..." 텍스트였다), 두 화면의 로딩 UX가 통일됐다.

### 필터는 서버 재요청 없이 클라이언트에서만 처리

전체/목표/기록 필터를 누를 때마다 `GET /api/timeline`을 다시 부르지
않고, 이미 받아온 `timelineItems`를 `computed`로 걸러서 보여준다.
한 달치 데이터량이 크지 않고(대시보드/타임라인 모두 월 단위 스코프),
새 API 파라미터나 백엔드 변경 없이 필터를 구현할 수 있어서 이 방식을
택했다. 필터 문구도 상황별로 다르게 뒀다 — "아직 이번 달 기록이
없어요"(전체가 비었을 때)와 "이번 달 등록한 목표가 없어요"(목표만
필터링했는데 없을 때)를 구분해서, 빈 상태가 "원래 데이터가 없는 것"인지
"필터링해서 안 보이는 것"인지 헷갈리지 않게 했다.

Playwright로 목표 필터를 실제로 클릭해서 성장 기록 카드가 화면에서
사라지고 선택된 필터 버튼 색이 바뀌는 것까지 확인했다.
