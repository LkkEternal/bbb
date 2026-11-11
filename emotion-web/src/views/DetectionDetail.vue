<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { authAPI, detectionAPI } from '@/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const user = computed(() => authStore.user.value);
const detail = ref(null);
const showFeedbackModal = ref(false);
const feedbackForm = ref({
  correctLabel: '',
  description: ''
});

const emotionLabels = {
  happy: { cn: '开心', emoji: '😊', color: '#f97316', type: 'positive' },
  sad: { cn: '悲伤', emoji: '😢', color: '#6366f1', type: 'negative' },
  angry: { cn: '生气', emoji: '😠', color: '#ef4444', type: 'negative' },
  neutral: { cn: '平静', emoji: '😐', color: '#eab308', type: 'positive' },
  fear: { cn: '恐惧', emoji: '😨', color: '#9ca3af', type: 'negative' },
  surprise: { cn: '惊讶', emoji: '😲', color: '#8b5cf6', type: 'neutral' },
  disgust: { cn: '厌恶', emoji: '🤢', color: '#6b7280', type: 'negative' },
  unknown: { cn: '未知', emoji: '❓', color: '#9ca3af', type: 'neutral' }
};

const getInsight = (emotions) => {
  if (!emotions || emotions.length === 0) return '暂无情绪数据';
  const sorted = [...emotions].sort((a, b) => b.value - a.value);
  const topEmotion = sorted[0];
  if (topEmotion.value >= 50) {
    return `检测到用户当前情绪以${topEmotion.name}为主（${topEmotion.value.toFixed(1)}%）`;
  } else if (topEmotion.value >= 30) {
    return `用户当前情绪倾向于${topEmotion.name}（${topEmotion.value.toFixed(1)}%）`;
  } else {
    return `用户当前情绪较为复杂，${topEmotion.name}占比最高（${topEmotion.value.toFixed(1)}%）`;
  }
};

const loadDetail = async () => {
  try {
    const res = await detectionAPI.getDetail(route.params.id);
    detail.value = res.data.data;
    setTimeout(() => renderChart(), 100);
  } catch (e) {
    ElMessage.error('获取详情失败');
    router.push('/history');
  }
};

const chartData = ref([]);
const insightText = ref('');

const renderChart = () => {
  const faceData = detail.value?.detectDetail?.faces?.[0];
  const emotionsData = faceData?.allEmotions || faceData?.emotions;
  
  if (!emotionsData || !Array.isArray(emotionsData)) {
    chartData.value = [];
    insightText.value = '暂无情绪数据';
    return;
  }
  
  chartData.value = emotionsData.map(e => ({
    name: emotionLabels[e.label]?.cn || e.label,
    emoji: emotionLabels[e.label]?.emoji || '❓',
    value: parseFloat(((e.confidence || e.probability || 0) * 100).toFixed(1)),
    color: emotionLabels[e.label]?.color || '#6366f1'
  })).sort((a, b) => b.value - a.value);
  
  insightText.value = getInsight(chartData.value);
};

const openFeedback = () => { showFeedbackModal.value = true; };

const submitFeedback = async () => {
  if (!feedbackForm.value.correctLabel) {
    ElMessage.warning('请选择正确的表情标签');
    return;
  }
  try {
    await detectionAPI.submitFeedback(detail.value.recordId, {
      correctLabel: feedbackForm.value.correctLabel,
      description: feedbackForm.value.description || undefined
    });
    ElMessage.success('反馈提交成功');
    showFeedbackModal.value = false;
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败');
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

onMounted(() => { loadDetail(); });
</script>

<template>
  <div class="detail-page">
    <main class="main-content" v-if="detail">
      <div class="detail-header">
        <div class="header-left">
          <button @click="router.push('/history')" class="back-btn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 19l-7-7 7-7"/>
            </svg>
            返回列表
          </button>
          <h1 class="page-title">检测详情</h1>
        </div>
        <button @click="openFeedback" class="feedback-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 5H6a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h11a2 2 0 0 0 2-2v-5m-1.414-9.414a2 2 0 1 1 2.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
          </svg>
          纠错反馈
        </button>
      </div>

      <div class="detail-grid">
        <div class="primary-card">
          <div class="image-section">
            <div class="image-wrapper">
              <img :src="detail.imageUrl" alt="检测图片" class="detection-image" />
              <div class="image-overlay">
                <span class="emotion-badge" :style="{ background: emotionLabels[detail.emotionLabel]?.color }">
                  {{ emotionLabels[detail.emotionLabel]?.emoji }}
                </span>
              </div>
            </div>
            <div class="emotion-result">
              <span class="emotion-text">{{ emotionLabels[detail.emotionLabel]?.emoji }} {{ emotionLabels[detail.emotionLabel]?.cn }}</span>
              <span class="confidence-value">{{ (detail.confidence * 100).toFixed(1) }}%</span>
            </div>
          </div>
          
          <div class="stats-section">
            <div class="stat-card">
              <span class="stat-label">检测到人脸</span>
              <span class="stat-num">{{ detail.faceCount }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">有效人脸</span>
              <span class="stat-num">{{ detail.validFaceCount }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">检测耗时</span>
              <span class="stat-num">{{ detail.processingTimeMs }}ms</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">检测时间</span>
              <span class="stat-num">{{ detail.createdAt }}</span>
            </div>
          </div>
          <div class="advice-section" v-if="detail.adviceText">
            <div class="advice-header">
              <svg class="advice-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M13 2L3 14l9 3 9-3-10-12z"/>
              </svg>
              <h3>心理健康建议</h3>
            </div>
            <p class="advice-content">{{ detail.adviceText }}</p>
            <div class="advice-meta">
              <span>来源: {{ detail.adviceSource }}</span>
              <span>类型: {{ detail.adviceSourceType === 'llm' ? 'AI生成' : detail.adviceSourceType === 'dict' ? '兜底字典' : '无' }}</span>
            </div>
          </div>
        </div>

        <div class="side-panel">
          <div class="env-card">
            <div class="card-header">
              <svg class="card-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"/>
              </svg>
              <h3>环境数据</h3>
            </div>
            <div class="env-grid">
              <div class="env-item">
                <span class="env-icon">🌤️</span>
                <span class="env-text">{{ detail.environment?.weatherMain || '-' }}</span>
              </div>
              <div class="env-item">
                <span class="env-icon">🌡️</span>
                <span class="env-text">{{ detail.environment?.temperature || '-' }}°C</span>
              </div>
              <div class="env-item">
                <span class="env-icon">💧</span>
                <span class="env-text">{{ detail.environment?.humidity || '-' }}%</span>
              </div>
              <div class="env-item">
                <span class="env-icon">💨</span>
                <span class="env-text">{{ detail.environment?.windDir || '-' }}</span>
              </div>
              <div class="env-item">
                <span class="env-icon">🌬️</span>
                <span class="env-text">{{ detail.environment?.windScale || '-' }}级</span>
              </div>
              <div class="env-item">
                <span class="env-icon">📍</span>
                <span class="env-text">{{ detail.environment?.locationName || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="chart-card">
            <div class="card-header">
              <svg class="card-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a4 4 0 0 0-4 4v1H7a3 3 0 0 0-3 3v1H3a2 2 0 0 0 0 4h1v1a3 3 0 0 0 3 3h1v1a4 4 0 0 0 8 0v-1h1a3 3 0 0 0 3-3v-1h1a2 2 0 0 0 0-4h-1v-1a3 3 0 0 0-3-3h-1V6a4 4 0 0 0-4-4Z"/>
                <circle cx="9" cy="11" r="1" fill="currentColor"/>
                <circle cx="15" cy="11" r="1" fill="currentColor"/>
                <path d="M9 15h6"/>
              </svg>
              <h3>模型分析</h3>
            </div>
            <div class="chart-content">
              <div class="insight-box">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="16" x2="12" y2="12"/>
                  <line x1="8" y1="9" x2="12" y2="12"/>
                  <line x1="16" y1="9" x2="12" y2="12"/>
                </svg>
                <span>{{ insightText }}</span>
              </div>
              <div class="bar-chart">
                <div 
                  v-for="(item, index) in chartData" 
                  :key="index" 
                  class="bar-item"
                >
                  <span class="bar-emoji">{{ item.emoji }}</span>
                  <span class="bar-label">{{ item.name }}</span>
                  <div class="bar-track">
                    <div 
                      class="bar-fill" 
                      :style="{ width: item.value + '%', background: item.color }"
                    ></div>
                  </div>
                  <span class="bar-value">{{ item.value }}%</span>
                </div>
              </div>
              <div class="age-gender-box" v-if="detail.modelGender || detail.modelAge != null || detail.modelRange">
                <div class="age-gender-item">
                  <span class="ag-icon">👤</span>
                  <span class="ag-label">性别</span>
                  <span class="ag-value">{{ detail.modelGender || '-' }}</span>
                </div>
                <div class="age-gender-item">
                  <span class="ag-icon">🎂</span>
                  <span class="ag-label">年龄</span>
                  <span class="ag-value">{{ detail.modelAge != null ? detail.modelAge + ' 岁' : '-' }}</span>
                </div>
                <div class="age-gender-item">
                  <span class="ag-icon">📊</span>
                  <span class="ag-label">年龄段</span>
                  <span class="ag-value">{{ detail.modelRange || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showFeedbackModal" class="modal-overlay" @click.self="showFeedbackModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>纠错反馈</h3>
          <button @click="showFeedbackModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <p>当前识别结果: {{ emotionLabels[detail?.emotionLabel]?.emoji }} {{ emotionLabels[detail?.emotionLabel]?.cn }}</p>
          <p>请选择正确的表情:</p>
          <div class="emotion-grid">
            <button 
              v-for="(info, label) in emotionLabels" 
              :key="label"
              @click="feedbackForm.correctLabel = label"
              class="emotion-btn"
              :class="{ active: feedbackForm.correctLabel === label }"
            >
              <span class="emoji">{{ info.emoji }}</span>
              <span>{{ info.cn }}</span>
            </button>
          </div>
          <div class="form-group">
            <label>补充说明（选填）</label>
            <textarea v-model="feedbackForm.description" rows="3" placeholder="请描述您的纠正原因..." class="form-textarea"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showFeedbackModal = false" class="btn-secondary">取消</button>
          <button @click="submitFeedback" class="btn-primary">提交反馈</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #fef3f2 100%);
  padding-left: var(--page-offset);
}

.main-content {
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: white;
  border: none;
  border-radius: 12px;
  color: #6366f1;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateX(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e1b4b;
}

.feedback-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: none;
  border-radius: 12px;
  color: #b45309;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.feedback-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.3);
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 24px;
}

.primary-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.image-section {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.image-wrapper {
  position: relative;
  width: 100%;
  margin: 0 auto 20px;
}

.detection-image {
  width: 100%;
  height: auto;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  display: block;
}

.image-overlay {
  position: absolute;
  top: -12px;
  right: -12px;
}

.emotion-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.emotion-result {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.emotion-text {
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
}

.confidence-value {
  padding: 6px 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border-radius: 20px;
  font-weight: 600;
  font-size: 16px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 24px 32px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 14px;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #6366f1;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.env-card, .chart-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.card-icon {
  color: #6366f1;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
}

.env-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 20px;
}

.env-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.env-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.env-text {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.chart-content {
  padding: 20px 24px 24px;
}

.age-gender-box {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.age-gender-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  background: #f8fafc;
  border-radius: 12px;
}

.ag-icon {
  font-size: 22px;
}

.ag-label {
  font-size: 12px;
  color: #94a3b8;
}

.ag-value {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.insight-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #faf5ff 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #334155;
  line-height: 1.5;
}

.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-emoji {
  font-size: 20px;
  width: 28px;
  text-align: center;
}

.bar-label {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
  width: 40px;
}

.bar-track {
  flex: 1;
  height: 24px;
  background: #f1f5f9;
  border-radius: 12px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.8s ease-out;
  min-width: 4px;
}

.bar-value {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  width: 48px;
  text-align: right;
}

.advice-section {
  padding: 24px 32px;
  border-top: 1px solid #f1f5f9;
}

.advice-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.advice-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
}

.advice-icon {
  color: #22c55e;
}

.advice-section .advice-content {
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
  margin-bottom: 12px;
}

.advice-section .advice-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94a3b8;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 520px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f1f5f9;
  border-radius: 10px;
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
}

.modal-body {
  padding: 24px;
}

.modal-body p {
  margin-bottom: 16px;
  color: #334155;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.emotion-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.emotion-btn:hover {
  border-color: #6366f1;
  background: #faf5ff;
}

.emotion-btn.active {
  border-color: #6366f1;
  background: #faf5ff;
}

.emotion-btn .emoji {
  font-size: 28px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #334155;
}

.form-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
}

.form-textarea:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #f1f5f9;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  padding: 12px 28px;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #f1f5f9;
  border: none;
  color: #64748b;
  padding: 12px 28px;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #e2e8f0;
}
</style>