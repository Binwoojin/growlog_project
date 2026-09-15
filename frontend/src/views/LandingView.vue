<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { useInViewOnce } from '../composables/useInViewOnce'
import BaseButton from '../components/common/BaseButton.vue'
import BaseCard from '../components/common/BaseCard.vue'
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
 * "Why GrowLog" — 문제 제기 3개 + GrowLog의 역할을 설명하는 결론 문장.
 * 이 섹션의 목적은 "무엇을 하는가"가 아니라 "왜 필요한가"이므로,
 * Growth Journey(사용자 경험 변화)와 겹치는 STEP 나열 구조는 쓰지 않는다.
 */
const whyGrowLogPoints = [
  '계획은 세웠지만, 시간이 지나면 왜 시작했는지 잊기 쉽습니다.',
  '하루의 변화는 작아서, 기록하지 않으면 그 과정은 금방 사라집니다.',
  '기록이 쌓이면 내가 어떻게 달라졌는지 더 분명하게 볼 수 있습니다.',
]

/*
 * Why GrowLog reveal — "점+문장 → 연결선 → 다음 점" 순서를 표현한다.
 * 항목마다 정확한 픽셀 위치까지 계산해 개별 선 segment를 긋는 대신
 * (문장이 몇 줄로 줄바꿈될지 미리 알 수 없어 Growth Journey의 모바일
 * rail과 같은 이유로 정확한 계산이 어렵다), 기존의 연속된 rail 선
 * 하나를 reveal된 항목 수에 비례해 scaleY로 자라게 한다 — 항목이
 * 스크롤에 따라 하나씩 나타날 때마다 선도 따라 자라 보이는 효과는
 * 동일하게 유지된다. 한 번 본 항목은 다시 숨기지 않는다(unobserve).
 */
const pointRefs = ref<(Element | null)[]>([])
const revealedPoints = ref<boolean[]>(whyGrowLogPoints.map(() => false))
const whyMotionEnabled = ref(false)
const revealedCount = computed(() => revealedPoints.value.filter(Boolean).length)

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
      <BaseCard class="intro-card">
        <ul
          class="intro-card__points"
          :class="[{ 'points--motion': whyMotionEnabled }, whyMotionEnabled ? `reveal-${revealedCount}` : '']"
        >
          <li
            v-for="(point, index) in whyGrowLogPoints"
            :key="point"
            :ref="(el) => setPointRef(el as Element | null, index)"
            :class="{ 'is-visible': revealedPoints[index] }"
          >
            {{ point }}
          </li>
        </ul>
        <p class="intro-card__conclusion">
          GrowLog는 흘려보내기 쉬운 작은 변화들을 기록으로 남기고, 그 안에서
          나의 성장을 발견할 수 있도록 돕습니다.
        </p>
      </BaseCard>
    </section>

    <div id="features">
      <FeatureSection />
    </div>

    <GrowthJourney />

    <section
      :ref="(el) => (finalCtaTarget = el as HTMLElement | null)"
      class="final-cta"
      :class="{ 'will-reveal': finalCtaMotion, 'is-visible': finalCtaVisible }"
    >
      <div class="final-cta__motif" aria-hidden="true">
        <span class="final-cta__motif-dot" />
        <span class="final-cta__motif-line" />
        <span class="final-cta__motif-dot final-cta__motif-dot--primary" />
      </div>
      <h2 class="final-cta__title">당신의 성장은 이미 시작되고 있습니다.</h2>
      <p class="final-cta__text">
        오늘의 목표와 작은 변화를 GrowLog에 기록해보세요. 쌓인 기록은 시간이
        지나 당신의 성장 과정을 보여줍니다.
      </p>
      <BaseButton variant="primary" @click="goToCtaTarget">{{ finalCtaLabel }}</BaseButton>
    </section>
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
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
}

/*
 * intro__title / feature-section__title(FeatureSection.vue) /
 * journey__title(GrowthJourney.vue) — Landing의 Section Title은 24px/600을
 * 쓰고 싶지만, 이 값을 --font-size-xl 전역 토큰에 반영하면 Dashboard/
 * Timeline/Goal/Login의 기존 제목 크기까지 함께 바뀐다. Application UI
 * 타이포 계층은 다음 단계에서 별도로 재검토하기로 했으므로, 지금은
 * 전역 토큰을 건드리지 않고 Landing 컴포넌트 스코프 안에서만 로컬 값으로
 * 적용한다.
 */
.intro__title {
  margin: 0 0 var(--space-6);
  font-size: 24px;
  font-weight: var(--font-weight-semibold);
  text-align: center;
}

.intro-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  max-width: 640px;
  margin: 0 auto;
}

/*
 * "기록이 쌓인다"를 표현하는 rail — 각 항목 왼쪽에 점을 두고, 점들을
 * 세로선으로 이어서 Visual Language의 "점 → 선 → 축적"을 리스트
 * 형태로 옮겼다. 아이콘/이미지 없이 border/pseudo-element만 사용한다.
 */
.intro-card__points {
  position: relative;
  margin: 0;
  padding-left: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  line-height: 1.7;
  list-style: none;
}

.intro-card__points::before {
  content: '';
  position: absolute;
  left: 3px;
  top: 0.5em;
  bottom: 0.5em;
  width: 1px;
  background: var(--color-border);
}

.intro-card__points li {
  position: relative;
}

.intro-card__points li::before {
  content: '';
  position: absolute;
  left: calc(-1 * var(--space-6));
  top: 0.5em;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-accent);
}

/*
 * Progressive Enhancement — 기본 상태(.points--motion 없음, 즉 JS
 * 비활성/reduced-motion)는 전부 보이고 rail 선도 끝까지 이어진
 * "최종 상태"다. `.points--motion`이 붙어야만(JS가 motion을 켤 때만)
 * 항목이 숨어서 reveal을 기다리는 상태가 된다.
 */
.intro-card__points.points--motion li {
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.intro-card__points.points--motion li.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.intro-card__points.points--motion::before {
  transform: scaleY(0);
  transform-origin: top;
  transition: transform 0.5s ease;
}

.intro-card__points.points--motion.reveal-1::before {
  transform: scaleY(0.34);
}

.intro-card__points.points--motion.reveal-2::before {
  transform: scaleY(0.67);
}

.intro-card__points.points--motion.reveal-3::before {
  transform: scaleY(1);
}

/*
 * 리스트(문제 제기)와 결론(GrowLog의 역할)을 톤으로 구분한다. 점 크기를
 * 키우거나 대비를 강하게 주면 infographic처럼 보일 수 있어서(요청사항)
 * rail 자체는 손대지 않고, 결론 문단만 아주 옅은 Soft Green 배경으로
 * "도착 지점"임을 표시하는 정도로 절제했다.
 */
.intro-card__conclusion {
  margin: 0;
  padding: var(--space-4);
  background: var(--color-primary-bg);
  border-radius: var(--radius-sm);
  font-weight: var(--font-weight-medium);
}

/*
 * Final CTA — 그라데이션 없이 단색 Soft Green 블록으로 섹션을 감싸서
 * 페이지의 "닫는 지점"을 다른 톤으로 구분한다.
 */
.final-cta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: calc(var(--space-12) * 1.5) var(--space-4);
  background: var(--color-primary-bg);
  text-align: center;
}

.final-cta__motif {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.final-cta__motif-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-accent);
}

.final-cta__motif-dot--primary {
  width: 7px;
  height: 7px;
  background: var(--color-primary);
}

.final-cta__motif-line {
  width: var(--space-8);
  height: 1px;
  background: var(--color-border);
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
 * 붙어야만(JS가 motion을 켤 때만) 자식들이 숨어서 reveal을 기다리고,
 * `.is-visible`이 붙으면(뷰포트 진입 1회) motif → headline → context →
 * 버튼 순으로 100ms씩 차이 나게 나타난다.
 */
.final-cta.will-reveal > * {
  opacity: 0;
  transform: translateY(10px);
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.final-cta.will-reveal > *:nth-child(1) {
  transition-delay: 0ms;
}

.final-cta.will-reveal > *:nth-child(2) {
  transition-delay: 100ms;
}

.final-cta.will-reveal > *:nth-child(3) {
  transition-delay: 200ms;
}

.final-cta.will-reveal > *:nth-child(4) {
  transition-delay: 300ms;
}

.final-cta.will-reveal.is-visible > * {
  opacity: 1;
  transform: translateY(0);
}
</style>
