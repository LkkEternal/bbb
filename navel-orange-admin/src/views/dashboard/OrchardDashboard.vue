<template>
  <div class="dashboard">
    <div class="bg-grid"></div>
    <div class="bg-terrain"></div>
    <div class="bg-glow g1"></div>
    <div class="bg-glow g2"></div>

    <svg class="topo-lines" viewBox="0 0 1000 600" preserveAspectRatio="xMidYMid slice">
      <line v-for="(l,i) in topoLines" :key="i" :x1="l.x1" :y1="l.y1" :x2="l.x2" :y2="l.y2" :style="{'--d':l.delay+'s'}"/>
      <circle v-for="(n,i) in topoNodes" :key="'n'+i" :cx="n.x" :cy="n.y" r="3" :style="{'--d':n.delay+'s'}"/>
    </svg>

    <div class="top-bar">
      <div class="tb-left">
        <span class="tb-logo">N</span>
        <span class="tb-divider"></span>
        <span class="tb-title">脐橙病虫害监测系统</span>
        <span class="tb-status"><span class="tb-dot"></span>系统运行正常</span>
      </div>
      <div class="tb-right">
        <span class="tb-time">{{ currentTime }}</span>
        <span class="tb-divider"></span>
        <span class="tb-weather">{{ weather.temperature ?? '--' }}&deg;C</span>
      </div>
    </div>

    <div class="map-area">
      <OrchardMap
        :centers="centers"
        :orchards="orchardOptions"
        :selected-orchard="selectedOrchard"
        :zones="currentZones"
        @update:selectedOrchard="selectedOrchard=$event"
      />
    </div>

    <div class="data-panel">
      <div class="dp-section orchard-select">
        <span class="dp-label">我的果园</span>
        <div class="orchard-chips">
          <button v-for="o in orchardOptions" :key="o.value" :class="{active:selectedOrchard===o.value}" @click="selectedOrchard=o.value">
            <span class="oc-dot"></span>{{ o.label }}
          </button>
        </div>
      </div>
      <div class="dp-section metrics">
        <span class="dp-label">关键指标</span>
        <div class="metric-grid">
          <div v-for="(s,i) in miniStats" :key="s.label" class="metric-card" :style="{'--d':i*0.06+'s'}">
            <div class="mc-value" :style="{color:s.color}">{{ s.value }}</div>
            <div class="mc-label">{{ s.label }}</div>
          </div>
        </div>
      </div>
      <div class="dp-section">
        <div class="dp-row"><span class="dp-label">病害分布</span><span class="dp-tag">今日</span></div>
        <div v-if="!hasChartData" class="skel-chart"><div class="skel-ring"></div><div class="skel-bars"><div v-for="i in 3" :key="i" class="skel-bar"><div class="skel-fill" style="width:30%"></div></div></div></div>
        <div ref="chartRef" v-show="hasChartData" class="chart-box"></div>
        <div v-if="hasChartData" class="chart-legend">
          <span v-for="item in chartLegend" :key="item.name" class="cl-item" @mouseenter="highlightChart(item.name)" @mouseleave="clearChartHighlight">
            <span class="cl-dot" :style="{background:item.color}"></span><span class="cl-n">{{ item.name }}</span>
          </span>
        </div>
      </div>
      <div class="dp-section">
        <div class="dp-row"><span class="dp-label">最新预警</span><span class="dp-link" @click="$router.push('/message')">全部</span></div>
        <div class="alert-list">
          <div v-for="(a,i) in alerts.slice(0,3)" :key="a.id" class="alert-item" :class="{seen:a.read}" :style="{'--i':i}">
            <span class="ai-badge" :style="{background:alertColor(a.level)}"></span><span class="ai-title">{{ a.title }}</span>
          </div>
          <div v-if="alerts.length===0" class="empty">暂无预警</div>
        </div>
      </div>
    </div>

    <div class="right-panel" :class="{folded:panelFolded}">
      <button class="panel-toggle" @click="panelFolded=!panelFolded">&#9776;</button>
      <div class="panel-scroll">
        <div class="pcard weather animated" style="--o:0.05s">
          <div class="w-bg"></div>
          <div class="w-body">
            <div class="w-temp">{{ weather.temperature ?? '--' }}<span class="w-deg">&deg;C</span></div>
            <div class="w-info"><span>湿度 {{ weather.humidity ?? '--' }}%</span><span class="w-dot"></span><span>{{ weatherStatus }}</span></div>
          </div>
        </div>
        <div class="pcard animated" style="--o:0.12s">
          <div class="pc-head"><span class="pc-t">待处理文献</span><el-badge v-if="pendingDocCount>0" :value="pendingDocCount" :max="99" class="doc-badge"/></div>
          <div class="pc-body" v-loading="docLoading" element-loading-background="rgba(255,255,255,0.1)">
            <div v-for="doc in pendingDocs.slice(0,2)" :key="doc.id" class="doc-item">
              <div class="di-icon" :style="{background:docStatusInfo(doc.status).color+'15',color:docStatusInfo(doc.status).color}">{{ (doc.fileType||'?').charAt(0) }}</div>
              <div class="di-body"><div class="di-t">{{ doc.title }}</div><div class="di-m">{{ formatFileSize(doc.fileSize) }} &middot; {{ docStatusInfo(doc.status).label }}</div></div>
            </div>
            <div v-if="!docLoading && pendingDocs.length===0" class="empty">暂无</div>
          </div>
        </div>
        <div class="pcard animated" style="--o:0.19s">
          <div class="pc-head"><span class="pc-t">&#127793; 农事日历</span></div>
          <div class="pc-body">
            <div v-if="calendarData?.current"><div class="cal-phen">{{ calendarData.current.phenologyName }} <span v-if="calendarData.current.solarTerm" class="cal-term">{{ calendarData.current.solarTerm }}</span></div><div class="cal-adv">{{ calendarData.current.adviceContent||'暂无建议' }}</div></div>
            <div v-else class="empty">暂无数据</div>
          </div>
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
import { getPendingDocuments, getFailedDocuments } from '@/api/modules/knowledge'
import { DISEASE_MAP } from '@/constants/disease'
import { KNOWLEDGE_STATUS_MAP } from '@/constants/enums'
import { formatFileSize } from '@/utils/format'
import dayjs from 'dayjs'

const chartRef=ref<HTMLElement>();let chartInstance:echarts.ECharts|null=null
const panelFolded=ref(false);const selectedOrchard=ref('A');const hasChartData=ref(false);const currentTime=ref('')
const chartLegend=ref<Array<{name:string;value:number;color:string}>>([])

const orchardOptions=[{value:'A',label:'我的果园 A'},{value:'B',label:'我的果园 B'}]
const centers:Record<string,[number,number]>={A:[25.830,114.930],B:[25.810,114.950]}
interface Zone{label:string;x:number;y:number;r:number;risk:number}
const orchardData:Record<string,Zone[]>={A:[{label:'东区A',x:72,y:28,r:14,risk:2},{label:'东区B',x:84,y:35,r:11,risk:1},{label:'西区A',x:22,y:32,r:16,risk:1},{label:'西区B',x:14,y:45,r:12,risk:3},{label:'中心',x:48,y:42,r:10,risk:1},{label:'北坡',x:52,y:14,r:11,risk:2},{label:'南坡',x:44,y:72,r:13,risk:1}],B:[{label:'东区',x:75,y:35,r:16,risk:1},{label:'西区',x:25,y:38,r:14,risk:2},{label:'中心',x:50,y:45,r:12,risk:1}]}
const currentZones=computed(()=>orchardData[selectedOrchard.value]||[])

const topoNodes=Array.from({length:12},(_,i)=>({x:80+Math.random()*840,y:40+Math.random()*520,delay:i*0.3}))
const topoLines=Array.from({length:16},(_,i)=>{const a=topoNodes[Math.floor(Math.random()*topoNodes.length)];const b=topoNodes[Math.floor(Math.random()*topoNodes.length)];return{x1:a.x,y1:a.y,x2:b.x,y2:b.y,delay:i*0.2}})

const miniStats=ref([{label:'果园面积',value:'-',color:'#5CB85C'},{label:'今日识别',value:'-',color:'#FFB74D'},{label:'设备在线',value:'-',color:'#64B5F6'},{label:'设备总数',value:'-',color:'#81C784'}])
const weather=ref<any>({});const weatherStatus=computed(()=>{const t=weather.value.temperature;if(t==null)return'';if(t<5)return'低温';if(t>35)return'高温';return'适宜'})
const alerts=ref<any[]>([]);function alertColor(lv:number){return lv>=3?'#EF5350':lv>=2?'#FFB74D':'#5CB85C'}
interface PendingDoc{id:number;title:string;fileType:string;fileSize:number;status:number;createdAt:string}
const pendingDocs=ref<PendingDoc[]>([]);const pendingDocCount=ref(0);const docLoading=ref(false)
async function fetchPendingDocs(){docLoading.value=true;try{const[p,f]=await Promise.all([getPendingDocuments(2),getFailedDocuments(2)]);const pl:PendingDoc[]=p?.records??p??[];const fl:PendingDoc[]=f?.records??f??[];pendingDocs.value=[...fl,...pl].slice(0,3);pendingDocCount.value=(p?.total??0)+(f?.total??0)}catch{}finally{docLoading.value=false}}
function docStatusInfo(s:number){return KNOWLEDGE_STATUS_MAP[s]??{label:'未知',color:'#909399'}}
function formatTime(t:string|undefined|null){return t?dayjs(t).format('YYYY-MM-DD HH:mm:ss'):''}
const calendarData=ref<any>(null);async function fetchCalendar(){try{calendarData.value=await getCurrentReminder()}catch{}}

function initChart(distribution:any[]){
  if(!chartRef.value)return
  if(!chartInstance)chartInstance=echarts.init(chartRef.value,'dark')
  const colors=['#5CB85C','#FFB74D','#EF5350','#D4A853','#64B5F6','#CE93D8']
  const data=distribution.map((d,i)=>({name:DISEASE_MAP[d.name]?.name??d.name,value:d.value,itemStyle:{color:DISEASE_MAP[d.name]?.color??colors[i%colors.length]}}))
  chartLegend.value=data.map(d=>({name:d.name,value:d.value,color:d.itemStyle.color as string}))
  chartInstance.setOption({tooltip:{trigger:'item',backgroundColor:'rgba(15,25,15,0.9)',borderColor:'rgba(92,184,92,0.1)',borderWidth:1,textStyle:{color:'#fff',fontSize:11},borderRadius:6,padding:[6,10],formatter:(p:any)=>`<b>${p.name}</b><br/>${p.value} 次 (${p.percent}%)`},series:[{type:'pie',radius:['30%','60%'],center:['50%','50%'],padAngle:3,itemStyle:{borderRadius:3,borderColor:'rgba(15,25,15,0.8)',borderWidth:2},label:{show:false},emphasis:{itemStyle:{shadowBlur:6,shadowColor:'rgba(92,184,92,0.15)'}},animationType:'scale',animationEasing:'elasticOut',animationDelay:()=>Math.random()*100,data}]})
}
function highlightChart(name:string){chartInstance?.dispatchAction({type:'highlight',seriesIndex:0,name})}
function clearChartHighlight(){chartInstance?.dispatchAction({type:'downplay',seriesIndex:0})}

async function fetchDashboard(){try{const data:any=await getDashboard();if(!data)return;if(data.currentWeather)weather.value=data.currentWeather;if(data.statistics){const s=data.statistics;miniStats.value=[{label:'果园面积',value:String(s.orchardCount??0),color:'#5CB85C'},{label:'今日识别',value:String(s.todayDetections??0),color:'#FFB74D'},{label:'设备在线',value:String(s.deviceOnline??0),color:'#64B5F6'},{label:'设备总数',value:String(s.deviceTotal??0),color:'#81C784'}]}if(data.recentAlerts)alerts.value=data.recentAlerts;if(data.diseaseDistribution?.length){hasChartData.value=true;await nextTick();initChart(data.diseaseDistribution)}else hasChartData.value=false}catch{hasChartData.value=false}}

onMounted(async()=>{await nextTick();fetchCalendar();fetchDashboard();fetchPendingDocs();setInterval(()=>{currentTime.value=dayjs().format('HH:mm:ss')},1000);currentTime.value=dayjs().format('HH:mm:ss')})
onUnmounted(()=>{chartInstance?.dispose()})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;
.dashboard{position:relative;height:calc(100vh - $header-height - 2px);overflow:hidden;background:#0a140a}
.bg-grid{position:absolute;inset:0;z-index:0;background-image:linear-gradient(rgba(92,184,92,0.02) 1px,transparent 1px),linear-gradient(90deg,rgba(92,184,92,0.02) 1px,transparent 1px);background-size:40px 40px;mask-image:radial-gradient(ellipse at 50% 40%,black 30%,transparent 70%);-webkit-mask-image:radial-gradient(ellipse at 50% 40%,black 30%,transparent 70%)}
.bg-terrain{position:absolute;inset:0;z-index:0;background:radial-gradient(ellipse at 30% 60%,rgba(61,139,64,0.04) 0%,transparent 50%),radial-gradient(ellipse at 70% 30%,rgba(61,139,64,0.02) 0%,transparent 50%),radial-gradient(ellipse at 50% 80%,rgba(255,183,77,0.02) 0%,transparent 50%)}
.bg-glow{position:absolute;border-radius:50%;filter:blur(120px);pointer-events:none;z-index:0}
.bg-glow.g1{width:600px;height:600px;top:-15%;right:-10%;background:rgba(61,139,64,0.04);animation:gd1 14s ease-in-out infinite alternate}
.bg-glow.g2{width:400px;height:400px;bottom:-10%;left:30%;background:rgba(255,183,77,0.03);animation:gd2 12s ease-in-out infinite alternate}
@keyframes gd1{0%{transform:translate(0,0)}100%{transform:translate(-50px,40px)}}
@keyframes gd2{0%{transform:translate(0,0)}100%{transform:translate(30px,-40px)}}
.topo-lines{position:absolute;inset:0;z-index:1;width:100%;height:100%;pointer-events:none}
.topo-lines line{stroke:rgba(92,184,92,0.03);stroke-width:0.5;animation:linePulse 4s ease-in-out infinite;animation-delay:var(--d)}
.topo-lines circle{fill:rgba(92,184,92,0.04);animation:nodePulse 3s ease-in-out infinite;animation-delay:var(--d)}
@keyframes linePulse{0%,100%{opacity:0.3}50%{opacity:0.6}}
@keyframes nodePulse{0%,100%{opacity:0.4;r:3}50%{opacity:0.8;r:4}}
.top-bar{position:absolute;top:0;left:0;right:0;z-index:100;display:flex;justify-content:space-between;align-items:center;padding:10px 20px;background:rgba(10,20,10,0.6);backdrop-filter:blur(16px);border-bottom:1px solid rgba(92,184,92,0.04);animation:barDown 0.5s ease}
@keyframes barDown{from{opacity:0;transform:translateY(-12px)}to{opacity:1;transform:translateY(0)}}
.tb-left{display:flex;align-items:center;gap:10px}
.tb-logo{width:28px;height:28px;background:#5CB85C;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:700;color:#0a140a}
.tb-divider{width:1px;height:14px;background:rgba(255,255,255,0.04)}
.tb-title{font-size:13px;color:rgba(255,255,255,0.3);font-weight:500;letter-spacing:1px}
.tb-status{display:flex;align-items:center;gap:5px;font-size:11px;color:rgba(255,255,255,0.12)}
.tb-dot{width:5px;height:5px;border-radius:50%;background:#5CB85C;animation:tbPulse 2s ease-in-out infinite}
@keyframes tbPulse{0%,100%{opacity:1;transform:scale(1)}50%{opacity:0.3;transform:scale(0.7)}}
.tb-right{display:flex;align-items:center;gap:10px;font-size:11px;color:rgba(255,255,255,0.1);font-variant-numeric:tabular-nums}
.tb-time{letter-spacing:1px}
.map-area{position:absolute;inset:48px 0 0;z-index:2}
.data-panel{position:absolute;top:60px;left:16px;z-index:50;width:220px;background:rgba(10,20,10,0.55);backdrop-filter:blur(20px);-webkit-backdrop-filter:blur(20px);border-radius:10px;border:1px solid rgba(92,184,92,0.04);padding:14px;display:flex;flex-direction:column;gap:14px;animation:panelIn 0.6s ease}
@keyframes panelIn{from{opacity:0;transform:translateX(-16px)}to{opacity:1;transform:translateX(0)}}
.dp-section{display:flex;flex-direction:column;gap:6px}
.dp-label{font-size:10px;color:rgba(255,255,255,0.1);letter-spacing:2px;text-transform:uppercase}
.dp-row{display:flex;justify-content:space-between;align-items:center}
.dp-tag{font-size:8px;padding:1px 6px;border-radius:4px;background:rgba(92,184,92,0.06);color:rgba(92,184,92,0.2)}
.dp-link{font-size:9px;color:rgba(255,255,255,0.08);cursor:pointer;&:hover{color:rgba(255,255,255,0.2)}}
.orchard-chips{display:flex;gap:4px}
.orchard-chips button{flex:1;padding:5px 8px;border-radius:5px;border:none;background:rgba(255,255,255,0.02);color:rgba(255,255,255,0.1);font-size:10px;cursor:pointer;transition:all 0.2s;display:flex;align-items:center;gap:4px;&:hover{background:rgba(255,255,255,0.04)}&.active{background:rgba(92,184,92,0.06);color:rgba(92,184,92,0.3)}}
.oc-dot{width:4px;height:4px;border-radius:50%;background:rgba(255,255,255,0.04)}
.orchard-chips button.active .oc-dot{background:#5CB85C;box-shadow:0 0 6px rgba(92,184,92,0.15)}
.metric-grid{display:grid;grid-template-columns:1fr 1fr;gap:4px}
.metric-card{padding:8px;border-radius:6px;background:rgba(255,255,255,0.015);border:1px solid rgba(255,255,255,0.015);position:relative;overflow:hidden;transition:all 0.2s;opacity:0;animation:mcIn 0.3s ease forwards;animation-delay:var(--d);&:hover{background:rgba(255,255,255,0.025)}}
@keyframes mcIn{from{opacity:0;transform:translateY(4px)}to{opacity:1;transform:translateY(0)}}
.mc-value{font-size:18px;font-weight:700;line-height:1;margin-bottom:2px}
.mc-label{font-size:9px;color:rgba(255,255,255,0.08)}
.chart-box{width:100%;height:72px}
.chart-legend{display:flex;flex-wrap:wrap;gap:3px;margin-top:2px}
.cl-item{display:flex;align-items:center;gap:3px;padding:1px 4px;border-radius:3px;cursor:pointer;transition:background 0.15s;&:hover{background:rgba(255,255,255,0.02)}}
.cl-dot{width:4px;height:4px;border-radius:50%;flex-shrink:0}
.cl-n{font-size:8px;color:rgba(255,255,255,0.08)}
.skel-chart{display:flex;align-items:center;gap:10px;padding:4px 0}
.skel-ring{width:36px;height:36px;border-radius:50%;background:conic-gradient(rgba(255,255,255,0.02) 0deg,rgba(92,184,92,0.02) 120deg,rgba(255,255,255,0.02) 240deg,rgba(255,255,255,0.02) 360deg);animation:skR 2s ease-in-out infinite;flex-shrink:0}
@keyframes skR{0%{transform:rotate(0deg);opacity:0.4}50%{opacity:0.7}100%{transform:rotate(360deg);opacity:0.4}}
.skel-bars{flex:1;display:flex;flex-direction:column;gap:4px}
.skel-bar{height:6px;border-radius:3px;background:rgba(255,255,255,0.02);overflow:hidden}
.skel-fill{height:100%;border-radius:3px;background:rgba(92,184,92,0.04);animation:skFill 1.5s ease-in-out infinite}
@keyframes skFill{0%,100%{width:30%}50%{width:70%}}
.alert-list{display:flex;flex-direction:column;gap:3px}
.alert-item{display:flex;align-items:center;gap:6px;padding:4px 6px;border-radius:4px;opacity:0;animation:alIn 0.25s ease forwards;animation-delay:calc(var(--i)*0.04s);&.seen{opacity:0.3}&:hover{background:rgba(255,255,255,0.015)}}
@keyframes alIn{from{opacity:0;transform:translateX(-4px)}to{opacity:1;transform:translateX(0)}}
.ai-badge{width:3px;height:12px;border-radius:2px;flex-shrink:0}
.ai-title{font-size:10px;color:rgba(255,255,255,0.1);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.empty{font-size:9px;color:rgba(255,255,255,0.04);text-align:center;padding:8px 0}
.right-panel{position:absolute;top:60px;right:12px;bottom:12px;z-index:50;width:260px;background:rgba(10,20,10,0.45);backdrop-filter:blur(24px);-webkit-backdrop-filter:blur(24px);border-radius:10px;border:1px solid rgba(92,184,92,0.03);display:flex;flex-direction:column;overflow:hidden;transition:width 0.35s cubic-bezier(0.4,0,0.2,1);animation:panelRight 0.6s ease}
@keyframes panelRight{from{opacity:0;transform:translateX(16px)}to{opacity:1;transform:translateX(0)}}
.right-panel.folded{width:36px;background:rgba(10,20,10,0.2);cursor:pointer;.panel-scroll{opacity:0;pointer-events:none}}
.panel-toggle{position:absolute;left:-22px;top:50%;transform:translateY(-50%);z-index:10;width:18px;height:32px;background:rgba(10,20,10,0.3);backdrop-filter:blur(8px);border:1px solid rgba(92,184,92,0.03);border-right:none;border-radius:4px 0 0 4px;color:rgba(255,255,255,0.06);font-size:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;transition:all 0.2s;&:hover{background:rgba(10,20,10,0.5);color:rgba(255,255,255,0.12)}}
.panel-scroll{flex:1;overflow-y:auto;padding:8px;display:flex;flex-direction:column;gap:6px;transition:opacity 0.3s;&::-webkit-scrollbar{width:2px}&::-webkit-scrollbar-thumb{background:rgba(255,255,255,0.02);border-radius:1px}}
.pcard{background:rgba(255,255,255,0.02);border-radius:8px;border:1px solid rgba(255,255,255,0.015);overflow:hidden;transition:all 0.2s;&:hover{background:rgba(255,255,255,0.03);border-color:rgba(255,255,255,0.025)}&.animated{opacity:0;animation:pcUp 0.4s ease forwards;animation-delay:var(--o)}}
@keyframes pcUp{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:translateY(0)}}
.pc-head{display:flex;justify-content:space-between;align-items:center;padding:8px 10px 2px}
.pc-t{font-size:10px;font-weight:600;color:rgba(255,255,255,0.15);letter-spacing:0.5px}
.pc-body{padding:4px 10px 8px}
.weather{background:linear-gradient(135deg,rgba(15,43,15,0.5),rgba(26,61,26,0.3));border-color:rgba(255,255,255,0.02)}
.w-bg{position:absolute;inset:0;background:radial-gradient(ellipse at 70% 10%,rgba(255,255,255,0.02) 0%,transparent 50%);animation:wDrift 8s ease-in-out infinite alternate}
@keyframes wDrift{0%{transform:scale(1) translate(0,0)}100%{transform:scale(1.05) translate(-6px,3px)}}
.w-body{position:relative;z-index:1;padding:10px}
.w-temp{font-size:26px;font-weight:200;color:#fff;line-height:1;margin-bottom:6px}
.w-deg{font-size:11px;opacity:0.3;font-weight:300}
.w-info{display:flex;align-items:center;gap:6px;font-size:10px;color:rgba(255,255,255,0.15)}
.w-dot{width:2px;height:2px;border-radius:50%;background:rgba(255,255,255,0.05)}
.doc-badge{:deep(.el-badge__content){font-size:7px;background:#EF5350;border:none;height:13px;line-height:13px;min-width:13px;padding:0 3px}}
.doc-item{display:flex;align-items:center;gap:6px;padding:4px;border-radius:4px;transition:background 0.15s;&:hover{background:rgba(255,255,255,0.01)}}
.di-icon{width:22px;height:22px;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:9px;font-weight:700;flex-shrink:0}
.di-body{flex:1;min-width:0}
.di-t{font-size:9px;font-weight:500;color:rgba(255,255,255,0.08);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.di-m{font-size:8px;color:rgba(255,255,255,0.04)}
.cal-phen{font-size:12px;font-weight:600;color:rgba(255,255,255,0.1);display:flex;align-items:baseline;gap:4px;margin-bottom:4px}
.cal-term{font-size:8px;color:rgba(92,184,92,0.15);background:rgba(92,184,92,0.03);padding:1px 4px;border-radius:2px}
.cal-adv{font-size:9px;color:rgba(255,255,255,0.06);line-height:1.4;padding:4px 6px;background:rgba(255,255,255,0.01);border-radius:3px;border-left:1px solid rgba(92,184,92,0.03)}
</style>
