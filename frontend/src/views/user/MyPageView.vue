<template>
  <div class="mypage-container">
    <div class="container mx-auto px-4">
      <!-- Navbar 스타일의 탭 네비게이션 -->
      <div class="tab-navbar">
        <button :class="['tab-button', { active: activeTab === 'my' }]" @click="activeTab = 'my'">내 퀴즈 목록</button>
        <button :class="['tab-button', { active: activeTab === 'following' }]" @click="activeTab = 'following'">팔로우한 유저들의 퀴즈</button>
        <button :class="['tab-button', { active: activeTab === 'liked' }]" @click="activeTab = 'liked'">좋아요한 퀴즈</button>
      </div>

      <!-- 내 퀴즈 목록 탭 -->
      <div v-show="activeTab === 'my'" class="tab-content">
        <div v-if="myQuizzesLoading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>

        <div v-else-if="myQuizzes.length === 0" class="text-center py-8 text-color-secondary">
          <p class="text-xl mb-4">아직 퀴즈가 없네요! 퀴즈를 만들어 친구들과 공유해보세요.</p>
          <Button label="퀴즈 만들기" icon="pi pi-plus" @click="goToCreateQuiz" />
        </div>

        <div v-else class="quiz-grid-container">
          <div v-for="quiz in myQuizzes" :key="quiz.id" class="quiz-card bg-white rounded-lg overflow-hidden border-2 border-gray-800 shadow-lg">
            <!-- 상단: 썸네일 -->
            <div class="quiz-thumbnail bg-gray-200 overflow-hidden flex items-center justify-center">
              <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
            </div>

            <!-- 하단: 제목과 좋아요 정보 -->
            <div class="px-2 py-2 flex items-center justify-between gap-2">
              <h3 class="text-sm font-bold m-0 truncate flex-1">{{ quiz.title }}</h3>
              <div class="flex items-center gap-1 text-xs text-color-secondary flex-shrink-0">
                <i class="pi pi-heart"></i>
                <span>{{ quiz.likeCount || 0 }}</span>
              </div>
            </div>

            <!-- 액션 버튼들 -->
            <div class="px-2 pb-2 flex gap-1">
              <Button label="View" icon="pi pi-eye" severity="secondary" outlined size="small" class="flex-1 text-xs" @click="goToQuiz(quiz.id)" />
              <Button label="Edit" icon="pi pi-pencil" severity="secondary" outlined size="small" class="flex-1 text-xs" @click="goToEdit(quiz.id)" />
              <Button label="Del" icon="pi pi-trash" severity="danger" outlined size="small" class="text-xs" @click="handleDelete(quiz.id)" />
            </div>
          </div>
        </div>
      </div>

      <!-- 팔로우한 유저들의 퀴즈 탭 -->
      <div v-show="activeTab === 'following'" class="tab-content">
        <div v-if="followingQuizzesLoading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>

        <div v-else-if="followingQuizzes.length === 0" class="text-center py-8 text-color-secondary">
          <h2 class="text-2xl font-bold mb-4">아직 팔로우하는 유저가 없어요.</h2>
        </div>

        <div v-else class="quiz-grid-container">
          <div v-for="quiz in followingQuizzes" :key="quiz.id" class="quiz-card cursor-pointer bg-white rounded-lg overflow-hidden border-2 border-gray-800 shadow-lg" @click="goToQuiz(quiz.id)">
            <!-- 상단: 썸네일 -->
            <div class="quiz-thumbnail bg-gray-200 overflow-hidden flex items-center justify-center">
              <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
            </div>

            <!-- 하단: 제목과 좋아요 정보 -->
            <div class="px-2 py-2 flex items-center justify-between gap-2">
              <h3 class="text-sm font-bold m-0 truncate flex-1">{{ quiz.title }}</h3>
              <div class="flex items-center gap-1 text-xs text-color-secondary flex-shrink-0">
                <i class="pi pi-heart"></i>
                <span>{{ quiz.likeCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 좋아요한 퀴즈 탭 -->
      <div v-show="activeTab === 'liked'" class="tab-content">
        <div v-if="likedQuizzesLoading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>

        <div v-else-if="likedQuizzes.length === 0" class="text-center py-8 text-color-secondary">
          <h2 class="text-2xl font-bold mb-4">아직 좋아하는 퀴즈가 없네요!</h2>
          <p class="text-xl">퀴즈 풀러 가기</p>
        </div>

        <div v-else class="quiz-grid-container">
          <div v-for="quiz in likedQuizzes" :key="quiz.id" class="quiz-card cursor-pointer bg-white rounded-lg overflow-hidden border-2 border-gray-800 shadow-lg" @click="goToQuiz(quiz.id)">
            <!-- 상단: 썸네일 -->
            <div class="quiz-thumbnail bg-gray-200 overflow-hidden flex items-center justify-center">
              <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
            </div>

            <!-- 하단: 제목과 좋아요 정보 -->
            <div class="px-2 py-2 flex items-center justify-between gap-2">
              <h3 class="text-sm font-bold m-0 truncate flex-1">{{ quiz.title }}</h3>
              <div class="flex items-center gap-1 text-xs text-color-secondary flex-shrink-0">
                <i class="pi pi-heart"></i>
                <span>{{ quiz.likeCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { getFollowingQuizzes, getMyLikedQuizzes } from "@/api/user";
import { getMyQuizzes, deleteQuiz } from "@/api/quiz";

const router = useRouter();
const toast = useToast();
const confirm = useConfirm();

// 활성 탭 상태
const activeTab = ref("my");

// 내 퀴즈 목록
const myQuizzes = ref([]);
const myQuizzesLoading = ref(false);

// 팔로우한 유저들의 퀴즈들
const followingQuizzes = ref([]);
const followingQuizzesLoading = ref(false);

// 좋아요한 퀴즈들
const likedQuizzes = ref([]);
const likedQuizzesLoading = ref(false);

// 내 퀴즈 목록 로드
const loadMyQuizzes = async () => {
  myQuizzesLoading.value = true;
  try {
    myQuizzes.value = await getMyQuizzes();
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load my quizzes",
      life: 3000,
    });
  } finally {
    myQuizzesLoading.value = false;
  }
};

// 팔로우한 유저들의 퀴즈들 로드
const loadFollowingQuizzes = async () => {
  followingQuizzesLoading.value = true;
  try {
    followingQuizzes.value = await getFollowingQuizzes();
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load following users' quizzes",
      life: 3000,
    });
  } finally {
    followingQuizzesLoading.value = false;
  }
};

// 좋아요한 퀴즈들 로드
const loadLikedQuizzes = async () => {
  likedQuizzesLoading.value = true;
  try {
    likedQuizzes.value = await getMyLikedQuizzes();
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load liked quizzes",
      life: 3000,
    });
  } finally {
    likedQuizzesLoading.value = false;
  }
};

// 모든 데이터 로드
const loadAllData = async () => {
  await Promise.all([loadMyQuizzes(), loadFollowingQuizzes(), loadLikedQuizzes()]);
};

const goToQuiz = (quizId: number) => {
  router.push(`/quiz/${quizId}`);
};

const goToEdit = (quizId: number) => {
  router.push(`/quiz/edit/${quizId}`);
};

const goToCreateQuiz = () => {
  router.push("/quiz/create");
};

const handleDelete = (quizId: number) => {
  confirm.require({
    message: "Are you sure you want to delete this quiz?",
    header: "Delete Quiz",
    icon: "pi pi-exclamation-triangle",
    accept: async () => {
      try {
        await deleteQuiz(quizId);
        toast.add({
          severity: "success",
          summary: "Success",
          detail: "Quiz deleted successfully",
          life: 3000,
        });
        loadMyQuizzes();
      } catch (error: any) {
        toast.add({
          severity: "error",
          summary: "Error",
          detail: "Failed to delete quiz",
          life: 3000,
        });
      }
    },
  });
};

const formatDate = (dateString: string) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  return date.toLocaleDateString();
};

onMounted(() => {
  loadAllData();
});
</script>

<style scoped>
.mypage-container {
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.tab-navbar {
  display: flex;
  gap: 0;
  border-bottom: 2px solid var(--surface-border);
  margin-bottom: 2rem;
  background: var(--surface-ground);
}

.tab-button {
  flex: 1;
  padding: 1rem 1.5rem;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-color-secondary);
  transition: all 0.3s ease;
  position: relative;
}

.tab-button:hover {
  background: var(--surface-hover);
  color: var(--text-color);
}

.tab-button.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  font-weight: 600;
}

.tab-content {
  padding: 1rem 0;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.quiz-grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

@media (max-width: 768px) {
  .quiz-grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .quiz-grid-container {
    grid-template-columns: 1fr;
  }
}

.quiz-thumbnail {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quiz-thumbnail-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  object-position: center;
  image-rendering: auto;
  transform: scale(0.5);
  transform-origin: center;
}

.quiz-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}
</style>
