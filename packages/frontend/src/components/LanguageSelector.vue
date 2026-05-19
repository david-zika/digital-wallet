<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import 'flag-icons/css/flag-icons.min.css'

const { locale } = useI18n()

const languages = [
  { code: 'en', flag: 'gb', name: 'English' },
  { code: 'cs', flag: 'cz', name: 'Čeština' },
  { code: 'sk', flag: 'sk', name: 'Slovenčina' },
  { code: 'es', flag: 'es', name: 'Español' },
  { code: 'de', flag: 'de', name: 'Deutsch' },
]

function setLocale(code: string) {
  locale.value = code
  localStorage.setItem('app-locale', code)
}
</script>

<template>
  <div class="dropdown">
    <button class="btn btn-light dropdown-toggle" type="button" data-bs-toggle="dropdown">
      <span :class="`fi fi-${languages.find(l => l.code === locale)?.flag}`" class="me-2"></span>
      {{ $t('language') }}
    </button>
    <ul class="dropdown-menu">
      <li v-for="lang in languages" :key="lang.code">
        <a
          class="dropdown-item"
          href="#"
          :class="{ active: locale === lang.code }"
          @click.prevent="setLocale(lang.code)"
        >
          <span :class="`fi fi-${lang.flag}`" class="me-2"></span>
          {{ lang.name }}
        </a>
      </li>
    </ul>
  </div>
</template>

<style scoped>
  .fi {
    font-size: 1.2em;
    vertical-align: middle;
  }
</style>
