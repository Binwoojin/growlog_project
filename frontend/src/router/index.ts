import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.store'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    name: 'dashboard',
    component: () => import('../views/DashboardView.vue'),
  },
  {
    path: '/timeline',
    name: 'timeline',
    component: () => import('../views/TimelineView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/*
 * 새로고침 등으로 앱이 처음 로드되면 로그인 여부를 알 수 없으므로
 * 첫 네비게이션에서 한 번 GET /api/me로 세션을 확인한다.
 */
router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authStore.initialized) {
    await authStore.fetchCurrentUser()
  }

  if (!to.meta.public && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.name === 'login' && authStore.isAuthenticated) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
