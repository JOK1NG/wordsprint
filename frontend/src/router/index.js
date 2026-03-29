import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '../layout/AppLayout.vue'
import pinia from '../stores'
import { useUserStore } from '../stores/user'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import StudyTrainingView from '../views/study/StudyTrainingView.vue'
import WordCardListView from '../views/wordcard/WordCardListView.vue'
import WordCardFormView from '../views/wordcard/WordCardFormView.vue'
import { getToken } from '../utils/token'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView,
        },
        {
          path: 'word-cards',
          name: 'wordCardList',
          component: WordCardListView,
        },
        {
          path: 'word-cards/create',
          name: 'wordCardCreate',
          component: WordCardFormView,
        },
        {
          path: 'word-cards/edit/:id',
          name: 'wordCardEdit',
          component: WordCardFormView,
        },
        {
          path: 'study',
          name: 'studyTraining',
          component: StudyTrainingView,
        },
        {
          path: 'wrong-words',
          name: 'wrongWordList',
          component: () => import('../views/wrongword/WrongWordListView.vue'),
        },
        {
          path: 'rank',
          name: 'rank',
          component: () => import('../views/rank/RankView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const token = getToken()
  const userStore = useUserStore(pinia)

  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && token) {
    if (!userStore.userInfo) {
      try {
        await userStore.fetchCurrentUser()
      } catch {
        return { name: 'login' }
      }
    }

    return { name: 'dashboard' }
  }

  if (to.meta.requiresAuth && token && !userStore.userInfo) {
    try {
      await userStore.fetchCurrentUser()
    } catch {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }

  return true
})

export default router
