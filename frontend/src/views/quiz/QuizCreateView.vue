<template>
  <div class="quiz-create-page">
    <!-- 상단 퀴즈 메타 + 저장 버튼 -->
    <div class="top-bar">
      <Button
        label="퀴즈 기본 정보"
        icon="pi pi-sliders-h"
        class="mr-2"
        @click="metaDialogVisible = true"
      />
      <div class="flex-1" />
      <Button
        label="취소"
        severity="secondary"
        outlined
        class="mr-2"
        @click="handleCancel"
      />
      <Button
        label="저장"
        icon="pi pi-check"
        :loading="submitting"
        :disabled="
          !isFormValid ||
          thumbnailUploading ||
          questionUploadingIndex !== null
        "
        @click="handleSubmit"
      />
    </div>

    <!-- 문제 카드 리스트 -->
    <div class="question-list-wrapper">
      <div class="question-list">
        <!-- 문제 카드들 -->
        <div
          v-for="(question, index) in quizForm.questions"
          :key="index"
          class="question-card"
          @click="openQuestionDialog(index)"
        >
          <div class="card-image-wrapper">
            <img
              v-if="question.imageUrl"
              :src="getImagePreview(question.imageUrl)"
              alt="question image"
            />
            <div v-else class="no-image">이미지를 설정해주세요</div>

            <!-- 설명 + 정답이 모두 세팅 안 된 경우 느낌표 표시 -->
            <span
              v-if="!isQuestionConfigured(question)"
              class="badge-warning"
            >
              !
            </span>
          </div>
          <div class="card-footer">
            <span class="card-title">
              {{ question.description || `Question ${question.questionOrder}` }}
            </span>
            <Button
              icon="pi pi-trash"
              severity="danger"
              text
              rounded
              @click.stop="removeQuestion(index)"
            />
          </div>
        </div>

        <!-- + 문제 추가 카드 -->
        <div class="question-card add-card" @click="handleAddQuestionClick">
          <div class="add-card-inner">
            <span class="plus-icon">+</span>
          </div>
        </div>
      </div>

      <!-- 문제 하나도 없을 때 안내 문구 -->
      <div
        v-if="quizForm.questions.length === 0"
        class="empty-helper text-color-secondary"
      >
        아직 등록된 문제가 없습니다. 오른쪽 <strong>+</strong> 카드를 눌러
        첫 문제를 추가해보세요.
      </div>
    </div>

    <!-- 숨겨진 파일 인풋: 문제 이미지 업로드용 -->
    <input
      ref="questionImageInputRef"
      type="file"
      accept="image/*"
      class="hidden-file-input"
      @change="handleQuestionImageChange"
    />

    <!-- 숨겨진 파일 인풋: 썸네일 업로드용 -->
    <input
      ref="thumbnailInputRef"
      type="file"
      accept="image/*"
      class="hidden-file-input"
      @change="handleThumbnailChange"
    />

    <!-- 퀴즈 메타 정보 모달 -->
    <Dialog
      v-model:visible="metaDialogVisible"
      header="퀴즈 만들기"
      :modal="true"
      :style="{ width: '480px' }"
    >
      <div class="meta-form">
        <div class="field">
          <label class="field-label">제목 *</label>
          <InputText
            v-model="quizForm.title"
            placeholder="퀴즈 제목을 입력하세요"
            class="w-full"
          />
        </div>

        <div class="field">
          <label class="field-label">설명</label>
          <Textarea
            v-model="quizForm.description"
            rows="3"
            placeholder="퀴즈에 대한 설명을 입력하세요"
            class="w-full"
          />
        </div>

        <div class="field">
          <label class="field-label">썸네일 (선택)</label>
          <div class="thumbnail-box" @click="triggerThumbnailSelect">
            <img
              v-if="thumbnailPreview"
              :src="thumbnailPreview"
              alt="thumbnail"
            />
            <div v-else class="thumbnail-placeholder">
              <span class="plus-icon">+</span>
              <span>썸네일을 추가해보세요. (설정하지 않으면 첫 문제 이미지)</span>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-2 mt-4">
          <Button label="닫기" text @click="metaDialogVisible = false" />
          <Button label="확인" @click="metaDialogVisible = false" />
        </div>
      </div>
    </Dialog>

    <!-- 문제 편집 모달 -->
    <Dialog
      v-model:visible="questionDialogVisible"
      header="문제 편집"
      :modal="true"
      :style="{ width: '720px' }"
    >
      <div v-if="editingQuestionIndex !== null" class="question-edit">
        <div class="question-image-preview">
          <img
            v-if="currentQuestion.imageUrl"
            :src="getImagePreview(currentQuestion.imageUrl)"
            alt="question"
          />
          <div v-else class="no-image large">
            아직 이미지가 없습니다.
            <Button
              label="이미지 선택"
              size="small"
              class="mt-2"
              @click="triggerQuestionImageSelect"
            />
          </div>
        </div>

        <div class="field mt-3">
          <label class="field-label">질문 설명</label>
          <Textarea
            v-model="questionForm.description"
            rows="2"
            placeholder="이 이미지에 대한 설명(힌트)을 입력하세요."
            class="w-full"
          />
        </div>

        <div class="field">
          <label class="field-label">정답 *</label>
          <small class="text-color-secondary mb-2 block"
            >여러 개의 정답(동의어, 변형)을 입력할 수 있습니다. 엔터를
            눌러 추가하세요.</small
          >
          <Chips
            v-model="questionForm.answers"
            placeholder="정답을 입력하고 Enter"
            class="w-full"
          />
        </div>

        <div class="flex justify-end gap-2 mt-4">
          <Button label="취소" text @click="closeQuestionDialog" />
          <Button label="확인" @click="confirmQuestionDialog" />
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useQuizStore } from "@/stores/quiz";
import { createQuiz } from "@/api/quiz";
import { uploadFile } from "@/api/file";
import { resolveImageUrl } from "@/lib/image";

const router = useRouter();
const toast = useToast();
const quizStore = useQuizStore();

const submitting = ref(false);
const thumbnailUploading = ref(false);
// 업로드 중인 문제 인덱스 (null이면 업로드 없음)
const questionUploadingIndex = ref<number | null>(null);

// 퀴즈 메타/문제 편집 모달
const metaDialogVisible = ref(true);
const questionDialogVisible = ref(false);
const editingQuestionIndex = ref<number | null>(null);

// file input refs
const questionImageInputRef = ref<HTMLInputElement | null>(null);
const thumbnailInputRef = ref<HTMLInputElement | null>(null);

const quizForm = computed(() => quizStore.quizForm);
const buildImageUrl = (path?: string) => resolveImageUrl(path);
const thumbnailPreview = computed(() =>
  buildImageUrl(quizForm.value.thumbnailUrl)
);

// 모달 내에서 쓰는 questionForm
const questionForm = reactive({
  description: "",
  answers: [] as string[],
});

// 현재 편집 중인 문제 객체
const currentQuestion = computed(() => {
  if (editingQuestionIndex.value === null) return {} as any;
  return quizForm.value.questions[editingQuestionIndex.value];
});

// 폼 유효성 검사
const isFormValid = computed(() => {
  if (!quizForm.value.title?.trim()) return false;
  if (quizForm.value.questions.length === 0) return false;

  return quizForm.value.questions.every((q) => {
    return q.answers && q.answers.length > 0;
  });
});

// 문제가 설정 완료됐는지 여부 (설명 + 정답)
const isQuestionConfigured = (q: any) => {
  const hasDesc = !!q.description?.trim();
  const hasAnswers = q.answers && q.answers.length > 0;
  return hasDesc && hasAnswers;
};

// ==== 퀴즈 메타 / 썸네일 ====
const triggerThumbnailSelect = () => {
  thumbnailInputRef.value?.click();
};

const handleThumbnailChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  thumbnailUploading.value = true;
  try {
    const { url } = await uploadFile(file, "thumbnail");
    quizForm.value.thumbnailUrl = url;
    toast.add({
      severity: "success",
      summary: "Upload success",
      detail: "썸네일 이미지가 설정되었습니다.",
      life: 2000,
    });
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Upload failed",
      detail: error.response?.data?.message || "이미지 업로드에 실패했습니다.",
      life: 3000,
    });
  } finally {
    thumbnailUploading.value = false;
    target.value = "";
  }
};

// ==== 문제 카드/이미지/모달 ====
const handleAddQuestionClick = () => {
  // 새 질문 추가 (questionOrder 등은 스토어에서 처리한다고 가정)
  quizStore.addQuestion();
  const index = quizForm.value.questions.length - 1;
  editingQuestionIndex.value = index;

  // 플로우: 먼저 이미지 선택 → 업로드 후 모달에서 나머지 정보 입력
  triggerQuestionImageSelect();
};

const triggerQuestionImageSelect = () => {
  questionImageInputRef.value?.click();
};

const handleQuestionImageChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;
  if (editingQuestionIndex.value === null) return;

  const index = editingQuestionIndex.value;
  questionUploadingIndex.value = index;

  try {
    const { url } = await uploadFile(file, "question");
    quizForm.value.questions[index].imageUrl = url;
    toast.add({
      severity: "success",
      summary: "Upload success",
      detail: `Question ${quizForm.value.questions[index].questionOrder} 이미지가 설정되었습니다.`,
      life: 2000,
    });

    // 이미지 업로드가 끝나면 문제 편집 모달 오픈
    openQuestionDialog(index);
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Upload failed",
      detail: error.response?.data?.message || "이미지 업로드에 실패했습니다.",
      life: 3000,
    });
  } finally {
    questionUploadingIndex.value = null;
    target.value = "";
  }
};

const openQuestionDialog = (index: number) => {
  editingQuestionIndex.value = index;
  const q = quizForm.value.questions[index];

  questionForm.description = q.description || "";
  questionForm.answers = q.answers ? [...q.answers] : [];

  questionDialogVisible.value = true;
};

const closeQuestionDialog = () => {
  questionDialogVisible.value = false;
  editingQuestionIndex.value = null;
};

const confirmQuestionDialog = () => {
  if (editingQuestionIndex.value === null) return;
  const idx = editingQuestionIndex.value;
  const q = quizForm.value.questions[idx];

  q.description = questionForm.description;
  q.answers = [...questionForm.answers];

  questionDialogVisible.value = false;
};

const removeQuestion = (index: number) => {
  quizStore.removeQuestion(index);
};

const getImagePreview = (path?: string) => buildImageUrl(path);

// ==== 취소/저장 ====
const handleCancel = () => {
  if (confirm("작성 중인 내용이 모두 사라집니다. 취소하시겠습니까?")) {
    quizStore.resetQuizForm();
    router.push("/");
  }
};

const handleSubmit = async () => {
  if (thumbnailUploading.value || questionUploadingIndex.value !== null) {
    toast.add({
      severity: "warn",
      summary: "Uploading images",
      detail: "이미지 업로드가 완료될 때까지 기다려주세요.",
      life: 2500,
    });
    return;
  }

  if (!isFormValid.value) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "필수 항목을 모두 입력해주세요.",
      life: 3000,
    });
    return;
  }

  submitting.value = true;
  try {
    const result = await createQuiz(quizForm.value);
    quizStore.resetQuizForm();
    toast.add({
      severity: "success",
      summary: "Success",
      detail: "퀴즈가 생성되었습니다.",
      life: 3000,
    });
    router.push(`/quiz/${result.id}`);
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: error.response?.data?.message || "퀴즈 생성에 실패했습니다.",
      life: 3000,
    });
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.quiz-create-page {
  min-height: calc(100vh - 120px);
  padding: 1.5rem 2rem;
  background: #ffffff;
  color: #000;
}

/* 상단 바 */
.top-bar {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

/* 문제 카드 리스트 */
.question-list-wrapper {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  padding: 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

/* 문제 카드 그리드 */
.question-list {
  display: grid;
  /* 한 카드 최소 260px, 최대 320px 정도로 제한하고 자동 줄바꿈 */
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

/* 반응형 - 필요하면 메인페이지랑 똑같이 */
@media (max-width: 768px) {
  .question-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .question-list {
    grid-template-columns: 1fr;
  }
}

.question-card {
  /* width: 180px;  ← 이런 거 있으면 삭제 */
  background: #f8f9fb;
  border-radius: 18px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 12px rgba(0,0,0,0.10);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #333;
  transition: 0.15s ease;
  padding: 0.6rem 0.6rem 0.3rem;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;    /* ✅ 정사각형 */
  overflow: hidden;
  border-radius: 14px;
  background: repeating-linear-gradient(
    45deg,
    #f3f4f6,
    #f3f4f6 10px,
    #e5e7eb 10px,
    #e5e7eb 20px
  );
}

.card-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 이미지 없음 표시 */
.no-image {
  width: 100%;
  height: 100%;
  font-size: 0.85rem;
  color: #777;
  display: flex;
  align-items: center;
  justify-content: center;
  background: repeating-linear-gradient(
    45deg,
    #f3f4f6,
    #f3f4f6 10px,
    #e5e7eb 10px,
    #e5e7eb 20px
  );
}

.no-image.large {
  height: 220px;
  flex-direction: column;
}

.badge-warning {
  position: absolute;
  top: 6px;
  left: 6px;
  width: 20px;
  height: 20px;
  background: #ff4d4f;
  color: #fff;
  border-radius: 50%;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-footer {
  padding: 0.5rem 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 0.85rem;
  font-weight: 600;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-card {
  background: #f3f4f6;
  border: 2px dashed #d1d5db;
  color: #6b7280;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.add-card-inner {
  width: 100%;
  aspect-ratio: 1 / 1;   /* 정사각형 영역 안에 + 표시 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.plus-icon {
  font-size: 3rem;
  font-weight: 300;
}

.empty-helper {
  margin-top: 1rem;
  font-size: 0.9rem;
}

/* 숨겨진 input[type=file] */
.hidden-file-input {
  display: none;
}

/* 메타 모달 */
.meta-form .field {
  margin-bottom: 1rem;
}

.field-label {
  display: block;
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.thumbnail-box {
  border: 2px dashed #7c4dff;
  border-radius: 16px;
  padding: 0.75rem;
  cursor: pointer;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(124, 77, 255, 0.05);
}

.thumbnail-box img {
  max-width: 100%;
  max-height: 160px;
  border-radius: 12px;
}

.thumbnail-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #7c4dff;
}

/* 문제 편집 모달 */
.question-image-preview img {
  width: 100%;
  max-height: 260px;
  object-fit: contain;
}

.answer-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>
