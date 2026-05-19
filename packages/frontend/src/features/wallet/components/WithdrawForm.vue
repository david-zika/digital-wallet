<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/stores/auth'
import { useNotificationStore } from '@/shared/stores/notification'
import { useWalletStore } from '@/features/wallet/stores/wallet'
import { useErrorHandler } from '@/shared/utils/errorHandler'

const walletStore = useWalletStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const { t } = useI18n()
const { handleError } = useErrorHandler()

const amount = ref('')
const currency = ref<'EUR' | 'CZK'>('EUR')
const recipientAccount = ref('')
const recipientName = ref('')
const paymentReference = ref('')
const error = ref('')
const isLoading = ref(false)
const isInitialized = ref(false)

const currentBalance = computed(() => walletStore.getBalance(currency.value))
const isInsufficientBalance = computed(() => {
  const amountNum = parseFloat(amount.value)
  return !Number.isNaN(amountNum) && amountNum > currentBalance.value
})

onMounted(async () => {
  try {
    isLoading.value = true
    const profile = await authStore.getProfile()
    recipientAccount.value = profile.bankAccount || ''
    recipientName.value = profile.fullName || ''
    isInitialized.value = true
  } catch (err) {
    error.value = handleError(err)
  } finally {
    isLoading.value = false
  }
})

const handleSubmit = async () => {
  if (isLoading.value) return

  const amountNum = parseFloat(amount.value)

  if (Number.isNaN(amountNum) || amountNum <= 0) {
    error.value = t('wallet.withdraw.invalidAmount')
    return
  }

  if (isInsufficientBalance.value) {
    error.value = t('wallet.withdraw.insufficientBalance')
    return
  }

  if (!recipientAccount.value) {
    error.value = t('wallet.withdraw.noBankAccount')
    return
  }

  if (!recipientName.value) {
    error.value = t('wallet.withdraw.recipientNameRequired')
    return
  }

  try {
    error.value = ''
    isLoading.value = true

    await walletStore.createTransaction(
      amountNum,
      currency.value,
      'WITHDRAWAL',
      recipientAccount.value ?? undefined,
      recipientName.value ?? undefined,
      paymentReference.value
    )

    notificationStore.addNotification(t('notifications.success.withdrawalCreated'))

    amount.value = ''
    paymentReference.value = ''
  } catch (err: unknown) {
    error.value = handleError(err)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="card">
    <div class="card-body">
      <h3 class="card-title h5 mb-4">{{ t('wallet.withdraw.title') }}</h3>

      <div v-if="!recipientAccount" class="alert alert-warning">
        {{ t('wallet.withdraw.noBankAccountWarning') }}
        <router-link to="/settings" class="alert-link">
          {{ t('wallet.withdraw.goToSettings') }}
        </router-link>
      </div>

      <form v-else @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.withdraw.amount') }} <span class="text-danger">*</span></label
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
            {{ t('wallet.withdraw.insufficientBalance') }}
          </div>
          <small class="text-muted">
            {{ t('wallet.withdraw.availableBalance') }}: {{ currentBalance.toFixed(2) }}
            {{ currency }}
          </small>
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.withdraw.currency') }} <span class="text-danger">*</span></label
          >
          <select v-model="currency" class="form-select">
            <option value="EUR">EUR</option>
            <option value="CZK">CZK</option>
          </select>
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.withdraw.bankAccount') }} <span class="text-danger">*</span></label
          >
          <p class="form-control-plaintext">{{ recipientAccount }}</p>
        </div>

        <div class="mb-3">
          <label class="form-label">{{ t('wallet.withdraw.recipientName') }}</label>
          <p class="form-control-plaintext">
            {{ recipientName || t('wallet.withdraw.noNameSet') }}
          </p>
        </div>

        <div class="mb-3">
          <label class="form-label">{{ t('wallet.withdraw.variableSymbol') }}</label>
          <input
            v-model="paymentReference"
            type="text"
            class="form-control"
            pattern="[0-9]*"
            maxlength="10"
            :placeholder="t('wallet.withdraw.variableSymbolPlaceholder')"
          />
        </div>

        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <button
          type="submit"
          class="btn btn-primary w-100"
          :disabled="isInsufficientBalance || isLoading"
        >
          <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ t('wallet.withdraw.submit') }}
        </button>
      </form>
    </div>
  </div>
</template>
