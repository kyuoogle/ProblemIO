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
          <div class="flex flex-col gap-8">
            
            <!-- SECTION 1: 닉네임 변경 & 상태메시지 수정 -->
            <section class="flex flex-col gap-6">
              <h2 class="text-xl font-bold border-l-4 border-primary pl-3">내 정보 수정</h2>
              
              <div class="flex flex-col gap-2 items-center my-8">
                <div class="relative inline-block">
                   <UserAvatar 
                   :user="previewUser"
                   class="font-bold surface-200" 
                   style="width: 300px; height: 300px; font-size: 100px"/>

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
                <small class="text-gray-500 mt-2">파란 버튼을 눌러 이미지를 변경하세요</small>
              </div>

              <div class="grid gap-4">
                <!-- 닉네임 -->
                <div class="flex flex-col gap-2">
                  <label class="text-sm font-medium">닉네임</label>
                  <div class="flex gap-2">
                    <InputText 
                      v-model="profileForm.nickname" 
                      placeholder="닉네임 입력" 
                      class="flex-1" 
                      :class="{ 'p-invalid': nicknameState.error }" 
                      maxlength="10" 
                      @input="handleNicknameChange" 
                    />
                    <Button
                      label="중복 확인"
                      icon="pi pi-check-circle"
                      severity="secondary"
                      :loading="nicknameState.isChecking"
                      :disabled="!profileForm.nickname || profileForm.nickname === originalNickname || nicknameState.isChecked"
                      @click="handleCheckNickname"
                    />
                  </div>
                  <small v-if="nicknameState.error" class="text-red-500">{{ nicknameState.error }}</small>
                  <small v-else-if="profileForm.nickname !== originalNickname && nicknameState.isChecked" class="text-green-500">사용 가능한 닉네임입니다.</small>
                </div>

                <!-- 상태 메시지 -->
                <div class="flex flex-col gap-2">
                  <label class="text-sm font-medium">상태 메시지(20자 제한)</label>
                  <Textarea v-model="profileForm.statusMessage" placeholder="나를 표현하는 한마디" rows="3" class="w-full" maxlength="20" />
                </div>
              </div>
            </section>

            <Divider />

            <!-- SECTION 2: 꾸미기 -->
            <section class="flex flex-col gap-6">
              <h2 class="text-xl font-bold border-l-4 border-primary pl-3">꾸미기</h2>
              
              <!-- 배경 테마 -->
              <div class="flex flex-col gap-3">
                <label class="text-sm font-medium">프로필 배경 테마</label>
                <div class="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                  <div 
                    v-for="theme in themes" 
                    :key="theme.key"
                    class="cursor-pointer border-2 rounded-lg p-1 min-w-[100px] h-[70px] relative transition-all hover:shadow-lg"
                    :class="{'border-primary ring-2 ring-primary/20': profileForm.profileTheme === theme.key, 'border-transparent hover:border-gray-300': profileForm.profileTheme !== theme.key}"
                    @click="profileForm.profileTheme = theme.key"
                  >
                    <div v-if="theme.image" class="w-full h-full">
                        <img :src="resolveImageUrl(theme.image)" class="w-full h-full object-cover rounded" />
                    </div>
                    <div v-else class="w-full h-full rounded shadow-sm" :class="theme.class" :style="theme.style"></div>
                    <span class="absolute bottom-0 left-0 w-full text-[10px] text-center bg-gray-200/80 text-gray-900 truncate px-1 rounded-b">
                        {{ theme.name }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 아바타 프레임 -->
              <div class="flex flex-col gap-3">
                <label class="text-sm font-medium">아바타 프레임</label>
                <div class="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                  <div 
                    v-for="deco in avatars" 
                    :key="deco.key"
                    class="cursor-pointer border-2 rounded-full p-1 min-w-[70px] h-[70px] relative transition-all hover:scale-105"
                    :class="{'border-primary ring-2 ring-primary/20': profileForm.avatarDecoration === deco.key, 'border-transparent hover:border-gray-300': profileForm.avatarDecoration !== deco.key}"
                    @click="profileForm.avatarDecoration = deco.key"
                  >
                     <div class="w-full h-full bg-gray-100 rounded-full flex items-center justify-center">
                       <i class="pi pi-user text-gray-300 text-xl"></i>
                     </div>
                     <img v-if="deco.image" :src="resolveImageUrl(deco.image)" class="absolute inset-0 w-full h-full object-contain" />
                     <span class="absolute -bottom-2 left-1/2 -translate-x-1/2 text-[10px] bg-gray-200/80 text-gray-900 rounded-full px-2 py-0.5 whitespace-nowrap z-10">
                        {{ deco.name }}
                     </span>
                  </div>
                </div>
              </div>

              <!-- 팝오버 꾸미기 -->
              <div class="flex flex-col gap-3">
                <label class="text-sm font-medium">팝오버 테마</label>
                <div class="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                  <div 
                    v-for="pop in popovers" 
                    :key="pop.key"
                    class="cursor-pointer border-2 rounded-lg p-1 min-w-[100px] h-[70px] relative transition-all hover:shadow-lg"
                    :class="{'border-primary ring-2 ring-primary/20': profileForm.popoverDecoration === pop.key, 'border-transparent hover:border-gray-300': profileForm.popoverDecoration !== pop.key}"
                    @click="profileForm.popoverDecoration = pop.key"
                  >
                    <div v-if="pop.image" class="w-full h-full">
                        <img :src="resolveImageUrl(pop.image)" class="w-full h-full object-cover rounded" />
                    </div>
                    <div v-else class="w-full h-full rounded shadow-sm" :style="pop.style"></div>
                    <span class="absolute bottom-0 left-0 w-full text-[10px] text-center bg-gray-200/80 text-gray-900 truncate px-1 rounded-b">
                        {{ pop.name }}
                     </span>
                  </div>
                </div>
              </div>
            </section>

            <!-- 저장 버튼 -->
            <div class="flex justify-end pt-4">
               <Button label="저장하기" icon="pi pi-check" :loading="savingProfile" @click="handleSaveProfile" class="w-full md:w-auto px-6" />
            </div>

            <Divider />

            <!-- SECTION 3: 비밀번호 변경 -->
            <section class="flex items-center justify-between p-4 rounded-lg action-card">
              <div class="flex flex-col">
                <h3 class="text-lg font-bold">비밀번호 변경</h3>
                <span class="text-sm muted-text">주기적인 비밀번호 변경으로 계정을 보호하세요.</span>
              </div>
              <Button label="변경하기" icon="pi pi-key" severity="help" outlined @click="showPasswordDialog = true" />
            </section>

            <!-- SECTION 4: 회원탈퇴 -->
            <section class="flex items-center justify-between p-4 rounded-lg danger-card mt-2">
              <div class="flex flex-col">
                <h3 class="text-lg font-bold">회원 탈퇴</h3>
                <span class="text-sm danger-muted">탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.</span>
              </div>
              <Button label="탈퇴하기" icon="pi pi-user-minus" severity="danger" outlined @click="showDeleteDialog = true" />
            </section>

          </div>
        </template>
      </Card>
      
      <!-- 비밀번호 변경 모달 -->
      <Dialog v-model:visible="showPasswordDialog" modal header="비밀번호 변경" :style="{ width: '90vw', maxWidth: '400px' }">
        <div class="flex flex-col gap-4 pt-2">
          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium">현재 비밀번호</label>
            <Password v-model="passwordForm.currentPassword" toggleMask :feedback="false" placeholder="현재 비밀번호 입력" class="w-full" inputClass="w-full" />
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium">새 비밀번호</label>
            <Password v-model="passwordForm.newPassword" toggleMask placeholder="새 비밀번호 입력" class="w-full" inputClass="w-full">
                <template #footer>
                    <Divider />
                    <p class="mt-2">Suggestions</p>
                    <ul class="pl-2 ml-2 mt-0" style="line-height: 1.5">
                        <li>At least one lowercase</li>
                        <li>At least one uppercase</li>
                        <li>At least one numeric</li>
                        <li>Minimum 8 characters</li>
                    </ul>
                </template>
            </Password>
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium">새 비밀번호 확인</label>
            <Password v-model="passwordForm.confirmPassword" toggleMask :feedback="false" placeholder="새 비밀번호 다시 입력" class="w-full" inputClass="w-full" />
          </div>
        </div>
        <template #footer>
          <Button label="취소" icon="pi pi-times" text @click="showPasswordDialog = false" />
          <Button label="변경하기" icon="pi pi-check" :loading="changingPassword" @click="handleChangePassword" />
        </template>
      </Dialog>

      <!-- 회원 탈퇴 모달 -->
      <Dialog v-model:visible="showDeleteDialog" modal header="회원 탈퇴" :style="{ width: '90vw', maxWidth: '400px' }">
        <div class="flex flex-col gap-4 pt-2">
          <div class="bg-red-50 p-3 rounded text-red-700 text-sm">
            <i class="pi pi-exclamation-triangle mr-2"></i>
            정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.
          </div>
          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium">비밀번호 확인</label>
            <Password v-model="deletePassword" toggleMask :feedback="false" placeholder="비밀번호를 입력하세요" class="w-full" inputClass="w-full" />
          </div>
        </div>
        <template #footer>
          <Button label="취소" icon="pi pi-times" text @click="showDeleteDialog = false" />
          <Button label="탈퇴하기" icon="pi pi-trash" severity="danger" :loading="deletingAccount" @click="handleDeleteAccount" />
        </template>
      </Dialog>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useAuthStore } from "@/stores/auth";
import { updateMyProfile, changePassword, deleteAccount, getMe, checkNickname } from "@/api/user";
import UserAvatar from '@/components/common/UserAvatar.vue'
import { resolveImageUrl } from "@/lib/image";
import { PROFILE_THEMES } from "@/constants/themeConfig";
import { AVATAR_DECORATIONS } from "@/constants/avatarConfig";
import { POPOVER_DECORATIONS } from "@/constants/popoverConfig"; 

const router = useRouter();
const toast = useToast();
const authStore = useAuthStore();

// UI Control
const showPasswordDialog = ref(false);
const showDeleteDialog = ref(false);

const profileForm = ref({
  nickname: "",
  statusMessage: "",
  profileTheme: null,
  avatarDecoration: null,
  popoverDecoration: null,
});

const themes = ref([]);
const avatars = ref([]);
const popovers = ref([]);

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

// 비밀번호 변경 폼
const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 회원탈퇴 비밀번호
const deletePassword = ref("");

const savingProfile = ref(false);
const changingPassword = ref(false);
const deletingAccount = ref(false);

// 실시간 미리보기를 위한 computed 속성
const previewUser = computed(() => {
    return {
        ...authStore.user,
        ...profileForm.value,
        // 새로 선택한 프로필 이미지가 있다면 (previewUrl이 있다면) 그것을 우선 사용, 아니면 기존 이미지
        profileImageUrl: previewUrl.value || authStore.user?.profileImageUrl
    };
});

const loadProfile = async () => {
  try {
    const user = await getMe();
    profileForm.value.nickname = user.nickname || "";
    profileForm.value.statusMessage = user.statusMessage || "";
    profileForm.value.profileTheme = user.profileTheme || null;
    profileForm.value.avatarDecoration = user.avatarDecoration || null;
    profileForm.value.popoverDecoration = user.popoverDecoration || null;

    //  원래 닉네임 저장 및 상태 초기화
    originalNickname.value = user.nickname || "";
    nicknameState.value.isChecked = true;
    nicknameState.value.error = "";

    // 리소스 로드 (Config 파일 사용)
    themes.value = Object.keys(PROFILE_THEMES).map(key => ({
      key,
      ...PROFILE_THEMES[key]
    }));
    
    avatars.value = Object.keys(AVATAR_DECORATIONS).map(key => ({
      key,
      ...AVATAR_DECORATIONS[key]
    }));

    popovers.value = Object.keys(POPOVER_DECORATIONS).map(key => ({
      key,
      ...POPOVER_DECORATIONS[key]
    }));

    // 서버 이미지 주소 설정
    if (user.profileImageUrl) {
      previewUrl.value = resolveImageUrl(user.profileImageUrl);
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

    toast.add({ severity: "success", summary: "성공", detail: "프로필이 업데이트 되었습니다.", life: 3000 });
    selectedFile.value = null;
  } catch (error: any) {
    toast.add({ severity: "error", summary: "실패", detail: "업데이트에 실패했습니다.", life: 3000 });
  } finally {
    savingProfile.value = false;
  }
};

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "비밀번호가 일치하지 않습니다.",
      life: 3000,
    });
    return;
  }

  if (passwordForm.value.newPassword.length < 8) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "비밀번호는 최소 8자 이상이어야 합니다.",
      life: 3000,
    });
    return;
  }

  changingPassword.value = true;
  try {
    await changePassword(passwordForm.value.currentPassword, passwordForm.value.newPassword);
    
    // 성공 시 폼 초기화 및 닫기
    passwordForm.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
    showPasswordDialog.value = false;
    
    toast.add({
      severity: "success",
      summary: "Success",
      detail: "비밀번호가 변경되었습니다.",
      life: 3000,
    });
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: error.response?.data?.message || "비밀번호 변경에 실패했습니다.",
      life: 3000,
    });
  } finally {
    changingPassword.value = false;
  }
};

const handleDeleteAccount = async () => {
    if (!deletePassword.value) {
        toast.add({ severity: 'warn', summary: '확인 필요', detail: '비밀번호를 입력해주세요.', life: 3000 });
        return;
    }

    deletingAccount.value = true;
    try {
    await deleteAccount(deletePassword.value);
    toast.add({
        severity: "success",
        summary: "Success",
        detail: "계정이 삭제되었습니다.",
        life: 3000,
    });
    authStore.logoutUser();
    showDeleteDialog.value = false;
    router.push("/");
    } catch (error: any) {
    toast.add({
        severity: "error",
        summary: "Error",
        detail: error.response?.data?.message || "계정 삭제에 실패했습니다.",
        life: 3000,
    });
    } finally {
        deletingAccount.value = false;
    }
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-edit-container {
  min-height: calc(100vh - 100px);
  padding-bottom: 3rem;
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

/* Fallback: hide any plain span or label siblings that display the filename/text */
:deep(.p-fileupload) span {
  display: none !important;
}

/* 스크롤바 숨기기 (가로 스크롤 깔끔하게) */
.scrollbar-hide {
    -ms-overflow-style: none; /* IE and Edge */
    scrollbar-width: none; /* Firefox */
}
.scrollbar-hide::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.action-card {
  background: var(--color-background-soft, #111827);
  border: 1px solid var(--color-border, rgba(255, 255, 255, 0.12));
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

.danger-card {
  background: rgba(255, 99, 71, 0.08);
  border: 1px solid rgba(255, 99, 71, 0.26);
  box-shadow: 0 12px 32px rgba(255, 99, 71, 0.15);
}

.muted-text {
  color: var(--color-text-muted, #9ca3af);
}

.danger-muted {
  color: #fca5a5;
}

:global([data-theme="dark"] .action-card) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.18);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"] .danger-card) {
  background: rgba(239, 68, 68, 0.14);
  border-color: rgba(239, 68, 68, 0.32);
  box-shadow: 0 18px 44px rgba(239, 68, 68, 0.2);
}

:global([data-theme="dark"] .muted-text) {
  color: rgba(255, 255, 255, 0.72);
}
</style>
