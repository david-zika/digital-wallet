<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="footer mt-auto py-3 bg-light">
    <div class="container">
      <div class="row">
        <div class="col-12 text-center">
          <p class="text-muted small mb-0">
            © {{ currentYear }} Digital Wallet. All rights reserved.
          </p>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
  .footer {
    margin-top: 3rem;
    border-top: 1px solid #dee2e6;

    h5 {
      font-size: 1rem;
      font-weight: 600;
    }

    ul li {
      margin-bottom: 0.5rem;
    }

    a:hover {
      text-decoration: underline !important;
    }
  }
</style>
