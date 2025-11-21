<template>
    <div class="mypage-container">
      <div class="container mx-auto px-4">
        <div v-if="loading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <div v-else-if="summary" class="flex flex-column gap-6">
          <!-- Summary Card -->
          <Card>
            <template #content>
              <div class="flex flex-column md:flex-row gap-6 align-items-center md:align-items-start">
                <Avatar
                :image="authStore.user?.profileImageUrl ? `http://localhost:8080${authStore.user.profileImageUrl}` : null"
                :label="!authStore.user?.profileImageUrl && authStore.user?.nickname ? authStore.user.nickname.charAt(0).toUpperCase() : ''"
                :icon="!authStore.user?.profileImageUrl && !authStore.user?.nickname ? 'pi pi-user' : ''"
                  shape="circle"
                  size="xlarge"
                  class="w-32 h-32"
                />
                <div class="flex-1 text-center md:text-left">
                  <h1 class="text-3xl font-bold mb-2">{{ summary.nickname }}</h1>
                  <p v-if="summary.statusMessage" class="text-lg text-color-secondary mb-4">
                    {{ summary.statusMessage }}
                  </p>
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div class="text-center p-4 border-round surface-border">
                      <p class="text-3xl font-bold m-0 text-primary">{{ summary.quizCount || 0 }}</p>
                      <p class="text-sm text-color-secondary m-0 mt-2">My Quizzes</p>
                    </div>
                    <div class="text-center p-4 border-round surface-border">
                      <p class="text-3xl font-bold m-0 text-primary">{{ summary.followerCount || 0 }}</p>
                      <p class="text-sm text-color-secondary m-0 mt-2">Followers</p>
                    </div>
                    <div class="text-center p-4 border-round surface-border">
                      <p class="text-3xl font-bold m-0 text-primary">{{ summary.followingCount || 0 }}</p>
                      <p class="text-sm text-color-secondary m-0 mt-2">Following</p>
                    </div>
                    <div class="text-center p-4 border-round surface-border">
                      <p class="text-3xl font-bold m-0 text-primary">{{ summary.likedQuizCount || 0 }}</p>
                      <p class="text-sm text-color-secondary m-0 mt-2">Liked</p>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </Card>
  
          <!-- Quick Links -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card class="cursor-pointer hover-card" @click="goToMyQuizzes">
              <template #content>
                <div class="text-center p-4">
                  <i class="pi pi-list text-4xl text-primary mb-3"></i>
                  <h3 class="text-xl font-bold m-0">My Quizzes</h3>
                  <p class="text-color-secondary m-0 mt-2">Manage your quizzes</p>
                </div>
              </template>
            </Card>
  
            <Card class="cursor-pointer hover-card" @click="goToEditProfile">
              <template #content>
                <div class="text-center p-4">
                  <i class="pi pi-user-edit text-4xl text-primary mb-3"></i>
                  <h3 class="text-xl font-bold m-0">Edit Profile</h3>
                  <p class="text-color-secondary m-0 mt-2">Update your information</p>
                </div>
              </template>
            </Card>
  
            <Card class="cursor-pointer hover-card" @click="goToCreateQuiz">
              <template #content>
                <div class="text-center p-4">
                  <i class="pi pi-plus-circle text-4xl text-primary mb-3"></i>
                  <h3 class="text-xl font-bold m-0">Create Quiz</h3>
                  <p class="text-color-secondary m-0 mt-2">Make a new quiz</p>
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  import { useToast } from 'primevue/usetoast'
  import { getMySummary } from '@/api/user'
  
  const router = useRouter()
  const toast = useToast()
  const authStore = useAuthStore()

  const summary = ref(null)
  const loading = ref(false)
  
  const loadSummary = async () => {
    loading.value = true
    try {
      summary.value = await getMySummary()
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load summary',
        life: 3000,
      })
    } finally {
      loading.value = false
    }
  }
  
  const goToMyQuizzes = () => {
    router.push('/mypage/quizzes')
  }
  
  const goToEditProfile = () => {
    router.push('/mypage/edit')
  }
  
  const goToCreateQuiz = () => {
    router.push('/quiz/create')
  }
  
  onMounted(() => {
    loadSummary()
  })
  </script>
  
  <style scoped>
  .mypage-container {
    min-height: calc(100vh - 200px);
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .w-32 {
    width: 8rem;
  }
  
  .h-32 {
    height: 8rem;
  }
  
  .hover-card {
    transition: transform 0.2s, box-shadow 0.2s;
  }
  
  .hover-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  }
  </style>