# GrowLog 리뉴얼 — 2주 실행 체크리스트

`docs/renewal-roadmap.md` (v3 전체 로드맵) 중 **17. 2주 압축 버전** 기준으로,
실제 작업을 시작할 때 하루 단위로 그대로 따라갈 수 있도록 만든 실행용 체크리스트.

- 기준: Week1(Day1~7) + Week2(Day8~14), Week3(Should/배포/README/인터뷰 준비)는 2주 안에 녹여서 진행
- Cut 대상(Record 작성/수정/삭제, 회원가입, 이메일 인증, S3 고도화, 복잡한 테스트, 추가 애니메이션)은 시간 남으면만 고려
- 매일 끝에 `docs/decisions.md`에 그날의 기술적 의사결정 한 줄 기록

---

## Week 1 — 연동 리스크 제거 + 대표 UI

### Day 1 — 프로젝트 기반 + 인증 기술 검증 ✅ (2026-09-14)
- [x] Vue 3 + Vite + TypeScript + Vue Router + Pinia + Axios 세팅 (`frontend/`)
- [x] CORS 설정 확인 (Spring Boot 쪽) — `SecurityConfig`에 Origin 화이트리스트 + credentials 허용 추가
- [x] Axios `withCredentials: true` 적용 (`frontend/src/api/axios.ts`)
- [x] Session Cookie 발급/유지 확인 — CSRF 쿠키(XSRF-TOKEN)까지 SPA가 읽을 수 있도록 전환
- [x] CSRF 동작 확인 — 쿠키 기반 저장소 + 매 요청 강제 로드 필터로 SPA 대응
- [x] **성공 기준**: Login → Session → Cookie 유지 → `GET /api/me` 성공
      — `AuthControllerTest` (MockMvc)로 자동 검증 + 로컬에서 실제 브라우저로
      IntelliJ Run + `npm run dev` 띄워서 최종 확인 완료 (2026-09-14).
      과정에서 Axios `withXSRFToken`, 세션 쿠키 `SameSite=None`, `app.s3.bucket`
      설정 누락 등 3개의 실제 버그를 브라우저 검증 중에 추가로 발견/수정함.
      자세한 배경은 `docs/decisions.md` Day 1 항목 참고.

### Day 2 — 인증 최소 흐름 완성 ✅ (2026-09-14)
- [x] Login / Logout — Day 1에 구현 완료 (`authStore.login/logout`)
- [x] 현재 사용자 조회 — Day 1에 구현 완료 (`GET /api/me`)
- [x] Router Guard — Day 1에 구현 완료, 오늘 로그인 후 원래 가려던 화면으로
      돌아가는 `?redirect=` 처리 추가 (`LoginView.vue`)
- [x] 401 / 403 처리 — Axios 응답 인터셉터(`frontend/src/api/interceptors.ts`)
      추가: 로그인된 화면에서 세션이 끊기면(401) 자동으로 로그인 화면으로
      이동시킨다. 앱 최초 로드 시의 "로그인 여부 확인"용 401은 라우터 가드가
      이미 조용히 처리하므로 중복 리다이렉트 없음.

### Day 3 — Design System + Dashboard UX 설계 ✅ (2026-09-14)
- [x] Color / Typography / Spacing 정의 — `frontend/src/styles/tokens.css`
      (CSS 변수: Primary/Success/Warning/Error/Background/Surface/Border/
      Text Primary/Secondary, 4px 기준 spacing scale)
- [x] Button / Card / Input / Badge / Modal 공통 컴포넌트 설계 —
      `frontend/src/components/common/` (`BaseButton`, `BaseCard`,
      `BaseInput`, `BaseBadge`, `BaseModal`). `LoginView.vue`를 이 컴포넌트로
      리팩터링해서 실제로 잘 조립되는지 확인함.
- [x] Dashboard 와이어프레임 (Greeting / Summary Cards / Quick Action /
      Recent Timeline) — `DashboardView.vue`. 지금은 목(mock) 데이터,
      실제 API 연동은 Day 4~5. Playwright로 데스크톱/모바일 스크린샷
      확인 완료 (여백/카드/반응형 정상).

### Day 4 — Dashboard 구현 ✅ (2026-09-14)
- [x] 실제 로그인 사용자 데이터 연결 — 새 `GET /api/dashboard` (`DashboardController`)가
      기존 `HomeController.homePage()`와 동일한 Service 메서드를 재사용해서
      JSON으로 조립. Service/Repository 변경 없음.
- [x] 이번 달 기록 수 / 진행 중 Goal 수 / 연속 기록 — `growthRecordService.countThisMonthRecords`,
      `goalService.countThisWeekInProgressGoals`, `attendanceService.getAttendanceSummary().currentStreak` 재사용
- [x] Quick Action, Recent Timeline Preview — `timelineService.getTimeline()` 상위 3개 재사용.
      Quick Action 버튼은 아직 기능 없이 배치만(Goal/Record 화면은 Day 8~9, 13에 연결 예정)
- 새 공통 컴포넌트 추가 없음 — Day 3의 `BaseCard`/`BaseButton`/`BaseBadge`로 충분해서
      재사용만 함 (검토 결과 신규 컴포넌트 불필요로 판단)
- [x] **실제 RDS 데이터 기준 최종 검증 완료** (2026-09-14, DBeaver로 SQL 직접 대조) —
      이번 달 기록 수 / 진행 중 목표 수 / 연속 기록 / Recent Timeline 4개 항목
      모두 실제 DB 값과 화면이 일치함을 확인. 현재 테스트 계정(member_no=1)에
      이번 주/이번 달 데이터가 없어서 전부 0/빈 상태로 확인됐고, Empty State
      문구("아직 이번 달 기록이 없어요...")도 정상 표시됨을 확인.

### Day 5 — Timeline + TypeScript 설계
- [ ] `TimelineItem` Discriminated Union 타입 정의 (Goal / Record)
- [ ] `normalizeTimeline()` 로 Goal/Record → `TimelineItem[]` 변환

### Day 6 — Timeline UX 완성
- [ ] Loading / Error / Empty / Success / Skeleton
- [ ] 필터 (전체 / 목표 / 기록)

### Day 7 — Responsive + Buffer
- [ ] Desktop / Tablet / Mobile 대응
- [ ] Card Width / Grid / Spacing / Typography / Navigation / Overflow 점검
- [ ] 밀린 작업 버퍼로 사용

---

## Week 2 — Goal 집중 + Record Read + 마무리

### Day 8 — Goal List
- [ ] `GoalCard` / `GoalProgress` / `GoalStatusBadge` 컴포넌트

### Day 9 — Goal 작성
- [ ] 입력 오류 처리
- [ ] 저장 중 / 저장 성공 / 저장 실패 상태

### Day 10 — Goal 수정 / 삭제
- [ ] `BaseModal`
- [ ] `ConfirmDialog`

### Day 11 — Goal UI/UX 고도화
- [ ] Progress 표현, Status Badge
- [ ] Empty State / Loading / Error / Success Feedback
- [ ] Mobile Layout 점검

### Day 12 — State Management Review
- [ ] `authStore` / `goalStore` / `timelineStore` 구조 점검
- [ ] "이 State가 정말 전역이어야 하는가?" 질문으로 Pinia vs Local State 재검토

### Day 13 — Record 상세 Read + 배포 준비
- [ ] Timeline → Record 클릭 → Record 상세 (작성 날짜 / 내용 / 기분 / 연결 Goal / 이미지)
- [ ] 수정/삭제는 만들지 않음
- [ ] Vercel 배포 세팅 시작 (env variable, API URL 등)

### Day 14 — Quality Review + 배포 + README
- [ ] 새 기능 추가 금지, 품질 점검만
  - [ ] UI 일관성 / 반응형 / 컴포넌트 중복
  - [ ] TypeScript `any` 제거 / `console.log` 제거
  - [ ] API 구조 / Error 처리 / Loading 처리 / Empty State / 접근성
- [ ] 실제 배포 도메인에서 CORS / Cookie / HTTPS / Session / CSRF 최종 검증
- [ ] README 작성 (프로젝트 소개, Before/After, Architecture, 기술 스택, 기술적 의사결정, UI/UX 개선, 상태관리·TS 설계)
- [ ] 스크린샷/GIF 추가

> **여기까지가 2주 기준 포트폴리오 완성선.**
> 시간이 남으면 Should 항목(Record 작성, 회원가입, 이메일 인증, Vitest) 중 우선순위 높은 것부터 추가.

---

## 참고
- 전체 배경/의사결정 근거는 `docs/renewal-roadmap.md` 참고
- 매일 작업 종료 시 `docs/decisions.md`에 "왜 이렇게 했는지" 한 줄씩 기록해두면 Day14 README/면접 준비에 그대로 재사용 가능
