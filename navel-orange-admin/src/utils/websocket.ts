/**
 * WebSocket 连接管理 — 检测数据实时推送
 *
 * - 自动连接 /ws/detections
 * - 收到消息后通过回调通知业务组件
 * - 自动重连（指数退避）
 */

type MessageCallback = (type: string, data: any) => void

class DetectionWebSocketClient {
  private ws: WebSocket | null = null
  private url: string
  private callbacks: MessageCallback[] = []
  private reconnectAttempts = 0
  private maxReconnectAttempts = 10
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private pingTimer: ReturnType<typeof setInterval> | null = null
  private destroyed = false

  constructor() {
    // 构建 WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    this.url = `${protocol}//${host}/ws/detections`
  }

  /** 建立连接 */
  connect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }

    try {
      this.ws = new WebSocket(this.url)
    } catch (e) {
      console.warn('[WS] 创建连接失败:', e)
      this.scheduleReconnect()
      return
    }

    this.ws.onopen = () => {
      console.log('[WS] 已连接')
      this.reconnectAttempts = 0
      // 启动心跳 ping（每 30 秒）
      this.startPing()
      // 通知业务组件连接状态
      this.notifyListeners('__connection', { status: 'connected' })
    }

    this.ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        if (msg.type === 'pong') return // 心跳响应，忽略
        this.notifyListeners(msg.type, msg.data)
      } catch {
        // 非 JSON 消息忽略
      }
    }

    this.ws.onclose = (event) => {
      console.log('[WS] 已断开, code:', event.code)
      this.stopPing()
      this.notifyListeners('__connection', { status: 'disconnected' })
      if (!this.destroyed) {
        this.scheduleReconnect()
      }
    }

    this.ws.onerror = () => {
      console.warn('[WS] 连接错误')
    }
  }

  /** 断开连接 */
  disconnect() {
    this.destroyed = true
    this.stopPing()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.onclose = null // 阻止触发重连
      this.ws.close()
      this.ws = null
    }
  }

  /** 注册消息回调 */
  onMessage(callback: MessageCallback) {
    this.callbacks.push(callback)
    return () => {
      this.callbacks = this.callbacks.filter(cb => cb !== callback)
    }
  }

  /** 向所有监听器分发消息 */
  private notifyListeners(type: string, data: any) {
    this.callbacks.forEach(cb => {
      try { cb(type, data) } catch (e) { console.error('[WS] callback error:', e) }
    })
  }

  /** 心跳 ping */
  private startPing() {
    this.stopPing()
    this.pingTimer = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send('ping')
      }
    }, 30000)
  }

  private stopPing() {
    if (this.pingTimer) {
      clearInterval(this.pingTimer)
      this.pingTimer = null
    }
  }

  /** 指数退避重连 */
  private scheduleReconnect() {
    if (this.destroyed || this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('[WS] 重连次数已达上限，停止重连')
      return
    }
    // 指数退避：1s, 2s, 4s, 8s ... 最大 30s
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000)
    this.reconnectAttempts++
    console.log(`[WS] ${delay}ms 后重连 (第${this.reconnectAttempts}次)...`)
    this.reconnectTimer = setTimeout(() => this.connect(), delay)
  }
}

/** 单例 */
let instance: DetectionWebSocketClient | null = null

export function getDetectionWsClient(): DetectionWebSocketClient {
  if (!instance) {
    instance = new DetectionWebSocketClient()
  }
  return instance
}
