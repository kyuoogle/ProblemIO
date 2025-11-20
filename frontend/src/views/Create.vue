<template>
  <div class="create-container">
    <!-- Navigation -->
    <nav class="border-bottom-1 surface-border bg-surface-0">
      <div class="max-w-4xl mx-auto px-4 py-4">
        <router-link to="/feed" class="flex align-items-center gap-2 text-primary no-underline">
          <i class="pi pi-arrow-left"></i>
          Back to Feed
        </router-link>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold mb-8">Create New Quiz</h1>

      <!-- Upload Step -->
      <Card v-if="step === 'upload'">
        <template #content>
          <div class="flex flex-column align-items-center justify-content-center gap-4 py-12">
            <i class="pi pi-upload text-6xl text-color-secondary"></i>
            <h2 class="text-2xl font-bold">Upload Quiz Image</h2>
            <p class="text-color-secondary text-center max-w-md">
              Upload an image that will be used for your quiz questions
            </p>
            <FileUpload
              mode="basic"
              accept="image/*"
              :maxFileSize="10000000"
              chooseLabel="Choose Image"
              @select="handleImageUpload"
              class="w-full max-w-md"
            />
          </div>
        </template>
      </Card>

      <!-- Details Step -->
      <Card v-if="step === 'details'">
        <template #content>
          <div class="flex flex-column gap-6">
            <div v-if="image" class="aspect-video border-round overflow-hidden border-2 surface-border">
              <img :src="image || '/placeholder.svg'" alt="Quiz" class="w-full h-full object-cover" />
            </div>

            <div class="flex flex-column gap-4">
              <div class="flex flex-column gap-2">
                <label for="title" class="text-sm font-medium">Quiz Title</label>
                <InputText
                  id="title"
                  v-model="title"
                  placeholder="Enter quiz title"
                  class="w-full"
                />
              </div>

              <div class="flex flex-column gap-2">
                <label for="description" class="text-sm font-medium">Description (Optional)</label>
                <Textarea
                  id="description"
                  v-model="description"
                  placeholder="Describe your quiz"
                  rows="3"
                  class="w-full"
                />
              </div>

              <div class="flex gap-4">
                <Button label="Back" severity="secondary" outlined @click="step = 'upload'" />
                <Button label="Add Questions" @click="step = 'questions'" :disabled="!title" />
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Questions Step -->
      <div v-if="step === 'questions'" class="flex flex-column gap-6">
        <!-- Summary -->
        <Card>
          <template #content>
            <div class="p-4">
              <h2 class="font-bold mb-2">{{ title }}</h2>
              <p class="text-sm text-color-secondary m-0">{{ questions.length }} questions added</p>
            </div>
          </template>
        </Card>

        <!-- Questions List -->
        <Card v-for="(q, idx) in questions" :key="idx">
          <template #content>
            <div class="p-4">
              <div class="flex justify-content-between align-items-start mb-4">
                <div>
                  <p class="font-semibold text-sm text-color-secondary m-0">Question {{ idx + 1 }}</p>
                  <p class="font-bold m-0">{{ q.text }}</p>
                </div>
                <Button
                  icon="pi pi-times"
                  severity="danger"
                  text
                  rounded
                  @click="questions = questions.filter((_, i) => i !== idx)"
                />
              </div>
              <div class="flex flex-column gap-2">
                <div
                  v-for="(opt, optIdx) in q.options"
                  :key="optIdx"
                  :class="[
                    'p-2 border-round text-sm',
                    optIdx === q.correct ? 'bg-primary-50 text-primary font-semibold' : 'bg-surface-100',
                  ]"
                >
                  {{ opt }}
                </div>
              </div>
            </div>
          </template>
        </Card>

        <!-- Add Question Form -->
        <Card>
          <template #content>
            <div class="flex flex-column gap-4 p-4">
              <h3 class="font-bold">Add Question</h3>

              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">Question Text</label>
                <InputText
                  v-model="newQuestion.text"
                  placeholder="Enter question"
                  class="w-full"
                />
              </div>

              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">Options</label>
                <div v-for="(opt, idx) in newQuestion.options" :key="idx" class="flex gap-2 align-items-center">
                  <InputText
                    v-model="newQuestion.options[idx]"
                    :placeholder="`Option ${idx + 1}`"
                    class="flex-1"
                  />
                  <input
                    type="radio"
                    name="correct"
                    :checked="newQuestion.correct === idx"
                    @change="newQuestion.correct = idx"
                    class="w-4 h-4"
                  />
                </div>
              </div>

              <Button label="Add Question" icon="pi pi-plus" @click="handleAddQuestion" class="w-full" />
            </div>
          </template>
        </Card>

        <!-- Publish -->
        <div class="flex gap-4">
          <Button label="Back" severity="secondary" outlined @click="step = 'details'" />
          <Button
            label="Publish Quiz"
            @click="handlePublish"
            :disabled="questions.length === 0"
            class="flex-1"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { createQuiz } from '@/api/quiz'

const router = useRouter()
const toast = useToast()
const step = ref<'upload' | 'details' | 'questions'>('upload')
const image = ref<string>('')
const title = ref('')
const description = ref('')
const questions = ref<Array<{ text: string; options: string[]; correct: number }>>([])
const newQuestion = ref({
  text: '',
  options: ['', '', '', ''],
  correct: 0,
})

const handleImageUpload = (event: any) => {
  const file = event.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      image.value = e.target?.result as string
      step.value = 'details'
    }
    reader.readAsDataURL(file)
  }
}

const handleAddQuestion = () => {
  if (newQuestion.value.text && newQuestion.value.options.every((o) => o)) {
    questions.value = [...questions.value, { ...newQuestion.value }]
    newQuestion.value = { text: '', options: ['', '', '', ''], correct: 0 }
  }
}

const handlePublish = async () => {
  try {
    await createQuiz({
      title: title.value,
      description: description.value,
      thumbnailUrl: image.value,
      isPublic: true,
      questions: questions.value.map((q, idx) => ({
        order: idx + 1,
        imageUrl: image.value,
        description: q.text,
        answers: q.options.map((opt, optIdx) => ({
          text: opt,
          sortOrder: optIdx === q.correct ? 1 : optIdx + 2,
        })),
      })),
    })
    toast.add({ severity: 'success', summary: 'Success', detail: 'Quiz published successfully!', life: 3000 })
    router.push('/feed')
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: error.response?.data?.message || 'Failed to publish quiz',
      life: 3000,
    })
  }
}
</script>

<style scoped>
.create-container {
  min-height: 100vh;
  background: var(--surface-ground);
}

.max-w-4xl {
  max-width: 56rem;
}

.max-w-md {
  max-width: 28rem;
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

.py-12 {
  padding-top: 3rem;
  padding-bottom: 3rem;
}

.p-4 {
  padding: 1rem;
}

.mb-8 {
  margin-bottom: 2rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.text-3xl {
  font-size: 1.875rem;
  line-height: 2.25rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.text-6xl {
  font-size: 3.75rem;
  line-height: 1;
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.font-bold {
  font-weight: 700;
}

.font-semibold {
  font-weight: 600;
}

.font-medium {
  font-weight: 500;
}

.text-center {
  text-align: center;
}

.aspect-video {
  aspect-ratio: 16 / 9;
}

.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

.object-cover {
  object-fit: cover;
}

.overflow-hidden {
  overflow: hidden;
}

.border-round {
  border-radius: 0.5rem;
}

.flex-1 {
  flex: 1 1 0%;
}

.m-0 {
  margin: 0;
}

.w-4 {
  width: 1rem;
}

.h-4 {
  height: 1rem;
}

.no-underline {
  text-decoration: none;
}
</style>
