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

api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const { handleError } = useErrorHandler()

    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
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


