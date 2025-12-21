<template>
  <div class="challenge-detail-container">
    <div class="container mx-auto px-4">
      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
        <p>Loading...</p>
      </div>
      
      <div v-if="error" class="text-center py-8 text-red-500">
        <i class="pi pi-exclamation-triangle text-4xl mb-2"></i>
        <p>{{ error }}</p>
        <Button label="목록으로" class="mt-4" @click="router.push('/challenges')" />
      </div>

      <div v-else-if="challenge" class="flex flex-col lg:flex-row justify-center gap-6 items-start relative">
        <!-- Left Column: Challenge Info (Centered Fixed Width) -->
        <div class="w-full max-w-3xl flex flex-col gap-6 py-6">
            <Card class="challenge-info-card shadow-lg border-0 rounded-2xl overflow-hidden">
                <template #header>
                    <div class="aspect-video bg-surface-100 overflow-hidden thumbnail-frame relative">
                         <img 
                            v-if="challenge.targetQuiz?.thumbnailUrl" 
                            :src="resolveImageUrl(challenge.targetQuiz.thumbnailUrl)" 
                            class="w-full h-full object-cover" 
                            alt="Challenge Thumbnail" 
                         />
                         <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-indigo-500/10 to-purple-500/10 text-gray-400">
                            <!-- Placeholder Icon -->
                            <i class="pi pi-bolt text-6xl text-indigo-300"></i>
                         </div>
                         <div class="absolute top-4 left-4 flex gap-2">
                            <Tag v-if="challenge.challengeType === 'TIME_ATTACK'" icon="pi pi-clock" severity="danger" value="TIME ATTACK" rounded></Tag>
                            <Tag v-else-if="challenge.challengeType === 'SURVIVAL'" icon="pi pi-shield" severity="help" value="SURVIVAL" rounded></Tag>
                            <Tag v-else severity="info" value="EVENT" rounded></Tag>
                         </div>
                    </div>
                </template>
                <template #content>
                    <div class="flex flex-col gap-4 p-2">
                        <div class="flex justify-between items-start">
                             <h1 class="text-3xl font-extrabold m-0 text-heading tracking-tight">{{ challenge.title }}</h1>
                             <span v-if="challenge.timeLimit > 0" class="text-color-secondary font-medium bg-surface-100 px-3 py-1 rounded-full text-sm">
                                <i class="pi pi-stopwatch mr-1 text-indigo-500"></i> {{ challenge.timeLimit }}초 제한
                             </span>
                        </div>
                        
                        <p class="text-lg text-color-secondary leading-relaxed whitespace-pre-line">
                            {{ challenge.description }}
                        </p>
    
                        <Divider />
    
                        <div class="flex flex-col gap-2">
                             <div class="flex items-center gap-2 text-color-secondary">
                                <i class="pi pi-calendar text-primary"></i>
                                <span class="font-medium">이벤트 기간:</span>
                                <span>{{ formatDate(challenge.startAt) }} ~ {{ formatDate(challenge.endAt) }}</span>
                             </div>
                        </div>
                    </div>
                </template>
                <template #footer>
                    <div class="flex gap-4 mt-2 p-2">
                        <Button label="뒤로가기" icon="pi pi-arrow-left" severity="secondary" outlined class="w-32" @click="router.back()" />
                        <Button :label="isExpired ? '이미 종료된 챌린지입니다' : '도전 시작하기'" 
                                :icon="isExpired ? 'pi pi-lock' : 'pi pi-bolt'" 
                                :disabled="isExpired"
                                size="large" class="flex-1 font-bold p-button-lg shadow-lg shadow-indigo-500/30" @click="startChallengeGame" />
                    </div>
                </template>
            </Card>
        </div>

        <!-- Right Column: Ranking Widget (Fixed Width) -->
        <div class="w-full lg:w-[380px] shrink-0">
             <div class="sticky top-6">
                 <ChallengeRankingWidget :challengeId="challengeId" />
             </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
// import { useToast } from "primevue/usetoast";
import { getChallenge } from "@/api/challenge";
import { resolveImageUrl } from "@/lib/image";
import Card from "primevue/card";
import Button from "primevue/button";
import Tag from "primevue/tag";
import Divider from "primevue/divider";
import ChallengeRankingWidget from "@/components/challenge/ChallengeRankingWidget.vue";

const route = useRoute();
const router = useRouter();
// const toast = useToast();

const challenge = ref<any | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const challengeId = computed(() => Number(route.params.id));

const isExpired = computed(() => {
    if (!challenge.value || !challenge.value.endAt) return false;
    return new Date(challenge.value.endAt) < new Date();
});

const loadChallengeDetail = async () => {
    loading.value = true;
    error.value = null;
    try {
        const data = await getChallenge(challengeId.value);
        challenge.value = data;
    } catch (err: any) {
        // console.error("Failed to load challenge:", err);
        error.value = "챌린지 정보를 불러올 수 없습니다.";
    } finally {
        loading.value = false;
    }
};

const startChallengeGame = () => {
    if (isExpired.value) return;
    router.push(`/challenges/${challengeId.value}/play`);
};

const formatDate = (dateStr: string) => {
    if (!dateStr) return "상시";
    const d = new Date(dateStr);
    return `${d.getFullYear()}.${d.getMonth() + 1}.${d.getDate()}`;
};

onMounted(() => {
    loadChallengeDetail();
});
</script>

<style scoped>
.challenge-detail-container {
  min-height: calc(100vh - 100px);
}

.text-heading {
    color: var(--color-heading);
}

/* Card Styling overrides */
.challenge-info-card :deep(.p-card-body) {
  padding: 0;
}
.challenge-info-card :deep(.p-card-content) {
  padding: 1.5rem;
}
</style>
