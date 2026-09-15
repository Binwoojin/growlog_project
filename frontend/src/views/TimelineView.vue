<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchTimeline } from '../api/timeline.api'
import type { TimelineFilter, TimelineResponse } from '../types/timeline'
import AppNav from '../components/common/AppNav.vue'
import BaseBadge from '../components/common/BaseBadge.vue'
import BaseButton from '../components/common/BaseButton.vue'
import BaseCard from '../components/common/BaseCard.vue'
import LoadingSkeleton from '../components/common/LoadingSkeleton.vue'

/*
 * Day 5 — TimelineItem Discriminated Union 타입으로 목표/기록을 안전하게
 * 분기해서 보여준다. 백엔드 TimelineService.getTimeline()이 이미 두 도메인을
 * 하나로 합쳐 최신순 정렬까지 해서 내려주므로, 프론트에서 별도로
 * normalizeTimeline() 병합 로직을 만들 필요가 없다 — 그대로 렌더링만 한다.
 *
 * Day 6 — Loading(Skeleton)/Error/Empty/Success 상태와 전체/목표/기록 필터를
 * 추가해서 UX를 완성한다.
 */
const timeline = ref<TimelineResponse | null>(null)
const status = ref<'loading' | 'success' | 'error'>('loading')
const filter = ref<TimelineFilter>('ALL')

const filteredItems = computed(() => {
  if (!timeline.value) return []
  if (filter.value === 'ALL') return timeline.value.timelineItems
  return timeline.value.timelineItems.filter((item) => item.type === filter.value)
})

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
      <div class="timeline__heading">
        <h1>타임라인</h1>
        <p v-if="timeline" class="timeline__subtitle">
          {{ timeline.selectedMonth }} · 목표 {{ timeline.monthlyGoalCount }}개 · 기록 {{ timeline.monthlyRecordCount }}개
        </p>
      </div>
      <AppNav />
    </header>

    <nav v-if="status === 'success'" class="timeline__filter" aria-label="타임라인 필터">
      <BaseButton
        :variant="filter === 'ALL' ? 'primary' : 'secondary'"
        @click="filter = 'ALL'"
      >
        전체
      </BaseButton>
      <BaseButton
        :variant="filter === 'GOAL' ? 'primary' : 'secondary'"
        @click="filter = 'GOAL'"
      >
        🌱 목표
      </BaseButton>
      <BaseButton
        :variant="filter === 'RECORD' ? 'primary' : 'secondary'"
        @click="filter = 'RECORD'"
      >
        📖 기록
      </BaseButton>
    </nav>

    <LoadingSkeleton v-if="status === 'loading'" :count="3" />

    <p v-else-if="status === 'error'" class="timeline__status timeline__status--error">
      데이터를 불러오지 못했어요. 잠시 후 다시 시도해주세요.
    </p>

    <template v-else-if="timeline">
      <p v-if="filteredItems.length === 0" class="timeline__status">
        {{
          filter === 'ALL'
            ? '아직 이번 달 기록이 없어요. 오늘의 성장을 기록해보세요.'
            : filter === 'GOAL'
              ? '이번 달 등록한 목표가 없어요.'
              : '이번 달 작성한 성장 기록이 없어요.'
        }}
      </p>

      <ul v-else class="timeline__list">
        <li v-for="item in filteredItems" :key="`${item.type}-${item.itemNum}`" class="timeline__node">
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

.timeline__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
}

.timeline__header h1 {
  font-size: var(--font-size-xl);
}

.timeline__subtitle {
  margin: var(--space-1) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.timeline__filter {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.timeline__status {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.timeline__status--error {
  color: var(--color-error);
}

/*
 * Reflect의 점-선 연결 구조 — Goal/Record 종류는 rail 색으로 구분하지
 * 않고 중립/Soft Green 톤으로 통일한다. 종류 구분은 카드 내부의
 * BaseBadge에만 맡기고, rail은 "시간에 따라 기록이 이어진다"는 흐름만
 * 보여준다.
 */
.timeline__list {
  position: relative;
  list-style: none;
  margin: 0;
  padding: 0 0 0 var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.timeline__list::before {
  content: '';
  position: absolute;
  left: 3px;
  top: 10px;
  bottom: 10px;
  width: 1px;
  background: var(--color-primary-bg);
}

.timeline__node {
  position: relative;
}

.timeline__node::before {
  content: '';
  position: absolute;
  left: calc(-1 * var(--space-4));
  top: 10px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-accent);
}

/* Day One — 콘텐츠(제목/본문) 자체가 중심이 되도록 줄간격을 넉넉하게 */
.timeline-item__title {
  margin: var(--space-3) 0 0;
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-lg);
  line-height: 1.5;
}

.timeline-item__meta {
  margin: var(--space-2) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}
</style>
