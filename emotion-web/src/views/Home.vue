i按段<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import * as echarts from 'echarts';
import { useAuthStore } from '@/stores/auth';
import { authAPI, detectionAPI, weatherAPI, alertAPI } from '@/api';
import { useRouter } from 'vue-router';
import * as faceapi from '@vladmandic/face-api';

const router = useRouter();
const authStore = useAuthStore();
const user = computed(() => authStore.user.value);

const selectedFile = ref(null);
const previewImage = ref(null);
const detectionResult = ref(null);
const weatherData = ref(null);
const alertSummary = ref(null);
const recentHistory = ref([]);
const lastDetectAt = ref('');

const loadingStats = ref(true);
const loadingWeather = ref(true);
const loadingHistory = ref(true);

const detectionStats = ref(null);

const chartRef = ref(null);
const fileInputRef = ref(null);
const chartInstance = ref(null);
const showFeedbackModal = ref(false);
const feedbackForm = ref({
  correctLabel: ''
});

const cameraMode = ref(false);
const cameraActive = ref(false);
const autoDetect = ref(false);
const detecting = ref(false);
const cameraStream = ref(null);
const videoRef = ref(null);
const cameraDevices = ref([]);
const selectedDeviceId = ref('');
let autoDetectTimer = null;

const faceTracking = ref(true);
const faceDetectorReady = ref(false);
const faceDetectorError = ref('');
const overlayCanvasRef = ref(null);
let faceTrackingRunning = false;
let faceTrackingRafId = null;
let lastFaceDetectTime = 0;
const FACE_DETECT_INTERVAL = 120;

let faceAges = [];
let ageNetReady = false;
let lastAgeUpdateTime = 0;
const AGE_UPDATE_INTERVAL = 600;
const MIN_AGE_FACE_SIZE = 40;

const emotionLabels = {
  happy: { cn: '开心', emoji: '😊', color: '#22c55e' },
  sad: { cn: '悲伤', emoji: '😢', color: '#3b82f6' },
  angry: { cn: '生气', emoji: '😠', color: '#ef4444' },
  neutral: { cn: '平静', emoji: '😐', color: '#6b7280' },
  fear: { cn: '恐惧', emoji: '😨', color: '#ec4899' },
  surprise: { cn: '惊讶', emoji: '😲', color: '#f59e0b' },
  disgust: { cn: '厌恶', emoji: '🤢', color: '#10b981' },
  unknown: { cn: '未知', emoji: '❓', color: '#9ca3af' }
};

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

const handleDragOver = (e) => {
  e.preventDefault();
};

const handleDrop = (e) => {
  e.preventDefault();
  const file = e.dataTransfer.files[0];
  if (file && file.type.startsWith('image/')) {
    selectedFile.value = file;
    previewImage.value = URL.createObjectURL(file);
  }
};

const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click();
  }
};

const clearImage = () => {
  if (previewImage.value) {
    URL.revokeObjectURL(previewImage.value);
  }
  previewImage.value = null;
  selectedFile.value = null;
  if (fileInputRef.value) {
    fileInputRef.value.value = '';
  }
};

const handleDetect = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片');
    return;
  }
  
  const loading = ElLoading.service({
    lock: true,
    text: '正在检测...',
    background: 'rgba(0, 0, 0, 0.5)'
  });
  
  try {
    const res = await detectionAPI.detect(selectedFile.value);
    detectionResult.value = res.data.data;
    await fetchStats();
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '检测失败');
  } finally {
    loading.close();
  }
};

const switchToUpload = () => {
  if (!cameraMode.value) return;
  cameraMode.value = false;
  stopCamera();
};

const switchToCamera = async () => {
  cameraMode.value = true;
  await nextTick();
  await loadCameraDevices();
};

const loadCameraDevices = async () => {
  try {
    if (!navigator.mediaDevices?.enumerateDevices) return;
    const devices = await navigator.mediaDevices.enumerateDevices();
    cameraDevices.value = devices.filter(d => d.kind === 'videoinput');
    if (!selectedDeviceId.value && cameraDevices.value.length > 0) {
      selectedDeviceId.value = cameraDevices.value[0].deviceId;
    }
  } catch (e) {
    console.error('获取摄像头列表失败:', e);
  }
};

const startCamera = async () => {
  if (cameraActive.value || cameraStream.value) return;
  if (!navigator.mediaDevices?.getUserMedia) {
    ElMessage.error('当前浏览器不支持摄像头访问');
    return;
  }
  try {
    const videoConstraints = { width: 1280, height: 720 };
    if (selectedDeviceId.value) {
      videoConstraints.deviceId = { exact: selectedDeviceId.value };
    }
    cameraStream.value = await navigator.mediaDevices.getUserMedia({
      video: videoConstraints,
      audio: false
    });
    await nextTick();
    if (videoRef.value) {
      videoRef.value.srcObject = cameraStream.value;
      videoRef.value.play().catch(() => {});
    }
    cameraActive.value = true;
    ElMessage.success('摄像头已开启');
    if (faceTracking.value) {
      startFaceTracking();
    }
  } catch (e) {
    console.error('开启摄像头失败:', e);
    let msg = '无法打开摄像头';
    if (e.name === 'NotAllowedError') {
      msg = '摄像头权限被拒绝，请在浏览器地址栏允许访问摄像头';
    } else if (e.name === 'NotFoundError') {
      msg = '未检测到摄像头设备';
    } else if (e.name === 'NotReadableError') {
      msg = '摄像头被其他程序占用，请关闭后重试';
    }
    ElMessage.error(msg);
    if (cameraStream.value) {
      cameraStream.value.getTracks().forEach(t => t.stop());
      cameraStream.value = null;
    }
    cameraActive.value = false;
  }
};

const stopCamera = () => {
  if (cameraStream.value) {
    cameraStream.value.getTracks().forEach(t => t.stop());
    cameraStream.value = null;
  }
  cameraActive.value = false;
  stopAutoDetect();
  stopFaceTracking();
};

const switchCameraDevice = async () => {
  stopCamera();
  await nextTick();
  await startCamera();
};

const captureFrame = () => {
  const video = videoRef.value;
  if (!video || !video.videoWidth || !video.videoHeight) return Promise.resolve(null);
  return new Promise(resolve => {
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    canvas.toBlob(resolve, 'image/jpeg', 0.9);
  });
};

const getMainFaceAgeInfo = () => {
  const canvas = overlayCanvasRef.value;
  const faces = canvas?.faces || [];
  if (!faces.length) return {};
  let mainIdx = 0;
  for (let i = 1; i < faces.length; i++) {
    const a = faces[i].width * faces[i].height;
    const b = faces[mainIdx].width * faces[mainIdx].height;
    if (a > b) mainIdx = i;
  }
  const info = faceAges[mainIdx];
  if (!info) return {};
  return { modelGender: info.genderCn, modelAge: info.age, modelRange: info.ageRange };
};

const runCameraDetect = async (showLoading) => {
  const blob = await captureFrame();
  if (!blob) {
    if (showLoading) ElMessage.warning('摄像头画面尚未就绪，请稍候');
    return false;
  }
  const file = new File([blob], 'camera.jpg', { type: 'image/jpeg' });
  const res = await detectionAPI.detect(file, getMainFaceAgeInfo());
  detectionResult.value = res.data.data;
  const now = new Date();
  lastDetectAt.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
  await fetchStats();
  await fetchRecentHistory();
  return true;
};

const handleCameraDetect = async () => {
  if (!cameraActive.value) {
    ElMessage.warning('请先开启摄像头');
    return;
  }
  if (detecting.value) return;
  detecting.value = true;
  const loading = ElLoading.service({
    lock: true,
    text: '正在检测...',
    background: 'rgba(0, 0, 0, 0.5)'
  });
  try {
    await runCameraDetect(true);
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '检测失败');
  } finally {
    detecting.value = false;
    loading.close();
  }
};

const toggleAutoDetect = () => {
  if (autoDetect.value) {
    startAutoDetect();
  } else {
    stopAutoDetect();
  }
};

const startAutoDetect = () => {
  stopAutoDetect();
  autoDetectTimer = setInterval(async () => {
    if (detecting.value || !cameraActive.value) return;
    detecting.value = true;
    try {
      await runCameraDetect(false);
    } catch (e) {
      console.error('自动检测失败:', e);
    } finally {
      detecting.value = false;
    }
  }, 2500);
};

const stopAutoDetect = () => {
  if (autoDetectTimer) {
    clearInterval(autoDetectTimer);
    autoDetectTimer = null;
  }
};

const initFaceDetector = async () => {
  if (!faceDetectorReady.value) {
    try {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/models');
      faceDetectorReady.value = true;
    } catch (e) {
      console.error('人脸检测模型加载失败:', e);
      faceDetectorError.value = '人脸检测模型加载失败，实时框不可用';
      return;
    }
  }
  if (!ageNetReady) {
    try {
      await faceapi.nets.ageGenderNet.loadFromUri('/models');
      ageNetReady = true;
    } catch (e) {
      console.warn('年龄模型加载失败，仅显示人脸框:', e);
    }
  }
};

const getDisplayRect = (video, container) => {
  const vidW = video.videoWidth;
  const vidH = video.videoHeight;
  const cw = container.clientWidth;
  const ch = container.clientHeight;
  const scale = Math.min(cw / vidW, ch / vidH);
  return {
    offsetX: (cw - vidW * scale) / 2,
    offsetY: (ch - vidH * scale) / 2,
    scale
  };
};

const getAgeRange = (age) => {
  if (age < 12) return '儿童';
  if (age < 20) return '青少年';
  if (age < 40) return '青年';
  if (age < 60) return '中年';
  return '老年';
};

const predictFaceAges = async (video, faces) => {
  const results = [];
  for (const f of faces) {
    if (f.width < MIN_AGE_FACE_SIZE || f.height < MIN_AGE_FACE_SIZE) {
      results.push(null);
      continue;
    }
    try {
      const sx = Math.max(0, Math.round(f.x));
      const sy = Math.max(0, Math.round(f.y));
      const sw = Math.min(video.videoWidth - sx, Math.round(f.width));
      const sh = Math.min(video.videoHeight - sy, Math.round(f.height));
      if (sw < 1 || sh < 1) {
        results.push(null);
        continue;
      }
      const crop = document.createElement('canvas');
      crop.width = sw;
      crop.height = sh;
      const cctx = crop.getContext('2d');
      cctx.drawImage(video, sx, sy, sw, sh, 0, 0, sw, sh);
      const pred = await faceapi.nets.ageGenderNet.predictAgeAndGender(crop);
      const age = Math.round(pred.age);
      results.push({
        age,
        ageRange: getAgeRange(age),
        gender: pred.gender,
        genderCn: pred.gender === 'male' ? '男' : '女'
      });
    } catch (e) {
      console.error('年龄预测失败:', e);
      results.push(null);
    }
  }
  return results;
};

const drawChip = (ctx, text, lx, ly, bgColor, cw) => {
  ctx.font = '12px sans-serif';
  const tw = ctx.measureText(text).width;
  const lh = 22;
  if (lx + tw > cw - 4) lx = Math.max(4, cw - tw - 4);
  ctx.fillStyle = bgColor;
  ctx.fillRect(lx, ly, tw + 8, lh);
  ctx.fillStyle = '#ffffff';
  ctx.fillText(text, lx + 4, ly + 4);
  return { lx, ly, w: tw + 8, h: lh };
};

const drawFaceBoxes = () => {
  const canvas = overlayCanvasRef.value;
  const video = videoRef.value;
  if (!canvas || !video || !canvas.parentElement) return;
  const container = canvas.parentElement;
  const cw = container.clientWidth;
  const ch = container.clientHeight;
  if (canvas.width !== cw || canvas.height !== ch) {
    canvas.width = cw;
    canvas.height = ch;
  }
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, cw, ch);

  const faces = canvas.faces;
  if (!faces || !faces.length || !video.videoWidth) return;

  const rect = getDisplayRect(video, container);
  ctx.lineWidth = 3;
  ctx.strokeStyle = '#22c55e';
  ctx.fillStyle = 'rgba(34, 197, 94, 0.15)';
  ctx.font = '13px sans-serif';
  ctx.textBaseline = 'top';

  for (let i = 0; i < faces.length; i++) {
    const f = faces[i];
    const x = f.x * rect.scale + rect.offsetX;
    const y = f.y * rect.scale + rect.offsetY;
    const w = f.width * rect.scale;
    const h = f.height * rect.scale;
    ctx.beginPath();
    ctx.rect(x, y, w, h);
    ctx.fill();
    ctx.stroke();

    ctx.fillStyle = '#059669';
    ctx.fillText(`${Math.round(f.score * 100)}%`, x + 4, y + 4);
    ctx.fillStyle = 'rgba(34, 197, 94, 0.15)';

    let chipY = y - 26;
    if (chipY < 4) chipY = y + h + 4;
    const ageInfo = faceAges[i];
    if (ageInfo) {
      drawChip(
        ctx,
        `${ageInfo.genderCn} · ${ageInfo.age}岁 · ${ageInfo.ageRange}`,
        x, chipY,
        'rgba(5, 150, 105, 0.92)',
        cw
      );
    }
    ctx.font = '13px sans-serif';
    ctx.fillStyle = 'rgba(34, 197, 94, 0.15)';
  }
};

const faceTrackingLoop = async () => {
  if (!faceTrackingRunning) return;
  const now = performance.now();
  if (now - lastFaceDetectTime >= FACE_DETECT_INTERVAL) {
    lastFaceDetectTime = now;
    const video = videoRef.value;
    const canvas = overlayCanvasRef.value;
    if (video && video.videoWidth && canvas && faceDetectorReady.value && cameraActive.value && faceTracking.value) {
      try {
        const options = new faceapi.TinyFaceDetectorOptions({
          inputSize: 320,
          scoreThreshold: 0.5
        });
        const detections = await faceapi.detectAllFaces(video, options);
        const faces = detections.map(d => ({
          x: d.box.x,
          y: d.box.y,
          width: d.box.width,
          height: d.box.height,
          score: d.score
        }));
        const ageDue = ageNetReady && (now - lastAgeUpdateTime >= AGE_UPDATE_INTERVAL);
        if (ageDue) {
          lastAgeUpdateTime = now;
          faceAges = await predictFaceAges(video, faces);
        }
        canvas.faces = faces;
        drawFaceBoxes();
      } catch (e) {
        console.error('人脸检测失败:', e);
      }
    } else if (canvas) {
      canvas.faces = [];
      drawFaceBoxes();
    }
  }
  faceTrackingRafId = requestAnimationFrame(faceTrackingLoop);
};

const startFaceTracking = async () => {
  if (faceTrackingRunning) return;
  faceTrackingRunning = true;
  await initFaceDetector();
  lastFaceDetectTime = 0;
  faceTrackingRafId = requestAnimationFrame(faceTrackingLoop);
};

const stopFaceTracking = () => {
  faceTrackingRunning = false;
  if (faceTrackingRafId) {
    cancelAnimationFrame(faceTrackingRafId);
    faceTrackingRafId = null;
  }
  faceAges = [];
  lastAgeUpdateTime = 0;
  const canvas = overlayCanvasRef.value;
  if (canvas) {
    canvas.faces = [];
    drawFaceBoxes();
  }
};

const toggleFaceTracking = async () => {
  if (faceTracking.value) {
    if (!faceDetectorReady.value) {
      await initFaceDetector();
      if (!faceDetectorReady.value) {
        faceTracking.value = false;
        ElMessage.error(faceDetectorError.value || '人脸检测模型加载失败');
        return;
      }
    }
    startFaceTracking();
  } else {
    stopFaceTracking();
  }
};

const fetchStats = async () => {
  loadingStats.value = true;
  try {
    const res = await detectionAPI.getStats();
    detectionStats.value = res.data.data;
    loadingStats.value = false;
    await nextTick();
    renderStatsChart();
  } catch (e) {
    console.error('获取统计失败:', e);
    loadingStats.value = false;
  }
};

const renderStatsChart = () => {
  if (!chartRef.value || !detectionStats.value) return;
  
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }
  
  chartInstance.value = echarts.init(chartRef.value);
  
  const counts = detectionStats.value.emotionCounts;
  const categories = [];
  const values = [];
  const colors = [];
  
  for (const [label, count] of Object.entries(counts)) {
    categories.push(emotionLabels[label]?.cn || label);
    values.push(count);
    colors.push(emotionLabels[label]?.color || '#9ca3af');
  }
  
  const totalCount = detectionStats.value.totalCount;
  
  chartInstance.value.setOption({
    grid: {
      left: '2%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { color: '#64748b', fontSize: 11 },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#94a3b8', fontSize: 11 },
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      borderRadius: [6, 6, 0, 0],
      data: values.map((v, i) => ({
        value: v,
        totalCount: totalCount,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[i] },
            { offset: 1, color: colors[i] + '88' }
          ])
        }
      }))
    }],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e2e8f0',
      borderWidth: 1,
      textStyle: { color: '#334155' },
      formatter: function(params) {
        var p = params[0];
        return p.name + '：' + p.value + ' 次<br/>总检测数：' + p.data.totalCount + ' 次';
      }
    }
  });
};

const fetchWeather = async () => {
  loadingWeather.value = true;
  try {
    const res = await weatherAPI.getCurrent();
    weatherData.value = res.data.data;
  } catch (e) {
    console.error('获取天气失败:', e);
  } finally {
    loadingWeather.value = false;
  }
};

const fetchLatestWeather = async () => {
  loadingWeather.value = true;
  try {
    const res = await weatherAPI.getLatest();
    weatherData.value = res.data.data;
  } catch (e) {
    console.error('获取天气回显失败:', e);
  } finally {
    loadingWeather.value = false;
  }
};

const fetchAlertSummary = async () => {
  try {
    const res = await alertAPI.getSummary();
    alertSummary.value = res.data.data;
  } catch (e) {
    console.error('获取预警摘要失败:', e);
  }
};

const handleLogout = async () => {
  try {
    await authAPI.logout();
    authStore.logout();
    router.push('/login');
  } catch (e) {
    authStore.logout();
    router.push('/login');
  }
};

const openFeedback = () => {
  showFeedbackModal.value = true;
};

const submitFeedback = async () => {
  if (!feedbackForm.value.correctLabel) {
    ElMessage.warning('请选择正确的表情标签');
    return;
  }
  
  try {
    await detectionAPI.submitFeedback(detectionResult.value.recordId, {
      correctLabel: feedbackForm.value.correctLabel
    });
    ElMessage.success('反馈提交成功');
    showFeedbackModal.value = false;
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败');
  }
};

const handleResize = () => {
  chartInstance.value?.resize();
};

onMounted(() => {
  fetchLatestWeather();
  fetchRecentHistory();
  fetchStats();
  fetchAlertSummary();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  stopCamera();
});

const fetchRecentHistory = async () => {
  loadingHistory.value = true;
  try {
    const res = await detectionAPI.getHistory({ page: 1, size: 2 });
    recentHistory.value = res.data.data?.records || res.data.data || [];
  } catch (e) {
    console.error('获取历史失败:', e);
  } finally {
    loadingHistory.value = false;
  }
};
</script>

<template>
  <div class="home-page">
    <main class="main-content">
      <div class="main-left">
        <div class="card upload-card">
          <div class="card-header">
            <h2 class="card-title hero-title">情绪画像</h2>
            <span class="card-subtitle">JPG/PNG · 最大10MB</span>
          </div>
          <div class="mode-tabs">
            <button class="mode-tab" :class="{ active: cameraMode }" @click="switchToCamera">
              <span>📷</span>实时监测
            </button>
            <button class="mode-tab" :class="{ active: !cameraMode }" @click="switchToUpload">
              <span>🖼️</span>图片检测
            </button>
          </div>

          <template v-if="!cameraMode">
            <div class="upload-wrapper">
              <div 
                class="upload-area" 
                @dragover="handleDragOver" 
                @drop="handleDrop"
                @click="triggerFileInput"
              >
                <div v-if="!previewImage" class="upload-placeholder">
                  <div class="upload-icon">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                      <polyline points="17 8 12 3 7 8"/>
                      <line x1="12" y1="3" x2="12" y2="15"/>
                    </svg>
                  </div>
                  <p>点击或拖拽图片到此处</p>
                  <input ref="fileInputRef" type="file" accept="image/*" @change="handleFileSelect" class="file-input" />
                </div>
                <div v-else class="preview-container">
                  <img :src="previewImage" alt="预览" class="preview-image" />
                  <button @click="previewImage = null; selectedFile = null" class="remove-btn">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
            <div class="btn-group">
              <button @click="handleDetect" class="btn-primary detect-btn">
                <span class="btn-icon">🔍</span>
                开始检测
              </button>
              <button 
                @click="clearImage" 
                :disabled="!previewImage"
                class="btn-secondary clear-btn"
                :class="{ disabled: !previewImage }"
              >
                清空
              </button>
            </div>
          </template>

          <template v-else>
            <div class="camera-wrapper">
              <div v-show="cameraActive" class="camera-view">
                <video ref="videoRef" autoplay playsinline muted class="camera-video"></video>
                <canvas ref="overlayCanvasRef" class="face-overlay-canvas"></canvas>
                <span class="camera-live-badge" :class="{ pulsing: autoDetect }">● LIVE</span>
              </div>
              <div v-show="!cameraActive" class="camera-placeholder">
                <div class="camera-icon">
                  <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                    <circle cx="12" cy="13" r="4"/>
                  </svg>
                </div>
                <p>摄像头未开启</p>
                <p class="camera-hint">点击下方按钮开启本机摄像头</p>
              </div>
              <div v-if="cameraDevices.length > 1" class="camera-device-row">
                <span class="device-label">设备</span>
                <select v-model="selectedDeviceId" @change="switchCameraDevice" class="device-select">
                  <option v-for="(dev, idx) in cameraDevices" :key="dev.deviceId" :value="dev.deviceId">
                    {{ dev.label || ('摄像头 ' + (idx + 1)) }}
                  </option>
                </select>
              </div>
              <div class="btn-group camera-btns">
                <button v-if="!cameraActive" @click="startCamera" class="btn-primary detect-btn">
                  <span class="btn-icon">📷</span>
                  开启实时监测
                </button>
                <template v-else>
                  <button @click="handleCameraDetect" class="btn-primary detect-btn" :disabled="detecting">
                    <span class="btn-icon">📸</span>
                    {{ detecting ? '检测中...' : '拍照检测' }}
                  </button>
                  <label class="auto-switch" :class="{ active: autoDetect }">
                    <input type="checkbox" v-model="autoDetect" @change="toggleAutoDetect" />
                    <span>自动检测</span>
                  </label>
                  <label class="auto-switch" :class="{ active: faceTracking }">
                    <input type="checkbox" v-model="faceTracking" @change="toggleFaceTracking" />
                    <span>实时人脸框</span>
                  </label>
                  <button @click="stopCamera" class="btn-danger clear-btn">关闭摄像头</button>
                </template>
              </div>
              <div v-if="cameraActive && lastDetectAt" class="detect-feedback">
                <span class="dot" :class="{ pulsing: autoDetect }"></span>
                <span>上次检测 {{ lastDetectAt }}<template v-if="autoDetect"> · 自动每 2.5s</template></span>
              </div>
            </div>
          </template>
        </div>

        <div class="card result-card">
          <div class="card-header">
            <h2 class="card-title">检测结果</h2>
            <button v-if="detectionResult" @click="openFeedback" class="feedback-btn">纠错反馈</button>
          </div>
          <div class="result-content">
            <div v-if="detectionResult" class="result-info">
              <div class="emotion-display">
                <span class="emotion-emoji">{{ emotionLabels[detectionResult.emotionLabel]?.emoji }}</span>
                <div class="emotion-info">
                  <span class="emotion-name">{{ emotionLabels[detectionResult.emotionLabel]?.cn }}</span>
                  <span class="confidence">置信度: {{ (detectionResult.confidence * 100).toFixed(1) }}%</span>
                </div>
              </div>
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="stat-icon">👤</span>
                  <span class="stat-value">{{ detectionResult.faceCount }}</span>
                  <span class="stat-label">检测到人脸</span>
                </div>
                <div class="stat-item">
                  <span class="stat-icon">✅</span>
                  <span class="stat-value">{{ detectionResult.validFaceCount }}</span>
                  <span class="stat-label">有效人脸</span>
                </div>
                <div class="stat-item">
                  <span class="stat-icon">⚡</span>
                  <span class="stat-value">{{ detectionResult.processingTimeMs }}ms</span>
                  <span class="stat-label">检测耗时</span>
                </div>
              </div>
              <div class="advice-box" v-if="detectionResult.adviceText">
                <div class="advice-header">
                  <span class="advice-icon">💡</span>
                  <span class="advice-title">心理健康建议</span>
                </div>
                <p class="advice-content">{{ detectionResult.adviceText }}</p>
                <span class="advice-source">来源: {{ detectionResult.adviceSource }}</span>
              </div>
            </div>
            <div v-else-if="detecting" class="empty-state">
              <div class="skeleton sk-circle"></div>
              <div class="skeleton sk-line" style="width: 40%"></div>
              <div class="skeleton sk-line" style="width: 60%"></div>
            </div>
            <div v-else class="empty-state">
              <span class="empty-icon">📷</span>
              <p class="empty-title">尚未检测</p>
              <p class="empty-hint">上传图片或开启实时监测查看结果</p>
            </div>
          </div>
        </div>
      </div>

      <div class="main-right">
        <div class="right-panel">
          <div class="card small-card weather-card">
            <div class="card-header">
              <h2 class="card-title">{{ weatherData?.cityName || '定位中...' }}</h2>
              <div class="weather-header-right">
                <button class="weather-refresh-btn" @click="fetchWeather" :disabled="loadingWeather">
                  <span class="refresh-icon" :class="{ spinning: loadingWeather }">🔄</span>
                  更新
                </button>
              </div>
            </div>
            <div v-if="weatherData" class="weather-content">
              <div class="weather-main">
                <span class="weather-icon">{{ getWeatherEmoji(weatherData.weatherMain) }}</span>
                <div class="weather-info">
                  <span class="weather-temp">{{ weatherData.temperature }}°C</span>
                  <span class="weather-desc">{{ weatherData.weatherMain }}</span>
                </div>
              </div>
              <div class="weather-details">
                <div class="weather-detail">
                  <span class="detail-icon">💧</span>
                  <span>湿度 {{ weatherData.humidity }}%</span>
                </div>
                <div class="weather-detail wind-row">
                  <div class="wind-info">
                    <span class="detail-icon">💨</span>
                    <span>风向 {{ weatherData.windDir }}</span>
                  </div>
                  <span v-if="weatherData.obsTime" class="weather-update-time">更新于 {{ weatherData.obsTime }}</span>
                </div>
              </div>
            </div>
            <div v-else-if="loadingWeather" class="empty-state">
              <div class="skeleton sk-circle"></div>
              <div class="skeleton sk-line" style="width: 45%"></div>
            </div>
            <div v-else class="empty-state">
              <span class="empty-icon">🌤️</span>
              <p class="empty-title">暂无天气数据</p>
            </div>
          </div>

          <div class="card small-card chart-card">
            <div class="card-header">
              <h2 class="card-title">检测统计</h2>
              <span v-if="detectionStats" class="card-subtitle">共 {{ detectionStats.totalCount }} 次检测</span>
            </div>
            <div v-if="loadingStats" class="empty-state">
              <div class="skeleton sk-bar"></div>
              <div class="skeleton sk-line" style="width: 55%"></div>
            </div>
            <div v-else-if="detectionStats && detectionStats.totalCount > 0" ref="chartRef" class="chart-container"></div>
            <div v-else class="empty-state">
              <span class="empty-icon">📊</span>
              <p class="empty-title">暂无统计数据</p>
              <p class="empty-hint">完成检测后生成统计图表</p>
            </div>
          </div>

          <div class="card small-card history-card" @click="router.push('/history')">
            <div class="card-header">
              <h2 class="card-title">检测历史</h2>
              <span class="view-all">查看全部 →</span>
            </div>
            <div v-if="loadingHistory" class="empty-state">
              <div class="skeleton sk-line" style="width: 90%"></div>
              <div class="skeleton sk-line" style="width: 90%"></div>
            </div>
            <div v-else-if="recentHistory.length" class="history-list">
              <div v-for="item in recentHistory" :key="item.recordId || item.id" class="history-item">
                <span class="history-emoji">{{ emotionLabels[item.emotionLabel]?.emoji || '❓' }}</span>
                <div class="history-item-info">
                  <span class="history-item-label">{{ emotionLabels[item.emotionLabel]?.cn || '未知' }}</span>
                  <span class="history-item-time">{{ item.createdAt?.slice(0, 16) }}</span>
                </div>
                <span class="history-item-conf">{{ (item.confidence * 100).toFixed(1) }}%</span>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="empty-icon">📋</span>
              <p class="empty-title">暂无检测记录</p>
              <p class="empty-hint">检测后自动记录</p>
            </div>
          </div>

          <div class="card small-card alert-card" @click="router.push('/alerts')">
            <div class="card-header">
              <h2 class="card-title">全部预警</h2>
              <span class="view-all">查看全部 →</span>
            </div>
            <div class="alert-preview">
              <div class="alert-icon">🔔</div>
              <div v-if="alertSummary" class="alert-info">
                <span class="alert-count">{{ alertSummary.total }} 条预警</span>
                <span v-if="alertSummary.recent && alertSummary.recent.length" class="alert-desc">
                  {{ alertSummary.recent[0].cityName || alertSummary.recent[0].location || '' }}
                  {{ alertSummary.recent[0].ruleName || '' }}：{{ (alertSummary.recent[0].alertContent || '').slice(0, 20) }}…
                </span>
                <span v-else class="alert-desc">暂无预警消息</span>
              </div>
              <div v-else class="alert-info">
                <span class="alert-count">暂无预警</span>
                <span class="alert-desc">天气预警消息将在此显示</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showFeedbackModal" class="modal-overlay" @click.self="showFeedbackModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>纠错反馈</h3>
          <button @click="showFeedbackModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <p>当前识别结果: {{ emotionLabels[detectionResult?.emotionLabel]?.emoji }} {{ emotionLabels[detectionResult?.emotionLabel]?.cn }}</p>
          <p>请选择正确的表情:</p>
          <div class="emotion-grid">
            <button 
              v-for="(info, label) in emotionLabels" 
              :key="label"
              @click="feedbackForm.correctLabel = label"
              class="emotion-btn"
              :class="{ active: feedbackForm.correctLabel === label }"
            >
              <span class="emoji">{{ info.emoji }}</span>
              <span>{{ info.cn }}</span>
            </button>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showFeedbackModal = false" class="btn-secondary">取消</button>
          <button @click="submitFeedback" class="btn-primary">提交反馈</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
function getWeatherEmoji(desc) {
  const map = {
    '晴': '☀️', '多云': '⛅', '阴': '☁️',
    '小雨': '🌦️', '中雨': '🌧️', '大雨': '🌧️', '暴雨': '⛈️',
    '雷阵雨': '⛈️', '阵雨': '🌦️', '雨夹雪': '🌨️',
    '小雪': '❄️', '中雪': '❄️', '大雪': '❄️', '暴雪': '❄️',
    '雾': '🌫️', '霾': '🌫️', '浮尘': '🌫️', '沙尘暴': '🌫️',
    '冻雨': '🌨️', '冰雹': '🧊', '台风': '🌀'
  };
  return map[desc] || '🌤️';
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f3ff 0%, #fdf2f8 50%, #f0f9ff 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo .smiley {
  font-size: 22px;
}

.header-left h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 14px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f1f5f9;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.avatar-placeholder {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.nickname {
  font-weight: 500;
  color: #334155;
  font-size: 14px;
}

.logout-btn {
  padding: 10px 22px;
  background: #fef2f2;
  border: none;
  border-radius: 10px;
  color: #ef4444;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #fee2e2;
}

.main-content {
  display: flex;
  gap: 24px;
  padding: 24px 0 24px var(--page-offset);
  min-height: calc(100vh - 72px - 48px);
}

.main-left {
  width: 55%;
  display: flex;
  flex-direction: column;
  gap: 24px;
  justify-content: space-between;
}

.main-right {
  flex: 1;
  margin-right: -4px;
}

.right-panel {
  background: linear-gradient(135deg, #f0f4ff 0%, #f5f0ff 50%, #fdf0f5 100%);
  border-radius: 24px 0 0 24px;
  padding: 24px 20px 24px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  border: 1px solid rgba(99, 102, 241, 0.1);
  border-right: none;
  box-shadow: -4px 4px 24px rgba(99, 102, 241, 0.06);
}

.right-panel .card {
  flex: 1;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.main-left .card,
.main-left .upload-card,
.main-left .result-card {
  flex: 1;
  min-height: 400px;
}

.card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 24px;
}

.small-card {
  flex-shrink: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e1b4b;
}

.card-subtitle {
  font-size: 13px;
  color: #94a3b8;
}

.view-all {
  font-size: 13px;
  color: #6366f1;
  font-weight: 500;
}

.upload-card {
  display: flex;
  flex-direction: column;
}

.upload-wrapper {
  min-height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-area {
  width: 100%;
  height: 280px;
  border: 2px dashed #cbd5e1;
  border-radius: 14px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  overflow: hidden;
}

.upload-area:hover, .upload-area.dragover {
  border-color: #6366f1;
  background: #faf5ff;
}

.upload-placeholder .upload-icon {
  color: #94a3b8;
  margin-bottom: 18px;
}

.upload-placeholder p {
  color: #64748b;
  margin-bottom: 8px;
  font-size: 15px;
}

.file-input {
  display: none;
}

.mode-tabs {
  display: flex;
  gap: 8px;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 16px;
}

.mode-tab {
  flex: 1;
  padding: 9px 0;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.mode-tab.active {
  background: white;
  color: #6366f1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.camera-wrapper {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 16px 12px;
}

.camera-view {
  position: relative;
  width: 100%;
  height: 280px;
  flex: none;
  border-radius: 14px;
  overflow: hidden;
  background: #000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.camera-live-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(220, 38, 38, 0.9);
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
}

.camera-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 280px;
  flex: none;
  background: #fafafa;
  border: 2px dashed #cbd5e1;
  border-radius: 14px;
  color: #94a3b8;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.camera-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #94a3b8;
}

.camera-placeholder p {
  margin: 4px 0;
  color: #64748b;
  font-size: 15px;
}

.camera-placeholder .camera-hint {
  font-size: 13px !important;
  color: #94a3b8 !important;
}

.camera-device-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.device-label {
  font-size: 13px;
  color: #64748b;
  flex-shrink: 0;
}

.device-select {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  color: #334155;
  background: white;
  outline: none;
  transition: border-color 0.2s;
}

.device-select:focus {
  border-color: #6366f1;
}

.camera-btns {
  margin-top: 8px;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 0 4px;
}

.auto-switch {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
  transition: all 0.2s ease;
  user-select: none;
}

.auto-switch input {
  accent-color: #6366f1;
  width: 15px;
  height: 15px;
  cursor: pointer;
}

.auto-switch.active {
  background: #f5f3ff;
  border-color: #c7d2fe;
  color: #6366f1;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.2);
}

.preview-container {
  position: relative;
  width: 100%;
  height: 280px;
  border-radius: 14px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: white;
}

.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  color: white;
  padding: 14px 32px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
}

.btn-group {
  display: flex;
  gap: 10px;
  margin-top: 0;
}

.btn-group .detect-btn {
  flex: 1;
  margin-top: 0;
  padding: 12px 20px;
}

.btn-danger {
  background: #fee2e2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 14px 24px;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-danger:hover:not(.disabled) {
  background: #fecaca;
  color: #b91c1c;
}

.btn-danger.disabled {
  background: #f3f4f6;
  border-color: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 18px;
}

.result-card {
  flex-shrink: 0;
  min-height: 380px;
}

.feedback-btn {
  padding: 8px 18px;
  background: #fef3c7;
  border: none;
  border-radius: 10px;
  color: #d97706;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.feedback-btn:hover {
  background: #fde68a;
}

.result-content {
  min-height: 320px;
}

.result-info {
  display: flex;
  flex-direction: column;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 160px;
  color: #94a3b8;
  padding: 16px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
}

.empty-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.emotion-display {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #f5f3ff 0%, #fdf2f8 100%);
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08);
}

.emotion-emoji {
  font-size: 72px;
  flex-shrink: 0;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

.emotion-info {
  flex: 1;
  min-width: 0;
}

.emotion-name {
  font-size: 32px;
  font-weight: 700;
  color: #1e1b4b;
  margin-bottom: 8px;
}

.confidence {
  display: inline-block;
  padding: 8px 20px;
  background: #6366f1;
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  background: white;
  padding: 20px;
  border-radius: 14px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.stat-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 28px;
  margin-bottom: 10px;
  display: block;
}

.stat-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #6366f1;
  margin-bottom: 6px;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  line-height: 1.4;
}

.advice-box {
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 50%, #d1fae5 100%);
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #10b981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.15);
}

.advice-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(16, 185, 129, 0.2);
}

.advice-icon {
  font-size: 24px;
}

.advice-title {
  font-weight: 600;
  color: #047857;
  font-size: 16px;
}

.advice-content {
  color: #065f46;
  line-height: 1.8;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 500;
}

.advice-source {
  font-size: 13px;
  color: #047857;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.8);
  padding: 6px 12px;
  border-radius: 8px;
}

.chart-card {
  flex-shrink: 0;
  min-height: 206px;
}

.chart-container {
  height: 180px;
}

.weather-card {
  min-height: 206px;
  display: flex;
  flex-direction: column;
}

.weather-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.weather-refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.weather-refresh-btn:hover:not(:disabled) {
  background: #eef2ff;
  border-color: #c7d2fe;
  color: #6366f1;
}

.weather-refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  font-size: 13px;
  line-height: 1;
}

.refresh-icon.spinning {
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.weather-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 18px;
}

.weather-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.weather-icon {
  font-size: 48px;
}

.weather-info {
  display: flex;
  flex-direction: column;
}

.weather-temp {
  font-size: 36px;
  font-weight: 700;
  color: #1e1b4b;
}

.weather-desc {
  color: #64748b;
  font-size: 13px;
}

.weather-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.weather-detail {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
}

.detail-icon {
  font-size: 16px;
}

.weather-update-time {
  font-size: 11px;
  color: #94a3b8;
  margin-left: auto;
}

.wind-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.wind-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-card, .alert-card {
  min-height: 206px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-card:hover, .alert-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.history-preview, .alert-preview {
  display: flex;
  align-items: center;
  gap: 16px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 12px;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f1f5f9;
}

.history-emoji {
  font-size: 28px;
}

.history-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.history-item-label {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.history-item-time {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.history-item-conf {
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
}

.alert-icon {
  font-size: 36px;
}

.alert-desc {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 520px;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 26px;
  border-bottom: 1px solid #f1f5f9;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
}

.close-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: #f1f5f9;
  border-radius: 10px;
  font-size: 22px;
  cursor: pointer;
  color: #64748b;
}

.modal-body {
  padding: 26px;
}

.modal-body p {
  margin-bottom: 18px;
  color: #334155;
  font-size: 15px;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.emotion-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 14px;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.emotion-btn:hover {
  border-color: #6366f1;
  background: #faf5ff;
  transform: translateY(-2px);
}

.emotion-btn.active {
  border-color: #6366f1;
  background: #faf5ff;
}

.emotion-btn .emoji {
  font-size: 30px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding: 20px 26px;
  border-top: 1px solid #f1f5f9;
}

.btn-secondary {
  background: #f1f5f9;
  border: none;
  color: #64748b;
  padding: 12px 26px;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-title {
  font-size: 22px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.upload-card {
  position: relative;
  overflow: hidden;
}

.upload-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899);
  border-radius: 20px 20px 0 0;
}

.detect-feedback {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  margin-top: 10px;
}

.detect-feedback .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
}

.detect-feedback .dot.pulsing {
  animation: pulse 1.4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.5); }
  50% { box-shadow: 0 0 0 6px rgba(34, 197, 94, 0); }
}

.skeleton {
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease infinite;
  border-radius: 8px;
}

.sk-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin-bottom: 12px;
}

.sk-bar {
  width: 70%;
  height: 120px;
  margin-bottom: 12px;
}

.sk-line {
  height: 12px;
  margin: 6px 0;
}

@keyframes shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}

@media (max-width: 1200px) {
  .main-content {
    flex-direction: column;
  }

  .main-left {
    width: 100%;
  }

  .main-right {
    width: 100%;
  }

  .right-panel {
    border-radius: 24px;
    border: 1px solid rgba(99, 102, 241, 0.1);
    margin-right: 0;
  }
}

@media (max-width: 768px) {
  .right-panel {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>