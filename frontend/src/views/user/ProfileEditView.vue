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
              <!-- Profile Info -->
              <div class="flex flex-column gap-4">
                <h2 class="text-2xl font-bold">Profile Information</h2>
                
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
  
                <div class="flex flex-column gap-2">
                  <label class="text-sm font-medium">Profile Image URL</label>
                  <InputText
                    v-model="profileForm.profileImageUrl"
                    placeholder="https://example.com/profile.jpg"
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
  
              <!-- Change Password -->
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
  
              <!-- Delete Account -->
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
  import {  updateMyProfile, changePassword, deleteAccount } from '@/api/user'
  import { getMe } from '@/api/user'
  
  const router = useRouter()
  const toast = useToast()
  const confirm = useConfirm()
  const authStore = useAuthStore()
  
  const profileForm = ref({
    nickname: '',
    statusMessage: '',
    profileImageUrl: '',
  })
  
  const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  
  const savingProfile = ref(false)
  const changingPassword = ref(false)
  
  const loadProfile = async () => {
    try {
      const user = await getMe()
      profileForm.value.nickname = user.nickname || ''
      profileForm.value.statusMessage = user.statusMessage || ''
      profileForm.value.profileImageUrl = user.profileImageUrl || ''
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load profile',
        life: 3000,
      })
    }
  }
  
  const handleSaveProfile = async () => {
    savingProfile.value = true
    try {
      await updateMyProfile(profileForm.value)
      await authStore.refreshUser()
      toast.add({
        severity: 'success',
        summary: 'Success',
        detail: 'Profile updated successfully',
        life: 3000,
      })
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
  </style>