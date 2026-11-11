<script setup>import { ref, onMounted, computed, watch } from 'vue';
import { ElMessage, ElPagination } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { adminAPI } from '@/api';
import { useRoute } from 'vue-router';
const route = useRoute();
const authStore = useAuthStore();
const user = computed(() => authStore.user.value);
const validTabs = ['users', 'feedbacks', 'rules', 'dict', 'logs'];
const initialTab = route.query.tab;
const activeTab = ref(validTabs.includes(initialTab) ? initialTab : 'users');
const users = ref([]);
const usersTotal = ref(0);
const usersPage = ref(1);
const usersSize = ref(10);
const searchKeyword = ref('');
const feedbacks = ref([]);
const feedbacksTotal = ref(0);
const feedbacksPage = ref(1);
const feedbacksSize = ref(10);
const alertRules = ref([]);
const ruleEditVisible = ref(false);
const ruleEditForm = ref({
  id: null,
  ruleName: '',
  emotionLabels: '',
  weatherConditions: '',
  minTemperature: null,
  maxTemperature: null,
  minHumidity: null,
  maxHumidity: null,
  minOccurrences: 1,
  timeWindowHours: 24,
  cooldownMinutes: 30
});
const emotionDict = ref([]);
const logs = ref([]);
const logsTotal = ref(0);
const logsPage = ref(1);
const logsSize = ref(10);
const logsModuleFilter = ref('');
const logsActionFilter = ref('');
const userDetailVisible = ref(false);
const userDetail = ref(null);
const emotionLabels = {
 happy: { cn: '开心', emoji: '😊' },
 sad: { cn: '悲伤', emoji: '😢' },
 angry: { cn: '生气', emoji: '😠' },
 neutral: { cn: '平静', emoji: '😐' },
 fear: { cn: '恐惧', emoji: '😨' },
 surprise: { cn: '惊讶', emoji: '😲' },
 disgust: { cn: '厌恶', emoji: '🤢' },
 unknown: { cn: '未知', emoji: '❓' }
};
const statusMap = {
 0: '禁用',
 1: '启用'
};
const roleMap = {
 1: '普通用户',
 2: '管理员'
};
const feedbackStatusMap = {
 0: '待审核',
 1: '已采用',
 2: '已驳回'
};
const logModuleMap = {
 'user': '用户管理',
 'detection': '表情检测',
 'system': '系统管理',
 'chat': 'AI聊天'
};
const logActionMap = {
 'create': '创建',
 'update': '更新',
 'delete': '删除',
 'review': '审核',
 'login': '登录',
 'logout': '退出'
};
const loadUsers = async () => {
 try {
 const res = await adminAPI.getUsers({
   page: usersPage.value,
   size: usersSize.value,
   keyword: searchKeyword.value || undefined
 });
 users.value = res.data.data.records;
 usersTotal.value = res.data.data.total;
 }
 catch (e) {
 ElMessage.error('获取用户列表失败');
 }
};
const loadUserDetail = async (userId) => {
 try {
 const res = await adminAPI.getUserDetail(userId);
 userDetail.value = res.data.data;
 userDetailVisible.value = true;
 }
 catch (e) {
 ElMessage.error('获取用户详情失败');
 }
};
const updateUserStatus = async (userId, status) => {
 try {
 await adminAPI.updateUserStatus(userId, { status: status === 1 ? 0 : 1 });
 loadUsers();
 ElMessage.success('操作成功');
 }
 catch (e) {
 ElMessage.error('操作失败');
 }
};
const loadFeedbacks = async () => {
 try {
 const res = await adminAPI.getFeedbacks({
   page: feedbacksPage.value,
   size: feedbacksSize.value
 });
 feedbacks.value = res.data.data.records;
 feedbacksTotal.value = res.data.data.total;
 }
 catch (e) {
 ElMessage.error('获取反馈列表失败');
 }
};
const reviewFeedback = async (feedbackId, status) => {
 try {
 await adminAPI.reviewFeedback(feedbackId, { status });
 loadFeedbacks();
 ElMessage.success('审核成功');
 }
 catch (e) {
 ElMessage.error('审核失败');
 }
};
const loadAlertRules = async () => {
  try {
    const res = await adminAPI.getAlertRules();
    alertRules.value = res.data.data;
  }
  catch (e) {
    ElMessage.error('获取预警规则失败');
  }
};
const toggleRuleEnabled = async (rule) => {
  try {
    await adminAPI.updateAlertRule(rule.id, { enabled: !rule.enabled });
    loadAlertRules();
    ElMessage.success(rule.enabled ? '已禁用' : '已启用');
  } catch (e) {
    ElMessage.error('操作失败');
  }
};
const openRuleEdit = (rule) => {
  ruleEditForm.value = {
    id: rule.id,
    ruleName: rule.ruleName,
    emotionLabels: rule.emotionLabels || '',
    weatherConditions: rule.weatherConditions || '',
    minTemperature: rule.minTemperature,
    maxTemperature: rule.maxTemperature,
    minHumidity: rule.minHumidity,
    maxHumidity: rule.maxHumidity,
    minOccurrences: rule.minOccurrences,
    timeWindowHours: rule.timeWindowHours,
    cooldownMinutes: rule.cooldownMinutes
  };
  ruleEditVisible.value = true;
};
const submitRuleEdit = async () => {
  try {
    await adminAPI.updateAlertRule(ruleEditForm.value.id, {
      ruleName: ruleEditForm.value.ruleName,
      emotionLabels: ruleEditForm.value.emotionLabels,
      weatherConditions: ruleEditForm.value.weatherConditions || null,
      minTemperature: ruleEditForm.value.minTemperature,
      maxTemperature: ruleEditForm.value.maxTemperature,
      minHumidity: ruleEditForm.value.minHumidity,
      maxHumidity: ruleEditForm.value.maxHumidity,
      minOccurrences: ruleEditForm.value.minOccurrences,
      timeWindowHours: ruleEditForm.value.timeWindowHours,
      cooldownMinutes: ruleEditForm.value.cooldownMinutes
    });
    ruleEditVisible.value = false;
    loadAlertRules();
    ElMessage.success('更新成功');
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败');
  }
};
const loadEmotionDict = async () => {
 try {
 const res = await adminAPI.getEmotionDict();
 emotionDict.value = res.data.data;
 }
 catch (e) {
 ElMessage.error('获取表情字典失败');
 }
};
const loadLogs = async () => {
 try {
 const res = await adminAPI.getLogs({
   page: logsPage.value,
   size: logsSize.value,
   module: logsModuleFilter.value || undefined,
   action: logsActionFilter.value || undefined
 });
 logs.value = res.data.data.records;
 logsTotal.value = res.data.data.total;
 }
 catch (e) {
 ElMessage.error('获取操作日志失败');
 }
};
const getFeedbackStatusClass = (status) => {
  switch(status) {
    case 0: return 'pending';
    case 1: return 'approved';
    case 2: return 'rejected';
    default: return '';
  }
};
const formatTempRange = (rule) => {
  if (rule.minTemperature == null && rule.maxTemperature == null) return '-';
  const min = rule.minTemperature != null ? rule.minTemperature : '-∞';
  const max = rule.maxTemperature != null ? rule.maxTemperature : '+∞';
  return `${min} ~ ${max} ℃`;
};
const formatHumidityRange = (rule) => {
  if (rule.minHumidity == null && rule.maxHumidity == null) return '-';
  const min = rule.minHumidity != null ? rule.minHumidity : '0';
  const max = rule.maxHumidity != null ? rule.maxHumidity : '不限';
  return `${min}% ~ ${max === '不限' ? max : max + '%'}`;
};

watch(activeTab, (tab) => {
  if (tab === 'users') loadUsers();
  else if (tab === 'feedbacks') loadFeedbacks();
  else if (tab === 'rules') loadAlertRules();
  else if (tab === 'dict') loadEmotionDict();
  else if (tab === 'logs') loadLogs();
});

watch(() => route.query.tab, (tab) => {
  if (tab && validTabs.includes(tab) && tab !== activeTab.value) {
    activeTab.value = tab;
  }
});

onMounted(() => {
  if (activeTab.value === 'users') loadUsers();
  else if (activeTab.value === 'feedbacks') loadFeedbacks();
  else if (activeTab.value === 'rules') loadAlertRules();
  else if (activeTab.value === 'dict') loadEmotionDict();
  else if (activeTab.value === 'logs') loadLogs();
});
</script>

<template>
  <div class="admin-page">
    <main class="admin-content">
      <div class="main-panel">
        <div v-show="activeTab === 'users'" class="tab-content">
          <div class="panel-header">
            <h2>用户管理</h2>
            <div class="search-bar">
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="搜索账号或昵称"
                class="search-input"
                @keyup.enter="loadUsers"
              />
              <button @click="loadUsers" class="search-btn">搜索</button>
            </div>
          </div>
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>账号</th>
                  <th>邮箱</th>
                  <th>昵称</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>注册时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.userId">
                  <td>{{ user.userId }}</td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.email }}</td>
                  <td>{{ user.nickname }}</td>
                  <td>{{ roleMap[user.role] || '未知' }}</td>
                  <td>
                    <span class="status-tag" :class="{ active: user.status === 1 }">
                      {{ statusMap[user.status] || '未知' }}
                    </span>
                  </td>
                  <td>{{ user.createdAt }}</td>
                  <td>
                    <div class="action-group">
                      <button 
                        @click="loadUserDetail(user.userId)"
                        class="action-btn detail-btn"
                      >
                        详情
                      </button>
                      <button 
                        @click="updateUserStatus(user.userId, user.status)"
                        class="action-btn"
                        :class="{ danger: user.status === 1 }"
                      >
                        {{ user.status === 1 ? '禁用' : '启用' }}
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination-container">
            <ElPagination
              :current-page="usersPage"
              :page-size="usersSize"
              :total="usersTotal"
              @current-change="(val) => { usersPage = val; loadUsers(); }"
              @size-change="(val) => { usersSize = val; loadUsers(); }"
              layout="total, prev, pager, next, jumper"
            />
          </div>
        </div>

        <div v-if="activeTab === 'feedbacks'" class="tab-content">
          <div class="panel-header">
            <h2>反馈审核</h2>
            <button @click="loadFeedbacks" class="refresh-btn">刷新</button>
          </div>
          <div class="feedback-list">
            <div v-for="feedback in feedbacks" :key="feedback.feedbackId" class="feedback-card">
              <div class="feedback-header">
                <div class="feedback-info">
                  <span class="user-name">{{ feedback.username }}</span>
                  <span class="feedback-time">{{ feedback.createdAt }}</span>
                </div>
                <span class="status-tag" :class="getFeedbackStatusClass(feedback.status)">
                  {{ feedbackStatusMap[feedback.status] }}
                </span>
              </div>
              <div class="feedback-content">
                <div class="image-container">
                  <img :src="feedback.imageUrl" alt="检测图片" class="feedback-image" />
                </div>
                <div class="feedback-details">
                  <div class="emotion-compare">
                    <span class="emotion-item">
                      系统识别: {{ emotionLabels[feedback.systemLabel]?.emoji }} {{ emotionLabels[feedback.systemLabel]?.cn }}
                    </span>
                    <span class="emotion-item correct">
                      用户纠正: {{ emotionLabels[feedback.correctLabel]?.emoji }} {{ emotionLabels[feedback.correctLabel]?.cn }}
                    </span>
                  </div>
                  <p v-if="feedback.description" class="description">{{ feedback.description }}</p>
                </div>
              </div>
              <div class="feedback-actions" v-if="feedback.status === 0">
                <button @click="reviewFeedback(feedback.feedbackId, 1)" class="approve-btn">采用</button>
                <button @click="reviewFeedback(feedback.feedbackId, 2)" class="reject-btn">驳回</button>
              </div>
            </div>
          </div>
          <div class="pagination-container">
            <ElPagination
              :current-page="feedbacksPage"
              :page-size="feedbacksSize"
              :total="feedbacksTotal"
              @current-change="(val) => { feedbacksPage = val; loadFeedbacks(); }"
              @size-change="(val) => { feedbacksSize = val; loadFeedbacks(); }"
              layout="total, prev, pager, next, jumper"
            />
          </div>
        </div>

        <div v-if="activeTab === 'rules'" class="tab-content">
          <div class="panel-header">
            <h2>预警规则</h2>
            <button @click="loadAlertRules" class="refresh-btn">刷新</button>
          </div>
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>规则名称</th>
                  <th>匹配表情</th>
                  <th>天气条件</th>
                  <th>温度范围</th>
                  <th>湿度范围</th>
                  <th>统计窗口</th>
                  <th>最少次数</th>
                  <th>冷却时间</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="rule in alertRules" :key="rule.id">
                  <td>{{ rule.id }}</td>
                  <td>{{ rule.ruleName }}</td>
                  <td>{{ rule.emotionLabels || '-' }}</td>
                  <td>{{ rule.weatherConditions || '-' }}</td>
                  <td>{{ formatTempRange(rule) }}</td>
                  <td>{{ formatHumidityRange(rule) }}</td>
                  <td>{{ rule.timeWindowHours }}小时</td>
                  <td>{{ rule.minOccurrences }}次</td>
                  <td>{{ rule.cooldownMinutes }}分钟</td>
                  <td>
                    <span class="status-tag" :class="{ active: rule.enabled }">
                      {{ rule.enabled ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td>
                    <div class="action-group">
                      <button @click="openRuleEdit(rule)" class="action-btn detail-btn">编辑</button>
                      <button
                        @click="toggleRuleEnabled(rule)"
                        class="action-btn"
                        :class="{ danger: rule.enabled }"
                      >
                        {{ rule.enabled ? '禁用' : '启用' }}
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="activeTab === 'dict'" class="tab-content">
          <div class="panel-header">
            <h2>表情字典</h2>
            <button @click="loadEmotionDict" class="refresh-btn">刷新</button>
          </div>
          <div class="dict-grid">
            <div v-for="item in emotionDict" :key="item.id" class="dict-card">
              <div class="dict-emoji">{{ item.emojiIcon }}</div>
              <div class="dict-info">
                <div class="dict-name">{{ item.emotionCn }}</div>
                <div class="dict-label">{{ item.emotionLabel }}</div>
              </div>
              <div class="dict-advice">{{ item.defaultAdvice }}</div>
              <div class="dict-footer">
                <span class="status-tag" :class="{ active: item.isActive }">{{ item.isActive ? '启用' : '禁用' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'logs'" class="tab-content">
          <div class="panel-header">
            <h2>操作日志</h2>
            <div class="filter-bar">
              <select v-model="logsModuleFilter" class="filter-select" @change="logsPage = 1; loadLogs()">
                <option value="">全部模块</option>
                <option value="user">用户管理</option>
                <option value="detection">表情检测</option>
                <option value="system">系统管理</option>
                <option value="chat">AI聊天</option>
              </select>
              <select v-model="logsActionFilter" class="filter-select" @change="logsPage = 1; loadLogs()">
                <option value="">全部操作</option>
                <option value="create">创建</option>
                <option value="update">更新</option>
                <option value="delete">删除</option>
                <option value="review">审核</option>
                <option value="login">登录</option>
                <option value="logout">退出</option>
              </select>
              <button @click="loadLogs" class="refresh-btn">刷新</button>
            </div>
          </div>
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户ID</th>
                  <th>模块</th>
                  <th>操作</th>
                  <th>描述</th>
                  <th>操作时间</th>
                  <th>IP地址</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in logs" :key="log.id">
                  <td>{{ log.id }}</td>
                  <td>{{ log.userId }}</td>
                  <td>
                    <span class="module-tag">{{ logModuleMap[log.module] || log.module }}</span>
                  </td>
                  <td>
                    <span class="action-tag">{{ logActionMap[log.action] || log.action }}</span>
                  </td>
                  <td>{{ log.description }}</td>
                  <td>{{ log.operationTime }}</td>
                  <td>{{ log.ipAddress }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination-container">
            <ElPagination
              :current-page="logsPage"
              :page-size="logsSize"
              :total="logsTotal"
              @current-change="(val) => { logsPage = val; loadLogs(); }"
              @size-change="(val) => { logsSize = val; loadLogs(); }"
              layout="total, prev, pager, next, jumper"
            />
          </div>
        </div>
      </div>
    </main>

    <div v-if="userDetailVisible" class="drawer-overlay" @click.self="userDetailVisible = false">
      <div class="drawer-panel">
        <div class="drawer-header">
          <h3>用户详情</h3>
          <button class="drawer-close" @click="userDetailVisible = false">✕</button>
        </div>
        <div class="drawer-body" v-if="userDetail">
          <div class="detail-avatar">
            <img v-if="userDetail.avatarUrl" :src="userDetail.avatarUrl" alt="头像" class="avatar-img" />
            <div v-else class="avatar-placeholder-lg">{{ userDetail.nickname?.charAt(0) || '用' }}</div>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">用户ID</span>
              <span class="detail-value">{{ userDetail.userId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">账号</span>
              <span class="detail-value">{{ userDetail.username }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">昵称</span>
              <span class="detail-value">{{ userDetail.nickname }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">邮箱</span>
              <span class="detail-value">{{ userDetail.email }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">角色</span>
              <span class="detail-value">{{ roleMap[userDetail.role] || '未知' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <span class="status-tag" :class="{ active: userDetail.status === 1 }">{{ statusMap[userDetail.status] || '未知' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">注册时间</span>
              <span class="detail-value">{{ userDetail.createdAt }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">更新时间</span>
              <span class="detail-value">{{ userDetail.updatedAt }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">最后登录时间</span>
              <span class="detail-value">{{ userDetail.lastLoginAt || '未登录' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">最后登录IP</span>
              <span class="detail-value">{{ userDetail.lastLoginIp || '无' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="ruleEditVisible" class="drawer-overlay" @click.self="ruleEditVisible = false">
      <div class="drawer-panel" style="width: 480px;">
        <div class="drawer-header">
          <h3>编辑预警规则</h3>
          <button class="drawer-close" @click="ruleEditVisible = false">✕</button>
        </div>
        <div class="drawer-body">
          <div class="form-group">
            <label class="form-label">规则名称</label>
            <input v-model="ruleEditForm.ruleName" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">匹配表情（英文标签，逗号分隔）</label>
            <input v-model="ruleEditForm.emotionLabels" class="form-input" placeholder="如: sad,angry,fear" />
          </div>
          <div class="form-group">
            <label class="form-label">天气条件（中文，逗号分隔，留空=不限）</label>
            <input v-model="ruleEditForm.weatherConditions" class="form-input" placeholder="如: 小雨,中雨,大雨,暴雨" />
          </div>
          <div class="form-group">
            <label class="form-label">最低温度（℃，留空=不限）</label>
            <input v-model.number="ruleEditForm.minTemperature" type="number" step="0.1" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">最高温度（℃，留空=不限）</label>
            <input v-model.number="ruleEditForm.maxTemperature" type="number" step="0.1" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">最低湿度（%，留空=不限）</label>
            <input v-model.number="ruleEditForm.minHumidity" type="number" min="0" max="100" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">最高湿度（%，留空=不限）</label>
            <input v-model.number="ruleEditForm.maxHumidity" type="number" min="0" max="100" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">统计窗口（小时）</label>
            <input v-model.number="ruleEditForm.timeWindowHours" type="number" min="1" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">最少出现次数</label>
            <input v-model.number="ruleEditForm.minOccurrences" type="number" min="1" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">冷却时间（分钟）</label>
            <input v-model.number="ruleEditForm.cooldownMinutes" type="number" min="1" class="form-input" />
          </div>
          <div class="form-actions">
            <button @click="ruleEditVisible = false" class="cancel-btn">取消</button>
            <button @click="submitRuleEdit" class="submit-btn">保存</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
.admin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  padding: 24px 24px 24px var(--page-offset);
}

.main-panel {
  padding: 0;
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 40px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
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
  color: #1f2937;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-badge {
  background: #fef3c7;
  color: #d97706;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.nickname {
  font-weight: 500;
  color: #374151;
}

.logout-btn {
  padding: 8px 20px;
  background: #fee2e2;
  border: none;
  border-radius: 8px;
  color: #dc2626;
  cursor: pointer;
  font-weight: 500;
}

.admin-content {
  height: calc(100vh - 65px);
}

.main-panel {
  height: 100%;
  padding: 24px;
  overflow-y: auto;
}

.tab-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.search-bar {
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 10px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.search-btn, .refresh-btn {
  padding: 10px 20px;
  background: #6366f1;
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn {
  background: #f3f4f6;
  color: #374151;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 14px;
  text-align: center;
  border-bottom: 1px solid #f3f4f6;
}

.data-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #fee2e2;
  color: #dc2626;
}

.status-tag.active {
  background: #dcfce7;
  color: #166534;
}

.action-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  background: #f3f4f6;
  color: #374151;
}

.action-btn.danger {
  background: #fee2e2;
  color: #dc2626;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-name {
  font-weight: 600;
  color: #374151;
}

.feedback-time {
  font-size: 13px;
  color: #9ca3af;
  margin-left: 10px;
}

.status-tag.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-tag.approved {
  background: #dcfce7;
  color: #166534;
}

.status-tag.rejected {
  background: #fee2e2;
  color: #dc2626;
}

.feedback-content {
  display: flex;
  gap: 20px;
}

.image-container {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
}

.feedback-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.feedback-details {
  flex: 1;
}

.emotion-compare {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.emotion-item {
  font-size: 14px;
  color: #374151;
}

.emotion-item.correct {
  color: #6366f1;
  font-weight: 500;
}

.description {
  color: #6b7280;
  font-size: 14px;
}

.feedback-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.approve-btn {
  padding: 8px 24px;
  background: #10b981;
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  font-weight: 500;
}

.reject-btn {
  padding: 8px 24px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  font-weight: 500;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rule-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.rule-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.rule-code {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 4px;
}

.rule-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.info-row {
  display: flex;
  gap: 8px;
}

.info-label {
  color: #6b7280;
  font-size: 14px;
}

.dict-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.dict-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.dict-emoji {
  font-size: 48px;
  margin-bottom: 12px;
}

.dict-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.dict-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
}

.dict-advice {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
  margin-bottom: 12px;
}

.dict-footer {
  display: flex;
  justify-content: center;
}

.action-group {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.detail-btn {
  background: #e0e7ff !important;
  color: #4f46e5 !important;
}

.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #374151;
  background: white;
  outline: none;
  cursor: pointer;
}

.filter-select:focus {
  border-color: #6366f1;
}

.module-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #e0e7ff;
  color: #4f46e5;
}

.action-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #f0fdf4;
  color: #166534;
}

.drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: flex;
  justify-content: center;
  align-items: center;
}

.drawer-panel {
  width: 480px;
  max-height: 80vh;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.drawer-close {
  width: 32px;
  height: 32px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drawer-close:hover {
  background: #e5e7eb;
}

.drawer-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.detail-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e2e8f0;
}

.avatar-placeholder-lg {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 28px;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 10px;
}

.detail-label {
  font-size: 14px;
  color: #6b7280;
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.form-group {
  margin-bottom: 18px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: #6366f1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn {
  padding: 8px 20px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #e5e7eb;
}

.submit-btn {
  padding: 8px 24px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.submit-btn:hover {
  opacity: 0.9;
}
</style>