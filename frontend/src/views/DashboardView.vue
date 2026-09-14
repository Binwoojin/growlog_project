<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'

/*
 * Day 1에서는 인증/CORS 연동 검증이 목표이므로 최소한의 placeholder만 둔다.
 * 실제 Dashboard UI(요약 카드, Timeline 등)는 로드맵 Day 3~4에서 구현한다.
 */
const authStore = useAuthStore()
const router = useRouter()

async function onLogout() {
  await authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <main class="dashboard">
    <p v-if="authStore.user">
      안녕하세요, {{ authStore.user.nickname }}님 👋 ({{ authStore.user.email }})
    </p>
    <button @click="onLogout">로그아웃</button>
  </main>
</template>

<style scoped>
.dashboard {
  max-width: 480px;
  margin: 80px auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

button {
  padding: 10px;
  cursor: pointer;
  width: fit-content;
}
</style>
