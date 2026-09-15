<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'
import { fetchDashboard } from '../api/dashboard.api'
import type { DashboardSummary } from '../types/dashboard'
import AppNav from '../components/common/AppNav.vue'
import BaseBadge from '../components/common/BaseBadge.vue'
import BaseButton from '../components/common/BaseButton.vue'
import BaseCard from '../components/common/BaseCard.vue'
import LoadingSkeleton from '../components/common/LoadingSkeleton.vue'

/*
 * Day 4 — GET /api/dashboard로 실제 로그인 사용자 데이터를 가져와 표시한다.
 * Day 6 — Loading 상태를 Timeline과 같은 LoadingSkeleton으로 통일했다.
 */
const authStore = useAuthStore()
const router = useRouter()

const summary = ref<DashboardSummary | null>(null)
const status = ref<'loading' | 'success' | 'error'>('loading')

onMounted(async () => {
  try {
    summary.value = await fetchDashboard()
    status.value = 'success'
  } catch {
    status.value = 'error'
  }
})

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
      <div class="dashboard__actions">
        <AppNav />
        <BaseButton variant="ghost" @click="onLogout">로그아웃</BaseButton>
      </div>
    </header>

    <LoadingSkeleton v-if="status === 'loading'" :count="3" />

    <p v-else-if="status === 'error'" class="dashboard__status dashboard__status--error">
      데이터를 불러오지 못했어요. 잠시 후 다시 시도해주세요.
    </p>

    <template v-else-if="summary">
      <section class="dashboard__summary">
        <BaseCard class="summary-card">
          <p class="summary-card__value">{{ summary.recordsThisMonth }}</p>
          <p class="summary-card__label">이번 달 기록</p>
          <p class="summary-card__hint">꾸준히 기록하고 있어요.</p>
        </BaseCard>
        <BaseCard class="summary-card">
          <p class="summary-card__value">{{ summary.activeGoalCount }}</p>
          <p class="summary-card__label">진행 중인 목표</p>
          <!--
            activeGoalCount는 GoalService.countThisWeekInProgressGoals()를 그대로
            재사용한 값이라 "전체 진행중 목표"가 아니라 "이번 주에 등록한 진행중
            목표"다. 기존 JSP 홈 화면(home.jsp)도 같은 값을 같은 라벨로 보여주면서
            이 부연 문구로 범위를 명시하는 방식을 쓰고 있어서, 그 문구를 그대로
            가져와 통일했다. 집계 로직을 새로 만들지 않고 라벨/문구만 맞춘 것.
          -->
          <p class="summary-card__hint">이번 주 목표를 이어가고 있어요.</p>
        </BaseCard>
        <BaseCard class="summary-card">
          <p class="summary-card__value">🔥 {{ summary.streakDays }}일</p>
          <p class="summary-card__label">연속 기록</p>
        </BaseCard>
      </section>

      <section class="dashboard__quick-actions">
        <BaseButton variant="primary" @click="router.push({ name: 'goal-new' })">목표 추가</BaseButton>
        <BaseButton variant="secondary" disabled title="Day 13 이후 연결 예정">기록 남기기</BaseButton>
      </section>

      <section class="dashboard__timeline">
        <h2 class="dashboard__section-title">최근 타임라인</h2>

        <p v-if="summary.recentTimeline.length === 0" class="dashboard__status">
          아직 이번 달 기록이 없어요. 오늘의 성장을 기록해보세요.
        </p>

        <BaseCard
          v-for="item in summary.recentTimeline"
          :key="`${item.type}-${item.itemNum}`"
          class="timeline-item"
        >
          <BaseBadge :variant="item.type === 'GOAL' ? 'primary' : 'success'">
            {{ item.type === 'GOAL' ? '🌱 목표' : '📖 성장 기록' }}
          </BaseBadge>
          <p class="timeline-item__title">{{ item.title }}</p>
          <p class="timeline-item__meta">{{ item.content }}</p>
        </BaseCard>
      </section>
    </template>
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
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
}

.dashboard__greeting {
  font-size: var(--font-size-xl);
}

.dashboard__subtitle {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.dashboard__actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: var(--space-4);
}

.dashboard__status {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.dashboard__status--error {
  color: var(--color-error);
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

.summary-card__hint {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: 12px;
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
