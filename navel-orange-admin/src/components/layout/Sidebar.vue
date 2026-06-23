<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <img src="@/assets/images/logo.png" alt="logo" class="logo-icon" />
      <div v-if="!collapsed" class="logo-text-group">
        <span class="logo-text">脐橙监测系统</span>
        <span class="logo-sub">管理后台</span>
      </div>
    </div>
    <el-scrollbar class="sidebar-menu-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="transparent"
        text-color="rgba(255,255,255,0.55)"
        active-text-color="#ffffff"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item :index="'/' + route.path" class="sidebar-item">
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>

        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users" class="sidebar-item">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/system/roles" class="sidebar-item">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/system/logs" class="sidebar-item">
            <el-icon><Notebook /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
    <div class="sidebar-footer" v-if="!collapsed">
      <div class="footer-user">
        <div class="footer-avatar">{{ userStore.username?.charAt(0) || 'U' }}</div>
        <div class="footer-info">
          <span class="footer-name">{{ userStore.username || '用户' }}</span>
          <span class="footer-role">{{ userStore.isAdmin ? '管理员' : '农户' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const collapsed = computed(() => appStore.sidebarCollapsed)

const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/orchard/') && path !== '/orchard') return '/orchard'
  if (path.startsWith('/feedback/') && path !== '/feedback') return '/feedback'
  if (path.startsWith('/detection/stats')) return '/detection'
  if (path.startsWith('/alert/')) return '/alert/rules'
  if (path.startsWith('/knowledge/')) return '/knowledge/docs'
  return path
})

const menuRoutes = computed(() => {
  const mainRoute = router.options.routes.find(r => r.path === '/')
  if (!mainRoute?.children) return []
  return mainRoute.children.filter(r => !r.meta?.hidden && !r.meta?.roles)
})
</script>

<style scoped lang="scss">
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #1B2028;
}

.sidebar-logo {
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  gap: 12px;
  overflow: hidden;

  .logo-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;
    border-radius: 8px;
    background: #fff;
    padding: 2px;
  }

  .logo-text-group {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .logo-text {
    color: rgba(255, 255, 255, 0.92);
    font-size: 15px;
    font-weight: 600;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }

  .logo-sub {
    color: rgba(255, 255, 255, 0.35);
    font-size: 11px;
    white-space: nowrap;
    margin-top: 2px;
  }
}

.sidebar-menu-scrollbar {
  flex: 1;
  overflow: hidden;
  padding: 8px 0;
}

:deep(.el-menu) {
  border-right: none;
  padding: 0 8px;
}

:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin-bottom: 2px;
  border-radius: 8px;
  transition: all 0.2s ease;
  padding-left: 16px !important;

  &:hover {
    background-color: rgba(255, 255, 255, 0.04) !important;
  }

  &.is-active {
    background-color: rgba(91, 140, 90, 0.08) !important;
    color: #ffffff !important;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      bottom: 8px;
      width: 3px;
      background-color: #5B8C5A;
      border-radius: 0 2px 2px 0;
    }

    .el-icon {
      color: #5B8C5A !important;
    }
  }
}

:deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  padding-left: 16px !important;

  &:hover {
    background-color: rgba(255, 255, 255, 0.04) !important;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.footer-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.footer-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(91, 140, 90, 0.2);
  color: #5B8C5A;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.footer-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .footer-name {
    color: rgba(255, 255, 255, 0.85);
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .footer-role {
    color: rgba(255, 255, 255, 0.35);
    font-size: 11px;
    margin-top: 1px;
  }
}
</style>
