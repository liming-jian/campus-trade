import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  const role = ref(localStorage.getItem('role') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function login(phone, password) {
    const res = await authApi.login(phone, password)
    token.value = res.data.token
    role.value = 'USER'
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('role', role.value)
    return res
  }

  async function register(data) {
    const res = await authApi.register(data)
    token.value = res.data.token
    role.value = 'USER'
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('role', role.value)
    return res
  }

  async function adminLogin(username, password) {
    const res = await authApi.adminLogin(username, password)
    token.value = res.data.token
    role.value = 'ADMIN'
    localStorage.setItem('token', token.value)
    localStorage.setItem('role', role.value)
    return res
  }

  async function fetchUser() {
    try {
      const res = await authApi.getMe()
      user.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    role.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('role')
  }

  return { user, token, role, isLoggedIn, isAdmin, login, register, adminLogin, fetchUser, logout }
})
