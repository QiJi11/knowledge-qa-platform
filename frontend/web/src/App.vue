<script setup lang="ts">
import { RouterView } from 'vue-router'
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
</script>

<template>
  <div id="app">
    <Header />
    <main class="main-content">
      <RouterView />
    </main>
    <Footer />
  </div>
</template>

<style>
@import '@/assets/styles/common.scss';

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}
</style>
