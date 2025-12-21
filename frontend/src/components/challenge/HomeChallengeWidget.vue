<template>
  <div class="challenge-widget">
    <div class="ranking-header">
      <div class="title">
        <span class="flame">⚔️</span>
        <span>Challenges</span>
        <span class="flame">⚔️</span>
      </div>
      <Button label="View All" text size="small" @click="router.push('/challenges')" />
    </div>
    <ChallengeList
      :challenges="challenges"
      :loading="loading"
      :sidebarMode="true"
      @select="goToChallenge"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getChallenges } from "@/api/challenge";
import ChallengeList from "@/components/challenge/ChallengeList.vue";
import Button from "primevue/button";

const router = useRouter();
const challenges = ref<any[]>([]);
const loading = ref(false);

const loadChallenges = async () => {
  loading.value = true;
  try {
    const data = await getChallenges();
    const now = new Date();
    challenges.value = data.filter((c: any) => !c.endAt || new Date(c.endAt) > now);
  } catch (error: any) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const goToChallenge = (challengeId: number) => {
  router.push(`/challenges/${challengeId}`);
};

onMounted(() => {
  loadChallenges();
});
</script>

<style scoped>
/* Challenge Widget Styling (Matches Ranking Widget) */
.challenge-widget {
  position: relative;
  width: 100%;
  padding: 22px;
  border-radius: 20px;
  background: var(--color-background-soft);
  border: 1px solid rgba(99, 102, 241, 0.3);
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.14), 0 0 0 1px rgba(255, 255, 255, 0.04);
  overflow: hidden;
}

:global([data-theme="dark"] .challenge-widget) {
  background: radial-gradient(circle at 20% 20%, rgba(88, 28, 135, 0.08), transparent 35%),
    radial-gradient(circle at 80% 0%, rgba(14, 165, 233, 0.08), transparent 35%),
    #0f172a;
  border: 1px solid rgba(94, 234, 212, 0.25);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(148, 163, 184, 0.1), 0 0 30px rgba(94, 234, 212, 0.18);
}

.ranking-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
  min-height: 48px;
}

.title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 2000;
  letter-spacing: 1px;
  color: var(--color-heading);
  font-size: 1.1rem;
  text-shadow: 0 4px 18px rgba(255, 115, 29, 0.35);
}

.flame {
  filter: drop-shadow(0 0 6px rgba(255, 68, 68, 0.65));
}
</style>
