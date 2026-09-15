<script setup lang="ts">
import type { Goal } from '../../types/goal'
import BaseCard from '../common/BaseCard.vue'
import GoalProgress from './GoalProgress.vue'
import GoalStatusBadge from './GoalStatusBadge.vue'

defineProps<{
  goal: Goal
}>()

function formatDateRange(startDate: string | null, endDate: string | null): string {
  if (!startDate && !endDate) return ''
  return `${startDate ?? '?'} ~ ${endDate ?? '?'}`
}
</script>

<template>
  <BaseCard class="goal-card">
    <div class="goal-card__header">
      <span class="goal-card__category">{{ goal.category.categoryIcon }} {{ goal.category.categoryName }}</span>
      <GoalStatusBadge :status="goal.goalStatus" />
    </div>

    <p class="goal-card__title">{{ goal.goalTitle }}</p>
    <p v-if="goal.goalContent" class="goal-card__content">{{ goal.goalContent }}</p>

    <GoalProgress :progress="goal.goalProgress" />

    <p v-if="goal.startDate || goal.endDate" class="goal-card__dates">
      {{ formatDateRange(goal.startDate, goal.endDate) }}
    </p>
  </BaseCard>
</template>

<style scoped>
.goal-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.goal-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.goal-card__category {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.goal-card__title {
  margin: 0;
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-lg);
}

.goal-card__content {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.goal-card__dates {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}
</style>
