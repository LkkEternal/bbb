<template>
  <div class="page-container">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/')">返回首页</el-button>
      <h2>个人中心</h2>
    </div>

    <!-- 用户信息展示 -->
    <el-card v-loading="loading" shadow="never" class="profile-card">
      <template #header>
        <span class="card-title">用户信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名" :span="1">
          {{ profile.username }}
        </el-descriptions-item>
        <el-descriptions-item label="昵称" :span="1">
          {{ profile.nickname || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="1">
          {{ profile.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号" :span="1">
          {{ profile.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色" :span="1">
          <el-tag :type="profile.role === 'admin' ? 'danger' : 'primary'" size="small">
            {{ profile.roleLabel || profile.role }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="1">
          {{ profile.createdAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">
          {{ profile.bio || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 修改个人资料 -->
    <el-card shadow="never" class="profile-card">
      <template #header>
        <span class="card-title">修改资料</span>
      </template>
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="100px"
        class="profile-form"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="30" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="profileForm.bio"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="profileSubmitting" @click="handleUpdateProfile">
            保存修改
          </el-button>
          <el-button @click="resetProfileForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card shadow="never" class="profile-card">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="120px"
        class="profile-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="passwordSubmitting" @click="handleChangePassword">
            修改密码
          </el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'UserProfile' })

/* ========== 加载状态 ========== */
const loading = ref(false)
const profileSubmitting = ref(false)
const passwordSubmitting = ref(false)

/* ========== 用户信息展示 ========== */
const profile = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: '',
  roleLabel: '',
  createdAt: '',
  bio: '',
})

async function fetchProfile() {
  loading.value = true
  try {
    // TODO: 替换为实际 API 调用
    // const res = await getUserProfile()
    // Object.assign(profile, res)
    //
    // 模拟数据
    Object.assign(profile, {
      username: 'admin',
      nickname: '管理员',
      email: 'admin@example.com',
      phone: '138****8888',
      role: 'admin',
      roleLabel: '超级管理员',
      createdAt: '2024-01-15 10:30:00',
      bio: '系统管理员，负责平台日常运维。',
    })
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

/* ========== 修改资料表单 ========== */
const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  bio: '',
})

const profileRules: FormRules = {
  nickname: [
    { max: 30, message: '昵称不能超过30个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入有效的手机号码',
      trigger: 'blur',
    },
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' },
  ],
}

/** 将 profile 数据同步到表单 */
function syncProfileToForm() {
  profileForm.nickname = profile.nickname
  profileForm.email = profile.email
  profileForm.phone = profile.phone
  profileForm.bio = profile.bio
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileSubmitting.value = true
  try {
    // TODO: 替换为实际 API 调用
    // await updateUserProfile(profileForm)
    ElMessage.success('个人资料更新成功')
    // 同步回展示区
    Object.assign(profile, profileForm)
  } catch {
    ElMessage.error('更新失败，请重试')
  } finally {
    profileSubmitting.value = false
  }
}

function resetProfileForm() {
  profileFormRef.value?.resetFields()
  syncProfileToForm()
}

/* ========== 修改密码表单 ========== */
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

/** 密码强度校验 */
const validateNewPassword = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度在 6 到 20 个字符之间'))
  } else {
    callback()
  }
}

/** 确认密码校验 */
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  passwordSubmitting.value = true
  try {
    // TODO: 替换为实际 API 调用
    // await changePassword({
    //   oldPassword: passwordForm.oldPassword,
    //   newPassword: passwordForm.newPassword,
    // })
    ElMessage.success('密码修改成功，请重新登录')
    resetPasswordForm()
  } catch {
    ElMessage.error('密码修改失败，请重试')
  } finally {
    passwordSubmitting.value = false
  }
}

function resetPasswordForm() {
  passwordFormRef.value?.resetFields()
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

/* ========== 生命周期 ========== */
onMounted(async () => {
  await fetchProfile()
  syncProfileToForm()
})
</script>

<style scoped lang="scss">
.page-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.profile-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.profile-form {
  max-width: 520px;
}
</style>
