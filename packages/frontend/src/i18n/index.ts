import { createI18n } from 'vue-i18n'
import cs from './locales/cs'
import de from './locales/de'
import en from './locales/en'
import es from './locales/es'
import sk from './locales/sk'

const LOCALE_KEY = 'app-locale'
const SUPPORTED_LOCALES = ['en', 'cs', 'sk', 'es', 'de']

function getSavedLocale(): string {
  const saved = localStorage.getItem(LOCALE_KEY)
  return saved && SUPPORTED_LOCALES.includes(saved) ? saved : 'en'
}

export const i18n = createI18n({
  legacy: false,
  locale: getSavedLocale(),
  fallbackLocale: 'en',
  messages: {
    en,
    cs,
    sk,
    es,
    de,
  },
})
