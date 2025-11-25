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

  // =============== Actions ===============

  // 퀴즈 생성 화면에서 문제 추가
  function addQuestion() {
    quizForm.value.questions.push({
      questionOrder: quizForm.value.questions.length + 1,
      description: '',
      imageUrl: '',
      answers: [],
    })
  }

  // 퀴즈 생성 화면에서 문제 삭제
  function removeQuestion(index) {
    quizForm.value.questions.splice(index, 1)
    // 순서 재정렬
    quizForm.value.questions.forEach((q, idx) => {
      q.questionOrder = idx + 1
    })
  }

  // 퀴즈 생성 폼 초기화
  function resetQuizForm() {
    quizForm.value = {
      title: '',
      description: '',
      thumbnailUrl: '',
      questions: [],
    }
  }

  // 퀴즈 풀기 시작
  function startQuiz(quiz) {
    currentQuiz.value = quiz
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
  }

  /**
   * 사용자가 입력한 답안 저장
   * - 동일 questionId 가 이미 있으면 덮어쓰기
   * - 없으면 새로 추가
   *
   * @param {{ questionId: number, answerText: string }} answer
   */
  function submitAnswer(answer) {
    if (!answer || !answer.questionId) return

    const idx = userAnswers.value.findIndex(
      (a) => a.questionId === answer.questionId,
    )

    if (idx !== -1) {
      // 기존 답안 수정
      userAnswers.value[idx].answerText = answer.answerText
    } else {
      // 새로운 답안 추가
      userAnswers.value.push({
        questionId: answer.questionId,
        answerText: answer.answerText,
      })
    }
  }

  // 다음 문제로 이동
  function nextQuestion() {
    if (
      currentQuiz.value &&
      currentQuestionIndex.value < currentQuiz.value.questions.length - 1
    ) {
      currentQuestionIndex.value++
    }
  }

  // 서버에서 받은 퀴즈 결과 저장 (정답/오답, 정답 텍스트 등)
  function setQuizResult(result) {
    quizResult.value = result
  }

  // 퀴즈 플레이 상태 초기화
  function resetQuizPlay() {
    currentQuiz.value = null
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
  }

  return {
    // State
    quizForm,
    currentQuiz,
    currentQuestionIndex,
    userAnswers,
    quizResult,
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
