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
        <template v-if="!authStore.isAuthenticated">
          <Button label="퀴즈 만들기" icon="pi pi-plus" text @click="handleCreateQuiz" />
          <Button label="로그인" icon="pi pi-sign-in" @click="goToLogin" />
        </template>

        <template v-else>
          <Button label="퀴즈 만들기" icon="pi pi-plus" text @click="goToCreateQuiz" />

          <Button @click="goToMyPage" class="flex align-items-center gap-2 p-button-text p-0" aria-label="My Page">

                <UserAvatar class="mr-1 font-bold" :class="{ 'surface-200 text-700': !authStore.user?.profileImageUrl }"
                size="medium" />
          
            <span class="text-color">{{ authStore.user?.nickname || "User" }}</span>
          </Button>

          <Button label="로그아웃" icon="pi pi-sign-out" severity="danger" text @click="handleLogout" />
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
</style>
