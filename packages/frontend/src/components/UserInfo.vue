<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const { t } = useI18n()
const router = useRouter()

const userEmail = computed(() => authStore.userEmail || '')
const accountReference = computed(() => authStore.accountReference)
const fullName = computed(() => authStore.profile?.fullName || '')

const isRouteSettings = computed(() => router.currentRoute.value.path === '/settings')

const handleSettings = () => {
  if (isRouteSettings.value) {
    router.push('/')
  } else {
    router.push('/settings')
  }
}
</script>

<template>
  <div class="d-flex align-items-center gap-4">
    <div class="text-end">
      <div class="small text-muted">{{ t('header.loggedInAs') }}</div>
      <div class="fw-bold" :title="userEmail">{{ fullName || userEmail }}</div>
      <div class="small text-muted mt-1">
        {{ t('header.accountReference') }}: <span class="fw-bold">{{ accountReference }}</span>
      </div>
    </div>
    <div class="vr"></div>
    <div class="d-flex flex-column gap-2">
      <button class="btn btn-outline-secondary btn-sm" @click="handleSettings">
        {{ isRouteSettings ? t('header.back') : t('header.settings') }}
      </button>
    </div>
  </div>
</template>
