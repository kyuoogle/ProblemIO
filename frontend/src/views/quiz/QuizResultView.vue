<template>
  <div class="quiz-result-container">
    <div class="container mx-auto px-4 max-w-4xl">
      <div v-if="!quizStore.quizResult" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else class="flex flex-col gap-6">
        <!-- 전체 요약 -->
        <Card>
          <template #content>
            <div class="text-center flex flex-col gap-6">
              <div
                class="inline-flex items-center justify-center w-28 h-28 border-round-full bg-primary-50"
              >
                <i class="pi pi-trophy text-primary text-6xl"></i>
              </div>

              <div>
                <h2 class="text-3xl md:text-4xl font-bold mb-4">
                  {{ resultMessage }}
                </h2>
                <p class="text-6xl md:text-7xl font-bold text-primary mb-4">
                  {{ scorePercentage }}%
                </p>
                <p class="text-xl text-color-secondary">
                  총
                  <span class="font-bold">{{ totalQuestions }}</span
                  >문제 중
                  <span class="font-bold">{{ correctCount }}</span
                  >문제 정답
                  ({{ answeredCount }}문제 풀이 완료)
                </p>
              </div>

              <div class="flex flex-col sm:flex-row gap-4 justify-center">
                <Button
                  :label="primaryButtonLabel"
                  icon="pi pi-arrow-right"
                  size="large"
                  @click="goNextOrFinish"
                />
                <Button
                  label="그만 풀기"
                  icon="pi pi-home"
                  severity="secondary"
                  outlined
                  size="large"
                  @click="goToHome"
                />
              </div>
            </div>
          </template>
        </Card>

        <!-- 방금 푼 문제에 대한 결과 -->
        <Card>
          <template #content>
            <div class="flex flex-col gap-6 items-center text-center">
              <div
                v-if="imageUrl"
                class="result-image-wrapper bg-surface-100 overflow-hidden border-round"
              >
                <img
                  :src="imageUrl"
                  alt="Question Image"
                  class="w-full h-full object-cover"
                />
              </div>

              <p class="result-text" :class="isCorrect ? 'correct' : 'wrong'">
                {{ isCorrect ? '정답!' : '오답!' }}
              </p>

              <div>
                <p class="font-semibold mb-2">정답은</p>
                <p class="answer-value">
                  {{ correctAnswer }}
                </p>
              </div>

              <div
                v-if="correctAnswers && correctAnswers.length > 1"
                class="extra-answers"
              >
                <p class="extra-label">허용되는 다른 정답들</p>
                <p class="extra-list">
                  {{ correctAnswers.join(', ') }}
                </p>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useQuizStore } from '@/stores/quiz'
import Button from 'primevue/button'
import Card from 'primevue/card'
import { resolveImageUrl } from '@/lib/image'

const quizStore = useQuizStore()
const router = useRouter()
const route = useRoute()

onMounted(() => {
  if (!quizStore.quizResult || !quizStore.currentQuiz) {
    // 결과도 없고 퀴즈도 없으면 홈으로
    router.push('/')
  }
})

// Derived Data
const totalQuestions = computed(
  () => quizStore.quizResult?.totalQuestions || 0,
)
const correctCount = computed(
  () => quizStore.quizResult?.correctCount || 0,
)
const answeredCount = computed(
  () => quizStore.quizResult?.answeredCount || 0,
)
const scorePercentage = computed(() =>
  totalQuestions.value
    ? Math.round((correctCount.value / totalQuestions.value) * 100)
    : 0,
)

const isCorrect = computed(() => quizStore.quizResult?.correct ?? false)
const correctAnswer = computed(
  () => quizStore.quizResult?.correctAnswer ?? '',
)
const correctAnswers = computed(
  () => quizStore.quizResult?.correctAnswers ?? [],
)
const imageUrl = computed(() => {
  const url = quizStore.quizResult?.imageUrl
  return url ? resolveImageUrl(url) : ''
})

// 현재 문제가 마지막 문제인지
const isLastQuestion = computed(() => {
  const quiz = quizStore.currentQuiz
  if (!quiz) return true
  return (
    quizStore.currentQuestionIndex >= quiz.questions.length - 1
  )
})

const resultMessage = computed(() => {
  const score = scorePercentage.value
  if (score === 100) return 'Perfect Score!'
  if (score >= 80) return 'Excellent!'
  if (score >= 60) return 'Good Job!'
  return 'Keep Practicing!'
})

const primaryButtonLabel = computed(() =>
  isLastQuestion.value ? '퀴즈 종료' : '다음 문제',
)

// 다음 문제로 가거나, 마지막이면 종료
const goNextOrFinish = () => {
  if (isLastQuestion.value) {
    router.push('/')
  } else {
    quizStore.nextQuestion()
    quizStore.setQuizResult(null) // 결과 초기화
    router.push(`/quiz/${route.params.id}/play`)
  }
}

// 그만 풀기
const goToHome = () => {
  router.push('/')
}
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
}

.quiz-result-container {
  min-height: calc(100vh - 200px);
}

.result-image-wrapper {
  width: 100%;
  max-width: 600px;
  aspect-ratio: 16 / 9;
}

.result-text {
  font-size: 24px;
  font-weight: 700;
}
.result-text.correct {
  color: #4ade80;
}
.result-text.wrong {
  color: #f97373;
}

.answer-value {
  font-size: 22px;
  font-weight: 700;
}

.extra-answers {
  margin-top: 8px;
}
.extra-label {
  font-size: 14px;
  opacity: 0.8;
}
.extra-list {
  font-size: 14px;
}
</style>
