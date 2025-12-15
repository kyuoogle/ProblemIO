<template>
  <Card class="h-full hover:shadow-lg transition-duration-200 cursor-pointer" @click="$emit('click')">
    <template #header>
      <div v-if="challenge.challengeType === 'TIME_ATTACK'" class="bg-red-100 text-red-600 px-3 py-1 font-bold text-center">
        TIME ATTACK
      </div>
      <div v-else-if="challenge.challengeType === 'SURVIVAL'" class="bg-purple-100 text-purple-600 px-3 py-1 font-bold text-center">
        SURVIVAL
      </div>
      <div v-else class="bg-gray-100 text-gray-600 px-3 py-1 font-bold text-center">
        EVENT
      </div>
    </template>
    
    <template #title>
      <div class="text-xl font-bold mb-2">{{ challenge.title }}</div>
    </template>
    
    <template #content>
      <p class="text-color-secondary line-clamp-2 h-3rem">
        {{ challenge.description }}
      </p>
      
      <div class="mt-4 flex gap-2" v-if="challenge.challengeType === 'TIME_ATTACK'">
        <Tag icon="pi pi-clock" severity="danger" :value="formatTime(challenge.timeLimit)"></Tag>
      </div>
    </template>

    <template #footer>
        <Button label="도전하기" class="w-full" icon="pi pi-bolt" />
    </template>
  </Card>
</template>

<script setup>
import { computed } from 'vue'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Tag from 'primevue/tag'

const props = defineProps({
  challenge: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m}분 ${s}초`
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
