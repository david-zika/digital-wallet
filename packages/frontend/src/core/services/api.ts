import type { AxiosRequestConfig } from 'axios'
import axios from 'axios'
import { useErrorHandler } from '@/shared/utils/errorHandler'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    const { handleError } = useErrorHandler()
    return Promise.reject(handleError(error))
  }
)

let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

function onRefreshSuccess(newToken: string) {
  for (const cb of refreshSubscribers) {
    cb(newToken)
  }
  refreshSubscribers = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { handleError } = useErrorHandler()
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    const isAuthEndpoint =
      originalRequest.url?.includes('/auth/login') ||
      originalRequest.url?.includes('/auth/register') ||
      originalRequest.url?.includes('/auth/refresh')

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      const refreshToken = localStorage.getItem('refreshToken')

      if (refreshToken) {
        if (isRefreshing) {
          return new Promise((resolve) => {
            subscribeTokenRefresh((newToken) => {
              originalRequest.headers = {
                ...originalRequest.headers,
                Authorization: `Bearer ${newToken}`,
              }
              resolve(api(originalRequest))
            })
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const response = await api.post('/auth/refresh', { refreshToken })
          const { token: newToken, refreshToken: newRefreshToken } = response.data
          localStorage.setItem('token', newToken)
          localStorage.setItem('refreshToken', newRefreshToken)
          api.defaults.headers.common.Authorization = `Bearer ${newToken}`
          onRefreshSuccess(newToken)
          isRefreshing = false
          originalRequest.headers = {
            ...originalRequest.headers,
            Authorization: `Bearer ${newToken}`,
          }
          return api(originalRequest)
        } catch {
          isRefreshing = false
          refreshSubscribers = []
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          window.location.href = '/'
        }
      } else {
        localStorage.removeItem('token')
        window.location.href = '/'
      }
    }

    if (error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      window.location.href = '/'
    }

    return Promise.reject(handleError(error))
  }
)

interface TransactionFilters {
  page?: number
  pageSize?: number
  amountFrom?: number
  amountTo?: number
  reference?: string
  type?: 'TRANSFER' | 'EXTERNAL'
}

interface Profile {
  email: string
  accountReference: string
  fullName: string | null
  bankAccount: string | null
}

export const auth = {
  register: (data: { email: string; password: string; fullName: string }) =>
    api.post('/auth/register', data),

  login: (data: { email: string; password: string }) => api.post('/auth/login', data),

  refresh: (refreshToken: string) => api.post('/auth/refresh', { refreshToken }),

  logout: (refreshToken: string) => api.post('/auth/logout', { refreshToken }),

  getProfile: () => api.get<Profile>('/auth/profile'),

  updateProfile: (data: Partial<Profile>) => api.put<Profile>('/auth/profile', data),

  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    api.post('/auth/change-password', data),
}

export const wallet = {
  getBalances: () => api.get('/wallet/balances'),

  getTransactions: (filters?: TransactionFilters) =>
    api.get('/wallet/transactions', {
      params: {
        page: filters?.page || 1,
        size: filters?.pageSize || 10,
        amountFrom: filters?.amountFrom,
        amountTo: filters?.amountTo,
        reference: filters?.reference,
        type: filters?.type,
      },
    }),

  createTransaction: (
    amount: number,
    currency: string,
    type: string,
    recipientAccount?: string,
    recipientName?: string,
    paymentReference?: string
  ) =>
    api.post('/wallet/transactions', {
      amount,
      currency,
      type,
      recipientAccount,
      recipientName,
      paymentReference,
    }),
}
