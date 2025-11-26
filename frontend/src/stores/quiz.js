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

  // 서버에서 받은 submissionId (플레이 중 동안 재사용)
  const submissionId = ref(null)

  // 🔥 [추가] 마지막 문제 정답/오답 카드 제어용 상태
  const showAnswerCard = ref(false)

  // 🔥 [추가] 현재 문제에 대한 정답/오답 정보
  const lastAnswerResult = ref({
    correct: null,
    correctAnswers: [],
    userAnswer: '',
  })

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

  // 퀴즈 시작 시 초기화
  function startQuiz(quiz) {
    currentQuiz.value = quiz
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
    submissionId.value = null

    showAnswerCard.value = false
    lastAnswerResult.value = {
      correct: null,
      correctAnswers: [],
      userAnswer: '',
    }
  }

  // 사용자 답변 저장
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

    // 🔥 정답 카드 표시 상태 On (정답 여부는 컴포넌트에서 set)
    showAnswerCard.value = true
  }

  // 🔥 컴포넌트에서 서버 응답 기반으로 정답/오답 데이터 저장
  function setLastAnswerResult(result) {
    lastAnswerResult.value = result
  }

  // 다음 문제 이동
  function nextQuestion() {
    if (
      currentQuiz.value &&
      currentQuestionIndex.value < currentQuiz.value.questions.length - 1
    ) {
      currentQuestionIndex.value++
    }

    // 정답 카드 닫기
    showAnswerCard.value = false
  }

  // 서버 결과 저장
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

    showAnswerCard.value = false
    lastAnswerResult.value = {
      correct: null,
      correctAnswers: [],
      userAnswer: '',
    }
  }

  return {
    // State
    quizForm,
    currentQuiz,
    currentQuestionIndex,
    userAnswers,
    quizResult,
    submissionId,

    // 🔥 추가된 State
    showAnswerCard,
    lastAnswerResult,

    // Actions
    addQuestion,
    removeQuestion,
    resetQuizForm,
    startQuiz,
    submitAnswer,
    setLastAnswerResult, // 추가된 함수
    nextQuestion,
    setQuizResult,
    resetQuizPlay,
  }
})
