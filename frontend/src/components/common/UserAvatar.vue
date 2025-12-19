<template>
  <div class="relative inline-block" :style="containerStyle">
    <Avatar
      :image="image"
      :label="label"
      :icon="icon"
      shape="circle" 
      :class="avatarClass"
      :style="avatarStyle"
    />
    <img 
      v-if="decorationUrl" 
      :src="decorationUrl" 
      class="absolute top-0 left-0 w-full h-full object-contain pointer-events-none z-10"
      alt="decoration"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { resolveImageUrl } from '@/lib/image'
import { AVATAR_DECORATIONS } from '@/constants/avatarConfig'

const props = defineProps({
  user: {
    type: Object,
    default: null,
  },
  // PrimeVue Avatar size prop (normal, large, xlarge - or explicit styling)
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

const decorationUrl = computed(() => {
  const u = targetUser.value
  if (!u?.avatarDecoration) return null
  const deco = AVATAR_DECORATIONS[u.avatarDecoration]
  return deco ? resolveImageUrl(deco.image) : null
})

// Size mapping: PrimeVue Avatar size classes aren't always enough for custom wrapper sizing
// We'll map 'size' prop to explicit pixel sizes for the wrapper to container the decoration
const sizeMap = {
  small: '1.5rem',
  normal: '2rem', // 32px
  medium: '2.5rem',
  large: '3rem',  // 48px
  xlarge: '4rem', // 64px
}

const containerStyle = computed(() => {
  const s = sizeMap[props.size] || '4rem' // default to xlarge if unknown
  return { width: s, height: s }
})

const avatarClass = computed(() => {
  const classes = []
  if (props.size) classes.push(`p-avatar-${props.size}`)
  return classes
})

const avatarStyle = computed(() => {
  return { 
    width: '100%', 
    height: '100%',
    // Reset inherited text styles that might come from themes (prevent neon effects on avatar text)
    textShadow: 'none',
    WebkitTextStroke: '0',
    color: '#374151', // Force dark text (gray-700) for readability inside avatar
    objectFit: 'cover', // Force image to not stretch
  }
})
</script>

<style scoped>
:deep(.p-avatar img) {
    object-fit: cover;
    width: 100%;
    height: 100%;
}
</style>
