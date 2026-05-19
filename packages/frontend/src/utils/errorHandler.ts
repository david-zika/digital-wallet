import type { AxiosError } from 'axios'
import { i18n } from '@/i18n'

interface ErrorResponse {
  code: string
  message: string
}

export const useErrorHandler = () => {
  const { t } = i18n.global

  const handleError = (error: unknown): string => {
    if (error && typeof error === 'object' && 'isAxiosError' in error) {
      const axiosError = error as AxiosError<ErrorResponse>

      if (axiosError.response?.data) {
        const { code, message } = axiosError.response.data

        // Maps backend error codes to i18n translation keys
        const errorMap: Record<string, string> = {
          RECIPIENT_NOT_FOUND: 'errors.recipientNotFound',
          INSUFFICIENT_FUNDS: 'errors.insufficientFunds',
          INVALID_AMOUNT: 'errors.invalidAmount',
          INVALID_CURRENCY: 'errors.invalidCurrency',
          TRANSACTION_FAILED: 'errors.transactionFailed',
        }

        if (code && code in errorMap) {
          return t(errorMap[code])
        }

        if (message) {
          return message
        }
      }

      if (axiosError.message === 'Network Error') {
        return t('errors.networkError')
      }

      return axiosError.message || t('errors.default')
    }

    if (error instanceof Error) {
      return error.message
    }

    return t('errors.default')
  }

  return {
    handleError,
  }
}
