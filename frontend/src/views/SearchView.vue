<template>
  <div class="search-container">
    <div class="container">
      <div class="home-search-row">
        <div class="search-bar-wide w-full">
          <span class="p-input-icon-right search-input-wrapper">
            <InputText
              v-model="searchKeyword"
              placeholder="검색어를 입력하세요."
              class="search-input w-full"
              @keyup.enter="handleSearch"
            />
            <i class="pi pi-search search-icon" @click="handleSearch" />
          </span>
        </div>
      </div>

      <div class="home-controls mb-6">
        <div class="flex gap-1 filter-group">
          <Button icon="pi pi-heart" :label="'인기'" :class="['filter-button', { 'is-active': sort === 'popular' }]" :severity="sort === 'popular' ? undefined : 'secondary'" :outlined="sort !== 'popular'" @click="sort = 'popular'" />
          <Button icon="pi pi-eye" :label="'조회'" :class="['filter-button', { 'is-active': sort === 'views' }]" :severity="sort === 'views' ? undefined : 'secondary'" :outlined="sort !== 'views'" @click="sort = 'views'" />
          <Button icon="pi pi-clock" :label="'최신'" :class="['filter-button', { 'is-active': sort === 'latest' }]" :severity="sort === 'latest' ? undefined : 'secondary'" :outlined="sort !== 'latest'" @click="sort = 'latest'" />
        </div>
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
          <div class="quiz-body">
            <h3 class="quiz-title">{{ quiz.title }}</h3>
            <div class="quiz-meta-row">
              <span class="quiz-meta-item">
                <i class="pi pi-eye"></i>
                <span>{{ quiz.playCount || 0 }}</span>
              </span>
              <span class="quiz-meta-item">
                <i class="pi pi-heart"></i>
                <span>{{ quiz.likeCount || 0 }}</span>
              </span>
              <span class="quiz-meta-item">
                <i class="pi pi-comments"></i>
                <span>{{ quiz.commentCount ?? 0 }}</span>
              </span>
            </div>
            <p class="quiz-description">{{ quiz.description || quiz.desc || quiz.summary || "설명이 없습니다." }}</p>
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

// 검색어 변경 시 라우팅을 자동으로 동기화: 비우면 메인, 입력하면 검색 결과로 이동
watch(
  searchKeyword,
  (value) => {
    const trimmed = value.trim();
    if (!trimmed) {
      router.push({ name: "home" });
      return;
    }
    if (route.name !== "search" || route.query.q !== trimmed) {
      router.push({ name: "search", query: { q: trimmed } });
    }
  },
  { flush: "post" }
);

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
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 12px;
}

.home-search-row {
  margin-top: 1.5rem;
  margin-bottom: 1.25rem;
  display: flex;
  justify-content: flex-start;
  width: 100%;
}

.search-bar-wide {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 0.75rem;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
}

.search-input-wrapper .search-input {
  width: 100%;
  border-radius: 999px;
  padding-left: 1.25rem;
  padding-right: 2.5rem;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--color-border);
  color: var(--color-text-main);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
}

.search-input-wrapper .search-icon {
  cursor: pointer;
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
}

:global([data-theme="dark"] .search-input-wrapper .search-input) {
  background: rgba(35, 45, 80, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: var(--color-heading);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.45);
}

.home-controls {
  display: grid;
  grid-template-columns: auto;
  align-items: center;
  gap: 1rem;
  padding: 0 0 1rem;
}

.filter-group {
  display: inline-flex;
  gap: 0.5rem;
  flex-wrap: nowrap;
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
  background: var(--color-background-soft);
  border-radius: 18px;
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
  border-color: rgba(148, 163, 184, 0.6);
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

.quiz-title {
  font-size: 1rem;
  font-weight: 700;
  margin: 0 0 0.3rem;
  color: var(--color-heading);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.quiz-body {
  padding: 0 0.4rem 0.6rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.quiz-meta-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.quiz-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
}

.quiz-meta-item i {
  font-size: 0.85rem;
}

.quiz-description {
  margin: 0.1rem 0 0;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
