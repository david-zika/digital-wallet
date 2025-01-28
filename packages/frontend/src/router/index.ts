import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

// Lazy-loaded components to prevent circular dependencies
const LoginView = () => import('@/views/LoginView.vue');
const WalletView = () => import('@/views/WalletView.vue');
const SettingsView = () => import('@/views/SettingsView.vue');

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      name: 'wallet',
      component: WalletView,
      meta: { requiresAuth: true },
    },
    {
      path: '/settings',
      name: 'settings',
      component: SettingsView,
      meta: { requiresAuth: true },
    },
  ],
});

// Navigation guard
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Přesměrování na login, pokud uživatel není přihlášen
    next('/login');
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    // Přesměrování na hlavní stránku, pokud je uživatel již přihlášen
    next('/');
  } else {
    next();
  }
});

export default router;
