import { jwtDecode } from 'jwt-decode'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { auth } from '@/core/services/api'

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
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
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

  function storeTokenPair(newToken: string, newRefreshToken: string) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    localStorage.setItem('token', newToken)
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  const register = async (email: string, password: string, fullName: string) => {
    try {
      isLoading.value = true
      error.value = null
      const response = await auth.register({ email, password, fullName })
      const { token: newToken, refreshToken: newRefresh } = response.data
      if (typeof newToken !== 'string') {
        error.value = 'Invalid token received'
        return
      }
      storeTokenPair(newToken, newRefresh)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Registration failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const login = async (email: string, password: string) => {
    try {
      isLoading.value = true
      error.value = null
      const response = await auth.login({ email, password })
      const { token: newToken, refreshToken: newRefresh } = response.data
      if (typeof newToken !== 'string') {
        error.value = 'Invalid token received'
        return
      }
      storeTokenPair(newToken, newRefresh)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const refreshAccessToken = async (): Promise<boolean> => {
    const rt = refreshToken.value
    if (!rt) return false
    try {
      const response = await auth.refresh(rt)
      const { token: newToken, refreshToken: newRefresh } = response.data
      storeTokenPair(newToken, newRefresh)
      return true
    } catch {
      logout()
      return false
    }
  }

  const updateProfile = async (data: Partial<Profile>) => {
    try {
      isLoading.value = true
      const response = await auth.updateProfile(data)
      profile.value = response.data
      return response.data
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update profile'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const changePassword = async (currentPassword: string, newPassword: string) => {
    try {
      isLoading.value = true
      error.value = null
      await auth.changePassword({ currentPassword, newPassword })
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Password change failed'
      throw err
    } finally {
      isLoading.value = false
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
    const rt = refreshToken.value
    token.value = null
    refreshToken.value = null
    profile.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    if (rt)
      auth.logout(rt).catch(() => {
        /* best-effort */
      })
  }

  return {
    token,
    refreshToken,
    isAuthenticated,
    isLoading,
    error,
    profile,
    userEmail,
    accountReference,
    register,
    login,
    logout,
    refreshAccessToken,
    getProfile,
    updateProfile,
    changePassword,
  }
})
