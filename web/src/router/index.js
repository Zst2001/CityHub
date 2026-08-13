import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/storage'
import DefaultLayout from '../layouts/DefaultLayout.vue'

const routes = [
  {
    path: '/', component: DefaultLayout,
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'activities', name: 'activities', component: () => import('../views/ActivityListView.vue') },
      { path: 'activities/:id', name: 'activity-detail', component: () => import('../views/ActivityDetailView.vue') },
      { path: 'community', name: 'community', component: () => import('../views/CommunityView.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue'), meta: { requiresAuth: true } },
    ],
  },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') },
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
