<script setup lang="ts">
import { onMounted } from 'vue'
import router from '@/core/router'
import { useAuthStore } from '@/features/auth/stores/auth'
import { useWalletStore } from '@/features/wallet/stores/wallet'
import logo from './assets/logo.svg'
import Footer from '@/shared/components/Footer.vue'
import LanguageSelector from '@/shared/components/LanguageSelector.vue'
import LoadingSpinner from '@/shared/components/LoadingSpinner.vue'
import NotificationList from '@/shared/components/NotificationList.vue'
import UserInfo from '@/features/auth/components/UserInfo.vue'

const authStore = useAuthStore()
const walletStore = useWalletStore()

onMounted(() => {
  if (authStore.isAuthenticated && !walletStore.isInitialized) {
    walletStore.initialize()
  }
})

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="d-flex flex-column min-vh-100">
    <!-- Notifications -->
    <NotificationList />

    <header class="bg-white shadow mb-4">
      <div class="container py-4 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center">
          <img :src="logo" alt="Digital Wallet Logo" class="logo me-3" />
          <h1 class="display-6 mb-0">{{ $t('header.title') }}</h1>
        </div>
        <div class="d-flex align-items-center gap-3">
          <LanguageSelector />
          <div v-if="authStore.isAuthenticated" class="d-flex align-items-center gap-3">
            <UserInfo />
            <button class="btn btn-outline-danger" @click="logout">
              {{ $t('header.logout') }}
            </button>
          </div>
        </div>
      </div>
    </header>

    <main class="container pb-4 flex-grow-1">
      <router-view v-slot="{ Component }">
        <Suspense>
          <template #default>
            <component :is="Component" v-if="Component" />
          </template>
          <template #fallback>
            <LoadingSpinner />
          </template>
        </Suspense>
      </router-view>
    </main>

    <Footer />
  </div>
</template>

<style scoped lang="scss">
  .logo {
    height: 60px;
    width: auto;
  }
</style>
