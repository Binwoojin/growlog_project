<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
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
        <ul class="intro-card__points">
          <li v-for="point in whyGrowLogPoints" :key="point">{{ point }}</li>
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

    <section class="final-cta">
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

.landing-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
  padding: var(--space-4) var(--space-6);
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

.intro-card__conclusion {
  margin: 0;
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
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

.final-cta__title {
  margin: 0;
  font-size: 28px;
  font-weight: var(--font-weight-bold);
}

.final-cta__text {
  margin: 0 0 var(--space-2);
  max-width: 480px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
}
</style>
