<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" content="视频监控" style="margin-bottom: 20px" />

    <el-row :gutter="20">
      <!-- 左侧：视频流 -->
      <el-col :xs="24" :lg="18">
        <el-card shadow="hover" class="video-card">
          <div class="video-box" id="videoBox">
            <div class="placeholder" v-if="!streamActive" @click="doConnect">
              <div class="icon">📹</div>
              <div class="text">点击连接视频流</div>
              <div class="sub-text">{{ streamHost }}:{{ wsPort }}</div>
            </div>
            <video ref="videoRef" autoplay playsinline muted
                   :style="{ display: streamActive ? 'block' : 'none' }"
                   @click="toggleFullscreen"></video>
          </div>

          <div class="status-row">
            <span class="dot" :class="statusClass"></span>
            <span>{{ statusText }}</span>
            <el-button size="small" :type="streamActive ? 'danger' : 'primary'"
                       @click="streamActive ? doDisconnect() : doConnect()"
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
              <el-slider v-model="ptzParams.yaw" :min="-90" :max="90" :step="1" show-input size="small" />
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

// ── WebRTC 配置（从后端获取） ─────────────
const streamHost = ref('localhost')
const wsPort = ref(8001)
const httpPort = ref(8000)
const configLoaded = ref(false)

async function loadVideoConfig() {
  try {
    const data: any = await getVideoConfig()
    streamHost.value = data?.host || 'localhost'
    wsPort.value = data?.wsPort || 8001
    httpPort.value = data?.httpPort || 8000
    configLoaded.value = true
  } catch {
    configLoaded.value = true
  }
}

const videoRef = ref<HTMLVideoElement>()
const streamActive = ref(false)
const statusText = ref('未连接')
const statusClass = ref('gray')

let websocket: WebSocket | null = null
let pc: RTCPeerConnection | null = null

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

function doConnect() {
  if (streamActive.value) return
  if (!configLoaded.value) {
    ElMessage.warning('视频配置加载中，请稍后')
    return
  }
  const clientId = randomId(10)
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
    if (streamActive.value) doDisconnect()
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
      doDisconnect()
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

function doDisconnect() {
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
  setTimeout(doConnect, 500)
})

onUnmounted(() => {
  doDisconnect()
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
