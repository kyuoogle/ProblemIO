<template>
  <div class="search-container">
    <div class="container mx-20 px-4">
      <div class="home-controls mb-6">
        <div class="flex gap-1">
          <Button :label="'인기순'" :severity="sort === 'popular' ? undefined : 'secondary'" :outlined="sort !== 'popular'" @click="sort = 'popular'" />
          <Button :label="'최신순'" :severity="sort === 'latest' ? undefined : 'secondary'" :outlined="sort !== 'latest'" @click="sort = 'latest'" />
        </div>
        <span class="p-input-icon-left home-search">
          <i class="pi pi-search" />
          <InputText v-model="searchKeyword" placeholder="Search quizzes..." class="w-20rem max-w-full" @keyup.enter="handleSearch" />
        </span>
      </div>

      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else-if="quizzes.length === 0" class="text-center py-8">
        <p class="text-color-secondary text-xl">검색 결과가 없습니다.</p>
      </div>

      <div v-else class="quiz-grid-container mb-6">
        <div v-for="quiz in quizzes" :key="quiz.id" class="quiz-card cursor-pointer" @click="goToQuiz(quiz.id)">
          <div class="quiz-thumbnail">
            <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
          </div>
          <div class="quiz-meta">
            <h3 class="quiz-title">{{ quiz.title }}</h3>
            <div class="quiz-stat">
              <i class="pi pi-heart text-xs"></i>
              <span>{{ quiz.likeCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <Paginator v-if="totalPages > 1" :first="(currentPage - 1) * pageSize" :rows="pageSize" :totalRecords="totalElements" @page="onPageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { getQuizzes } from "@/api/quiz";

const route = useRoute();
const router = useRouter();
const toast = useToast();

const quizzes = ref([]);
const loading = ref(false);
const searchKeyword = ref("");
const currentPage = ref(1);
const pageSize = ref(12);
const totalPages = ref(0);
const totalElements = ref(0);
const sort = ref("popular");

const loadQuizzes = async () => {
  if (!searchKeyword.value) {
    quizzes.value = [];
    return;
  }

  loading.value = true;
  try {
    const response = await getQuizzes({
      page: currentPage.value,
      size: pageSize.value,
      sort: sort.value,
      keyword: searchKeyword.value,
    });
    quizzes.value = response.content || [];
    totalPages.value = response.totalPages || 0;
    totalElements.value = response.totalElements || 0;
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load search results",
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

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: "search", query: { q: searchKeyword.value } });
  }
};

watch(
  () => route.query.q,
  (newQuery) => {
    searchKeyword.value = (newQuery as string) || "";
    currentPage.value = 1;
    if (searchKeyword.value) {
      loadQuizzes();
    }
  },
  { immediate: true }
);

watch(sort, () => {
  currentPage.value = 1;
  if (searchKeyword.value) {
    loadQuizzes();
  }
});
</script>

<style scoped>
.search-container {
  min-height: calc(100vh - 200px);
  padding: 1rem 0 2rem;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.home-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0 0 1rem;
  flex-wrap: wrap;
}

.home-search {
  margin-left: auto;
}

.w-20rem {
  width: 20rem;
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

.quiz-card {
  background: #ffffffee;
  border-radius: 18px;
  border: 1px solid rgba(55, 65, 81, 0.06);
  padding: 0.6rem 0.6rem 0.3rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  box-shadow: var(--surface-glow);
  transition: transform 0.2s, box-shadow 0.2s;
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 28px rgba(0, 0, 0, 0.08);
}

.quiz-card:hover .quiz-thumbnail-img {
  transform: scale(1.03);
}

.quiz-thumbnail {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 14px;
  background: linear-gradient(180deg, #eef3f6, #f7ede8);
}

.quiz-thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  image-rendering: auto;
  transition: transform 0.2s ease;
}

.quiz-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0 0.4rem 0.4rem;
}

.quiz-title {
  font-size: 1rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-heading);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quiz-stat {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.3rem 0.6rem;
  border-radius: 999px;
  background: rgba(137, 168, 124, 0.15);
  color: var(--color-heading);
  font-size: 0.85rem;
}
</style>
