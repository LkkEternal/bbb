<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div
        v-for="(stat, index) in statCards"
        :key="stat.label"
        class="stat-card"
        :style="{ animationDelay: `${index * 0.08}s` }"
      >
        <div class="stat-accent" :style="{ backgroundColor: stat.accentColor }"></div>
        <div class="stat-body">
          <div class="stat-info">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-extra" v-if="stat.extra">{{ stat.extra }}</div>
          </div>
          <div class="stat-icon-wrap" :style="{ backgroundColor: stat.iconBg }">
            <el-icon :size="22" :color="stat.iconColor">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card trend-card">
        <div class="card-head">
          <span class="card-title">病害识别趋势</span>
          <span class="card-tag">近7天</span>
        </div>
        <div ref="trendChartRef" class="chart-area"></div>
      </div>
      <div class="chart-card pie-card">
        <div class="card-head">
          <span class="card-title">病害类型分布</span>
        </div>
        <div ref="pieChartRef" class="chart-area"></div>
      </div>
    </div>

    <div class="bottom-row">
      <div class="list-card">
        <div class="card-head">
          <span class="card-title">最新预警</span>
          <span class="card-link" @click="$router.push('/alert/messages')">查看全部</span>
        </div>
        <div class="alert-list">
          <div
            v-for="alert in dashboardData.recentAlerts"
            :key="alert.id"
            class="alert-item"
            :class="{ unread: !alert.read }"
          >
            <div class="alert-level-bar" :style="{ backgroundColor: getAlertColor(alert.level) }"></div>
            <div class="alert-content">
              <div class="alert-title">{{ alert.title }}</div>
              <div class="alert-meta">{{ alert.orchardName }} · {{ formatTime(alert.createdAt) }}</div>
            </div>
            <el-tag
              :type="alert.level === 3 ? 'danger' : alert.level === 2 ? 'warning' : 'info'"
              size="small"
              effect="plain"
              round
            >
              L{{ alert.level }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="list-card">
        <div class="card-head">
          <span class="card-title">最新识别</span>
          <span class="card-link" @click="$router.push('/detection')">查看全部</span>
        </div>
        <div class="detection-list">
          <div
            v-for="item in dashboardData.recentDetections"
            :key="item.id"
            class="detection-item"
          >
            <el-image :src="item.imageUrl" fit="cover" class="detection-img" :preview-src-list="[item.imageUrl]" />
            <div class="detection-info">
              <div class="disease-row">
                <DiseaseTag :code="item.diseaseCode" />
                <span class="confidence">{{ formatPercent(item.confidence) }}</span>
              </div>
              <div class="detection-meta">{{ item.orchardName }} · {{ formatTime(item.time) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/modules/dashboard'
import { formatPercent } from '@/utils/format'
import DiseaseTag from '@/components/common/DiseaseTag.vue'

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const dashboardData = reactive<any>({
  statistics: {},
  currentWeather: {},
  riskLevel: {},
  recentAlerts: [],
  recentDetections: [],
  diseaseDistribution: [],
  detectionTrend: { dates: [], values: [] },
})

const statCards = ref([
  { label: '果园总数', value: 0, icon: 'Location', accentColor: '#5B8C5A', iconBg: 'rgba(91,140,90,0.08)', iconColor: '#5B8C5A', extra: '' },
  { label: '设备在线', value: 0, icon: 'Monitor', accentColor: '#E8863A', iconBg: 'rgba(232,134,58,0.08)', iconColor: '#E8863A', extra: '' },
  { label: '今日识别', value: 0, icon: 'Search', accentColor: '#D4A853', iconBg: 'rgba(212,168,83,0.08)', iconColor: '#D4A853', extra: '' },
  { label: '活跃预警', value: 0, icon: 'Bell', accentColor: '#D64545', iconBg: 'rgba(214,69,69,0.08)', iconColor: '#D64545', extra: '' },
])

function getAlertColor(level: number) {
  if (level === 3) return '#D64545'
  if (level === 2) return '#E8863A'
  return '#5B8C5A'
}

function formatTime(t: string) {
  if (!t) return ''
  return t.replace(/^\d{4}-/, '').replace(' ', ' ').slice(0, 11)
}

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: { color: '#1A1C1E', fontSize: 13 },
      borderRadius: 8,
      shadowBlur: 12,
      shadowColor: 'rgba(0,0,0,0.06)',
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dashboardData.detectionTrend.dates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#9AA0A6', fontSize: 12 },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F0F1F3', type: 'dashed' } },
      axisLabel: { color: '#9AA0A6', fontSize: 12 },
    },
    series: [{
      name: '识别次数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: dashboardData.detectionTrend.values,
      lineStyle: { color: '#5B8C5A', width: 2.5 },
      itemStyle: { color: '#5B8C5A', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(91,140,90,0.18)' },
          { offset: 1, color: 'rgba(91,140,90,0.02)' },
        ]),
      },
    }],
  })
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const colors = ['#5B8C5A', '#E8863A', '#D4A853', '#D64545', '#9AA0A6', '#7FA87E']
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#E5E7EB',
      textStyle: { color: '#1A1C1E' },
      borderRadius: 8,
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#5F6368', fontSize: 12 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 14,
    },
    series: [{
      type: 'pie',
      radius: ['42%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: '600', color: '#1A1C1E' },
        scaleSize: 4,
      },
      data: dashboardData.diseaseDistribution.map((item: any, i: number) => ({
        ...item,
        itemStyle: { color: colors[i % colors.length] },
      })),
    }],
  })
}

function handleResize() {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  try {
    const data: any = await getDashboard()
    Object.assign(dashboardData, data)
    statCards.value[0].value = data.statistics.orchardCount
    statCards.value[1].value = data.statistics.deviceOnline
    statCards.value[1].extra = `/ ${data.statistics.deviceTotal} 总数`
    statCards.value[2].value = data.statistics.todayDetections
    statCards.value[3].value = data.statistics.activeAlerts
    await nextTick()
    initTrendChart()
    initPieChart()
    window.addEventListener('resize', handleResize)
  } catch {}
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// Stat cards
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  overflow: hidden;
  animation: fadeInUp 0.4s ease forwards;
  opacity: 0;
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-accent {
  height: 3px;
}

.stat-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 18px;
}

.stat-label {
  font-size: 13px;
  color: #5F6368;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1A1C1E;
  line-height: 1;
}

.stat-extra {
  font-size: 12px;
  color: #9AA0A6;
  margin-top: 4px;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

// Chart row
.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  padding: 20px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1A1C1E;
}

.card-tag {
  font-size: 11px;
  color: #9AA0A6;
  background: #F2F7F2;
  padding: 2px 8px;
  border-radius: 10px;
}

.card-link {
  font-size: 13px;
  color: #5B8C5A;
  cursor: pointer;
  transition: color 0.2s;
  &:hover {
    color: #4A7A49;
  }
}

.chart-area {
  width: 100%;
  height: 260px;
}

// Bottom row
.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.list-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #E5E7EB;
  padding: 20px;
  max-height: 340px;
  display: flex;
  flex-direction: column;
}

.alert-list, .detection-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  flex: 1;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #FAFAFA;
  transition: all 0.2s;

  &:hover {
    background: #F2F7F2;
  }

  &.unread {
    background: #FFF8F0;
  }
}

.alert-level-bar {
  width: 3px;
  height: 28px;
  border-radius: 2px;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-title {
  font-size: 13px;
  font-weight: 500;
  color: #1A1C1E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.alert-meta {
  font-size: 12px;
  color: #9AA0A6;
  margin-top: 2px;
}

.detection-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #F8F9FA;
  }
}

.detection-img {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  flex-shrink: 0;
}

.detection-info {
  flex: 1;
  min-width: 0;
}

.disease-row {
  display: flex;
  align-items: center;
  gap: 8px;

  .confidence {
    font-size: 12px;
    color: #9AA0A6;
  }
}

.detection-meta {
  font-size: 12px;
  color: #9AA0A6;
  margin-top: 2px;
}
</style>
