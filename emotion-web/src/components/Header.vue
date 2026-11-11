<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox } from 'element-plus'
import { authAPI } from '@/api'

const router = useRouter()
const authStore = useAuthStore()
const user = computed(() => authStore.user.value)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try {
      await authAPI.logout()
    } catch (e) {}
    authStore.logout()
    router.push('/login')
  } catch (e) {}
}
</script>

<template>
  <header class="header">
    <div class="header-left">
      <div class="logo">
        <span class="smiley">😊</span>
      </div>
      <h1>表情检测系统</h1>
    </div>
    <div class="header-right">
      <div @click="router.push('/profile')" class="user-info">
        <img 
          v-if="user?.avatarUrl" 
          :src="user.avatarUrl" 
          alt="头像" 
          class="avatar"
        />
        <span v-else class="avatar-placeholder">{{ user?.nickname?.charAt(0) || '用' }}</span>
        <span class="nickname">{{ user?.nickname || '用户' }}</span>
      </div>
      <button @click="handleLogout" class="logout-btn">退出</button>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 72px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo .smiley {
  font-size: 22px;
}

.header-left h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 10px;
  transition: background 0.3s ease;
}

.user-info:hover {
  background: #f1f5f9;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.avatar-placeholder {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.nickname {
  font-weight: 500;
  color: #334155;
  font-size: 14px;
}

.admin-badge {
  padding: 4px 10px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-size: 12px;
  font-weight: 500;
  border-radius: 12px;
}

.logout-btn {
  padding: 10px 22px;
  background: #fef2f2;
  border: none;
  border-radius: 10px;
  color: #ef4444;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #fee2e2;
}
</style>