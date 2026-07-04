<template>
  <div class="dashboard">
    <!-- 地图全屏背景 -->
    <div class="map-canvas">
      <OrchardMap
        :centers="centers"
        :orchards="orchardOptions"
        :selected-orchard="selectedOrchard"
        :zones="currentZones"
        @update:selectedOrchard="selectedOrchard = $event"
      />
    </div>

    <!-- 全屏浮动信息层 -->
    <div class="hud">

      <!-- 顶栏：极简 -->
      <div class="hud-top">
        <div class="ht-left">
          <span class="ht-logo">N</span>
          <span class="ht-title">Navel Orange Guard</span>
        </div>
        <div class="ht-right">
          <div class="ht-temp">{{ weather.temperature ?? '--' }}&deg;C</div>
          <div class="ht-humi">{{ weather.humidity ?? '--' }}%</div>
          <div class="ht-divider"></div>
          <div class="ht-time">{{ currentTime }}</div>
        </div>
      </div>

      <!-- 中央：超大主数字 -->
      <div class="hud-center">
        <div class="hud-hero">
          <div class="hh-label">在线设备</div>
          <div class="hh-value" style="color: #5CB85C;">{{ deviceOnline }}</div>
          <div class="hh-sub">/ {{ deviceTotal }} 台设备</div>
        </div>
      </div>

      <!-- 左侧：病害分布 -->
      <div class="hud-left">
        <div class="hl-header">
          <span class="hl-title">病害分布</span>
          <span class="hl-badge">今日</span>
        </div>
        <div class="hl-chart-wrap">
          <div v-if="!hasChartData" class="hl-sk"><div class="hl-spin"></div></div>
          <div ref="chartRef" v-show="hasChartData" class="hl-chart"></div>
        </div>
        <div v-if="hasChartData" class="hl-legend">
          <div v-for="item in chartLegend" :key="item.name" class="hl-li"
            @mouseenter="highlightChart(item.name)" @mouseleave="clearChartHighlight">
            <span class="hll-dot" :style="{ background: item.color }"></span>
            <span class="hll-n">{{ item.name }}</span>
            <span class="hll-v">{{ item.value }}</span>
          </div>
        </div>
      </div>

      <!-- 底部：预警滚动条 -->
      <div class="hud-bottom">
        <div class="hb-label">&#9888; 实时预警</div>
        <div class="hb-scroll">
          <div class="hb-track">
            <div v-for="a in alerts" :key="a.id" class="hb-item" :style="{ '--c': alertColor(a.level) }">
              <span class="hbi-dot"></span>
              <span class="hbi-t">{{ a.title }}</span>
              <span class="hbi-l">{{ alertLevelLabel(a.level) }}</span>
            </div>
            <div v-if="!alerts.length" class="hb-item" style="opacity:0.2">
              <span class="hbi-t">系统运行正常，暂无预警</span>
            </div>
          </div>
        </div>
        <div class="hb-divider"></div>
        <div class="hb-orchards">
          <button v-for="o in orchardOptions" :key="o.value"
            :class="{ active: selectedOrchard === o.value }"
            @click="selectedOrchard = o.value">
            {{ o.label.replace('脐橙', '') }}
          </button>
        </div>
      </div>

      <!-- 右下：果园日历简报 -->
      <div class="hud-right">
        <div class="hr-weather">
          <div class="hrw-icon">&#9728;</div>
          <div class="hrw-body">
            <span class="hrw-t">{{ weather.temperature ?? '--' }}&deg;C</span>
            <span class="hrw-s">{{ weatherStatus }}</span>
          </div>
        </div>
        <div class="hr-cal" v-if="calendarData?.current">
          <div class="hrc-p">{{ calendarData.current.phenologyName }}</div>
          <div v-if="calendarData.current.solarTerm" class="hrc-st">{{ calendarData.current.solarTerm }}</div>
        </div>
        <div class="hr-stats">
          <div class="hrs-item"><span class="hrs-v" style="color:#3D8B40">{{ orchardCount }}</span><span class="hrs-l">果园</span></div>
          <div class="hrs-item"><span class="hrs-v" style="color:#FF8C42">{{ todayDetections }}</span><span class="hrs-l">识别</span></div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getCurrentReminder } from '@/api/modules/calendar'
import { getDashboard } from '@/api/modules/dashboard'
import { DISEASE_MAP } from '@/constants/disease'
import dayjs from 'dayjs'

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null
const selectedOrchard = ref('A')
const hasChartData = ref(false)
const chartLegend = ref<Array<{ name: string; value: number; color: string }>>([])
const currentTime = ref('')

const deviceOnline = ref('--')
const deviceTotal = ref('--')
const orchardCount = ref('--')
const todayDetections = ref('--')

const orchardOptions = [
  { value: 'A', label: '脐橙果园 A' }, { value: 'B', label: '脐橙果园 B' }, { value: 'C', label: '脐橙果园 C' },
]
const centers: Record<string, [number, number]> = { A: [25.830, 114.930], B: [25.810, 114.950], C: [25.850, 114.910] }
interface Zone { label: string; x: number; y: number; r: number; risk: number }
const orchardData: Record<string, Zone[]> = {
  A: [{ label: '东区A', x: 72, y: 28, r: 14, risk: 2 }, { label: '东区B', x: 84, y: 35, r: 11, risk: 1 }, { label: '西区A', x: 22, y: 32, r: 16, risk: 1 }, { label: '西区B', x: 14, y: 45, r: 12, risk: 3 }, { label: '中心', x: 48, y: 42, r: 10, risk: 1 }, { label: '北坡', x: 52, y: 14, r: 11, risk: 2 }, { label: '南坡', x: 44, y: 72, r: 13, risk: 1 }],
  B: [{ label: '东区', x: 75, y: 35, r: 16, risk: 1 }, { label: '西区', x: 25, y: 38, r: 14, risk: 2 }, { label: '中心', x: 50, y: 45, r: 12, risk: 1 }],
  C: [{ label: '东区', x: 76, y: 32, r: 14, risk: 3 }, { label: '西区', x: 24, y: 36, r: 12, risk: 1 }, { label: '中心', x: 50, y: 42, r: 10, risk: 2 }],
}
const currentZones = computed(() => orchardData[selectedOrchard.value] || [])

const weather = ref<any>({})
const weatherStatus = computed(() => { const t = weather.value.temperature; if (t == null) return ''; if (t < 5) return '低温'; if (t > 35) return '高温'; return '适宜' })
const alerts = ref<any[]>([])
function alertColor(lv: number) { return lv >= 3 ? '#EF5350' : lv >= 2 ? '#FFB74D' : '#5CB85C' }
function alertLevelLabel(lv: number) { return lv >= 3 ? '紧急' : lv >= 2 ? '中级' : '一般' }

const calendarData = ref<any>(null)
async function fetchCalendar() { try { calendarData.value = await getCurrentReminder() } catch { } }

function initChart(distribution: any[]) {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  const colors = ['#3D8B40', '#FF8C42', '#EF5350', '#D4A853', '#7A8A8A', '#42A5F5']
  const data = distribution.map((d, i) => ({ name: DISEASE_MAP[d.name]?.name ?? d.name, value: d.value, itemStyle: { color: DISEASE_MAP[d.name]?.color ?? colors[i % colors.length] } }))
  chartLegend.value = data.map(d => ({ name: d.name, value: d.value, color: d.itemStyle.color as string }))
  chartInstance.setOption({
    tooltip: { trigger: 'item', backgroundColor: 'rgba(0,0,0,0.8)', borderColor: 'rgba(255,255,255,0.05)', borderWidth: 1, textStyle: { color: '#fff', fontSize: 11 }, borderRadius: 8, padding: [6, 10], formatter: (p: any) => `<b style="color:#fff">${p.name}</b><br/><span style="color:rgba(255,255,255,0.5)">${p.value} 次 (${p.percent}%)</span>` },
    series: [{ type: 'pie', radius: ['45%', '68%'], center: ['50%', '50%'], padAngle: 3, itemStyle: { borderRadius: 4, borderColor: 'rgba(10,20,10,0.9)', borderWidth: 2 }, label: { show: false }, emphasis: { itemStyle: { shadowBlur: 8, shadowColor: 'rgba(92,184,92,0.15)' } }, animationType: 'scale', animationEasing: 'elasticOut', animationDelay: () => Math.random() * 100, data }],
  })
}
function highlightChart(name: string) { chartInstance?.dispatchAction({ type: 'highlight', seriesIndex: 0, name }) }
function clearChartHighlight() { chartInstance?.dispatchAction({ type: 'downplay', seriesIndex: 0 }) }

async function fetchDashboardData() {
  try {
    const data: any = await getDashboard()
    if (!data) return
    if (data.currentWeather) weather.value = data.currentWeather
    if (data.statistics) {
      const s = data.statistics
      orchardCount.value = String(s.orchardCount ?? 0)
      todayDetections.value = String(s.todayDetections ?? 0)
      deviceOnline.value = String(s.deviceOnline ?? 0)
      deviceTotal.value = String(s.deviceTotal ?? 0)
    }
    if (data.recentAlerts) alerts.value = data.recentAlerts
    if (data.diseaseDistribution?.length) { hasChartData.value = true; await nextTick(); initChart(data.diseaseDistribution) } else hasChartData.value = false
  } catch { hasChartData.value = false }
}

onMounted(async () => {
  await nextTick(); fetchCalendar(); fetchDashboardData()
  setInterval(() => { currentTime.value = dayjs().format('HH:mm:ss') }, 1000)
  currentTime.value = dayjs().format('HH:mm:ss')
})
onUnmounted(() => { chartInstance?.dispose() })
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.dashboard {
  position: relative; height: calc(100vh - $header-height - 2px); overflow: hidden;
  background: #060d06;
}

.map-canvas { position: absolute; inset: 0; z-index: 0; }

/* ─── HUD 全息信息层 ─── */
.hud {
  position: absolute; inset: 0; z-index: 10;
  pointer-events: none;
  & > * { pointer-events: auto; }
}

/* ─── 顶栏 ─── */
.hud-top {
  position: absolute; top: 0; left: 0; right: 0;
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 24px;
}
.ht-left { display: flex; align-items: center; gap: 10px; }
.ht-logo { width: 28px; height: 28px; background: #5CB85C; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; color: #060d06; font-style: normal; }
.ht-title { font-size: 13px; color: rgba(255,255,255,0.15); font-weight: 500; letter-spacing: 1px; }
.ht-right { display: flex; align-items: center; gap: 12px; font-size: 12px; color: rgba(255,255,255,0.06); }
.ht-divider { width: 1px; height: 10px; background: rgba(255,255,255,0.03); }
.ht-time { font-variant-numeric: tabular-nums; color: rgba(255,255,255,0.03); letter-spacing: 1px; }

/* ─── 中央主数字 ─── */
.hud-center {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -60%);
  text-align: center;
}
.hud-hero {}
.hh-label { font-size: 11px; color: rgba(255,255,255,0.06); letter-spacing: 4px; text-transform: uppercase; margin-bottom: 4px; }
.hh-value { font-size: 80px; font-weight: 200; line-height: 1; letter-spacing: -3px; animation: hhPulse 3s ease-in-out infinite; }
@keyframes hhPulse { 0%,100% { opacity: 0.8; } 50% { opacity: 1; } }
.hh-sub { font-size: 14px; color: rgba(255,255,255,0.04); margin-top: 4px; letter-spacing: 2px; }

/* ─── 左侧病害 ─── */
.hud-left {
  position: absolute; top: 60px; left: 24px;
  width: 180px;
  background: rgba(6,13,6,0.5); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.01); border-radius: 8px;
  padding: 12px;
}
.hl-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.hl-title { font-size: 9px; font-weight: 600; color: rgba(255,255,255,0.04); text-transform: uppercase; letter-spacing: 1px; }
.hl-badge { font-size: 7px; padding: 1px 5px; border-radius: 3px; background: rgba(92,184,92,0.03); color: rgba(92,184,92,0.08); }
.hl-chart-wrap { height: 60px; display: flex; align-items: center; justify-content: center; }
.hl-chart { width: 100%; height: 60px; }
.hl-sk { display: flex; align-items: center; justify-content: center; }
.hl-spin { width: 24px; height: 24px; border-radius: 50%; border: 2px solid rgba(255,255,255,0.02); border-top-color: rgba(92,184,92,0.08); animation: hlS 0.8s linear infinite; }
@keyframes hlS { to { transform: rotate(360deg); } }
.hl-legend { display: flex; flex-wrap: wrap; gap: 2px; margin-top: 4px; }
.hl-li { display: flex; align-items: center; gap: 3px; padding: 1px 4px; border-radius: 2px; cursor: pointer; &:hover { background: rgba(255,255,255,0.01); } }
.hll-dot { width: 4px; height: 4px; border-radius: 50%; flex-shrink: 0; }
.hll-n { font-size: 7px; color: rgba(255,255,255,0.03); }
.hll-v { font-size: 7px; font-weight: 600; color: rgba(255,255,255,0.05); }

/* ─── 底部预警 ─── */
.hud-bottom {
  position: absolute; bottom: 0; left: 0; right: 0;
  display: flex; align-items: center; gap: 16px;
  padding: 8px 24px;
  background: rgba(6,13,6,0.4); backdrop-filter: blur(12px);
  border-top: 1px solid rgba(255,255,255,0.005);
}
.hb-label { font-size: 10px; color: rgba(255,255,255,0.03); letter-spacing: 1px; white-space: nowrap; }
.hb-scroll { flex: 1; overflow: hidden; }
.hb-track { display: flex; gap: 20px; animation: hbMarquee 20s linear infinite; }
@keyframes hbMarquee { 0% { transform: translateX(0); } 100% { transform: translateX(-50%); } }
.hb-item { display: flex; align-items: center; gap: 6px; font-size: 10px; color: rgba(255,255,255,0.04); white-space: nowrap; }
.hbi-dot { width: 4px; height: 4px; border-radius: 50%; background: var(--c, #5CB85C); flex-shrink: 0; }
.hbi-l { font-size: 8px; padding: 0 4px; border-radius: 2px; background: var(--c, #5CB85C); color: #fff; line-height: 1.4; }
.hb-divider { width: 1px; height: 16px; background: rgba(255,255,255,0.01); }
.hb-orchards { display: flex; gap: 3px; }
.hb-orchards button {
  padding: 3px 10px; border-radius: 4px; border: none;
  background: rgba(255,255,255,0.01); color: rgba(255,255,255,0.04); font-size: 9px;
  cursor: pointer; transition: all 0.2s;
  &:hover { background: rgba(255,255,255,0.02); }
  &.active { background: rgba(92,184,92,0.04); color: rgba(92,184,92,0.1); }
}

/* ─── 右下信息 ─── */
.hud-right {
  position: absolute; bottom: 48px; right: 24px;
  display: flex; flex-direction: column; gap: 8px;
  background: rgba(6,13,6,0.4); backdrop-filter: blur(16px);
  border: 1px solid rgba(255,255,255,0.005); border-radius: 8px;
  padding: 14px;
  min-width: 130px;
}
.hr-weather { display: flex; align-items: center; gap: 8px; }
.hrw-icon { font-size: 22px; opacity: 0.2; }
.hrw-body { display: flex; flex-direction: column; }
.hrw-t { font-size: 16px; font-weight: 300; color: rgba(255,255,255,0.2); }
.hrw-s { font-size: 9px; color: rgba(255,255,255,0.03); }

.hr-cal { border-top: 1px solid rgba(255,255,255,0.005); padding-top: 8px; }
.hrc-p { font-size: 12px; font-weight: 600; color: rgba(255,255,255,0.06); }
.hrc-st { font-size: 9px; color: rgba(92,184,92,0.08); margin-top: 1px; }

.hr-stats { display: flex; gap: 12px; border-top: 1px solid rgba(255,255,255,0.005); padding-top: 8px; }
.hrs-item { display: flex; flex-direction: column; }
.hrs-v { font-size: 18px; font-weight: 200; line-height: 1; }
.hrs-l { font-size: 8px; color: rgba(255,255,255,0.02); margin-top: 1px; letter-spacing: 1px; }
</style>
