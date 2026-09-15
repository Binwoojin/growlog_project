<script setup lang="ts">
/*
 * GrowLog 핵심 흐름 Goal → Record → Timeline → Growth를 순서가 가장
 * 분명하게 보이는 가로 스텝 UI로만 표현한다. 이모지는 와이어프레임
 * placeholder이며 최종 디자인 요소가 아니다.
 */
const steps = [
  { icon: '🎯', label: 'Goal', description: '목표를 세운다' },
  { icon: '📝', label: 'Record', description: '기록을 남긴다' },
  { icon: '🗓️', label: 'Timeline', description: '흐름을 모아본다' },
  { icon: '📈', label: 'Growth', description: '성장을 확인한다' },
]
</script>

<template>
  <section class="journey">
    <h2 class="journey__title">Growth Journey</h2>

    <ol class="journey__steps">
      <li v-for="(step, index) in steps" :key="step.label" class="journey__step">
        <div class="journey__step-box">
          <span class="journey__step-icon" aria-hidden="true">{{ step.icon }}</span>
          <p class="journey__step-label">{{ step.label }}</p>
          <p class="journey__step-description">{{ step.description }}</p>
        </div>
        <span v-if="index < steps.length - 1" class="journey__arrow" aria-hidden="true">→</span>
      </li>
    </ol>
  </section>
</template>

<style scoped>
.journey {
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
}

.journey__title {
  margin: 0 0 var(--space-6);
  font-size: var(--font-size-xl);
  text-align: center;
}

.journey__steps {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
}

.journey__step {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.journey__step-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  width: 140px;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  text-align: center;
}

.journey__step-icon {
  font-size: var(--font-size-lg);
}

.journey__step-label {
  margin: 0;
  font-weight: var(--font-weight-medium);
}

.journey__step-description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.journey__arrow {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
}

@media (max-width: 720px) {
  .journey__steps {
    flex-direction: column;
  }

  .journey__arrow {
    transform: rotate(90deg);
  }
}
</style>
