<script setup lang="ts">
  const currentYear = new Date().getFullYear();
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

<style scoped>
  .footer {
    margin-top: 3rem;
    border-top: 1px solid #dee2e6;
  }

  .footer h5 {
    font-size: 1rem;
    font-weight: 600;
  }

  .footer ul li {
    margin-bottom: 0.5rem;
  }

  .footer a:hover {
    text-decoration: underline !important;
  }
</style>
