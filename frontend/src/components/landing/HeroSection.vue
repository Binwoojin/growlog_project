<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Flame, NotebookText, Target } from '@lucide/vue'
import BaseButton from '../common/BaseButton.vue'

/*
 * Hero — GrowLog 화면을 "카드 여러 장이 따로 떠 있는" 모습이 아니라
 * 하나의 Dashboard Frame 안에서 목표(Goal)와 기록(Record)이 파생/연결
 * 되는 장면으로 보여준다.
 *
 *   Dashboard Frame  — greeting + summary stats + Goal progress까지
 *                      전부 한 프레임 안의 nested surface로 둔다(Goal은
 *                      완전히 프레임 내부에 있다 — "Dashboard 안에서
 *                      파생되는 정보"라는 관계를 그대로 구조로 표현).
 *   Record Card      — 유일하게 프레임 경계를 살짝 넘어 겹치는 요소.
 *                      "최근 기록이 Dashboard/Timeline과 이어진다"는
 *                      관계를 프레임 밖으로 살짝 삐져나온 카드 하나로만
 *                      표현한다(Goal까지 같이 떨어뜨리면 다시 "따로 노는
 *                      카드들"처럼 보이기 때문에 겹침은 하나로 제한).
 *
 * rotate/perspective/3D/계속 움직이는 floating은 쓰지 않는다.
 */
defineProps<{
  ctaLabel: string
}>()

const emit = defineEmits<{
  cta: []
}>()

const animate = ref(false)

onMounted(() => {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (!prefersReducedMotion) {
    animate.value = true
  }
})
</script>

<template>
  <section class="hero" :class="{ 'hero--animate': animate }">
    <div class="hero__copy">
      <h1 class="hero__headline">오늘의 기록이 내일의 성장이 됩니다.</h1>
      <p class="hero__subcopy">
        GrowLog는 목표를 세우고, 매일의 과정과 변화를 기록하며, 쌓인 기록
        속에서 나의 성장을 발견하는 개인 성장 아카이브입니다.
      </p>
      <BaseButton variant="primary" class="hero__cta" @click="emit('cta')">{{ ctaLabel }}</BaseButton>
    </div>

    <div class="hero__scene" aria-hidden="true">
      <div class="hero__dashboard">
        <p class="hero__dashboard-greeting">안녕하세요, 성장러님 👋</p>

        <div class="hero__dashboard-stats">
          <div class="hero__dashboard-stat">
            <p class="hero__dashboard-stat-value">12</p>
            <p class="hero__dashboard-stat-label">이번 달 기록</p>
          </div>
          <div class="hero__dashboard-stat">
            <p class="hero__dashboard-stat-value">3</p>
            <p class="hero__dashboard-stat-label">진행 중 목표</p>
          </div>
          <div class="hero__dashboard-stat">
            <p class="hero__dashboard-stat-value">
              <Flame :size="14" :stroke-width="1.75" />
              7일
            </p>
            <p class="hero__dashboard-stat-label">연속 기록</p>
          </div>
        </div>

        <div class="hero__dashboard-goal">
          <div class="hero__dashboard-goal-head">
            <Target :size="13" :stroke-width="1.75" />
            <span>포트폴리오 완성하기</span>
          </div>
          <div class="hero__dashboard-goal-track">
            <div class="hero__dashboard-goal-fill" />
          </div>
          <span class="hero__dashboard-goal-percent">72%</span>
        </div>

        <div class="hero__dashboard-record">
          <div class="hero__dashboard-record-head">
            <NotebookText :size="13" :stroke-width="1.75" />
            <span>오늘의 기록</span>
          </div>
          <p class="hero__dashboard-record-body">작은 진전도 기록으로 남겼어요</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(480px, 1.1fr);
  align-items: center;
  gap: var(--space-12);
  max-width: 1200px;
  margin: 0 auto;
  padding: calc(var(--space-12) * 1.5) var(--space-8);
}

.hero__copy {
  max-width: 560px;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  align-items: flex-start;
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
 * Scene — Dashboard Frame이 normal flow에 그대로 있어서(position만
 * relative) scene의 높이가 실제 컨텐츠 높이를 그대로 따라간다. Record
 * Card만 그 프레임을 기준(anchor)으로 오른쪽-아래 모서리에 살짝 걸쳐
 * 겹치므로, 프레임이 커지거나 작아져도 항상 프레임에 맞게 따라온다.
 */
.hero__scene {
  position: relative;
  min-width: 0;
  padding-bottom: 64px;
}

.hero__dashboard {
  /*
   * box-sizing:border-box — 기본(content-box)에서는 padding이 max-width
   * 위에 추가로 붙어서, 이 프레임이 자기 max-width(420px)보다 좁은
   * 부모(예: Mobile 1-column grid) 안에 들어갈 때 padding만큼 부모
   * 경계를 넘어 horizontal overflow가 생겼다. border-box로 두면
   * padding이 width 안에 포함돼 부모 폭을 절대 넘지 않는다.
   */
  box-sizing: border-box;
  position: relative;
  width: 100%;
  max-width: 420px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-elevated);
}

.hero__dashboard-greeting {
  margin: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__dashboard-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-2);
}

.hero__dashboard-stat {
  box-sizing: border-box;
  padding: var(--space-3) var(--space-2);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
  text-align: center;
}

.hero__dashboard-stat-value {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.hero__dashboard-stat-label {
  margin: 2px 0 0;
  font-size: 11px;
  color: var(--color-text-secondary);
}

/*
 * Goal Progress — 별도 floating card가 아니라 Dashboard Frame 내부의
 * nested surface(한 단계 낮은 --color-bg)로 둬서 "Dashboard 정보에서
 * 파생된 세부"라는 관계를 구조 자체로 표현한다.
 */
.hero__dashboard-goal {
  box-sizing: border-box;
  padding: var(--space-4);
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.hero__dashboard-goal-head {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__dashboard-goal-head span {
  color: var(--color-text-primary);
}

.hero__dashboard-goal-track {
  height: 6px;
  border-radius: 999px;
  background: var(--color-surface);
  overflow: hidden;
}

.hero__dashboard-goal-fill {
  width: 72%;
  height: 100%;
  border-radius: 999px;
  background: var(--color-primary);
}

.hero__dashboard-goal-percent {
  display: block;
  margin-top: var(--space-1);
  text-align: right;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

/*
 * Record Card — Dashboard Frame(anchor) 기준 오른쪽-아래로 살짝 걸쳐
 * 겹치는 유일한 요소. "최근 기록이 Dashboard/Timeline과 이어진다"는
 * 관계를 프레임 밖으로 나온 카드 하나로만 표현한다.
 */
.hero__dashboard-record {
  box-sizing: border-box;
  position: absolute;
  right: -16px;
  bottom: -32px;
  width: 210px;
  max-width: 56%;
  padding: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  z-index: 1;
}

.hero__dashboard-record-head {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__dashboard-record-head span {
  color: var(--color-text-primary);
}

.hero__dashboard-record-body {
  margin: var(--space-1) 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ===== Entrance sequence (opacity/translateY) ===== */
@keyframes hero-fade-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes hero-fade-scale {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.hero--animate .hero__headline {
  animation: hero-fade-up 0.45s ease-out both;
}

.hero--animate .hero__subcopy {
  animation: hero-fade-up 0.45s ease-out both;
  animation-delay: 70ms;
}

.hero--animate .hero__cta {
  animation: hero-fade-up 0.4s ease-out both;
  animation-delay: 140ms;
}

.hero--animate .hero__dashboard {
  animation: hero-fade-scale 0.45s ease-out both;
  animation-delay: 120ms;
}

.hero--animate .hero__dashboard-record {
  animation: hero-fade-scale 0.4s ease-out both;
  animation-delay: 280ms;
}

/*
 * Tablet(768~1199px) — 2-column grid를 유지하기엔 480px min 폭이 너무
 * 빡빡해서(카피가 심하게 눌림) 억지로 축소하는 대신, 아래 900px
 * 스택 규칙으로 Tablet 전체를 자연스러운 세로 구성으로 통일한다.
 */
@media (max-width: 1199px) {
  .hero {
    padding-left: var(--space-6);
    padding-right: var(--space-6);
  }
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero__copy {
    max-width: none;
  }

  /* 좁은 화면에서는 Record Card의 겹침을 풀어 자연스러운 세로 스택으로 전환한다 */
  .hero__scene {
    padding-bottom: 0;
  }

  .hero__dashboard-record {
    position: static;
    width: auto;
    max-width: none;
    margin-top: var(--space-3);
  }
}

@media (max-width: 767px) {
  .hero {
    padding-left: var(--space-4);
    padding-right: var(--space-4);
    padding-top: var(--space-8);
    padding-bottom: var(--space-8);
  }
}
</style>
