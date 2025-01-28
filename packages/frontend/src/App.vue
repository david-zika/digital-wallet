<script setup lang="ts">
  import { onMounted } from 'vue';
  import { useAuthStore } from '@/stores/auth';
  import { useWalletStore } from '@/stores/wallet';
  import LanguageSelector from './components/LanguageSelector.vue';
  import UserInfo from './components/UserInfo.vue';
  import Footer from './components/Footer.vue';
  import NotificationList from './components/NotificationList.vue';
  import logo from './assets/logo.svg';
  import router from '@/router';
  import LoadingSpinner from './components/LoadingSpinner.vue';

  const authStore = useAuthStore();
  const walletStore = useWalletStore();

  onMounted(() => {
    if (authStore.isAuthenticated && !walletStore.isInitialized) {
      walletStore.initialize();
    }
  });

  function logout() {
    authStore.logout();
    router.push('/login');
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
          <component :is="Component" />
          <template #fallback>
            <LoadingSpinner />
          </template>
        </Suspense>
      </router-view>
    </main>

    <Footer />
  </div>
</template>

<style>
  .min-vh-100 {
    min-height: 100vh;
  }

  .flex-grow-1 {
    flex-grow: 1;
  }
</style>

<style scoped>
  .logo {
    height: 60px;
    width: auto;
  }
</style>
