<template>
  <div class="container mx-auto px-4 py-8 max-w-6xl">
    <div class="mb-6">
      <h1 class="text-3xl font-bold mb-2">이벤트 챌린지</h1>
      <p class="text-color-secondary">한계를 시험하고 랭킹에 도전하세요!</p>
    </div>
    
    <ChallengeList :challenges="challenges" :loading="loading" @select="goDetail" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ChallengeList from '@/components/challenge/ChallengeList.vue'
import { getChallenges } from '@/api/challenge'

const router = useRouter()
const challenges = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    challenges.value = await getChallenges()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
})

const goDetail = (id) => {
  // 상세 페이지가 따로 있다면 거기로, 없다면 바로 play? 
  // 기획상: 1. 목록 -> 2. 상세(ChallengeDetail??) -> 3. Start -> 4. Play
  // 현재 상세View를 안 만들었음. 바로 Play로 가기엔 "도전하기" 버튼이 Start를 의미하므로,
  // 중간에 설명 페이지가 있으면 좋겠지만, 
  // 1-3. Controller: GET /challenges/{id} (상세) 가 있음.
  // ChallengeCard에서도 "도전하기" 누르면 바로 시작?
  // User Requirements 2-2: "ChallengePlayView: 실제 게임 플레이 화면"
  // "ChallengeDetailView" was not explicitly asked but usually needed.
  // For now, let's simplify: Click card -> Go to Play view directly?
  // Or maybe a modal? 
  // "POST /api/challenges/{id}/start" is called in ChallengePlayView onMounted.
  // So navigating to /challenges/{id}/play triggers start.
  // Let's go to play directly for MVP.
  // Wait, startChallenge is POST. If I navigate to page invoke it on mount, standard page refresh will re-start? Yes.
  // Usually acceptable for simple web games.
  router.push(`/challenges/${id}`)
}
</script>
