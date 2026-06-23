<template>
  <div class="calendar-page">
    <div class="page-header">
      <h2>农事日历</h2>
      <div class="header-info">
        <span class="header-date">{{ today }}</span>
      </div>
    </div>

    <!-- 当前农事 — 独立设计 -->
    <div v-if="current" class="current-section">
      <div class="cs-bg" :style="{ background: periodColor(current.phenologyName) }"></div>
      <div class="cs-body">
        <div class="cs-main">
          <div class="cs-top">
            <span class="cs-tag">当前农事 · {{ current.phenologyName }}期</span>
            <span class="cs-term" v-if="current.solarTerm">{{ current.solarTerm }}</span>
          </div>
          <div class="cs-progress-row">
            <div class="cs-ring-wrap">
              <svg viewBox="0 0 100 100" class="cs-svg">
                <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="5"/>
                <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(255,255,255,0.85)" stroke-width="5"
                  :stroke-dasharray="circumference"
                  :stroke-dashoffset="circumference - (progressPercent / 100) * circumference"
                  stroke-linecap="round" transform="rotate(-90 50 50)"/>
              </svg>
              <div class="cs-ring-text">{{ progressPercent }}%</div>
            </div>
            <div class="cs-progress-info">
              <div class="cs-progress-title">本阶段进度</div>
              <div class="cs-progress-bar">
                <div class="cs-progress-fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <div class="cs-progress-dates">{{ formatMonthDay(current.startMonth, current.startDay) }} — {{ formatMonthDay(current.endMonth, current.endDay) }}</div>
            </div>
          </div>
          <div class="cs-advice" v-if="current.adviceContent">{{ current.adviceContent }}</div>
          <div class="cs-diseases" v-if="current.diseaseRelated?.length">
            <span class="cs-disease-label">重点防治</span>
            <span v-for="d in current.diseaseRelated" :key="d" class="cs-disease">{{ d }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 全年时段 -->
    <div class="year-section">
      <div class="year-header">
        <span class="year-title">全年物候</span>
        <div class="year-legend">
          <span class="yl-item" v-for="l in legend" :key="l.label">
            <span class="yl-dot" :style="{ background: l.color }"></span>{{ l.label }}
          </span>
        </div>
      </div>
      <div class="year-list">
        <div v-for="(item, i) in timelineItems" :key="item.id" class="year-row"
          :class="{ active: selectedItem?.id === item.id, current: current?.id === item.id }"
          @click="selectedItem = item">
          <div class="yr-line">
            <div class="yr-dot" :style="{ borderColor: periodColor(item.phenologyName) }"
              :class="{ filled: current?.id === item.id }"></div>
            <div v-if="i < timelineItems.length - 1" class="yr-bar"></div>
          </div>
          <div class="yr-body">
            <div class="yr-top">
              <span class="yr-name" :style="{ color: periodColor(item.phenologyName) }">{{ item.phenologyName }}</span>
              <span v-if="current?.id === item.id" class="yr-now" :style="{ background: periodColor(item.phenologyName) }">当前</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface CalendarItem {
  id: number; phenologyName: string; solarTerm: string
  startMonth: number; startDay: number; endMonth: number; endDay: number
  adviceContent: string; diseaseRelated: string[]
}

const today = ref('')
const current = ref<CalendarItem | null>(null)
const list = ref<CalendarItem[]>([])
const selectedItem = ref<CalendarItem | null>(null)

const legend = [
  { label: '春梢', color: '#3D8B40' }, { label: '开花', color: '#FF8C42' },
  { label: '夏梢', color: '#5B8C5A' }, { label: '果期', color: '#E8863A' }, { label: '冬管', color: '#8A8A80' },
]

function periodColor(name: string = ''): string {
  if (!name) return '#3D8B40'
  if (name.includes('春') || name.includes('萌芽')) return '#3D8B40'
  if (name.includes('花')) return '#FF8C42'
  if (name.includes('夏') || name.includes('梢')) return '#5B8C5A'
  if (name.includes('果') || name.includes('膨大') || name.includes('着色') || name.includes('成熟')) return '#E8863A'
  return '#8A8A80'
}

const timelineItems = computed(() => {
  return [...list.value].sort((a, b) => {
    return (a.startMonth * 100 + a.startDay) - (b.startMonth * 100 + b.startDay)
  })
})

const circumference = 2 * Math.PI * 42
const progressPercent = computed(() => {
  if (!current.value) return 0
  const start = current.value.startMonth * 100 + current.value.startDay
  const end = current.value.endMonth * 100 + current.value.endDay
  const now = new Date()
  const todayMd = (now.getMonth() + 1) * 100 + now.getDate()
  const total = end >= start ? end - start : 365 - start + end
  const passed = end >= start ? todayMd - start : (todayMd >= start ? todayMd - start : 365 - start + todayMd)
  return Math.min(100, Math.max(0, Math.round((passed / total) * 100)))
})

function formatMonthDay(m: number, d: number): string {
  return m ? `${m}月${d}日` : ''
}

function computeCurrent(items: CalendarItem[]): CalendarItem | null {
  const now = new Date()
  const md = (now.getMonth() + 1) * 100 + now.getDate()
  for (const item of items) {
    const s = item.startMonth * 100 + item.startDay
    const e = item.endMonth * 100 + item.endDay
    if (s <= e ? (md >= s && md <= e) : (md >= s || md <= e)) return item
  }
  return items[0] || null
}

const mockData: CalendarItem[] = [
  { id: 1, phenologyName: '春梢萌芽', solarTerm: '立春-雨水', startMonth: 2, startDay: 1, endMonth: 3, endDay: 15, adviceContent: '修剪整形，施春肥，注意防治疮痂病和炭疽病。', diseaseRelated: ['疮痂病', '炭疽病'] },
  { id: 2, phenologyName: '开花坐果', solarTerm: '惊蛰-清明', startMonth: 3, startDay: 16, endMonth: 4, endDay: 30, adviceContent: '花期管理，追施花前肥，防治花蕾蛆和红蜘蛛。', diseaseRelated: ['花蕾蛆', '红蜘蛛'] },
  { id: 3, phenologyName: '夏梢生长期', solarTerm: '立夏-芒种', startMonth: 5, startDay: 1, endMonth: 6, endDay: 30, adviceContent: '夏季修剪，及时抹除夏梢，保持树冠通风透光。此阶段潜叶蛾和溃疡病进入高发期，需每7-10天喷药一次，重点关注嫩梢部位。同时加强肥水管理，施好壮果肥。', diseaseRelated: ['潜叶蛾', '溃疡病', '炭疽病'] },
  { id: 4, phenologyName: '果实膨大', solarTerm: '小暑-立秋', startMonth: 7, startDay: 1, endMonth: 8, endDay: 31, adviceContent: '壮果肥，抗旱排涝，防治日灼病和吸果夜蛾。', diseaseRelated: ['日灼病', '吸果夜蛾'] },
  { id: 5, phenologyName: '秋梢成熟', solarTerm: '处暑-秋分', startMonth: 9, startDay: 1, endMonth: 10, endDay: 15, adviceContent: '秋梢管理，施秋肥，注意红蜘蛛和锈壁虱。', diseaseRelated: ['红蜘蛛', '锈壁虱'] },
  { id: 6, phenologyName: '果实着色', solarTerm: '寒露-霜降', startMonth: 10, startDay: 16, endMonth: 11, endDay: 15, adviceContent: '控水增糖，促进着色，防治果实蝇和青霉病。', diseaseRelated: ['果实蝇', '青霉病'] },
  { id: 7, phenologyName: '越冬管理', solarTerm: '小雪-大寒', startMonth: 11, startDay: 16, endMonth: 1, endDay: 31, adviceContent: '采后清园，冬施基肥，树干刷白防冻害。', diseaseRelated: ['冻害', '流胶病'] },
]

onMounted(() => {
  const d = new Date()
  today.value = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  // 直接用模拟数据
  list.value = mockData
  const c = computeCurrent(mockData)
  current.value = c
  selectedItem.value = c
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.calendar-page {
  padding: 24px 28px; max-width: 960px; margin: 0 auto;
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 28px;
  h2 { margin: 0; font-size: 22px; font-weight: 700; color: $text-primary; }
}
.header-info { display: flex; align-items: center; gap: 12px; }
.header-date { font-size: 13px; color: $text-secondary; }

// ═══ 当前农事 ═══
.current-section {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

.cs-bg {
  position: absolute; inset: 0;
  &::after {
    content: ''; position: absolute; inset: 0;
    background: linear-gradient(135deg, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.02) 60%, transparent 100%);
  }
}

.cs-body {
  position: relative; z-index: 1;
  padding: 24px 28px; color: #fff;
}

.cs-main {
  display: flex; flex-direction: column; gap: 16px;
}

.cs-top { display: flex; align-items: center; gap: 10px; }
.cs-tag { font-size: 13px; font-weight: 600; letter-spacing: 1px; }
.cs-term { font-size: 12px; opacity: 0.5; }

// 进度行
.cs-progress-row {
  display: flex; align-items: center; gap: 16px;
}

.cs-ring-wrap {
  width: 52px; height: 52px; flex-shrink: 0;
  position: relative;
}
.cs-svg { width: 100%; height: 100%; display: block; }
.cs-ring-text {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}

.cs-progress-info { flex: 1; }
.cs-progress-title { font-size: 11px; opacity: 0.5; margin-bottom: 6px; }
.cs-progress-bar {
  width: 100%; max-width: 280px; height: 4px;
  background: rgba(255,255,255,0.15); border-radius: 2px; overflow: hidden;
}
.cs-progress-fill { height: 100%; border-radius: 2px; background: rgba(255,255,255,0.85); transition: width 0.5s ease; }
.cs-progress-dates { font-size: 11px; opacity: 0.4; margin-top: 4px; }

// 农事建议
.cs-advice {
  font-size: 14px; line-height: 1.8; opacity: 0.9;
  padding: 14px 16px;
  background: rgba(0,0,0,0.06);
  border-radius: 10px;
  border-left: 3px solid rgba(255,255,255,0.2);
}

// 病虫害
.cs-diseases { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.cs-disease-label { font-size: 11px; opacity: 0.5; }
.cs-disease {
  background: rgba(255,255,255,0.12);
  color: #fff; font-size: 11px; font-weight: 500;
  padding: 3px 10px; border-radius: 8px;
}

// ═══ 全年物候 — 时间线 ═══
.year-section {
  background: $card; border-radius: $radius-lg; box-shadow: $shadow-sm; padding: 24px;
}

.year-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;
}
.year-title { font-size: 16px; font-weight: 600; color: $text-primary; }
.year-legend { display: flex; gap: 14px; }
.yl-item { display: flex; align-items: center; gap: 5px; font-size: 11px; color: $text-secondary; }
.yl-dot { width: 8px; height: 8px; border-radius: 50%; }

.year-list { display: flex; flex-direction: column; gap: 0; }

.year-row {
  display: flex; gap: 16px;
  padding: 12px 0;
  cursor: pointer; transition: all 0.2s;
  border-radius: 8px; margin: 0 -12px; padding: 12px;

  &:hover { background: $primary-subtle; }
  &.active { background: $primary-subtle; }
  &.current { background: linear-gradient(135deg, #FAFCFA, #F5F8F2); }
}

.yr-line {
  display: flex; flex-direction: column; align-items: center;
  width: 16px; flex-shrink: 0; padding-top: 4px;
}

.yr-dot {
  width: 12px; height: 12px; border-radius: 50%;
  border: 2.5px solid; background: $card;
  transition: all 0.2s;
  flex-shrink: 0;
  &.filled { background: currentColor; }
}

.yr-bar {
  width: 2px; flex: 1; min-height: 20px;
  margin-top: 2px;
  background: $divider;
}

.yr-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }

.yr-top { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.yr-name { font-size: 15px; font-weight: 700; letter-spacing: -0.2px; }
.yr-term { font-size: 11px; color: $text-placeholder; }
.yr-now { font-size: 9px; font-weight: 700; color: #fff; padding: 1px 8px; border-radius: 6px; letter-spacing: 0.5px; }

.yr-date { font-size: 12px; color: $text-secondary; }

.yr-advice {
  font-size: 13px; color: $text-primary; line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.yr-pests { display: flex; gap: 4px; flex-wrap: wrap; }
.yr-pest {
  background: rgba(229,57,53,0.06); color: #E53935;
  font-size: 10px; font-weight: 500; padding: 2px 8px; border-radius: 6px;
}
</style>
