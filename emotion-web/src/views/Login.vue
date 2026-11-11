<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { authAPI } from '@/api';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  account: '',
  password: ''
});

const loading = ref(false);

const handleLogin = async () => {
  if (!form.value.account || !form.value.password) {
    ElMessage.warning('请填写账号和密码');
    return;
  }
  
  loading.value = true;
  try {
    const res = await authAPI.login(form.value);
    authStore.login(res.data.data);
    ElMessage.success('登录成功');
    router.push('/');
  } catch (e) {
    const code = e.code || e.response?.data?.code;
    const msg = e.message || e.response?.data?.message;
    if (code === 1003) {
      ElMessage.error('账号或密码错误，请检查后重新输入');
    } else if (code === 1004) {
      ElMessage.error('该账号已被禁用，请联系管理员');
    } else if (code === 1005) {
      ElMessage.error(msg || '账号已被锁定，请30分钟后再试');
    } else {
      ElMessage.error(msg || '登录失败，请稍后重试');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="logo-section">
          <div class="logo">
            <span class="smiley">😊</span>
          </div>
          <h1>表情检测系统</h1>
          <p>通过AI技术，读懂您的情绪</p>
        </div>
        <div class="feature-list">
          <div class="feature">
            <span class="icon">😊</span>
            <span>实时表情识别</span>
          </div>
          <div class="feature">
            <span class="icon">🤝</span>
            <span>AI心理陪伴</span>
          </div>
          <div class="feature">
            <span class="icon">📊</span>
            <span>情绪数据分析</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="card">
          <div class="card-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账号</p>
          </div>
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label>账号</label>
              <input 
                v-model="form.account" 
                type="text" 
                placeholder="手机号或邮箱"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码"
                class="form-input"
              />
            </div>
            <button type="submit" class="btn-primary btn-block" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>
          <div class="links">
            <span>还没有账号？</span>
            <button @click="router.push('/register')" class="link-btn">立即注册</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.login-left {
  width: 55%;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
  padding: 60px 40px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-section {
  text-align: center;
  margin-bottom: 50px;
}

.logo {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  backdrop-filter: blur(10px);
}

.logo .smiley {
  font-size: 50px;
}

.logo-section h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.logo-section p {
  opacity: 0.85;
  font-size: 15px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.08);
  padding: 16px 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.feature:hover {
  background: rgba(255, 255, 255, 0.12);
  transform: translateX(5px);
}

.feature .icon {
  font-size: 26px;
}

.login-right {
  width: 45%;
  padding: 50px 40px;
  display: flex;
  align-items: center;
}

.card {
  width: 100%;
  background: #f8fafc;
  border-radius: 20px;
  padding: 35px;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-header h2 {
  font-size: 26px;
  font-weight: 600;
  color: #1e1b4b;
  margin-bottom: 8px;
}

.card-header p {
  color: #64748b;
  font-size: 14px;
}

.form-group {
  margin-bottom: 22px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s ease;
  outline: none;
  background: white;
}

.form-input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.15);
}

.form-input::placeholder {
  color: #94a3b8;
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  color: white;
  padding: 14px 32px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-block {
  width: 100%;
}

.links {
  text-align: center;
  margin-top: 24px;
  color: #64748b;
  font-size: 14px;
}

.link-btn {
  background: none;
  border: none;
  color: #6366f1;
  cursor: pointer;
  font-weight: 600;
  margin-left: 6px;
  font-size: 14px;
}

.spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>