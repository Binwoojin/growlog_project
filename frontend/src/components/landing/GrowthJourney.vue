<script setup lang="ts">
/*
 * "주요 기능"이 기능 목록을 나열하는 섹션이라면, 여기는 GrowLog를
 * 사용했을 때 사용자가 실제로 경험하는 변화의 과정을 보여주는
 * 섹션이다 — 기능명(Goal/Record/Timeline/Growth) 나열을 반복하지
 * 않는다. 이모지는 와이어프레임 placeholder이며 최종 디자인 요소가
 * 아니다.
 */
const steps = [
  { icon: '🎯', label: '방향을 정합니다', description: '지금 이루고 싶은 목표를 정합니다.' },
  { icon: '📝', label: '오늘을 남깁니다', description: '작은 행동과 생각도 기록으로 남깁니다.' },
  { icon: '🗓️', label: '시간이 쌓입니다', description: '기록들이 하루, 일주일, 한 달의 흐름으로 이어집니다.' },
  { icon: '📈', label: '변화를 발견합니다', description: '쌓인 기록 속에서 내가 얼마나 달라졌는지 확인합니다.' },
]
</script>

<template>
  <section class="journey">
    <h2 class="journey__title">작은 기록이 성장으로 이어지는 과정</h2>

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
  width: 180px;
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
