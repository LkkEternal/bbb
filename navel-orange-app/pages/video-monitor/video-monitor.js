/**
 * video-monitor.js - 视频监控 + 云台控制页面逻辑
 * 
 * 功能：
 * 1. MJPEG 视频流播放（通过 <image> 标签）
 * 2. 视频地址栏（始终可见，可编辑 IP:Port）
 * 3. 云台控制（pitch/roll/yaw 数字输入框 + 快捷定位）
 * 4. 命令下发日志
 * 
 * 后端依赖（均已实现，无需改动）：
 * - GET  /api/config/video          → 获取视频服务器配置
 * - POST /api/devices/{id}/command   → 下发云台控制命令
 */

const app = getApp();

Page({
  data: {
    // 设备信息
    deviceId: null,
    orchardId: null,

    // 视频配置
    streamHost: '192.168.137.112',
    httpPort: 8080,
    editableHost: '192.168.137.112',
    editablePort: 8080,

    // 视频播放状态
    mjpegUrl: '',
    streamActive: false,
    statusText: '未连接',
    statusClass: 'gray',

    // 云台控制参数
    ptzParams: {
      pitch: 0,
      roll: 0,
      yaw: 0
    },
    sending: false,

    // 命令日志
    cmdLogs: []
  },

  /**
   * 页面加载 — 接收设备ID和果园ID
   */
  onLoad(options) {
    const deviceId = Number(options.deviceId);
    const orchardId = Number(options.orchardId);
    this.setData({ deviceId, orchardId });
    this.loadVideoConfig();
  },

  /**
   * 页面卸载 — 断开视频连接
   */
  onUnload() {
    this.doDisconnect();
  },

  // ================================================================
  //  视频配置 — 地址栏
  // ================================================================

  /**
   * 从后端获取视频流服务器配置
   */
  loadVideoConfig() {
    const accessToken = app.globalData.accessToken;
    if (!accessToken) return;

    wx.request({
      url: `${app.globalData.baseUrl}/api/config/video`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${accessToken}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          this.setData({
            streamHost: data.host || '192.168.137.112',
            httpPort: data.httpPort || 8080,
            editableHost: data.host || '192.168.137.112',
            editablePort: data.httpPort || 8080
          });
        }
      },
      fail: () => {
        console.log('获取视频配置失败，使用默认值');
      }
    });
  },

  /**
   * IP地址输入
   */
  onHostInput(e) {
    this.setData({ editableHost: e.detail.value });
  },

  /**
   * 端口输入
   */
  onPortInput(e) {
    this.setData({ editablePort: Number(e.detail.value) || 8080 });
  },

  /**
   * 应用新的地址配置
   */
  applyAddress() {
    const { editableHost, editablePort } = this.data;
    this.setData({
      streamHost: editableHost,
      httpPort: editablePort
    });
    wx.showToast({ title: `已设置为 ${editableHost}:${editablePort}`, icon: 'none' });

    // 如果当前已连接，自动重连
    if (this.data.streamActive) {
      this.doDisconnect();
      setTimeout(() => this.doConnect(), 500);
    }
  },

  // ================================================================
  //  视频连接控制
  // ================================================================

  /**
   * 切换连接/断开
   */
  toggleConnect() {
    if (this.data.streamActive) {
      this.doDisconnect();
    } else {
      this.doConnect();
    }
  },

  /**
   * 连接 MJPEG 视频流
   */
  doConnect() {
    if (this.data.streamActive) return;

    const { streamHost, httpPort } = this.data;
    const mjpegUrl = `http://${streamHost}:${httpPort}/`;

    this.setData({
      mjpegUrl: mjpegUrl,
      streamActive: true,
      statusText: '连接中...',
      statusClass: 'yellow'
    });
    this.addLog(`连接: ${mjpegUrl}`);
  },

  /**
   * 断开视频连接
   */
  doDisconnect() {
    this.setData({
      mjpegUrl: '',
      streamActive: false,
      statusText: '已断开',
      statusClass: 'gray'
    });
    this.addLog('已断开');
  },

  /**
   * MJPEG 图片加载成功
   */
  onMjpegLoad() {
    this.setData({
      statusText: '播放中',
      statusClass: 'green'
    });
    this.addLog('视频流已连接 ✅');
  },

  /**
   * MJPEG 图片加载失败
   */
  onMjpegError() {
    this.setData({
      statusText: '连接失败',
      statusClass: 'red',
      streamActive: false
    });
    this.addLog('视频连接失败 ❌', false);
    wx.showToast({ title: '无法连接到视频服务器', icon: 'none' });
  },

  // ================================================================
  //  云台控制 (PTZ) — 数字输入框版
  // ================================================================

  /**
   * pitch 输入变化 — 限制范围 -90~90
   */
  onPitchInput(e) {
    const raw = e.detail.value;
    // 允许输入负号前缀和空值
    if (raw === '' || raw === '-') {
      this.setData({ 'ptzParams.pitch': raw });
      return;
    }
    let val = Number(raw);
    if (isNaN(val)) return;
    if (val > 90) val = 90;
    if (val < -90) val = -90;
    this.setData({ 'ptzParams.pitch': val });
  },

  onPitchDec() {
    let val = typeof this.data.ptzParams.pitch === 'number' ? this.data.ptzParams.pitch : 0;
    val = val - 1;
    if (val < -90) val = -90;
    this.setData({ 'ptzParams.pitch': val });
  },

  /** 失焦时若为空则置0 */
  onPitchBlur() {
    const v = this.data.ptzParams.pitch;
    if (v === '' || v === '-' || isNaN(Number(v))) {
      this.setData({ 'ptzParams.pitch': 0 });
    }
  },

  onPitchInc() {
    let val = typeof this.data.ptzParams.pitch === 'number' ? this.data.ptzParams.pitch : 0;
    val = val + 1;
    if (val > 90) val = 90;
    this.setData({ 'ptzParams.pitch': val });
  },

  /**
   * roll 输入变化 — 限制范围 -90~90
   */
  onRollInput(e) {
    const raw = e.detail.value;
    if (raw === '' || raw === '-') {
      this.setData({ 'ptzParams.roll': raw });
      return;
    }
    let val = Number(raw);
    if (isNaN(val)) return;
    if (val > 90) val = 90;
    if (val < -90) val = -90;
    this.setData({ 'ptzParams.roll': val });
  },

  onRollDec() {
    let val = typeof this.data.ptzParams.roll === 'number' ? this.data.ptzParams.roll : 0;
    val = val - 1;
    if (val < -90) val = -90;
    this.setData({ 'ptzParams.roll': val });
  },

  /** 失焦时若为空则置0 */
  onRollBlur() {
    const v = this.data.ptzParams.roll;
    if (v === '' || v === '-' || isNaN(Number(v))) {
      this.setData({ 'ptzParams.roll': 0 });
    }
  },

  onRollInc() {
    let val = typeof this.data.ptzParams.roll === 'number' ? this.data.ptzParams.roll : 0;
    val = val + 1;
    if (val > 90) val = 90;
    this.setData({ 'ptzParams.roll': val });
  },

  /**
   * yaw 输入变化 — 限制范围 -180~180
   */
  onYawInput(e) {
    const raw = e.detail.value;
    if (raw === '' || raw === '-') {
      this.setData({ 'ptzParams.yaw': raw });
      return;
    }
    let val = Number(raw);
    if (isNaN(val)) return;
    if (val > 180) val = 180;
    if (val < -180) val = -180;
    this.setData({ 'ptzParams.yaw': val });
  },

  onYawDec() {
    let val = typeof this.data.ptzParams.yaw === 'number' ? this.data.ptzParams.yaw : 0;
    val = val - 1;
    if (val < -180) val = -180;
    this.setData({ 'ptzParams.yaw': val });
  },

  /** 失焦时若为空则置0 */
  onYawBlur() {
    const v = this.data.ptzParams.yaw;
    if (v === '' || v === '-' || isNaN(Number(v))) {
      this.setData({ 'ptzParams.yaw': 0 });
    }
  },

  onYawInc() {
    let val = typeof this.data.ptzParams.yaw === 'number' ? this.data.ptzParams.yaw : 0;
    val = val + 1;
    if (val > 180) val = 180;
    this.setData({ 'ptzParams.yaw': val });
  },

  /**
   * 下发云台控制命令前，确保值为数字
   */
  normalizePtzValue(v) {
    const n = Number(v);
    return isNaN(n) ? 0 : n;
  },

  /**
   * 下发云台控制命令
   * 调用后端 POST /api/devices/{id}/command
   */
  sendPtzCommand() {
    const { deviceId, ptzParams } = this.data;
    if (!deviceId) {
      wx.showToast({ title: '设备ID为空', icon: 'none' });
      return;
    }

    this.setData({ sending: true });

    const data = {
      commandName: 'control_ptz',
      paras: {
        pitch: this.normalizePtzValue(ptzParams.pitch),
        roll: this.normalizePtzValue(ptzParams.roll),
        yaw: this.normalizePtzValue(ptzParams.yaw)
      }
    };

    this.addLog(`发送 control_ptz: pitch=${data.paras.pitch} roll=${data.paras.roll} yaw=${data.paras.yaw}`);

    const accessToken = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/devices/${deviceId}/command`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      data: data,
      success: (res) => {
        if (res.data && res.data.code === 200) {
          const result = res.data.data;
          this.addLog(`命令下发成功: commandId=${result.commandId} resultCode=${result.resultCode}`);
          wx.showToast({ title: '命令已下发', icon: 'success' });
        } else {
          const msg = (res.data && res.data.message) || '命令下发失败';
          this.addLog(msg, false);
          wx.showToast({ title: msg, icon: 'error' });
        }
      },
      fail: (err) => {
        this.addLog(`网络错误: ${err.errMsg}`, false);
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      },
      complete: () => {
        this.setData({ sending: false });
      }
    });
  },

  /**
   * 快捷定位 — 设置预设角度并立即下发
   */
  quickPtz(e) {
    const dataset = e.currentTarget.dataset;
    this.setData({
      'ptzParams.pitch': Number(dataset.pitch),
      'ptzParams.roll': Number(dataset.roll),
      'ptzParams.yaw': Number(dataset.yaw)
    });
    this.sendPtzCommand();
  },

  // ================================================================
  //  日志工具
  // ================================================================

  /**
   * 添加命令日志
   * @param {string} msg  日志内容
   * @param {boolean} ok  是否成功
   */
  addLog(msg, ok = true) {
    const now = new Date();
    const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    const logs = [{ time, msg, ok }].concat(this.data.cmdLogs);
    if (logs.length > 50) logs.pop();
    this.setData({ cmdLogs: logs });
  },

  /**
   * 清空日志
   */
  clearLogs() {
    this.setData({ cmdLogs: [] });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  }
});
