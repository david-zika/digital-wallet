import { jwtDecode } from 'jwt-decode'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { auth } from '@/services/api'

interface JWTPayload {
  sub: string
  accountReference: string
  exp: number
}

interface Profile {
  fullName: string | null
  bankAccount: string | null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const profile = ref<Profile | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const isAuthenticated = computed(() => !!token.value)

  const userEmail = computed(() => {
    if (!token.value) return null
    try {
      const decoded = jwtDecode<JWTPayload>(token.value)
      return decoded.sub
    } catch {
      return null
    }
  })

  const accountReference = computed(() => {
    if (!token.value) return null
    try {
      const decoded = jwtDecode<JWTPayload>(token.value)
      return decoded.accountReference
    } catch {
      return null
    }
  })

  const register = async (email: string, password: string, fullName: string) => {
    try {
      error.value = null
      const response = await auth.register({ email, password, fullName })
      const newToken = response.data.token
      if (typeof newToken !== 'string') {
        throw new Error('Invalid token received')
      }
      token.value = newToken
      localStorage.setItem('token', newToken)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Registration failed'
      throw err
    }
  }

  const login = async (email: string, password: string) => {
    try {
      error.value = null
      const response = await auth.login({ email, password })
      const newToken = response.data.token
      if (typeof newToken !== 'string') {
        throw new Error('Invalid token received')
      }
      token.value = newToken
      localStorage.setItem('token', newToken)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Login failed'
      throw err
    }
  }

  const updateProfile = async (data: Partial<Profile>) => {
    try {
      const response = await auth.updateProfile(data)
      profile.value = response.data

      return response.data
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update profile'
      throw err
    }
  }

  const changePassword = async (currentPassword: string, newPassword: string) => {
    try {
      error.value = null
      await auth.changePassword({ currentPassword, newPassword })
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Password change failed'
      throw err
    }
  }

  const getProfile = async () => {
    try {
      isLoading.value = true
      const response = await auth.getProfile()
      profile.value = {
        fullName: response.data.fullName,
        bankAccount: response.data.bankAccount,
      }
      return response.data
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to load profile'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const logout = () => {
    token.value = null
    profile.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    isAuthenticated,
    error,
    profile,
    userEmail,
    accountReference,
    register,
    login,
    logout,
    getProfile,
    updateProfile,
    changePassword,
  }
})
