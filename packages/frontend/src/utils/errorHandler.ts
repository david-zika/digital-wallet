import { i18n } from '@/i18n';
import type { AxiosError } from 'axios';

interface ErrorResponse {
  code: string;
  message: string;
}

export const useErrorHandler = () => {
  const { t } = i18n.global;

  const handleError = (error: unknown): string => {
    // Handle Axios errors
    if (error && typeof error === 'object' && 'isAxiosError' in error) {
      const axiosError = error as AxiosError<ErrorResponse>;

      // Handle response with error data
      if (axiosError.response?.data) {
        const { code, message } = axiosError.response.data;

        // Map backend error codes to translation keys
        const errorMap: Record<string, string> = {
          RECIPIENT_NOT_FOUND: 'errors.recipientNotFound',
          INSUFFICIENT_FUNDS: 'errors.insufficientFunds',
          INVALID_AMOUNT: 'errors.invalidAmount',
          INVALID_CURRENCY: 'errors.invalidCurrency',
          TRANSACTION_FAILED: 'errors.transactionFailed',
        };

        // Use mapped translation key or fallback to code
        if (code && message in errorMap) {
          return t(errorMap[message]);
        }

        // Return the error message if no translation exists
        if (message) {
          return message;
        }
      }

      // Handle network errors
      if (axiosError.message === 'Network Error') {
        return t('errors.networkError');
      }

      return axiosError.message;
    }

    // Handle standard errors
    if (error instanceof Error) {
      return error.message;
    }

    // Default error message
    return t('errors.default');
  };

  return {
    handleError,
  };
};
