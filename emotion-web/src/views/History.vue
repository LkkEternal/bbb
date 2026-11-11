<script setup>
import { ref, onMounted, computed, onBeforeUnmount, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { useAuthStore } from '@/stores/auth';
import { authAPI, detectionAPI } from '@/api';
import { useRouter } from 'vue-router';

const router = useRouter();
const authStore = useAuthStore();
const user = computed(() => authStore.user.value);

const historyList = ref([]);
const chartRef = ref(null);
const chartInstance = ref(null);
const contentRef = ref(null);
const scale = ref(1);
const DESIGN_WIDTH = 960;

const updateScale = () => {
  const offsetCss = getComputedStyle(document.documentElement).getPropertyValue('--page-offset').trim();
  const sidebarOffset = parseInt(offsetCss) || 228;
  const available = window.innerWidth - sidebarOffset;
  const targetWidth = available * (2 / 3);
  scale.value = targetWidth / DESIGN_WIDTH;
};
const filterEmotion = ref('');
const currentPage = ref(1);
const pageSize = ref(9);
const total = ref(0);

const emotionLabels = {
  happy: { cn: '开心', emoji: '😊', color: '#22c55e' },
  sad: { cn: '悲伤', emoji: '😢', color: '#3b82f6' },
  angry: { cn: '生气', emoji: '😠', color: '#ef4444' },
  neutral: { cn: '平静', emoji: '😐', color: '#6b7280' },
  fear: { cn: '恐惧', emoji: '😨', color: '#ec4899' },
  surprise: { cn: '惊讶', emoji: '😲', color: '#f59e0b' },
  disgust: { cn: '厌恶', emoji: '🤢', color: '#10b981' },
  unknown: { cn: '未知', emoji: '❓', color: '#9ca3af' }
};

const emotionOptions = [
  { value: '', label: '全部' },
  { value: 'happy', label: '😊 开心' },
  { value: 'sad', label: '😢 悲伤' },
  { value: 'angry', label: '😠 生气' },
  { value: 'neutral', label: '😐 平静' },
  { value: 'fear', label: '😨 恐惧' },
  { value: 'surprise', label: '😲 惊讶' },
  { value: 'disgust', label: '🤢 厌恶' },
  { value: 'unknown', label: '❓ 未知' }
];

const emotionStats = ref({
  happy: 0,
  sad: 0,
  angry: 0,
  neutral: 0,
  fear: 0,
  surprise: 0,
  disgust: 0,
  unknown: 0
});

const loadHistory = async () => {
  try {
    const res = await detectionAPI.getHistory({
      emotionLabel: filterEmotion.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    });
    historyList.value = res.data.data.records;
    total.value = res.data.data.total;
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取历史记录失败');
  }
};

const loadStats = async () => {
  try {
    const res = await detectionAPI.getStats();
    emotionStats.value = res.data.data.emotionCounts;
    renderChart();
  } catch (e) {
    console.error('获取统计失败:', e);
  }
};

const renderChart = () => {
  if (!chartRef.value) return;
  
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }
  
  chartInstance.value = echarts.init(chartRef.value);
  
  const totalCount = Object.values(emotionStats.value).reduce((a, b) => a + b, 0);
  
  const emotions = Object.entries(emotionStats.value)
    .filter(([, value]) => value > 0)
    .map(([label, value]) => ({
      name: emotionLabels[label]?.cn || label,
      value: value,
      itemStyle: { color: emotionLabels[label]?.color || '#9ca3af' }
    }));
  
  chartInstance.value.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#334155' },
      formatter: function(params) {
        return params.name + '：' + params.value + ' 次（' + params.percent + '%）<br/>总检测数：' + totalCount + ' 次';
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      itemWidth: 14,
      itemHeight: 14,
      itemGap: 16,
      icon: 'roundRect',
      textStyle: { color: '#64748b', fontSize: 12 },
      data: Object.entries(emotionStats.value).map(([label]) => ({
        name: emotionLabels[label]?.cn || label,
        itemStyle: { color: emotionLabels[label]?.color || '#9ca3af' }
      }))
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: { show: false },
      data: emotions
    }]
  });
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

const goToDetail = (recordId) => {
  router.push(`/history/detail/${recordId}`);
};

onMounted(() => {
  loadHistory();
  loadStats();
  updateScale();
  window.addEventListener('resize', updateScale);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateScale);
});
</script>

<template>
  <div class="history-page">
    <main class="main-content">
      <div ref="contentRef" class="scale-wrapper" :style="{ transform: `scale(${scale})`, transformOrigin: 'top left' }">
        <div class="stats-section">
          <div class="card stats-card">
            <div class="card-header">
              <h2 class="card-title">检测统计</h2>
              <span class="card-subtitle">共 {{ Object.values(emotionStats).reduce((a, b) => a + b, 0) }} 次检测</span>
            </div>
            <div ref="chartRef" class="chart-container"></div>
          </div>
        </div>

        <div class="history-section">
          <div class="card history-card">
            <div class="card-header">
              <h2 class="card-title">检测历史</h2>
              <div class="filter-bar">
                <select 
                  v-model="filterEmotion" 
                  @change="currentPage = 1; loadHistory()"
                  class="filter-select"
                >
                  <option v-for="opt in emotionOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="history-grid" v-if="historyList.length > 0">
              <div 
                v-for="item in historyList" 
                :key="item.recordId"
                class="history-card-item"
                @click="goToDetail(item.recordId)"
              >
                <img :src="item.imageUrl" alt="检测图片" class="history-image" />
                <div class="history-info">
                  <div class="emotion-tag" :class="'emotion-' + item.emotionLabel">
                    {{ emotionLabels[item.emotionLabel]?.emoji }} {{ emotionLabels[item.emotionLabel]?.cn }}
                  </div>
                  <span class="confidence">置信度: {{ (item.confidence * 100).toFixed(1) }}%</span>
                  <span class="detect-time">{{ item.createdAt }}</span>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <span class="empty-icon">📋</span>
              <p>暂无检测记录</p>
              <button @click="router.push('/')" class="btn-primary">去检测</button>
            </div>

            <div class="pagination" v-if="total > pageSize">
              <button 
                @click="currentPage > 1 && (currentPage--, loadHistory())" 
                :disabled="currentPage <= 1"
                class="page-btn"
              >
                ←
              </button>
              <span class="page-info">第 {{ currentPage }} 页 / 共 {{ Math.ceil(total / pageSize) }} 页</span>
              <button 
                @click="currentPage < Math.ceil(total / pageSize) && (currentPage++, loadHistory())" 
                :disabled="currentPage >= Math.ceil(total / pageSize)"
                class="page-btn"
              >
                →
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.history-page {
  min-height: 100vh;
  background: #f1f5f9;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px 0 24px var(--page-offset);
}

.scale-wrapper {
  width: 960px;
  transform-origin: top center;
  margin: 0 auto;
}

.stats-section {
  width: 100%;
}

.history-section {
  width: 100%;
}

.card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 28px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
}

.stats-card {
  height: fit-content;
}

.chart-container {
  height: 220px;
  margin-bottom: 20px;
}

.stats-summary {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.stat-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #f8fafc;
  border-radius: 20px;
}

.stat-emoji {
  font-size: 18px;
}

.stat-count {
  font-weight: 600;
  color: #334155;
}

.filter-bar {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 10px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  background: white;
  color: #334155;
  font-size: 14px;
  outline: none;
  cursor: pointer;
}

.filter-select:focus {
  border-color: #6366f1;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.history-card-item {
  background: #f8fafc;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.history-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.history-info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.emotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  width: fit-content;
}

.emotion-happy { background: #dcfce7; color: #166534; }
.emotion-sad { background: #dbeafe; color: #1e40af; }
.emotion-angry { background: #fee2e2; color: #991b1b; }
.emotion-neutral { background: #f3f4f6; color: #4b5563; }
.emotion-fear { background: #fce7f3; color: #831843; }
.emotion-surprise { background: #fef9c3; color: #854d0e; }
.emotion-disgust { background: #d1fae5; color: #065f46; }
.emotion-unknown { background: #e5e7eb; color: #6b7280; }

.confidence {
  color: #64748b;
  font-size: 13px;
}

.detect-time {
  color: #94a3b8;
  font-size: 12px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  color: #64748b;
  margin-bottom: 20px;
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  color: white;
  padding: 12px 32px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.page-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #f1f5f9;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #e2e8f0;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #64748b;
  font-size: 14px;
}
</style>