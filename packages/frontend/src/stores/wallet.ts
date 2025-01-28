import { defineStore } from 'pinia';
import { ref } from 'vue';
import { wallet } from '@/services/api';
import type { Transaction, WalletBalance } from '@/types/wallet';
import { Decimal } from 'decimal.js';

export type TransactionType = 'TRANSFER' | 'EXTERNAL';

interface TransactionFilters {
  page: number;
  pageSize: number;
  amountFrom?: number;
  amountTo?: number;
  reference?: string;
  type: TransactionType;
}

export const useWalletStore = defineStore('wallet', () => {
  // State
  const balances = ref<WalletBalance[]>([]);
  const transactions = ref<Transaction[]>([]);
  const totalTransactions = ref(0);
  const totalPages = ref(0);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const isInitialized = ref(false);

  // Transaction filters state
  const currentPage = ref(1);
  const pageSize = ref(10);
  const amountFrom = ref<number | undefined>();
  const amountTo = ref<number | undefined>();
  const searchReference = ref('');
  const selectedType = ref<TransactionType>('TRANSFER');

  // Computed
  const getBalance = (currency: 'EUR' | 'CZK') => {
    const balance = balances.value.find(b => b.currency === currency);
    return balance?.balance || 0;
  };

  // Actions
  const initialize = async () => {
    if (isInitialized.value) return;

    try {
      isLoading.value = true;
      await Promise.all([fetchBalances(), fetchTransactions()]);
      isInitialized.value = true;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to initialize wallet data';
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const fetchBalances = async () => {
    try {
      const response = await wallet.getBalances();
      balances.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch balances';
      throw err;
    }
  };

  const fetchTransactions = async () => {
    try {
      isLoading.value = true;
      const filters: TransactionFilters = {
        page: currentPage.value,
        pageSize: pageSize.value,
        amountFrom: amountFrom.value,
        amountTo: amountTo.value,
        reference: searchReference.value || undefined,
        type: selectedType.value,
      };

      const response = await wallet.getTransactions(filters);
      transactions.value = response.data.transactions;
      totalTransactions.value = response.data.total;
      totalPages.value = Math.ceil(response.data.total / pageSize.value);
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch transactions';
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const updateFilters = async (filters: Partial<TransactionFilters>) => {
    // Update filter values
    if (filters.page !== undefined) currentPage.value = filters.page;
    if (filters.pageSize !== undefined) pageSize.value = filters.pageSize;
    if (filters.amountFrom !== undefined) amountFrom.value = filters.amountFrom;
    if (filters.amountTo !== undefined) amountTo.value = filters.amountTo;
    if (filters.reference !== undefined) searchReference.value = filters.reference;
    if (filters.type !== undefined) selectedType.value = filters.type;

    // Reset to first page when filters change (except when page is explicitly set)
    if (filters.page === undefined) {
      currentPage.value = 1;
    }

    // Fetch transactions with new filters
    await fetchTransactions();
  };

  const resetFilters = async () => {
    currentPage.value = 1;
    pageSize.value = 10;
    amountFrom.value = undefined;
    amountTo.value = undefined;
    searchReference.value = '';
    selectedType.value = 'TRANSFER';
    await fetchTransactions();
  };

  const createTransaction = async (
    amount: number,
    currency: 'EUR' | 'CZK',
    type: 'DEPOSIT' | 'WITHDRAWAL',
    recipientAccount?: string,
    recipientName?: string,
    paymentReference?: string,
    isDemoMode = false
  ) => {
    try {
      isLoading.value = true;
      error.value = null;

      if (amount <= 0) {
        throw new Error('Amount must be greater than 0');
      }

      if (type === 'WITHDRAWAL') {
        const currentBalance = getBalance(currency);
        if (new Decimal(currentBalance).lessThan(amount)) {
          throw new Error('Insufficient balance');
        }
      }

      await wallet.createTransaction(
        amount,
        currency,
        type,
        recipientAccount,
        recipientName,
        paymentReference,
        isDemoMode
      );

      await Promise.all([fetchBalances(), fetchTransactions()]);
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Transaction failed';
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    // State
    balances,
    transactions,
    totalTransactions,
    totalPages,
    isLoading,
    error,
    isInitialized,

    // Filter state
    currentPage,
    pageSize,
    amountFrom,
    amountTo,
    searchReference,
    selectedType,

    // Methods
    getBalance,
    initialize,
    fetchBalances,
    fetchTransactions,
    updateFilters,
    resetFilters,
    createTransaction,
  };
});
