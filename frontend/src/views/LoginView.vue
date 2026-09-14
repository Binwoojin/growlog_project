<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'

const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')

async function onSubmit() {
  try {
    await authStore.login({ email: email.value, password: password.value })
    router.push({ name: 'dashboard' })
  } catch {
    /* authStore.error에 메시지가 이미 채워져 있으므로 템플릿에서 그대로 보여준다 */
  }
}
</script>

<template>
  <main class="login">
    <h1>GrowLog 로그인</h1>

    <form @submit.prevent="onSubmit">
      <label>
        이메일
        <input v-model="email" type="email" required />
      </label>

      <label>
        비밀번호
        <input v-model="password" type="password" required />
      </label>

      <button type="submit" :disabled="authStore.status === 'loading'">
        {{ authStore.status === 'loading' ? '로그인 중...' : '로그인' }}
      </button>

      <p v-if="authStore.error" class="error">{{ authStore.error }}</p>
    </form>
  </main>
</template>

<style scoped>
.login {
  max-width: 320px;
  margin: 80px auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
}

input {
  padding: 8px;
  font-size: 14px;
}

button {
  padding: 10px;
  cursor: pointer;
}

.error {
  color: #d93025;
  font-size: 13px;
}
</style>
