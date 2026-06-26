<template>
  <div class="dashboard">
    <!-- 左：地图 + 热力覆盖 -->
    <div class="map-section">
      <div class="map-header">
        <span class="map-title">果园风险分布</span>
        <el-select v-model="selectedOrchard" size="small" style="width:150px">
          <el-option v-for="o in orchardOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <div class="map-legend">
          <span class="legend-item"><span class="legend-bar" style="background:#3D8B40"></span>低风险</span>
          <span class="legend-item"><span class="legend-bar" style="background:#FF8C42"></span>中风险</span>
          <span class="legend-item"><span class="legend-bar" style="background:#E53935"></span>高风险</span>
        </div>
      </div>
      <div class="map-body">
        <div ref="mapRef" class="map-base"></div>
        <canvas ref="heatmapRef" class="heatmap-canvas"></canvas>
        <div v-for="zone in currentZones" :key="zone.label"
          class="zone-label" :style="{ left: zone.x + '%', top: zone.y + '%' }">
          {{ zone.label }}
        </div>
      </div>
    </div>

    <!-- 右：数据面板 -->
    <div class="data-panel">
      <!-- 天气卡片 -->
      <div class="dp-card weather-card">
        <div class="wc-row">
          <div class="wc-icon"><el-icon :size="28" color="#fff"><Sunny /></el-icon></div>
          <div class="wc-info">
            <div class="wc-temp">{{ weather.temperature ?? '--' }}<span class="wc-deg">&deg;C</span></div>
            <div class="wc-meta">湿度 {{ weather.humidity ?? '--' }}%</div>
          </div>
        </div>
        <div class="wc-footer">
          <span class="wc-status">{{ weatherStatus }}</span>
          <span class="wc-time">{{ weather.updateTime ? '更新于 ' + formatTime(weather.updateTime).slice(11, 16) : '' }}</span>
        </div>
      </div>

      <!-- 快速指标 -->
      <div class="dp-card">
        <div class="dp-title">果园概况</div>
        <div class="metrics-grid">
          <div class="metric-item" v-for="s in miniStats" :key="s.label">
            <div class="metric-value" :style="{ color: s.color }">{{ s.value }}</div>
            <div class="metric-label">{{ s.label }}</div>
          </div>
        </div>
      </div>

      <!-- 病害趋势 -->
      <div class="dp-card">
        <div class="dp-title-row">
          <span class="dp-title">病害分布</span>
          <span class="dp-tag">今日</span>
        </div>
        <div ref="trendChartRef" class="mini-chart"></div>
      </div>

      <!-- 最新预警 -->
      <div class="dp-card">
        <div class="dp-title-row">
          <span class="dp-title">最新预警</span>
          <span class="dp-link" @click="$router.push('/message')">查看全部 &rarr;</span>
        </div>
        <div class="alert-list">
          <div v-for="a in alerts" :key="a.id" class="alert-row" :class="{ unread: !a.read }">
            <span class="alert-dot" :style="{ background: alertColor(a.level) }"></span>
            <span class="alert-text">{{ a.title }}</span>
          </div>
          <div v-if="alerts.length === 0" class="alert-empty">暂无预警</div>
        </div>
      </div>

      <!-- 待处理文献 -->
      <div class="dp-card">
        <div class="dp-title-row">
          <span class="dp-title">待处理文献</span>
          <el-badge v-if="pendingDocCount > 0" :value="pendingDocCount" :max="99" class="doc-badge" />
          <span class="dp-link" @click="$router.push('/knowledge/docs')">查看全部 &rarr;</span>
        </div>
        <div class="doc-list" v-loading="docLoading">
          <div v-for="doc in pendingDocs" :key="doc.id" class="doc-row">
            <div class="doc-icon" :style="{ background: docStatusInfo(doc.status).color + '18', color: docStatusInfo(doc.status).color }">
              {{ (doc.fileType || '?').toUpperCase().charAt(0) }}
            </div>
            <div class="doc-info">
              <div class="doc-title">{{ doc.title }}</div>
              <div class="doc-meta">
                <span>{{ formatFileSize(doc.fileSize) }}</span>
                <el-tag :color="docStatusInfo(doc.status).color" effect="dark" size="small" style="border:none">
                  {{ docStatusInfo(doc.status).label }}
                </el-tag>
              </div>
            </div>
            <div class="doc-actions">
              <el-button v-if="doc.status === 0 || doc.status === 3" link type="primary" size="small" @click="handleRetryDoc(doc)">
                {{ doc.status === 3 ? '重试' : '处理' }}
              </el-button>
              <el-button link type="danger" size="small" @click="handleDeleteDoc(doc)">删除</el-button>
            </div>
          </div>
          <div v-if="!docLoading && pendingDocs.length === 0" class="doc-empty">暂无待处理文献</div>
        </div>
      </div>

      <!-- 农事日历 -->
      <div class="dp-card calendar-card">
        <div class="dp-title-row">
          <span class="dp-title">📅 农事日历</span>
        </div>
        <div v-if="calendarData?.current" class="calendar-body">
          <div class="calendar-phenology">{{ calendarData.current.phenologyName }}</div>
          <div v-if="calendarData.current.solarTerm" class="calendar-term">{{ calendarData.current.solarTerm }}</div>
          <div class="calendar-advice">{{ calendarData.current.adviceContent || '暂无建议' }}</div>
          <div v-if="calendarData.current.diseaseRelated?.length" class="calendar-diseases">
            <span class="calendar-label">关联病害：</span>
            <el-tag v-for="d in calendarData.current.diseaseRelated" :key="d.diseaseCode" size="small" effect="plain">{{ d.diseaseName }}</el-tag>
          </div>
        </div>
        <div v-else class="calendar-empty">暂无当前物候期数据</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { Sunny } from '@element-plus/icons-vue'
import { getCurrentReminder } from '@/api/modules/calendar'
import { getDashboard } from '@/api/modules/dashboard'
import { getPendingDocuments, getFailedDocuments, processDocument, deleteDocument } from '@/api/modules/knowledge'
import { DISEASE_MAP } from '@/constants/disease'
import { KNOWLEDGE_STATUS_MAP } from '@/constants/enums'
import { formatFileSize } from '@/utils/format'
import dayjs from 'dayjs'

const mapRef = ref<HTMLElement>()
const heatmapRef = ref<HTMLCanvasElement>()
const trendChartRef = ref<HTMLElement>()
let mapInstance: L.Map | null = null
let trendChart: echarts.ECharts | null = null

const selectedOrchard = ref('A')

const orchardOptions = [
  { value: 'A', label: '脐橙果园 A' },
  { value: 'B', label: '脐橙果园 B' },
  { value: 'C', label: '脐橙果园 C' },
]

// 各果园中心坐标
const centers: Record<string, [number, number]> = {
  A: [25.830, 114.930],
  B: [25.810, 114.950],
  C: [25.850, 114.910],
}

// 风险区域（坐标百分比 + 风险等级）
interface Zone { label: string; x: number; y: number; r: number; risk: number }
const orchardData: Record<string, Zone[]> = {
  A: [
    { label: '东区A', x: 72, y: 28, r: 14, risk: 2 },
    { label: '东区B', x: 84, y: 35, r: 11, risk: 1 },
    { label: '西区A', x: 22, y: 32, r: 16, risk: 1 },
    { label: '西区B', x: 14, y: 45, r: 12, risk: 3 },
    { label: '中心', x: 48, y: 42, r: 10, risk: 1 },
    { label: '北坡', x: 52, y: 14, r: 11, risk: 2 },
    { label: '南坡', x: 44, y: 72, r: 13, risk: 1 },
  ],
  B: [
    { label: '东区', x: 75, y: 35, r: 16, risk: 1 },
    { label: '西区', x: 25, y: 38, r: 14, risk: 2 },
    { label: '中心', x: 50, y: 45, r: 12, risk: 1 },
  ],
  C: [
    { label: '东区', x: 76, y: 32, r: 14, risk: 3 },
    { label: '西区', x: 24, y: 36, r: 12, risk: 1 },
    { label: '中心', x: 50, y: 42, r: 10, risk: 2 },
  ],
}

const currentZones = computed(() => orchardData[selectedOrchard.value] || [])
const riskColors = ['#3D8B40', '#FF8C42', '#E53935']

// 高德瓦片
// 高德普通地图瓦片 URL
const AMAP_ROAD = 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}'

function initMap() {
  if (!mapRef.value) return
  mapInstance = L.map(mapRef.value, {
    center: centers.A,
    zoom: 17,
    zoomControl: false,
    attributionControl: false,
    dragging: false,
    scrollWheelZoom: false,
    doubleClickZoom: false,
    touchZoom: false,
    keyboard: false,
  })
  // 高德普通地图
  L.tileLayer(AMAP_ROAD, { maxZoom: 19 }).addTo(mapInstance)
}

function drawHeatmap() {
  const canvas = heatmapRef.value
  if (!canvas) return

  const container = canvas.parentElement!
  const rect = container.getBoundingClientRect()
  const dpr = window.devicePixelRatio || 1
  canvas.width = rect.width * dpr
  canvas.height = rect.height * dpr
  canvas.style.width = rect.width + 'px'
  canvas.style.height = rect.height + 'px'

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const w = canvas.width
  const h = canvas.height
  ctx.clearRect(0, 0, w, h)

  const zones = currentZones.value

  // 用"叠加混合"让颜色融合更饱满
  ctx.globalCompositeOperation = 'source-over'

  zones.forEach(z => {
    const cx = (z.x / 100) * w
    const cy = (z.y / 100) * h
    const radius = (z.r / 100) * Math.max(w, h) * 1.3
    const color = riskColors[z.risk - 1]

    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, radius)
    // 实心到渐变的过渡 — 核心区域实色，边缘渐变融到透明
    grad.addColorStop(0, color + 'CC')      // 80% 实心
    grad.addColorStop(0.3, color + '99')    // 60%
    grad.addColorStop(0.6, color + '55')    // 33%
    grad.addColorStop(1, color + '00')      // 透明

    ctx.beginPath()
    ctx.fillStyle = grad
    ctx.arc(cx, cy, radius, 0, Math.PI * 2)
    ctx.fill()
  })

  // 区域边线 — 让分区更清晰
  zones.forEach(z => {
    const cx = (z.x / 100) * w
    const cy = (z.y / 100) * h
    const radius = (z.r / 100) * Math.max(w, h) * 0.65
    const color = riskColors[z.risk - 1]
    ctx.beginPath()
    ctx.strokeStyle = color + '60'
    ctx.lineWidth = 2
    ctx.setLineDash([6, 4])
    ctx.arc(cx, cy, radius, 0, Math.PI * 2)
    ctx.stroke()
    ctx.setLineDash([])
  })
}

const miniStats = ref([
  { label: '果园数量', value: '-', color: '#3D8B40' },
  { label: '今日识别', value: '-', color: '#FF8C42' },
  { label: '设备在线', value: '-', color: '#D4A853' },
  { label: '设备总数', value: '-', color: '#3D8B40' },
])

const weather = ref<any>({})
const weatherStatus = computed(() => {
  const t = weather.value.temperature
  if (t == null) return ''
  if (t < 5) return '低温，注意防冻'
  if (t > 35) return '高温，注意防晒'
  return '适宜农事作业'
})

const alerts = ref<any[]>([])

function alertColor(lv: number) { return lv >= 3 ? '#E53935' : lv >= 2 ? '#FF8C42' : '#3D8B40' }

// ── 待处理文献 ──
interface PendingDoc {
  id: number
  title: string
  fileType: string
  fileSize: number
  status: number
  createdAt: string
}

const pendingDocs = ref<PendingDoc[]>([])
const pendingDocCount = ref(0)
const docLoading = ref(false)

async function fetchPendingDocs() {
  docLoading.value = true
  try {
    const [pending, failed] = await Promise.all([
      getPendingDocuments(3),
      getFailedDocuments(3),
    ])
    const pendingList: PendingDoc[] = pending?.records ?? pending ?? []
    const failedList: PendingDoc[] = failed?.records ?? failed ?? []
    pendingDocs.value = [...failedList, ...pendingList].slice(0, 5)
    pendingDocCount.value = (pending?.total ?? 0) + (failed?.total ?? 0)
  } catch {
  } finally {
    docLoading.value = false
  }
}

async function handleRetryDoc(doc: PendingDoc) {
  try {
    await processDocument(doc.id)
    fetchPendingDocs()
  } catch {}
}

async function handleDeleteDoc(doc: PendingDoc) {
  try {
    await deleteDocument(doc.id)
    fetchPendingDocs()
  } catch {}
}

function docStatusInfo(status: number) {
  return KNOWLEDGE_STATUS_MAP[status] ?? { label: '未知', color: '#909399' }
}

function formatTime(time: string | undefined | null): string {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const calendarData = ref<any>(null)

async function fetchCalendar() {
  try {
    const data: any = await getCurrentReminder()
    calendarData.value = data
  } catch {}
}

async function fetchDashboard() {
  try {
    const data: any = await getDashboard()
    if (!data) return

    // 天气
    if (data.currentWeather) {
      weather.value = data.currentWeather
    }

    // 统计
    if (data.statistics) {
      const s = data.statistics
      miniStats.value = [
        { label: '果园数量', value: String(s.orchardCount ?? 0), color: '#3D8B40' },
        { label: '今日识别', value: String(s.todayDetections ?? 0) + ' 次', color: '#FF8C42' },
        { label: '设备在线', value: String(s.deviceOnline ?? 0), color: '#D4A853' },
        { label: '设备总数', value: String(s.deviceTotal ?? 0), color: '#3D8B40' },
      ]
    }

    // 最新预警
    if (data.recentAlerts) {
      alerts.value = data.recentAlerts
    }

    // 病害分布 → 趋势图
    if (data.diseaseDistribution) {
      await nextTick()
      initTrendFromData(data.diseaseDistribution)
    }
  } catch {}
}

function initTrend() {
  // 默认初始化空图表，数据由 fetchDashboard 填充
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#E6E0D6',
      borderWidth: 1,
      textStyle: { color: '#2C2C28', fontSize: 12 },
      borderRadius: 8,
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['35%', '70%'],
      center: ['50%', '50%'],
      label: { show: false },
      data: []
    }]
  })
}

function initTrendFromData(distribution: any[]) {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#E6E0D6',
      borderWidth: 1,
      textStyle: { color: '#2C2C28', fontSize: 12 },
      borderRadius: 8,
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: ['35%', '70%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      data: distribution.map(d => ({
        name: DISEASE_MAP[d.name]?.name ?? d.name,
        value: d.value,
        itemStyle: { color: DISEASE_MAP[d.name]?.color ?? '#5B8C5A' }
      }))
    }]
  })
}

function handleResize() {
  trendChart?.resize()
  mapInstance?.invalidateSize()
  drawHeatmap()
}

watch(selectedOrchard, (val) => {
  const c = centers[val]
  if (mapInstance && c) {
    mapInstance.setView(c, 17)
    nextTick(() => drawHeatmap())
  }
})

onMounted(async () => {
  await nextTick()
  initMap()
  initTrend()
  drawHeatmap()
  fetchCalendar()
  fetchDashboard()
  fetchPendingDocs()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  mapInstance?.remove()
  trendChart?.dispose()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.dashboard {
  display: flex;
  height: calc(100vh - 64px - 2px); overflow: hidden;
}

// ═══ 左：地图 ═══
.map-section {
  flex: 1; display: flex; flex-direction: column;
  min-width: 0;
}

.map-header {
  display: flex; align-items: center; gap: 14px;
  padding: 8px 20px; background: $card; flex-shrink: 0;
  border-bottom: 1px solid $divider;
}

.map-title { font-size: 14px; font-weight: 600; color: $text-primary; }

.map-legend { display: flex; gap: 12px; margin-left: auto; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 11px; color: $text-secondary; }
.legend-bar { width: 16px; height: 4px; border-radius: 2px; }

.map-body {
  flex: 1; position: relative; overflow: hidden;
  background: #1A2E1A;
}

.map-base {
  position: absolute; inset: 0;
  z-index: 1;
}

.heatmap-canvas {
  position: absolute; inset: 0;
  z-index: 2;
  pointer-events: none;
  width: 100%; height: 100%;
}

.zone-label {
  position: absolute; z-index: 3;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 8px;
  pointer-events: none;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

// ═══ 右：数据面板 ═══
.data-panel {
  width: 320px; min-width: 320px;
  border-left: 1px solid $divider;
  background: $canvas;
  overflow-y: auto;
  padding: 12px;
  display: flex; flex-direction: column; gap: 10px;
}

// 统一卡片
.dp-card {
  background: $card;
  border-radius: 10px;
  box-shadow: $shadow-sm;
  padding: 14px 16px;
}

.dp-title {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
}

.dp-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.dp-tag {
  font-size: 10px;
  color: $text-placeholder;
  background: $primary-subtle;
  padding: 1px 8px;
  border-radius: 8px;
}

.dp-link {
  font-size: 11px;
  color: $primary;
  cursor: pointer;
  &:hover { color: $primary-hover; }
}

// 天气
.weather-card {
  background: linear-gradient(135deg, #1A3A1A, #2E4828);
  color: #fff;
  padding: 14px 16px;
}

.wc-row { display: flex; align-items: center; gap: 14px; }
.wc-icon {
  width: 44px; height: 44px;
  background: rgba(255,255,255,0.08);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}
.wc-temp { font-size: 26px; font-weight: 300; .wc-deg { font-size: 13px; opacity: 0.6; } }
.wc-meta { font-size: 11px; opacity: 0.6; margin-top: 2px; }
.wc-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 10px; padding-top: 10px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.wc-status { font-size: 11px; opacity: 0.7; }
.wc-time { font-size: 10px; opacity: 0.4; }

// 风险
.risk-card { }
.rc-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.rc-badge { padding: 2px 10px; border-radius: 10px; color: #fff; font-size: 11px; font-weight: 600; }
.rc-body { display: flex; align-items: center; gap: 14px; }
.gauge-box { width: 80px; height: 60px; flex-shrink: 0; }
.rc-stats { flex: 1; }
.rc-num { font-size: 28px; font-weight: 700; }
.rc-unit { font-size: 12px; color: $text-secondary; }
.rc-items { margin-top: 6px; display: flex; flex-direction: column; gap: 4px; }
.rc-item { display: flex; align-items: center; gap: 6px; font-size: 11px; color: $text-secondary; }
.rc-dot { width: 5px; height: 5px; border-radius: 50%; flex-shrink: 0; }

// 指标
.metrics-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 10px; }
.metric-item {
  background: #FAFAFA; border-radius: 8px;
  padding: 10px; display: flex; flex-direction: column;
}
.metric-value { font-size: 20px; font-weight: 700; line-height: 1; margin-bottom: 2px; }
.metric-label { font-size: 11px; color: $text-secondary; }

// 趋势
.mini-chart { width: 100%; height: 50px; }

// 预警
.alert-list { display: flex; flex-direction: column; gap: 4px; margin-top: 2px; }
.alert-row {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 8px; border-radius: 6px; cursor: pointer;
  transition: background 0.15s;
  &.unread { background: $accent-light; }
  &:hover { background: $primary-subtle; }
}
.alert-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.alert-text { font-size: 11px; color: $text-primary; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.alert-empty { font-size: 11px; color: $text-placeholder; text-align: center; padding: 8px 0; }

// 待处理文献
.doc-badge {
  :deep(.el-badge__content) { font-size: 10px; }
}
.doc-list { display: flex; flex-direction: column; gap: 6px; margin-top: 2px; min-height: 40px; }
.doc-row {
  display: flex; align-items: center; gap: 8px;
  padding: 8px; border-radius: 8px;
  transition: background 0.15s;
  &:hover { background: $primary-subtle; }
}
.doc-icon {
  width: 32px; height: 32px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0;
}
.doc-info { flex: 1; min-width: 0; }
.doc-title {
  font-size: 12px; font-weight: 500; color: $text-primary;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.doc-meta {
  display: flex; align-items: center; gap: 6px; margin-top: 3px;
  font-size: 10px; color: $text-placeholder;
  .el-tag { height: 16px; line-height: 16px; font-size: 9px; padding: 0 4px; }
}
.doc-actions { display: flex; gap: 2px; flex-shrink: 0; }
.doc-empty { font-size: 11px; color: $text-placeholder; text-align: center; padding: 12px 0; }

// 识别
.detect-list { display: flex; flex-direction: column; gap: 4px; margin-top: 2px; }
.detect-row {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 8px; border-radius: 6px;
  transition: background 0.15s;
  &:hover { background: $primary-subtle; }
}
.detect-tag {
  width: 26px; height: 26px; border-radius: 5px;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; font-weight: 700; flex-shrink: 0;
}
.detect-info { display: flex; align-items: center; gap: 6px; }
.detect-name { font-size: 12px; font-weight: 500; color: $text-primary; }
.detect-conf { font-size: 10px; color: $text-placeholder; }

// 农事日历
.calendar-body {
  .calendar-phenology {
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .calendar-term {
    font-size: 12px;
    color: $primary;
    margin-bottom: 8px;
  }

  .calendar-advice {
    font-size: 12px;
    color: $text-secondary;
    line-height: 1.6;
    margin-bottom: 8px;
  }

  .calendar-diseases {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 4px;
    font-size: 12px;
    color: $text-secondary;
  }

  .calendar-label {
    margin-right: 4px;
  }
}

.calendar-empty {
  font-size: 12px;
  color: $text-placeholder;
  text-align: center;
  padding: 12px 0;
}
</style>
