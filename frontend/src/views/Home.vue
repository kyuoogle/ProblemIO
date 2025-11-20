<template>
  <div class="home-container">
    <div class="flex align-items-center justify-content-center min-h-screen">
      <div class="max-w-4xl mx-auto px-4 py-12 text-center">
        <div class="mb-8">
          <div class="inline-block mb-6 px-4 py-2 bg-primary-50 dark:bg-primary-900 border-round">
            <span class="text-primary font-semibold">No login required to play!</span>
          </div>

          <h1 class="text-5xl md:text-7xl font-bold mb-6">
            Play Image Quizzes
            <span class="block text-primary">Right Now!</span>
          </h1>

          <p class="text-xl md:text-2xl text-color-secondary mb-10 max-w-2xl mx-auto leading-relaxed">
            Jump into fun quizzes made by people around the world. Test your knowledge, compete with friends, and
            climb the leaderboard!
          </p>
        </div>

        <div class="flex flex-column sm:flex-row gap-4 justify-content-center mb-16">
          <router-link to="/feed">
            <Button label="Start Playing" icon="pi pi-play" size="large" class="w-full sm:w-auto" />
          </router-link>
          <router-link to="/auth/login">
            <Button label="Sign In to Create" icon="pi pi-user-plus" size="large" severity="secondary" outlined class="w-full sm:w-auto" />
          </router-link>
        </div>

        <div class="grid md:grid-cols-3 gap-6 mb-12">
          <Card class="hover-card">
            <template #content>
              <div class="text-center p-4">
                <div class="w-12 h-12 mb-4 mx-auto bg-primary-100 dark:bg-primary-900 border-round-xl flex align-items-center justify-content-center">
                  <i class="pi pi-bolt text-primary text-2xl"></i>
                </div>
                <h3 class="font-bold text-lg mb-2">Jump Right In</h3>
                <p class="text-color-secondary leading-relaxed">
                  No sign-up needed. Start playing quizzes instantly and see how you rank!
                </p>
              </div>
            </template>
          </Card>

          <Card class="hover-card">
            <template #content>
              <div class="text-center p-4">
                <div class="w-12 h-12 mb-4 mx-auto bg-accent-100 dark:bg-accent-900 border-round-xl flex align-items-center justify-content-center">
                  <i class="pi pi-sparkles text-accent text-2xl"></i>
                </div>
                <h3 class="font-bold text-lg mb-2">Fun & Visual</h3>
                <p class="text-color-secondary leading-relaxed">
                  Every quiz is based on cool images. Learn while having fun!
                </p>
              </div>
            </template>
          </Card>

          <Card class="hover-card">
            <template #content>
              <div class="text-center p-4">
                <div class="w-12 h-12 mb-4 mx-auto bg-secondary-100 dark:bg-secondary-900 border-round-xl flex align-items-center justify-content-center">
                  <i class="pi pi-users text-primary text-2xl"></i>
                </div>
                <h3 class="font-bold text-lg mb-2">Community Made</h3>
                <p class="text-color-secondary leading-relaxed">
                  Play quizzes created by students, teachers, and quiz enthusiasts!
                </p>
              </div>
            </template>
          </Card>
        </div>

        <p class="text-sm text-color-secondary">
          Want to create your own quizzes?
          <router-link to="/auth/signup" class="text-primary hover:underline font-semibold">
            Sign up free
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(to bottom right, var(--primary-50), var(--surface-ground), var(--accent-50));
}

.hover-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.hover-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

@media (min-width: 768px) {
  .max-w-4xl {
    max-width: 56rem;
  }
  
  .max-w-2xl {
    max-width: 42rem;
  }
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.py-12 {
  padding-top: 3rem;
  padding-bottom: 3rem;
}

.text-center {
  text-align: center;
}

.mb-8 {
  margin-bottom: 2rem;
}

.mb-6 {
  margin-bottom: 1.5rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-10 {
  margin-bottom: 2.5rem;
}

.mb-12 {
  margin-bottom: 3rem;
}

.mb-16 {
  margin-bottom: 4rem;
}

.text-5xl {
  font-size: 3rem;
  line-height: 1;
}

.text-xl {
  font-size: 1.25rem;
  line-height: 1.75rem;
}

.text-lg {
  font-size: 1.125rem;
  line-height: 1.75rem;
}

.text-sm {
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.font-bold {
  font-weight: 700;
}

.font-semibold {
  font-weight: 600;
}

.leading-relaxed {
  line-height: 1.625;
}

.inline-block {
  display: inline-block;
}

.px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.py-2 {
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
}

.bg-primary-50 {
  background-color: var(--primary-50);
}

.border-round {
  border-radius: 0.5rem;
}

.text-primary {
  color: var(--primary-color);
}

.block {
  display: block;
}

@media (min-width: 768px) {
  .md\:text-7xl {
    font-size: 4.5rem;
    line-height: 1;
  }
  
  .md\:text-2xl {
    font-size: 1.5rem;
    line-height: 2rem;
  }
  
  .md\:grid-cols-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
