<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

/*
 * GrowLog의 대표 Visual Identity 섹션 — "주요 기능"이 기능을 나열한다면
 * 여기는 GrowLog를 사용했을 때 사용자가 실제로 경험하는 변화의 과정을
 * 보여준다. 기능명(Goal/Record/Timeline/Growth) 나열을 반복하지 않는다.
 *
 * 01→04로 갈수록 노드가 아주 조금씩 커지고(30→36px), 색이 --color-
 * primary-bg → --color-accent → --color-primary → --color-primary-hover
 * 순으로 짙어진다 — "작은 기록이 점점 쌓여 성장한다"는 걸 은유한다.
 *
 * Scroll Progression — Progressive Enhancement: 기본 CSS(.journey--
 * motion 없음)는 4단계가 전부 최종 활성 상태로 보인다(지금까지의 정적
 * 모습 그대로). `.journey--motion`은 JS가 motion을 켤 수 있을 때만
 * (prefers-reduced-motion이 아닐 때만) 붙고, 그때만 노드가 흐려진
 * "대기" 상태로 시작해서 스크롤에 따라 하나씩 활성화된다. 이미
 * 활성화된 단계는 다시 비활성화하지 않는다(단방향 축적).
 *
 * 구현 방식 — 처음엔 4개 노드 각각을 IntersectionObserver로 관찰했는데,
 * Desktop에서는 4개 노드가 가로로 나란히 배치돼 있어(같은 row) 뷰포트
 * 진입 시점(Y 좌표)이 사실상 동일해서 전부 동시에 활성화되는 문제가
 * 있었다 — 가로 배치에서는 "스크롤에 따라 순차 진입"이라는 신호 자체가
 * 없기 때문이다. 그래서 개별 노드 관찰 대신, 섹션 전체의 스크롤 진행률
 * (섹션이 뷰포트 중간 지점을 지나가는 비율)을 계산해서 그 진행률을
 * 4단계에 매핑하는 방식으로 바꿨다. "지속적인 scroll 리스너 금지"
 * 원칙은, 이 계산을 섹션이 뷰포트 안에 있을 때만(IntersectionObserver로
 * 게이팅) 붙였다 떼는 rAF 스로틀 리스너로 지켜서 절충했다 — 화면 어디에
 * 있든 항상 감시하는 전역 scroll 리스너가 아니라, 이 섹션 근처에 있을
 * 때만 짧게 붙는다.
 */
const steps = [
  { label: '방향을 정합니다', description: '지금 이루고 싶은 목표를 정합니다.' },
  { label: '오늘을 남깁니다', description: '작은 행동과 생각도 기록으로 남깁니다.' },
  { label: '시간이 쌓입니다', description: '기록들이 하루, 일주일, 한 달의 흐름으로 이어집니다.' },
  { label: '변화를 발견합니다', description: '쌓인 기록 속에서 내가 얼마나 달라졌는지 확인합니다.' },
]

const railEl = ref<HTMLElement | null>(null)
const activatedSteps = ref<boolean[]>(steps.map(() => false))
const journeyMotionEnabled = ref(false)
const activatedCount = computed(() => activatedSteps.value.filter(Boolean).length)
/* Mobile 세로 rail은 항목별 실제 렌더링 높이(줄바꿈에 따라 달라짐)를 CSS만으로
 * 정확히 알 수 없어서, Why GrowLog와 같은 이유로 활성화 개수 비례 scaleY로 단순화한다. */
const mobileRailFraction = computed(() => activatedCount.value / steps.length)

function activateUpTo(index: number) {
  for (let i = 0; i <= index; i++) {
    activatedSteps.value[i] = true
  }
}

let sectionObserver: IntersectionObserver | null = null
let rafId: number | null = null
let scrollListenerAttached = false

/*
 * 진행률 0 = 섹션 상단이 뷰포트 아래쪽(85%)에 막 나타나는 시점, 진행률
 * 1 = 섹션 상단이 뷰포트 중간보다 살짝 위(45%)에 닿는 시점. 4단계에
 * 균등 매핑한다. rail 자신의 높이(rect.height)를 분모로 쓰지 않는
 * 이유 — Desktop에서는 한 줄짜리 rail이라 높이가 아주 작아서(약
 * 110px), "중간을 지나 자기 높이만큼 더" 방식으로 계산하면 페이지
 * 맨 아래(Final CTA 바로 다음, 그 아래 콘텐츠가 없음)에서 스크롤이
 * 막혀 마지막 단계가 끝내 활성화되지 못하는 문제가 있었다. 뷰포트
 * 높이 비율 기반의 고정된 구간(뷰포트의 40%)으로 바꿔서 문서 맨
 * 아래에서도 항상 도달 가능하게 했다.
 */
function computeProgressStep() {
  rafId = null
  if (!railEl.value) return

  const rect = railEl.value.getBoundingClientRect()
  const viewportH = window.innerHeight
  const startTrigger = viewportH * 0.85
  const endTrigger = viewportH * 0.45
  const progress = (startTrigger - rect.top) / (startTrigger - endTrigger)
  const clamped = Math.min(1, Math.max(0, progress))
  const stepIndex = Math.min(steps.length - 1, Math.floor(clamped * steps.length))

  activateUpTo(stepIndex)
}

function onScroll() {
  if (rafId !== null) return
  rafId = requestAnimationFrame(computeProgressStep)
}

function attachScrollListener() {
  if (scrollListenerAttached) return
  window.addEventListener('scroll', onScroll, { passive: true })
  scrollListenerAttached = true
  computeProgressStep()
}

function detachScrollListener() {
  if (!scrollListenerAttached) return
  window.removeEventListener('scroll', onScroll)
  scrollListenerAttached = false
  if (rafId !== null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
}

onMounted(() => {
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (prefersReducedMotion) {
    activatedSteps.value = activatedSteps.value.map(() => true)
    return
  }

  journeyMotionEnabled.value = true
  if (!railEl.value) return

  sectionObserver = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        attachScrollListener()
        return
      }
      detachScrollListener()
      if (activatedCount.value === steps.length) {
        sectionObserver?.disconnect()
        sectionObserver = null
      }
    },
    { threshold: 0 },
  )
  sectionObserver.observe(railEl.value)
})

onBeforeUnmount(() => {
  detachScrollListener()
  sectionObserver?.disconnect()
  sectionObserver = null
})
</script>

<template>
  <section class="journey">
    <h2 class="journey__title">작은 기록이 성장으로 이어지는 과정</h2>

    <ol
      :ref="(el) => (railEl = el as HTMLElement | null)"
      class="journey__rail"
      :class="{ 'journey--motion': journeyMotionEnabled }"
      :style="{ '--mobile-rail-fraction': mobileRailFraction }"
    >
      <span
        class="journey__connector journey__connector--1"
        :class="{ 'is-active': activatedSteps[1] }"
        aria-hidden="true"
      />
      <span
        class="journey__connector journey__connector--2"
        :class="{ 'is-active': activatedSteps[2] }"
        aria-hidden="true"
      />
      <span
        class="journey__connector journey__connector--3"
        :class="{ 'is-active': activatedSteps[3] }"
        aria-hidden="true"
      />

      <li
        v-for="(step, index) in steps"
        :key="step.label"
        class="journey__node"
        :class="[`journey__node--${index}`, { 'is-active': activatedSteps[index] }]"
      >
        <span class="journey__dot" aria-hidden="true">{{ index + 1 }}</span>
        <p class="journey__label">{{ step.label }}</p>
        <p class="journey__description">{{ step.description }}</p>
      </li>
    </ol>
  </section>
</template>

<style scoped>
.journey {
  max-width: 1180px;
  margin: 0 auto;
  padding: calc(var(--space-12) * 1.2) var(--space-6);
}

/*
 * 이 섹션은 slightly-offset center로 둔다 — Hero/Why GrowLog/Feature의
 * left 정렬과, Final CTA의 정확한 center 사이에서 "중앙 제목 → 콘텐츠"
 * 패턴이 기계적으로 반복되지 않게 하는 중간 지점.
 */
.journey__title {
  margin: 0 auto var(--space-10);
  max-width: 640px;
  padding-left: var(--space-4);
  font-size: var(--font-size-section-title);
  font-weight: var(--font-weight-semibold);
  text-align: left;
}

/*
 * gap은 주지 않는다 — connector(아래)가 4개 flex:1 노드를 12.5/37.5/
 * 62.5% 고정 비율로 잇는 계산이 "노드 사이에 gap 없음"을 전제로 하기
 * 때문이다(gap을 주면 노드 중심과 connector 위치가 어긋난다). 노드
 * 사이 여백은 대신 .journey 자체의 max-width를 960→1180px로 넓혀서
 * 4개 flex:1 노드가 자연스럽게 더 넓게 벌어지게 하는 방식으로 늘렸다.
 */
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
  transition: color 0.4s ease;
}

/*
 * 마지막 단계("변화를 발견합니다")는 Journey의 도착 지점이라는 느낌을
 * 주기 위해 라벨만 살짝 더 강조한다 — 별도 카드/배경 없이 굵기와 색만
 * primary로 올린다.
 */
.journey__node--3 .journey__label {
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.journey__description {
  margin: var(--space-1) var(--space-2) 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

/*
 * Progressive Enhancement — 기본 상태(.journey--motion 없음)는 이미 위
 * 색/크기 값 그대로 "전부 활성화된 최종 모습"이다. `.journey--motion`이
 * 붙어야만(JS가 motion을 켤 때만) 연결선이 접히고 점이 흐려진 대기
 * 상태로 시작해서, `.is-active`가 붙을 때마다 원래 모습으로 펼쳐진다.
 */
.journey--motion .journey__connector {
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.6s ease;
}

.journey--motion .journey__connector.is-active {
  transform: scaleX(1);
}

.journey--motion .journey__dot {
  opacity: 0.5;
  transform: translateX(-50%) scale(0.85);
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.journey--motion .journey__node.is-active .journey__dot {
  opacity: 1;
  transform: translateX(-50%) scale(1);
}

.journey--motion .journey__label {
  color: var(--color-text-secondary);
}

.journey--motion .journey__node.is-active .journey__label {
  color: var(--color-text-primary);
}

/* 마지막 단계는 motion 활성화 상태에서도 계속 primary 톤을 유지한다 */
.journey--motion .journey__node--3.is-active .journey__label {
  color: var(--color-primary);
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
   * 각 점의 크기/색으로 계속 보인다. Scroll Progression이 켜져 있으면
   * (.journey--motion) 이 선도 활성화된 단계 수에 비례해 scaleY로 자란다.
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

  .journey--motion.journey__rail::before {
    transform: scaleY(var(--mobile-rail-fraction, 1));
    transform-origin: top;
    transition: transform 0.5s ease;
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

  .journey--motion .journey__dot {
    transform: scale(0.85);
  }

  .journey--motion .journey__node.is-active .journey__dot {
    transform: scale(1);
  }

  .journey__description {
    margin-left: 0;
  }
}
</style>
