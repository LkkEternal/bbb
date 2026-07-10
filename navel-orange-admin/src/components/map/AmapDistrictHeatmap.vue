<template>
  <div class="amap-container">
    <div ref="mapRef" class="map-base"></div>

    <!-- 图例 -->
    <div class="map-legend">
      <div class="legend-title">风险等级</div>
      <div class="legend-bar-wrap">
        <div class="legend-gradient"></div>
        <div class="legend-labels">
          <span>低</span>
          <span>中</span>
          <span>高</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="map-loading">
      <span class="loading-text">🌏 地图加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

const props = withDefaults(defineProps<{
  points?: Array<{ lng: number; lat: number; weight: number }>
  center?: [number, number]
  zoom?: number
  amapKey?: string
}>(), {
  center: () => [114.90, 25.35],
  zoom: 11,
  amapKey: 'acb49eaba3bed860392346e6f34b0bfb',
})

const mapRef = ref<HTMLDivElement>()
const loading = ref(true)

let mapInstance: any = null
let heatmapInstance: any = null
let boundaryOverlays: any[] = []
let textLabels: any[] = []

// ── 模拟数据：惠州区域均匀分布，包含不同风险等级 ──
function generateSimulatedPoints(): Array<{ lng: number; lat: number; weight: number }> {
  const points: Array<{ lng: number; lat: number; weight: number }> = []
  const seed = [
    // 高风险区域 (75-100)
    { lng: 114.86, lat: 25.40, weight: 95 },
    { lng: 114.88, lat: 25.38, weight: 88 },
    { lng: 114.84, lat: 25.35, weight: 82 },
    { lng: 114.92, lat: 25.30, weight: 78 },
    { lng: 114.80, lat: 25.32, weight: 75 },
    // 中高风险区域 (50-74)
    { lng: 114.95, lat: 25.28, weight: 72 },
    { lng: 114.90, lat: 25.42, weight: 68 },
    { lng: 114.82, lat: 25.37, weight: 65 },
    { lng: 114.98, lat: 25.35, weight: 60 },
    { lng: 114.86, lat: 25.28, weight: 55 },
    // 中风险区域 (30-49)
    { lng: 114.93, lat: 25.43, weight: 48 },
    { lng: 114.96, lat: 25.32, weight: 45 },
    { lng: 114.88, lat: 25.44, weight: 42 },
    { lng: 115.00, lat: 25.30, weight: 38 },
    { lng: 114.78, lat: 25.34, weight: 35 },
    // 低风险区域 (10-29)
    { lng: 114.94, lat: 25.38, weight: 28 },
    { lng: 114.85, lat: 25.40, weight: 25 },
    { lng: 114.91, lat: 25.36, weight: 22 },
    { lng: 114.97, lat: 25.40, weight: 18 },
    { lng: 114.83, lat: 25.30, weight: 15 },
    { lng: 114.89, lat: 25.34, weight: 12 },
    { lng: 114.87, lat: 25.42, weight: 10 },
  ]
  // 在种子点周围插值生成更多点，形成平滑过渡
  seed.forEach(s => {
    // 每个种子点生成 3 个微偏移点
    for (let i = 0; i < 3; i++) {
      const offsetLng = (Math.random() - 0.5) * 0.06
      const offsetLat = (Math.random() - 0.5) * 0.06
      const weightJitter = s.weight + (Math.random() - 0.5) * 15
      points.push({
        lng: s.lng + offsetLng,
        lat: s.lat + offsetLat,
        weight: Math.max(5, Math.min(100, Math.round(weightJitter))),
      })
    }
  })
  // 加入原始种子点
  points.push(...seed)
  return points
}

// ── 计算各区县的均值（模拟） ──
function getDistrictData(): Array<{ name: string; center: [number, number]; value: number }> {
  return [
    { name: '惠城区', center: [114.41, 23.08], value: 37.8 },
    { name: '惠阳区', center: [114.46, 22.80], value: 42.5 },
    { name: '惠东县', center: [114.72, 22.98], value: 28.3 },
    { name: '博罗县', center: [114.29, 23.17], value: 55.6 },
    { name: '龙门县', center: [114.26, 23.73], value: 18.2 },
    { name: '大亚湾', center: [114.54, 22.74], value: 35.1 },
    { name: '仲恺区', center: [114.33, 23.05], value: 48.7 },
    { name: '深圳',   center: [114.06, 22.55], value: 32.4 },
    { name: '东莞',   center: [113.75, 23.05], value: 25.8 },
    { name: '河源',   center: [114.70, 23.75], value: 15.3 },
  ]
}

// ── 加载 AMap ──
function loadAMap(): Promise<void> {
  return new Promise((resolve, reject) => {
    if ((window as any).AMap) { resolve(); return }
    const key = props.amapKey
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}&plugin=AMap.HeatMap,AMap.DistrictSearch`
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('AMap 加载失败'))
    document.head.appendChild(script)
  })
}

// ── 渲染边界 + 标签 ──
function renderBoundaries() {
  const AMap = (window as any).AMap
  if (!AMap || !mapInstance) return

  // 清除旧的边界和标签
  boundaryOverlays.forEach(o => mapInstance.remove(o))
  textLabels.forEach(t => mapInstance.remove(t))
  boundaryOverlays = []
  textLabels = []

  // 用 DistrictSearch 获取区县边界
  const district = new AMap.DistrictSearch({
    level: 'district',
    subdistrict: 0,
    extensions: 'all',
  })

  // 搜索惠州及周边区县
  const cities = ['惠城区', '惠阳区', '惠东县', '博罗县', '龙门县']
  const districts = getDistrictData()

  cities.forEach(cityName => {
    district.search(cityName, (status: any, result: any) => {
      if (status === 'complete' && result.districtList?.length) {
        const bounds = result.districtList[0].boundaries
        if (!bounds?.length) return
        const districtData = districts.find(d => d.name === cityName)
        const value = districtData?.value ?? 0
        const riskLabel = value > 50 ? '高风险' : value > 30 ? '中风险' : '低风险'
        const riskColor = value > 50 ? '#FF6B6B' : value > 30 ? '#FFD93D' : '#6BCB77'

        // 计算中心点
        let latSum = 0, lngSum = 0, count = 0
        bounds.forEach((ring: any) => {
          ring.forEach((p: any) => {
            lngSum += Array.isArray(p) ? p[0] : p.lng
            latSum += Array.isArray(p) ? p[1] : p.lat
            count++
          })
        })
        const centerLng = lngSum / count
        const centerLat = latSum / count

        // 画多边形边界（虚线白色）+ 添加交互
        bounds.forEach((ring: any) => {
          const polygon = new AMap.Polygon({
            path: ring,
            fillColor: 'transparent',
            strokeColor: '#ffffff',
            strokeWeight: 2,
            strokeOpacity: 0.6,
            strokeDasharray: [6, 4],
            zIndex: 50,
          })
          polygon.setMap(mapInstance)

          // 鼠标移入：实线 + 加粗 + 指针
          polygon.on('mouseover', () => {
            polygon.setOptions({ strokeWeight: 4, strokeOpacity: 1, strokeDasharray: [1, 0], fillColor: riskColor, fillOpacity: 0.1 })
            mapInstance.setDefaultCursor('pointer')
          })
          polygon.on('mouseout', () => {
            polygon.setOptions({ strokeWeight: 2, strokeOpacity: 0.6, strokeDasharray: [6, 4], fillColor: 'transparent', fillOpacity: 0 })
            mapInstance.setDefaultCursor('')
          })
          // 点击弹出详情
          polygon.on('click', () => showInfo(cityName, value, riskColor, riskLabel, [centerLng, centerLat]))

          boundaryOverlays.push(polygon)
        })

        // 添加数值标签
        const text = new AMap.Text({
          text: value.toFixed(1),
          anchor: 'center',
          draggable: false,
          cursor: 'pointer',
          style: {
            'background-color': 'rgba(0,0,0,0.6)',
            'border': 'none',
            'border-radius': '6px',
            'padding': '4px 10px',
            'font-size': '16px',
            'font-weight': '700',
            'color': riskColor,
            'text-align': 'center',
          },
          position: [centerLng, centerLat],
          zIndex: 60,
        })
        text.setMap(mapInstance)
        // 标签点击也弹出详情
        text.on('click', () => showInfo(cityName, value, riskColor, riskLabel, [centerLng, centerLat]))
        textLabels.push(text)
      }
    })
  })

  // 额外搜索深圳和东莞
  ;['深圳', '东莞'].forEach(cityName => {
    district.search(cityName, (status: any, result: any) => {
      if (status === 'complete' && result.districtList?.length) {
        const bounds = result.districtList[0].boundaries
        if (!bounds?.length) return
        const d = districts.find(x => x.name === cityName)
        if (!d) return
        const val = d.value
        const clr = val > 50 ? '#FF6B6B' : val > 30 ? '#FFD93D' : '#6BCB77'
        const lbl = val > 50 ? '高风险' : val > 30 ? '中风险' : '低风险'
        bounds.forEach((ring: any) => {
          const polygon = new AMap.Polygon({
            path: ring, fillColor: 'transparent',
            strokeColor: '#ffffff', strokeWeight: 2,
            strokeOpacity: 0.6, strokeDasharray: [6, 4], zIndex: 50,
          })
          polygon.setMap(mapInstance)
          boundaryOverlays.push(polygon)
        })
        const text = new AMap.Text({
          text: val.toFixed(1), anchor: 'center', draggable: false, cursor: 'pointer',
          style: {
            'background-color': 'rgba(0,0,0,0.6)', 'border': 'none', 'border-radius': '6px',
            'padding': '4px 10px', 'font-size': '16px', 'font-weight': '700',
            'color': clr, 'text-align': 'center',
          },
          position: d.center, zIndex: 60,
        })
        text.setMap(mapInstance)
        text.on('click', () => showInfo(cityName, val, clr, lbl, d.center))
        textLabels.push(text)
      }
    })
  })
}

/** 显示信息弹窗 */
function showInfo(n: string, v: number, c: string, l: string, center: [number, number]) {
  const AMap = (window as any).AMap
  if (!AMap || !mapInstance) return
  const html = [
    '<div style="min-width:180px;font-family:sans-serif">',
    '<div style="font-size:15px;font-weight:700;color:#1A1A1A;margin-bottom:6px">', n, '</div>',
    '<div style="font-size:12px;color:#888;margin-bottom:8px">惠州市 · 柑橘产区</div>',
    '<table style="font-size:12px;color:#595959;width:100%">',
    '<tr><td style="color:#888;width:72px;padding:2px 0">风险指数</td>',
    '<td style="padding:2px 0;font-weight:600;color:', c, '">', v.toFixed(1), '</td></tr>',
    '<tr><td style="color:#888;padding:2px 0">风险等级</td>',
    '<td style="padding:2px 0"><span style="display:inline-block;padding:0 8px;border-radius:4px;font-size:11px;font-weight:600;color:#fff;background:', c, '">', l, '</span></td></tr>',
    '<tr><td style="color:#888;padding:2px 0">果园数量</td>',
    '<td style="padding:2px 0;font-weight:600">', Math.floor(Math.random() * 8) + 3, ' 个</td></tr>',
    '<tr><td style="color:#888;padding:2px 0">今日识别</td>',
    '<td style="padding:2px 0;font-weight:600">', Math.floor(Math.random() * 20) + 5, ' 次</td></tr></table>',
    '<div style="margin-top:8px;padding-top:8px;border-top:1px solid #eee;font-size:11px;color:#999;text-align:center">点击地图关闭</div></div>',
  ].join('')
  const w = new AMap.InfoWindow({ isCustom: false, offset: new AMap.Pixel(0, -20), closeWhenClickMap: true })
  w.setContent(html)
  w.open(mapInstance, center)
}

// ── 渲染热力图 ──
function renderHeatmap(data: Array<{ lng: number; lat: number; weight: number }>) {
  const AMap = (window as any).AMap
  if (!AMap || !mapInstance) return

  if (heatmapInstance) {
    heatmapInstance.setMap(null)
    heatmapInstance = null
  }

  heatmapInstance = new AMap.HeatMap(mapInstance, {
    radius: 50,
    opacity: [0.2, 0.7],
    gradient: {
      0.2: '#00BBFF',
      0.4: '#00FF00',
      0.6: '#FFFF00',
      0.8: '#FF8C00',
      1.0: '#FF0000',
    },
  })

  heatmapInstance.setDataSet({
    data: data.map(p => ({ lng: p.lng, lat: p.lat, count: p.weight })),
    max: 100,
  })
}

// ── 初始化地图 ──
function initMap() {
  if (!mapRef.value) return

  loadAMap()
    .then(() => {
      const AMap = (window as any).AMap

      mapInstance = new AMap.Map(mapRef.value, {
        zoom: props.zoom,
        center: props.center,
        mapStyle: 'amap://styles/darkblue',
        layers: [new AMap.TileLayer()],
        resizeEnable: true,
      })

      mapInstance.on('complete', () => {
        loading.value = false

        const data = props.points ?? generateSimulatedPoints()
        renderHeatmap(data)
        renderBoundaries()
      })

      setTimeout(() => { loading.value = false }, 10000)
    })
    .catch(() => { loading.value = false })
}

// ── 监听外部数据变化 ──
watch(() => props.points, (val) => {
  if (mapInstance && val) {
    renderHeatmap(val)
    renderBoundaries()
  }
}, { deep: true })

onMounted(() => initMap())

onUnmounted(() => {
  if (heatmapInstance) { heatmapInstance.setMap(null); heatmapInstance = null }
  boundaryOverlays.forEach(o => mapInstance?.remove(o))
  textLabels.forEach(t => mapInstance?.remove(t))
  if (mapInstance) { mapInstance.destroy(); mapInstance = null }
})
</script>

<style scoped lang="scss">
.amap-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.map-base {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.map-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 8px;
  z-index: 100;
}

.loading-text {
  font-size: 16px;
  color: #fff;
  background: rgba(0, 0, 0, 0.7);
  padding: 12px 28px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

/* 图例（右下角） */
.map-legend {
  position: absolute;
  bottom: 30px;
  right: 16px;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  padding: 12px 16px;
  border-radius: 10px;
  z-index: 60;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.legend-title {
  font-size: 12px;
  font-weight: 600;
  color: #ccc;
  margin-bottom: 8px;
  text-align: center;
}

.legend-bar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.legend-gradient {
  width: 140px;
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(90deg, #00BBFF, #00FF00, #FFFF00, #FF8C00, #FF0000);
}

.legend-labels {
  width: 100%;
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #aaa;
}
</style>
