import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, vi } from 'vitest'

// Globální setup před každým testem
beforeEach(() => {
  setActivePinia(createPinia())

  // Mock localStorage
  const localStorageMock = (() => {
    let store: Record<string, string> = {}
    return {
      getItem: (key: string) => store[key] ?? null,
      setItem: (key: string, value: string) => {
        store[key] = value
      },
      removeItem: (key: string) => {
        delete store[key]
      },
      clear: () => {
        store = {}
      },
    }
  })()
  Object.defineProperty(window, 'localStorage', { value: localStorageMock })

  // Mock window.location
  Object.defineProperty(window, 'location', {
    value: { href: '/' },
    writable: true,
  })
})

// Globální Vue Test Utils konfigurace
config.global.stubs = {
  RouterLink: true,
  RouterView: true,
}

// Potlačit console.error ve výpisech testů pro Vue warnings
vi.spyOn(console, 'warn').mockImplementation(() => {})
