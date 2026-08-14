import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/storage'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/', component: DefaultLayout,
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'activities', name: 'activities', component: () => import('../views/ActivityListView.vue') },
      { path: 'activities/:id', name: 'activity-detail', component: () => import('../views/ActivityDetailView.vue') },
      { path: 'community', name: 'community', component: () => import('../views/CommunityView.vue') },
      { path: 'assistant', name: 'assistant', component: () => import('../views/AssistantView.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue'), meta: { requiresAuth: true } },
    ],
  },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/admin/activities', name: 'admin-activities', component: () => import('../views/AdminActivitiesView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') },
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !getToken()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (getToken() && !userStore.user) await userStore.restoreSession()
  if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') return { name: 'home' }
  return true
})

export default router
