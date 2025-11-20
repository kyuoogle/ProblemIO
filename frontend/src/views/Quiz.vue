<template>
  <div class="quiz-container">
    <nav class="border-bottom-1 surface-border bg-surface-0">
      <div class="max-w-3xl mx-auto px-4 py-4">
        <router-link
          to="/feed"
          class="flex align-items-center gap-2 text-primary no-underline font-semibold"
        >
          <i class="pi pi-arrow-left"></i>
          Back to Quizzes
        </router-link>
      </div>
    </nav>

    <div class="max-w-3xl mx-auto px-4 py-8">
      <template v-if="!showResults">
        <div class="mb-8">
          <div class="flex justify-content-between align-items-center mb-3">
            <h1 class="text-2xl md:text-3xl font-bold">{{ quiz.title }}</h1>
            <Badge :value="`${currentQuestion + 1}/${quiz.questions.length}`" severity="info" />
          </div>
          <ProgressBar :value="progressPercentage" class="h-1rem" />
        </div>

        <Card>
          <template #content>
            <div class="flex flex-column gap-6">
              <h2 class="text-2xl md:text-3xl font-bold">{{ question.text }}</h2>

              <div class="flex flex-column gap-4">
                <Button
                  v-for="(option, index) in question.options"
                  :key="index"
                  :label="`${String.fromCharCode(65 + index)}. ${option}`"
                  :class="[
                    'w-full text-left justify-content-start',
                    selectedAnswer === index ? 'selected-answer' : '',
                  ]"
                  :disabled="selectedAnswer !== null"
                  @click="handleAnswer(index)"
                />
              </div>
            </div>
          </template>
        </Card>
      </template>

      <template v-else>
        <Card>
          <template #content>
            <div class="text-center flex flex-column gap-8">
              <div class="inline-flex align-items-center justify-content-center w-28 h-28 border-round-full bg-primary-50">
                <i class="pi pi-trophy text-primary text-6xl"></i>
              </div>

              <div>
                <h2 class="text-3xl md:text-4xl font-bold mb-4">
                  {{ percentage >= 80 ? 'Amazing! 🎉' : percentage >= 60 ? 'Great Job! 👏' : 'Nice Try! 💪' }}
                </h2>
                <p class="text-6xl md:text-7xl font-bold text-primary mb-4">{{ percentage }}%</p>
                <p class="text-xl text-color-secondary">
                  You got <span class="font-bold">{{ score }}</span> out of
                  <span class="font-bold">{{ quiz.questions.length }}</span> correct!
                </p>
              </div>

              <div class="flex flex-column sm:flex-row gap-4 justify-content-center">
                <Button
                  label="Try Again"
                  icon="pi pi-refresh"
                  @click="resetQuiz"
                  size="large"
                />
                <router-link to="/feed" class="no-underline">
                  <Button
                    label="More Quizzes"
                    icon="pi pi-list"
                    severity="secondary"
                    outlined
                    size="large"
                  />
                </router-link>
              </div>
            </div>
          </template>
        </Card>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getQuiz } from '@/api/quiz'
import { submitQuiz } from '@/api/submission'

interface Props {
  id: string
}

const props = defineProps<Props>()
const route = useRoute()

interface Question {
  id: number
  text: string
  options: string[]
  correctAnswer: number
}

const quiz = ref({
  id: 1,
  title: 'Biology: Parts of a Cell',
  creator: 'Sarah Chen',
  questions: [
    {
      id: 1,
      text: 'What is the function of the mitochondria?',
      options: ['Protein synthesis', 'Energy production', 'Photosynthesis', 'Storage'],
      correctAnswer: 1,
    },
    {
      id: 2,
      text: 'Where is the genetic material stored?',
      options: ['Cytoplasm', 'Ribosome', 'Nucleus', 'Mitochondria'],
      correctAnswer: 2,
    },
    {
      id: 3,
      text: 'What does the cell membrane do?',
      options: ['Produce energy', 'Control entry and exit', 'Store DNA', 'Synthesize proteins'],
      correctAnswer: 1,
    },
  ] as Question[],
})

const currentQuestion = ref(0)
const score = ref(0)
const answers = ref<number[]>([])
const showResults = ref(false)
const selectedAnswer = ref<number | null>(null)

const question = computed(() => quiz.value.questions[currentQuestion.value])
const percentage = computed(() => Math.round((score.value / quiz.value.questions.length) * 100))
const progressPercentage = computed(() => ((currentQuestion.value + 1) / quiz.value.questions.length) * 100)

const handleAnswer = async (optionIndex: number) => {
  selectedAnswer.value = optionIndex

  setTimeout(() => {
    const isCorrect = optionIndex === question.value.correctAnswer
    answers.value = [...answers.value, optionIndex]

    if (isCorrect) {
      score.value = score.value + 1
    }

    if (currentQuestion.value < quiz.value.questions.length - 1) {
      currentQuestion.value = currentQuestion.value + 1
      selectedAnswer.value = null
    } else {
      // 제출
      submitAnswers()
      showResults.value = true
    }
  }, 300)
}

const submitAnswers = async () => {
  try {
    const submissionData = quiz.value.questions.map((q, idx) => ({
      questionId: q.id,
      answerText: q.options[answers.value[idx]],
    }))
    await submitQuiz(Number(props.id), submissionData)
  } catch (error) {
    console.error('Failed to submit quiz:', error)
  }
}

const resetQuiz = () => {
  currentQuestion.value = 0
  score.value = 0
  answers.value = []
  showResults.value = false
  selectedAnswer.value = null
}
</script>

<style scoped>
.quiz-container {
  min-height: 100vh;
  background: linear-gradient(to bottom right, var(--primary-50), var(--surface-ground), var(--accent-50));
}

.max-w-3xl {
  max-width: 48rem;
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.py-4 {
  padding-top: 1rem;
  padding-bottom: 1rem;
}

.py-8 {
  padding-top: 2rem;
  padding-bottom: 2rem;
}

.mb-8 {
  margin-bottom: 2rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.text-3xl {
  font-size: 1.875rem;
  line-height: 2.25rem;
}

.text-4xl {
  font-size: 2.25rem;
  line-height: 2.5rem;
}

.text-6xl {
  font-size: 3.75rem;
  line-height: 1;
}

.text-xl {
  font-size: 1.25rem;
  line-height: 1.75rem;
}

.font-bold {
  font-weight: 700;
}

.text-center {
  text-align: center;
}

.w-28 {
  width: 7rem;
}

.h-28 {
  height: 7rem;
}

.h-1rem {
  height: 1rem;
}

.selected-answer {
  background-color: var(--primary-100) !important;
  border-color: var(--primary-color) !important;
}

.no-underline {
  text-decoration: none;
}

@media (min-width: 768px) {
  .md\:text-3xl {
    font-size: 1.875rem;
    line-height: 2.25rem;
  }
  
  .md\:text-4xl {
    font-size: 2.25rem;
    line-height: 2.5rem;
  }
  
  .md\:text-7xl {
    font-size: 4.5rem;
    line-height: 1;
  }
}
</style>
