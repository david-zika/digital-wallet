<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/features/auth/stores/auth'
import { useNotificationStore } from '@/shared/stores/notification'

const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const { t } = useI18n()

const fullName = ref('')
const bankAccount = ref('')
const error = ref('')
const isLoading = ref(false)
const userEmail = computed(() => authStore.userEmail || '')

onMounted(async () => {
  try {
    const profile = await authStore.getProfile()
    fullName.value = profile.fullName || ''
    bankAccount.value = profile.bankAccount || ''
  } catch (err: unknown) {
    error.value = String(err)
  }
})

const handleSubmit = async () => {
  if (isLoading.value) return

  try {
    error.value = ''
    isLoading.value = true

    await authStore.updateProfile({
      fullName: fullName.value,
      bankAccount: bankAccount.value,
    })

    notificationStore.addNotification(t('notifications.success.profileUpdated'))
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
      <h3 class="card-title h5 mb-4">{{ t('settings.profile.title') }}</h3>

      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label">
            {{ t('settings.profile.fullName') }} <span class="text-danger">*</span>
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
            {{ t('settings.profile.bankAccount') }} <span class="text-danger">*</span>
          </label>
          <input
            v-model="bankAccount"
            type="text"
            class="form-control"
            required
            :disabled="isLoading"
            :placeholder="t('settings.profile.bankAccountPlaceholder')"
          />
        </div>

        <div class="mb-3">
          <label class="form-label">{{ t('auth.email') }}</label>
          <input
            :value="userEmail"
            type="text"
            class="form-control"
            :disabled="true"
          />
        </div>

        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <button type="submit" class="btn btn-primary w-100" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ t('settings.profile.save') }}
        </button>
      </form>
    </div>
  </div>
</template>
