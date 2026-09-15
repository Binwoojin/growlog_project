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
 */
const router = useRouter()
const authStore = useAuthStore()

const ctaLabel = computed(() => (authStore.isAuthenticated ? 'Dashboard로 이동' : '로그인하고 시작하기'))

function goToCtaTarget() {
  router.push({ name: authStore.isAuthenticated ? 'dashboard' : 'login' })
}

const introSteps = [
  { step: '1', title: '목표를 세운다', description: '이루고 싶은 목표를 정합니다.' },
  { step: '2', title: '하루의 성장을 기록한다', description: '오늘의 과정을 짧게 남깁니다.' },
  { step: '3', title: '변화와 성장을 확인한다', description: '쌓인 기록으로 나의 변화를 돌아봅니다.' },
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
      <h2 class="intro__title">GrowLog 소개</h2>
      <div class="intro__grid">
        <BaseCard v-for="item in introSteps" :key="item.step" class="intro-card">
          <span class="intro-card__step">STEP {{ item.step }}</span>
          <p class="intro-card__title">{{ item.title }}</p>
          <p class="intro-card__description">{{ item.description }}</p>
        </BaseCard>
      </div>
    </section>

    <div id="features">
      <FeatureSection />
    </div>

    <GrowthJourney />

    <section class="final-cta">
      <p class="final-cta__text">오늘의 작은 기록부터 시작해보세요.</p>
      <BaseButton variant="primary" @click="goToCtaTarget">{{ ctaLabel }}</BaseButton>
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

.intro__title {
  margin: 0 0 var(--space-6);
  font-size: var(--font-size-xl);
  text-align: center;
}

.intro__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.intro-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  text-align: center;
}

.intro-card__step {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.intro-card__title {
  margin: 0;
  font-weight: var(--font-weight-medium);
}

.intro-card__description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.final-cta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.final-cta__text {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
}

@media (max-width: 720px) {
  .intro__grid {
    grid-template-columns: 1fr;
  }
}
</style>
