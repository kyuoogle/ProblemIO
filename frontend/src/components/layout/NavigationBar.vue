<template>
  <Menubar :model="menuItems" class="border-bottom-1 surface-border app-header">
    <template #start>
      <router-link to="/" class="flex align-items-center gap-2 no-underline text-dark logo-link">
        <img src="/problemio-logo-light.png" alt="Problem.io" class="logo-img logo-light" />
        <img src="/problemio-logo.png" alt="Problem.io" class="logo-img logo-dark" />
      </router-link>
    </template>

    <template #end>
      <div class="flex align-items-center gap-3">
        <template v-if="!authStore.isAuthenticated">
          <Button label="퀴즈 만들기" icon="pi pi-plus" class="nav-ghost" text @click="handleCreateQuiz" />
          <Button label="로그인" icon="pi pi-sign-in" class="nav-ghost" @click="goToLogin" />
        </template>

        <template v-else>
          <Button label="퀴즈 만들기" icon="pi pi-plus" class="nav-ghost" text @click="goToCreateQuiz" />

          <Button @click="goToMyPage" class="nav-ghost flex align-items-center gap-2 p-button-text p-0" aria-label="My Page">

                <UserAvatar class="mr-1 font-bold" :class="{ 'surface-200 text-700': !authStore.user?.profileImageUrl }"
                size="medium" />
          
            <span class="text-color">{{ authStore.user?.nickname || "User" }}</span>
          </Button>

          <Button label="로그아웃" icon="pi pi-sign-out" severity="danger" text class="logout-btn" @click="handleLogout" />
        </template>
      </div>
    </template>
  </Menubar>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import UserAvatar from '@/components/common/UserAvatar.vue' // 유저 아바타 불러오기 

const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();
const confirm = useConfirm();

const searchKeyword = ref("");
const menuItems = ref([]); // 빈 배열이라도 ref로 감싸는 것이 안전함

// [비로그인용] 퀴즈 생성 클릭 시 → 로그인 유도
const handleCreateQuiz = () => {
  if (!authStore.isAuthenticated) {
    confirmLoginRedirect();
  } else {
    router.push("/quiz/create");
  }
};

// 공통: 로그인 리다이렉트 확인창
const confirmLoginRedirect = () => {
  confirm.require({
    message: "로그인 창으로 이동하시겠습니까?",
    header: "로그인이 필요한 서비스입니다.    ",
    icon: "pi pi-exclamation-triangle",
    accept: () => {
      router.push("/login");
    },
  });
};

// 단순 이동 함수들
const goToLogin = () => router.push("/login");
const goToCreateQuiz = () => router.push("/quiz/create");
const goToMyPage = () => router.push("/mypage");

// 로그아웃 핸들러
const handleLogout = () => {
  confirm.require({
    message: "로그아웃 하시겠습니까?",
    header: "Logout",
    icon: "pi pi-sign-out",
    acceptClass: "p-button-danger",
    accept: () => {
      authStore.logoutUser();
      toast.add({
        severity: "success",
        summary: "로그아웃",
        detail: "로그아웃 되었습니다",
        life: 3000,
      });
      router.push("/"); // 로그아웃 후 메인으로 이동
    },
  });
};
</script>

<style scoped>
.w-20rem {
  width: 20rem;
}

:deep(.p-button.p-button-text) {
  color: var(--text-color);
}
:deep(.p-button.p-button-text:hover) {
  background-color: var(--surface-hover);
}

.nav-ghost {
  border: none !important;
  background: transparent !important;
  color: var(--color-heading) !important;
  font-weight: 600;
}

.nav-ghost:hover {
  background: rgba(0, 150, 136, 0.08) !important; /* 아주 연한 틸 */
  color: #006f62 !important; /* 틸톤 텍스트 */
  border: none !important;
}

.app-header {
  background:
    linear-gradient(var(--app-header-bg-overlay), var(--app-header-bg-overlay)),
    var(--app-header-bg-image);
  background-color: var(--color-background-soft);
  background-size: cover;
  background-position: center center;
  border-bottom: 1px solid var(--color-border);
  backdrop-filter: blur(10px);
}

.app-header :deep(.p-menubar-root-list),
.app-header :deep(.p-menubar-button) {
  color: var(--color-heading);
}

.app-header :deep(.p-inputtext) {
  background: var(--color-background-soft);
  color: var(--color-heading);
  border-color: var(--color-border);
}

.logo-link {
  height: 44px;
}

.logo-img {
  height: 44px;
  width: auto;
  object-fit: contain;
}

.logo-dark {
  display: none;
}

:global([data-theme="dark"] .logo-light) {
  display: none;
}

:global([data-theme="dark"] .logo-dark) {
  display: block;
}

.logout-btn {
  color: #dc2626 !important;
  transition: background-color 0.15s ease, color 0.15s ease, transform 0.12s ease;
}

.logout-btn:hover {
  background: rgba(220, 38, 38, 0.1) !important;
  color: #b91c1c !important;
  transform: translateY(-1px);
}

.text-dark {
  color: var(--color-heading);
}

/* Dark theme header harmonization */
:global([data-theme="dark"] .p-menubar) {
  background: var(--color-background-soft) !important;
  border-color: var(--color-border) !important;
  color: var(--color-heading) !important;
}

:global([data-theme="dark"] .p-menubar .p-menuitem-link) {
  color: var(--color-heading) !important;
}

:global([data-theme="dark"] .p-menubar .p-menuitem-link:hover) {
  background: rgba(255, 255, 255, 0.04) !important;
}

:global([data-theme="dark"] .p-menubar .p-inputtext) {
  background: #111821 !important;
  color: var(--color-heading) !important;
  border-color: var(--color-border) !important;
}

:global([data-theme="dark"] .p-menubar .p-inputtext:focus) {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 1px rgba(26, 188, 156, 0.4);
}

:global([data-theme="dark"] .nav-ghost) {
  color: var(--color-heading) !important;
}

:global([data-theme="dark"] .nav-ghost:hover) {
  background: rgba(26, 188, 156, 0.12) !important;
  color: var(--color-heading) !important;
}

:global([data-theme="dark"] .logout-btn) {
  color: #fca5a5 !important;
}

:global([data-theme="dark"] .logout-btn:hover) {
  background: rgba(239, 68, 68, 0.16) !important;
  color: #fecdd3 !important;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.35);
}
</style>
