<template>
  <div class="profile-edit-container">
    <div class="container mx-auto px-4 max-w-2xl">
      <Card>
        <template #header>
          <div class="p-4 border-bottom-1 surface-border">
            <h1 class="text-3xl font-bold m-0">프로필 수정</h1>
          </div>
        </template>
        <template #content>
          <div class="flex flex-col gap-6">
            <div class="flex flex-col gap-4">
              <h2 class="text-2xl font-bold">프로필</h2>

              <div class="flex flex-col gap-2 items-center my-8">
                <div class="relative inline-block">
                  <Avatar
                    :image="previewUrl"
                    :label="!previewUrl && profileForm.nickname ? profileForm.nickname.charAt(0).toUpperCase() : ''"
                    :icon="!previewUrl && !profileForm.nickname ? 'pi pi-user' : ''"
                    class="font-bold border-4 border-gray-200 surface-200"
                    shape="circle"
                    style="width: 300px; height: 300px; font-size: 100px"
                  />

                  <div class="absolute bottom-4 right-4">
                    <FileUpload
                      mode="basic"
                      name="file"
                      accept="image/*"
                      :maxFileSize="5000000"
                      @select="onFileSelect"
                      :auto="false"
                      chooseIcon="pi pi-camera"
                      chooseLabel=" "
                      class="custom-upload p-button-rounded p-button-lg shadow-4"
                      style="width: 4rem; height: 4rem"
                    />
                  </div>
                </div>
                <small class="text-gray-500 mt-2">Click camera icon to change image</small>
              </div>

              <!-- 닉네임 변경 부분 수정 -->
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">닉네임 변경하기</label>
                <div class="flex gap-2">
                  <InputText v-model="profileForm.nickname" placeholder="닉네임을 써주세요" class="flex-1" :class="{ 'p-invalid': nicknameState.error }" maxlength="10" @input="handleNicknameChange" />
                  <!-- 본인 닉네임일 경우 버튼 비활성화 -->
                  <Button
                    label="중복 확인"
                    icon="pi pi-check-circle"
                    severity="secondary"
                    :loading="nicknameState.isChecking"
                    :disabled="!profileForm.nickname || profileForm.nickname === originalNickname || nicknameState.isChecked"
                    @click="handleCheckNickname"
                  />
                </div>

                <!-- 상태 메시지 표시 -->
                <small v-if="nicknameState.error" class="text-red-500">{{ nicknameState.error }}</small>
                <small v-else-if="profileForm.nickname !== originalNickname && nicknameState.isChecked" class="text-green-500">사용 가능한 닉네임입니다.</small>
                <small v-else class="text-gray-500">닉네임 변경 시 중복 확인이 필요합니다.</small>
              </div>

              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">상태 메시지</label>
                <Textarea v-model="profileForm.statusMessage" placeholder="내 상태 기입하기" rows="3" class="w-full" />
              </div>

              <Button label="Save Profile" icon="pi pi-check" :loading="savingProfile" @click="handleSaveProfile" class="mt-2" />
            </div>

            <Divider />
            <div class="flex flex-col gap-4">
              <h2 class="text-2xl font-bold">비밀번호 변경</h2>
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">현재 비밀번호</label>
                <Password v-model="passwordForm.currentPassword" toggleMask placeholder="Enter current password" class="w-full" />
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">새 비밀번호</label>
                <Password v-model="passwordForm.newPassword" toggleMask placeholder="Enter new password" class="w-full" />
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">비밀번호 다시 입력</label>
                <Password v-model="passwordForm.confirmPassword" toggleMask placeholder="Confirm new password" class="w-full" />
              </div>
              <Button label="Change Password" icon="pi pi-key" :loading="changingPassword" @click="handleChangePassword" class="mt-2" />
            </div>

            <Divider />

            <div class="flex flex-col gap-4">
              <h2 class="text-2xl font-bold text-red-500">Danger Zone</h2>
              <div class="p-4 border-round bg-red-50 border-1 border-red-200">
                <p class="font-semibold mb-2">Delete Account</p>
                <p class="text-sm text-color-secondary mb-4">Once you delete your account, there is no going back.</p>
                <Button label="Delete Account" icon="pi pi-trash" severity="danger" outlined @click="handleDeleteAccount" />
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { useAuthStore } from "@/stores/auth";
// checkNickname 추가
import { updateMyProfile, changePassword, deleteAccount, getMe, checkNickname } from "@/api/user";

const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();

const profileForm = ref({
  nickname: "",
  statusMessage: "",
});

//  원래 닉네임 보관용 (변경 여부 판단)
const originalNickname = ref("");

//  닉네임 중복 확인 상태 관리
const nicknameState = ref({
  isChecked: true, // 초기값은 본인 닉네임이므로 true
  isChecking: false,
  error: "",
});

const selectedFile = ref<File | null>(null);
const previewUrl = ref("");

const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const savingProfile = ref(false);
const changingPassword = ref(false);

const loadProfile = async () => {
  try {
    const user = await getMe();
    profileForm.value.nickname = user.nickname || "";
    profileForm.value.statusMessage = user.statusMessage || "";

    //  원래 닉네임 저장 및 상태 초기화
    originalNickname.value = user.nickname || "";
    nicknameState.value.isChecked = true;
    nicknameState.value.error = "";

    // 서버 이미지 주소 설정
    if (user.profileImageUrl) {
      previewUrl.value = `http://localhost:8080${user.profileImageUrl}`;
    } else {
      previewUrl.value = "";
    }
  } catch (error: any) {
    console.error(error);
  }
};

const onFileSelect = (event: any) => {
  const file = event.files[0];
  if (file) {
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
};

//  닉네임 입력 핸들러
const handleNicknameChange = () => {
  nicknameState.value.error = "";

  // 원래 닉네임과 같으면 중복확인 필요 없음
  if (profileForm.value.nickname === originalNickname.value) {
    nicknameState.value.isChecked = true;
  } else {
    // 닉네임이 변경되었으면 중복확인 상태 false
    nicknameState.value.isChecked = false;
  }
};

//  닉네임 중복 확인 핸들러
const handleCheckNickname = async () => {
  const nickname = profileForm.value.nickname;

  if (!nickname || nickname.length < 2) {
    nicknameState.value.error = "닉네임은 2자 이상이어야 합니다.";
    return;
  }

  nicknameState.value.isChecking = true;
  nicknameState.value.error = "";

  try {
    await checkNickname(nickname);
    // 에러가 안 나면 성공
    nicknameState.value.isChecked = true;
    toast.add({ severity: "success", summary: "확인 완료", detail: "사용 가능한 닉네임입니다.", life: 3000 });
  } catch (error: any) {
    nicknameState.value.isChecked = false;
    // 백엔드 에러 메시지 표시
    const msg = error.response?.data?.message || "이미 사용 중인 닉네임입니다.";
    nicknameState.value.error = msg;
    toast.add({ severity: "error", summary: "중복", detail: msg, life: 3000 });
  } finally {
    nicknameState.value.isChecking = false;
  }
};

const handleSaveProfile = async () => {
  // [추가] 저장 전 닉네임 검증 확인
  if (!nicknameState.value.isChecked) {
    toast.add({ severity: "warn", summary: "확인 필요", detail: "닉네임 중복 확인을 해주세요.", life: 3000 });
    return;
  }

  if (nicknameState.value.error) {
    toast.add({ severity: "warn", summary: "확인 필요", detail: "닉네임을 확인해주세요.", life: 3000 });
    return;
  }

  savingProfile.value = true;
  try {
    const formData = new FormData();
    formData.append("data", new Blob([JSON.stringify(profileForm.value)], { type: "application/json" }));

    if (selectedFile.value) {
      formData.append("file", selectedFile.value);
    }

    await updateMyProfile(formData);
    await authStore.refreshUser(); // 변경된 정보(특히 닉네임, 프사) 갱신

    //  저장 성공 후 현재 상태를 '원본'으로 갱신
    originalNickname.value = profileForm.value.nickname;
    nicknameState.value.isChecked = true;

    toast.add({ severity: "success", summary: "성공", detail: "프로필 업데이트 성공", life: 3000 });
    selectedFile.value = null;
  } catch (error: any) {
    toast.add({ severity: "error", summary: "Error", detail: "Failed update", life: 3000 });
  } finally {
    savingProfile.value = false;
  }
};

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "Passwords do not match",
      life: 3000,
    });
    return;
  }

  if (passwordForm.value.newPassword.length < 8) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "Password must be at least 8 characters",
      life: 3000,
    });
    return;
  }

  changingPassword.value = true;
  try {
    await changePassword(passwordForm.value.currentPassword, passwordForm.value.newPassword);
    passwordForm.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
    toast.add({
      severity: "success",
      summary: "Success",
      detail: "Password changed successfully",
      life: 3000,
    });
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: error.response?.data?.message || "Failed to change password",
      life: 3000,
    });
  } finally {
    changingPassword.value = false;
  }
};

const handleDeleteAccount = () => {
  confirm.require({
    message: "Are you absolutely sure? This action cannot be undone.",
    header: "Delete Account",
    icon: "pi pi-exclamation-triangle",
    acceptClass: "p-button-danger",
    accept: async () => {
      const password = prompt("Please enter your password to confirm:");
      if (!password) return;

      try {
        await deleteAccount(password);
        toast.add({
          severity: "success",
          summary: "Success",
          detail: "Account deleted successfully",
          life: 3000,
        });
        authStore.logoutUser();
        router.push("/");
      } catch (error: any) {
        toast.add({
          severity: "error",
          summary: "Error",
          detail: error.response?.data?.message || "Failed to delete account",
          life: 3000,
        });
      }
    },
  });
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-edit-container {
  min-height: calc(100vh - 200px);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
}

:deep(.custom-upload .p-button-label) {
  display: none;
}

:deep(.custom-upload input[type="file"]) {
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  position: absolute !important;
  z-index: -1 !important;
  overflow: hidden !important;
}

/* 카메라 버튼 스타일 (둥글게, 중앙 정렬) */
:deep(.p-fileupload-choose) {
  border-radius: 50%;
  padding: 0 !important;
  display: flex !important;
  align-items: center;
  justify-content: center;
}

/* Fallback: hide any plain span or label siblings that display the filename/text
   (covers browser default "No file chosen" rendering and PrimeVue variants) */
:deep(.p-fileupload) span {
  display: none !important;
}
</style>
