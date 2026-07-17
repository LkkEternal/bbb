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

      <!-- 病害分布 -->
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
            <span class="alert-bar" :style="{ background: alertColor(a.level) }"></span>
            <span class="alert-text">{{ a.title }}</span>
          </div>
          <div v-if="alerts.length === 0" class="alert-empty">暂无预警</div>
        </div>
      </div>

      <!-- AI 风险预测 -->
      <div class="dp-card">
        <div class="dp-title-row">
          <span class="dp-title">AI 风险预测</span>
          <span class="dp-link" @click="$router.push('/prediction')">查看详情 &rarr;</span>
        </div>
        <div v-if="predictionLoading" class="pred-empty">加载中...</div>
        <div v-else-if="predictionData" class="pred-body">
          <div class="pred-level-row">
            <el-tag :color="predictionData.riskColor" effect="dark" size="large" class="pred-badge">
              {{ predictionData.riskLabel }}
            </el-tag>
            <span class="pred-score">评分: {{ (predictionData.riskScore * 100).toFixed(1) }}</span>
          </div>
          <div class="pred-prob-bar">
            <div v-for="(p, i) in predictionData.probabilities" :key="i" class="pred-prob-seg" :style="{ width: (p * 100) + '%', backgroundColor: ['#67c23a','#e6a23c','#f56c6c','#c03636'][i] }" />
          </div>
          <div class="pred-time">更新: {{ predictionData.createdAt }}</div>
        </div>
        <div v-else class="pred-empty">暂无预测数据</div>
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
import { getDashboard, getAlertsList, getMessageDetail } from '@/api/modules/dashboard'
import { getOrchardList } from '@/api/modules/orchard'
import { getRiskAssessment } from '@/api/modules/alert'
import { getLatestPrediction, parsePrediction, type PredictionDetail } from '@/api/modules/prediction'
import { DISEASE_MAP } from '@/constants/disease'
import dayjs from 'dayjs'

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
    options.push({ value: o.id, label: o.name + (o.riskLevel >= 2 ? ' ⚠' : '') })
  })
  return options
})

const riskColors = ['#34D399', '#FBBF24', '#F87171']
const riskLabels = ['低风险', '中风险', '高风险']

const AMAP_ROAD = 'https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}'

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
        let matchedAlert = alerts.value.find((a: any) => a.orchardName === o.name && a.relatedType !== 'detection')
        if (!matchedAlert) {
          try {
            const msgRes: any = await getAlertsList({ page: 1, size: 20 })
            const msgList = msgRes?.records ?? (Array.isArray(msgRes) ? msgRes : [])
            const found = msgList.find((a: any) => a.orchardName === o.name)
            if (found) {
              const detail: any = await getMessageDetail(found.id)
              const detailData = detail?.data ?? detail
              if (detailData) matchedAlert = detailData
            }
          } catch {}
        }
        if (matchedAlert) {
          // ── 有触发规则预警 → 风险摘要 ──
          // 数据来源：phenology ← calendarData (农事日历API); temp/humidity ← weather (dashboard API currentWeather)
          const phenology = calendarData.value?.current?.phenologyName || '—'
          const temp = weather.value.temperature != null ? weather.value.temperature + '℃' : '—'
          const hum = weather.value.humidity != null ? weather.value.humidity + '%' : '—'
          const lv = matchedAlert.level || o.riskLevel || 1
          const advice = matchedAlert.advice || '暂无建议'
          const ll = ['', 'L1 低', 'L2 中', 'L3 高']; const lc = ['', '#34D399', '#FBBF24', '#F87171']
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
          marker.bindPopup(`<div style="min-width:160px"><div style="font-size:15px;font-weight:600;color:#1A1A1A">${o.name}</div>
            <div style="font-size:12px;color:#888;margin-bottom:8px">${o.address || ''}</div>
            <div style="margin-top:8px;padding-top:8px;border-top:1px solid #eee;font-size:12px;color:#999;text-align:center">暂无预警数据</div></div>`).openPopup()
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
  const heatPoints: Array<[number, number, number]> = []
  orchardList.forEach(o => {
    const [lat, lng] = parseLocation(o.location)
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
    orchards.value.forEach(o => fetchOrchardRisk(o.id))
  } catch {}
}

async function fetchOrchardRisk(orchardId: number) {
  try {
    const res: any = await getRiskAssessment(orchardId)
    const data = res?.data ?? res
    if (!data) return
    const orchard = orchards.value.find(o => o.id === orchardId)
    if (orchard) {
      orchard.riskLevel = data.riskLevel || 1
      orchard.riskScore = data.riskScore || 0
    }
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
const weatherStatus = computed(() => {
  const t = weather.value.temperature
  if (t == null) return ''
  if (t < 5) return '低温，注意防冻'
  if (t > 35) return '高温，注意防晒'
  return '适宜农事作业'
})

const alerts = ref<any[]>([])

// ── AI 风险预测 ──
const predictionData = ref<PredictionDetail | null>(null)
const predictionLoading = ref(false)

async function fetchPrediction() {
  predictionLoading.value = true
  try {
    const list: any[] = await getOrchardList() as any
    if (list?.length) {
      const res: any = await getLatestPrediction(list[0].id)
      if (res?.id) predictionData.value = parsePrediction(res)
    }
  } catch { /* ignore */ }
  finally { predictionLoading.value = false }
}

function alertColor(lv: number) { return lv >= 3 ? '#E53935' : lv >= 2 ? '#FF8C42' : '#3D8B40' }

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

    if (data.currentWeather) {
      weather.value = data.currentWeather
    }

    if (data.statistics) {
      const s = data.statistics
      miniStats.value = [
        { label: '果园数量', value: String(s.orchardCount ?? 0), color: '#34D399' },
        { label: '今日识别', value: String(s.todayDetections ?? 0) + ' 次', color: '#FB923C' },
        { label: '设备在线', value: String(s.deviceOnline ?? 0), color: '#60A5FA' },
        { label: '设备总数', value: String(s.deviceTotal ?? 0), color: '#60A5FA' },
      ]
    }

    if (data.recentAlerts) {
      alerts.value = data.recentAlerts
    }

    if (data.diseaseDistribution) {
      await nextTick()
      initTrendFromData(data.diseaseDistribution)
    }
  } catch {}
}

function initTrend() {
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
}</style>

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
