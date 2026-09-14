<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import BaseBadge from '../components/common/BaseBadge.vue'
import BaseButton from '../components/common/BaseButton.vue'
import BaseCard from '../components/common/BaseCard.vue'

/*
 * Day 3 — Dashboard 와이어프레임.
 * Summary Card / Recent Timeline의 숫자와 목록은 아직 목(mock) 데이터다.
 * 실제 GET /api/dashboard, /api/timeline 연동은 Day 4~5에서 진행한다.
 */
const authStore = useAuthStore()
const router = useRouter()

const summary = {
  recordsThisMonth: 12,
  activeGoals: 3,
  streakDays: 7,
}

const recentTimeline = [
  { id: 1, type: 'goal' as const, title: '프론트엔드 포트폴리오 완성하기', progress: 80 },
  { id: 2, type: 'record' as const, title: 'Vue Composition API 학습', summary: '오늘 컴포넌트 구조를 정리했다.' },
]

async function onLogout() {
  await authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <main class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1 class="dashboard__greeting">
          안녕하세요{{ authStore.user ? `, ${authStore.user.nickname}님` : '' }} 👋
        </h1>
        <p class="dashboard__subtitle">이번 달에도 꾸준히 성장하고 있어요.</p>
      </div>
      <BaseButton variant="ghost" @click="onLogout">로그아웃</BaseButton>
    </header>

    <section class="dashboard__summary">
      <BaseCard class="summary-card">
        <p class="summary-card__value">{{ summary.recordsThisMonth }}</p>
        <p class="summary-card__label">이번 달 기록</p>
      </BaseCard>
      <BaseCard class="summary-card">
        <p class="summary-card__value">{{ summary.activeGoals }}</p>
        <p class="summary-card__label">진행 중 목표</p>
      </BaseCard>
      <BaseCard class="summary-card">
        <p class="summary-card__value">🔥 {{ summary.streakDays }}일</p>
        <p class="summary-card__label">연속 기록</p>
      </BaseCard>
    </section>

    <section class="dashboard__quick-actions">
      <BaseButton variant="primary">목표 추가</BaseButton>
      <BaseButton variant="secondary">기록 남기기</BaseButton>
    </section>

    <section class="dashboard__timeline">
      <h2 class="dashboard__section-title">최근 타임라인</h2>
      <BaseCard v-for="item in recentTimeline" :key="item.id" class="timeline-item">
        <template v-if="item.type === 'goal'">
          <BaseBadge variant="primary">🌱 목표</BaseBadge>
          <p class="timeline-item__title">{{ item.title }}</p>
          <p class="timeline-item__meta">진행률 {{ item.progress }}%</p>
        </template>
        <template v-else>
          <BaseBadge variant="success">📖 성장 기록</BaseBadge>
          <p class="timeline-item__title">{{ item.title }}</p>
          <p class="timeline-item__meta">{{ item.summary }}</p>
        </template>
      </BaseCard>
    </section>
  </main>
</template>

<style scoped>
.dashboard {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.dashboard__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.dashboard__greeting {
  font-size: var(--font-size-xl);
}

.dashboard__subtitle {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.dashboard__summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.summary-card {
  text-align: center;
}

.summary-card__value {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.summary-card__label {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.dashboard__quick-actions {
  display: flex;
  gap: var(--space-3);
}

.dashboard__section-title {
  font-size: var(--font-size-lg);
  margin: 0 0 var(--space-3);
}

.dashboard__timeline {
  display: flex;
  flex-direction: column;
}

.timeline-item + .timeline-item {
  margin-top: var(--space-3);
}

.timeline-item__title {
  margin: var(--space-2) 0 0;
  font-weight: var(--font-weight-medium);
}

.timeline-item__meta {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

@media (max-width: 640px) {
  .dashboard__summary {
    grid-template-columns: 1fr;
  }
}
</style>
