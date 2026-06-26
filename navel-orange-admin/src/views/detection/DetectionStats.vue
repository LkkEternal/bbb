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
        <div v-for="item in recentList" :key="item.id" class="detection-card">
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { DataAnalysis, Warning, TrendCharts } from '@element-plus/icons-vue'
import { getDetectionList, getDetectionStats } from '@/api/modules/detection'
import { getOrchardList } from '@/api/modules/orchard'
import { DISEASE_MAP } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import DiseaseTag from '@/components/common/DiseaseTag.vue'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()

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

const avgPerDay = computed(() => {
  const total = stats.value.totalCount ?? 0
  return days.value > 0 ? (total / days.value).toFixed(1) : '0'
})

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
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
    recentList.value = Array.isArray(listData) ? listData : listData.records ?? []
    
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
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  lineChart?.dispose()
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
</style>
