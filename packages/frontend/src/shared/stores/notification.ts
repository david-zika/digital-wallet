import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Notification {
  id: number
  message: string
  type: 'success' | 'error'
  timeout?: number
}

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  let nextId = 1

  const addNotification = (
    message: string,
    type: 'success' | 'error' = 'success',
    timeout = 5000
  ) => {
    const id = nextId++
    const notification: Notification = {
      id,
      message,
      type,
      timeout,
    }

    notifications.value.push(notification)

    if (timeout) {
      setTimeout(() => {
        removeNotification(id)
      }, timeout)
    }

    return id
  }

  const removeNotification = (id: number) => {
    const index = notifications.value.findIndex((n) => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  return {
    notifications,
    addNotification,
    removeNotification,
  }
})
