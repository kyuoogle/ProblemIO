<template>
  <div class="quiz-result-container">
    <div class="container mx-auto px-4 max-w-4xl">
      <div v-if="!quizStore.quizResult" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else class="flex flex-col gap-6">
        <!-- ===================== -->
        <!--   최종 결과 모드일 때만 -->
        <!-- ===================== -->
        <Card v-if="isFinished">
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
                <!-- 제작자의 다른 문제 풀기 -->
                <Button
                  label="제작자의 다른 문제 풀기"
                  icon="pi pi-arrow-right"
                  size="large"
                  :disabled="!authorId"
                  @click="goAuthorQuizzes"
                />
                <!-- 그만 풀기(메인으로) -->
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

        <!-- ===================== -->
        <!--     방금 푼 문제 결과   -->
        <!-- ===================== -->
        <Card>
          <template #content>
            <div class="flex flex-col gap-6 items-center text-center">
              <!-- 문제 이미지 -->
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

              <!-- 정답 / 오답 -->
              <p class="result-text" :class="isCorrect ? 'correct' : 'wrong'">
                {{ isCorrect ? '정답!' : '오답!' }}
              </p>

              <!-- 대표 정답 -->
              <div>
                <p class="font-semibold mb-2">정답은</p>
                <p class="answer-value">
                  {{ correctAnswer }}
                </p>
              </div>

              <!-- 허용되는 다른 정답들 -->
              <div
                v-if="correctAnswers && correctAnswers.length > 1"
                class="extra-answers"
              >
                <p class="extra-label">허용되는 다른 정답들</p>
                <p class="extra-list">
                  {{ correctAnswers.join(', ') }}
                </p>
              </div>

              <!-- 아직 끝이 아니라면 '다음 문제로' 버튼 -->
              <Button
                v-if="!isFinished"
                label="다음 문제로"
                icon="pi pi-arrow-right"
                size="large"
                @click="goToNextQuestion"
              />
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useQuizStore } from '@/stores/quiz'
import Button from 'primevue/button'
import Card from 'primevue/card'
import { resolveImageUrl } from '@/lib/image'

const quizStore = useQuizStore()
const router = useRouter()
const route = useRoute()

// ===== 가드 + 엔터 키 핸들러 =====
const handleKeyUp = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !isFinished.value) {
    goToNextQuestion()
  }
}

onMounted(() => {
  // 결과 없으면 플레이 화면으로 되돌리기
  if (!quizStore.quizResult) {
    router.push(`/quiz/${route.params.id}/play`)
    return
  }
  window.addEventListener('keyup', handleKeyUp)
})

onUnmounted(() => {
  window.removeEventListener('keyup', handleKeyUp)
})

// ===== Derived Data =====
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

// 마지막 문제까지 다 풀었는지
const isFinished = computed(
  () => answeredCount.value >= totalQuestions.value && totalQuestions.value > 0,
)

// 제작자 id (없을 수 있으니 optional)
const authorId = computed(
  () => quizStore.currentQuiz?.author?.id ?? null,
)

const resultMessage = computed(() => {
  const score = scorePercentage.value
  if (score === 100) return 'Perfect Score!'
  if (score >= 80) return 'Excellent!'
  if (score >= 60) return 'Good Job!'
  return 'Keep Practicing!'
})

// ===== Actions =====
const goToNextQuestion = () => {
  quizStore.nextQuestion()
  quizStore.setQuizResult(null) // 결과만 초기화 (submissionId는 유지)
  router.push(`/quiz/${route.params.id}/play`)
}

const goAuthorQuizzes = () => {
  if (!authorId.value) return
  router.push(`/users/${authorId.value}`)
}

const goToHome = () => {
  quizStore.resetQuizPlay()
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
