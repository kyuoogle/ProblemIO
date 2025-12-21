<template>
  <Card class="h-full hover:shadow-lg transition-duration-200 cursor-pointer" :class="{ 'opacity-75 grayscale-0': isExpired }" @click="handleClick">
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
        <Button 
          :label="isExpired ? '이미 종료됨 (결과 보기)' : '도전하기'" 
          :class="['w-full', isExpired ? 'opacity-80' : '']" 
          :icon="isExpired ? 'pi pi-chart-bar' : 'pi pi-bolt'" 
          :severity="isExpired ? 'secondary' : 'primary'"
        />
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

const emit = defineEmits(['click'])

const isExpired = computed(() => {
    if (!props.challenge.endAt) return false;
    const end = new Date(props.challenge.endAt);
    const now = new Date();
    return now > end;
});

const handleClick = () => {
    // if (isExpired.value) return; // Allow click even if expired
    emit('click');
}

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
