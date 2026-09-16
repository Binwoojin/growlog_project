<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@lucide/vue'
import { fetchRecordDetail } from '../api/record.api'
import type { RecordDetail } from '../types/record'
import { formatTimelineDate } from '../utils/date'
import AppNav from '../components/common/AppNav.vue'
import BaseBadge from '../components/common/BaseBadge.vue'
import BaseButton from '../components/common/BaseButton.vue'
import BaseCard from '../components/common/BaseCard.vue'

/*
 * Timeline의 성장 기록 카드를 클릭하면 이 화면으로 온다(detailUrl이 이미
 * "/record/{recordNum}" 형태라 라우트 경로를 그대로 맞췄다).
 *
 * 새 Create/Edit 화면이 아니라 "읽기 전용" 화면이므로 GrowthRecordApiController.
 * detail()이 이미 검증까지 끝낸(본인 소유 확인) 데이터를 그대로 보여주기만 한다.
 * 백엔드는 기록이 없거나 다른 회원의 기록일 때 동일하게 400 + 메시지를
 * 내려주므로, 그 상태를 별도 'not-found'로 구분해서 보여준다(진짜 네트워크/서버
 * 오류인 'error'와 문구를 다르게 해서 사용자가 뒤로 갈지 다시 시도할지
 * 판단할 수 있게 했다).
 */
const props = defineProps<{ recordNum: string }>()
const router = useRouter()

const record = ref<RecordDetail | null>(null)
const status = ref<'loading' | 'success' | 'not-found' | 'error'>('loading')

onMounted(async () => {
  try {
    record.value = await fetchRecordDetail(props.recordNum)
    status.value = 'success'
  } catch (error: unknown) {
    const isKnownInvalid =
      typeof error === 'object' &&
      error !== null &&
      'response' in error &&
      (error as { response?: { status?: number } }).response?.status === 400
    status.value = isKnownInvalid ? 'not-found' : 'error'
  }
})

function goBack() {
  router.back()
}
</script>

<template>
  <main class="record-detail">
    <header class="record-detail__header">
      <AppNav />
    </header>

    <BaseCard v-if="status === 'loading'" class="record-detail__status">
      <p>불러오는 중...</p>
    </BaseCard>

    <BaseCard v-else-if="status === 'not-found'" class="record-detail__status">
      <p>기록을 찾을 수 없어요. 삭제되었거나 접근 권한이 없는 기록일 수 있어요.</p>
      <BaseButton variant="secondary" @click="goBack">
        <ArrowLeft :size="14" :stroke-width="1.75" />
        뒤로가기
      </BaseButton>
    </BaseCard>

    <BaseCard v-else-if="status === 'error'" class="record-detail__status">
      <p>기록을 불러오지 못했어요. 잠시 후 다시 시도해주세요.</p>
      <BaseButton variant="secondary" @click="goBack">
        <ArrowLeft :size="14" :stroke-width="1.75" />
        뒤로가기
      </BaseButton>
    </BaseCard>

    <template v-else-if="record">
      <BaseCard class="record-detail__card">
        <div class="record-detail__meta">
          <BaseBadge v-if="record.goal" variant="primary">{{ record.goal.goalTitle }}</BaseBadge>
          <span class="record-detail__date">{{ formatTimelineDate(record.createdAt) }}</span>
        </div>

        <h1 class="record-detail__title">{{ record.title }}</h1>

        <p class="record-detail__content">{{ record.content }}</p>

        <div v-if="record.mediaList.length > 0" class="record-detail__media">
          <template v-for="media in record.mediaList" :key="media.mediaNum">
            <img
              v-if="media.mediaType === 'IMAGE'"
              :src="media.mediaUrl"
              alt=""
              class="record-detail__image"
            />
            <div v-else class="record-detail__youtube">
              <iframe
                :src="media.mediaUrl"
                title="YouTube video"
                frameborder="0"
                allowfullscreen
              ></iframe>
            </div>
          </template>
        </div>

        <BaseButton variant="secondary" class="record-detail__back" @click="goBack">
          <ArrowLeft :size="14" :stroke-width="1.75" />
          뒤로가기
        </BaseButton>
      </BaseCard>
    </template>
  </main>
</template>

<style scoped>
.record-detail {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.record-detail__header {
  display: flex;
  justify-content: flex-end;
}

.record-detail__status {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-4);
  color: var(--color-text-secondary);
}

.record-detail__card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.record-detail__meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.record-detail__date {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.record-detail__title {
  font-size: var(--font-size-page-title);
  font-weight: var(--font-weight-bold);
}

.record-detail__content {
  color: var(--color-text-primary);
  font-size: var(--font-size-base);
  line-height: 1.7;
  white-space: pre-wrap;
}

.record-detail__media {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.record-detail__image {
  width: 100%;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}

.record-detail__youtube {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
}

.record-detail__youtube iframe {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border-radius: var(--radius-sm);
}

.record-detail__back {
  align-self: flex-start;
}
</style>
