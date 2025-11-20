import apiClient from './axios'

/**
 * 인증 관련 API
 */

// 기본 API 경로
const BASE_URL = '/auth'

// 이메일 인증 코드 발송
export const sendEmailVerification = async (email) => {
  const response = await apiClient.post(`/auth/email/send?email=${encodeURIComponent(email)}`)
  return response.data
}

// 이메일 인증 코드 확인
export const verifyEmailCode = async (email, code) => {
  const response = await apiClient.post('/auth/email/verify', {
    email,
    code,
  })
  return response.data
}

// 회원가입
export const signup = async (data) => {
  const response = await apiClient.post(`${BASE_URL}/signup`, {
    email: data.email,
    password: data.password,
    nickname: data.nickname,
  })
  return response.data.data
}

// 로그인
export const login = async (email, password) => {
  const response = await apiClient.post(`${BASE_URL}/login`, {
    email,
    password,
  })
  
  // 토큰 저장
  if (response.data.data?.accessToken) {
    localStorage.setItem('accessToken', response.data.data.accessToken)
    // Backend TokenResponse에는 user 정보가 없으므로 별도 조회 필요
    const meResponse = await getMe()
    if (meResponse) {
      localStorage.setItem('user', JSON.stringify(meResponse))
    }
  }
  
  return response.data.data
}

// 로그아웃
export const logout = async () => {
  try {
    await apiClient.post(`${BASE_URL}/logout`)
  } finally {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
  }
}

// 내 정보 조회
export const getMe = async () => {
  const response = await apiClient.get(`${BASE_URL}/me`)
  return response.data.data
}

// 카카오 로그인 URL 조회
export const getKakaoLoginUrl = async () => {
  const response = await apiClient.get(`${BASE_URL}/kakao/login-url`)
  return response.data.data
}

// 카카오 콜백 처리
export const handleKakaoCallback = async (code) => {
  const response = await apiClient.get(`${BASE_URL}/kakao/callback?code=${code}`)
  
  // 토큰 저장
  if (response.data.data?.accessToken) {
    localStorage.setItem('accessToken', response.data.data.accessToken)
    if (response.data.data.user) {
      localStorage.setItem('user', JSON.stringify(response.data.data.user))
    }
  }
  
  return response.data.data
}

