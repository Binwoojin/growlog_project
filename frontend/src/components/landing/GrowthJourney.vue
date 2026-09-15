<script setup lang="ts">
/*
 * GrowLog의 대표 Visual Identity 섹션 — "주요 기능"이 기능을 나열한다면
 * 여기는 GrowLog를 사용했을 때 사용자가 실제로 경험하는 변화의 과정을
 * 보여준다. 기능명(Goal/Record/Timeline/Growth) 나열을 반복하지 않는다.
 *
 * 01→04로 갈수록 노드가 아주 조금씩 커지고(30→36px), 색이 --color-
 * primary-bg → --color-accent → --color-primary → --color-primary-hover
 * 순으로 짙어진다 — "작은 기록이 점점 쌓여 성장한다"는 걸 은유한다.
 * 연결선(desktop)도 같은 3개 톤을 구간별로 써서 "line progress"를
 * 표현했지만, 굵기는 1.5px로 얇게 유지했다. 애니메이션은 없다.
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
      <span class="journey__connector journey__connector--1" aria-hidden="true" />
      <span class="journey__connector journey__connector--2" aria-hidden="true" />
      <span class="journey__connector journey__connector--3" aria-hidden="true" />

      <li v-for="(step, index) in steps" :key="step.label" class="journey__node" :class="`journey__node--${index}`">
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

.journey__title {
  margin: 0 0 var(--space-8);
  font-size: var(--font-size-section-title);
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

/* 구간별로 --color-primary-bg → --color-accent → --color-primary 순서로 짙어진다 */
.journey__connector {
  position: absolute;
  top: 17px;
  height: 1.5px;
}

.journey__connector--1 {
  left: 12.5%;
  width: 25%;
  background: var(--color-primary-bg);
}

.journey__connector--2 {
  left: 37.5%;
  width: 25%;
  background: var(--color-accent);
}

.journey__connector--3 {
  left: 62.5%;
  width: 25%;
  background: var(--color-primary);
}

.journey__node {
  position: relative;
  flex: 1;
  padding-top: 48px;
  text-align: center;
}

.journey__dot {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
}

/* 노드 크기(30→36px)와 색(연함→짙음)이 단계마다 아주 조금씩 진행된다 */
.journey__node--0 .journey__dot {
  top: 3px;
  width: 30px;
  height: 30px;
  background: var(--color-primary-bg);
  color: var(--color-text-primary);
}

.journey__node--1 .journey__dot {
  top: 2px;
  width: 32px;
  height: 32px;
  background: var(--color-accent);
  color: var(--color-text-primary);
}

.journey__node--2 .journey__dot {
  top: 1px;
  width: 34px;
  height: 34px;
  background: var(--color-primary);
  color: var(--color-text-inverse);
}

.journey__node--3 .journey__dot {
  top: 0;
  width: 36px;
  height: 36px;
  background: var(--color-primary-hover);
  color: var(--color-text-inverse);
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

  /*
   * 세로 스택에서는 각 노드의 본문 줄 수가 달라 실제 중심 좌표를 CSS만으로
   * 정확히 계산할 수 없다. desktop처럼 구간별 톤을 정확히 맞추는 대신
   * 하나의 은은한 세로선으로 단순화해서 "흐름"만 유지한다 — 단계별 진행은
   * 각 점의 크기/색으로 계속 보인다.
   */
  .journey__connector {
    display: none;
  }

  .journey__rail::before {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    left: 17px;
    width: 1.5px;
    background: var(--color-primary-bg);
  }

  .journey__node {
    padding-top: 0;
    padding-left: 48px;
    text-align: left;
  }

  .journey__node--0 .journey__dot,
  .journey__node--1 .journey__dot,
  .journey__node--2 .journey__dot,
  .journey__node--3 .journey__dot {
    top: 0;
    left: 0;
    transform: none;
  }

  .journey__description {
    margin-left: 0;
  }
}
</style>
