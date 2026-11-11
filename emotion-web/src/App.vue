<script setup>
import { computed } from 'vue'
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/api'
import Sidebar from '@/components/Sidebar.vue'
import Header from '@/components/Header.vue'

const authStore = useAuthStore()
const route = useRoute()
const showNav = computed(() => !['/login', '/register'].includes(route.path))

onMounted(async () => {
  if (authStore.accessToken.value && !authStore.user.value) {
    try {
      const res = await userAPI.getProfile()
      authStore.setUser(res.data.data)
    } catch (e) {
      console.error('Failed to fetch user profile')
    }
  }
})
</script>

<template>
  <div class="app-container" :class="{ 'no-nav': !showNav }">
    <Header v-if="showNav" />
    <Sidebar v-if="showNav" />
    <router-view />
  </div>
</template>

<style>
.app-container {
  min-height: 100vh;
  background: #f1f5f9;
  padding-top: 72px;
}

.app-container.no-nav {
  padding-top: 0;
}
</style>