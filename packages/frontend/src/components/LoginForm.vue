<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const { t } = useI18n()

const email = ref('')
const password = ref('')
const fullName = ref('')
const isRegistering = ref(false)
const error = ref('')
const isLoading = ref(false)

const handleSubmit = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true
    error.value = ''

    if (isRegistering.value) {
      if (!fullName.value) {
        throw new Error(t('auth.fullNameRequired'))
      }
      await authStore.register(email.value, password.value, fullName.value)
      notificationStore.addNotification(t('notifications.success.register'))
    } else {
      await authStore.login(email.value, password.value)
      notificationStore.addNotification(t('notifications.success.login'))
    }

    email.value = ''
    password.value = ''
    fullName.value = ''

    router.push('/')
  } catch (err: unknown) {
    error.value = String(err)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="card">
    <div class="card-body">
      <h2 class="card-title h4 mb-4">
        {{ isRegistering ? t('auth.register') : t('auth.login') }}
      </h2>

      <form @submit.prevent="handleSubmit">
        <div v-if="isRegistering" class="mb-3">
          <label class="form-label">
            {{ t('auth.fullName') }} <span class="text-danger">*</span>
          </label>
          <input
            v-model="fullName"
            type="text"
            class="form-control"
            required
            :disabled="isLoading"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">
            {{ t('auth.email') }} <span class="text-danger">*</span>
          </label>
          <input v-model="email" type="email" class="form-control" required :disabled="isLoading" />
        </div>

        <div class="mb-3">
          <label class="form-label">
            {{ t('auth.password') }} <span class="text-danger">*</span>
          </label>
          <input
            v-model="password"
            type="password"
            class="form-control"
            required
            :disabled="isLoading"
          />
        </div>

        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <div class="d-grid gap-2">
          <button type="submit" class="btn btn-primary" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
            {{ isRegistering ? t('auth.register') : t('auth.login') }}
          </button>
          <button
            type="button"
            class="btn btn-link"
            :disabled="isLoading"
            @click="isRegistering = !isRegistering"
          >
            {{ isRegistering ? t('auth.switchToLogin') : t('auth.switchToRegister') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
