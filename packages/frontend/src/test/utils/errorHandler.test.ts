import { describe, expect, it, vi } from 'vitest'
import { useErrorHandler } from '@/shared/utils/errorHandler'

// Mock i18n
vi.mock('@/core/i18n', () => ({
  i18n: {
    global: {
      t: (key: string) => key,
    },
  },
}))

describe('useErrorHandler', () => {
  const { handleError } = useErrorHandler()

  describe('handleError – Axios chyby', () => {
    it('přeloží RECIPIENT_NOT_FOUND přes title', () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: { title: 'RECIPIENT_NOT_FOUND', detail: 'Recipient not found' },
        },
        message: 'Request failed',
      }
      const result = handleError(axiosError)
      expect(result).toBe('errors.recipientNotFound')
    })

    it('přeloží INSUFFICIENT_FUNDS přes title', () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: { title: 'INSUFFICIENT_FUNDS', detail: 'Not enough money' },
        },
        message: 'Request failed',
      }
      const result = handleError(axiosError)
      expect(result).toBe('errors.insufficientFunds')
    })

    it('přeloží INVALID_AMOUNT přes title', () => {
      const axiosError = {
        isAxiosError: true,
        response: { data: { title: 'INVALID_AMOUNT', detail: 'Bad amount' } },
        message: 'Request failed',
      }
      expect(handleError(axiosError)).toBe('errors.invalidAmount')
    })

    it('přeloží INVALID_CURRENCY přes title', () => {
      const axiosError = {
        isAxiosError: true,
        response: { data: { title: 'INVALID_CURRENCY', detail: 'Bad currency' } },
        message: 'Request failed',
      }
      expect(handleError(axiosError)).toBe('errors.invalidCurrency')
    })

    it('přeloží TRANSACTION_FAILED přes title', () => {
      const axiosError = {
        isAxiosError: true,
        response: { data: { title: 'TRANSACTION_FAILED', detail: 'Failed' } },
        message: 'Request failed',
      }
      expect(handleError(axiosError)).toBe('errors.transactionFailed')
    })

    it('vrátí detail ze serveru, pokud title není v mapě', () => {
      const axiosError = {
        isAxiosError: true,
        response: { data: { title: 'UNKNOWN_CODE', detail: 'Server message' } },
        message: 'Request failed',
      }
      expect(handleError(axiosError)).toBe('Server message')
    })

    it('vrátí errors.networkError při síťové chybě', () => {
      const axiosError = {
        isAxiosError: true,
        response: undefined,
        message: 'Network Error',
      }
      expect(handleError(axiosError)).toBe('errors.networkError')
    })

    it('vrátí axiosError.message při neznámé Axios chybě', () => {
      const axiosError = {
        isAxiosError: true,
        response: undefined,
        message: 'timeout of 5000ms exceeded',
      }
      expect(handleError(axiosError)).toBe('timeout of 5000ms exceeded')
    })

    it('vrátí errors.default pro Axios chybu bez message', () => {
      const axiosError = {
        isAxiosError: true,
        response: { data: {} },
        message: '',
      }
      expect(handleError(axiosError)).toBe('errors.default')
    })
  })

  describe('handleError – standardní Error', () => {
    it('vrátí message z Error instance', () => {
      expect(handleError(new Error('Something went wrong'))).toBe('Something went wrong')
    })
  })

  describe('handleError – neznámé chyby', () => {
    it('vrátí errors.default pro null', () => {
      expect(handleError(null)).toBe('errors.default')
    })

    it('vrátí errors.default pro undefined', () => {
      expect(handleError(undefined)).toBe('errors.default')
    })

    it('vrátí errors.default pro string', () => {
      expect(handleError('something')).toBe('errors.default')
    })
  })
})
