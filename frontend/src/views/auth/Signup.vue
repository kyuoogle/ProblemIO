<template>
  <div class="signup-container">
    <Toast />
    <div class="flex align-items-center justify-content-center min-h-screen px-4">
      <Card class="w-full max-w-md shadow-5">
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
              <h2 class="text-2xl font-bold mb-2">Create Account</h2>
              <p class="text-color-secondary mb-4">Join thousands of learners creating image quizzes</p>
            </div>

            <form @submit.prevent="handleSignup" class="flex flex-column gap-4">
              <!-- 이메일 입력 및 인증 -->
              <div class="flex flex-column gap-2">
                <label for="email" class="text-sm font-medium">Email (Username)</label>
                <div class="flex gap-2">
                  <InputText
                    id="email"
                    v-model="formData.email"
                    type="email"
                    placeholder="you@example.com"
                    :disabled="emailState.isVerified"
                    :class="{ 'p-invalid': errors.email }"
                    class="flex-1"
                    @blur="validateEmail"
                    @input="clearError('email')"
                  />
                  <Button
                    v-if="!emailState.isVerified"
                    type="button"
                    label="인증번호 전송"
                    :loading="emailState.sending"
                    :disabled="!isEmailValid || emailState.isVerified"
                    @click="handleSendVerificationCode"
                    severity="secondary"
                  />
                </div>
                <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
                <small v-else-if="emailState.isVerified" class="text-green-500">
                  ✓ 이메일 인증이 완료되었습니다.
                </small>
                <small v-else class="text-color-secondary">이메일을 입력하고 인증번호를 받아주세요.</small>
              </div>

              <!-- 인증번호 입력 (인증번호 전송 후 표시) -->
              <div v-if="emailState.codeSent && !emailState.isVerified" class="flex flex-column gap-2">
                <label for="verificationCode" class="text-sm font-medium">인증번호</label>
                <div class="flex gap-2">
                  <InputText
                    id="verificationCode"
                    v-model="formData.verificationCode"
                    type="text"
                    placeholder="6자리 숫자"
                    maxlength="6"
                    :class="{ 'p-invalid': errors.verificationCode }"
                    class="flex-1"
                    @input="handleCodeInput"
                  />
                  <Button
                    type="button"
                    label="확인"
                    :loading="emailState.verifying"
                    :disabled="!isVerificationCodeValid"
                    @click="handleVerifyCode"
                    severity="success"
                  />
                </div>
                <small v-if="errors.verificationCode" class="p-error">{{ errors.verificationCode }}</small>
                <small v-else class="text-color-secondary">인증번호는 3분간 유효합니다.</small>
              </div>

              <!-- 닉네임 입력 -->
              <div class="flex flex-column gap-2">
                <label for="nickname" class="text-sm font-medium">Nickname</label>
                <InputText
                  id="nickname"
                  v-model="formData.nickname"
                  type="text"
                  placeholder="닉네임을 설정해주세요"
                  :class="{ 'p-invalid': errors.nickname }"
                  class="w-full"
                  @blur="validateNickname"
                  @input="clearError('nickname')"
                />
                <small v-if="errors.nickname" class="p-error">{{ errors.nickname }}</small>
                <small v-else class="text-color-secondary">2-10자 사이로 입력해주세요.</small>
              </div>

              <!-- 비밀번호 입력 -->
              <div class="flex flex-column gap-2">
                <label for="password" class="text-sm font-medium">Password</label>
                <Password
                  id="password"
                  v-model="formData.password"
                  :feedback="true"
                  toggleMask
                  placeholder="••••••••"
                  :class="{ 'p-invalid': errors.password }"
                  class="w-full"
                  @blur="validatePassword"
                  @input="clearError('password')"
                />
                <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
                <small v-else class="text-color-secondary">
                  최소 8자 이상, 영문/숫자/특수문자 각 1개 이상 포함
                </small>
              </div>

              <!-- 비밀번호 확인 -->
              <div class="flex flex-column gap-2">
                <label for="confirmPassword" class="text-sm font-medium">Confirm Password</label>
                <Password
                  id="confirmPassword"
                  v-model="formData.confirmPassword"
                  toggleMask
                  placeholder="••••••••"
                  :class="{ 'p-invalid': errors.confirmPassword }"
                  class="w-full"
                  @blur="validateConfirmPassword"
                  @input="clearError('confirmPassword')"
                />
                <small v-if="errors.confirmPassword" class="p-error">{{ errors.confirmPassword }}</small>
              </div>

              <!-- 회원가입 버튼 -->
              <Button
                type="submit"
                label="회원가입"
                :loading="loading"
                :disabled="!isFormValid"
                class="w-full"
                size="large"
              />
            </form>

            <Divider />

            <div class="text-center text-sm">
              <span class="text-color-secondary">Already have an account? </span>
              <router-link to="/auth/login" class="text-primary font-semibold no-underline">
                Sign in
              </router-link>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { signup, sendEmailVerification, verifyEmailCode } from '@/api/auth'

const router = useRouter()
const toast = useToast()

// 폼 데이터
const formData = ref({
  email: '',
  verificationCode: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

// 이메일 인증 상태
const emailState = ref({
  isVerified: false,
  codeSent: false,
  sending: false,
  verifying: false,
})

// 에러 메시지
const errors = ref<Record<string, string>>({})

// 로딩 상태
const loading = ref(false)

// 이메일 정규식
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// 이메일 유효성 검사
const isEmailValid = computed(() => {
  return emailRegex.test(formData.value.email)
})

// 인증번호 유효성 검사 (6자리 숫자)
const isVerificationCodeValid = computed(() => {
  return /^\d{6}$/.test(formData.value.verificationCode)
})

// 비밀번호 강도 검사
const isPasswordStrong = computed(() => {
  const pwd = formData.value.password
  if (pwd.length < 8) return false
  const hasLetter = /[a-zA-Z]/.test(pwd)
  const hasNumber = /[0-9]/.test(pwd)
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(pwd)
  return hasLetter && hasNumber && hasSpecial
})

// 전체 폼 유효성 검사
const isFormValid = computed(() => {
  return (
    emailState.value.isVerified &&
    isEmailValid.value &&
    formData.value.nickname.length >= 2 &&
    formData.value.nickname.length <= 10 &&
    isPasswordStrong.value &&
    formData.value.password === formData.value.confirmPassword &&
    formData.value.password.length > 0
  )
})

// 이메일 유효성 검사
const validateEmail = () => {
  if (!formData.value.email) {
    errors.value.email = '이메일을 입력해주세요.'
    return false
  }
  if (!isEmailValid.value) {
    errors.value.email = '올바른 이메일 형식이 아닙니다.'
    return false
  }
  delete errors.value.email
  return true
}

// 닉네임 유효성 검사
const validateNickname = () => {
  const nickname = formData.value.nickname
  if (!nickname) {
    errors.value.nickname = '닉네임을 입력해주세요.'
    return false
  }
  if (nickname.length < 2 || nickname.length > 10) {
    errors.value.nickname = '닉네임은 2-10자 사이여야 합니다.'
    return false
  }
  delete errors.value.nickname
  return true
}

// 비밀번호 유효성 검사
const validatePassword = () => {
  const pwd = formData.value.password
  if (!pwd) {
    errors.value.password = '비밀번호를 입력해주세요.'
    return false
  }
  if (pwd.length < 8) {
    errors.value.password = '비밀번호는 최소 8자 이상이어야 합니다.'
    return false
  }
  if (!isPasswordStrong.value) {
    errors.value.password = '영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.'
    return false
  }
  delete errors.value.password
  return true
}

// 비밀번호 확인 유효성 검사
const validateConfirmPassword = () => {
  if (!formData.value.confirmPassword) {
    errors.value.confirmPassword = '비밀번호 확인을 입력해주세요.'
    return false
  }
  if (formData.value.password !== formData.value.confirmPassword) {
    errors.value.confirmPassword = '비밀번호가 일치하지 않습니다.'
    return false
  }
  delete errors.value.confirmPassword
  return true
}

// 에러 메시지 제거
const clearError = (field: string) => {
  delete errors.value[field]
}

// 인증번호 입력 처리 (숫자만 허용)
const handleCodeInput = (event: Event) => {
  const input = event.target as HTMLInputElement
  const value = input.value.replace(/\D/g, '') // 숫자가 아닌 문자 제거
  formData.value.verificationCode = value.slice(0, 6) // 최대 6자리
  clearError('verificationCode')
}

// 인증번호 전송
const handleSendVerificationCode = async () => {
  if (!validateEmail()) {
    return
  }

  emailState.value.sending = true
  try {
    await sendEmailVerification(formData.value.email)
    emailState.value.codeSent = true
    toast.add({
      severity: 'success',
      summary: '성공',
      detail: '인증번호가 발송되었습니다. 이메일을 확인해주세요.',
      life: 3000,
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: error.response?.data || '인증번호 발송에 실패했습니다.',
      life: 3000,
    })
  } finally {
    emailState.value.sending = false
  }
}

// 인증번호 확인
const handleVerifyCode = async () => {
  if (!isVerificationCodeValid.value) {
    errors.value.verificationCode = '6자리 숫자를 입력해주세요.'
    return
  }

  emailState.value.verifying = true
  try {
    await verifyEmailCode(formData.value.email, formData.value.verificationCode)
    emailState.value.isVerified = true
    toast.add({
      severity: 'success',
      summary: '성공',
      detail: '이메일 인증이 완료되었습니다.',
      life: 3000,
    })
  } catch (error: any) {
    errors.value.verificationCode = '인증번호가 올바르지 않거나 만료되었습니다.'
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: error.response?.data || '인증에 실패했습니다.',
      life: 3000,
    })
  } finally {
    emailState.value.verifying = false
  }
}

// 회원가입 처리
const handleSignup = async () => {
  // 전체 유효성 검사
  if (!validateEmail() || !validateNickname() || !validatePassword() || !validateConfirmPassword()) {
    toast.add({
      severity: 'warn',
      summary: '알림',
      detail: '입력 정보를 확인해주세요.',
      life: 3000,
    })
    return
  }

  // 이메일 인증 확인
  if (!emailState.value.isVerified) {
    toast.add({
      severity: 'warn',
      summary: '알림',
      detail: '이메일 인증을 완료해주세요.',
      life: 3000,
    })
    return
  }

  loading.value = true
  try {
    await signup({
      email: formData.value.email,
      password: formData.value.password,
      nickname: formData.value.nickname,
    })
    toast.add({
      severity: 'success',
      summary: '성공',
      detail: '회원가입이 완료되었습니다.',
      life: 3000,
    })
    // 로그인 페이지로 이동
    setTimeout(() => {
      router.push('/auth/login')
    }, 1000)
  } catch (error: any) {
    const errorMessage =
      error.response?.data?.message ||
      error.response?.data?.data?.message ||
      error.response?.data ||
      '회원가입에 실패했습니다.'
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: errorMessage,
      life: 3000,
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.signup-container {
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

.shadow-5 {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06),
    0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.flex-1 {
  flex: 1 1 0%;
}

.text-green-500 {
  color: #10b981;
}
</style>