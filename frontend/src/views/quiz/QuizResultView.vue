<template>
  <div class="quiz-result-container">
    <div class="container mx-auto px-4 max-w-4xl">
      <div v-if="!quizStore.quizResult" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else class="flex flex-col gap-6">
        <!-- 최종 결과만 표시 -->
        <Card>
          <template #content>
            <div class="text-center flex flex-col gap-6">
              <div
                class="inline-flex items-center justify-center w-28 h-28 border-round-full bg-primary-50 mx-auto"
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
                  총 <span class="font-bold">{{ totalQuestions }}</span>문제 중
                  <span class="font-bold">{{ correctCount }}</span>문제 정답
                  ({{ answeredCount }}문제 풀이 완료)
                </p>
              </div>

              <div class="flex flex-col sm:flex-row gap-4 justify-center">
                <Button
                  label="제작자의 다른 문제 풀기"
                  icon="pi pi-arrow-right"
                  size="large"
                  :disabled="!authorId"
                  @click="goAuthorQuizzes"
                >
                </Button>
                <Button
                  label="그만 풀기"
                  icon="pi pi-home"
                  severity="secondary"
                  outlined
                  size="large"
                  @click="goToHome"
                >
                </Button>
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

const quizStore = useQuizStore()
const router = useRouter()
const route = useRoute()

onMounted(() => {
  // 결과 없으면 플레이 화면으로 되돌리기
  if (!quizStore.quizResult) {
    router.push(`/quiz/${route.params.id}/play`)
  }
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
</style>
