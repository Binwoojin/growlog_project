<script setup lang="ts">
import type { GoalStatus } from '../../types/goal'
import BaseBadge from '../common/BaseBadge.vue'

defineProps<{
  status: GoalStatus
}>()

const variantByStatus: Record<GoalStatus, 'primary' | 'success' | 'neutral'> = {
  진행중: 'primary',
  완료: 'success',
  중단: 'neutral',
}
</script>

<template>
  <BaseBadge :variant="variantByStatus[status]">{{ status }}</BaseBadge>
</template>
