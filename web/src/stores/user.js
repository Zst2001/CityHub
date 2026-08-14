import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, logout as logoutApi } from '../api/user'
import { getToken, removeToken, setToken as persistToken } from '../utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(null)
  const isLoggedIn = computed(() => Boolean(token.value && user.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setToken(value) {
    token.value = value
    persistToken(value)
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    removeToken()
  }

  async function fetchCurrentUser(config = {}) {
    if (!token.value) return null
    const currentUser = await getCurrentUser(config)
    user.value = currentUser
    return currentUser
  }

  async function restoreSession() {
    if (!token.value || user.value) return user.value
    try {
      return await fetchCurrentUser()
    } catch {
      clearAuth()
      return null
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clearAuth()
    }
  }

  return { token, user, isLoggedIn, isAdmin, setToken, clearAuth, fetchCurrentUser, restoreSession, logout }
})
