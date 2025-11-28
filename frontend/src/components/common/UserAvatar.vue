<template>
  <Avatar
    :image="image"
    :label="label"
    :icon="icon"
    shape="circle" 
    :size="size"
  />

  <!-- image 경로 나(user)인지 / 타인(auth, profile 등) 인지 --> 
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { resolveImageUrl } from '@/lib/image'

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },

  size: {
    type: String,
    default: 'xlarge',
  },
})

const authStore = useAuthStore()
const targetUser = computed(() => props.user || authStore.user)

const image = computed(() => {
  const u = targetUser.value
  if (!u?.profileImageUrl) return null
  return resolveImageUrl(u.profileImageUrl)
})

const label = computed(() => {
  const u = targetUser.value
  if (!u?.profileImageUrl && u?.nickname) {
    return u.nickname.charAt(0).toUpperCase()
  }
  return ''
})

const icon = computed(() => {
  const u = targetUser.value
  if (!u?.profileImageUrl && !u?.nickname) {
    return 'pi pi-user'
  }
  return ''
})
</script>
