<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useNotificationStore } from '@/stores/notification.ts'
import { useWalletStore } from '@/stores/wallet'

const notificationStore = useNotificationStore()
const walletStore = useWalletStore()
const { t } = useI18n()

const amount = ref('')
const currency = ref<'EUR' | 'CZK'>('EUR')
const recipientAccount = ref('')
const variableSymbol = ref('')
const error = ref('')

const currentBalance = computed(() => walletStore.getBalance(currency.value))
const isInsufficientBalance = computed(() => {
  const amountNum = parseFloat(amount.value)
  return !Number.isNaN(amountNum) && amountNum > currentBalance.value
})

const handleSubmit = async () => {
  try {
    error.value = ''
    const amountNum = parseFloat(amount.value)

    if (Number.isNaN(amountNum) || amountNum <= 0) {
      throw new Error(t('wallet.transfer.invalidAmount'))
    }

    if (isInsufficientBalance.value) {
      throw new Error(t('wallet.transfer.insufficientBalance'))
    }

    if (!recipientAccount.value) {
      throw new Error(t('wallet.transfer.recipientAccountRequired'))
    }

    if (!recipientAccount.value.startsWith('ACC-')) {
      throw new Error(t('wallet.transfer.invalidAccountFormat'))
    }

    await walletStore.createTransaction(
      amountNum,
      currency.value,
      'WITHDRAWAL',
      recipientAccount.value,
      undefined,
      variableSymbol.value
    )

    notificationStore.addNotification(t('notifications.success.transferCreated'))

    amount.value = ''
    recipientAccount.value = ''
    variableSymbol.value = ''
  } catch (err: unknown) {
    error.value = String(err)
  }
}
</script>

<template>
  <div class="card">
    <div class="card-body">
      <h3 class="card-title h5 mb-4">{{ t('wallet.transfer.title') }}</h3>

      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.transfer.amount') }} <span class="text-danger">*</span></label
          >
          <input
            v-model="amount"
            type="number"
            step="0.01"
            min="0"
            class="form-control"
            required
            :class="{ 'is-invalid': isInsufficientBalance }"
          />
          <div v-if="isInsufficientBalance" class="invalid-feedback">
            {{ t('wallet.transfer.insufficientBalance') }}
          </div>
          <small class="text-muted">
            {{ t('wallet.transfer.availableBalance') }}: {{ currentBalance.toFixed(2) }}
            {{ currency }}
          </small>
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.transfer.currency') }} <span class="text-danger">*</span></label
          >
          <select v-model="currency" class="form-select">
            <option value="EUR">EUR</option>
            <option value="CZK">CZK</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.transfer.recipientAccount') }} <span class="text-danger">*</span></label
          >
          <input
            v-model="recipientAccount"
            type="text"
            class="form-control"
            required
            :placeholder="t('wallet.transfer.recipientAccountPlaceholder')"
          />
          <small class="text-muted">
            {{ t('wallet.transfer.recipientAccountHelp') }}
          </small>
        </div>

        <div class="mb-3">
          <label class="form-label">{{ t('wallet.transfer.variableSymbol') }}</label>
          <input
            v-model="variableSymbol"
            type="text"
            class="form-control"
            pattern="[0-9]*"
            maxlength="10"
            :placeholder="t('wallet.transfer.variableSymbolPlaceholder')"
          />
        </div>

        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <button type="submit" class="btn btn-primary w-100" :disabled="isInsufficientBalance">
          {{ t('wallet.transfer.submit') }}
        </button>
      </form>
    </div>
  </div>
</template>
