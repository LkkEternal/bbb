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
        <div class="app-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
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
@use '@/assets/styles/variables.scss' as *;

.app-layout {
  height: 100vh;
  overflow: hidden;
}

.app-sidebar {
  background-color: $sidebar-bg;
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
  height: $header-height;
  background: $card;
  border-bottom: 1px solid $border;
  padding: 0;
  display: flex;
  align-items: center;
}

.app-main {
  flex: 1;
  background-color: $canvas;
  overflow-y: auto;
  padding: 0;
  position: relative;

  // 右下角极淡品牌水印
  &::after {
    content: '';
    position: fixed;
    right: -40px;
    bottom: -40px;
    width: 320px;
    height: 320px;
    background:
      radial-gradient(circle at 50% 50%, rgba(232, 134, 58, 0.04) 0%, transparent 70%),
      radial-gradient(circle at 30% 70%, rgba(91, 140, 90, 0.03) 0%, transparent 50%);
    border-radius: 50%;
    pointer-events: none;
    z-index: 0;
  }
}

.main-accent-line {
  height: 2px;
  background: linear-gradient(90deg, $accent 0%, $primary 50%, $primary 100%);
  opacity: 0.5;
  position: relative;
  z-index: 1;

  &::after {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 4px;
    background: linear-gradient(90deg, transparent 0%, rgba(232, 134, 58, 0.15) 30%, rgba(91, 140, 90, 0.1) 70%, transparent 100%);
    filter: blur(3px);
  }
}

.app-content {
  position: relative;
  z-index: 1;
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
