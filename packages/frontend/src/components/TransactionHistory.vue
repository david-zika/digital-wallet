<script setup lang="ts">
import { format } from 'date-fns'
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWalletStore } from '@/stores/wallet'
import type { Transaction } from '@/types/wallet'

const walletStore = useWalletStore()
const { t } = useI18n()

const pageSizeOptions = [10, 50, 100]

const handleFilterChange = () => {
  walletStore.updateFilters({})
}

const handlePageSizeChange = (newSize: number) => {
  walletStore.updateFilters({ pageSize: newSize })
}

const getTransactionReference = (transaction: Transaction) => {
  if (transaction.paymentReference) {
    return transaction.paymentReference
  }
  if (transaction.recipientAccount) {
    return `${t('wallet.transactions.transferTo')} ${transaction.recipientAccount}`
  }
  return '-'
}

onMounted(() => {
  walletStore.fetchTransactions()
})
</script>

<template>
  <div class="card">
    <div class="card-body">
      <h3 class="card-title h5 mb-4">{{ t('wallet.transactions.title') }}</h3>

      <!-- Tabs -->
      <ul class="nav nav-tabs mb-4">
        <li class="nav-item">
          <a
            class="nav-link"
            :class="{ active: walletStore.selectedType === 'TRANSFER' }"
            href="#"
            @click.prevent="walletStore.updateFilters({ type: 'TRANSFER' })"
          >
            {{ t('wallet.transactions.tabs.transfers') }}
          </a>
        </li>
        <li class="nav-item">
          <a
            class="nav-link"
            :class="{ active: walletStore.selectedType === 'EXTERNAL' }"
            href="#"
            @click.prevent="walletStore.updateFilters({ type: 'EXTERNAL' })"
          >
            {{ t('wallet.transactions.tabs.external') }}
          </a>
        </li>
      </ul>

      <!-- Filters -->
      <div class="row g-3 mb-4">
        <div class="col-md-3">
          <label class="form-label">{{ t('wallet.transactions.filters.amountFrom') }}</label>
          <input
            v-model.number="walletStore.amountFrom"
            type="number"
            step="0.01"
            class="form-control"
            @input="handleFilterChange"
          />
        </div>
        <div class="col-md-3">
          <label class="form-label">{{ t('wallet.transactions.filters.amountTo') }}</label>
          <input
            v-model.number="walletStore.amountTo"
            type="number"
            step="0.01"
            class="form-control"
            @input="handleFilterChange"
          />
        </div>
        <div class="col-md-4">
          <label class="form-label">{{ t('wallet.transactions.filters.reference') }}</label>
          <input
            v-model="walletStore.searchReference"
            type="text"
            class="form-control"
            @input="handleFilterChange"
          />
        </div>
        <div class="col-md-2">
          <label class="form-label">{{ t('wallet.transactions.pageSize') }}</label>
          <select
            v-model="walletStore.pageSize"
            class="form-select"
            @change="handlePageSizeChange(walletStore.pageSize)"
          >
            <option v-for="size in pageSizeOptions" :key="size" :value="size">
              {{ size }}
            </option>
          </select>
        </div>
      </div>

      <!-- Transactions table -->
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>{{ t('wallet.transactions.date') }}</th>
              <th>{{ t('wallet.transactions.type') }}</th>
              <th>{{ t('wallet.transactions.amount') }}</th>
              <th>{{ t('wallet.transactions.status') }}</th>
              <th>{{ t('wallet.transactions.reference') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="transaction in walletStore.transactions" :key="transaction.id">
              <td>
                {{ format(new Date(transaction.createdAt), 'dd.MM.yyyy HH:mm') }}
              </td>
              <td>{{ t(`wallet.transactions.types.${transaction.type}`) }}</td>
              <td :class="transaction.type === 'DEPOSIT' ? 'amount-deposit' : 'amount-withdrawal'">
                {{ transaction.type === 'DEPOSIT' ? '+' : '-' }}
                {{ transaction.amount.toFixed(2) }}
                {{ transaction.currency }}
              </td>
              <td>
                <span
                  class="badge rounded-pill"
                  :class="{
                    'transaction-pending': transaction.status === 'PENDING',
                    'transaction-completed': transaction.status === 'COMPLETED',
                    'transaction-failed': transaction.status === 'FAILED',
                  }"
                >
                  {{ t(`wallet.transactions.statuses.${transaction.status}`) }}
                </span>
              </td>
              <td class="text-muted">
                {{ getTransactionReference(transaction) }}
              </td>
            </tr>
            <tr v-if="walletStore.transactions.length === 0">
              <td colspan="5" class="text-center py-4">
                {{ t('wallet.transactions.noTransactions') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="d-flex justify-content-between align-items-center mt-4">
        <div>
          {{
            t('wallet.transactions.showing', {
              from: (walletStore.currentPage - 1) * walletStore.pageSize + 1,
              to: Math.min(
                walletStore.currentPage * walletStore.pageSize,
                walletStore.totalTransactions
              ),
              total: walletStore.totalTransactions,
            })
          }}
        </div>
        <nav v-if="walletStore.totalPages > 1">
          <ul class="pagination mb-0">
            <li class="page-item" :class="{ disabled: walletStore.currentPage === 1 }">
              <a
                class="page-link"
                href="#"
                @click.prevent="walletStore.updateFilters({ page: walletStore.currentPage - 1 })"
              >
                {{ t('wallet.transactions.previous') }}
              </a>
            </li>
            <li
              v-for="page in walletStore.totalPages"
              :key="page"
              class="page-item"
              :class="{ active: page === walletStore.currentPage }"
            >
              <a class="page-link" href="#" @click.prevent="walletStore.updateFilters({ page })">
                {{ page }}
              </a>
            </li>
            <li
              class="page-item"
              :class="{ disabled: walletStore.currentPage === walletStore.totalPages }"
            >
              <a
                class="page-link"
                href="#"
                @click.prevent="walletStore.updateFilters({ page: walletStore.currentPage + 1 })"
              >
                {{ t('wallet.transactions.next') }}
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>
