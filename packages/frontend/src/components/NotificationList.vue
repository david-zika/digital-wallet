<script setup lang="ts">
import { useNotificationStore } from '@/stores/notification'

const notificationStore = useNotificationStore()
</script>

<template>
  <div class="notification-container">
    <div
      v-for="notification in notificationStore.notifications"
      :key="notification.id"
      class="notification"
      :class="{
        'notification-success': notification.type === 'success',
        'notification-error': notification.type === 'error',
      }"
      @click="notificationStore.removeNotification(notification.id)"
    >
      {{ notification.message }}
    </div>
  </div>
</template>

<style scoped>
  .notification-container {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 1050;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .notification {
    padding: 1rem;
    border-radius: 4px;
    min-width: 300px;
    max-width: 500px;
    cursor: pointer;
    animation: slideIn 0.3s ease-out;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .notification-success {
    background-color: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
  }

  .notification-error {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
  }

  @keyframes slideIn {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
</style>
