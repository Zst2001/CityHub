<script setup>
import AppHeader from '../components/common/AppHeader.vue'
import AppFooter from '../components/common/AppFooter.vue'
</script>

<template>
  <div class="page-shell">
    <AppHeader />
    <main class="page-main"><RouterView /></main>
    <AppFooter />
  </div>
</template>
