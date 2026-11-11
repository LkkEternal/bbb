<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { authAPI, userAPI } from '@/api';
import { useRouter } from 'vue-router';

const router = useRouter();
const authStore = useAuthStore();
const user = computed(() => authStore.user.value);

const pushEnabled = ref(user.value?.pushEnabled === 1);
const emailEnabled = ref(user.value?.emailEnabled === 1);

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNDAiIGhlaWdodD0iMTQwIiB2aWV3Qm94PSIwIDAgMTQwIDE0MCI+PGRlZnM+PGxpbmVhckdyYWRpZW50IGlkPSJnIiB4MT0iMCUiIHkxPSIwJSIgeDI9IjEwMCUiIHkyPSIxMDAlIj48c3RvcCBvZmZzZXQ9IjAlIiBzdG9wLWNvbG9yPSIjNjM2NmUxIi8+PHN0b3Agb2Zmc2V0PSIxMDAlIiBzdG9wLWNvbG9yPSIjOGI1Y2Y2Ii8+PC9saW5lYXJHcmFkaWVudD48L2RlZnM+PGNpcmNsZSBjeD0iNzAiIGN5PSI3MCIgcj0iNzAiIGZpbGw9InVybCgjZykiLz48Y2lyY2xlIGN4PSI3MCIgY3k9IjU1IiByPSIyNiIgZmlsbD0id2hpdGUiIG9wYWNpdHk9IjAuOSIvPjxjaXJjbGUgY3g9IjcwIiBjeT0iMTEwIiByPSIzOCIgZmlsbD0id2hpdGUiIG9wYWNpdHk9IjAuOSIvPjwvc3ZnPg==';

const avatarFile = ref(null);
const avatarPreview = ref(null);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const activeTab = ref('basic');

const handleAvatarChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    avatarFile.value = file;
    avatarPreview.value = URL.createObjectURL(file);
  }
};

const clearAvatarSelection = () => {
  avatarFile.value = null;
  avatarPreview.value = null;
};

const uploadAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.warning('请选择图片');
    return;
  }
  
  try {
    const res = await userAPI.uploadAvatar(avatarFile.value);
    authStore.setUser({ ...authStore.user.value, avatarUrl: res.data.data });
    ElMessage.success('头像上传成功');
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '上传失败');
  }
};

const saveNotificationSettings = async () => {
  try {
    const res = await userAPI.updateNotificationSettings({
      pushEnabled: pushEnabled.value,
      emailEnabled: emailEnabled.value
    });
    authStore.setUser(res.data.data);
    ElMessage.success('通知设置已保存');
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败');
  }
};

const changePassword = async () => {  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写所有字段');
    return;
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度至少6位');
    return;
  }
  
  try {
    await userAPI.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });
    ElMessage.success('密码修改成功');
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败');
  }
};

const handleLogout = async () => {
  try {
    await authAPI.logout();
    authStore.logout();
    router.push('/login');
  } catch (e) {
    authStore.logout();
    router.push('/login');
  }
};
</script>

<template>
  <div class="profile-page">
    <main class="main-content">
      <div class="profile-card">
        <div class="card-header">
          <h2 class="card-title">个人中心</h2>
        </div>
        
        <div class="tabs">
          <button 
            @click="activeTab = 'basic'" 
            class="tab-btn"
            :class="{ active: activeTab === 'basic' }"
          >
            基本信息
          </button>
          <button 
            @click="activeTab = 'password'" 
            class="tab-btn"
            :class="{ active: activeTab === 'password' }"
          >
            修改密码
          </button>
          <button 
            @click="activeTab = 'notify'" 
            class="tab-btn"
            :class="{ active: activeTab === 'notify' }"
          >
            通知设置
          </button>
        </div>

        <div class="tab-content" v-show="activeTab === 'basic'">
          <div class="avatar-section">
            <label class="avatar-container" title="点击更换头像">
              <div class="avatar-ring"></div>
              <img 
                :src="avatarPreview || user?.avatarUrl || defaultAvatar" 
                alt="头像" 
                class="profile-avatar"
              />
              <div class="avatar-hover-mask">
                <span class="hover-text">点击更换头像</span>
              </div>
              <input type="file" accept="image/*" @change="handleAvatarChange" class="avatar-input" />
            </label>
            <div class="avatar-info">
              <span class="avatar-tip">点击头像即可上传新照片</span>
              <span class="avatar-hint">支持 JPG、PNG 格式</span>
            </div>
            <div v-if="avatarFile" class="avatar-actions">
              <button @click="clearAvatarSelection" class="btn-secondary">
                取消
              </button>
              <button @click="uploadAvatar" class="btn-primary">
                保存头像
              </button>
            </div>
          </div>

          <div class="info-display">
            <div class="info-row">
              <span class="info-label">用户ID</span>
              <span class="info-value">{{ user?.userId }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ user?.username }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ user?.email }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">昵称</span>
              <span class="info-value">{{ user?.nickname || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">角色</span>
              <span class="info-value">{{ user?.role === 2 ? '管理员' : '普通用户' }}</span>
            </div>
          </div>
        </div>

        <div class="tab-content" v-show="activeTab === 'password'">
          <div class="form-group">
            <label>原密码 *</label>
            <input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              placeholder="请输入原密码"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>新密码 *</label>
            <input 
              v-model="passwordForm.newPassword" 
              type="password" 
              placeholder="请输入新密码（6-20位）"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>确认新密码 *</label>
            <input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入新密码"
              class="form-input"
            />
          </div>
          <button @click="changePassword" class="btn-primary save-btn">
            修改密码
          </button>
        </div>

        <div class="tab-content" v-show="activeTab === 'notify'">
          <div class="notify-section">
            <p class="notify-tip">开启后，系统将通过站内/邮箱方式向您推送情绪与天气预警提醒。</p>

            <div class="notify-row">
              <div class="notify-info">
                <span class="notify-title">接收预警消息</span>
                <span class="notify-desc">关闭后，情绪预警定时任务对该账号停用（天气预警按地区不受影响）</span>
              </div>
              <el-switch
                v-model="pushEnabled"
                :active-value="true"
                :inactive-value="false"
              />
            </div>

            <div class="notify-row">
              <div class="notify-info">
                <span class="notify-title">邮箱推送</span>
                <span class="notify-desc">开启后，未及时处理的情绪预警会在每日22:00通过邮件提醒</span>
              </div>
              <el-switch
                v-model="emailEnabled"
                :active-value="true"
                :inactive-value="false"
              />
            </div>

            <button @click="saveNotificationSettings" class="btn-primary save-btn">
              保存设置
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f3ff 0%, #fdf2f8 50%, #f0f9ff 100%);
  padding-left: var(--page-offset);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f1f5f9;
  border: none;
  border-radius: 10px;
  color: #64748b;
  cursor: pointer;
  font-size: 14px;
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

.nav {
  display: flex;
  gap: 6px;
}

.nav-item {
  padding: 10px 18px;
  border: none;
  background: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-item.active {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #6366f1;
  font-weight: 600;
}

.nav-item.logout {
  color: #ef4444;
}

.main-content {
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 28px;
}

.card-header {
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.tab-btn {
  padding: 10px 24px;
  border: none;
  background: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: #f1f5f9;
}

.tab-btn.active {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #6366f1;
  font-weight: 600;
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
  padding: 30px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.avatar-container {
  position: relative;
  width: 140px;
  height: 140px;
  margin: 0 auto 20px;
  cursor: pointer;
  display: block;
}

.avatar-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  opacity: 0.2;
  pointer-events: none;
}

.profile-avatar {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.2);
  transition: all 0.3s ease;
}

.avatar-hover-mask {
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
  pointer-events: none;
}

.avatar-container:hover .avatar-hover-mask {
  opacity: 1;
}

.avatar-container:hover .profile-avatar {
  transform: scale(1.02);
}

.hover-text {
  color: white;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
  padding: 0 10px;
}

.avatar-input {
  display: none;
}

.avatar-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.avatar-tip {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.avatar-hint {
  font-size: 12px;
  color: #94a3b8;
}

.avatar-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.avatar-actions .btn-secondary {
  background: white;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 10px 24px;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-actions .btn-secondary:hover {
  background: #f1f5f9;
  color: #334155;
}

.avatar-actions .btn-primary {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.form-group {
  margin-bottom: 20px;
}

.form-grid .form-group {
  margin-bottom: 0;
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

.form-input:disabled {
  background: #f8fafc;
  color: #94a3b8;
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

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
}

.save-btn {
  width: 100%;
}

.info-display {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 100px;
  font-weight: 600;
  color: #64748b;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #1e1b4b;
  font-size: 15px;
}

.notify-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notify-tip {
  font-size: 13px;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px 16px;
}

.notify-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
}

.notify-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notify-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.notify-desc {
  font-size: 12px;
  color: #94a3b8;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>