<template>
  <div class="page-container">
    <div class="page-header">
      <el-button @click="$router.push('/detection')">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>识别统计</h2>
    </div>

    <el-row :gutter="20" class="summary-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">总识别次数</div>
          <div class="stat-value">{{ stats.totalCount ?? 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="card-header">病害分布</span>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
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
import { ArrowLeft } from '@element-plus/icons-vue'
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
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E6E0D6',
      borderWidth: 1,
      textStyle: { color: '#2C2C28', fontSize: 13 },
      borderRadius: 8,
    },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: distribution.map(d => DISEASE_MAP[d.diseaseCode]?.name ?? d.diseaseName ?? d.diseaseCode),
      axisLabel: { rotate: 25, color: '#7A7A70', fontSize: 11 },
      axisLine: { lineStyle: { color: '#E6E0D6' } },
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
        type: 'bar',
        data: distribution.map(d => ({
          value: d.count,
          itemStyle: {
            color: DISEASE_MAP[d.diseaseCode]?.color ?? '#5B8C5A',
            borderRadius: [4, 4, 0, 0],
          },
        })),
        barMaxWidth: 40,
        barGap: '30%',
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
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E6E0D6',
      borderWidth: 1,
      textStyle: { color: '#2C2C28', fontSize: 13 },
      borderRadius: 8,
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: trendDates,
      axisLine: { lineStyle: { color: '#E6E0D6' } },
      axisLabel: { color: '#7A7A70', fontSize: 12 },
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
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        data: trendValues,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91,140,90,0.25)' },
            { offset: 1, color: 'rgba(91,140,90,0.02)' },
          ]),
        },
        lineStyle: { color: '#5B8C5A', width: 2.5 },
        itemStyle: { color: '#5B8C5A', borderColor: '#fff', borderWidth: 2 },
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
@use '@/assets/styles/variables.scss' as *;

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
    color: $text-secondary;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
  }
}

.chart-row {
  margin-top: 20px;

  .el-card {
    height: 400px;
  }
}

.card-header {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
