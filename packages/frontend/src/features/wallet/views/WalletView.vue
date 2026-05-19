<script setup lang="ts">
import DepositForm from '@/features/wallet/components/DepositForm.vue'
import LoadingSpinner from '@/shared/components/LoadingSpinner.vue'
import TransactionHistory from '@/features/wallet/components/TransactionHistory.vue'
import TransferForm from '@/features/wallet/components/TransferForm.vue'
import WalletBalance from '@/features/wallet/components/WalletBalance.vue'
import WithdrawForm from '@/features/wallet/components/WithdrawForm.vue'
import { useWalletStore } from '@/features/wallet/stores/wallet'

const walletStore = useWalletStore()
</script>

<template>
  <LoadingSpinner v-if="walletStore.isLoading" />

  <WalletBalance />

  <div class="row mt-4">
    <div class="col-md-6 mx-auto">
      <DepositForm />
    </div>
  </div>

  <div class="row mt-4 g-4">
    <div class="col-md-6">
      <TransferForm />
    </div>
    <div class="col-md-6">
      <WithdrawForm />
    </div>
  </div>

  <div class="mt-4">
    <TransactionHistory />
  </div>
</template>
