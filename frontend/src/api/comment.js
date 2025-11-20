import apiClient from './axios'

/**
 * 댓글 관련 API
 */

// 댓글 목록 조회
export const getComments = async (quizId) => {
  const response = await apiClient.get(`/quizzes/${quizId}/comments`)
  return response.data.data
}

// 댓글 작성
export const createComment = async (quizId, content) => {
  const response = await apiClient.post(`/quizzes/${quizId}/comments`, {
    content,
  })
  return response.data.data
}

// 댓글 삭제
export const deleteComment = async (commentId) => {
  await apiClient.delete(`/comments/${commentId}`)
}

// 댓글 좋아요
export const likeComment = async (commentId) => {
  const response = await apiClient.post(`/comments/${commentId}/like`)
  return response.data.data
}

// 댓글 좋아요 취소
export const unlikeComment = async (commentId) => {
  const response = await apiClient.delete(`/comments/${commentId}/like`)
  return response.data.data
}

