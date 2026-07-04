<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" content="个人中心" style="margin-bottom: 24px" />

    <el-card v-loading="loading">
      <div class="profile-header">
        <div class="avatar-wrapper" @click="triggerAvatarUpload">
          <div class="avatar" v-if="!userInfo?.avatarUrl">
            {{ userInfo?.username?.charAt(0) || 'U' }}
          </div>
          <el-avatar v-else :size="80" :src="userInfo.avatarUrl" />
          <div class="avatar-overlay">
            <el-icon :size="20"><Camera /></el-icon>
          </div>
          <input ref="avatarInputRef" type="file" accept="image/jpeg,image/png" style="display: none" @change="handleAvatarChange" />
        </div>
        <div class="user-basic">
          <h2>{{ userInfo?.username || '-' }}</h2>
          <el-tag v-for="role in userInfo?.roles" :key="role.id" :type="role.code === 'ROLE_ADMIN' ? 'danger' : undefined" size="small">
            {{ role.name }}
          </el-tag>
        </div>
      </div>

      <el-divider />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ userInfo?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ userInfo?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">
          <div v-if="editingEmail">
            <el-input v-model="emailValue" size="small" style="width:200px" placeholder="请输入邮箱" />
            <el-button size="small" type="primary" @click="saveEmail" style="margin-left:8px">保存</el-button>
            <el-button size="small" @click="cancelEditEmail">取消</el-button>
          </div>
          <span v-else>
            {{ userInfo?.email || '未设置' }}
            <el-button size="small" link type="primary" @click="startEditEmail" style="margin-left:8px">修改</el-button>
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'" size="small">
            {{ userInfo?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色" :span="2">
          <el-tag v-for="role in userInfo?.roles" :key="role.id" :type="role.code === 'ROLE_ADMIN' ? 'danger' : undefined" size="small" style="margin-right: 4px">
            {{ role.name }}
          </el-tag>
          <span v-if="!userInfo?.roles?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(userInfo?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ formatTime(userInfo?.lastLoginAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ userInfo?.lastLoginIp || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h3 class="section-title">通知设置</h3>
      <el-form label-width="120px" class="notification-form">
        <el-form-item label="邮件预警">
          <el-switch v-model="notificationSettings.emailEnabled" @change="handleNotificationChange" />
          <span class="setting-desc">开启后将通过邮件接收预警通知</span>
        </el-form-item>
        <el-form-item label="小程序推送">
          <el-switch v-model="notificationSettings.pushEnabled" @change="handleNotificationChange" />
          <span class="setting-desc">开启后将通过小程序接收推送通知</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateNotificationSettings, uploadAvatar, updateProfile } from '@/api/modules/user'
import dayjs from 'dayjs'

interface UserProfile {
  id: number
  username: string
  phone: string
  email: string | null
  avatarUrl: string | null
  roles: Array<{ id: number; code: string; name: string }>
  status: number
  emailEnabled: boolean
  pushEnabled: boolean
  createdAt: string
  lastLoginAt: string
  lastLoginIp: string
}

const loading = ref(false)
const avatarUploading = ref(false)
const userInfo = ref<UserProfile | null>(null)
const avatarInputRef = ref<HTMLInputElement>()

const notificationSettings = reactive({
  emailEnabled: false,
  pushEnabled: false,
})

const editingEmail = ref(false)
const emailValue = ref('')

function startEditEmail() {
  emailValue.value = userInfo.value?.email || ''
  editingEmail.value = true
}

function cancelEditEmail() {
  editingEmail.value = false
  emailValue.value = ''
}

async function saveEmail() {
  try {
    await updateProfile({ email: emailValue.value })
    if (userInfo.value) {
      userInfo.value.email = emailValue.value
    }
    editingEmail.value = false
    ElMessage.success('邮箱已更新')
  } catch {
    editingEmail.value = false
  }
}

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

async function fetchProfile() {
  loading.value = true
  try {
    const data: any = await getProfile()
    userInfo.value = data
    notificationSettings.emailEnabled = data.emailEnabled ?? false
    notificationSettings.pushEnabled = data.pushEnabled ?? false
  } catch {} finally {
    loading.value = false
  }
}

async function handleNotificationChange() {
  try {
    await updateNotificationSettings({
      emailEnabled: notificationSettings.emailEnabled,
      pushEnabled: notificationSettings.pushEnabled,
    })
    ElMessage.success('通知设置已更新')
  } catch {
    fetchProfile()
  }
}

function triggerAvatarUpload() {
  avatarInputRef.value?.click()
}

async function handleAvatarChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像文件大小不能超过2MB')
    input.value = ''
    return
  }

  avatarUploading.value = true
  try {
    const avatarUrl: any = await uploadAvatar(file)
    if (userInfo.value) {
      userInfo.value.avatarUrl = avatarUrl
    }
    ElMessage.success('头像更新成功')
  } catch {} finally {
    avatarUploading.value = false
    input.value = ''
  }
}

onMounted(fetchProfile)
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, #7FA87E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
}

.user-basic {
  h2 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
    color: $text-primary;
  }
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.notification-form {
  max-width: 400px;

  .setting-desc {
    margin-left: 12px;
    font-size: 12px;
    color: $text-secondary;
  }
}
</style>
