<script setup lang="ts">
/*
 * "주요 기능"이 기능 목록을 나열하는 섹션이라면, 여기는 GrowLog를
 * 사용했을 때 사용자가 실제로 경험하는 변화의 과정을 보여주는
 * 섹션이다 — 기능명(Goal/Record/Timeline/Growth) 나열을 반복하지
 * 않는다.
 *
 * emoji를 쓰지 않고 번호가 있는 점(dot)과 연결선으로만 표현한다 —
 * GrowLog Visual Language "점 → 선 → 흐름 → 축적 → 성장"을 가장
 * 직접적으로 대표하는 요소로 이 섹션을 쓴다. 정적 레이아웃만 쓰고
 * 애니메이션은 추가하지 않는다.
 */
const steps = [
  { label: '방향을 정합니다', description: '지금 이루고 싶은 목표를 정합니다.' },
  { label: '오늘을 남깁니다', description: '작은 행동과 생각도 기록으로 남깁니다.' },
  { label: '시간이 쌓입니다', description: '기록들이 하루, 일주일, 한 달의 흐름으로 이어집니다.' },
  { label: '변화를 발견합니다', description: '쌓인 기록 속에서 내가 얼마나 달라졌는지 확인합니다.' },
]
</script>

<template>
  <section class="journey">
    <h2 class="journey__title">작은 기록이 성장으로 이어지는 과정</h2>

    <ol class="journey__rail">
      <li v-for="(step, index) in steps" :key="step.label" class="journey__node">
        <span class="journey__dot" aria-hidden="true">{{ index + 1 }}</span>
        <p class="journey__label">{{ step.label }}</p>
        <p class="journey__description">{{ step.description }}</p>
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

/* LandingView.vue의 intro__title과 같은 이유로 로컬 값을 쓴다 */
.journey__title {
  margin: 0 0 var(--space-8);
  font-size: 24px;
  font-weight: var(--font-weight-semibold);
  text-align: center;
}

.journey__rail {
  position: relative;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
}

/*
 * 점들을 잇는 연결선 — 첫 점과 마지막 점의 중심 사이만 지나가도록 좌우를
 * 인셋한다. Primary Green 점과 연결되는 선이라 중립 border보다 살짝
 * 진한 Soft Green 톤(--color-primary-bg)을 써서 연결감을 또렷하게 했다.
 */
.journey__rail::before {
  content: '';
  position: absolute;
  top: 16px;
  left: 12.5%;
  right: 12.5%;
  height: 2px;
  background: var(--color-primary-bg);
}

.journey__node {
  position: relative;
  flex: 1;
  padding-top: 48px;
  text-align: center;
}

.journey__dot {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-inverse);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
}

.journey__label {
  margin: 0;
  font-weight: var(--font-weight-medium);
}

.journey__description {
  margin: var(--space-1) var(--space-2) 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

@media (max-width: 720px) {
  .journey__rail {
    flex-direction: column;
    gap: var(--space-6);
  }

  .journey__rail::before {
    top: 0;
    bottom: 0;
    left: 15px;
    right: auto;
    width: 2px;
    height: auto;
  }

  .journey__node {
    padding-top: 0;
    padding-left: 48px;
    text-align: left;
  }

  .journey__dot {
    top: 0;
    left: 0;
    transform: none;
  }

  .journey__description {
    margin-left: 0;
  }
}
</style>
