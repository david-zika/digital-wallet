<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useWalletStore } from '@/stores/wallet'

const walletStore = useWalletStore()
const { t } = useI18n()

onMounted(() => {
  if (!walletStore.balances.length) {
    walletStore.fetchBalances()
  }
})
</script>

<template>
  <div class="row g-4">
    <div class="col-md-6">
      <div class="card">
        <div class="card-body">
          <h3 class="card-title h5">{{ t('wallet.balance') }} (EUR)</h3>
          <p class="display-6 text-primary mb-0">€{{ walletStore.getBalance('EUR').toFixed(2) }}</p>
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="card">
        <div class="card-body">
          <h3 class="card-title h5">{{ t('wallet.balance') }} (CZK)</h3>
          <p class="display-6 text-primary mb-0">
            {{ walletStore.getBalance('CZK').toFixed(2) }} Kč
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
