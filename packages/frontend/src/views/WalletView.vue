<script setup lang="ts">
  import { useWalletStore } from '@/stores/wallet';
  import WalletBalance from '@/components/WalletBalance.vue';
  import TransactionHistory from '@/components/TransactionHistory.vue';
  import DepositForm from '@/components/DepositForm.vue';
  import TransferForm from '@/components/TransferForm.vue';
  import WithdrawForm from '@/components/WithdrawForm.vue';
  import LoadingSpinner from '@/components/LoadingSpinner.vue';

  const walletStore = useWalletStore();
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
