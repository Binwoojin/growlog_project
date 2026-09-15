<script setup lang="ts">
import BaseButton from '../common/BaseButton.vue'

/*
 * 실제 Dashboard UI를 복제하지 않고, 구조만 보여주는 placeholder
 * 블록(레이블 텍스트만 존재)으로 대체한다 — 최종 Dashboard 화면을
 * 여기서 미리 디자인하지 않는다.
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

    <div class="hero__preview" aria-hidden="true">
      <p class="hero__preview-label">Dashboard Preview</p>
      <div class="hero__preview-block">
        <span>Summary</span>
      </div>
      <div class="hero__preview-block">
        <span>Recent Timeline</span>
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

.hero__preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.hero__preview-label {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.hero__preview-block {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80px;
  background: var(--color-primary-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: stretch;
    padding: var(--space-8) var(--space-4);
  }
}
</style>
