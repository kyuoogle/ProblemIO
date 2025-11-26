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
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">닉네임 변경하기</label>
                <InputText v-model="profileForm.nickname" placeholder="닉네임을 써주세요" class="w-full" />
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
import { updateMyProfile, changePassword, deleteAccount, getMe } from "@/api/user";

const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();

const profileForm = ref({
  nickname: "",
  statusMessage: "",
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

    // 서버 이미지 주소 설정 (도메인 수정 필요)
    if (user.profileImageUrl) {
      previewUrl.value = `http://localhost:8080${user.profileImageUrl}`;
    } else {
      previewUrl.value = "";
    }
  } catch (error: any) {
    console.error(error);
  }
};

// [로직] 파일 선택 시 즉시 이미지 교체
const onFileSelect = (event: any) => {
  const file = event.files[0];
  if (file) {
    selectedFile.value = file;
    // URL.createObjectURL을 사용하여 즉시 미리보기
    previewUrl.value = URL.createObjectURL(file);
  }
};

const handleSaveProfile = async () => {
  savingProfile.value = true;
  try {
    const formData = new FormData();
    formData.append("data", new Blob([JSON.stringify(profileForm.value)], { type: "application/json" }));

    if (selectedFile.value) {
      formData.append("file", selectedFile.value);
    }

    await updateMyProfile(formData);
    await authStore.refreshUser();

    toast.add({ severity: "success", summary: "Success", detail: "Profile updated", life: 3000 });
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
  opacity: 0 !important; /* 투명하게 만듦 */
  width: 0 !important; /* 너비를 0으로 만듦 */
  height: 0 !important; /* 높이를 0으로 만듦 */
  position: absolute !important; /* 다른 요소에 영향을 주지 않도록 위치 설정 */
  z-index: -1 !important; /* 화면에서 가장 뒤로 보냄 */
  overflow: hidden !important; /* 넘치는 내용 숨김 */
}
</style>
