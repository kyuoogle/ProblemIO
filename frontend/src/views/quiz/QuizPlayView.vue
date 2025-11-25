<template>
  <div class="quiz-play-container">
    <div class="container mx-auto px-4 max-w-4xl">
      <div v-if="!quizStore.currentQuiz" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else class="flex flex-col gap-6">
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
              <div
                v-if="currentQuestion?.imageUrl"
                class="aspect-video bg-surface-100 overflow-hidden border-round"
              >
                <img
                  :src="currentQuestion.imageUrl"
                  :alt="currentQuestion.description"
                  class="w-full h-full object-cover"
                />
              </div>

              <div v-if="currentQuestion?.description" class="text-xl">
                {{ currentQuestion.description }}
              </div>

              <div class="flex flex-col gap-3">
                <label class="text-lg font-semibold">Your Answer:</label>
                <InputText
                  v-model="currentAnswer"
                  placeholder="Enter your answer..."
                  class="w-full"
                  @keyup.enter="submitAnswer"
                />
              </div>

              <div class="flex">
                <Button
                  label="Submit"
                  icon="pi pi-check"
                  class="flex-1"
                  :disabled="!currentAnswer.trim() || submitting"
                  :loading="submitting"
                  @click="submitAnswer"
                />
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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

/**
 * 한 문제에 대한 답안을 제출하고 → 서버에 채점 요청 → 결과 화면으로 이동
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

  // 프론트 쪽 상태에도 답안 저장 (나중에 필요하면 사용)
  quizStore.submitAnswer({
    questionId: currentQuestion.value.id,
    answerText: currentAnswer.value.trim(),
  })

  submitting.value = true
  try {
    const payload = {
      // 첫 문제면 null, 이후엔 이전 결과에서 받은 submissionId 재사용
      submissionId: quizStore.quizResult?.submissionId ?? null,
      questionId: currentQuestion.value.id,
      answerText: currentAnswer.value.trim(),
    }

    const result = await submitQuizAPI(
      Number(route.params.id),
      payload,
    )

    // 이번 문제에 대한 채점 결과 + 전체 누적 정답 수/총 문제 수 등 저장
    quizStore.setQuizResult(result)

    // 입력창은 비워두고
    currentAnswer.value = ''

    // 결과 페이지로 이동
    router.push(`/quiz/${route.params.id}/result`)
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

onMounted(() => {
  if (!quizStore.currentQuiz) {
    router.push('/')
    return
  }

  // 이미 답을 적어둔 문제가 있다면 복원 (뒤로가기 등)
  const q = currentQuestion.value
  if (!q) return
  const existing = quizStore.userAnswers.find(
    (a) => a.questionId === q.id,
  )
  currentAnswer.value = existing?.answerText || ''
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
