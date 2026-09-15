<script setup lang="ts">
import { Flame, NotebookText, Target } from '@lucide/vue'
import BaseButton from '../common/BaseButton.vue'

/*
 * Hero Product Preview — 실제 API에 연결하지 않고 정적 프레젠테이션
 * 데이터만 사용한다. GrowLog Dashboard가 실제로 보여주는 정보(이번 달
 * 기록 수 / 진행 중 목표 수 / 연속 기록 / Goal Progress / 최근 타임라인)만
 * 축약해서 보여주고, 존재하지 않는 기능을 새로 지어내지 않는다.
 *
 * hero__motif: GrowLog Visual Language "점 → 선"을 아주 작게 표현한
 * 브랜드 보조 그래픽. 순수 CSS, 정적(애니메이션 없음), 커지는 점
 * 3개 + 연결선만으로 구성해 큰 일러스트처럼 보이지 않게 했다.
 * 헤드라인/설명이 여전히 Hero의 중심이고, 이 모티프는 그 위에 놓인
 * 작은 보조 요소일 뿐이다.
 */
defineProps<{
  ctaLabel: string
}>()

const emit = defineEmits<{
  cta: []
}>()
</script>

<template>
  <section class="hero">
    <div class="hero__copy">
      <div class="hero__motif" aria-hidden="true">
        <span class="hero__motif-dot hero__motif-dot--sm" />
        <span class="hero__motif-line" />
        <span class="hero__motif-dot hero__motif-dot--md" />
        <span class="hero__motif-line" />
        <span class="hero__motif-dot hero__motif-dot--lg" />
      </div>

      <h1 class="hero__headline">오늘의 기록이 내일의 성장이 됩니다.</h1>
      <p class="hero__subcopy">
        GrowLog는 목표를 세우고, 매일의 과정과 변화를 기록하며, 쌓인 기록
        속에서 나의 성장을 발견하는 개인 성장 아카이브입니다.
      </p>
      <BaseButton variant="primary" @click="emit('cta')">{{ ctaLabel }}</BaseButton>
    </div>

    <div class="hero__preview-wrap" aria-hidden="true">
      <div class="hero__preview-backdrop" />

      <div class="hero__preview">
        <div class="hero__preview-header">
          <p class="hero__preview-greeting">안녕하세요, 성장러님 👋</p>
          <span class="hero__preview-tag">Dashboard</span>
        </div>

        <div class="hero__preview-stats">
          <div class="hero__preview-stat">
            <p class="hero__preview-stat-value">12</p>
            <p class="hero__preview-stat-label">이번 달 기록</p>
          </div>
          <div class="hero__preview-stat">
            <p class="hero__preview-stat-value">3</p>
            <p class="hero__preview-stat-label">진행 중 목표</p>
          </div>
          <div class="hero__preview-stat">
            <p class="hero__preview-stat-value">
              <Flame :size="14" :stroke-width="1.75" />
              7일
            </p>
            <p class="hero__preview-stat-label">연속 기록</p>
          </div>
        </div>

        <div class="hero__preview-goal">
          <div class="hero__preview-goal-head">
            <Target :size="14" :stroke-width="1.75" />
            <span class="hero__preview-goal-title">포트폴리오 완성하기</span>
            <span class="hero__preview-goal-percent">72%</span>
          </div>
          <div class="hero__preview-goal-track">
            <div class="hero__preview-goal-fill" />
          </div>
        </div>

        <div class="hero__preview-timeline">
          <span class="hero__preview-timeline-dot" />
          <NotebookText :size="14" :stroke-width="1.75" class="hero__preview-timeline-icon" />
          <div>
            <p class="hero__preview-timeline-title">오늘의 기록</p>
            <p class="hero__preview-timeline-meta">작은 진전도 기록으로 남겼어요</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  gap: var(--space-12);
  padding: calc(var(--space-12) * 1.5) var(--space-4);
  max-width: 960px;
  margin: 0 auto;
}

.hero__copy {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  align-items: flex-start;
}

.hero__motif {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.hero__motif-dot {
  display: block;
  border-radius: 50%;
  background: var(--color-accent);
}

.hero__motif-dot--sm {
  width: 4px;
  height: 4px;
}

.hero__motif-dot--md {
  width: 6px;
  height: 6px;
}

.hero__motif-dot--lg {
  width: 9px;
  height: 9px;
  background: var(--color-primary);
}

.hero__motif-line {
  width: var(--space-6);
  height: 1px;
  background: var(--color-border);
}

.hero__headline {
  margin: 0;
  font-size: var(--font-size-hero);
  line-height: 1.2;
  font-weight: var(--font-weight-bold);
}

.hero__subcopy {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  line-height: 1.7;
}

/*
 * Product Preview — 실제 Dashboard와 같은 디자인 언어(색/타이포/카드)를
 * 쓰되, 여기서만 쓰는 축소된 정적 프레젠테이션이다. 뒤에 Soft Green
 * backdrop 카드를 살짝 어긋나게 겹쳐서(layered surface) depth를 준다.
 * elevation은 --shadow-elevated 한 단계만 쓴다.
 */
.hero__preview-wrap {
  position: relative;
  flex: 1;
  max-width: 360px;
}

.hero__preview-backdrop {
  position: absolute;
  inset: var(--space-4) calc(-1 * var(--space-3)) calc(-1 * var(--space-3)) var(--space-4);
  background: var(--color-primary-bg);
  border-radius: var(--radius-lg);
}

.hero__preview {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-elevated);
}

.hero__preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero__preview-greeting {
  margin: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__preview-tag {
  padding: 2px var(--space-2);
  border-radius: 999px;
  background: var(--color-bg);
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: var(--font-weight-medium);
}

.hero__preview-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-2);
}

.hero__preview-stat {
  padding: var(--space-3) var(--space-2);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  text-align: center;
}

.hero__preview-stat-value {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.hero__preview-stat-label {
  margin: 2px 0 0;
  font-size: 11px;
  color: var(--color-text-secondary);
}

.hero__preview-goal {
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.hero__preview-goal-head {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
}

.hero__preview-goal-title {
  flex: 1;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.hero__preview-goal-percent {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.hero__preview-goal-track {
  height: 6px;
  border-radius: 999px;
  background: var(--color-bg);
  overflow: hidden;
}

.hero__preview-goal-fill {
  width: 72%;
  height: 100%;
  border-radius: 999px;
  background: var(--color-primary);
}

.hero__preview-timeline {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
}

.hero__preview-timeline-dot {
  position: absolute;
  left: 0;
  top: calc(var(--space-3) + 2px);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--color-accent);
}

.hero__preview-timeline-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--color-text-secondary);
}

.hero__preview-timeline-title {
  margin: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__preview-timeline-meta {
  margin: 2px 0 0;
  font-size: 11px;
  color: var(--color-text-secondary);
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: stretch;
    padding: var(--space-8) var(--space-4);
  }

  .hero__preview-wrap {
    max-width: none;
  }
}
</style>
