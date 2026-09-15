<script setup lang="ts">
import BaseButton from '../common/BaseButton.vue'

/*
 * 와이어프레임 단계 — 실제 Dashboard UI를 복제하지 않고, 구조만 보여주는
 * 회색 placeholder 블록(레이블 텍스트만 존재)으로 대체한다.
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
  gap: var(--space-8);
  padding: var(--space-12) var(--space-4);
  max-width: 960px;
  margin: 0 auto;
}

.hero__copy {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  align-items: flex-start;
}

.hero__headline {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.hero__subcopy {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
}

.hero__preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
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
  background: var(--color-bg);
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
