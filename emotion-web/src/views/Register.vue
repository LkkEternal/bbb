<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { authAPI } from '@/api';
import { useRouter } from 'vue-router';

const router = useRouter();

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  address: ''
});

const loading = ref(false);

const handleRegister = async () => {
  if (!form.value.username || !form.value.email || !form.value.password || !form.value.address) {
    ElMessage.warning('请填写所有必填项');
    return;
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }
  
  if (form.value.password.length < 6) {
    ElMessage.warning('密码长度至少6位');
    return;
  }
  
  loading.value = true;
  try {
    const res = await authAPI.register({
      username: form.value.username,
      email: form.value.email,
      password: form.value.password,
      nickname: form.value.nickname || undefined,
      address: form.value.address
    });
    ElMessage.success('注册成功');
    router.push('/login');
  } catch (e) {
    const code = e.code || e.response?.data?.code;
    const msg = e.message || e.response?.data?.message;
    if (code === 1006) {
      ElMessage.error('该手机号已注册，请直接登录');
    } else if (code === 1007) {
      ElMessage.error('该邮箱已注册，请直接登录');
    } else if (code === 1008) {
      ElMessage.error('该手机号已注册，请直接登录');
    } else {
      ElMessage.error(msg || '注册失败');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-left">
        <div class="logo-section">
          <div class="logo">
            <span class="smiley">😊</span>
          </div>
          <h1>表情检测系统</h1>
          <p>开启您的情绪之旅</p>
        </div>
        <div class="benefits">
          <div class="benefit-item">
            <span class="number">1</span>
            <span>上传照片</span>
          </div>
          <div class="benefit-item">
            <span class="number">2</span>
            <span>AI分析情绪</span>
          </div>
          <div class="benefit-item">
            <span class="number">3</span>
            <span>获取专业建议</span>
          </div>
        </div>
      </div>
      <div class="register-right">
        <div class="card">
          <div class="card-header">
            <h2>创建账号</h2>
            <p>开启您的心理健康之旅</p>
          </div>
          <form @submit.prevent="handleRegister">
            <div class="form-row">
              <div class="form-group">
                <label>手机号 *</label>
                <input 
                  v-model="form.username" 
                  type="tel" 
                  placeholder="请输入手机号"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label>邮箱 *</label>
                <input 
                  v-model="form.email" 
                  type="email" 
                  placeholder="请输入邮箱"
                  class="form-input"
                />
              </div>
            </div>
            <div class="form-group">
              <label>昵称</label>
              <input 
                v-model="form.nickname" 
                type="text" 
                placeholder="请输入昵称（选填）"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>城市地址 *</label>
              <input 
                v-model="form.address" 
                type="text" 
                placeholder="例如，广州市天河区"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>密码 *</label>
              <input 
                v-model="form.password" 
                type="password" 
                placeholder="6-20位，需含字母和数字"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>确认密码 *</label>
              <input 
                v-model="form.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码"
                class="form-input"
              />
            </div>
            <button type="submit" class="btn-primary btn-block" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? '注册中...' : '立即注册' }}
            </button>
          </form>
          <div class="links">
            <span>已有账号？</span>
            <button @click="router.push('/login')" class="link-btn">立即登录</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.register-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.register-left {
  width: 50%;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
  padding: 60px 45px;
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

.benefits {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.benefit-item .number {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.register-right {
  width: 50%;
  padding: 45px;
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

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.form-group {
  margin-bottom: 20px;
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