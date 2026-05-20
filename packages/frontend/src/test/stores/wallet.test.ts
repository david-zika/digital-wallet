import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useWalletStore } from '@/features/wallet/stores/wallet'

vi.mock('@/core/services/api', () => ({
  wallet: {
    getBalances: vi.fn(),
    getTransactions: vi.fn(),
    createTransaction: vi.fn(),
  },
}))

import { wallet as walletApi } from '@/core/services/api'

const mockBalances = [
  { currency: 'EUR', balance: 1000.0, lastUpdated: '2026-01-01T00:00:00Z' },
  { currency: 'CZK', balance: 25000.0, lastUpdated: '2026-01-01T00:00:00Z' },
]

const mockTransactionsResponse = {
  transactions: [
    {
      id: '1',
      amount: 100,
      currency: 'EUR',
      type: 'DEPOSIT',
      status: 'COMPLETED',
      createdAt: '2026-01-01T10:00:00Z',
    },
    {
      id: '2',
      amount: 50,
      currency: 'EUR',
      type: 'WITHDRAWAL',
      status: 'PENDING',
      createdAt: '2026-01-02T10:00:00Z',
    },
  ],
  total: 2,
}

describe('useWalletStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(walletApi.getBalances).mockResolvedValue({ data: mockBalances } as never)
    vi.mocked(walletApi.getTransactions).mockResolvedValue({
      data: mockTransactionsResponse,
    } as never)
  })

  describe('počáteční stav', () => {
    it('balances je prázdné pole', () => {
      const store = useWalletStore()
      expect(store.balances).toHaveLength(0)
    })

    it('isLoading je false', () => {
      const store = useWalletStore()
      expect(store.isLoading).toBe(false)
    })

    it('isInitialized je false', () => {
      const store = useWalletStore()
      expect(store.isInitialized).toBe(false)
    })
  })

  describe('getBalance', () => {
    it('vrátí 0 pro neexistující měnu', () => {
      const store = useWalletStore()
      expect(store.getBalance('EUR')).toBe(0)
    })

    it('vrátí správný zůstatek po načtení', async () => {
      const store = useWalletStore()
      await store.fetchBalances()
      expect(store.getBalance('EUR')).toBe(1000.0)
      expect(store.getBalance('CZK')).toBe(25000.0)
    })
  })

  describe('fetchBalances', () => {
    it('načte zůstatky z API', async () => {
      const store = useWalletStore()
      await store.fetchBalances()
      expect(store.balances).toHaveLength(2)
      expect(store.balances[0].currency).toBe('EUR')
    })

    it('nastaví error při chybě API', async () => {
      vi.mocked(walletApi.getBalances).mockRejectedValue(new Error('API Error'))
      const store = useWalletStore()
      await expect(store.fetchBalances()).rejects.toThrow()
      expect(store.error).toBe('API Error')
    })
  })

  describe('fetchTransactions', () => {
    it('načte transakce z API', async () => {
      const store = useWalletStore()
      await store.fetchTransactions()
      expect(store.transactions).toHaveLength(2)
      expect(store.totalTransactions).toBe(2)
    })

    it('vypočítá totalPages správně', async () => {
      vi.mocked(walletApi.getTransactions).mockResolvedValue({
        data: { transactions: [], total: 25 },
      } as never)
      const store = useWalletStore()
      await store.fetchTransactions()
      expect(store.totalPages).toBe(3) // 25 / 10 = 2.5 → 3
    })
  })

  describe('initialize', () => {
    it('nastaví isInitialized na true po úspěšném načtení', async () => {
      const store = useWalletStore()
      await store.initialize()
      expect(store.isInitialized).toBe(true)
    })

    it('nevolá API podruhé pokud isInitialized je true', async () => {
      const store = useWalletStore()
      await store.initialize()
      await store.initialize()
      expect(walletApi.getBalances).toHaveBeenCalledTimes(1)
    })
  })

  describe('updateFilters', () => {
    it('resetuje stránku na 1 při změně filtru', async () => {
      const store = useWalletStore()
      await store.updateFilters({ page: 3 })
      expect(store.currentPage).toBe(3)
      await store.updateFilters({ reference: 'TEST' })
      expect(store.currentPage).toBe(1)
    })

    it('neresetuje stránku pokud je explicitně nastavena', async () => {
      const store = useWalletStore()
      await store.updateFilters({ page: 5 })
      expect(store.currentPage).toBe(5)
    })

    it('aktualizuje searchReference', async () => {
      const store = useWalletStore()
      await store.updateFilters({ reference: 'ACC-123' })
      expect(store.searchReference).toBe('ACC-123')
    })
  })

  describe('resetFilters', () => {
    it('resetuje všechny filtry na výchozí hodnoty', async () => {
      const store = useWalletStore()
      store.$patch({
        currentPage: 5,
        pageSize: 50,
        amountFrom: 100,
        amountTo: 500,
        searchReference: 'TEST',
        selectedType: 'EXTERNAL',
      })

      await store.resetFilters()

      expect(store.currentPage).toBe(1)
      expect(store.pageSize).toBe(10)
      expect(store.amountFrom).toBeUndefined()
      expect(store.amountTo).toBeUndefined()
      expect(store.searchReference).toBe('')
      expect(store.selectedType).toBe('TRANSFER')
    })
  })

  describe('createTransaction', () => {
    beforeEach(async () => {
      const store = useWalletStore()
      await store.fetchBalances()
      vi.mocked(walletApi.createTransaction).mockResolvedValue({ data: {} } as never)
    })

    it('odhodí chybu pro amount <= 0', async () => {
      const store = useWalletStore()
      await expect(store.createTransaction(0, 'EUR', 'DEPOSIT')).rejects.toThrow(
        'Amount must be greater than 0'
      )
    })

    it('odhodí chybu pro zápornou částku', async () => {
      const store = useWalletStore()
      await expect(store.createTransaction(-50, 'EUR', 'DEPOSIT')).rejects.toThrow()
    })

    it('odhodí chybu pro WITHDRAWAL s nedostatkem prostředků', async () => {
      const store = useWalletStore()
      await expect(store.createTransaction(9999, 'EUR', 'WITHDRAWAL')).rejects.toThrow(
        'Insufficient balance'
      )
    })

    it('úspěšně vytvoří DEPOSIT transakci', async () => {
      const store = useWalletStore()
      await store.createTransaction(100, 'EUR', 'DEPOSIT')
      expect(walletApi.createTransaction).toHaveBeenCalledWith(
        100,
        'EUR',
        'DEPOSIT',
        undefined,
        undefined,
        undefined
      )
    })

    it('úspěšně vytvoří WITHDRAWAL transakci s dostatkem prostředků', async () => {
      const store = useWalletStore()
      await store.createTransaction(500, 'EUR', 'WITHDRAWAL', 'ACC-789', 'Příjemce', 'REF123')
      expect(walletApi.createTransaction).toHaveBeenCalledWith(
        500,
        'EUR',
        'WITHDRAWAL',
        'ACC-789',
        'Příjemce',
        'REF123'
      )
    })

    it('aktualizuje zůstatky a transakce po vytvoření', async () => {
      const store = useWalletStore()
      await store.createTransaction(100, 'EUR', 'DEPOSIT')
      expect(walletApi.getBalances).toHaveBeenCalledTimes(2) // 1x fetchBalances na začátku + 1x po transakci
      expect(walletApi.getTransactions).toHaveBeenCalled()
    })

    it('nastaví isLoading na false i po chybě API', async () => {
      vi.mocked(walletApi.createTransaction).mockRejectedValue(new Error('Server Error'))
      const store = useWalletStore()
      try {
        await store.createTransaction(100, 'EUR', 'DEPOSIT')
      } catch {}
      expect(store.isLoading).toBe(false)
    })
  })
})
