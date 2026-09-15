<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createGoal, fetchCategories } from '../api/goal.api'
import type { Category } from '../types/goal'
import { extractErrorMessage } from '../utils/errors'
import AppNav from '../components/common/AppNav.vue'
import BaseButton from '../components/common/BaseButton.vue'
import BaseCard from '../components/common/BaseCard.vue'
import BaseInput from '../components/common/BaseInput.vue'

/*
 * Day 9 — Goal 작성.
 * 생성 요청은 GoalService.saveGoal()을 그대로 타므로 진행률/상태는
 * 보내지 않는다 (항상 0%/"진행중"으로 시작한다).
 */
const router = useRouter()

const categories = ref<Category[]>([])
const categoriesStatus = ref<'loading' | 'success' | 'error'>('loading')

const goalTitle = ref('')
const goalContent = ref('')
const categoryNum = ref<number | null>(null)
const startDate = ref('')
const endDate = ref('')

const saveStatus = ref<'idle' | 'saving' | 'error'>('idle')
const errorMessage = ref('')

onMounted(async () => {
  try {
    categories.value = await fetchCategories()
    categoriesStatus.value = 'success'
  } catch {
    categoriesStatus.value = 'error'
  }
})

async function onSubmit() {
  if (!categoryNum.value) {
    saveStatus.value = 'error'
    errorMessage.value = '카테고리를 선택해주세요.'
    return
  }

  saveStatus.value = 'saving'
  errorMessage.value = ''

  try {
    await createGoal({
      goalTitle: goalTitle.value,
      goalContent: goalContent.value,
      categoryNum: categoryNum.value,
      startDate: startDate.value || null,
      endDate: endDate.value || null,
    })
    router.push({ name: 'goals' })
  } catch (error) {
    saveStatus.value = 'error'
    errorMessage.value = extractErrorMessage(error, '목표를 저장하지 못했어요. 다시 시도해주세요.')
  }
}
</script>

<template>
  <main class="goal-form">
    <header class="goal-form__header">
      <h1>목표 추가</h1>
      <AppNav />
    </header>

    <BaseCard>
      <form class="goal-form__fields" @submit.prevent="onSubmit">
        <BaseInput v-model="goalTitle" label="목표 제목" required />
        <BaseInput v-model="goalContent" label="목표 설명" />

        <label class="goal-form__field">
          <span class="goal-form__label">카테고리</span>
          <select v-model.number="categoryNum" class="goal-form__select" required>
            <option :value="null" disabled>카테고리를 선택하세요</option>
            <option v-for="category in categories" :key="category.categoryNum" :value="category.categoryNum">
              {{ category.categoryIcon }} {{ category.categoryName }}
            </option>
          </select>
          <span v-if="categoriesStatus === 'error'" class="goal-form__hint goal-form__hint--error">
            카테고리 목록을 불러오지 못했어요. 새로고침해주세요.
          </span>
        </label>

        <div class="goal-form__dates">
          <BaseInput v-model="startDate" label="시작일" type="date" />
          <BaseInput v-model="endDate" label="종료일" type="date" />
        </div>

        <BaseButton type="submit" :disabled="saveStatus === 'saving'">
          {{ saveStatus === 'saving' ? '저장 중...' : '목표 저장' }}
        </BaseButton>

        <p v-if="saveStatus === 'error'" class="goal-form__hint goal-form__hint--error">
          {{ errorMessage }}
        </p>
      </form>
    </BaseCard>
  </main>
</template>

<style scoped>
.goal-form {
  max-width: 480px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.goal-form__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
}

.goal-form__header h1 {
  font-size: var(--font-size-page-title);
  font-weight: var(--font-weight-bold);
}

.goal-form__fields {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.goal-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.goal-form__select {
  padding: var(--space-3);
  font-size: var(--font-size-base);
  font-family: inherit;
  color: var(--color-text-primary);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.goal-form__dates {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}

.goal-form__hint {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.goal-form__hint--error {
  color: var(--color-error);
}

@media (max-width: 480px) {
  .goal-form__dates {
    grid-template-columns: 1fr;
  }
}
</style>
