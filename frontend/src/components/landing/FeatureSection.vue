<script setup lang="ts">
import { LayoutDashboard, NotebookPen, Route, Target } from '@lucide/vue'
import BaseCard from '../common/BaseCard.vue'

/*
 * emoji placeholder를 실제 Rounded Outline 아이콘(lucide)으로 교체했다.
 * 색상은 --color-primary 하나로만 제한하고(아이콘마다 다른 색 남발 금지),
 * 아이콘은 장식이 아니라 각 기능을 빠르게 식별하는 정보 보조 역할이다.
 *
 * 타이틀은 한글 중심으로만 노출한다 — 영문 명칭(Goal Management 등)은
 * 화면에 표시하지 않는다.
 */
const features = [
  { icon: Target, title: '목표 관리', description: '이루고 싶은 목표를 정하고 진행 상황을 확인합니다.' },
  { icon: NotebookPen, title: '성장 기록', description: '하루의 과정, 생각, 변화를 짧게 남깁니다.' },
  { icon: Route, title: '성장 타임라인', description: '목표와 기록이 시간에 따라 어떻게 이어졌는지 확인합니다.' },
  { icon: LayoutDashboard, title: '성장 대시보드', description: '쌓인 기록과 목표 진행 상황을 한눈에 돌아봅니다.' },
]
</script>

<template>
  <section class="feature-section">
    <h2 class="feature-section__title">주요 기능</h2>

    <div class="feature-section__grid">
      <BaseCard v-for="feature in features" :key="feature.title" class="feature-card">
        <span class="feature-card__icon">
          <component :is="feature.icon" :size="22" :stroke-width="1.75" />
        </span>
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
  font-size: var(--font-size-section-title);
  font-weight: var(--font-weight-semibold);
  text-align: center;
}

.feature-section__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-6);
}

/*
 * 낮은 shadow(기본) → hover 시 --shadow-elevated 한 단계만 더한다.
 * translateY도 1~2px 수준으로만 움직여서 뜨는 느낌이 과하지 않게 한다.
 */
.feature-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  text-align: center;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-bg);
  box-shadow: var(--shadow-elevated);
}

.feature-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin: 0 auto;
  color: var(--color-primary);
}

.feature-card__title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.feature-card__description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
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
