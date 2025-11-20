<template>
  <div class="profile-container">
    <!-- Navigation -->
    <nav class="border-bottom-1 surface-border bg-surface-0">
      <div class="max-w-2xl mx-auto px-4 py-4">
        <router-link to="/feed" class="flex align-items-center gap-2 text-primary no-underline">
          <i class="pi pi-arrow-left"></i>
          Back to Feed
        </router-link>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="max-w-2xl mx-auto px-4 py-8">
      <!-- Profile Header -->
      <Card class="mb-6">
        <template #content>
          <div class="text-center p-6">
            <Avatar
              :image="user.avatar || '/placeholder.svg'"
              :label="!user.avatar ? user.name.charAt(0) : undefined"
              size="xlarge"
              shape="circle"
              class="mb-4"
            />
            <h1 class="text-3xl font-bold mb-2">{{ user.name }}</h1>
            <p class="text-color-secondary mb-4">{{ user.email }}</p>
            <p class="mb-6">{{ user.bio }}</p>
            <Button label="Edit Profile" />
          </div>
        </template>
      </Card>

      <!-- Stats -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <Card>
          <template #content>
            <div class="text-center p-4">
              <i class="pi pi-trophy text-primary text-2xl mb-2"></i>
              <p class="text-2xl font-bold m-0">{{ user.score }}</p>
              <p class="text-sm text-color-secondary m-0">Total Score</p>
            </div>
          </template>
        </Card>
        <Card>
          <template #content>
            <div class="text-center p-4">
              <i class="pi pi-book text-primary text-2xl mb-2"></i>
              <p class="text-2xl font-bold m-0">{{ user.quizzesCreated }}</p>
              <p class="text-sm text-color-secondary m-0">Quizzes Created</p>
            </div>
          </template>
        </Card>
        <Card>
          <template #content>
            <div class="text-center p-4">
              <i class="pi pi-book text-primary text-2xl mb-2"></i>
              <p class="text-2xl font-bold m-0">{{ user.quizzesSolved }}</p>
              <p class="text-sm text-color-secondary m-0">Quizzes Solved</p>
            </div>
          </template>
        </Card>
        <Card>
          <template #content>
            <div class="text-center p-4">
              <i class="pi pi-users text-primary text-2xl mb-2"></i>
              <p class="text-2xl font-bold m-0">{{ user.followers }}</p>
              <p class="text-sm text-color-secondary m-0">Followers</p>
            </div>
          </template>
        </Card>
      </div>

      <!-- My Quizzes -->
      <div>
        <h2 class="text-2xl font-bold mb-4">My Quizzes</h2>
        <div class="flex flex-column gap-4">
          <Card v-for="quiz in userQuizzes" :key="quiz.id">
            <template #content>
              <div class="flex justify-content-between align-items-start p-4">
                <div class="flex-1">
                  <h3 class="font-bold text-lg mb-2">{{ quiz.title }}</h3>
                  <p class="text-sm text-color-secondary m-0">
                    {{ quiz.questions }} questions • {{ quiz.solved }} people solved
                  </p>
                </div>
                <Button label="View" severity="secondary" outlined size="small" />
              </div>
            </template>
          </Card>
        </div>
      </div>

      <!-- Logout -->
      <div class="mt-12 pt-8 border-top-1 surface-border">
        <Button label="Logout" severity="danger" class="w-full" @click="handleLogout" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { getMySummary, getMyQuizzes } from '@/api/user'
import { logout } from '@/api/auth'

const router = useRouter()
const toast = useToast()

const user = ref({
  name: 'John Doe',
  email: 'john@example.com',
  avatar: '/diverse-user-avatars.png',
  bio: 'Biology teacher passionate about interactive learning',
  quizzesCreated: 12,
  quizzesSolved: 45,
  followers: 234,
  following: 156,
  score: 2345,
})

const userQuizzes = ref([
  {
    id: 1,
    title: 'Biology: Parts of a Cell',
    questions: 8,
    solved: 124,
  },
  {
    id: 2,
    title: 'Photosynthesis Process',
    questions: 6,
    solved: 89,
  },
  {
    id: 3,
    title: 'Genetics Basics',
    questions: 10,
    solved: 156,
  },
])

const handleLogout = async () => {
  try {
    await logout()
    toast.add({ severity: 'success', summary: 'Success', detail: 'Logged out successfully', life: 3000 })
    router.push('/')
  } catch (error) {
    console.error('Logout error:', error)
  }
}

onMounted(async () => {
  try {
    const summary = await getMySummary()
    user.value = {
      name: summary.nickname,
      email: summary.email || '',
      avatar: summary.profileImageUrl,
      bio: summary.statusMessage || '',
      quizzesCreated: summary.quizCount || 0,
      quizzesSolved: 0,
      followers: summary.followerCount || 0,
      following: summary.followingCount || 0,
      score: 0,
    }

    const quizzes = await getMyQuizzes()
    userQuizzes.value = quizzes.map((q: any) => ({
      id: q.id,
      title: q.title,
      questions: 0,
      solved: q.playCount || 0,
    }))
  } catch (error) {
    console.error('Failed to load profile:', error)
  }
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: var(--surface-ground);
}

.max-w-2xl {
  max-width: 42rem;
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

.p-4 {
  padding: 1rem;
}

.p-6 {
  padding: 1.5rem;
}

.pt-8 {
  padding-top: 2rem;
}

.mb-8 {
  margin-bottom: 2rem;
}

.mb-6 {
  margin-bottom: 1.5rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mt-12 {
  margin-top: 3rem;
}

.text-3xl {
  font-size: 1.875rem;
  line-height: 2.25rem;
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

.font-bold {
  font-weight: 700;
}

.text-center {
  text-align: center;
}

.w-full {
  width: 100%;
}

.flex-1 {
  flex: 1 1 0%;
}

.m-0 {
  margin: 0;
}

.grid {
  display: grid;
}

.grid-cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.gap-4 {
  gap: 1rem;
}

.no-underline {
  text-decoration: none;
}

@media (min-width: 768px) {
  .md\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>
