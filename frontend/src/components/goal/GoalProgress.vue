<script setup lang="ts">
const props = defineProps<{
  progress: number
}>()

/* 0~100 밖의 값이 들어와도 막대가 넘치거나 사라지지 않도록 방어한다 */
const clampedProgress = () => Math.min(100, Math.max(0, props.progress))
</script>

<template>
  <div class="goal-progress">
    <div
      class="goal-progress__track"
      role="progressbar"
      :aria-valuenow="progress"
      aria-valuemin="0"
      aria-valuemax="100"
    >
      <div class="goal-progress__fill" :style="{ width: `${clampedProgress()}%` }" />
    </div>
    <span class="goal-progress__label">{{ progress }}%</span>
  </div>
</template>

<style scoped>
.goal-progress {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.goal-progress__track {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  background: var(--color-bg);
  overflow: hidden;
}

.goal-progress__fill {
  height: 100%;
  border-radius: 999px;
  background: var(--color-primary);
  transition: width 0.2s ease;
}

.goal-progress__label {
  min-width: 36px;
  text-align: right;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
</style>
