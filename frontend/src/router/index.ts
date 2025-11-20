import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Feed from '@/views/Feed.vue'
import Quiz from '@/views/Quiz.vue'
import Profile from '@/views/Profile.vue'
import Login from '@/views/auth/Login.vue'
import Signup from '@/views/auth/Signup.vue'
import Create from '@/views/Create.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/feed',
      name: 'feed',
      component: Feed,
    },
    {
      path: '/quiz/:id',
      name: 'quiz',
      component: Quiz,
      props: true,
    },
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
    },
    {
      path: '/auth/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/auth/signup',
      name: 'signup',
      component: Signup,
    },
    {
      path: '/create',
      name: 'create',
      component: Create,
    },
  ],
})

export default router

