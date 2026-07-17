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
          <span class="legend-item"><span class="legend-bar" style="background:#34D399"></span>低风险</span>
          <span class="legend-item"><span class="legend-bar" style="background:#FBBF24"></span>中风险</span>
          <span class="legend-item"><span class="legend-bar" style="background:#F87171"></span>高风险</span>
        </div>
      </div>
      <div class="map-body">
        <div ref="mapRef" class="map-base"></div>
      </div>
    </div>

    <!-- 右：数据面板 -->
    <div class="data-panel">
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

      <!-- 天气卡片 -->
      <div class="dp-card weather-card">
        <div class="wc-row">
          <div class="wc-icon"><el-icon :size="28"><Sunny /></el-icon></div>
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

      <!-- 传感器详细数据 -->
      <div class="dp-card" v-if="sensors && Object.keys(sensors).length > 0">
        <div class="dp-title-row">
          <span class="dp-title">📡 传感器数据</span>
        </div>
        <div class="sensor-grid">
          <div class="sensor-item" v-for="(val, key) in sensorLabels" :key="key">
            <div class="sensor-value" :style="{ color: sensorColor(key, sensors[key]) }">
              {{ sensors[key] ?? '--' }}<span class="sensor-unit">{{ sensorUnit(key) }}</span>
            </div>
            <div class="sensor-label">{{ val }}</div>
          </div>
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
          <div v-for="a in recentAlerts" :key="a.id" class="alert-row" :class="{ unread: !a.read }">
            <span class="alert-bar" :style="{ background: alertColor(a.level) }"></span>
            <span class="alert-text">{{ a.title }}</span>
          </div>
          <div v-if="recentAlerts.length === 0" class="alert-empty">暂无预警</div>
        </div>
      </div>

      <!-- AI 风险预测 -->
      <div class="dp-card">
        <div class="dp-title-row">
          <span class="dp-title">AI 风险预测</span>
          <span class="dp-link" @click="$router.push('/prediction')">查看详情 &rarr;</span>
        </div>
        <div v-if="predictionLoading" class="pred-loading">加载中...</div>
        <div v-else-if="predictionData" class="pred-body">
          <div class="pred-level-row">
            <el-tag :color="predictionData.riskColor" effect="dark" size="large" class="pred-badge">
              {{ predictionData.riskLabel }}
            </el-tag>
            <span class="pred-score">评分: {{ (predictionData.riskScore * 100).toFixed(1) }}</span>
          </div>
          <div class="pred-prob-bar">
            <div
              v-for="(p, i) in predictionData.probabilities"
              :key="i"
              class="pred-prob-seg"
              :style="{ width: (p * 100) + '%', backgroundColor: ['#67c23a','#e6a23c','#f56c6c','#c03636'][i] }"
            />
          </div>
          <div class="pred-time">更新: {{ predictionData.createdAt }}</div>
        </div>
        <div v-else class="pred-empty">暂无预测数据</div>
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


    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { createHeatLayer } from '@/utils/heatmap'
import { Sunny } from '@element-plus/icons-vue'
import { getCurrentReminder } from '@/api/modules/calendar'
import { getDashboard } from '@/api/modules/dashboard'
import { getOrchardList } from '@/api/modules/orchard'
import { getPendingDocuments, getFailedDocuments, processDocument, deleteDocument } from '@/api/modules/knowledge'
import { DISEASE_MAP } from '@/constants/disease'
import { KNOWLEDGE_STATUS_MAP } from '@/constants/enums'
import { formatFileSize } from '@/utils/format'
import dayjs from 'dayjs'
import { getLatestPrediction, parsePrediction, type PredictionDetail } from '@/api/modules/prediction'

const mapRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
let mapInstance: L.Map | null = null
let trendChart: echarts.ECharts | null = null
let markersLayer: L.LayerGroup | null = null
let heatLayer: any = null

// 果园数据接口
interface OrchardMapItem {
  id: number
  name: string
  location: string  // "lng,lat"
  address: string
  deviceCount: number
  riskLevel: number  // 1=低 2=中 3=高
  riskScore: number
}

const orchards = ref<OrchardMapItem[]>([])
const selectedOrchard = ref<number>(0)

const orchardOptions = computed(() => {
  const options: { value: number; label: string }[] = [
    { value: 0, label: '📍 全部果园' }
  ]
  orchards.value.forEach(o => {
    const hasRuleAlert = alerts.value.some((a: any) => a.orchardName === o.name && a.relatedType !== 'detection')
    options.push({ value: o.id, label: o.name + (hasRuleAlert ? ' ⚠' : '') })
  })
  return options
})

const riskColors = ['#34D399', '#FBBF24', '#F87171']
const riskLabels = ['低风险', '中风险', '高风险']

const AMAP_ROAD = 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}'

// 解析 location "lng,lat" → [lat, lng]
function parseLocation(loc: string): [number, number] {
  const parts = loc.split(',')
  return [parseFloat(parts[1]), parseFloat(parts[0])]
}

function initMap() {
  if (!mapRef.value) return
  mapInstance = L.map(mapRef.value, {
    center: [25.830, 114.930],
    zoom: 12,
    maxZoom: 18,
    minZoom: 6,
    attributionControl: false,
  })
  L.tileLayer(AMAP_ROAD, { maxZoom: 18 }).addTo(mapInstance)
}

function updateOrchardMarkers() {
  if (!mapInstance) return
  if (markersLayer) {
    markersLayer.clearLayers()
  } else {
    markersLayer = L.layerGroup().addTo(mapInstance)
  }

  // 根据 selectedOrchard 筛选：0=全部，非0=单个果园
  const orchardList = orchards.value.filter(o =>
    o.location && o.location.includes(',') &&
    (selectedOrchard.value === 0 || o.id === selectedOrchard.value)
  )
  if (orchardList.length === 0) {
    mapInstance.setView([25.830, 114.930], 12)
    return
  }

  orchardList.forEach(o => {
    const [lat, lng] = parseLocation(o.location)
    // 查找最新一条预警（alerts 已按 createdAt DESC 排序），取等级确定颜色
    const latestAlert = alerts.value.find((a: any) => a.orchardName === o.name && a.relatedType !== 'detection')
    const level = latestAlert ? (latestAlert.level || 1) : 0
    const color = level === 0 ? riskColors[0] : riskColors[Math.min(level, 3) - 1]

    // 波纹涟漪扩散标记
    const icon = L.divIcon({
      className: '',
      html: `<div class="ripple-marker" style="--color: ${color}">
        <div class="ripple-ring"></div>
        <div class="ripple-dot"></div>
      </div>`,
      iconSize: [40, 40],
      iconAnchor: [20, 20],
    })
    const marker = L.marker([lat, lng], { icon }).addTo(markersLayer!)

    // 点击弹窗 — 查找触发规则预警并展示风险摘要
    marker.on('click', async () => {
      marker.bindPopup(`<div style="min-width:160px;text-align:center;padding:8px">加载中...</div>`).openPopup()
      try {
        const matchedAlert = alerts.value.find((a: any) => a.orchardName === o.name && a.relatedType !== 'detection')
        if (matchedAlert) {
          // ── 有触发规则预警 → 风险摘要 ──
          // 数据来源：phenology ← calendarData (农事日历API); temp/humidity ← weather (dashboard API currentWeather)
          const phenology = calendarData.value?.current?.phenologyName || '—'
          const temp = weather.value.temperature != null ? weather.value.temperature + '℃' : '—'
          const hum = weather.value.humidity != null ? weather.value.humidity + '%' : '—'
          const lv = matchedAlert.level || 1
          const advice = matchedAlert.advice || '暂无建议'
          const ll = ['', 'L1 低', 'L2 中', 'L3 高']
          const lc = ['', '#34D399', '#FBBF24', '#F87171']
          marker.bindPopup(`<div style="min-width:200px"><div style="font-size:15px;font-weight:600;color:#1A1A1A">${o.name}</div>
            <div style="font-size:12px;color:#888;margin-bottom:8px">${o.address || ''}</div>
            <div style="font-size:13px;font-weight:600;margin-bottom:6px">📊 风险摘要</div>
            <table style="font-size:12px;color:#595959;width:100%"><tr><td style="color:#888;width:62px;padding:2px 0">触发规则</td><td style="padding:2px 0">${matchedAlert.triggerRuleName || '温湿度物候其他'}</td></tr>
              <tr><td style="color:#888;padding:2px 0">当前物候</td><td style="padding:2px 0">${phenology}</td></tr>
              <tr><td style="color:#888;padding:2px 0">环境数据</td><td style="padding:2px 0">温度 ${temp} / 湿度 ${hum}</td></tr></table>
            <div style="margin-top:8px;font-size:13px;font-weight:600;margin-bottom:4px">🛡️ 防治建议</div>
            <div style="font-size:12px;color:#595959;line-height:1.6;margin-bottom:4px">${advice}</div>
            <div style="font-size:12px;color:#595959;line-height:1.6;margin-bottom:4px"><span style="color:#888">扩散风险：</span>${lv >= 3 ? '该病害扩散风险极高，已检测到周边果园存在相同病害，若不及时处理可能导致大面积感染，严重影响产量和果实品质。' : lv === 2 ? '该病害存在中等扩散风险，当前环境条件适宜病害发展，建议尽快采取防治措施，防止蔓延至更多果树。' : '目前病害范围较小，扩散风险较低，但仍需保持观察，防止条件变化后加速扩散。'}</div>
            <div style="font-size:12px;margin-bottom:8px"><span style="color:#888">紧急程度：</span><span style="display:inline-block;padding:1px 8px;border-radius:8px;font-size:11px;font-weight:600;color:#fff;background:${lc[lv] || '#888'}">${ll[lv] || '—'}</span></div>
            <div style="margin-top:8px;padding-top:8px;border-top:1px solid #eee;display:flex;justify-content:space-between;align-items:center">
              <span style="font-size:12px;color:#999">📬 更多消息请前往消息中心</span>
              <span onclick="window.location.href='/message'" style="font-size:12px;color:#3D8B40;cursor:pointer;font-weight:500">查看全部 &rarr;</span>
            </div></div>`).openPopup()
        } else {
          // ── 无触发规则预警 → 果园状态正常 ──
          marker.bindPopup(`<div style="min-width:160px"><div style="font-size:15px;font-weight:600;color:#1A1A1A">${o.name}</div>
            <div style="font-size:12px;color:#888;margin-bottom:8px">${o.address || ''}</div>
            <div style="margin-top:8px;padding-top:8px;border-top:1px solid #eee;font-size:12px;color:#34D399;text-align:center;font-weight:500">✅ 当前果园状态很好</div></div>`).openPopup()
        }
      } catch (err) {
        console.error('加载预警详情失败:', err)
        marker.setPopupContent(`<div style="min-width:160px;text-align:center;padding:8px;color:#999">加载失败</div>`).openPopup()
      }
    })
  })

  // ── 热力图层 ──
  if (heatLayer) {
    mapInstance.removeLayer(heatLayer)
  }
  // 收集果园位置 + 风险强度
  const heatPoints: Array<[number, number, number]> = []
  orchardList.forEach(o => {
    const [lat, lng] = parseLocation(o.location)
    // 用风险分数 (0-100) 归一化为 0-1 作为强度，如果没有 riskScore 则用 level 推算
    const intensity = o.riskScore ? o.riskScore / 100 : (o.riskLevel || 1) * 0.33
    heatPoints.push([lat, lng, Math.min(intensity, 1)])
  })
  if (heatPoints.length > 0) {
    heatLayer = createHeatLayer(heatPoints, {
      radius: 40,
      blur: 25,
      maxZoom: 16,
      max: 1.0,
      gradient: { 0.3: '#34D399', 0.5: '#FBBF24', 0.7: '#F87171', 1.0: '#DC2626' }
    }).addTo(mapInstance)
  }

  // 全部果园 → fitBounds；单选 → 最大缩放
  if (selectedOrchard.value === 0) {
    const bounds = L.latLngBounds(orchardList.map(o => parseLocation(o.location)))
    mapInstance.fitBounds(bounds, { padding: [50, 50], maxZoom: 15 })
  } else {
    const [lat, lng] = parseLocation(orchardList[0].location)
    mapInstance.setView([lat, lng], 18)
  }
}

async function fetchOrchards() {
  try {
    const res: any = await getOrchardList()
    const list: any[] = res?.data ?? res ?? []
    orchards.value = list.map((item: any) => ({
      id: item.id,
      name: item.name,
      location: item.location,
      address: item.address || '',
      deviceCount: item.deviceCount || 0,
      riskLevel: 1,
      riskScore: 0,
    }))
    if (orchards.value.length > 0) {
      selectedOrchard.value = 0  // 默认选中全部果园
    }
    await nextTick()
    updateOrchardMarkers()
  } catch {}
}


const miniStats = ref([
  { label: '果园数量', value: '-', color: '#34D399' },
  { label: '今日识别', value: '-', color: '#FB923C' },
  { label: '设备在线', value: '-', color: '#60A5FA' },
  { label: '设备总数', value: '-', color: '#60A5FA' },
])

const weather = ref<any>({})
const sensors = ref<any>({})

const sensorLabels: Record<string, string> = {
  air_temp: '空气温度',
  humidity: '湿度',
  soil_temp: '土壤温度',
  moisture: '土壤含水率',
  conductivity: '电导率',
  ph: 'PH值',
  battery: '电量',
}

function sensorUnit(key: string): string {
  const units: Record<string, string> = {
    air_temp: '°C',
    humidity: '%',
    soil_temp: '°C',
    moisture: '%',
    conductivity: 'uS/cm',
    ph: '',
    battery: 'V',
  }
  return units[key] || ''
}

function sensorColor(key: string, val: any): string {
  if (val == null || val === '--') return '#909399'
  const n = Number(val)
  if (key === 'air_temp' || key === 'soil_temp') {
    if (n > 35 || n < 5) return '#F87171'
    if (n > 30 || n < 10) return '#FBBF24'
    return '#34D399'
  }
  if (key === 'humidity') {
    if (n > 85 || n < 20) return '#F87171'
    return '#34D399'
  }
  if (key === 'ph') {
    if (n < 6 || n > 7.5) return '#F87171'
    return '#34D399'
  }
  if (key === 'battery') {
    if (n < 10) return '#F87171'
    if (n < 11.5) return '#FBBF24'
    return '#34D399'
  }
  return '#60A5FA'
}
const weatherStatus = computed(() => {
  const t = weather.value.temperature
  if (t == null) return ''
  if (t < 5) return '低温，注意防冻'
  if (t > 35) return '高温，注意防晒'
  return '适宜农事作业'
})

const alerts = ref<any[]>([])

// 最新预警列表：取全部预警的前5条
const recentAlerts = computed(() => alerts.value.slice(0, 5))

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

// ── AI 风险预测 ──
const predictionData = ref<PredictionDetail | null>(null)
const predictionLoading = ref(false)

async function fetchPrediction() {
  predictionLoading.value = true
  try {
    const list: any[] = await getOrchardList() as any
    if (list?.length) {
      const firstOrchardId = list[0].id
      const res: any = await getLatestPrediction(firstOrchardId)
      if (res?.id) {
        predictionData.value = parsePrediction(res)
      }
    }
  } catch { /* ignore */ }
  finally { predictionLoading.value = false }
}

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

    // 全部传感器数据
    if (data.sensors) {
      sensors.value = data.sensors
    }

    // 统计
    if (data.statistics) {
      const s = data.statistics
      miniStats.value = [
        { label: '果园数量', value: String(s.orchardCount ?? 0), color: '#34D399' },
        { label: '今日识别', value: String(s.todayDetections ?? 0) + ' 次', color: '#FB923C' },
        { label: '设备在线', value: String(s.deviceOnline ?? 0), color: '#60A5FA' },
        { label: '设备总数', value: String(s.deviceTotal ?? 0), color: '#60A5FA' },
      ]
    }

    // 最新预警
    if (data.recentAlerts) {
      alerts.value = data.recentAlerts
      updateOrchardMarkers() // 预警数据就绪后刷新标记颜色
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
  if (markersLayer) updateOrchardMarkers()
}

watch(selectedOrchard, (val) => {
  if (!mapInstance) return
  // 0=全部果园 → 重新渲染全部标记并fitBounds
  updateOrchardMarkers()
})

onMounted(async () => {
  await nextTick()
  initMap()
  initTrend()
  fetchOrchards()
  fetchCalendar()
  fetchDashboard()
  fetchPendingDocs()
  fetchPrediction()
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
  padding: 8px 20px;
  flex-shrink: 0;
}

.map-title { font-size: 15px; font-weight: 600; color: #1A1A1A; }

.map-legend { display: flex; gap: 16px; margin-left: auto; }
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #1A1A1A; font-weight: 500; }
.legend-bar { width: 24px; height: 8px; border-radius: 4px; }

.map-body {
  flex: 1; position: relative; overflow: hidden;
  background: #E8E4DC;
}

.map-base {
  position: absolute; inset: 0;
  z-index: 1;
}


// ═══ 右：数据面板 — 浅色毛玻璃 ═══
.data-panel {
  width: 360px; min-width: 360px;
  border-left: 1px solid rgba(0,0,0,0.06);
  background: rgba(255,255,255,0.78);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  overflow-y: auto;
  padding: 14px;
  display: flex; flex-direction: column; gap: 12px;
}

// 统一卡片 — 纯白 + 细微阴影 + 微边框
.dp-card {
  background: #FFFFFF;
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.dp-title {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A1A;
  margin-bottom: 2px;
}

.dp-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.dp-tag {
  font-size: 11px;
  color: #8C8C8C;
  background: #F0F2F5;
  padding: 2px 8px;
  border-radius: 6px;
}

.dp-link {
  font-size: 12px;
  color: #3D8B40;
  cursor: pointer;
  &:hover { color: #2E7D32; }
}

// 天气卡片
.weather-card {
  background: linear-gradient(135deg, #F5F8FC, #FAFCFE);
}
.wc-row { display: flex; align-items: center; gap: 14px; }
.wc-icon {
  width: 44px; height: 44px;
  background: rgba(61, 139, 64, 0.08);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #3D8B40;
}
.wc-temp { font-size: 28px; font-weight: 400; color: #1A1A1A; .wc-deg { font-size: 14px; color: #8C8C8C; } }
.wc-meta { font-size: 12px; color: #8C8C8C; margin-top: 2px; }
.wc-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 10px; padding-top: 10px;
  border-top: 1px solid #E8EBF0;
}
.wc-status { font-size: 12px; color: #595959; }
.wc-time { font-size: 11px; color: #8C8C8C; }

// 指标
.metrics-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: 8px; }
.metric-item {
  background: #FAFBFC; border-radius: 10px;
  padding: 10px; display: flex; flex-direction: column;
  border: 1px solid rgba(0,0,0,0.04);
}
.metric-value { font-size: 22px; font-weight: 700; line-height: 1.2; margin-bottom: 2px; }
.metric-label { font-size: 11px; color: #8C8C8C; }

// 趋势
.mini-chart { width: 100%; height: 50px; margin-top: 2px; }

// 预警
.alert-list { display: flex; flex-direction: column; gap: 2px; margin-top: 2px; }
.alert-row {
  display: flex; align-items: center; gap: 8px;
  padding: 8px; border-radius: 8px; cursor: pointer;
  transition: background 0.15s;
  &.unread { background: #F5F7FA; }
  &:hover { background: #FAFBFC; }
}
.alert-bar { width: 4px; height: 26px; border-radius: 2px; flex-shrink: 0; }
.alert-text { font-size: 13px; color: #1A1A1A; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.alert-empty { font-size: 12px; color: #8C8C8C; text-align: center; padding: 10px 0; }

// AI 风险预测
.pred-loading, .pred-empty { font-size: 12px; color: #8C8C8C; text-align: center; padding: 10px 0; }
.pred-level-row { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.pred-badge { font-size: 14px; padding: 4px 14px; }
.pred-score { font-size: 13px; color: #666; }
.pred-prob-bar { display: flex; height: 10px; border-radius: 5px; overflow: hidden; background: #f0f0f0; }
.pred-prob-seg { transition: width 0.3s; }
.pred-time { font-size: 11px; color: #999; margin-top: 6px; }

// 待处理文献
.doc-badge { :deep(.el-badge__content) { font-size: 10px; } }
.doc-list { display: flex; flex-direction: column; gap: 6px; margin-top: 4px; min-height: 40px; }
.doc-row {
  display: flex; align-items: center; gap: 8px;
  padding: 8px; border-radius: 8px;
  transition: background 0.15s;
  &:hover { background: #F5F7FA; }
}
.doc-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; flex-shrink: 0; }
.doc-info { flex: 1; min-width: 0; }
.doc-title { font-size: 13px; font-weight: 500; color: #1A1A1A; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.doc-meta {
  display: flex; align-items: center; gap: 6px; margin-top: 2px;
  font-size: 11px; color: #8C8C8C;
  .el-tag { height: 18px; line-height: 18px; font-size: 10px; padding: 0 6px; }
}
.doc-actions { display: flex; gap: 2px; flex-shrink: 0; }
.doc-empty { font-size: 12px; color: #8C8C8C; text-align: center; padding: 12px 0; }

// 农事日历
.calendar-body {
  .calendar-phenology { font-size: 16px; font-weight: 600; color: #1A1A1A; margin-bottom: 4px; }
  .calendar-term { font-size: 13px; color: #3D8B40; margin-bottom: 8px; }
  .calendar-advice { font-size: 13px; color: #595959; line-height: 1.6; margin-bottom: 8px; }
  .calendar-diseases { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; font-size: 12px; color: #595959; }
  .calendar-label { margin-right: 4px; color: #8C8C8C; }
}

.calendar-empty {
  font-size: 12px; color: #8C8C8C;
  text-align: center; padding: 12px 0;
}

/* 传感器网格 */
.sensor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.sensor-item {
  background: rgba(255,255,255,0.4);
  border-radius: 8px;
  padding: 10px 12px;
  text-align: center;
}
.sensor-value {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}
.sensor-unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}
.sensor-label {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}
</style>

<!-- 地图标记点波纹涟漪扩散动画（非 scoped，作用于 Leaflet 动态生成的 DOM） -->
<style>
.ripple-marker {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ripple-dot {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color);
  border: 2px solid #fff;
  box-shadow: 0 0 4px rgba(0,0,0,0.25);
  z-index: 2;
}
.ripple-ring {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--color);
  animation: ripple-expand 0.6s ease-out infinite;
  z-index: 1;
}
@keyframes ripple-expand {
  0% { width: 20px; height: 20px; opacity: 0.6; }
  100% { width: 80px; height: 80px; opacity: 0; border-width: 1px; }
}
</style>
