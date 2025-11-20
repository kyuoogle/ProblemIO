import apiClient from './axios'

/**
 * 퀴즈 관련 API
 */

// 퀴즈 목록 조회
export const getQuizzes = async (params = {}) => {
  const { page = 1, size = 20, sort = 'latest', keyword = '' } = params
  const queryParams = new URLSearchParams({
    page: page.toString(),
    size: size.toString(),
    sort,
  })
  
  if (keyword) {
    queryParams.append('keyword', keyword)
  }
  
  const response = await apiClient.get(`/quizzes?${queryParams.toString()}`)
  return response.data.data  // ApiResponse 구조에 맞게 .data 추가
}

// 퀴즈 상세 조회
export const getQuiz = async (quizId) => {
  const response = await apiClient.get(`/quizzes/${quizId}`)
  return response.data.data
}

// 퀴즈 생성
export const createQuiz = async (data) => {
  const response = await apiClient.post('/quizzes', {
    title: data.title,
    description: data.description,
    thumbnailUrl: data.thumbnailUrl,
    isPublic: data.isPublic ?? true,
    questions: data.questions.map((q) => ({
      order: q.order,
      imageUrl: q.imageUrl,
      description: q.description,
      answers: q.answers.map((a) => ({
        text: a.text,
        sortOrder: a.sortOrder,
      })),
    })),
  })
  return response.data.data
}

// 퀴즈 수정
export const updateQuiz = async (quizId, data) => {
  const response = await apiClient.put(`/quizzes/${quizId}`, {
    title: data.title,
    description: data.description,
    thumbnailUrl: data.thumbnailUrl,
    isPublic: data.isPublic,
    questions: data.questions.map((q) => ({
      id: q.id,
      order: q.order,
      imageUrl: q.imageUrl,
      description: q.description,
      answers: q.answers.map((a) => ({
        id: a.id,
        text: a.text,
        sortOrder: a.sortOrder,
      })),
    })),
  })
  return response.data.data
}

// 퀴즈 삭제
export const deleteQuiz = async (quizId) => {
  await apiClient.delete(`/quizzes/${quizId}`)
}

// 퀴즈 좋아요
export const likeQuiz = async (quizId) => {
  const response = await apiClient.post(`/quizzes/${quizId}/like`)
  return response.data.data
}

// 퀴즈 좋아요 취소
export const unlikeQuiz = async (quizId) => {
  const response = await apiClient.delete(`/quizzes/${quizId}/like`)
  return response.data.data
}

