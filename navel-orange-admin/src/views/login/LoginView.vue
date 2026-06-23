<template>
  <div class="login-page">
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
    <div class="login-card">
      <div class="card-header">
        <img src="@/assets/images/logo.png" alt="logo" class="brand-logo" />
        <h1 class="brand-title">脐橙病虫害监测系统</h1>
        <p class="brand-subtitle">边缘视觉识别 · 智能决策 · 主动预警</p>
      </div>

      <el-tabs v-model="loginType" class="login-tabs" v-if="!isRegister">
        <el-tab-pane label="密码登录" name="password" />
        <el-tab-pane label="验证码登录" name="sms" />
      </el-tabs>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleSubmit"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item prop="username" v-if="isRegister">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>

        <el-form-item prop="password" v-if="loginType === 'password' || isRegister">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            show-password
          />
        </el-form-item>

        <el-form-item prop="smsCode" v-if="loginType === 'sms' || isRegister">
          <div class="sms-row">
            <el-input v-model="form.smsCode" placeholder="验证码" maxlength="6" />
            <el-button
              :disabled="smsCooldown > 0"
              @click="handleSendSms"
              :loading="smsLoading"
            >
              {{ smsCooldown > 0 ? `${smsCooldown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            class="submit-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isRegister ? '注册' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span class="toggle-text" @click="isRegister = !isRegister">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendSmsCode, register } from '@/api/modules/auth'

const router = useRouter()
const userStore = useUserStore()
const canvasRef = ref<HTMLCanvasElement>()
const formRef = ref<FormInstance>()
const loading = ref(false)
const smsLoading = ref(false)
const smsCooldown = ref(0)
const isRegister = ref(false)
const loginType = ref('password')

const form = reactive({
  phone: '',
  password: '',
  smsCode: '',
  username: '',
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
}

// Particle animation
interface Particle {
  x: number
  y: number
  size: number
  speedX: number
  speedY: number
  opacity: number
  color: string
}

let animationId = 0
let particles: Particle[] = []

function initParticles() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const colors = [
    'rgba(91, 140, 90,',   // sage green
    'rgba(232, 134, 58,',  // citrus orange
    'rgba(127, 168, 126,', // light green
  ]

  particles = Array.from({ length: 55 }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    size: Math.random() * 3 + 1.5,
    speedX: (Math.random() - 0.5) * 0.25,
    speedY: (Math.random() - 0.5) * 0.25,
    opacity: Math.random() * 0.15 + 0.05,
    color: colors[Math.floor(Math.random() * colors.length)],
  }))

  function animate() {
    if (!canvas || !ctx) return
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    particles.forEach(p => {
      p.x += p.speedX + (Math.random() - 0.5) * 0.08
      p.y += p.speedY + (Math.random() - 0.5) * 0.08

      if (p.x < 0) p.x = canvas.width
      if (p.x > canvas.width) p.x = 0
      if (p.y < 0) p.y = canvas.height
      if (p.y > canvas.height) p.y = 0

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      ctx.fillStyle = `${p.color} ${p.opacity})`
      ctx.fill()
    })

    animationId = requestAnimationFrame(animate)
  }

  animate()
}

function handleResize() {
  const canvas = canvasRef.value
  if (!canvas) return
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
}

let smsTimer: ReturnType<typeof setInterval> | null = null

async function handleSendSms() {
  try {
    await formRef.value?.validateField('phone')
  } catch { return }
  smsLoading.value = true
  try {
    await sendSmsCode({ phone: form.phone, scene: isRegister.value ? 'register' : 'login' })
    ElMessage.success('验证码已发送')
    smsCooldown.value = 60
    smsTimer = setInterval(() => {
      smsCooldown.value--
      if (smsCooldown.value <= 0 && smsTimer) { clearInterval(smsTimer); smsTimer = null }
    }, 1000)
  } catch {} finally { smsLoading.value = false }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (isRegister.value) {
      await register({ phone: form.phone, username: form.username, password: form.password, smsCode: form.smsCode })
      ElMessage.success('注册成功')
      isRegister.value = false
    } else if (loginType.value === 'password') {
      await userStore.loginByPwd(form.phone, form.password)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      await userStore.loginBySmsCode(form.phone, form.smsCode)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch {} finally { loading.value = false }
}

onMounted(() => {
  initParticles()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  width: 100vw;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0a100d;
}

.bg-image {
  position: absolute;
  inset: 0;
  background: url('@/assets/images/orchard-bg.png') center center / cover no-repeat;
  z-index: 0;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(10, 16, 13, 0.72) 0%, rgba(27, 32, 40, 0.65) 50%, rgba(10, 16, 13, 0.78) 100%);
  z-index: 1;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.login-card {
  position: relative;
  z-index: 3;
  width: 400px;
  padding: 48px 40px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.35);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand-logo {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  border-radius: 12px;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.92);
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 2px;
}

.login-tabs {
  margin-bottom: 8px;
  :deep(.el-tabs__nav-wrap::after) {
    background-color: rgba(255, 255, 255, 0.08);
  }
  :deep(.el-tabs__item) {
    color: rgba(255, 255, 255, 0.45);
    font-size: 13px;
    &.is-active {
      color: rgba(255, 255, 255, 0.9);
    }
  }
  :deep(.el-tabs__active-bar) {
    background-color: #5B8C5A;
  }
}

:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.06);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  border-radius: 8px;
  transition: all 0.2s;
  &:hover, &.is-focus {
    box-shadow: 0 0 0 1px rgba(91, 140, 90, 0.5) inset;
    background-color: rgba(255, 255, 255, 0.1);
  }
}

:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  &::placeholder {
    color: rgba(255, 255, 255, 0.3);
  }
}

:deep(.el-form-item__error) {
  color: #F2994A;
}

.sms-row {
  display: flex;
  gap: 12px;
  width: 100%;
  .el-input { flex: 1; }
  .el-button {
    flex-shrink: 0;
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.15);
    color: rgba(255, 255, 255, 0.75);
    &:hover {
      border-color: rgba(91, 140, 90, 0.5);
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #5B8C5A, #4A7A49);
  color: #fff;
  letter-spacing: 2px;
  transition: all 0.25s;
  &:hover {
    background: linear-gradient(135deg, #6a9e69, #5B8C5A);
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(91, 140, 90, 0.3);
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.toggle-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: color 0.2s;
  &:hover {
    color: rgba(255, 255, 255, 0.7);
  }
}
</style>
