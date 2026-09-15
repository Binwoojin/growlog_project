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
 * Scene — main panel을 normal flow에 그대로 둬서(position만 relative)
 * scene의 높이가 실제 컨텐츠 높이를 그대로 따라간다. 이전처럼 scene
 * 전체에 고정 height(460px)를 주고 세 레이어를 모두 absolute로 배치하면
 * 뷰포트/폰트 크기에 따라 내용이 길어졌을 때 패널이 잘리거나 서로
 * 벌어지는 문제가 있었다. goal/record는 main panel을 기준으로(anchor)
 * 그 모서리에 걸쳐 겹치므로, main이 커지거나 작아져도 항상 main에 맞게
 * 따라온다. goal/record가 main 아래로 살짝 튀어나오는 만큼만
 * padding-bottom으로 여유를 둔다.
 */
.hero__scene {
  position: relative;
  min-width: 0;
  padding-bottom: 72px;
}

.hero__scene-main {
  position: relative;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
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
 * Goal Progress Panel — main panel(anchor) 기준 left/bottom으로 왼쪽-
 * 아래 모서리에 걸쳐 겹친다. main의 실제 렌더 크기와 무관하게 항상 그
 * 모서리를 따라가므로 고정 top 좌표(과거 170px)에 의존하지 않는다.
 */
.hero__scene-goal {
  position: absolute;
  left: 0;
  bottom: -24px;
  width: 220px;
  max-width: 58%;
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
 * Record Card — main panel(anchor) 기준 right/bottom으로 반대쪽
 * 아래에 걸쳐 겹친다. Goal과 다른 코너를 써서 서로 충돌하지 않는다.
 */
.hero__scene-record {
  position: absolute;
  right: 0;
  bottom: -44px;
  width: 230px;
  max-width: 58%;
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

  /*
   * absolute 겹침 구성은 좁은 화면에서 깨지기 쉬워서, 세 레이어를
   * 자연스러운 세로 stack으로 전환한다(Dashboard → Goal → Record
   * 순서, 겹침 없이).
   */
  .hero__scene {
    padding-bottom: 0;
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
    margin: 0;
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
