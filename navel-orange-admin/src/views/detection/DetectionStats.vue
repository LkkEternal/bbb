<template>
  <div class="page-container">
    <div class="page-header">
      <h2>识别统计</h2>
      <div class="header-right">
        <el-select
          v-if="!userStore.isAdmin"
          v-model="selectedOrchard"
          placeholder="选择果园"
          clearable
          style="width: 160px"
          @change="handleOrchardChange"
        >
          <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 300px"
          @change="handleDateChange"
        />
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(91, 140, 90, 0.1)">
          <el-icon :size="24" color="#5B8C5A"><DataAnalysis /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalCount ?? 0 }}</div>
          <div class="stat-label">总识别次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(232, 134, 58, 0.1)">
          <el-icon :size="24" color="#E8863A"><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.alertCount ?? 0 }}</div>
          <div class="stat-label">预警次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(69, 130, 196, 0.1)">
          <el-icon :size="24" color="#4582C4"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ avgPerDay }}</div>
          <div class="stat-label">日均识别</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">病害分布</span>
                <span class="card-subtitle">近{{ days }}天统计</span>
              </div>
            </template>
            <div ref="barChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">识别趋势</span>
                <span class="card-subtitle">近{{ days }}天统计</span>
              </div>
            </template>
            <div ref="lineChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最近识别记录 -->
    <el-card shadow="never" class="recent-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">最近识别记录</span>
          <el-button link type="primary" @click="$router.push('/detection')">查看全部</el-button>
        </div>
      </template>
      <div class="detection-grid" v-loading="listLoading">
        <div v-for="item in recentList" :key="item.id" class="detection-card" @click="showDetail(item)">
          <el-image
            :src="item.imageUrl"
            fit="cover"
            class="detection-image"
            :preview-src-list="[item.imageUrl]"
          />
          <div class="detection-overlay">
            <div class="detection-name">
              <DiseaseTag :code="item.topClassCode" />
            </div>
            <div class="detection-detail">
              <span class="detection-conf">{{ (item.topProbability * 100).toFixed(1) }}%</span>
              <span class="detection-orchard">{{ item.orchardName || '-' }}</span>
            </div>
            <div class="detection-time">{{ formatTime(item.createdAt).slice(5, 16) }}</div>
          </div>
        </div>
        <el-empty v-if="!listLoading && recentList.length === 0" description="暂无识别记录" />
      </div>
    </el-card>

    <!-- 识别记录详情弹窗 -->
    <el-dialog v-model="detailVisible" title="识别详情" width="520px" :close-on-click-modal="false">
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="detail-section-title"><span class="title-badge">检测详情</span></div>
            <div class="detail-image-wrap">
              <el-image :src="detailData.imageUrl" fit="contain" class="detail-image" :preview-src-list="[detailData.imageUrl]" />
            </div>
            <table class="detail-table">
              <tr><td class="dt-label">病害名称</td><td class="dt-value">{{ detailData.diseaseName || '—' }}</td></tr>
              <tr><td class="dt-label">识别置信度</td><td class="dt-value">{{ detailData.topProbability ? (Number(detailData.topProbability) * 100).toFixed(1) + '%' : '—' }}</td></tr>
              <tr><td class="dt-label">识别时间</td><td class="dt-value">{{ detailData.deviceTimestamp ? formatTime(detailData.deviceTimestamp) : (detailData.createdAt ? formatTime(detailData.createdAt) : '—') }}</td></tr>
            </table>
          </div>
          <div class="detail-section">
            <div class="detail-section-title"><span class="title-badge">防治建议</span></div>
            <div class="detail-advice">{{ detailData.advice || detailData.preventionGuide || '暂无建议' }}</div>
            <div class="detail-risk">
              <span class="dr-label">扩散风险：</span>
              <span>{{ severityText(detailData.severityLevel) }}</span>
            </div>
            <div class="detail-urgency">
              <span class="dr-label">紧急程度：</span>
              <span :style="{ color: severityColor(detailData.severityLevel) }">{{ severityLabel(detailData.severityLevel) }}</span>
            </div>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="加载失败" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { DataAnalysis, Warning, TrendCharts } from '@element-plus/icons-vue'
import { getDetectionList, getDetectionStats, getDetectionDetail } from '@/api/modules/detection'
import { getOrchardList } from '@/api/modules/orchard'
import { DISEASE_MAP } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import { getDetectionWsClient } from '@/utils/websocket'
import DiseaseTag from '@/components/common/DiseaseTag.vue'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()

// WebSocket 监听器注销函数
let unsubWs: (() => void) | null = null

// 实时推送的新检测记录
const realtimeDetections = ref<any[]>([])

function addRealtimeDetection(d: any) {
  realtimeDetections.value.unshift({
    id: d.id || `new_${Date.now()}_${Math.random()}`,
    imageUrl: d.imageUrl,
    topClassCode: d.topClassCode,
    topClassName: d.topClassName,
    topProbability: d.topProbability,
    orchardName: d.orchardName || '-',
    deviceCode: d.deviceCode || '-',
    deviceTimestamp: d.deviceTimestamp,
    createdAt: d.createdAt || dayjs().format(),
    _realtime: true,
  })
  if (realtimeDetections.value.length > 50) {
    realtimeDetections.value = realtimeDetections.value.slice(0, 50)
  }
}

const barChartRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()
let barChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null

const stats = ref<any>({})
const recentList = ref<any[]>([])
const listLoading = ref(false)
const orchardList = ref<any[]>([])
const selectedOrchard = ref<number | ''>('')
const days = ref(30)
const dateRange = ref<string[]>([])

// 识别记录详情弹窗
const detailVisible = ref(false)
const detailData = ref<any>(null)
const detailLoading = ref(false)

async function showDetail(item: any) {
  detailLoading.value = true
  detailVisible.value = true
  detailData.value = null
  try {
    const res: any = await getDetectionDetail(item.id)
    detailData.value = res?.data ?? res
  } catch {
    detailData.value = null
  } finally {
    detailLoading.value = false
  }
}

const avgPerDay = computed(() => {
  const total = stats.value.totalCount ?? 0
  return days.value > 0 ? (total / days.value).toFixed(1) : '0'
})

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function severityText(level: number | undefined | null): string {
  if (!level || level < 1) return '目前病害范围较小，扩散风险较低，但仍需保持观察，防止条件变化后加速扩散。'
  if (level >= 3) return '该病害扩散风险极高，已检测到周边果园存在相同病害，若不及时处理可能导致大面积感染，严重影响产量和果实品质。'
  if (level === 2) return '该病害存在中等扩散风险，当前环境条件适宜病害发展，建议尽快采取防治措施，防止蔓延至更多果树。'
  return '目前病害范围较小，扩散风险较低，但仍需保持观察，防止条件变化后加速扩散。'
}

function severityColor(level: number | undefined | null): string {
  if (!level || level < 1) return '#34D399'
  if (level >= 3) return '#F87171'
  if (level === 2) return '#FBBF24'
  return '#34D399'
}

function severityLabel(level: number | undefined | null): string {
  if (!level || level < 1) return 'L1 低'
  if (level >= 3) return 'L3 高'
  if (level === 2) return 'L2 中'
  return 'L1 低'
}

async function handleOrchardChange() {
  await fetchData()
}

async function handleDateChange() {
  if (dateRange.value?.length === 2) {
    const start = dayjs(dateRange.value[0])
    const end = dayjs(dateRange.value[1])
    days.value = end.diff(start, 'day') + 1
  } else {
    days.value = 30
  }
  await fetchData()
}

async function fetchOrchards() {
  if (userStore.isAdmin) return
  try {
    const data: any = await getOrchardList()
    orchardList.value = Array.isArray(data) ? data : data.records ?? []
  } catch {}
}

async function fetchData() {
  const endDate = dateRange.value?.[1] || dayjs().format('YYYY-MM-DD')
  const startDate = dateRange.value?.[0] || dayjs().subtract(days.value - 1, 'day').format('YYYY-MM-DD')
  
  const params: any = { startDate, endDate }
  if (selectedOrchard.value) params.orchardId = selectedOrchard.value

  try {
    const [statsData, listData]: any[] = await Promise.all([
      getDetectionStats(params),
      getDetectionList({ ...params, page: 1, size: 10 })
    ])
    stats.value = statsData
    const dbList = Array.isArray(listData) ? listData : listData.records ?? []
    // 合并实时推送的数据
    recentList.value = [...realtimeDetections.value, ...dbList]
    
    await nextTick()
    initBarChart()
    initLineChart()
  } catch {}
}

function initBarChart() {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  const distribution: any[] = stats.value.diseaseDistribution ?? []
  
  barChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#E6E0D6',
      borderWidth: 1,
      textStyle: { color: '#2C2C28', fontSize: 13 },
      borderRadius: 8,
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#7A7A70', fontSize: 12 }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: distribution.map(d => ({
          name: DISEASE_MAP[d.diseaseCode]?.name ?? d.diseaseName ?? d.diseaseCode,
          value: d.count,
          itemStyle: { color: DISEASE_MAP[d.diseaseCode]?.color ?? '#5B8C5A' }
        }))
      }
    ]
  })
}

function initLineChart() {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)
  const rawTrend: any[] = stats.value.dailyTrend ?? []
  const trendDates = rawTrend.map(t => t.date)
  const trendValues = rawTrend.map(t => t.count)
  const alertValues = rawTrend.map(t => t.alertCount ?? 0)

  lineChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E6E0D6',
      borderWidth: 1,
      textStyle: { color: '#2C2C28', fontSize: 13 },
      borderRadius: 8,
    },
    legend: {
      data: ['识别次数', '预警次数'],
      top: 0,
      textStyle: { color: '#7A7A70', fontSize: 12 }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: trendDates,
      axisLine: { lineStyle: { color: '#E6E0D6' } },
      axisLabel: { color: '#7A7A70', fontSize: 11, rotate: 30 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F0ECE4', type: 'dashed' } },
      axisLabel: { color: '#7A7A70', fontSize: 12 },
    },
    series: [
      {
        name: '识别次数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: trendValues,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91,140,90,0.3)' },
            { offset: 1, color: 'rgba(91,140,90,0.02)' },
          ]),
        },
        lineStyle: { color: '#5B8C5A', width: 2.5 },
        itemStyle: { color: '#5B8C5A', borderColor: '#fff', borderWidth: 2 },
      },
      {
        name: '预警次数',
        type: 'line',
        smooth: true,
        symbol: 'diamond',
        symbolSize: 6,
        data: alertValues,
        lineStyle: { color: '#E8863A', width: 2, type: 'dashed' },
        itemStyle: { color: '#E8863A', borderColor: '#fff', borderWidth: 2 },
      }
    ],
  })
}

function handleResize() {
  barChart?.resize()
  lineChart?.resize()
}

onMounted(async () => {
  // 从路由参数获取果园ID
  const orchardId = route.query.orchardId
  if (orchardId) {
    selectedOrchard.value = Number(orchardId)
  }
  
  await fetchOrchards()
  await fetchData()
  window.addEventListener('resize', handleResize)

  // 连接 WebSocket 接收实时推送
  const wsClient = getDetectionWsClient()
  unsubWs = wsClient.onMessage((type, data) => {
    if (type === 'detection_new') {
      addRealtimeDetection(data)
      // 合并到列表显示
      recentList.value = [...realtimeDetections.value, ...recentList.value.slice(0, 9)]
    }
  })
  wsClient.connect()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  lineChart?.dispose()
  if (unsubWs) {
    unsubWs()
    unsubWs = null
  }
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

// 统计概览
.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: $card;
  border-radius: 12px;
  box-shadow: $shadow-sm;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    line-height: 1;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 13px;
    color: $text-secondary;
  }
}

// 图表区域
.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  height: 380px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
}

.card-subtitle {
  font-size: 12px;
  color: $text-placeholder;
}

.chart-container {
  width: 100%;
  height: 300px;
}

// 最近记录 - 卡片网格
.recent-card {
  .detection-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }

  .detection-card {
    position: relative;
    border-radius: 10px;
    overflow: hidden;
    aspect-ratio: 3 / 4;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
  }

  .detection-image {
    width: 100%;
    height: 100%;

    :deep(.el-image__inner) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .detection-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.75));
    color: #fff;
  }

  .detection-name {
    margin-bottom: 6px;
  }

  .detection-detail {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
  }

  .detection-conf {
    font-size: 14px;
    font-weight: 600;
    opacity: 0.95;
  }

  .detection-orchard {
    font-size: 12px;
    opacity: 0.8;
  }

  .detection-time {
    font-size: 11px;
    opacity: 0.6;
  }
}

// 详情弹窗
.detail-body {
  min-height: 200px;
}
.detail-section {
  margin-bottom: 20px;
  &:last-child { margin-bottom: 0; }
}
.detail-section-title {
  margin-bottom: 12px;
  .title-badge {
    display: inline-block;
    font-size: 14px;
    font-weight: 600;
    color: #3D8B40;
    background: rgba(61, 139, 64, 0.08);
    padding: 4px 12px;
    border-radius: 6px;
  }
}
.detail-image-wrap {
  text-align: center;
  margin-bottom: 12px;
}
.detail-image {
  width: 100%;
  max-height: 240px;
  border-radius: 8px;
}
.detail-table {
  width: 100%;
  font-size: 13px;
  border-collapse: collapse;
  .dt-label {
    width: 90px;
    color: #8C8C8C;
    padding: 6px 0;
    vertical-align: top;
  }
  .dt-value {
    color: #1A1A1A;
    padding: 6px 0;
    font-weight: 500;
  }
}
.detail-advice {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #FAFBFC;
  border-radius: 8px;
}
.detail-risk, .detail-urgency {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
  margin-bottom: 6px;
  .dr-label {
    color: #8C8C8C;
  }
}
</style>
