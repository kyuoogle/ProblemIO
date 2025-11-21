<template>
  <div class="profile-edit-container">
    <div class="container mx-auto px-4 max-w-2xl">
      <Card>
        <template #header>
          <div class="p-4 border-bottom-1 surface-border">
            <h1 class="text-3xl font-bold m-0">Edit Profile</h1>
          </div>
        </template>
        <template #content>
          <div class="flex flex-column gap-6">
            <div class="flex flex-column gap-4">
              <h2 class="text-2xl font-bold">Profile Information</h2>

              <div class="flex flex-column gap-2 align-items-center">
                <div class="relative inline-block">
                  <Avatar
                   :image="authStore.user?.profileImageUrl ? `http://localhost:8080${authStore.user.profileImageUrl}` : null"
                    :label="!previewUrl && profileForm.nickname ? profileForm.nickname.charAt(0).toUpperCase() : ''"
                    :icon="!previewUrl && !profileForm.nickname ? 'pi pi-user' : ''"
                    class="w-32 h-32 text-6xl font-bold border-2 border-gray-200"
                    :class="{ 'surface-200 text-700': !previewUrl }"
                    shape="circle"
                  />
                  
                  <div class="absolute bottom-0 right-0">
                    <FileUpload
                      mode="basic"
                      name="file"
                      accept="image/*"
                      :maxFileSize="5000000"
                      @select="onFileSelect"
                      @clear="onFileRemove"
                      :auto="false"
                      chooseLabel=" "
                      chooseIcon="pi pi-camera"
                      class="p-button-rounded p-button-sm shadow-2"
                      style="width: 2.5rem; height: 2.5rem;"
                    />
                  </div>
                </div>
                <small class="text-gray-500">Click camera icon to change image (Max 5MB)</small>
              </div>

              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">Nickname</label>
                <InputText
                  v-model="profileForm.nickname"
                  placeholder="Enter nickname"
                  class="w-full"
                />
              </div>

              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">Status Message</label>
                <Textarea
                  v-model="profileForm.statusMessage"
                  placeholder="Enter status message"
                  rows="3"
                  class="w-full"
                />
              </div>

              <Button
                label="Save Profile"
                icon="pi pi-check"
                :loading="savingProfile"
                @click="handleSaveProfile"
              />
            </div>

            <Divider />

            <div class="flex flex-column gap-4">
              <h2 class="text-2xl font-bold">Change Password</h2>
              
              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">Current Password</label>
                <Password
                  v-model="passwordForm.currentPassword"
                  toggleMask
                  placeholder="Enter current password"
                  class="w-full"
                />
              </div>

              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">New Password</label>
                <Password
                  v-model="passwordForm.newPassword"
                  toggleMask
                  placeholder="Enter new password"
                  class="w-full"
                />
              </div>

              <div class="flex flex-column gap-2">
                <label class="text-sm font-medium">Confirm New Password</label>
                <Password
                  v-model="passwordForm.confirmPassword"
                  toggleMask
                  placeholder="Confirm new password"
                  class="w-full"
                />
              </div>

              <Button
                label="Change Password"
                icon="pi pi-key"
                :loading="changingPassword"
                @click="handleChangePassword"
              />
            </div>

            <Divider />

            <div class="flex flex-column gap-4">
              <h2 class="text-2xl font-bold text-red-500">Danger Zone</h2>
              
              <div class="p-4 border-round bg-red-50 border-1 border-red-200">
                <p class="font-semibold mb-2">Delete Account</p>
                <p class="text-sm text-color-secondary mb-4">
                  Once you delete your account, there is no going back. Please be certain.
                </p>
                <Button
                  label="Delete Account"
                  icon="pi pi-trash"
                  severity="danger"
                  outlined
                  @click="handleDeleteAccount"
                />
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { useAuthStore } from '@/stores/auth'
import { updateMyProfile, changePassword, deleteAccount } from '@/api/user'
import { getMe } from '@/api/user'
// PrimeVue 컴포넌트가 자동 import가 안된다면 아래 주석 해제
// import Avatar from 'primevue/avatar';

const router = useRouter()
const toast = useToast()
const confirm = useConfirm()
const authStore = useAuthStore()

const profileForm = ref({
  nickname: '',
  statusMessage: '',
})

const selectedFile = ref<File | null>(null)
const previewUrl = ref('') // 미리보기 이미지 URL (없으면 빈 문자열)

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const savingProfile = ref(false)
const changingPassword = ref(false)

// 1. 초기 데이터 로드
const loadProfile = async () => {
  try {
    const user = await getMe()
    profileForm.value.nickname = user.nickname || ''
    profileForm.value.statusMessage = user.statusMessage || ''
    // 서버에서 받은 이미지가 있으면 설정, 없으면 빈 값 (Avatar가 닉네임 표시함)
    previewUrl.value = user.profileImageUrl || ''
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Failed to load profile',
      life: 3000,
    })
  }
}

// 2. 파일 선택 핸들러
const onFileSelect = (event: any) => {
  const file = event.files[0]
  selectedFile.value = file
  // 파일 객체로부터 미리보기 URL 생성
  previewUrl.value = URL.createObjectURL(file)
}

// 3. 파일 선택 취소 핸들러
const onFileRemove = () => {
  selectedFile.value = null
  // 취소 시 원래 서버 이미지 URL로 복구하거나 초기화
  // (여기서는 간단히 다시 로드하여 원래 상태로 복구)
  loadProfile()
}

// 4. 프로필 저장 (FormData 사용)
const handleSaveProfile = async () => {
  savingProfile.value = true
  try {
    const formData = new FormData()

    // JSON 데이터
    const jsonPart = JSON.stringify(profileForm.value)
    const blob = new Blob([jsonPart], { type: 'application/json' })
    formData.append('data', blob)

    // 파일 데이터 (선택된 경우만)
    if (selectedFile.value) {
      formData.append('file', selectedFile.value)
    }

    await updateMyProfile(formData)
    
    // Auth Store 갱신
    await authStore.refreshUser()
    
    toast.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Profile updated successfully',
      life: 3000,
    })
    selectedFile.value = null

  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: error.response?.data?.message || 'Failed to update profile',
      life: 3000,
    })
  } finally {
    savingProfile.value = false
  }
}

// ... (handleChangePassword, handleDeleteAccount 로직은 기존과 동일하므로 생략 가능, 파일에는 포함되어 있어야 함) ...
const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.add({
      severity: 'warn',
      summary: 'Warning',
      detail: 'Passwords do not match',
      life: 3000,
    })
    return
  }

  if (passwordForm.value.newPassword.length < 8) {
    toast.add({
      severity: 'warn',
      summary: 'Warning',
      detail: 'Password must be at least 8 characters',
      life: 3000,
    })
    return
  }

  changingPassword.value = true
  try {
    await changePassword(passwordForm.value.currentPassword, passwordForm.value.newPassword)
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    }
    toast.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Password changed successfully',
      life: 3000,
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: error.response?.data?.message || 'Failed to change password',
      life: 3000,
    })
  } finally {
    changingPassword.value = false
  }
}

const handleDeleteAccount = () => {
  confirm.require({
    message: 'Are you absolutely sure? This action cannot be undone.',
    header: 'Delete Account',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: async () => {
      const password = prompt('Please enter your password to confirm:')
      if (!password) return

      try {
        await deleteAccount(password)
        toast.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Account deleted successfully',
          life: 3000,
        })
        authStore.logoutUser()
        router.push('/')
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Error',
          detail: error.response?.data?.message || 'Failed to delete account',
          life: 3000,
        })
      }
    },
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-edit-container {
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 카메라 버튼 스타일 (둥글게, 중앙 정렬) */
:deep(.p-fileupload-choose) {
  border-radius: 50%;
  padding: 0 !important;
  display: flex !important;
  align-items: center;
  justify-content: center;
}
</style>