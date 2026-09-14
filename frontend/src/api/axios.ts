import axios from 'axios'

/*
 * 백엔드(Spring Boot)와 통신할 Axios 인스턴스.
 *
 * withCredentials: true
 *   세션 쿠키(JSESSIONID)를 Cross-Origin 요청에도 실어 보내기 위해 필수.
 *   이게 없으면 로그인에 성공해도 브라우저가 세션 쿠키를 저장/전송하지 않는다.
 *
 * xsrfCookieName / xsrfHeaderName
 *   Spring Security가 CookieCsrfTokenRepository로 내려주는 쿠키 이름(XSRF-TOKEN)과
 *   기대하는 헤더 이름(X-XSRF-TOKEN)을 Axios 기본값과 똑같이 맞춘 것.
 *   두 값이 서버/클라이언트에서 일치하면 Axios가 CSRF 토큰을 자동으로
 *   쿠키에서 읽어 헤더에 넣어 보내주기 때문에 별도 코드가 필요 없다.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

export default api
