<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" content="视频监控" style="margin-bottom: 20px" />

    <!-- 视频地址配置栏 -->
    <el-card shadow="hover" style="margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <span style="font-size:13px;color:#666;white-space:nowrap">视频地址：</span>
        <span style="font-size:13px">http://</span>
        <el-input v-model="editableHost" placeholder="IP地址" style="width:160px" size="small" />
        <span style="font-size:13px">:</span>
        <el-input-number v-model="editablePort" :min="1" :max="65535" style="width:120px" size="small" controls-position="right" />
        <span style="font-size:13px">/</span>
        <el-button size="small" type="primary" @click="applyAddress">应用</el-button>
        <el-button size="small" @click="resetAddress">重置</el-button>
        <el-button size="small" @click="useProxy">使用代理</el-button>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 左侧：视频流 -->
      <el-col :xs="24" :lg="18">
        <el-card shadow="hover" class="video-card">
          <div class="video-box" id="videoBox">
            <div class="placeholder" v-if="!streamActive">
              <div class="icon">📹</div>
              <div class="text">点击播放视频流</div>
              <div class="sub-text">http://{{ streamHost }}:{{ httpPort }}/</div>
            </div>
            <img ref="imgRef" v-show="streamActive"
                 :src="blobUrl || httpUrl"
                 style="width:100%"
                 referrerpolicy="no-referrer"
                 crossorigin="anonymous"
                 @error="onImageError"
                 @load="onImageLoad" />
            <video ref="videoRef" v-show="streamActive"
                   autoplay playsinline muted
                   style="width:100%"></video>
            <img ref="imgRef" v-show="false"
                 :src="blobUrl || httpUrl"
                 style="width:100%;display:none"
                 referrerpolicy="no-referrer"
                 crossorigin="anonymous"
                 @error="onImageError"
                 @load="onImageLoad" />
          </div>

          <div class="status-row">
            <span class="dot" :class="statusClass"></span>
            <span>{{ statusText }}</span>
            <el-button size="small" :type="streamActive ? 'danger' : 'primary'"
                       @click="streamActive ? doWsDisconnect() : doConnect()"
                       style="margin-left:auto">
              {{ streamActive ? '断开' : '连接' }}
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：云台控制 -->
      <el-col :xs="24" :lg="6">
        <el-card shadow="hover">
          <template #header>
            <span>🎮 云台控制</span>
          </template>

          <div class="ptz-panel">
            <div class="param-group">
              <label>pitch（俯仰）</label>
              <el-slider v-model="ptzParams.pitch" :min="-90" :max="90" :step="1" show-input size="small" />
            </div>
            <div class="param-group">
              <label>roll（横滚）</label>
              <el-slider v-model="ptzParams.roll" :min="-90" :max="90" :step="1" show-input size="small" />
            </div>
            <div class="param-group">
              <label>yaw（旋转）</label>
              <el-slider v-model="ptzParams.yaw" :min="-180" :max="180" :step="1" show-input size="small" />
            </div>

            <div class="ptz-buttons">
              <el-button type="primary" @click="sendPtzCommand" :loading="sending" style="width:100%">
                📡 发送控制
              </el-button>
            </div>

            <el-divider />

            <div class="quick-buttons">
              <div class="quick-label">快捷定位</div>
              <el-button size="small" @click="quickPtz(0, 0, 0)">回中</el-button>
              <el-button size="small" @click="quickPtz(0, 0, 45)">右45°</el-button>
              <el-button size="small" @click="quickPtz(0, 0, -45)">左45°</el-button>
              <el-button size="small" @click="quickPtz(-30, 0, 0)">下30°</el-button>
              <el-button size="small" @click="quickPtz(30, 0, 0)">上30°</el-button>
            </div>
          </div>
        </el-card>

        <!-- 命令日志 -->
        <el-card shadow="hover" style="margin-top:16px">
          <template #header>
            <span>📋 命令日志</span>
          </template>
          <div class="cmd-log">
            <div v-for="(log, i) in cmdLogs" :key="i" class="log-line"
                 :class="{ success: log.ok, error: !log.ok }">
              {{ log.msg }}
            </div>
            <div v-if="cmdLogs.length === 0" class="log-line" style="color:#666">暂无记录</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCommand } from '@/api/modules/orchard'
import { getVideoConfig } from '@/api/modules/video'

const route = useRoute()
const deviceId = Number(route.query.deviceId)

// ── 视频配置（从后端获取 + 可编辑） ──
const httpUrl = ref('')
const streamHost = ref('192.168.137.112')
const httpPort = ref(8080)
const wsPort = ref(8080)
const configLoaded = ref(false)

// 可编辑的地址
const editableHost = ref('192.168.137.112')
const editablePort = ref(8080)

function applyAddress() {
  streamHost.value = editableHost.value
  httpPort.value = editablePort.value
  wsPort.value = editablePort.value
  ElMessage.success(`已设置为 ${streamHost.value}:${httpPort.value}`)
}

function resetAddress() {
  loadVideoConfig()
}

function useProxy() {
  editableHost.value = '115.29.228.222'
  editablePort.value = 80
  applyAddress()
  ElMessage.info('使用代理地址(点击播放后生效)')
}

async function loadVideoConfig() {
  try {
    const data: any = await getVideoConfig()
    streamHost.value = data?.host || '192.168.137.112'
    httpPort.value = data?.httpPort || 8080
    editableHost.value = streamHost.value
    editablePort.value = httpPort.value
    configLoaded.value = true
  } catch {
    configLoaded.value = true
  }
}

const imgRef = ref<HTMLImageElement>()
const videoRef = ref<HTMLVideoElement>()
const streamActive = ref(false)
const statusText = ref('未连接')
const statusClass = ref('gray')

// ── HTTP 视频连接 ─────────────────────
const SRS_WHEP_URL = 'https://115.29.228.222:1986/rtc/v1/whep/?app=live&stream=stream'
let webrtcPC: RTCPeerConnection | null = null

async function doConnect() {
  if (streamActive.value) return
  if (!configLoaded.value) {
    ElMessage.warning('视频配置加载中，请稍后')
    return
  }
  addLog('连接 SRS WebRTC: ' + SRS_WHEP_URL)
  setStatus('连接中...', 'orange')
  streamActive.value = true

  try {
    webrtcPC = new RTCPeerConnection({ iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] })

    webrtcPC.ontrack = (event: RTCTrackEvent) => {
      addLog('接收视频流成功!')
      const v = videoRef.value
      if (v) { v.srcObject = event.streams[0]; v.play() }
      setStatus('播放中', 'green')
    }

    webrtcPC.oniceconnectionstatechange = () => {
      if (webrtcPC?.iceConnectionState === 'disconnected' || webrtcPC?.iceConnectionState === 'failed') {
        addLog('连接断开', false)
        doDisconnect()
      }
    }

    const offer = await webrtcPC.createOffer({ offerToReceiveAudio: false, offerToReceiveVideo: true })
    await webrtcPC.setLocalDescription(offer)

    const resp = await fetch(SRS_WHEP_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/sdp' },
      body: offer.sdp
    })
    if (!resp.ok) throw new Error('SRS error ' + resp.status)
    const answer = await resp.text()
    await webrtcPC.setRemoteDescription({ type: 'answer', sdp: answer })
    addLog('WebRTC 连接成功')
  } catch (e: any) {
    addLog('失败: ' + e.message, false)
    doDisconnect()
  }
}

let fetchController: AbortController | null = null
const blobUrl = ref('')

async function fetchVideo(url: string) {
  fetchController?.abort()
  fetchController = new AbortController()
  try {
    const resp = await fetch(url, { signal: fetchController.signal })
    const blob = await resp.blob()
    if (blobUrl.value) URL.revokeObjectURL(blobUrl.value)
    blobUrl.value = URL.createObjectURL(blob)
    if (streamActive.value) setTimeout(() => fetchVideo(url), 200)
  } catch (e: any) {
    if (e.name !== 'AbortError') addLog(`视频流错误: ${e.message}`, false)
    if (streamActive.value) setTimeout(() => fetchVideo(url), 2000)
  }
}

function onImageLoad() {
  setStatus('播放中', 'green')
}

function onImageError() {
  setStatus('连接失败', 'red')
  streamActive.value = false
  addLog('HTTP 视频流连接失败 ❌', false)
}

function doDisconnect() {
  streamActive.value = false
  if (webrtcPC) { webrtcPC.close(); webrtcPC = null }
  const v = videoRef.value
  if (v) v.srcObject = null
  streamActive.value = false
  fetchController?.abort()
  if (blobUrl.value) URL.revokeObjectURL(blobUrl.value)
  blobUrl.value = ''
  setStatus('已断开', 'gray')
  addLog('已断开')
}

// ── PTZ ──────────────────────────────────
const ptzParams = reactive({ pitch: 0, roll: 0, yaw: 0 })
const sending = ref(false)
const cmdLogs = ref<Array<{ msg: string; ok: boolean }>>([])

function addLog(msg: string, ok = true) {
  cmdLogs.value.unshift({ msg, ok })
  if (cmdLogs.value.length > 50) cmdLogs.value.pop()
}

// ── WebRTC 连接 ─────────────────────────
function randomId(len: number) {
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
  return Array.from({ length: len }, () => chars[Math.floor(Math.random() * chars.length)]).join('')
}

function setStatus(text: string, cls: string) {
  statusText.value = text
  statusClass.value = cls
}

function doWsConnect() {
  const wsUrl = `ws://${streamHost.value}:${wsPort.value}/${clientId}`

  addLog(`WebSocket: ${wsUrl}`)
  setStatus('连接中 ...', 'yellow')

  websocket = new WebSocket(wsUrl)

  websocket.onopen = () => {
    addLog('WebSocket 已连接 ✅')
    setStatus('等待视频流 ...', 'yellow')
    websocket!.send(JSON.stringify({ id: 'server', type: 'request' }))
  }

  websocket.onmessage = async (evt: MessageEvent) => {
    if (typeof evt.data !== 'string') return
    const msg = JSON.parse(evt.data)
    if (msg.type === 'offer') {
      addLog('收到 SDP Offer，建立 WebRTC ...')
      await handleOffer(msg)
    }
  }

  websocket.onerror = () => {
    addLog('WebSocket 连接失败 ❌', false)
    setStatus('连接失败', 'red')
  }

  websocket.onclose = () => {
    if (streamActive.value) doWsDisconnect()
    else setStatus('未连接', 'gray')
  }
}

async function handleOffer(offer: RTCSessionDescriptionInit) {
  const config: RTCConfiguration = {
    bundlePolicy: 'max-bundle',
    iceServers: [{ urls: ['stun:stun.l.google.com:19302'] }],
  }
  pc = new RTCPeerConnection(config)

  pc.ontrack = (evt) => {
    addLog('收到视频流 ✅')
    setStatus('播放中', 'green')
    streamActive.value = true
    if (videoRef.value) {
      videoRef.value.srcObject = evt.streams[0]
      videoRef.value.play()
    }
  }

  pc.oniceconnectionstatechange = () => {
    if (pc?.iceConnectionState === 'disconnected' || pc?.iceConnectionState === 'failed') {
      doWsDisconnect()
    }
  }

  await pc.setRemoteDescription(offer)
  const answer = await pc.createAnswer()
  await pc.setLocalDescription(answer)

  await new Promise<void>(resolve => {
    if (pc!.iceGatheringState === 'complete') resolve()
    else pc!.addEventListener('icegatheringstatechange', () => {
      if (pc!.iceGatheringState === 'complete') resolve()
    })
  })

  websocket?.send(JSON.stringify({
    id: 'server',
    type: answer.type,
    sdp: answer.sdp,
  }))
  addLog('SDP Answer 已发送')
}

function doWsDisconnect() {
  streamActive.value = false
  if (pc) { pc.close(); pc = null }
  if (websocket) { websocket.close(); websocket = null }
  if (videoRef.value) videoRef.value.srcObject = null
  setStatus('已断开', 'gray')
  addLog('已断开')
}

function toggleFullscreen() {
  const el = videoRef.value
  if (!el) return
  if (!document.fullscreenElement) {
    el.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

// ── PTZ 控制 ────────────────────────────
async function sendPtzCommand() {
  sending.value = true
  try {
    const data = {
      commandName: 'control_ptz',
      paras: {
        pitch: ptzParams.pitch,
        roll: ptzParams.roll,
        yaw: ptzParams.yaw,
      },
    }
    addLog(`发送 control_ptz: pitch=${data.paras.pitch} roll=${data.paras.roll} yaw=${data.paras.yaw}`)

    const res: any = await sendCommand(deviceId, data)
    addLog(`命令下发成功: commandId=${res.commandId} resultCode=${res.resultCode}`)
    ElMessage.success('命令已下发')
  } catch (e: any) {
    addLog(`命令下发失败: ${e.message}`, false)
    ElMessage.error('命令下发失败: ' + (e.message || '未知错误'))
  } finally {
    sending.value = false
  }
}

function quickPtz(pitch: number, roll: number, yaw: number) {
  ptzParams.pitch = pitch
  ptzParams.roll = roll
  ptzParams.yaw = yaw
  sendPtzCommand()
}

// ── 生命周期 ─────────────────────────────
onMounted(async () => {
  await loadVideoConfig()
  setTimeout(doWsConnect, 500)
})

onUnmounted(() => {
  doWsDisconnect()
})
</script>

<style scoped>
.video-card { padding: 0; }
.video-box {
  background: #000;
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.video-box video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.placeholder {
  text-align: center;
  color: #556;
  cursor: pointer;
  padding: 40px;
}
.placeholder .icon { font-size: 56px; margin-bottom: 12px; }
.placeholder .text { font-size: 16px; margin-bottom: 6px; }
.placeholder .sub-text { font-size: 12px; color: #445; }

.status-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  font-size: 13px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
.dot.green { background: #22c55e; animation: blink 1.2s infinite; }
.dot.red { background: #ef4444; }
.dot.yellow { background: #eab308; animation: blink 1.2s infinite; }
.dot.gray { background: #555; }
@keyframes blink {
  0%, 100% { opacity: 1; } 50% { opacity: 0.3; }
}

.ptz-panel { padding: 4px 0; }
.param-group { margin-bottom: 16px; }
.param-group label {
  display: block;
  font-size: 12px;
  color: #aaa;
  margin-bottom: 6px;
}

.quick-buttons { display: flex; flex-wrap: wrap; gap: 6px; }
.quick-label {
  width: 100%;
  font-size: 12px;
  color: #aaa;
  margin-bottom: 2px;
}

.cmd-log {
  max-height: 200px;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.8;
}
.log-line { padding: 2px 0; border-bottom: 1px solid #1a1a2e; }
.log-line.success { color: #94a3b8; }
.log-line.error { color: #ef4444; }
</style>
