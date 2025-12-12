<template>
  <div class="login-container">
    <div class="flex items-center justify-center min-h-screen px-4">
      <Card class="w-full max-w-md shadow-5 auth-card">
        <template #header>
          <div class="p-6 text-center">
            <router-link to="/" class="no-underline">
              <h1 class="text-3xl font-bold text-primary m-0">Problem.io</h1>
            </router-link>
          </div>
        </template>
        <template #content>
          <div class="flex flex-col gap-4">
            <form @submit.prevent="handleLogin" class="flex flex-col gap-4">
              <div class="flex flex-col gap-2">
                <label for="email" class="text-sm font-medium">Email</label>
                <InputText id="email" v-model="email" type="email" placeholder="ID" required class="w-full" />
              </div>
              <div class="flex flex-col gap-2">
                <label for="password" class="text-sm font-medium">Password</label>
                <Password id="password" v-model="password" toggleMask placeholder="••••••••" required class="w-full" />
              </div>
              <Button type="submit" label="로그인" :loading="loading" class="w-full" size="large" />
            </form>
            <Divider />
            <div class="text-center text-sm">
              <span class="text-color-secondary">아이디가 없으신가요?</span>
              <router-link to="/signup" class="text-primary font-semibold no-underline">회원 가입</router-link>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const route = useRoute();
const toast = useToast();
const authStore = useAuthStore();

const email = ref("");
const password = ref("");
const loading = ref(false);

const handleLogin = async () => {
  loading.value = true;
  try {
    await authStore.loginUser(email.value, password.value);
    toast.add({ severity: "success", summary: "로그인", detail: "로그인 성공", life: 3000 });

    const redirect = (route.query.redirect as string) || "/";
    router.push(redirect);
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "실패",
      detail: error.response?.data?.message || "로그인에 실패했습니다.",
      life: 3000,
    });
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(to bottom right, var(--primary-50), var(--surface-ground), var(--accent-50));
}

:global([data-theme="dark"] .login-container) {
  /* background: #000; */
  background: transparent;
}

.auth-card {
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

:global([data-theme="dark"] .auth-card) {
  background: #111;
  border: 1px solid #2c2c2c;
  box-shadow:
    0 18px 48px rgba(0, 0, 0, 0.55),
    inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.auth-card:hover {
  transform: translateY(-3px);
  border-color: var(--color-border-hover);
  box-shadow: 0 20px 42px rgba(0, 0, 0, 0.18);
}

.max-w-md {
  max-width: 28rem;
}

.w-full {
  width: 100%;
}

.min-h-screen {
  min-height: 50vh;
}

.px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.p-6 {
  padding: 1.5rem;
}

.text-3xl {
  font-size: 1.875rem;
  line-height: 2.25rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.font-bold {
  font-weight: 700;
}

.font-semibold {
  font-weight: 600;
}

.text-center {
  text-align: center;
}

.m-0 {
  margin: 0;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.no-underline {
  text-decoration: none;
}

.shadow-5 {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06), 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

:global([data-theme="dark"] .p-inputtext),
:global([data-theme="dark"] .p-password-input) {
  background: #1b1b1b !important;
  border-color: #333 !important;
  color: var(--color-heading) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.02);
}

:global([data-theme="dark"] .p-inputtext:focus),
:global([data-theme="dark"] .p-password-input:focus) {
  border-color: var(--color-primary) !important;
  box-shadow:
    0 0 0 1px rgba(26, 188, 156, 0.45),
    0 12px 26px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"] .p-password-panel) {
  background: #111 !important;
  border-color: #2c2c2c !important;
  color: var(--color-heading) !important;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.55);
}

:global(.p-button) {
  transition: transform 0.12s ease, box-shadow 0.15s ease;
}

:global(.p-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.18);
}

:global(.p-button:active) {
  transform: translateY(0);
}
</style>
