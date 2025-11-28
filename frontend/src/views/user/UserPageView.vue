<template>
  <div class="user-profile-container">
    <div class="container mx-auto px-4">
      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else-if="user" class="flex flex-col gap-6">
        <!-- Profile Header -->
        <Card>
          <template #content>
            <div class="flex flex-col md:flex-row gap-6 items-center md:items-start">
              <Avatar :image="user.profileImageUrl" :label="user.nickname?.charAt(0)" shape="circle" size="xlarge" class="w-32 h-32" />
              <div class="flex-1 text-center md:text-left">
                <h1 class="text-3xl font-bold mb-2">{{ user.nickname }}</h1>
                <p v-if="user.statusMessage" class="text-lg text-color-secondary mb-4">
                  {{ user.statusMessage }}
                </p>
                <div class="flex flex-wrap justify-center md:justify-start gap-4">
                  <div class="text-center">
                    <p class="text-2xl font-bold m-0">{{ user.quizCount || 0 }}</p>
                    <p class="text-sm text-color-secondary m-0">Quizzes</p>
                  </div>
                  <div class="text-center">
                    <p class="text-2xl font-bold m-0">{{ user.followerCount || 0 }}</p>
                    <p class="text-sm text-color-secondary m-0">Followers</p>
                  </div>
                  <div class="text-center">
                    <p class="text-2xl font-bold m-0">{{ user.followingCount || 0 }}</p>
                    <p class="text-sm text-color-secondary m-0">Following</p>
                  </div>
                </div>
                <div class="mt-4">
                  <Button
                    v-if="authStore.isAuthenticated && authStore.user?.id !== user.id"
                    :label="isFollowing ? 'Following' : 'Follow'"
                    :icon="isFollowing ? 'pi pi-check' : 'pi pi-user-plus'"
                    :severity="isFollowing ? 'secondary' : undefined"
                    :outlined="isFollowing"
                    @click="handleFollow"
                  />
                </div>
              </div>
            </div>
          </template>
        </Card>

        <!-- User's Quizzes -->
        <div>
          <h2 class="text-2xl font-bold mb-4">Quizzes</h2>
          <div v-if="loadingQuizzes" class="text-center py-8">
            <i class="pi pi-spin pi-spinner text-4xl"></i>
          </div>
          <div v-else-if="quizzes.length === 0" class="text-center py-8 text-color-secondary">
            <p>No quizzes yet</p>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card v-for="quiz in quizzes" :key="quiz.id" class="quiz-card cursor-pointer" @click="goToQuiz(quiz.id)">
              <template #header>
                <div class="aspect-video bg-surface-100 overflow-hidden">
                  <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="w-full h-full object-cover" />
                </div>
              </template>
              <template #content>
                <div class="flex flex-col gap-3">
                  <h3 class="text-xl font-bold m-0">{{ quiz.title }}</h3>
                  <div class="flex items-center gap-4 text-sm text-color-secondary">
                    <span>
                      <i class="pi pi-heart"></i>
                      {{ quiz.likeCount || 0 }}
                    </span>
                    <span>
                      <i class="pi pi-play"></i>
                      {{ quiz.playCount || 0 }}
                    </span>
                  </div>
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { useAuthStore } from "@/stores/auth";
import { getUserProfile, followUser, unfollowUser, getUserQuizzes } from "@/api/user";

const route = useRoute();
const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();

const user = ref(null);
const quizzes = ref([]);
const loading = ref(false);
const loadingQuizzes = ref(false);
const isFollowing = ref(false);

const loadUserProfile = async () => {
  loading.value = true;
  try {
    const data = await getUserProfile(Number(route.params.id));
    user.value = data;
    isFollowing.value = data.isFollowedByMe || false;
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load user profile",
      life: 3000,
    });
    router.push("/");
  } finally {
    loading.value = false;
  }
};

const loadUserQuizzes = async () => {
  loadingQuizzes.value = true;
  try {
    quizzes.value = await getUserQuizzes(Number(route.params.id));
    if (user.value) {
      user.value.quizCount = quizzes.value.length;
    }
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load quizzes",
      life: 3000,
    });
  } finally {
    loadingQuizzes.value = false;
  }
};

const handleFollow = async () => {
  if (!authStore.isAuthenticated) {
    confirm.require({
      message: "로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?",
      header: "Login Required",
      icon: "pi pi-exclamation-triangle",
      accept: () => {
        router.push("/login");
      },
    });
    return;
  }

  try {
    if (isFollowing.value) {
      await unfollowUser(Number(route.params.id));
      isFollowing.value = false;
      user.value.followerCount = Math.max(0, (user.value.followerCount || 0) - 1);
    } else {
      await followUser(Number(route.params.id));
      isFollowing.value = true;
      user.value.followerCount = (user.value.followerCount || 0) + 1;
    }
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to toggle follow",
      life: 3000,
    });
  }
};

const goToQuiz = (quizId: number) => {
  router.push(`/quiz/${quizId}`);
};

onMounted(() => {
  loadUserProfile();
  loadUserQuizzes();
});

watch(
  () => route.params.id,
  () => {
    loadUserProfile();
    loadUserQuizzes();
  }
);
</script>

<style scoped>
.user-profile-container {
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

.aspect-video {
  aspect-ratio: 16 / 9;
}

.quiz-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}
</style>
