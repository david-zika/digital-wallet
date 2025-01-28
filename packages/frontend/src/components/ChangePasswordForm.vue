<script setup lang="ts">
  import { ref } from 'vue';
  import { useAuthStore } from '@/stores/auth';
  import { useNotificationStore } from '@/stores/notification';
  import { useI18n } from 'vue-i18n';

  const authStore = useAuthStore();
  const notificationStore = useNotificationStore();
  const { t } = useI18n();

  const currentPassword = ref('');
  const newPassword = ref('');
  const confirmPassword = ref('');
  const error = ref('');
  const isLoading = ref(false);

  const handleSubmit = async () => {
    if (isLoading.value) return;

    try {
      error.value = '';
      isLoading.value = true;

      if (newPassword.value !== confirmPassword.value) {
        throw new Error(t('auth.passwordsDoNotMatch'));
      }

      if (newPassword.value.length < 6) {
        throw new Error(t('auth.passwordTooShort'));
      }

      await authStore.changePassword(currentPassword.value, newPassword.value);

      notificationStore.addNotification(t('notifications.success.passwordChanged'));

      currentPassword.value = '';
      newPassword.value = '';
      confirmPassword.value = '';
    } catch (err: unknown) {
      error.value = err?.toString()!;
    } finally {
      isLoading.value = false;
    }
  };
</script>

<template>
  <div class="card">
    <div class="card-body">
      <h3 class="card-title h5 mb-4">{{ t('auth.changePassword') }}</h3>

      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label class="form-label"
            >{{ t('auth.currentPassword') }} <span class="text-danger">*</span></label
          >
          <input
            v-model="currentPassword"
            type="password"
            class="form-control"
            required
            :disabled="isLoading"
          />
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('auth.newPassword') }} <span class="text-danger">*</span></label
          >
          <input
            v-model="newPassword"
            type="password"
            class="form-control"
            required
            :disabled="isLoading"
          />
        </div>

        <div class="mb-3">
          <label class="form-label"
            >{{ t('auth.confirmPassword') }} <span class="text-danger">*</span></label
          >
          <input
            v-model="confirmPassword"
            type="password"
            class="form-control"
            required
            :disabled="isLoading"
          />
        </div>

        <div v-if="error" class="alert alert-danger">{{ error }}</div>

        <button type="submit" class="btn btn-primary w-100" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ t('auth.changePassword') }}
        </button>
      </form>
    </div>
  </div>
</template>
