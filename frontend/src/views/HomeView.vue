<template>
  <div class="home-container">
    <div class="container page-container">
      <!-- 긴 검색바 -->
      <div class="home-search-row">
        <div class="search-bar-wide w-full">
          <Button class="search-filter-btn" icon="pi pi-filter" rounded text @click="toggleFilterPanel($event)"></Button>
          <OverlayPanel ref="filterPanel">
            <div class="search-filter-menu">
              <div v-for="opt in filterOptions" :key="opt.value" class="search-filter-item" :class="{ 'is-active': searchSort === opt.value }" @click="selectSearchSort(opt.value)">
                <i v-if="searchSort === opt.value" class="pi pi-check" ></i>
                <span>{{ opt.label }}</span>
              </div>
            </div>
          </OverlayPanel>

          <span class="p-input-icon-right search-input-wrapper">
            <InputText v-model="searchKeyword" placeholder="검색어를 입력하세요." class="search-input w-full" @keyup.enter="handleSearch" />
            <i class="pi pi-search search-icon" @click="handleSearch" ></i>
          </span>
        </div>
      </div>

      <!-- 기존 퀵 필터 -->
      <div class="home-controls mb-6">
        <div class="flex gap-1 filter-group">
          <Button
            icon="pi pi-heart"
            :label="'인기'"
            :class="['filter-button', { 'is-active': sort === 'popular' }]"
            :severity="sort === 'popular' ? undefined : 'secondary'"
            :outlined="sort !== 'popular'"
            @click="sort = 'popular'"
          ></Button>
          <Button
            icon="pi pi-eye"
            :label="'조회'"
            :class="['filter-button', { 'is-active': sort === 'views' }]"
            :severity="sort === 'views' ? undefined : 'secondary'"
            :outlined="sort !== 'views'"
            @click="sort = 'views'"
          ></Button>
          <Button
            icon="pi pi-clock"
            :label="'최신'"
            :class="['filter-button', { 'is-active': sort === 'latest' }]"
            :severity="sort === 'latest' ? undefined : 'secondary'"
            :outlined="sort !== 'latest'"
            @click="sort = 'latest'"
          ></Button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else-if="quizzes.length === 0" class="text-center py-8 empty-state">
        <p class="text-color-secondary text-xl">No quizzes found</p>
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

      <Paginator v-if="totalPages > 1" :first="(currentPage - 1) * pageSize" :rows="pageSize" :totalRecords="totalElements" @page="onPageChange" class="mb-6" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { getQuizzes } from "@/api/quiz";
import OverlayPanel from "primevue/overlaypanel";

const router = useRouter();
const toast = useToast();

const quizzes = ref([]);
const searchKeyword = ref("");
const searchSort = ref<"popular" | "latest">("popular");
const filterOptions = [
  { label: "인기순", value: "popular" },
  { label: "최신순", value: "latest" },
];
const filterPanel = ref();
const loading = ref(false);
const sort = ref("popular");
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

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: "search", query: { q: searchKeyword.value, sort: searchSort.value } });
  }
};

const onPageChange = (event: any) => {
  currentPage.value = event.page + 1;
  loadQuizzes();
};

const toggleFilterPanel = (event: Event) => {
  filterPanel.value?.toggle(event);
};

const selectSearchSort = (value: "popular" | "latest") => {
  searchSort.value = value;
  filterPanel.value?.hide();
  handleSearch();
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
  padding: 1rem 0 2rem;
}

.container {
  width: 100%;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 12px;
}

/* Filters + Search in one line */
.home-controls {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 0 1.5rem;
  width: 100%;
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

.search-filter-btn {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  background: var(--color-bg-card) !important;
  border: 1px solid var(--color-border) !important;
  color: var(--color-text-main) !important;
}

:global([data-theme="dark"] .search-filter-btn) {
  background: rgba(35, 45, 80, 0.96) !important;
  border: 1px solid rgba(255, 255, 255, 0.18) !important;
  color: var(--color-heading) !important;
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
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
}

:global([data-theme="dark"] .search-input-wrapper .search-input) {
  background: rgba(35, 45, 80, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: var(--color-heading);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.45);
}

.search-filter-menu {
  min-width: 160px;
  padding: 6px 0;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.search-filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 0.875rem;
  color: var(--color-heading);
}

.search-filter-item:hover {
  background-color: var(--color-background-mute);
}

.search-filter-item.is-active {
  font-weight: 600;
}

.filter-group {
  display: inline-flex;
  gap: 0.5rem;
  flex-wrap: nowrap;
}

.filter-button {
  color: var(--color-text-muted) !important;
  background: var(--color-bg-card) !important;
  border-color: var(--color-border) !important;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

:global(.filter-button.is-active) {
  background: var(--brand-cyan-soft) !important;
  border-color: var(--brand-cyan) !important;
  color: #0369a1 !important;
  box-shadow: 0 10px 26px rgba(19, 184, 163, 0.26);
}

:global([data-theme="dark"] .filter-button.is-active) {
  background: #0fb397 !important;
  border-color: #1abc9c !important;
  color: #ffffff !important;
  box-shadow: 0 10px 28px rgba(16, 185, 129, 0.35);
}

:global(.filter-button.is-active:hover) {
  background: var(--color-primary-hover) !important;
  border-color: var(--color-primary-hover) !important;
  color: #00131c !important;
}

:global([data-theme="dark"] .filter-button) {
  color: #e5e7eb !important;
  border-color: #374151 !important;
  background: rgba(255, 255, 255, 0.04) !important;
}

:global([data-theme="dark"] .filter-button.p-button-outlined) {
  border-color: #4b5563 !important;
  color: #e5e7eb !important;
}

.filter-button:hover {
  background: rgba(0, 150, 136, 0.1) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-heading) !important;
}

:global([data-theme="dark"] .filter-button:hover) {
  background: rgba(26, 188, 156, 0.18) !important;
  border-color: #34d399 !important;
  color: #f9fafb !important;
}

.home-search {
  width: 100%;
  display: block;
}

.search-bar {
  width: 100%;
  max-width: 720px;
  min-width: 360px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  color: var(--color-text-main);
}

/* Grid */
.quiz-grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  padding: 0;
  width: 100%;
}

@media (max-width: 1200px) {
  .quiz-grid-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 960px) {
  .quiz-grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-controls {
    grid-template-columns: 1fr;
    max-width: 100%;
  }
  .home-search-row {
    margin-top: 1rem;
    margin-bottom: 1rem;
  }
  .search-bar {
    width: 100%;
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
  overflow: hidden;
  border-radius: 14px;
  background: linear-gradient(180deg, #eef3f6, #f7ede8);
}

:global([data-theme="dark"] .quiz-thumbnail) {
  background: #0f0f0f;
  border: 1px solid #2d2d2d;
}

.quiz-thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  image-rendering: auto;
  transition: transform 0.2s ease;
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

:global([data-theme="dark"] .quiz-card) {
  background: #161616;
  border: 1px solid #262626;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
  border-color: rgba(148, 163, 184, 0.6);
}

:global([data-theme="dark"] .quiz-card:hover) {
  background: #1e1e1e;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.55), 0 0 0 1px rgba(255, 255, 255, 0.03);
}

.quiz-card:hover .quiz-thumbnail-img {
  transform: scale(1.03);
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

.empty-state {
  background: rgba(244, 241, 236, 0.6);
  border-radius: 14px;
  padding: 2rem;
}
</style>
