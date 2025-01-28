<script setup lang="ts">
  import { ref } from 'vue';
  import { useWalletStore } from '@/stores/wallet';
  import { useNotificationStore } from '@/stores/notification';
  import { useI18n } from 'vue-i18n';
  import QRCode from 'qrcode.vue';
  import type { CZKInstructions, EURInstructions, PaymentInstructions } from '@/types/wallet';

  const walletStore = useWalletStore();
  const notificationStore = useNotificationStore();
  const { t } = useI18n();

  const amount = ref('');
  const currency = ref<'EUR' | 'CZK'>('EUR');
  const demoMode = ref(false);
  const error = ref('');

  const getPaymentInstructions = () => {
    if (!amount.value || demoMode.value) return null;

    const instructions: PaymentInstructions = {
      EUR: {
        IBAN: 'CZ1234567890123456789012',
        SWIFT: 'GIBACZPX',
        Bank: 'Example Bank',
      },
      CZK: {
        AccountNumber: '123456789/0100',
        Bank: 'Example Bank',
      },
    };

    return instructions[currency.value];
  };

  const getQRValue = () => {
    if (!amount.value || demoMode.value) return '';
    const instructions = getPaymentInstructions();
    return JSON.stringify({
      amount: amount.value,
      currency: currency.value,
      ...instructions,
    });
  };

  const handleSubmit = async () => {
    try {
      error.value = '';
      const amountNum = parseFloat(amount.value);

      if (isNaN(amountNum) || amountNum <= 0) {
        throw new Error(t('wallet.deposit.invalidAmount'));
      }

      await walletStore.createTransaction(
        amountNum,
        currency.value,
        'DEPOSIT',
        undefined,
        undefined,
        undefined,
        demoMode.value
      );

      notificationStore.addNotification(t('notifications.success.depositCreated'));
      amount.value = '';
    } catch (err: unknown) {
      error.value = err?.toString()!;
    }
  };
</script>

<template>
  <div class="card">
    <div class="card-body">
      <h3 class="card-title h5 mb-4">{{ t('wallet.deposit.title') }}</h3>

      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.deposit.amount') }} <span class="text-danger">*</span></label
          >
          <input v-model="amount" type="number" step="0.01" min="0" class="form-control" required />
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('wallet.deposit.currency') }} <span class="text-danger">*</span></label
          >
          <select v-model="currency" class="form-select">
            <option value="EUR">EUR</option>
            <option value="CZK">CZK</option>
          </select>
        </div>

        <div class="mb-3">
          <div class="form-check">
            <input id="demoMode" v-model="demoMode" type="checkbox" class="form-check-input" />
            <label class="form-check-label" for="demoMode">
              {{ t('wallet.deposit.demoMode') }}
            </label>
          </div>
        </div>

        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <div
          v-if="amount && !demoMode && getPaymentInstructions()"
          class="mt-4 p-3 bg-light rounded"
        >
          <h4 class="h6 mb-3">{{ t('wallet.deposit.instructions') }}</h4>
          <dl>
            <template v-if="currency === 'EUR'">
              <dt class="text-muted">{{ t('wallet.deposit.iban') }}</dt>
              <dd class="fw-bold">{{ (getPaymentInstructions() as EURInstructions)?.IBAN }}</dd>
              <dt class="text-muted">{{ t('wallet.deposit.swift') }}</dt>
              <dd class="fw-bold">{{ (getPaymentInstructions() as EURInstructions)?.SWIFT }}</dd>
            </template>
            <template v-else>
              <dt class="text-muted">{{ t('wallet.deposit.accountNumber') }}</dt>
              <dd class="fw-bold">
                {{ (getPaymentInstructions() as CZKInstructions)?.AccountNumber }}
              </dd>
            </template>
            <dt class="text-muted">{{ t('wallet.deposit.bank') }}</dt>
            <dd class="fw-bold">{{ getPaymentInstructions()?.Bank }}</dd>
            <dt class="text-muted">{{ t('wallet.deposit.amount') }}</dt>
            <dd class="fw-bold">{{ amount }} {{ currency }}</dd>
          </dl>

          <div class="text-center mt-4">
            <QRCode :value="getQRValue()" :size="200" level="M" />
          </div>
        </div>

        <button type="submit" class="btn btn-primary w-100 mt-3">
          {{ demoMode ? t('wallet.deposit.submitDemo') : t('wallet.deposit.submit') }}
        </button>
      </form>
    </div>
  </div>
</template>
