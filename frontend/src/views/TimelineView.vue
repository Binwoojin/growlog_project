<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchTimeline } from '../api/timeline.api'
import type { TimelineResponse } from '../types/timeline'
import BaseBadge from '../components/common/BaseBadge.vue'
import BaseCard from '../components/common/BaseCard.vue'

/*
 * Day 5 — TimelineItem Discriminated Union 타입으로 목표/기록을 안전하게
 * 분기해서 보여준다. 백엔드 TimelineService.getTimeline()이 이미 두 도메인을
 * 하나로 합쳐 최신순 정렬까지 해서 내려주므로, 프론트에서 별도로
 * normalizeTimeline() 병합 로직을 만들 필요가 없다 — 그대로 렌더링만 한다.
 *
 * 필터/Loading·Error·Empty 상태를 제대로 갖춘 UX는 Day 6에서 다듬는다.
 * 지금은 화면이 깨지지 않을 정도의 최소 상태만 둔다.
 */
const timeline = ref<TimelineResponse | null>(null)
const status = ref<'loading' | 'success' | 'error'>('loading')

onMounted(async () => {
  try {
    timeline.value = await fetchTimeline()
    status.value = 'success'
  } catch {
    status.value = 'error'
  }
})
</script>

<template>
  <main class="timeline">
    <header class="timeline__header">
      <h1>타임라인</h1>
      <p v-if="timeline" class="timeline__subtitle">
        {{ timeline.selectedMonth }} · 목표 {{ timeline.monthlyGoalCount }}개 · 기록 {{ timeline.monthlyRecordCount }}개
      </p>
    </header>

    <p v-if="status === 'loading'" class="timeline__status">불러오는 중...</p>
    <p v-else-if="status === 'error'" class="timeline__status timeline__status--error">
      데이터를 불러오지 못했어요. 잠시 후 다시 시도해주세요.
    </p>

    <template v-else-if="timeline">
      <p v-if="timeline.timelineItems.length === 0" class="timeline__status">
        아직 이번 달 기록이 없어요. 오늘의 성장을 기록해보세요.
      </p>

      <ul v-else class="timeline__list">
        <li v-for="item in timeline.timelineItems" :key="`${item.type}-${item.itemNum}`">
          <BaseCard class="timeline-item">
            <BaseBadge :variant="item.type === 'GOAL' ? 'primary' : 'success'">
              {{ item.type === 'GOAL' ? '🌱 목표' : '📖 성장 기록' }}
            </BaseBadge>
            <p class="timeline-item__title">{{ item.title }}</p>
            <p class="timeline-item__meta">{{ item.content }}</p>
          </BaseCard>
        </li>
      </ul>
    </template>
  </main>
</template>

<style scoped>
.timeline {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.timeline__header h1 {
  font-size: var(--font-size-xl);
}

.timeline__subtitle {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.timeline__status {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.timeline__status--error {
  color: var(--color-error);
}

.timeline__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
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
</style>
