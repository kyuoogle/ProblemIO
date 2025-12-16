import instance from './axios'

// 관리자용 퀴즈 목록 조회 (숨김 포함)
export const getAdminQuizzes = async (params) => {
  const response = await instance.get('/admin/quizzes', { params })
  return response.data
}

// 퀴즈 숨김 처리 토글
export const toggleQuizVisibility = async (quizId) => {
  const response = await instance.patch(`/admin/quizzes/${quizId}/hide`)
  return response.data
}

// 챌린지 생성
export const createChallenge = async (challengeData) => {
  const response = await instance.post('/admin/challenges', challengeData)
  return response.data
}

// 커스텀 아이템 생성
export const createCustomItem = async (itemData) => {
    const response = await instance.post('/admin/items', itemData)
    return response.data
}

// 커스텀 아이템 목록 조회
export const getCustomItems = async () => {
    const response = await instance.get('/admin/items')
    return response.data
}

// 아이템 유저 할당
export const assignItemToUser = async (itemId, userId) => {
    const response = await instance.post(`/admin/items/${itemId}/assign/${userId}`)
    return response.data
}
