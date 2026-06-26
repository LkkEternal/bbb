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
        :default-openeds="defaultOpeneds"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="transparent"
        text-color="rgba(255,255,255,0.55)"
        active-text-color="#ffffff"
        router
      >
        <!-- 首页看板 -->
        <el-menu-item :index="dashboardPath" class="sidebar-item" @click="handleDashboardClick">
          <el-icon><Odometer /></el-icon>
          <template #title>
            <span class="menu-title">首页看板</span>
          </template>
        </el-menu-item>

        <!-- 果园管理（非管理员：子菜单） -->
        <el-sub-menu v-if="!userStore.isAdmin" index="/orchard-group">
          <template #title>
            <el-icon><Location /></el-icon>
            <span class="menu-title">果园管理</span>
          </template>
          <el-menu-item index="/orchard" class="sidebar-item">
            <span>果园列表</span>
          </el-menu-item>
          <el-menu-item index="/orchard/members" class="sidebar-item">
            <span>成员管理</span>
          </el-menu-item>
          <el-menu-item index="/devices" class="sidebar-item">
            <span>设备管理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 管理员：果园管理（子菜单） -->
        <el-sub-menu v-if="userStore.isAdmin" index="/orchard-group">
          <template #title>
            <el-icon><Location /></el-icon>
            <span class="menu-title">果园管理</span>
          </template>
          <el-menu-item index="/orchard" class="sidebar-item">
            <span>果园列表</span>
          </el-menu-item>
          <el-menu-item index="/devices" class="sidebar-item">
            <span>设备管理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 识别统计 -->
        <el-menu-item v-if="userStore.isAdmin || userStore.hasOrchard" index="/detection" class="sidebar-item">
          <el-icon><Search /></el-icon>
          <template #title>
            <span class="menu-title">识别统计</span>
          </template>
        </el-menu-item>

        <!-- 农事日历 -->
        <el-menu-item v-if="userStore.isAdmin || userStore.hasOrchard" index="/calendar" class="sidebar-item">
          <el-icon><Calendar /></el-icon>
          <template #title>
            <span class="menu-title">农事日历</span>
          </template>
        </el-menu-item>

        <!-- 消息中心 -->
        <el-menu-item v-if="userStore.isAdmin || userStore.hasOrchard" index="/message" class="sidebar-item">
          <el-icon><Message /></el-icon>
          <template #title>
            <span class="menu-title">消息中心</span>
          </template>
        </el-menu-item>

        <!-- 管理员专属菜单 -->
        <template v-if="userStore.isAdmin">
          <!-- 预警规则 -->
          <el-menu-item index="/alert/rules" class="sidebar-item">
            <el-icon><Bell /></el-icon>
            <template #title>
              <span class="menu-title">预警规则</span>
            </template>
          </el-menu-item>

          <!-- 文献管理 -->
          <el-menu-item index="/knowledge/docs" class="sidebar-item">
            <el-icon><Document /></el-icon>
            <template #title>
              <span class="menu-title">文献管理</span>
            </template>
          </el-menu-item>

          <!-- 用户反馈 -->
          <el-menu-item index="/feedback" class="sidebar-item">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>
              <span class="menu-title">用户反馈</span>
            </template>
          </el-menu-item>

          <!-- 系统管理（子菜单） -->
          <el-sub-menu index="/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span class="menu-title">系统管理</span>
            </template>
            <el-menu-item index="/system/users" class="sidebar-item">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/system/logs" class="sidebar-item">
              <el-icon><Notebook /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer" v-if="!collapsed">
      <div class="footer-user">
        <div class="footer-avatar">{{ userStore.username?.charAt(0) || 'U' }}</div>
        <div class="footer-info">
          <span class="footer-name">{{ userStore.username || '用户' }}</span>
          <span class="footer-role">{{ roleLabel }}</span>
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
import { Odometer, Location, Search, Calendar, Message, Bell, Document, ChatDotRound, Setting, User, Notebook } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const collapsed = computed(() => appStore.sidebarCollapsed)

const defaultOpeneds = computed(() => {
  const opens: string[] = []
  opens.push('/orchard-group')
  if (userStore.isAdmin) {
    opens.push('/system')
  }
  return opens
})

const dashboardPath = computed(() => {
  if (userStore.isAdmin) return '/dashboard'
  if (!userStore.hasOrchard) return '/welcome'
  return '/orchard-dashboard'
})

function handleDashboardClick() {
  const target = dashboardPath.value
  if (route.path === target) {
    router.push({ path: target, query: { _t: Date.now() } })
  } else {
    router.push(target)
  }
}

const roleLabel = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isOrchardOwner) return '园主'
  if (userStore.isOrchardManager) return '果园管理员'
  return '普通用户'
})

const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/orchard/') && path !== '/orchard') return '/orchard'
  if (path.startsWith('/devices')) return '/devices'
  if (path.startsWith('/detection/stats')) return '/detection'
  if (path.startsWith('/alert/')) return '/alert/rules'
  if (path.startsWith('/knowledge/')) return '/knowledge/docs'
  if (path.startsWith('/feedback/')) return '/feedback'
  return path
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: $sidebar-bg;
  position: relative;

  // 极淡的纹理光晕
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      radial-gradient(ellipse at 50% 0%, rgba(91, 140, 90, 0.08) 0%, transparent 60%);
    pointer-events: none;
  }
}

// ── Logo ────────────────────────────────────────────────
.sidebar-logo {
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  gap: 12px;
  overflow: hidden;
  position: relative;
  z-index: 1;

  .logo-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.95);
    padding: 2px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    transition: transform 0.3s ease;
  }

  &:hover .logo-icon {
    transform: scale(1.05);
  }

  .logo-text-group {
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .logo-text {
    color: rgba(255, 255, 255, 0.92);
    font-size: 16px;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }

  .logo-sub {
    color: rgba(255, 255, 255, 0.35);
    font-size: 11px;
    white-space: nowrap;
    margin-top: 1px;
    letter-spacing: 1px;
  }
}

// ── 菜单 ────────────────────────────────────────────────
.sidebar-menu-scrollbar {
  flex: 1;
  overflow: hidden;
  padding: 12px 0;
  position: relative;
  z-index: 1;
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
  position: relative;

  .el-icon {
    transition: color 0.2s ease;
  }

  &:hover {
    background-color: $sidebar-hover !important;

    .el-icon {
      color: rgba(255, 255, 255, 0.7) !important;
    }
  }

  &.is-active {
    background-color: $sidebar-active-bg !important;
    color: $sidebar-text-active !important;

    .el-icon {
      color: $primary !important;
    }

    // 左侧激活指示条 — 橙绿渐变色
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      bottom: 8px;
      width: 3px;
      background: linear-gradient(180deg, #E8863A 0%, #5B8C5A 100%);
      border-radius: 0 2px 2px 0;
      box-shadow: 0 0 6px rgba(91, 140, 90, 0.3);
    }

    // 右侧微弱光晕
    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 0;
      bottom: 0;
      width: 1px;
      background: linear-gradient(180deg, transparent, rgba(232, 134, 58, 0.15), transparent);
    }
  }
}

.menu-title {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

:deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  border-radius: 8px;
  padding-left: 16px !important;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;

  &:hover {
    background-color: $sidebar-hover !important;
  }
}

// ── 底部用户 ────────────────────────────────────────────
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
  z-index: 1;

  // 底部装饰线
  &::before {
    content: '';
    position: absolute;
    top: -1px;
    left: 20%;
    right: 20%;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(232, 134, 58, 0.3), rgba(91, 140, 90, 0.3), transparent);
  }
}

.footer-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.footer-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(91, 140, 90, 0.25), rgba(232, 134, 58, 0.15));
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  border: 1.5px solid rgba(255, 255, 255, 0.08);
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
