<template>
  <div class="login-container">
    <div class="flex align-items-center justify-content-center min-h-screen px-4">
      <Card class="w-full max-w-md">
        <template #header>
          <div class="p-6 text-center">
            <router-link to="/" class="no-underline">
              <h1 class="text-3xl font-bold text-primary m-0">Problem.io</h1>
            </router-link>
          </div>
        </template>
        <template #content>
          <div class="flex flex-column gap-4">
            <div>
              <h2 class="text-2xl font-bold mb-2">Welcome Back</h2>
              <p class="text-color-secondary mb-4">Sign in to your account to continue</p>
            </div>

            <form @submit.prevent="handleLogin" class="flex flex-column gap-4">
              <div class="flex flex-column gap-2">
                <label for="email" class="text-sm font-medium">Email</label>
                <InputText
                  id="email"
                  v-model="email"
                  type="email"
                  placeholder="you@example.com"
                  required
                  class="w-full"
                />
              </div>
              <div class="flex flex-column gap-2">
                <label for="password" class="text-sm font-medium">Password</label>
                <InputText
                  id="password"
                  v-model="password"
                  type="password"
                  placeholder="••••••••"
                  required
                  class="w-full"
                />
              </div>
              <Button
                type="submit"
                label="Sign In"
                :loading="loading"
                class="w-full"
                size="large"
              />
            </form>

            <Divider />

            <div class="text-center text-sm">
              <span class="text-color-secondary">Don't have an account? </span>
              <router-link to="/auth/signup" class="text-primary font-semibold no-underline">
                Sign up
              </router-link>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { login } from '@/api/auth'

const router = useRouter()
const toast = useToast()
const email = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    await login(email.value, password.value)
    toast.add({ severity: 'success', summary: 'Success', detail: 'Logged in successfully', life: 3000 })
    router.push('/feed')
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: error.response?.data?.message || 'Login failed',
      life: 3000,
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(to bottom right, var(--primary-50), var(--surface-ground), var(--accent-50));
}

.max-w-md {
  max-width: 28rem;
}

.w-full {
  width: 100%;
}

.min-h-screen {
  min-height: 100vh;
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

.font-medium {
  font-weight: 500;
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
</style>
