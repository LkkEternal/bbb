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
          placeholder="搜索功能..."
          :prefix-icon="Search"
          clearable
          size="default"
        />
      </div>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notify-badge">
        <el-icon :size="20" class="header-icon" @click="$router.push('/message')">
          <Bell />
        </el-icon>
      </el-badge>
      <el-divider direction="vertical" />
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <div class="user-avatar">{{ userStore.username?.charAt(0) || 'U' }}</div>
          <span class="username">{{ userStore.username || '用户' }}</span>
          <el-icon :size="14"><ArrowDown /></el-icon>
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

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const unreadCount = ref(0)
const searchQuery = ref('')

onMounted(() => {
  // TODO: load unread count
})

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
      } catch {}
      break
  }
}
</script>

<style scoped lang="scss">
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
  color: #5F6368;
  transition: color 0.2s;
  &:hover {
    color: #5B8C5A;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  :deep(.el-input) {
    width: 200px;
    .el-input__wrapper {
      border-radius: 20px;
      background-color: #F8F9FA;
      box-shadow: none;
      border: 1px solid transparent;
      transition: all 0.2s;
      &:hover, &.is-focus {
        border-color: #E5E7EB;
        background-color: #fff;
      }
    }
  }
}

.notify-badge {
  display: flex;
  align-items: center;
}

.header-icon {
  cursor: pointer;
  color: #5F6368;
  transition: color 0.2s;
  &:hover {
    color: #5B8C5A;
  }
}

.el-divider--vertical {
  border-left-color: #E5E7EB;
  margin: 0 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #F2F7F2;
  }

  .username {
    font-size: 13px;
    color: #1A1C1E;
    font-weight: 500;
  }
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5B8C5A, #7FA87E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}
</style>
