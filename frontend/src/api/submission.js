import apiClient from './axios'

/**
 * 풀이 제출 관련 API
 */

// 퀴즈 풀이 제출
export const submitQuiz = async (quizId, answers) => {
  const response = await apiClient.post(`/quizzes/${quizId}/submissions`, {
    answers: answers.map((a) => ({
      questionId: a.questionId,
      answerText: a.answerText,
    })),
  })
  return response.data.data
}

// 풀이 결과 상세 조회
export const getSubmission = async (submissionId) => {
  const response = await apiClient.get(`/submissions/${submissionId}`)
  return response.data.data
}

