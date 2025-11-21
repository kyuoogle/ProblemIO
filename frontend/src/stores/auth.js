// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout as apiLogout } from '@/api/auth'
import { getMe } from '@/api/user'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // 1) 초기 상태: localStorage에서 복원
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)

  // 2) 로그인 액션: 여기서 토큰 + 유저 모두 세팅
  async function loginUser(email, password) {
    try {
      // 2-1) /auth/login → accessToken 받기
      const data = await login(email, password) // { accessToken }
      accessToken.value = data.accessToken
      localStorage.setItem('accessToken', accessToken.value)

      // 2-2) 토큰 들고 /auth/me → user 정보 가져오기
      const me = await getMe()
      user.value = me
      localStorage.setItem('user', JSON.stringify(me))

      return data
    } catch (error) {
      // 에러 시 깨끗하게 초기화
      accessToken.value = null
      user.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      throw error
    }
  }

  // 3) 로그아웃
  async function logoutUser() {
    try {
      if (accessToken.value) {
        await apiLogout()
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      accessToken.value = null
      user.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      router.push('/login')
    }
  }

  // 4) 새로고침 이후 내 정보 다시 동기화
  async function refreshUser() {
    try {
      if (!accessToken.value) return
      const userData = await getMe()
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
    } catch (error) {
      console.error('Failed to refresh user:', error)
    }
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    loginUser,
    logoutUser,
    refreshUser,
  }
})
