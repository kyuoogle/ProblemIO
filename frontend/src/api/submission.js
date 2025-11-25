import apiClient from './axios'

// 퀴즈 풀이 제출 (한 문제씩)
export const submitQuiz = async (quizId, payload) => {
  // payload: { submissionId?, questionId, answerText }
  const response = await apiClient.post(`/quizzes/${quizId}/submissions`, payload)
  return response.data.data   // QuizAnswerResponse
}

// 풀이 결과 상세 조회 (전체 통계용 – 나중에 사용)
export const getSubmission = async (submissionId) => {
  const response = await apiClient.get(`/submissions/${submissionId}`)
  return response.data.data
}

