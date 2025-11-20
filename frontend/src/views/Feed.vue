<template>
  <div class="feed-container">
    <!-- Navigation -->
    <nav class="sticky top-0 z-5 border-bottom-1 surface-border bg-surface-0 backdrop-blur">
      <div class="max-w-3xl mx-auto px-4 py-4 flex justify-content-between align-items-center">
        <router-link to="/" class="text-2xl font-bold text-primary flex align-items-center gap-2 no-underline">
          <span class="text-3xl">🎯</span>
          Problem.io
        </router-link>
        <div class="flex gap-6 align-items-center">
          <router-link to="/feed" class="text-primary font-semibold no-underline">
            Play
          </router-link>
          <router-link to="/auth/login" class="text-color-secondary hover:text-color no-underline text-sm">
            Create Quiz
          </router-link>
          <router-link to="/auth/login">
            <Button label="Sign In" icon="pi pi-user" severity="secondary" text size="small" />
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-3xl mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-4xl font-bold mb-3">Ready to Play?</h1>
        <p class="text-lg text-color-secondary leading-relaxed">Pick a quiz below and test your skills!</p>
      </div>

      <!-- Quiz Cards -->
      <div class="flex flex-column gap-6">
        <Card v-for="quiz in quizzes" :key="quiz.id" class="quiz-card">
          <!-- Quiz Header -->
          <template #header>
            <div class="p-4 border-bottom-1 surface-border flex align-items-center gap-3 bg-surface-50">
              <Avatar :label="quiz.creator.charAt(0)" class="bg-primary text-primary-50" size="large" shape="circle" />
              <div class="flex-1">
                <p class="font-semibold m-0">{{ quiz.creator }}</p>
                <p class="text-sm text-color-secondary m-0">created this quiz</p>
              </div>
            </div>
          </template>

          <template #content>
            <!-- Quiz Image -->
            <div class="aspect-video bg-surface-100 overflow-hidden relative mb-4 border-round">
              <img
                :src="quiz.image || '/placeholder.svg'"
                :alt="quiz.title"
                class="w-full h-full object-cover"
                style="transition: transform 0.3s;"
                @mouseenter="handleImageHover($event, true)"
                @mouseleave="handleImageHover($event, false)"
              />
              <div class="absolute inset-0 bg-black-alpha-50 flex align-items-center justify-content-center opacity-0 hover-overlay">
                <span class="text-white font-bold text-lg">Click to Play! 🎮</span>
              </div>
            </div>

            <!-- Quiz Details -->
            <div class="flex flex-column gap-4">
              <div>
                <h2 class="text-2xl font-bold mb-2">{{ quiz.title }}</h2>
                <p class="text-color-secondary font-medium">
                  {{ quiz.questionCount }} questions · Take the challenge!
                </p>
              </div>

              <router-link :to="`/quiz/${quiz.id}`" class="no-underline">
                <Button label="Play Quiz →" icon="pi pi-play" class="w-full" size="large" />
              </router-link>

              <!-- Stats -->
              <div class="flex gap-6 pt-2">
                <Button
                  :icon="quiz.liked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                  :label="quiz.likes.toString()"
                  :class="quiz.liked ? 'text-primary' : 'text-color-secondary'"
                  severity="secondary"
                  text
                  @click="handleLike(quiz.id)"
                />
                <Button
                  icon="pi pi-comments"
                  :label="quiz.comments.toString()"
                  severity="secondary"
                  text
                />
                <Button
                  icon="pi pi-share-alt"
                  label="Share"
                  severity="secondary"
                  text
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
import { ref, onMounted } from 'vue'
import { getQuizzes } from '@/api/quiz'
import { likeQuiz, unlikeQuiz } from '@/api/quiz'

interface Quiz {
  id: number
  title: string
  creator: string
  creatorAvatar: string
  image: string
  questionCount: number
  likes: number
  comments: number
  liked: boolean
}

const quizzes = ref<Quiz[]>([])

const handleLike = async (id: number) => {
  const quiz = quizzes.value.find((q) => q.id === id)
  if (!quiz) return

  try {
    if (quiz.liked) {
      await unlikeQuiz(id)
      quiz.liked = false
      quiz.likes = Math.max(0, quiz.likes - 1)
    } else {
      await likeQuiz(id)
      quiz.liked = true
      quiz.likes += 1
    }
  } catch (error) {
    console.error('Failed to toggle like:', error)
  }
}

const handleImageHover = (event: MouseEvent, isEntering: boolean) => {
  const img = event.target as HTMLImageElement
  const overlay = img.nextElementSibling as HTMLElement
  if (isEntering) {
    img.style.transform = 'scale(1.05)'
    if (overlay) overlay.style.opacity = '1'
  } else {
    img.style.transform = 'scale(1)'
    if (overlay) overlay.style.opacity = '0'
  }
}

onMounted(async () => {
  try {
    const response = await getQuizzes({ page: 1, size: 20, sort: 'latest' })
    quizzes.value = response.content.map((q: any) => ({
      id: q.id,
      title: q.title,
      creator: q.author?.nickname || 'Unknown',
      creatorAvatar: q.author?.profileImageUrl || '/diverse-user-avatars.png',
      image: q.thumbnailUrl || '/placeholder.svg',
      questionCount: q.questionsCount || 0,
      likes: q.likeCount || 0,
      comments: 0,
      liked: q.isLikedByMe || false,
    }))
  } catch (error) {
    console.error('Failed to load quizzes:', error)
    // Mock data 제거
  }
})
</script>

<style scoped>
.feed-container {
  min-height: 100vh;
  background: var(--surface-ground);
}

.sticky {
  position: sticky;
}

.top-0 {
  top: 0;
}

.z-5 {
  z-index: 5;
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

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.pt-2 {
  padding-top: 0.5rem;
}

.text-4xl {
  font-size: 2.25rem;
  line-height: 2.5rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.text-lg {
  font-size: 1.125rem;
  line-height: 1.75rem;
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.text-3xl {
  font-size: 1.875rem;
  line-height: 2.25rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
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

.leading-relaxed {
  line-height: 1.625;
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

.relative {
  position: relative;
}

.absolute {
  position: absolute;
}

.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}

.border-round {
  border-radius: 0.5rem;
}

.quiz-card {
  transition: box-shadow 0.3s;
}

.quiz-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.hover-overlay {
  transition: opacity 0.3s;
}

.no-underline {
  text-decoration: none;
}

.flex-1 {
  flex: 1 1 0%;
}

.m-0 {
  margin: 0;
}
</style>
