<template>
  <div class="login-page">
    <!-- 左侧：实景果园图 -->
    <div class="login-banner">
      <div class="banner-image"></div>
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <div class="banner-brand">
          <div class="banner-logo">
            <img src="@/assets/images/logo.png" alt="logo" />
          </div>
          <span class="banner-title">脐橙病虫害监测系统</span>
        </div>
        <div class="banner-quote">
          <p class="quote-text">用 AI 守护每一棵脐橙树</p>
          <p class="quote-sub">边缘视觉识别 · 智能决策 · 主动预警</p>
        </div>
        <div class="banner-features">
          <div class="feature-item" v-for="f in features" :key="f.label">
            <div class="feature-dot"></div>
            <span>{{ f.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：登录表单 -->
    <div class="login-form-panel">
      <div class="form-container">
        <div class="form-header">
          <div class="form-logo">
            <img src="@/assets/images/logo.png" alt="logo" />
          </div>
          <h1 class="form-title">欢迎登录</h1>
          <p class="form-subtitle">脐橙病虫害监测系统管理后台</p>
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
          class="login-form"
          @keyup.enter="handleSubmit"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="手机号"
              maxlength="11"
              :prefix-icon="Iphone"
            />
          </el-form-item>

          <el-form-item prop="username" v-if="isRegister">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password" v-if="loginType === 'password' || isRegister">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="smsCode" v-if="loginType === 'sms' || isRegister">
            <div class="sms-row">
              <el-input
                v-model="form.smsCode"
                placeholder="验证码"
                maxlength="6"
                :prefix-icon="ChatDotRound"
              />
              <el-button
                :disabled="smsCooldown > 0"
                class="sms-btn"
                @click="handleSendSms"
              >
                {{ smsCooldown > 0 ? `${smsCooldown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="password" v-if="isRegister">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
            >
              {{ isRegister ? '注 册' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-footer">
          <span v-if="!isRegister">
            还没有账号？
            <a class="form-link" @click="isRegister = true">立即注册</a>
          </span>
          <span v-else>
            已有账号？
            <a class="form-link" @click="isRegister = false">返回登录</a>
          </span>
        </div>

        <div class="form-version">v1.0.0</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Iphone, User, Lock, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const isRegister = ref(false)
const loginType = ref<'password' | 'sms'>('password')
const smsCooldown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const formRef = ref<FormInstance>()

const form = reactive({
  phone: '',
  username: '',
  password: '',
  confirmPassword: '',
  smsCode: '',
})

const rules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

const features = [
  { label: 'AI 病害智能识别' },
  { label: 'IoT 设备实时监控' },
  { label: '气象环境自动采集' },
  { label: '预警消息精准推送' },
]

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || loading.value) return

  loading.value = true
  try {
    if (isRegister.value) {
      // TODO: 调用注册接口
      // await userStore.register({ phone: form.phone, password: form.password, smsCode: form.smsCode })
      ElMessage.success('注册成功')
      isRegister.value = false
    } else if (loginType.value === 'password') {
      await userStore.loginByPwd(form.phone, form.password)
      router.push('/dashboard')
    } else {
      await userStore.loginBySmsCode(form.phone, form.smsCode)
      router.push('/dashboard')
    }
  } catch {
    // 错误由 Axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

function handleSendSms() {
  if (smsCooldown.value > 0) return
  // TODO: 调用发送验证码接口
  // sendSmsCode({ phone: form.phone })
  ElMessage.success('验证码已发送')
  smsCooldown.value = 60
  smsTimer = setInterval(() => {
    smsCooldown.value--
    if (smsCooldown.value <= 0 && smsTimer) {
      clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.login-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: $canvas;
}

// ═══════════════════════════════════════════════════════════
// 左侧：实景图
// ═══════════════════════════════════════════════════════════
.login-banner {
  position: relative;
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.banner-image {
  position: absolute;
  inset: 0;
  // 实景脐橙果园图 — 可替换为真实图片 URL
  background:
    url('https://images.unsplash.com/photo-1547514701-42782101795e?w=1600&q=80')
    center / cover no-repeat;
  // 降级渐变
  background-color: #3D4F3A;
  transition: transform 0.3s ease;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(46, 52, 40, 0.7) 0%,
    rgba(46, 52, 40, 0.4) 40%,
    rgba(46, 52, 40, 0.2) 100%
  );
}

.banner-content {
  position: relative;
  z-index: 1;
  padding: 60px;
  max-width: 520px;
  color: #fff;
  animation: bannerFadeIn 0.8s ease forwards;
}

@keyframes bannerFadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.banner-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 40px;

  .banner-logo {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.95);
    padding: 4px;
    display: flex;
    align-items: center;
    justify-content: center;

    img { width: 100%; height: 100%; object-fit: contain; }
  }

  .banner-title {
    font-size: 22px;
    font-weight: 700;
    letter-spacing: 1px;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
}

.banner-quote {
  margin-bottom: 48px;

  .quote-text {
    font-size: 32px;
    font-weight: 300;
    line-height: 1.3;
    margin-bottom: 12px;
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  }

  .quote-sub {
    font-size: 15px;
    opacity: 0.7;
    font-weight: 300;
    letter-spacing: 2px;
  }
}

.banner-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 400;
  opacity: 0.85;
  animation: featureSlideIn 0.5s ease forwards;
  animation-delay: calc(0.15s * var(--i, 0));
  opacity: 0;

  &:nth-child(1) { --i: 1; }
  &:nth-child(2) { --i: 2; }
  &:nth-child(3) { --i: 3; }
  &:nth-child(4) { --i: 4; }
}

@keyframes featureSlideIn {
  from { opacity: 0; transform: translateX(-12px); }
  to { opacity: 0.85; transform: translateX(0); }
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $accent;
  flex-shrink: 0;
  box-shadow: 0 0 8px rgba(232, 134, 58, 0.4);
}

// ═══════════════════════════════════════════════════════════
// 右侧：登录表单
// ═══════════════════════════════════════════════════════════
.login-form-panel {
  width: 480px;
  min-width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: $card;
  position: relative;
  overflow-y: auto;

  // 左侧分隔装饰
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 10%;
    bottom: 10%;
    width: 1px;
    background: linear-gradient(180deg, transparent, $border, transparent);
  }
}

.form-container {
  width: 360px;
  padding: 40px 0;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;

  .form-logo {
    width: 56px;
    height: 56px;
    margin: 0 auto 16px;
    background: $primary-subtle;
    border-radius: 14px;
    padding: 6px;
    display: flex;
    align-items: center;
    justify-content: center;

    img { width: 100%; height: 100%; object-fit: contain; }
  }

  .form-title {
    font-size: 26px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
    letter-spacing: -0.5px;
  }

  .form-subtitle {
    font-size: 14px;
    color: $text-secondary;
  }
}

.login-tabs {
  margin-bottom: 24px;

  :deep(.el-tabs__nav-wrap::after) {
    background-color: $divider;
  }

  :deep(.el-tabs__active-bar) {
    background-color: $primary;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    color: $text-secondary;
    transition: color 0.2s;

    &.is-active {
      color: $primary;
      font-weight: 600;
    }
  }
}

.login-form {
  .sms-row {
    display: flex;
    gap: 12px;

    .el-input { flex: 1; }
  }

  .sms-btn {
    width: 110px;
    flex-shrink: 0;
  }

  .submit-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 2px;
    border-radius: 10px;
    transition: all 0.25s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 16px rgba(91, 140, 90, 0.3);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: $text-secondary;
}

.form-link {
  color: $primary;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;
  text-decoration: none;

  &:hover {
    color: $primary-hover;
    text-decoration: underline;
  }
}

.form-version {
  text-align: center;
  margin-top: 40px;
  font-size: 12px;
  color: $text-placeholder;
}

// ── 响应式 ────────────────────────────────────────────────
@media (max-width: 900px) {
  .login-banner { display: none; }
  .login-form-panel {
    width: 100%;
    min-width: 0;

    &::before { display: none; }
  }
}
</style>
