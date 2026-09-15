<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { useInViewOnce } from '../composables/useInViewOnce'
import BaseButton from '../components/common/BaseButton.vue'
import HeroSection from '../components/landing/HeroSection.vue'
import FeatureSection from '../components/landing/FeatureSection.vue'
import GrowthJourney from '../components/landing/GrowthJourney.vue'

/*
 * Landing Page — GrowLog가 어떤 서비스인지 설명하는 진입 화면.
 * 로그인 여부와 무관하게 누구나 볼 수 있다(router meta.public: true).
 * 로그인된 사용자를 강제로 Dashboard로 보내지 않고, CTA 라벨/이동 경로만
 * 로그인 상태에 맞춰 바꾼다.
 *
 * Header/Intro/Final CTA는 로직 없는 마크업이라 별도 컴포넌트로 분리하지
 * 않고 이 파일에 그대로 둔다. Hero/Feature/GrowthJourney만 분리했다.
 *
 * Header/Hero의 CTA 문구(ctaLabel)와 Final CTA의 문구(finalCtaLabel)는
 * 역할이 다르다 — Header/Hero는 "무엇인지 소개하고 바로 이동"이고, Final
 * CTA는 "성장 기록을 시작하라"는 클로징 메시지라 별도 computed로 둔다.
 */
const router = useRouter()
const authStore = useAuthStore()

const ctaLabel = computed(() => (authStore.isAuthenticated ? 'Dashboard로 이동' : '로그인하고 시작하기'))
const finalCtaLabel = computed(() => (authStore.isAuthenticated ? '내 성장 대시보드 보기' : '나의 성장 기록 시작하기'))

function goToCtaTarget() {
  router.push({ name: authStore.isAuthenticated ? 'dashboard' : 'login' })
}

/*
 * "Why GrowLog" — 문제 제기 3개 + GrowLog의 역할을 설명하는 결론 문장을
 * 카드/불릿 리스트가 아니라 번호가 붙은 editorial statement로 보여준다.
 * 01→03 번호는 장식이 아니라 읽는 순서를 지정하는 역할이라, 문장마다
 * 정렬/여백을 조금씩 다르게 줘서(01 왼쪽 끝 → 03으로 갈수록 오른쪽으로
 * 밀림) 시선이 자연스럽게 아래로 내려가며 계단처럼 읽히게 했다. 번호
 * 색은 --color-accent → --color-primary → --color-primary-hover 순으로
 * 짙어진다(Growth Journey의 노드 색 진행과 같은 언어) — 크기는 카피보다
 * 강해 보이지 않도록 작게 제한한다.
 */
const whyGrowLogPoints = [
  { number: '01', text: '계획은 세웠지만, 시간이 지나면 왜 시작했는지 잊기 쉽습니다.' },
  { number: '02', text: '하루의 변화는 작아서, 기록하지 않으면 그 과정은 금방 사라집니다.' },
  { number: '03', text: '기록이 쌓이면 내가 어떻게 달라졌는지 더 분명하게 볼 수 있습니다.' },
]

/*
 * 문장마다 개별 IntersectionObserver 대상으로 두고, 뷰포트에 들어오면
 * opacity/translateY로 한 번만 나타난다(다시 숨기지 않음/unobserve).
 * Growth Journey와 달리 여기는 rail/connector 선을 쓰지 않으므로 진행률
 * 계산이 필요 없다 — 항목별 상태만 추적하면 된다.
 */
const pointRefs = ref<(Element | null)[]>([])
const revealedPoints = ref<boolean[]>(whyGrowLogPoints.map(() => false))
const whyMotionEnabled = ref(false)

function setPointRef(el: Element | null, index: number) {
  pointRefs.value[index] = el
}

let whyObserver: IntersectionObserver | null = null

onMounted(() => {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (prefersReducedMotion) {
    revealedPoints.value = revealedPoints.value.map(() => true)
    return
  }

  whyMotionEnabled.value = true
  whyObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return
        const index = pointRefs.value.indexOf(entry.target)
        if (index === -1) return
        revealedPoints.value[index] = true
        whyObserver?.unobserve(entry.target)
      })
    },
    { threshold: 0.4, rootMargin: '0px 0px -10% 0px' },
  )
  pointRefs.value.forEach((el) => el && whyObserver?.observe(el))
})

onBeforeUnmount(() => {
  whyObserver?.disconnect()
  whyObserver = null
})

const {
  target: finalCtaTarget,
  isVisible: finalCtaVisible,
  motionEnabled: finalCtaMotion,
} = useInViewOnce()
</script>

<template>
  <div class="landing">
    <header class="landing-header">
      <span class="landing-header__logo">GrowLog</span>
      <nav class="landing-header__nav" aria-label="페이지 내 이동">
        <a href="#intro">GrowLog 소개</a>
        <a href="#features">주요 기능</a>
      </nav>
      <BaseButton variant="secondary" @click="goToCtaTarget">
        {{ authStore.isAuthenticated ? 'Dashboard로 이동' : '로그인' }}
      </BaseButton>
    </header>

    <HeroSection :cta-label="ctaLabel" @cta="goToCtaTarget" />

    <section id="intro" class="intro">
      <h2 class="intro__title">성장은 눈에 잘 보이지 않습니다.</h2>

      <div class="intro__statements">
        <p
          v-for="(point, index) in whyGrowLogPoints"
          :key="point.number"
          :ref="(el) => setPointRef(el as Element | null, index)"
          class="intro__statement"
          :class="[`intro__statement--${index}`, { 'points--motion': whyMotionEnabled, 'is-visible': revealedPoints[index] }]"
        >
          <span class="intro__statement-number">{{ point.number }}</span>
          <span class="intro__statement-text">{{ point.text }}</span>
        </p>
      </div>

      <p class="intro__conclusion">
        GrowLog는 흘려보내기 쉬운 작은 변화들을 기록으로 남기고, 그 안에서
        나의 성장을 발견할 수 있도록 돕습니다.
      </p>
    </section>

    <div id="features">
      <FeatureSection />
    </div>

    <GrowthJourney />

    <div class="final-cta-band">
      <section
        :ref="(el) => (finalCtaTarget = el as HTMLElement | null)"
        class="final-cta"
        :class="{ 'will-reveal': finalCtaMotion, 'is-visible': finalCtaVisible }"
      >
        <h2 class="final-cta__title">당신의 성장은 이미 시작되고 있습니다.</h2>
        <p class="final-cta__text">
          오늘의 목표와 작은 변화를 GrowLog에 기록해보세요. 쌓인 기록은 시간이
          지나 당신의 성장 과정을 보여줍니다.
        </p>
        <BaseButton variant="primary" @click="goToCtaTarget">{{ finalCtaLabel }}</BaseButton>
      </section>
    </div>
  </div>
</template>

<style scoped>
.landing {
  display: flex;
  flex-direction: column;
}

/* 상단 네비게이션 바는 페이지 캔버스(Background)와 구분되는 Surface로 둔다 */
.landing-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
  padding: var(--space-4) var(--space-6);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.landing-header__logo {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.landing-header__nav {
  display: flex;
  gap: var(--space-4);
}

.landing-header__nav a {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  text-decoration: none;
}

.landing-header__nav a:hover {
  color: var(--color-primary);
}

.intro {
  max-width: 1180px;
  margin: 0 auto;
  padding: calc(var(--space-12) * 1.2) var(--space-6);
}

/*
 * intro__title / feature-section__title(FeatureSection.vue) /
 * journey__title(GrowthJourney.vue) — Landing의 Section Title은 24px/600을
 * 쓰고 싶지만, 이 값을 --font-size-xl 전역 토큰에 반영하면 Dashboard/
 * Timeline/Goal/Login의 기존 제목 크기까지 함께 바뀐다. Application UI
 * 타이포 계층은 다음 단계에서 별도로 재검토하기로 했으므로, 지금은
 * 전역 토큰을 건드리지 않고 Landing 컴포넌트 스코프 안에서만 로컬 값으로
 * 적용한다.
 *
 * 정렬은 Hero(left)와 같은 left/editorial 톤을 이어받는다 — "중앙 제목 →
 * 콘텐츠"가 섹션마다 반복되지 않도록 하기 위함이다.
 */
.intro__title {
  margin: 0 0 var(--space-10);
  max-width: 640px;
  font-size: 24px;
  font-weight: var(--font-weight-semibold);
  text-align: left;
}

.intro__statements {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

/*
 * 01 → 03으로 갈수록 왼쪽에서 오른쪽으로 조금씩 밀려서, 세 문장이 계단
 * 처럼 아래로 읽히게 한다. 좁은 화면에서는 이 오프셋을 0으로 되돌린다
 * (media query에서 처리).
 */
.intro__statement {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin: 0;
  max-width: 620px;
}

.intro__statement--0 {
  margin-left: 0;
}

.intro__statement--1 {
  margin-left: clamp(0px, 8vw, 96px);
}

.intro__statement--2 {
  margin-left: clamp(0px, 16vw, 192px);
}

.intro__statement-number {
  font-size: 13px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.06em;
}

.intro__statement--0 .intro__statement-number {
  color: var(--color-accent);
}

.intro__statement--1 .intro__statement-number {
  color: var(--color-primary);
}

.intro__statement--2 .intro__statement-number {
  color: var(--color-primary-hover);
}

.intro__statement-text {
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  line-height: 1.7;
}

/*
 * Progressive Enhancement — 기본 상태(.points--motion 없음, 즉 JS
 * 비활성/reduced-motion)는 문장이 전부 보이는 "최종 상태"다.
 * `.points--motion`이 붙어야만(JS가 motion을 켤 때만) 문장이 숨어서
 * reveal을 기다리는 상태가 된다. Rail/connector 선은 더 이상 쓰지
 * 않는다(Hero와 마찬가지로 dot/line 모티프를 이 섹션에서도 제거).
 */
.intro__statement.points--motion {
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.intro__statement.points--motion.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/*
 * 결론 — 카드/배경 박스 없이 충분한 상단 여백만으로 "도착 지점"임을
 * 표시한다. 색은 Primary Green으로 톤만 구분한다.
 */
.intro__conclusion {
  margin: calc(var(--space-12) * 1.1) 0 0;
  max-width: 620px;
  color: var(--color-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  line-height: 1.7;
}

@media (max-width: 720px) {
  .intro__statement--0,
  .intro__statement--1,
  .intro__statement--2 {
    margin-left: 0;
  }
}

/*
 * Final CTA — 그라데이션/모티프 없이 단색 Soft Green 블록 + headline /
 * context / button / 충분한 whitespace로만 페이지를 닫는다. 배경은
 * 계속 화면 끝까지 full-bleed로 채우되(section 자체에는 max-width를
 * 주지 않는다), 안쪽 콘텐츠 폭만 다른 섹션과 같은 스케일(~1180px)로
 * 넓혀서 좌우 padding 리듬을 맞춘다. 내용 자체는 계속 중앙 정렬된 좁은
 * 텍스트로 읽힌다(각 요소의 max-width로 제한).
 */
.final-cta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  max-width: 1180px;
  margin: 0 auto;
  padding: calc(var(--space-12) * 1.5) var(--space-6);
  text-align: center;
}

.final-cta-band {
  background: var(--color-primary-bg);
}

.final-cta__title {
  margin: 0;
  font-size: var(--font-size-page-title);
  font-weight: var(--font-weight-bold);
}

.final-cta__text {
  margin: 0 0 var(--space-2);
  max-width: 480px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
}

/*
 * Progressive Enhancement — 기본 상태는 전부 보인다. `.will-reveal`이
 * 붙어야만(JS가 motion을 켤 때만) 섹션 전체가 한 번에 숨어서 reveal을
 * 기다린다. 여러 단계로 나눠 stagger하지 않고, 섹션 전체가 한 번의
 * subtle fade-up으로 나타나는 정도면 충분하다(요청사항).
 */
.final-cta.will-reveal {
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.final-cta.will-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}
</style>
