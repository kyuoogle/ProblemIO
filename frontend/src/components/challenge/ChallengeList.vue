<template>
  <div v-if="loading" class="text-center py-8">
    <i class="pi pi-spin pi-spinner text-4xl text-primary"></i>
  </div>
  
  <div v-else-if="!challenges || challenges.length === 0" class="text-center py-8 text-color-secondary">
    <i class="pi pi-flag text-4xl mb-3 block"></i>
    진행 중인 챌린지가 없습니다.
  </div>

  <div v-else :class="['grid gap-6', sidebarMode ? 'grid-cols-1' : 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3']">
    <ChallengeCard 
      v-for="challenge in challenges" 
      :key="challenge.id" 
      :challenge="challenge"
      @click="onCardClick(challenge)"
    />
  </div>
</template>

<script setup lang="ts">
import ChallengeCard from './ChallengeCard.vue'

defineProps<{
  challenges: any[]
  loading: boolean
  sidebarMode?: boolean
}>()

const emit = defineEmits(['select'])

const onCardClick = (challenge: any) => {
  emit('select', challenge.id)
}
</script>
