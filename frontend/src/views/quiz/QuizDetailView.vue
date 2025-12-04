<template>
  <div class="quiz-detail-container">
    <div class="container mx-auto px-4">
      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else-if="quiz" class="flex flex-col gap-6">
        <Card>
          <template #header>
            <div class="aspect-video bg-surface-100 overflow-hidden">
              <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="w-full h-full object-cover" />
            </div>
          </template>
          <template #content>
            <div class="flex flex-col gap-4">
              <div class="flex justify-between items-start gap-4">
                <div class="flex-1">
                  <h1 class="text-3xl font-bold mb-2">
                    {{ quiz.title }}
                  </h1>
                  <p class="text-color-secondary text-lg">
                    {{ quiz.description }}
                  </p>
                </div>

                <div class="flex flex-col items-end gap-2 shrink-0">
                  <div class="view-chip">
                    <i class="pi pi-eye text-xs"></i>
                    <span>{{ quiz.playCount || 0 }}</span>
                  </div>
                  <!-- 👍 좋아요 버튼: 내가 만든 퀴즈면 disabled -->
                  <Button
                    :icon="isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                    :label="`${quiz.likeCount || 0}`"
                    :severity="isLiked ? undefined : 'secondary'"
                    :outlined="!isLiked"
                    :disabled="isMyQuiz"
                    :title="isMyQuiz ? '내가 만든 퀴즈에는 좋아요를 누를 수 없습니다.' : ''"
                    @click="handleLike"
                  />
                </div>
              </div>

              <!-- 작성자 영역: 클릭 시 팝오버 오픈 -->
              <div class="flex items-center gap-3 cursor-pointer" @click="openAuthorPopover">
                           <UserAvatar
                            :user="quiz.author"
                            size="medium"
                            class="mr-1 font-bold"
                            :class="{ 'surface-200 text-700': !quiz.author?.profileImageUrl }"
                          />
                <div>
                  <p class="font-semibold m-0">
                    {{ quiz.author?.nickname }}
                  </p>
                  <p class="text-sm text-color-secondary m-0">{{ quiz.questions?.length || 0 }} questions</p>
                </div>
              </div>

              <div class="flex gap-3">
                <Button label="Start Quiz" icon="pi pi-play" size="large" class="flex-1" @click="startQuiz"></Button>
              </div>
            </div>
          </template>
        </Card>

        <!-- 댓글 섹션 (퀴즈 시작 화면에서 노출) -->
        <Card>
          <template #content>
            <div class="flex justify-between items-center mb-3">
              <h3 class="text-xl font-bold m-0">댓글</h3>
              <span class="text-sm text-color-secondary"
                >퀴즈 시작 전에 자유롭게 소통하세요</span
              >
            </div>
            <CommentSection :quiz-id="quizId" />
          </template>
        </Card>

        <!-- ✅ 유저 팝오버 컴포넌트 -->
        <UserPopover ref="userPopoverRef" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { useAuthStore } from "@/stores/auth";
import { useQuizStore } from "@/stores/quiz";
import { getQuiz, likeQuiz, unlikeQuiz } from "@/api/quiz";
import { followUser, unfollowUser } from "@/api/user";
import UserPopover from "@/components/common/UserPopover.vue";
import UserAvatar from '@/components/common/UserAvatar.vue' // 유저 아바타 불러오기 
import CommentSection from "@/components/comment/CommentSection.vue";

const route = useRoute();
const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();
const quizStore = useQuizStore();

const quiz = ref<any | null>(null);
const loading = ref(false);
const isLiked = ref(false);
const isFollowed = ref(false);
const quizId = computed(() => Number(route.params.id));

// 팝오버 ref
const userPopoverRef = ref<any | null>(null);

// 로그인한 유저 ID
const currentUserId = computed(() => authStore.user?.id ?? null);

// 내가 만든 퀴즈인지 여부 (백엔드 QuizResponse.userId 사용)
const isMyQuiz = computed(() => {
  if (!quiz.value || !currentUserId.value) return false;
  return quiz.value.userId === currentUserId.value;
});

const loadQuiz = async () => {
  loading.value = true;
  try {
    const data = await getQuiz(Number(route.params.id));
    quiz.value = data;
    isLiked.value = data.isLikedByMe || false;
    isFollowed.value = data.isFollowedByMe || false;
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load quiz",
      life: 3000,
    });
  } finally {
    loading.value = false;
  }
};

// 작성자 프로필 클릭 → 팝오버 오픈
const openAuthorPopover = (event: MouseEvent) => {
  if (!quiz.value?.author?.id) return;
  userPopoverRef.value?.open(event, quiz.value.author.id);
};

const handleLike = async () => {
  // 1. 내 퀴즈면 프론트에서도 바로 막기
  if (isMyQuiz.value) {
    toast.add({
      severity: "warn",
      summary: "알림",
      detail: "내가 만든 퀴즈에는 좋아요를 누를 수 없습니다.",
      life: 3000,
    });
    return;
  }

  // 2. 비로그인인 경우 로그인 유도 (기존 로직 유지)
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
    if (isLiked.value) {
      await unlikeQuiz(Number(route.params.id));
      isLiked.value = false;
      quiz.value.likeCount = Math.max(0, (quiz.value.likeCount || 0) - 1);
    } else {
      await likeQuiz(Number(route.params.id));
      isLiked.value = true;
      quiz.value.likeCount = (quiz.value.likeCount || 0) + 1;
    }
  } catch (error: any) {
    if (error.code === "Q003") {
      toast.add({
        severity: "warn",
        summary: "알림",
        detail: error.message || "내가 만든 퀴즈에는 좋아요를 누를 수 없습니다.",
        life: 3000,
      });
      return;
    }

    toast.add({
      severity: "error",
      summary: "Error",
      detail: error.message || "Failed to toggle like",
      life: 3000,
    });
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
    if (isFollowed.value) {
      await unfollowUser(quiz.value.author.id);
      isFollowed.value = false;
    } else {
      await followUser(quiz.value.author.id);
      isFollowed.value = true;
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

const startQuiz = () => {
  if (quiz.value) {
    quizStore.startQuiz(quiz.value);
    router.push(`/quiz/${route.params.id}/play`);
  }
};

onMounted(() => {
  loadQuiz();
});
</script>

<style scoped>
.quiz-detail-container {
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.aspect-video {
  aspect-ratio: 16 / 9;
}

.view-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: var(--color-heading);
  font-size: 0.9rem;
}
</style>
