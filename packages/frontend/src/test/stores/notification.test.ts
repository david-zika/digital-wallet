import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useNotificationStore } from '@/stores/notification'

describe('useNotificationStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
  })

  it('přidá notifikaci s výchozím typem success', () => {
    const store = useNotificationStore()
    store.addNotification('Test zpráva')
    expect(store.notifications).toHaveLength(1)
    expect(store.notifications[0].message).toBe('Test zpráva')
    expect(store.notifications[0].type).toBe('success')
  })

  it('přidá notifikaci s typem error', () => {
    const store = useNotificationStore()
    store.addNotification('Chyba!', 'error')
    expect(store.notifications[0].type).toBe('error')
  })

  it('přiřadí unikátní id každé notifikaci', () => {
    const store = useNotificationStore()
    const id1 = store.addNotification('První')
    const id2 = store.addNotification('Druhá')
    expect(id1).not.toBe(id2)
    expect(store.notifications[0].id).toBe(id1)
    expect(store.notifications[1].id).toBe(id2)
  })

  it('odstraní notifikaci po uplynutí timeoutu', () => {
    const store = useNotificationStore()
    store.addNotification('Zmizím', 'success', 3000)
    expect(store.notifications).toHaveLength(1)
    vi.advanceTimersByTime(3000)
    expect(store.notifications).toHaveLength(0)
  })

  it('neodstraní notifikaci před uplynutím timeoutu', () => {
    const store = useNotificationStore()
    store.addNotification('Ještě tady', 'success', 5000)
    vi.advanceTimersByTime(4999)
    expect(store.notifications).toHaveLength(1)
  })

  it('removeNotification odstraní konkrétní notifikaci', () => {
    const store = useNotificationStore()
    const id1 = store.addNotification('První')
    store.addNotification('Druhá')
    store.removeNotification(id1)
    expect(store.notifications).toHaveLength(1)
    expect(store.notifications[0].message).toBe('Druhá')
  })

  it('removeNotification ignoruje neexistující id', () => {
    const store = useNotificationStore()
    store.addNotification('Zpráva')
    store.removeNotification(9999)
    expect(store.notifications).toHaveLength(1)
  })

  it('může přidat více notifikací najednou', () => {
    const store = useNotificationStore()
    store.addNotification('První')
    store.addNotification('Druhá')
    store.addNotification('Třetí')
    expect(store.notifications).toHaveLength(3)
  })

  it('notifikace bez timeoutu (timeout=0) se neodstraní automaticky', () => {
    const store = useNotificationStore()
    store.addNotification('Trvalá', 'success', 0)
    vi.advanceTimersByTime(99999)
    expect(store.notifications).toHaveLength(1)
  })
})

