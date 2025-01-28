<script setup lang="ts">
  import ProfileForm from '@/components/ProfileForm.vue';
  import ChangePasswordForm from '@/components/ChangePasswordForm.vue';
</script>

<template>
  <div class="row g-4">
    <div class="col-md-6">
      <ProfileForm />
    </div>

    <div class="col-md-6">
      <ChangePasswordForm />
    </div>
  </div>
</template>
