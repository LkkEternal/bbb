<template>
  <el-container class="app-layout">
    <el-aside :width="sidebarWidth" class="app-sidebar">
      <Sidebar />
    </el-aside>
    <el-container class="app-main-container">
      <el-header class="app-header">
        <Header />
      </el-header>
      <el-main class="app-main">
        <div class="main-accent-line"></div>
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'

const appStore = useAppStore()

const sidebarWidth = computed(() =>
  appStore.sidebarCollapsed ? '72px' : '240px'
)
</script>

<style scoped lang="scss">
.app-layout {
  height: 100vh;
  overflow: hidden;
}

.app-sidebar {
  background-color: #1B2028;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.app-main-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.app-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #E5E7EB;
  padding: 0;
  display: flex;
  align-items: center;
}

.app-main {
  flex: 1;
  background-color: #F8F9FA;
  overflow-y: auto;
  padding: 0;
  position: relative;
}

.main-accent-line {
  height: 1px;
  background: linear-gradient(90deg, #5B8C5A, #E8863A);
  opacity: 0.6;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
