<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/detection')">
      <template #content>
        <span class="page-title">识别统计</span>
      </template>
    </el-page-header>

    <el-row :gutter="20" class="summary-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总识别次数</div>
          <div class="stat-value">{{ stats.total ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-header">病害分布</span>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-header">每日趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDetectionStats } from '@/api/modules/detection'
import { DISEASE_MAP } from '@/constants/disease'
import dayjs from 'dayjs'

const barChartRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()
let barChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null

const stats = ref<any>({})

function initBarChart() {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  const distribution: any[] = stats.value.diseaseDistribution ?? []
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: distribution.map(d => DISEASE_MAP[d.diseaseCode]?.name ?? d.diseaseName ?? d.diseaseCode),
      axisLabel: { rotate: 30, color: '#606266' },
      axisLine: { lineStyle: { color: '#dcdfe6' } },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#606266' },
    },
    series: [
      {
        type: 'bar',
        data: distribution.map(d => ({
          value: d.count,
          itemStyle: { color: DISEASE_MAP[d.diseaseCode]?.color ?? '#409eff' },
        })),
        barMaxWidth: 40,
      },
    ],
  })
}

function initLineChart() {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)
  const rawTrend: any[] = stats.value.dailyTrend ?? []
  const trendDates = Array.isArray(rawTrend) ? rawTrend.map((t: any) => t.date) : []
  const trendValues = Array.isArray(rawTrend) ? rawTrend.map((t: any) => t.count) : []
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: trendDates,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266' },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#606266' },
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: trendValues,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' },
          ]),
        },
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff' },
      },
    ],
  })
}

function handleResize() {
  barChart?.resize()
  lineChart?.resize()
}

onMounted(async () => {
  try {
    const endDate = dayjs().format('YYYY-MM-DD')
    const startDate = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
    const data: any = await getDetectionStats({ startDate, endDate })
    stats.value = data
    await nextTick()
    initBarChart()
    initLineChart()
    window.addEventListener('resize', handleResize)
  } catch {}
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped lang="scss">
.page-title {
  font-size: 18px;
  font-weight: 600;
}

.summary-row {
  margin-top: 20px;
}

.stat-card {
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
  }
}

.chart-row {
  margin-top: 20px;

  .el-card {
    height: 400px;
  }
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
