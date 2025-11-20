import apiClient from './axios'

/**
 * 사용자 관련 API
 */

// 유저 공개 프로필 조회
export const getUserProfile = async (userId) => {
  const response = await apiClient.get(`/users/${userId}`)
  return response.data.data
}

// 내 프로필 수정
export const updateMyProfile = async (data) => {
  const response = await apiClient.patch('/users/me', {
    nickname: data.nickname,
    statusMessage: data.statusMessage,
    profileImageUrl: data.profileImageUrl,
  })
  return response.data.data
}

// 비밀번호 변경
export const changePassword = async (currentPassword, newPassword) => {
  await apiClient.patch('/users/me/password', {
    currentPassword,
    newPassword,
  })
}

// 회원 탈퇴
export const deleteAccount = async () => {
  await apiClient.delete('/users/me')
}

// 팔로우
export const followUser = async (userId) => {
  const response = await apiClient.post(`/users/${userId}/follow`)
  return response.data.data
}

// 언팔로우
export const unfollowUser = async (userId) => {
  await apiClient.delete(`/users/${userId}/follow`)
}

// 내 팔로워 목록
export const getMyFollowers = async () => {
  const response = await apiClient.get('/users/me/followers')
  return response.data.data
}

// 내 팔로잉 목록
export const getMyFollowings = async () => {
  const response = await apiClient.get('/users/me/followings')
  return response.data.data
}

// 마이페이지 상단 요약
export const getMySummary = async () => {
  const response = await apiClient.get('/users/me/summary')
  return response.data.data
}

// 내가 만든 퀴즈 목록
export const getMyQuizzes = async () => {
  const response = await apiClient.get('/users/me/quizzes')
  return response.data.data
}

// 내가 좋아요한 퀴즈 목록
export const getMyLikedQuizzes = async () => {
  const response = await apiClient.get('/users/me/liked-quizzes')
  return response.data.data
}

// 팔로잉 유저들의 퀴즈 목록
export const getFollowingQuizzes = async (sort = 'latest') => {
  const response = await apiClient.get(`/users/me/following-quizzes?sort=${sort}`)
  return response.data.data
}

