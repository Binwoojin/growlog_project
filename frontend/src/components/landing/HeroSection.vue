<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Flame, NotebookText, Target } from '@lucide/vue'
import BaseButton from '../common/BaseButton.vue'

/*
 * Hero — "카드 하나"가 아니라 GrowLog의 여러 기록 요소가 한 장면을
 * 이루는 layered composition. 세 레이어 사이에 의미 관계가 있다:
 *
 *   Dashboard Main Panel   — 현재 성장 상태를 보여주는 중심(가장 크고,
 *                            가장 위 z-index, --shadow-elevated)
 *   Goal Progress Panel    — 그 목표 정보에서 파생된 세부 상태(main
 *                            panel 왼쪽 아래에 걸쳐 겹침, --shadow-card)
 *   Record Card            — 최근 기록이 Dashboard/Timeline과 연결되는
 *                            요소(더 아래, main panel 하단에 걸쳐 겹침)
 *
 * 세 레이어 모두 2D 위치 오프셋 + 겹침으로만 관계를 표현한다 — rotate/
 * perspective/3D/계속 움직이는 floating은 쓰지 않는다. 점/선 장식
 * 모티프는 Hero에서 완전히 제거했다(Growth Journey/Timeline Preview로
 * 대표 사용처를 좁힘). Hero의 정체성은 이 레이어드 구성과 typography로
 * 전달한다.
 *
 * 실제 API에는 연결하지 않고 정적 프레젠테이션 데이터만 쓰되, GrowLog가
 * 실제로 보여주는 정보(이번 달 기록 수/진행 중 목표 수/연속 기록, Goal
 * Progress, 최근 타임라인 1개)만 축약해서 보여준다.
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
      <p class="hero__label">PERSONAL GROWTH ARCHIVE</p>
      <h1 class="hero__headline">오늘의 기록이 내일의 성장이 됩니다.</h1>
      <p class="hero__subcopy">
        GrowLog는 목표를 세우고, 매일의 과정과 변화를 기록하며, 쌓인 기록
        속에서 나의 성장을 발견하는 개인 성장 아카이브입니다.
      </p>
      <BaseButton variant="primary" class="hero__cta" @click="emit('cta')">{{ ctaLabel }}</BaseButton>
    </div>

    <div class="hero__scene" aria-hidden="true">
      <div class="hero__scene-main">
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
      </div>

      <div class="hero__scene-goal">
        <div class="hero__scene-goal-head">
          <Target :size="13" :stroke-width="1.75" />
          <span>포트폴리오 완성하기</span>
        </div>
        <div class="hero__preview-goal-track">
          <div class="hero__preview-goal-fill" />
        </div>
        <span class="hero__scene-goal-percent">72%</span>
      </div>

      <div class="hero__scene-record">
        <div class="hero__scene-record-head">
          <NotebookText :size="13" :stroke-width="1.75" />
          <span>오늘의 기록</span>
        </div>
        <p class="hero__scene-record-body">작은 진전도 기록으로 남겼어요</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  gap: var(--space-12);
  max-width: 1220px;
  margin: 0 auto;
  padding: calc(var(--space-12) * 1.5) var(--space-6);
}

.hero__copy {
  flex: 1;
  max-width: 560px;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  align-items: flex-start;
}

.hero__label {
  margin: 0;
  font-size: 11px;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.08em;
  color: var(--color-text-secondary);
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
 * Scene — 세 레이어가 겹치는 고정 캔버스. 폭은 Hero 전체가 넓어진 만큼
 * copy(560px 상한)보다 훨씬 넉넉하게 쓴다.
 */
.hero__scene {
  position: relative;
  flex: 1.15;
  min-width: 0;
  height: 460px;
}

.hero__scene-main {
  position: absolute;
  top: 0;
  right: 0;
  width: 360px;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-elevated);
  z-index: 3;
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

/*
 * Goal Progress Panel — Dashboard의 목표 정보에서 파생된 세부라는 관계가
 * 느껴지도록 main panel의 왼쪽-아래 모서리에 걸쳐 겹친다. depth를
 * --shadow-card로 한 단계 낮춰서 main panel보다 뒤/아래에 있다는 걸
 * 표현한다.
 */
.hero__scene-goal {
  position: absolute;
  left: 0;
  top: 170px;
  width: 230px;
  max-width: 62%;
  padding: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  z-index: 2;
}

.hero__scene-goal-head {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-2);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__scene-goal-head span {
  color: var(--color-text-primary);
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

.hero__scene-goal-percent {
  display: block;
  margin-top: var(--space-1);
  text-align: right;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

/*
 * Record Card — "최근 기록이 Dashboard/Timeline과 연결된다"는 관계를
 * main panel 하단에 걸쳐 겹치는 위치로 표현한다. Goal panel과는 다른
 * 코너(오른쪽)에 둬서 세 레이어가 한 대각선 흐름(위→아래, 요약→세부→
 * 최근 활동)으로 읽히게 했다.
 */
.hero__scene-record {
  position: absolute;
  right: 24px;
  bottom: 0;
  width: 250px;
  max-width: 66%;
  padding: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  z-index: 2;
}

.hero__scene-record-head {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.hero__scene-record-head span {
  color: var(--color-text-primary);
}

.hero__scene-record-body {
  margin: var(--space-1) 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ===== Entrance sequence (opacity/translateY, scene 레이어만 scale 추가) ===== */
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

.hero--animate .hero__label {
  animation: hero-fade-up 0.4s ease-out both;
}

.hero--animate .hero__headline {
  animation: hero-fade-up 0.45s ease-out both;
  animation-delay: 70ms;
}

.hero--animate .hero__subcopy {
  animation: hero-fade-up 0.45s ease-out both;
  animation-delay: 140ms;
}

.hero--animate .hero__cta {
  animation: hero-fade-up 0.4s ease-out both;
  animation-delay: 210ms;
}

.hero--animate .hero__scene-main {
  animation: hero-fade-scale 0.45s ease-out both;
  animation-delay: 180ms;
}

.hero--animate .hero__scene-goal {
  animation: hero-fade-scale 0.4s ease-out both;
  animation-delay: 320ms;
}

.hero--animate .hero__scene-record {
  animation: hero-fade-scale 0.4s ease-out both;
  animation-delay: 420ms;
}

@media (max-width: 900px) {
  .hero__scene {
    height: 420px;
  }

  .hero__scene-main {
    width: 320px;
  }
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
    align-items: stretch;
    padding: var(--space-8) var(--space-4);
  }

  .hero__copy {
    max-width: none;
  }

  /*
   * Mobile — absolute 겹침 구성은 좁은 화면에서 깨지기 쉬워서, 세
   * 레이어를 자연스러운 세로 stack으로 전환한다(main → goal → record
   * 순서, 겹침 없이).
   */
  .hero__scene {
    height: auto;
    display: flex;
    flex-direction: column;
    gap: var(--space-3);
  }

  .hero__scene-main,
  .hero__scene-goal,
  .hero__scene-record {
    position: static;
    width: auto;
    max-width: none;
  }
}
</style>
