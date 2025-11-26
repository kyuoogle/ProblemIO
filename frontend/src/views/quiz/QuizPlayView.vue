<template>
  <div class="quiz-play-container">
    <div class="container mx-auto px-4 max-w-4xl">
      <div v-if="!quizStore.currentQuiz" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else class="flex flex-col gap-6">
        <!-- 상단 제목 + 진행도 -->
        <div class="mb-4">
          <div class="flex justify-between items-center mb-2">
            <h2 class="text-2xl font-bold m-0">
              {{ quizStore.currentQuiz.title }}
            </h2>
            <Badge
              :value="`${quizStore.currentQuestionIndex + 1} / ${quizStore.currentQuiz.questions.length}`"
              severity="info"
            />
          </div>
          <ProgressBar :value="progressPercentage" class="h-1rem" />
        </div>

        <Card>
          <template #content>
            <div class="flex flex-col gap-6">
              <!-- 1) 평소 문제 풀이 화면 -->
              <template v-if="!quizStore.showAnswerCard">
                <div
                  v-if="currentQuestion?.imageUrl"
                  class="aspect-video bg-surface-100 overflow-hidden border-round"
                >
                  <img
                    :src="currentQuestion.imageUrl"
                    :alt="currentQuestion.description"
                    class="w-full h-full object-contain" 
                  />
                </div>

                <div v-if="currentQuestion?.description" class="text-xl">
                  {{ currentQuestion.description }}
                </div>

                <div class="flex flex-col gap-3">
                  <label class="text-lg font-semibold">Your Answer:</label>
                  <InputText
                    ref="answerInput"
                    v-model="currentAnswer"
                    placeholder="Enter your answer..."
                    class="w-full"
                  />
                </div>

                <div class="flex">
                  <Button
                    label="제출"
                    icon="pi pi-check"
                    class="flex-1"
                    :disabled="!currentAnswer.trim() || submitting"
                    :loading="submitting"
                    @click="submitAnswer"
                  >
                  </Button>
                </div>
              </template>

              <!-- 2) 한 문제에 대한 정답/오답 카드 -->
              <template v-else>
                <!-- 문제 이미지도 같이 보여주기 -->
                <div
                  v-if="currentQuestion?.imageUrl"
                  class="aspect-video bg-surface-100 overflow-hidden border-round"
                >
                  <img
                    :src="currentQuestion.imageUrl"
                    :alt="currentQuestion.description"
                    class="w-full h-full object-contain"
                  />
                </div>

                <div class="flex flex-col items-center text-center gap-4 py-6">
                  <div
                    class="text-2xl font-bold"
                    :class="quizStore.lastAnswerResult.correct ? 'text-green-500' : 'text-red-500'"
                  >
                    {{ quizStore.lastAnswerResult.correct ? '정답!' : '오답!' }}
                  </div>

                  <!-- 정답 문구 -->
                  <div
                    v-if="quizStore.lastAnswerResult.correctAnswers && quizStore.lastAnswerResult.correctAnswers.length"
                    class="text-lg"
                  >
                    <div class="font-semibold mb-1">정답은</div>
                    <div>
                      {{ quizStore.lastAnswerResult.correctAnswers.join(', ') }}
                    </div>
                  </div>

                  <!-- 사용자가 입력한 답 -->
                  <div
                    v-if="quizStore.lastAnswerResult.userAnswer"
                    class="text-sm text-surface-500"
                  >
                    당신의 답: "{{ quizStore.lastAnswerResult.userAnswer }}"
                  </div>

                  <div class="flex w-full mt-4">
                    <!-- 마지막 문제가 아니면 다음 문제로 -->
                    <Button
                      v-if="!isLastQuestion"
                      label="다음 문제"
                      icon="pi pi-arrow-right"
                      class="flex-1"
                      @click="handleNextAfterAnswer"
                    >
                    </Button>

                    <!-- 마지막 문제면 결과 페이지로 -->
                    <Button
                      v-else
                      label="결과 보기"
                      icon="pi pi-chart-bar"
                      class="flex-1"
                      @click="goToResult"
                    >
                    </Button>
                  </div>
                </div>
              </template>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useQuizStore } from '@/stores/quiz'
import { submitQuiz as submitQuizAPI } from '@/api/submission'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const quizStore = useQuizStore()

const currentAnswer = ref('')
const submitting = ref(false)

// InputText DOM 접근용 ref
const answerInput = ref<any>(null)

const currentQuestion = computed(() => {
  if (!quizStore.currentQuiz) return null
  return quizStore.currentQuiz.questions[quizStore.currentQuestionIndex]
})

const progressPercentage = computed(() => {
  if (!quizStore.currentQuiz) return 0
  return (
    ((quizStore.currentQuestionIndex + 1) /
      quizStore.currentQuiz.questions.length) *
    100
  )
})

const isLastQuestion = computed(() => {
  if (!quizStore.currentQuiz) return false
  return (
    quizStore.currentQuestionIndex ===
    quizStore.currentQuiz.questions.length - 1
  )
})

/**
 * 답 입력창에 포커스를 주는 헬퍼
 * - PrimeVue InputText는 컴포넌트일 수 있으니 $el 및 내부 input까지 처리
 */
const focusAnswerInput = () => {
  nextTick(() => {
    const target = answerInput.value?.$el ?? answerInput.value
    if (!target) return

    const inputEl =
      target instanceof HTMLInputElement
        ? target
        : target.querySelector?.('input')

    inputEl?.focus()
  })
}

/**
 * 엔터 키 입력 공통 처리
 * - 정답 카드가 아직 안 떠 있으면: 제출
 * - 정답 카드가 떠 있으면:
 *   - 마지막 문제가 아니면: 다음 문제
 *   - 마지막 문제면: 결과 페이지로
 */
const handleEnter = () => {
  if (!currentQuestion.value) return

  // 아직 정답 카드 안 뜬 상태 -> 제출 단계
  if (!quizStore.showAnswerCard) {
    if (!currentAnswer.value.trim() || submitting.value) {
      return
    }
    submitAnswer()
    return
  }

  // 정답 카드가 떠 있는 상태
  if (!isLastQuestion.value) {
    handleNextAfterAnswer()
  } else {
    goToResult()
  }
}

/**
 * 전역 keydown 핸들러 (엔터 키를 잡아서 handleEnter로 위임)
 */
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    handleEnter()
  }
}

/**
 * 한 문제에 대한 답안을 제출하고 → 서버에 채점 요청 → 정답/오답 카드 표시
 */
const submitAnswer = async () => {
  if (!currentQuestion.value || !currentAnswer.value.trim()) {
    toast.add({
      severity: 'warn',
      summary: 'Warning',
      detail: 'Please enter an answer',
      life: 3000,
    })
    return
  }

  const trimmedAnswer = currentAnswer.value.trim()

  // 프론트 쪽 상태에도 답안 저장
  quizStore.submitAnswer({
    questionId: currentQuestion.value.id,
    answerText: trimmedAnswer,
  })

  submitting.value = true
  try {
    const payload = {
      submissionId: quizStore.submissionId,
      questionId: currentQuestion.value.id,
      answerText: trimmedAnswer,
    }

    const result = await submitQuizAPI(Number(route.params.id), payload)

    // 전체 누적 결과 저장
    quizStore.setQuizResult(result)

    // 이번 문제 정답/오답 정보 저장 (필드명은 실제 응답에 맞게 조정)
    quizStore.setLastAnswerResult({
      correct: result.correct ?? result.isCorrect ?? null,
      correctAnswers: result.correctAnswers ?? result.acceptedAnswers ?? [],
      userAnswer: trimmedAnswer,
    })

    // 다음 입력을 위해 입력칸 clear
    currentAnswer.value = ''
  } catch (error: any) {
    console.error(error)
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Failed to submit quiz',
      life: 3000,
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 정답/오답 카드에서 "다음 문제" 클릭 시
 */
const handleNextAfterAnswer = () => {
  quizStore.nextQuestion() // 내부에서 showAnswerCard = false

  const q = currentQuestion.value
  if (!q) return

  const existing = quizStore.userAnswers.find(
    (a) => a.questionId === q.id,
  )
  currentAnswer.value = existing?.answerText || ''

  // 다음 문제로 넘어왔으니 다시 입력창에 포커스
  focusAnswerInput()
}

/**
 * 마지막 문제 정답/오답 카드에서 "결과 보기" 클릭 시
 */
const goToResult = () => {
  router.push(`/quiz/${route.params.id}/result`)
}

onMounted(() => {
  if (!quizStore.currentQuiz) {
    router.push('/')
    return
  }

  const q = currentQuestion.value
  if (!q) return
  const existing = quizStore.userAnswers.find(
    (a) => a.questionId === q.id,
  )
  currentAnswer.value = existing?.answerText || ''

  // 첫 진입 시 입력창 포커스
  focusAnswerInput()

  // 전역 엔터 키 핸들러 등록
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  // 전역 핸들러 정리
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.quiz-play-container {
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.aspect-video {
  aspect-ratio: 16 / 9;
}

.h-1rem {
  height: 1rem;
}
</style>
