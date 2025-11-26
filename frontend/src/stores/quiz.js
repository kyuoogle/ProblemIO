// frontend/src/stores/quiz.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useQuizStore = defineStore('quiz', () => {
  // Quiz Creation State
  const quizForm = ref({
    title: '',
    description: '',
    thumbnailUrl: '',
    questions: [],
  })

  // Quiz Play State
  const currentQuiz = ref(null)
  const currentQuestionIndex = ref(0)
  const userAnswers = ref([])
  const quizResult = ref(null)
  // ✅ 제출 흐름용 submissionId (서버에서 받은 걸 계속 재사용)
  const submissionId = ref(null)

  // ----- Actions -----

  function addQuestion() {
    quizForm.value.questions.push({
      questionOrder: quizForm.value.questions.length + 1,
      description: '',
      imageUrl: '',
      answers: [],
    })
  }

  function removeQuestion(index) {
    quizForm.value.questions.splice(index, 1)
    quizForm.value.questions.forEach((q, idx) => {
      q.questionOrder = idx + 1
    })
  }

  function resetQuizForm() {
    quizForm.value = {
      title: '',
      description: '',
      thumbnailUrl: '',
      questions: [],
    }
  }

  // 퀴즈 시작할 때 초기화
  function startQuiz(quiz) {
    currentQuiz.value = quiz
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
    submissionId.value = null
  }

  // questionId 기준으로 답안 저장/갱신
  function submitAnswer(answer) {
    if (!answer || !answer.questionId) return
    const idx = userAnswers.value.findIndex(
      (a) => a.questionId === answer.questionId,
    )
    if (idx !== -1) {
      userAnswers.value[idx].answerText = answer.answerText
    } else {
      userAnswers.value.push({
        questionId: answer.questionId,
        answerText: answer.answerText,
      })
    }
  }

  function nextQuestion() {
    if (
      currentQuiz.value &&
      currentQuestionIndex.value < currentQuiz.value.questions.length - 1
    ) {
      currentQuestionIndex.value++
    }
  }

  // 서버 응답 저장 + submissionId 갱신
  function setQuizResult(result) {
    quizResult.value = result
    if (result && result.submissionId) {
      submissionId.value = result.submissionId
    }
  }

  function resetQuizPlay() {
    currentQuiz.value = null
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
    submissionId.value = null
  }

  return {
    // State
    quizForm,
    currentQuiz,
    currentQuestionIndex,
    userAnswers,
    quizResult,
    submissionId,
    // Actions
    addQuestion,
    removeQuestion,
    resetQuizForm,
    startQuiz,
    submitAnswer,
    nextQuestion,
    setQuizResult,
    resetQuizPlay,
  }
})
