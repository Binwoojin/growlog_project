<script setup lang="ts">
/*
 * href가 주어지면 <a>로 렌더링한다 — 레거시 JSP 화면처럼 Vue Router가
 * 모르는 주소(예: /record/write)로 안내하는 버튼이 필요할 때, 일반
 * <button disabled>로 죽은 UI를 남기는 대신 실제 이동 가능한 링크로
 * 쓸 수 있게 하기 위함이다.
 */
withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost'
    type?: 'button' | 'submit'
    disabled?: boolean
    href?: string
  }>(),
  {
    variant: 'primary',
    type: 'button',
    disabled: false,
  },
)
</script>

<template>
  <a v-if="href" :href="href" class="base-button" :class="`variant-${variant}`">
    <slot />
  </a>
  <button v-else :type="type" :disabled="disabled" class="base-button" :class="`variant-${variant}`">
    <slot />
  </button>
</template>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
  cursor: pointer;
  transition: background-color 0.15s, border-color 0.15s, opacity 0.15s;
}

.base-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.variant-primary {
  background: var(--color-primary);
  color: var(--color-text-inverse);
}
.variant-primary:not(:disabled):hover {
  background: var(--color-primary-hover);
}

.variant-secondary {
  background: var(--color-surface);
  color: var(--color-text-primary);
  border-color: var(--color-border);
}
.variant-secondary:not(:disabled):hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.variant-ghost {
  background: transparent;
  color: var(--color-text-secondary);
}
.variant-ghost:not(:disabled):hover {
  background: var(--color-bg);
  color: var(--color-text-primary);
}
</style>
