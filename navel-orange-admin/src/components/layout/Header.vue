<template>
  <div class="header">
    <div class="header-left">
      <el-icon
        class="collapse-btn"
        :size="20"
        @click="appStore.toggleSidebar()"
      >
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <Breadcrumb />
    </div>

    <div class="header-right">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索菜单或功能..."
          :prefix-icon="Search"
          clearable
          size="default"
          @keyup.enter="handleSearch"
        />
      </div>

      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notify-badge">
          <el-icon :size="20" class="header-icon" @click="$router.push('/message')">
            <Bell />
          </el-icon>
        </el-badge>
      </div>

      <el-divider direction="vertical" class="header-divider" />

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <div class="user-avatar">{{ userStore.username?.charAt(0) || 'U' }}</div>
          <span class="username">{{ userStore.username || '用户' }}</span>
          <el-icon :size="14" class="dropdown-arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <ChangePasswordDialog v-model="showPasswordDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import Breadcrumb from './Breadcrumb.vue'
import ChangePasswordDialog from '../ChangePasswordDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const unreadCount = ref(0)
const searchQuery = ref('')
const showPasswordDialog = ref(false)

onMounted(async () => {
  // TODO: 从接口获取未读消息数
  // try { const res = await getUnreadCount(); unreadCount.value = res.total ?? 0 } catch {}
})

function handleSearch() {
  // TODO: 全局搜索功能
}

async function handleCommand(command: string) {
  switch (command) {
    case 'logout':
      try {
        await ElMessageBox.confirm('确定退出登录？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
        await userStore.doLogout()
        router.push('/login')
      } catch {} // 取消则不操作
      break
    case 'profile':
      router.push('/user/profile')
      break
    case 'password':
      showPasswordDialog.value = true
      break
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.header {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: $text-secondary;
  transition: all 0.2s ease;
  padding: 6px;
  border-radius: 6px;

  &:hover {
    color: $primary;
    background-color: $primary-subtle;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

// ── 搜索框 ─────────────────────────────────────────────
.search-box {
  :deep(.el-input) {
    width: 220px;
    transition: width 0.3s ease;

    &:focus-within {
      width: 280px;
    }

    .el-input__wrapper {
      border-radius: 20px;
      background-color: $canvas;
      box-shadow: none;
      border: 1px solid transparent;
      transition: all 0.25s ease;

      &:hover {
        border-color: $border;
        background-color: $card;
      }

      &.is-focus {
        border-color: $primary;
        background-color: $card;
        box-shadow: 0 0 0 3px rgba(91, 140, 90, 0.08);
      }
    }

    .el-input__inner {
      &::placeholder {
        color: $text-placeholder;
        font-size: 13px;
      }
    }
  }
}

// ── 操作区域 ────────────────────────────────────────────
.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.notify-badge {
  display: flex;
  align-items: center;
}

.header-icon {
  cursor: pointer;
  color: $text-secondary;
  transition: all 0.2s ease;
  padding: 6px;
  border-radius: 6px;

  &:hover {
    color: $accent;
    background-color: $accent-light;
  }
}

.header-divider {
  border-left-color: $divider !important;
  margin: 0 4px;
  height: 24px;
}

// ── 用户信息 ────────────────────────────────────────────
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 6px;
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background-color: $primary-subtle;

    .dropdown-arrow {
      color: $primary;
    }
  }
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, #7FA87E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.username {
  font-size: 13px;
  color: $text-primary;
  font-weight: 500;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  color: $text-placeholder;
  transition: color 0.2s ease;
}
</style>
