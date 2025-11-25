<template>
  <div class="home-container">
    <div class="container mx-auto px-4">
      <!-- Filter Tabs -->
      <div class="flex justify-content-center gap-4 mb-6">
        <Button :label="'Latest'" :severity="sort === 'latest' ? undefined : 'secondary'" :outlined="sort !== 'latest'" @click="sort = 'latest'" />
        <Button :label="'Popular'" :severity="sort === 'popular' ? undefined : 'secondary'" :outlined="sort !== 'popular'" @click="sort = 'popular'" />
      </div>

      <!-- Quiz Grid -->
      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else-if="quizzes.length === 0" class="text-center py-8">
        <p class="text-color-secondary text-xl">No quizzes found</p>
      </div>

      <div v-else class="quiz-grid-container mb-6">
        <div v-for="quiz in quizzes" :key="quiz.id" class="quiz-card cursor-pointer bg-white rounded-lg overflow-hidden border-2 border-gray-800 shadow-lg" @click="goToQuiz(quiz.id)">
          <!-- 상단: 썸네일 -->
          <div class="quiz-thumbnail bg-gray-200 overflow-hidden flex items-center justify-center">
            <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
          </div>

          <!-- 하단: 제목과 좋아요 정보 -->
          <div class="px-2 py-2 flex items-center justify-between gap-2">
            <h3 class="text-base font-bold m-0 truncate flex-1">{{ quiz.title }}</h3>
            <div class="flex items-center gap-[3px] text-xs text-color-secondary flex-shrink-0">
              <i class="pi pi-heart text-xs"></i>
              <span class="text-xs">{{ quiz.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <Paginator v-if="totalPages > 1" :first="(currentPage - 1) * pageSize" :rows="pageSize" :totalRecords="totalElements" @page="onPageChange" class="mb-6" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { getQuizzes } from "@/api/quiz";

const router = useRouter();
const toast = useToast();

const quizzes = ref([]);
const loading = ref(false);
const sort = ref("latest");
const currentPage = ref(1);
const pageSize = ref(12);
const totalPages = ref(0);
const totalElements = ref(0);

const loadQuizzes = async () => {
  loading.value = true;
  try {
    const response = await getQuizzes({
      page: currentPage.value,
      size: pageSize.value,
      sort: sort.value,
    });
    quizzes.value = response.content || [];
    totalPages.value = response.totalPages || 0;
    totalElements.value = response.totalElements || 0;
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load quizzes",
      life: 3000,
    });
  } finally {
    loading.value = false;
  }
};

const goToQuiz = (quizId: number) => {
  router.push(`/quiz/${quizId}`);
};

const onPageChange = (event: any) => {
  currentPage.value = event.page + 1;
  loadQuizzes();
};

watch(sort, () => {
  currentPage.value = 1;
  loadQuizzes();
});

onMounted(() => {
  loadQuizzes();
});
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
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
