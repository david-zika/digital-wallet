import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useAuthStore } from '@/features/auth/stores/auth'

// Mock API modulu
vi.mock('@/core/services/api', () => ({
  auth: {
    login: vi.fn(),
    register: vi.fn(),
    getProfile: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn(),
  },
}))

import { auth as authApi } from '@/core/services/api'

const mockToken =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
  btoa(JSON.stringify({ sub: 'test@example.com', accountReference: 'ACC-123', exp: 9999999999 })) +
  '.signature'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('počáteční stav', () => {
    it('isAuthenticated je false při prázdném localStorage', () => {
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(false)
    })

    it('isAuthenticated je true pokud je token v localStorage', () => {
      localStorage.setItem('token', mockToken)
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(true)
    })

    it('isLoading je false na začátku', () => {
      const store = useAuthStore()
      expect(store.isLoading).toBe(false)
    })

    it('error je null na začátku', () => {
      const store = useAuthStore()
      expect(store.error).toBeNull()
    })
  })

  describe('login', () => {
    it('uloží token po úspěšném přihlášení', async () => {
      vi.mocked(authApi.login).mockResolvedValue({ data: { token: mockToken } } as never)
      const store = useAuthStore()
      await store.login('test@example.com', 'password')
      expect(store.token).toBe(mockToken)
      expect(localStorage.getItem('token')).toBe(mockToken)
      expect(store.isAuthenticated).toBe(true)
    })

    it('nastaví isLoading na true během přihlašování a false po dokončení', async () => {
      const loadingStates: boolean[] = []
      vi.mocked(authApi.login).mockImplementation(async () => {
        await new Promise((r) => setTimeout(r, 0))
        return { data: { token: mockToken } } as never
      })
      const store = useAuthStore()
      const promise = store.login('test@example.com', 'password')
      loadingStates.push(store.isLoading) // true during async
      await promise
      loadingStates.push(store.isLoading) // false after
      expect(loadingStates[0]).toBe(true)
      expect(loadingStates[1]).toBe(false)
    })

    it('nastaví error a hodí výjimku při neúspěšném přihlášení', async () => {
      vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid credentials'))
      const store = useAuthStore()
      await expect(store.login('bad@example.com', 'wrong')).rejects.toThrow('Invalid credentials')
      expect(store.error).toBe('Invalid credentials')
      expect(store.isAuthenticated).toBe(false)
    })

    it('isLoading je false i po chybě', async () => {
      vi.mocked(authApi.login).mockRejectedValue(new Error('Network Error'))
      const store = useAuthStore()
      try {
        await store.login('test@example.com', 'password')
      } catch {}
      expect(store.isLoading).toBe(false)
    })
  })

  describe('register', () => {
    it('uloží token po úspěšné registraci', async () => {
      vi.mocked(authApi.register).mockResolvedValue({ data: { token: mockToken } } as never)
      const store = useAuthStore()
      await store.register('new@example.com', 'password', 'Jan Novák')
      expect(store.token).toBe(mockToken)
      expect(store.isAuthenticated).toBe(true)
    })

    it('nastaví error při neúspěšné registraci', async () => {
      vi.mocked(authApi.register).mockRejectedValue(new Error('Email already exists'))
      const store = useAuthStore()
      await expect(store.register('existing@example.com', 'pass', 'Test')).rejects.toThrow()
      expect(store.error).toBe('Email already exists')
    })
  })

  describe('logout', () => {
    it('odstraní token a nastaví isAuthenticated na false', async () => {
      vi.mocked(authApi.login).mockResolvedValue({ data: { token: mockToken } } as never)
      const store = useAuthStore()
      await store.login('test@example.com', 'password')
      expect(store.isAuthenticated).toBe(true)

      store.logout()
      expect(store.token).toBeNull()
      expect(store.isAuthenticated).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
    })

    it('odstraní profil při odhlášení', async () => {
      vi.mocked(authApi.login).mockResolvedValue({ data: { token: mockToken } } as never)
      vi.mocked(authApi.getProfile).mockResolvedValue({
        data: { fullName: 'Jan Novák', bankAccount: '123456/0100', email: 'test@example.com', accountReference: 'ACC-123' },
      } as never)
      const store = useAuthStore()
      await store.login('test@example.com', 'password')
      await store.getProfile()
      expect(store.profile).not.toBeNull()

      store.logout()
      expect(store.profile).toBeNull()
    })
  })

  describe('computed hodnoty z JWT', () => {
    it('userEmail vrátí sub z tokenu', () => {
      localStorage.setItem('token', mockToken)
      const store = useAuthStore()
      expect(store.userEmail).toBe('test@example.com')
    })

    it('accountReference vrátí accountReference z tokenu', () => {
      localStorage.setItem('token', mockToken)
      const store = useAuthStore()
      expect(store.accountReference).toBe('ACC-123')
    })

    it('userEmail vrátí null bez tokenu', () => {
      const store = useAuthStore()
      expect(store.userEmail).toBeNull()
    })
  })

  describe('getProfile', () => {
    it('načte a uloží profil', async () => {
      vi.mocked(authApi.login).mockResolvedValue({ data: { token: mockToken } } as never)
      vi.mocked(authApi.getProfile).mockResolvedValue({
        data: { fullName: 'Jan Novák', bankAccount: 'CZ1234567890', email: 'test@example.com', accountReference: 'ACC-123' },
      } as never)
      const store = useAuthStore()
      await store.login('test@example.com', 'password')
      await store.getProfile()
      expect(store.profile?.fullName).toBe('Jan Novák')
      expect(store.profile?.bankAccount).toBe('CZ1234567890')
    })

    it('nastaví isLoading na false i po chybě', async () => {
      vi.mocked(authApi.getProfile).mockRejectedValue(new Error('Unauthorized'))
      const store = useAuthStore()
      try {
        await store.getProfile()
      } catch {}
      expect(store.isLoading).toBe(false)
    })
  })

  describe('changePassword', () => {
    it('zavolá API a nastaví isLoading správně', async () => {
      vi.mocked(authApi.changePassword).mockResolvedValue({ data: {} } as never)
      const store = useAuthStore()
      await store.changePassword('oldPass', 'newPass')
      expect(authApi.changePassword).toHaveBeenCalledWith({
        currentPassword: 'oldPass',
        newPassword: 'newPass',
      })
      expect(store.isLoading).toBe(false)
    })

    it('hodí výjimku při chybě', async () => {
      vi.mocked(authApi.changePassword).mockRejectedValue(new Error('Wrong password'))
      const store = useAuthStore()
      await expect(store.changePassword('wrong', 'new')).rejects.toThrow('Wrong password')
      expect(store.isLoading).toBe(false)
    })
  })
})

