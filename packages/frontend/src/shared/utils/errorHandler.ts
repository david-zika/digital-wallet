import type { AxiosError } from 'axios'
import { i18n } from '@/core/i18n'

/** RFC 9457 Problem Detail response (Spring ProblemDetail) */
interface ProblemDetail {
  status: number
  title?: string
  detail?: string
  code?: string
  // legacy fallback – kept for backwards compatibility
  message?: string
}

export const useErrorHandler = () => {
  const { t } = i18n.global

  const handleError = (error: unknown): string => {
    if (error && typeof error === 'object' && 'isAxiosError' in error) {
      const axiosError = error as AxiosError<ProblemDetail>

      if (axiosError.response?.data) {
        const { title, detail, message } = axiosError.response.data
        const errorText = detail || message

        // Map backend error-code names (ProblemDetail.title) to i18n keys
        const errorMap: Record<string, string> = {
          RECIPIENT_NOT_FOUND: 'errors.recipientNotFound',
          INSUFFICIENT_FUNDS: 'errors.insufficientFunds',
          INVALID_AMOUNT: 'errors.invalidAmount',
          INVALID_CURRENCY: 'errors.invalidCurrency',
          TRANSACTION_FAILED: 'errors.transactionFailed',
        }

        if (title && title in errorMap) {
          return t(errorMap[title])
        }

        if (errorText) {
          return errorText
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
