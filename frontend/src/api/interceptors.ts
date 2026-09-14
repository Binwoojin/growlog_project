import api from './axios'
import router from '../router'
import { useAuthStore } from '../stores/auth.store'

/*
 * 세션이 도중에 끊기면(서버 재시작, 세션 만료, 쿠키 삭제 등) 보호된 화면에
 * 있던 사용자를 자동으로 로그인 화면으로 돌려보낸다.
 *
 * 앱을 처음 열 때의 "로그인 여부 확인"용 GET /api/me 401은 이 인터셉터가
 * 아니라 authStore.fetchCurrentUser()가 자체적으로 조용히 처리하므로
 * (로그인 화면에 있을 때는 아래 조건에서 자연히 제외된다) 중복으로
 * 리다이렉트를 걸지 않는다. 여기서는 "이미 인증된 화면에 있다가 갑자기
 * 401을 받은" 경우만 대응한다.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      const currentRoute = router.currentRoute.value

      if (currentRoute.name !== 'login') {
        authStore.user = null
        router.push({ name: 'login', query: { redirect: currentRoute.fullPath } })
      }
    }

    return Promise.reject(error)
  },
)
