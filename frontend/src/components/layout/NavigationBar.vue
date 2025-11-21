<template>
    <Menubar :model="menuItems" class="border-bottom-1 surface-border">
      <template #start>
        <router-link to="/" class="flex align-items-center gap-2 no-underline text-primary">
          <i class="pi pi-home text-2xl"></i>
          <span class="text-xl font-bold">Problem.io</span>
        </router-link>
      </template>
  
      <template #end>
        <div class="flex align-items-center gap-3">
          
          <span class="p-input-icon-left">
            <i class="pi pi-search" />
            <InputText
              v-model="searchKeyword"
              placeholder="Search quizzes..."
              class="w-20rem"
              @keyup.enter="handleSearch"
            />
          </span>
  
          <template v-if="!authStore.isAuthenticated">
            <Button label="Create Quiz" icon="pi pi-plus" text @click="handleCreateQuiz" />
            <Button label="My Page" icon="pi pi-user" text @click="handleMyPage" />
            <Button label="Login" icon="pi pi-sign-in" @click="goToLogin" />
            <Button label="Signup" severity="secondary" @click="goToSignup" />
          </template>
  
          <template v-else>
            <Button label="Create Quiz" icon="pi pi-plus" @click="goToCreateQuiz" />
            
            <Button
              @click="goToMyPage"
              class="flex align-items-center gap-2 p-button-text p-0"
              aria-label="My Page"
            >
              <Avatar
                :image="authStore.user?.profileImageUrl ? `http://localhost:8080${authStore.user.profileImageUrl}` : null"
                :label="!authStore.user?.profileImageUrl && authStore.user?.nickname ? authStore.user.nickname.charAt(0).toUpperCase() : ''"
                :icon="!authStore.user?.profileImageUrl && !authStore.user?.nickname ? 'pi pi-user' : ''"
                class="mr-1 font-bold"
                :class="{ 'surface-200 text-700': !authStore.user?.profileImageUrl }"
                shape="circle"
              />
              <span class="text-color">{{ authStore.user?.nickname || 'User' }}</span>
            </Button>
  
            <Button label="Logout" icon="pi pi-sign-out" severity="danger" text @click="handleLogout" />
          </template>
  
        </div>
      </template>
    </Menubar>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  import { useToast } from 'primevue/usetoast'
  import { useConfirm } from 'primevue/useconfirm'
  // PrimeVue 컴포넌트들이 자동 import 설정되어 있다고 가정합니다.
  // 만약 아니라면 아래 주석을 해제하세요.
  // import Avatar from 'primevue/avatar'; 
  
  const router = useRouter()
  const authStore = useAuthStore()
  const toast = useToast()
  const confirm = useConfirm()
  
  const searchKeyword = ref('')
  const menuItems = ref([]) // 빈 배열이라도 ref로 감싸는 것이 안전함
  
  // 검색 핸들러
  const handleSearch = () => {
    if (searchKeyword.value.trim()) {
      router.push({ name: 'search', query: { q: searchKeyword.value } })
    }
  }
  
  // [비로그인용] 퀴즈 생성 클릭 시 → 로그인 유도
  const handleCreateQuiz = () => {
    if (!authStore.isAuthenticated) {
      confirmLoginRedirect()
    } else {
      router.push('/quiz/create')
    }
  }
  
  // [비로그인용] 마이페이지 클릭 시 → 로그인 유도
  const handleMyPage = () => {
    if (!authStore.isAuthenticated) {
      confirmLoginRedirect()
    } else {
      router.push('/mypage')
    }
  }
  
  // 공통: 로그인 리다이렉트 확인창
  const confirmLoginRedirect = () => {
    confirm.require({
      message: 'Login is required. Would you like to go to the login page?',
      header: 'Login Required',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        router.push('/login')
      },
    })
  }
  
  // 단순 이동 함수들
  const goToLogin = () => router.push('/login')
  const goToSignup = () => router.push('/signup')
  const goToCreateQuiz = () => router.push('/quiz/create')
  const goToMyPage = () => router.push('/mypage')
  
  // 로그아웃 핸들러
  const handleLogout = () => {
    confirm.require({
      message: 'Are you sure you want to logout?',
      header: 'Logout',
      icon: 'pi pi-sign-out',
      acceptClass: 'p-button-danger',
      accept: () => {
        authStore.logoutUser()
        toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Logged out successfully.',
          life: 3000,
        })
        router.push('/') // 로그아웃 후 메인으로 이동
      },
    })
  }
  </script>
  
  <style scoped>
  .w-20rem {
    width: 20rem;
  }
  
  /* 마이페이지 버튼 스타일 조정 (텍스트 버튼처럼 보이게) */
  :deep(.p-button.p-button-text) {
    color: var(--text-color);
  }
  :deep(.p-button.p-button-text:hover) {
    background-color: var(--surface-hover);
  }
  </style>