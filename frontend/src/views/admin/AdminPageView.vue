<template>
  <div class="admin-container">
    <div class="container mx-auto px-4">
      <h1 class="page-title">관리자 대시보드</h1>

      <!-- Tabs -->
      <div class="tab-navbar">
        <button :class="['tab-button', { active: activeTab === 'quizzes' }]" @click="activeTab = 'quizzes'">
          퀴즈 관리
        </button>
        <button :class="['tab-button', { active: activeTab === 'challenges' }]" @click="activeTab = 'challenges'">
          챌린지 관리
        </button>
      </div>

      <!-- Quiz Management Tab -->
      <div v-show="activeTab === 'quizzes'" class="tab-content">
        <!-- Search Bar -->
        <div class="search-bar-wide w-full mb-6 flex justify-center">
          <span class="p-input-icon-right search-input-wrapper" style="max-width: 600px; width: 100%;">
            <InputText
              v-model="searchKeyword"
              placeholder="퀴즈 검색..."
              class="search-input w-full"
              @keyup.enter="loadQuizzes"
            />
            <i class="pi pi-search search-icon" @click="loadQuizzes"></i>
          </span>
        </div>

        <div v-if="loading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl" style="color: var(--primary-color)"></i>
        </div>

        <div v-else-if="quizzes.length === 0" class="text-center py-8 text-secondary">
          <p class="text-xl">퀴즈가 없습니다.</p>
        </div>

        <div v-else class="quiz-grid-container mb-6">
          <div v-for="quiz in quizzes" :key="quiz.id" class="quiz-card">
            <div class="quiz-thumbnail">
              <img :src="resolveImageUrl(quiz.thumbnailUrl) || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
              <div v-if="quiz.hidden" class="hidden-badge">숨김</div>
            </div>
            <div class="quiz-body">
              <div class="quiz-meta">
                  <h3 class="quiz-title">{{ quiz.title }}</h3>
                  <div class="quiz-stat">
                    <span>{{ quiz.author?.nickname || '알 수 없음' }}</span>
                  </div>
              </div>
              <div class="quiz-actions mt-auto">
                <Button
                  :label="quiz.hidden ? '보이기' : '숨기기'"
                  :icon="quiz.hidden ? 'pi pi-eye' : 'pi pi-eye-slash'"
                  :severity="quiz.hidden ? 'success' : 'warn'"
                  size="small"
                  outlined
                  class="w-full text-xs"
                  @click="handleToggleHide(quiz)"
                />
              </div>
            </div>
          </div>
        </div>
        
        <Paginator
            v-if="quizzes.length > 0"
            :first="(page - 1) * size"
            :rows="size"
            :totalRecords="totalRecords" 
            @page="onPageChange"
            template="PrevPageLink PageLinks NextPageLink"
            class="custom-paginator"
        />
      </div>

      <!-- Challenge Management Tab -->
      <div v-show="activeTab === 'challenges'" class="tab-content">
        <div class="challenge-form-card">
          <h2 class="form-title">새 챌린지 생성</h2>
          
          <div class="field mb-4">
            <label class="field-label">챌린지 유형</label>
            <div class="flex gap-4">
              <div class="flex align-items-center">
                <RadioButton v-model="challengeForm.challengeType" inputId="survival" name="type" value="SURVIVAL" />
                <label for="survival" class="ml-2 field-label-text">서바이벌</label>
              </div>
              <div class="flex align-items-center">
                <RadioButton v-model="challengeForm.challengeType" inputId="timeattack" name="type" value="TIME_ATTACK" />
                <label for="timeattack" class="ml-2 field-label-text">타임어택</label>
              </div>
            </div>
          </div>

          <div class="field mb-4">
            <label class="field-label">제목</label>
            <InputText v-model="challengeForm.title" class="w-full search-input" placeholder="챌린지 제목을 입력하세요" />
          </div>

          <div class="field mb-4">
            <label class="field-label">설명</label>
            <Textarea v-model="challengeForm.description" rows="3" class="w-full search-input" placeholder="챌린지 설명을 입력하세요" />
          </div>

          <!-- Quiz Picker -->
          <div class="field mb-4">
            <label class="field-label">대상 퀴즈</label>
            <div class="flex gap-2">
               <div class="flex-1 p-3 border rounded-lg bg-surface-card text-surface-900 border-surface-border flex items-center justify-between" style="background: var(--color-background-soft); border-color: var(--color-border);">
                 <span v-if="selectedQuiz" class="font-medium" style="color: var(--color-heading)">
                   {{ selectedQuiz.title }} (ID: {{ selectedQuiz.id }})
                 </span>
                 <span v-else class="text-secondary">선택된 퀴즈 없음</span>
                 <Button v-if="selectedQuiz" icon="pi pi-times" text rounded size="small" @click="clearSelectedQuiz" />
               </div>
               <Button label="퀴즈 선택" icon="pi pi-search" @click="openQuizPicker" outlined />
            </div>
            <input type="hidden" :value="challengeForm.targetQuizId" />
          </div>

          <div v-if="challengeForm.challengeType === 'TIME_ATTACK'" class="field mb-4">
            <label class="field-label">제한 시간 (초)</label>
            <InputNumber v-model="challengeForm.timeLimit" class="w-full" placeholder="예: 60" />
          </div>

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div class="field">
              <label class="field-label">시작 일시</label>
              <Calendar v-model="challengeForm.startAt" showTime hourFormat="24" class="w-full" showIcon />
            </div>
            <div class="field">
              <label class="field-label">종료 일시</label>
              <Calendar v-model="challengeForm.endAt" showTime hourFormat="24" class="w-full" showIcon />
            </div>
          </div>

          <Button label="챌린지 생성" class="w-full" icon="pi pi-check" :loading="submitting" @click="submitChallenge" />
        </div>
      </div>
    </div>

    <!-- Quiz Picker Dialog -->
    <Dialog v-model:visible="showQuizPicker" header="퀴즈 선택" :style="{ width: '50rem' }" :modal="true" :draggable="false">
      <div class="p-4">
         <div class="search-bar-wide w-full mb-4">
          <span class="p-input-icon-right search-input-wrapper w-full">
            <InputText
              v-model="pickerKeyword"
              placeholder="제목으로 검색..."
              class="search-input w-full"
              @keyup.enter="loadPickerQuizzes"
            />
            <i class="pi pi-search search-icon" @click="loadPickerQuizzes"></i>
          </span>
        </div>

        <div v-if="pickerLoading" class="text-center py-8">
           <i class="pi pi-spin pi-spinner text-3xl"></i>
        </div>
        <div v-else-if="pickerQuizzes.length === 0" class="text-center py-8">
           <p class="text-secondary">퀴즈가 없습니다.</p>
        </div>
        <div v-else class="quiz-list-compact">
           <div 
              v-for="quiz in pickerQuizzes" 
              :key="quiz.id" 
              class="quiz-list-item cursor-pointer hover:bg-surface-hover p-2 rounded-lg flex gap-3 items-center border-b border-surface-border"
              @click="selectQuiz(quiz)"
           >
              <img :src="resolveImageUrl(quiz.thumbnailUrl) || '/placeholder.svg'" class="w-16 h-16 object-cover rounded-md" />
              <div class="flex-1">
                 <h4 class="font-bold text-lg m-0" style="color: var(--color-heading)">{{ quiz.title }}</h4>
                 <span class="text-sm text-secondary">ID: {{ quiz.id }} | {{ quiz.author?.nickname }}</span>
              </div>
              <Button icon="pi pi-plus" text rounded />
           </div>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { getAdminQuizzes, toggleQuizVisibility, createChallenge } from '@/api/admin';
import { resolveImageUrl } from '@/lib/image';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Textarea from 'primevue/textarea';
import RadioButton from 'primevue/radiobutton';
import Calendar from 'primevue/calendar';
import Paginator from 'primevue/paginator';
import Dialog from 'primevue/dialog';
import { useToast } from 'primevue/usetoast';

const toast = useToast();
const activeTab = ref('quizzes');
const loading = ref(false);
const submitting = ref(false);

// Quiz Tab State
const quizzes = ref([]);
const searchKeyword = ref('');
const page = ref(1);
const size = ref(21);

// Quiz Picker State
const showQuizPicker = ref(false);
const pickerQuizzes = ref([]);
const pickerKeyword = ref('');
const pickerLoading = ref(false);
const selectedQuiz = ref(null);

// Challenge Tab State
const challengeForm = reactive({
  title: '',
  description: '',
  challengeType: 'SURVIVAL',
  targetQuizId: null,
  timeLimit: null,
  startAt: null,
  endAt: null
});

// --- Main Quiz List ---
const totalRecords = ref(0);

// --- Main Quiz List ---
const loadQuizzes = async () => {
  loading.value = true;
  try {
    const data = await getAdminQuizzes({
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value
    });
    // Backend now returns PageResponse { content, totalElements, ... }
    if (data && data.content) {
        quizzes.value = data.content;
        totalRecords.value = data.totalElements;
    } else {
        // Fallback or empty
        quizzes.value = [];
        totalRecords.value = 0;
    }
  } catch (error) {
    toast.add({ severity: 'error', summary: '오류', detail: '퀴즈 목록을 불러오지 못했습니다.', life: 3000 });
  } finally {
    loading.value = false;
  }
};

const handleToggleHide = async (quiz) => {
  try {
    await toggleQuizVisibility(quiz.id);
    quiz.hidden = !quiz.hidden;
    const msg = quiz.hidden ? '퀴즈가 숨김 처리되었습니다.' : '퀴즈 숨김이 해제되었습니다.';
    toast.add({ severity: 'success', summary: '성공', detail: msg, life: 3000 });
  } catch (error) {
    toast.add({ severity: 'error', summary: '오류', detail: '상태 변경에 실패했습니다.', life: 3000 });
  }
};

const onPageChange = (event) => {
  page.value = event.page + 1;
  loadQuizzes();
};

// --- Quiz Picker ---
const openQuizPicker = () => {
    showQuizPicker.value = true;
    if (pickerQuizzes.value.length === 0) {
        loadPickerQuizzes();
    }
};

const loadPickerQuizzes = async () => {
    pickerLoading.value = true;
    try {
         const data = await getAdminQuizzes({
            page: 1, 
            size: 50, // Load more for picker
            keyword: pickerKeyword.value
         });
         // Handle PageResponse structure for picker too if API returns it
         if (data && data.content) {
            pickerQuizzes.value = data.content;
         } else if (Array.isArray(data)) {
             pickerQuizzes.value = data;
         }
    } catch (error) {
        // silent or toast
    } finally {
        pickerLoading.value = false;
    }
};

const selectQuiz = (quiz) => {
    selectedQuiz.value = quiz;
    challengeForm.targetQuizId = quiz.id;
    showQuizPicker.value = false;
};

const clearSelectedQuiz = () => {
    selectedQuiz.value = null;
    challengeForm.targetQuizId = null;
};

// --- Challenge Submit ---
const submitChallenge = async () => {
  // Manual Validation
  if (!challengeForm.targetQuizId) {
      toast.add({ severity: 'warn', summary: '알림', detail: '대상 퀴즈를 선택해주세요.', life: 3000 });
      return;
  }
  if (!challengeForm.title) {
      toast.add({ severity: 'warn', summary: '알림', detail: '제목을 입력해주세요.', life: 3000 });
      return;
  }

  submitting.value = true;
  try {
    await createChallenge(challengeForm);
    toast.add({ severity: 'success', summary: '성공', detail: '챌린지가 성공적으로 생성되었습니다.', life: 3000 });
    // Reset form
    challengeForm.title = '';
    challengeForm.description = '';
    clearSelectedQuiz();
    challengeForm.timeLimit = null;
    challengeForm.startAt = null;
    challengeForm.endAt = null;
  } catch (error) {
    toast.add({ severity: 'error', summary: '오류', detail: '챌린지 생성에 실패했습니다.', life: 3000 });
  } finally {
    submitting.value = false;
  }
};


onMounted(() => {
  loadQuizzes();
});
</script>

<style scoped>
/* Global Layout */
.admin-container {
  min-height: calc(100vh - 200px);
  padding: 1rem 0 3rem;
  color: var(--color-text);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
    color: var(--color-heading);
    margin-bottom: 2rem;
    text-align: center;
}

/* Tabs */
.tab-navbar {
  display: flex;
  gap: 0;
  border-bottom: 2px solid var(--surface-border);
  margin-bottom: 2rem;
  /* justify-content: center; Removed to allow full width fill */
  background: var(--surface-ground);
}

.tab-button {
  flex: 1; 
  /* max-width: 200px; Removed to fill full width */
  padding: 1rem 1.5rem;
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-color-secondary);
  transition: all 0.3s ease;
  border-radius: 12px 12px 0 0;
}

/* ... existing hover/active styles ... */
.tab-button:hover {
    background: var(--surface-hover);
    color: var(--text-color);
}

.tab-button.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  font-weight: 600;
}

/* Search Bar positioning */
.search-input-wrapper {
    position: relative;
    display: block;
}

.search-icon {
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-color-secondary);
    cursor: pointer;
}

/* Tab Content Animation */
.tab-content {
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Quiz Grid (Matches MyPage) */
.quiz-grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); /* Keep responsive */
  gap: 1rem;
  padding: 0.5rem;
}

/* Quiz Card (Matches MyPage) */
.quiz-card {
  background: var(--color-background-soft);
  border-radius: 18px;
  border: 1px solid var(--color-border);
  padding: 0.6rem 0.6rem 0.3rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  box-shadow: var(--surface-glow);
  transition: transform 0.2s, box-shadow 0.2s;
  overflow: visible; /* Adjust if needed */
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
  position: relative;
}

.quiz-thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s ease;
}

.hidden-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 4px 8px;
    border-radius: 6px;
    font-size: 0.75rem;
    font-weight: bold;
    z-index: 2;
}

.quiz-body {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    padding: 0 0.4rem 0.4rem;
}

.quiz-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
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

.quiz-actions {
  display: flex;
  gap: 0.5rem;
}

/* Forms */
.challenge-form-card {
    background: var(--color-background-soft);
    border: 1px solid var(--color-border);
    border-radius: 18px;
    padding: 2rem;
    max-width: 800px;
    margin: 0 auto;
    box-shadow: var(--surface-glow);
}

.form-title {
    color: var(--color-heading);
    margin-bottom: 1.5rem;
    text-align: center;
}

.field {
  display: flex;
  flex-direction: column;
}

.field-label {
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: var(--color-heading);
}

.field-label-text {
    color: var(--color-text);
}

.text-secondary {
    color: var(--color-text-muted);
}

/* Input Styles */
.search-input {
    background: var(--color-background-soft);
    border-color: var(--color-border);
    color: var(--color-text);
}

/* Picker List */
.quiz-list-compact {
    max-height: 400px; 
    overflow-y: auto;
}
.quiz-list-item {
    transition: background-color 0.2s;
}
.quiz-list-item:hover {
    background-color: var(--color-background-mute);
}
</style>
