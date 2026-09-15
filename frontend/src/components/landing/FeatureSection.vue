<script setup lang="ts">
import BaseCard from '../common/BaseCard.vue'

/*
 * 이모지는 최종 디자인 요소가 아니라, 와이어프레임 단계에서 기능 카드
 * 위치를 구분하기 위한 임시 placeholder다.
 *
 * 타이틀은 한글 중심으로만 노출한다 — 영문 명칭(Goal Management 등)은
 * 화면에 표시하지 않는다.
 */
const features = [
  { icon: '🎯', title: '목표 관리', description: '이루고 싶은 목표를 정하고 진행 상황을 확인합니다.' },
  { icon: '📝', title: '성장 기록', description: '하루의 과정, 생각, 변화를 짧게 남깁니다.' },
  { icon: '🗓️', title: '성장 타임라인', description: '목표와 기록이 시간에 따라 어떻게 이어졌는지 확인합니다.' },
  { icon: '📈', title: '성장 대시보드', description: '쌓인 기록과 목표 진행 상황을 한눈에 돌아봅니다.' },
]
</script>

<template>
  <section class="feature-section">
    <h2 class="feature-section__title">주요 기능</h2>

    <div class="feature-section__grid">
      <BaseCard v-for="feature in features" :key="feature.title" class="feature-card">
        <span class="feature-card__icon" aria-hidden="true">{{ feature.icon }}</span>
        <p class="feature-card__title">{{ feature.title }}</p>
        <p class="feature-card__description">{{ feature.description }}</p>
      </BaseCard>
    </div>
  </section>
</template>

<style scoped>
.feature-section {
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
}

.feature-section__title {
  margin: 0 0 var(--space-6);
  font-size: var(--font-size-xl);
  text-align: center;
}

.feature-section__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.feature-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  text-align: center;
}

.feature-card__icon {
  font-size: var(--font-size-xl);
}

.feature-card__title {
  margin: 0;
  font-weight: var(--font-weight-medium);
}

.feature-card__description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

@media (max-width: 720px) {
  .feature-section__grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 420px) {
  .feature-section__grid {
    grid-template-columns: 1fr;
  }
}
</style>
