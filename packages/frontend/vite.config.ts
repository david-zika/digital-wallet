import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/styles/main.scss";`,
      },
    },
  },
  build: {
    target: 'es2020',
    minify: 'oxc',
    rolldownOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (['vue', 'pinia', 'vue-i18n'].some((pkg) => id.includes(`/${pkg}/`))) return 'vendor'
          if (['decimal.js', 'qrcode.vue', 'date-fns'].some((pkg) => id.includes(`/${pkg}/`))) return 'wallet'
          if (id.includes('/bootstrap/')) return 'ui'
        },
        entryFileNames: 'assets/[name].[hash].js',
        chunkFileNames: 'assets/[name].[hash].js',
        assetFileNames: 'assets/[name].[hash].[ext]',
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['src/**/*.{ts,vue}'],
      exclude: ['src/test/**', 'src/vite-env.d.ts', 'src/main.ts'],
    },
  },
})