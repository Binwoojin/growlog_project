<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchGoals } from '../api/goal.api'
import type { Goal } from '../types/goal'
import AppNav from '../components/common/AppNav.vue'
import BaseButton from '../components/common/BaseButton.vue'
import GoalCard from '../components/goal/GoalCard.vue'
import LoadingSkeleton from '../components/common/LoadingSkeleton.vue'

/*
 * Day 8 — Goal List. GoalCard/GoalProgress/GoalStatusBadge 컴포넌트로
 * 기존 GoalService.findGoalsByMember()를 그대로 노출한 GET /api/goals 결과를
 * 렌더링한다. 작성(Day 9), 수정/삭제(Day 10)는 다음 날 이어서 붙인다.
 */
const router = useRouter()
const goals = ref<Goal[]>([])
const status = ref<'loading' | 'success' | 'error'>('loading')

onMounted(async () => {
  try {
    goals.value = await fetchGoals()
    status.value = 'success'
  } catch {
    status.value = 'error'
  }
})
</script>

<template>
  <main class="goal-list">
    <header class="goal-list__header">
      <h1>목표</h1>
      <div class="goal-list__actions">
        <AppNav />
        <BaseButton variant="primary" @click="router.push({ name: 'goal-new' })">목표 추가</BaseButton>
      </div>
    </header>

    <LoadingSkeleton v-if="status === 'loading'" :count="3" />

    <p v-else-if="status === 'error'" class="goal-list__status goal-list__status--error">
      데이터를 불러오지 못했어요. 잠시 후 다시 시도해주세요.
    </p>

    <template v-else>
      <p v-if="goals.length === 0" class="goal-list__status">
        아직 등록한 목표가 없어요. 첫 목표를 등록해보세요.
      </p>

      <ul v-else class="goal-list__grid">
        <li v-for="goal in goals" :key="goal.goalNum">
          <GoalCard :goal="goal" />
        </li>
      </ul>
    </template>
  </main>
</template>

<style scoped>
.goal-list {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.goal-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
}

.goal-list__actions {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.goal-list__header h1 {
  font-size: var(--font-size-xl);
}

.goal-list__status {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.goal-list__status--error {
  color: var(--color-error);
}

.goal-list__grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
</style>
