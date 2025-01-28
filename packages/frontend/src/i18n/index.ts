import { createI18n } from 'vue-i18n';
import en from './locales/en';
import cs from './locales/cs';
import sk from './locales/sk';
import es from './locales/es';
import de from './locales/de';

export const i18n = createI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'en',
  messages: {
    en,
    cs,
    sk,
    es,
    de,
  },
});
