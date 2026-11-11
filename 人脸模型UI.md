# 人脸模型前端 —— 摄像头视频回显检测、实时人脸框与年龄识别

> 本文档记录表情检测系统**前端**如何调用本机摄像头、实现视频回显、前端本地实时人脸框、人脸年龄识别（年龄段），以及年龄数据随检测写入数据库的实现思路与关键代码解析。
>
> 涉及文件：
> - `emotion-web/src/views/Home.vue`（全部前端逻辑与样式）
> - `emotion-web/package.json`（依赖 `@vladmandic/face-api`）
> - `emotion-web/public/models/`（前端本地模型：tiny face detector + age gender）

---

## 一、功能概述

原系统只支持**上传图片**做表情检测。为贴近"用户不会主动上传"的实际场景，前端新增了三种能力：

1. **摄像头检测（视频回显）**：在网页内直接调用本机摄像头，实时显示画面，支持"拍照检测"和"自动检测"两种模式，截取的一帧图片走原有 `POST /api/detection/detect` 链路完成情绪识别（记录、历史、统计、建议全复用，后端零改动）。
2. **实时人脸框**：在摄像头实时画面上，用一个随人脸位置/大小实时变化的绿色矩形框把人脸框出来。为保证"流畅实时"，人脸检测**在浏览器本地完成**（face-api.js 的 tiny face detector），不请求服务器；情绪识别仍由服务端在拍照/自动检测时完成。
3. **人脸年龄识别（年龄段）**：在实时人脸框上方叠加该人脸的年龄段文本（如 `男 · 28岁 · 青年`），同样**在浏览器本地完成**（face-api 的 `ageGenderNet`）。实时画面上的年龄**仅展示、不入库**；拍照/自动检测时会把主脸的性别/年龄/年龄段随请求传给后端存入 `detection_record`。

**情绪的去向**：服务端情绪**不做实时**，也不画在实时框上——拍照/自动检测后仍在右侧结果卡片展示（原行为），与实时框的人脸框/置信度/年龄标签互不干扰。

### 整体架构

```
┌──────────────────────── 浏览器（前端） ────────────────────────┐
│                                                                │
│  摄像头流  ──►  <video> 实时回显                                │
│                  │                                             │
│                  ├─► 本地 face-api (tinyFaceDetector)          │
│                  │       每 ~120ms 检测人脸 ──► <canvas> 画绿框 │
│                  │                                             │
│                  ├─► 本地 face-api (ageGenderNet)              │
│                  │       每 ~600ms 按框裁剪人脸 → 年龄段文本     │
│                  │                                             │
│                  └─► 每 2.5s 截帧(canvas) ──► POST /detection/detect
│                        + 主脸 modelGender/modelAge/modelRange   │
└──────────────────────────────┬─────────────────────────────────┘
                               │  multipart/form-data (image/jpeg + 3 个表单字段)
                               ▼
                    Java Spring Boot (8080)
                               │  调用
                               ▼
                    Python FastAPI (8001)
                        MTCNN + YOLOv8-cls
```

### 关键认知：系统中有"三套"本地/服务端模型分工

| 维度 | 实时框（前端本地） | 年龄识别（前端本地） | 情绪识别（服务端） |
|---|---|---|---|
| 模型 | `tiny_face_detector` | `age_gender_model`（AgeGenderNet） | MTCNN + YOLOv8-cls |
| 运行位置 | 浏览器（TensorFlow.js） | 浏览器（TensorFlow.js） | Python FastAPI |
| 模型文件 | `public/models/tiny_face_detector_model.bin`（约193KB） | `public/models/age_gender_model.bin`（约430KB） | `python-service/models/` 下的 `.pt` |
| 作用 | 定位人脸、画框（纯视觉） | 估算年龄 + 性别（仅展示） | 检测人脸 + 分类情绪 + 存库 |
| 频率 | ~120ms / 次 | ~600ms / 次 | 2.5s / 次 |
| 相互影响 | 无 | 无 | 无 |

> 提示：页面上报错"人脸检测模型加载失败"指的是**前端** tiny face detector 模型加载失败，与 Python 的 MTCNN / YOLOv8 无关；年龄模型若加载失败则仅在控制台告警，页面上只不显示年龄文本，不影响人脸框。

---

## 二、依赖与模型文件

### 1. 安装依赖

```bash
npm install @vladmandic/face-api
```

使用 `@vladmandic/face-api`（face-api.js 的维护版 fork）而非原版 `face-api.js`，因为原版对现代打包器（Vite/Webpack5）兼容性差，维护版可直接 `import * as faceapi` 使用。

`package.json` 新增：

```json
"dependencies": {
  "@vladmandic/face-api": "^x.x.x"
}
```

### 2. 模型文件

从 `node_modules/@vladmandic/face-api/model/` 复制 tiny face detector 与 age gender 模型文件到前端静态目录：

```bash
mkdir -p public/models
# 实时人脸框：tiny face detector
cp node_modules/@vladmandic/face-api/model/tiny_face_detector_model.bin \
   node_modules/@vladmandic/face-api/model/tiny_face_detector_model-weights_manifest.json \
   public/models/
# 年龄识别：age gender（人脸年龄 + 性别）
cp node_modules/@vladmandic/face-api/model/age_gender_model.bin \
   node_modules/@vladmandic/face-api/model/age_gender_model-weights_manifest.json \
   public/models/
```

- `tiny_face_detector_model.bin`：量化后的权重（约193KB）
- `age_gender_model.bin`：年龄/性别模型权重（约430KB），基于 TinyXception 主干 + VGG-Face 预训练
- `*-weights_manifest.json`：权重清单，声明各层名称、shape、量化参数以及 bin 文件路径

Vite 会把 `public/` 原样拷贝到构建产物 `dist/`，因此生产环境可直接通过 `/models/...` 访问。

> **注意**：`@vladmandic/face-api` 的 ESM 构建**只导出 `nets` 对象（内含 `nets.ageGenderNet`）与顶层函数 `predictAgeAndGender`**，并没有顶层 `ageGenderNet` 实例。因此代码中必须通过 `faceapi.nets.ageGenderNet` 访问，直接写 `faceapi.ageGenderNet` 会报 "not exported" 构建错误。

---

## 三、实现过程思路

整体实现分五步推进：

1. **接入摄像头与视频回显**：Tab 切换「上传图片 / 摄像头检测」，用 `getUserMedia` 打开摄像头，把 `MediaStream` 挂到 `<video>` 实现实时回显，并支持多摄像头选择。
2. **截帧检测**：点击"拍照检测"时把 `<video>` 当前帧绘制到临时 `<canvas>`，`toBlob` 成 JPEG，包装成 `File` 走现有 `detectionAPI.detect()`，复用完整后端链路（存库/统计/建议/历史）。
3. **自动检测**：`setInterval` 每 2.5s 截帧检测一次，加"检测锁"防止上一帧请求未返回时重复发起。
4. **实时人脸框**：摄像头开启后加载 face-api 本地模型，用 `requestAnimationFrame` 循环每 ~120ms 检测一次人脸，把结果画在叠加于视频上的 `<canvas>`；人脸移动/靠近/远离时，框随之平滑变化。
5. **年龄识别（年龄段）**：在检测循环中按人脸框裁剪脸部，交给本地 `ageGenderNet` 估算年龄+性别，每 ~600ms 更新一次（避免拖慢框的实时性），并把 `男 · 28岁 · 青年` 文本绘制到框上方；拍照/自动检测时把主脸的三个值随请求传给后端存库。

设计上遵循四个原则：
- **框的实时性交给浏览器本地模型**：人脸框要做到"流畅实时"，如果每帧都请求服务器（WebSocket 推帧），在 CPU 上 MTCNN 单帧就要几百毫秒，且网络开销大；本地 tiny 模型可在 ~120ms 内完成，且不占服务器资源。
- **情绪的准确性交给服务端模型**：情绪分类仍走 YOLOv8-cls（前端模型不含情绪分类），且**情绪不做实时**——只在拍照/自动检测时由服务端识别，结果展示在右侧结果卡片，不画到实时框上。
- **年龄同样走前端本地**：年龄仅作页面展示、不涉及业务实时存储，因此复用 face-api 本地模型即可；检测存库时把主脸的性别/年龄/年龄段随请求带上，Python 零改动。
- **前端代码尽量贴合原有结构**：只改 `Home.vue` 与 `api/index.js`，不新增路由/组件，后端 Java 仅加 3 个可选入参。

---

## 四、前端调用本机摄像头（关键代码解析）

### 1. 摄像头相关状态（`Home.vue`）

```js
const cameraMode = ref(false);      // 是否处于摄像头模式（vs 上传模式）
const cameraActive = ref(false);    // 摄像头流是否已成功打开
const autoDetect = ref(false);      // 自动检测开关
const detecting = ref(false);       // 检测请求锁（防重复）
const cameraStream = ref(null);     // MediaStream 引用（用于释放）
const videoRef = ref(null);         // <video> 元素（回显画面）
const cameraDevices = ref([]);      // 摄像头设备列表
const selectedDeviceId = ref('');   // 当前选中的设备 id
let autoDetectTimer = null;         // 自动检测定时器句柄
```

### 2. 模式切换（上传 / 摄像头）

```js
const switchToUpload = () => {
  if (!cameraMode.value) return;
  cameraMode.value = false;
  stopCamera();                     // 切回上传时释放摄像头
};

const switchToCamera = async () => {
  cameraMode.value = true;
  await nextTick();                 // 等 <video>/<canvas> 渲染完成
  await startCamera();              // 打开摄像头
  await loadCameraDevices();        // 列出可用设备（权限授予后 label 才可见）
};
```

**解析**：
- 摄像头区域用 `v-if="!cameraMode"` / `v-else` 切换，`switchToCamera` 里必须先 `nextTick()`，因为 `startCamera` 要向 `<video>` 挂流，而该元素是切到摄像头模式后才渲染的，此刻 DOM 尚未就绪。
- 先 `getUserMedia` 授予权限，再 `enumerateDevices`，否则设备 `label` 为空字符串（浏览器出于隐私限制）。

### 3. 设备枚举（多摄像头支持）

```js
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
```

**解析**：`enumerateDevices()` 返回音频输入/音频输出/视频输入等全部设备，过滤 `kind === 'videoinput'` 即摄像头。`deviceId` 是设备唯一标识，切换设备时把新 `deviceId` 传给 `getUserMedia` 重新打开即可。

### 4. 打开/关闭摄像头（核心）

```js
const startCamera = async () => {
  if (cameraActive.value || cameraStream.value) return;   // 防重复打开
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
      videoRef.value.srcObject = cameraStream.value;   // 关键：把流挂到 video
      videoRef.value.play().catch(() => {});
    }
    cameraActive.value = true;
    ElMessage.success('摄像头已开启');
    if (faceTracking.value) {
      startFaceTracking();                             // 开启实时人脸框
    }
  } catch (e) {
    console.error('开启摄像头失败:', e);
    let msg = '无法打开摄像头';
    if (e.name === 'NotAllowedError')  msg = '摄像头权限被拒绝，请在浏览器地址栏允许访问摄像头';
    else if (e.name === 'NotFoundError') msg = '未检测到摄像头设备';
    else if (e.name === 'NotReadableError') msg = '摄像头被其他程序占用，请关闭后重试';
    ElMessage.error(msg);
    if (cameraStream.value) {
      cameraStream.value.getTracks().forEach(t => t.stop());  // 失败清理
      cameraStream.value = null;
    }
    cameraActive.value = false;
  }
};

const stopCamera = () => {
  if (cameraStream.value) {
    cameraStream.value.getTracks().forEach(t => t.stop());    // 关键：停止所有轨道释放设备
    cameraStream.value = null;
  }
  cameraActive.value = false;
  stopAutoDetect();
  stopFaceTracking();
};
```

**解析**：
- **视频回显的本质**：`video.srcObject = mediaStream` 一行代码即可让 `<video>` 播放摄像头画面，无需任何流媒体服务器。
- `{ exact: deviceId }` 表示"精确使用该设备"，如果不满足会抛错；`deviceId` 为空时不传该字段，浏览器使用默认摄像头。
- **释放资源**：`stream.getTracks().forEach(t => t.stop())` 必须调用，否则摄像头指示灯常亮、被占用的设备无法被其他程序使用。
- **权限错误分类**：`NotAllowedError`（用户拒绝/非 HTTPS）、`NotFoundError`（无设备）、`NotReadableError`（被占用），分别给出中文提示。
- **必须在 `localhost` 或 HTTPS 下**使用，否则浏览器直接拒绝 `getUserMedia`（安全策略）。

### 5. 设备切换

```js
const switchCameraDevice = async () => {
  stopCamera();      // 先释放当前设备
  await nextTick();
  await startCamera();  // 再用新 deviceId 打开
};
```

### 6. 截帧检测（视频回显 → 图片 → 走原接口）

```js
const captureFrame = () => {
  const video = videoRef.value;
  if (!video || !video.videoWidth || !video.videoHeight) return Promise.resolve(null);
  return new Promise(resolve => {
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;      // 用视频原生分辨率
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);  // 画一帧
    canvas.toBlob(resolve, 'image/jpeg', 0.9);                // 转 JPEG Blob
  });
};

const runCameraDetect = async (showLoading) => {
  const blob = await captureFrame();
  if (!blob) {
    if (showLoading) ElMessage.warning('摄像头画面尚未就绪，请稍候');
    return false;
  }
  const file = new File([blob], 'camera.jpg', { type: 'image/jpeg' });
  const res = await detectionAPI.detect(file);   // 完全复用上传检测接口
  detectionResult.value = res.data.data;
  await fetchStats();
  await fetchWeather();
  await fetchRecentHistory();
  return true;
};
```

**解析**：
- `drawImage(video, ...)` 把 `<video>` 当前帧绘制到离屏 canvas；canvas 尺寸取 `video.videoWidth/Height`（原生分辨率，非 CSS 尺寸），保证发送的图片清晰无损。
- `toBlob(..., 'image/jpeg', 0.9)` 输出 JPEG（比 PNG 小得多），后端 `validateFile` 接受 jpeg/png，完全兼容。
- 截得的 Blob 包装成 `File` 后，直接复用 `detectionAPI.detect()` —— **这就是"视频回显检测"与原有上传共用同一链路的关键**：历史记录、统计、建议、环境数据全部自动生效。

### 7. 手动拍照 / 自动检测

```js
const handleCameraDetect = async () => {
  if (!cameraActive.value) { ElMessage.warning('请先开启摄像头'); return; }
  if (detecting.value) return;                 // 锁
  detecting.value = true;
  const loading = ElLoading.service({ lock: true, text: '正在检测...', background: 'rgba(0, 0, 0, 0.5)' });
  try {
    await runCameraDetect(true);
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '检测失败');
  } finally {
    detecting.value = false;
    loading.close();
  }
};

const startAutoDetect = () => {
  stopAutoDetect();
  autoDetectTimer = setInterval(async () => {
    if (detecting.value || !cameraActive.value) return;   // 请求未返回则跳过本帧
    detecting.value = true;
    try { await runCameraDetect(false); }
    catch (e) { console.error('自动检测失败:', e); }
    finally { detecting.value = false; }
  }, 2500);
};

const stopAutoDetect = () => {
  if (autoDetectTimer) { clearInterval(autoDetectTimer); autoDetectTimer = null; }
};
```

**解析**：
- 自动检测间隔 **2500ms**，带 `detecting` 锁：上一帧请求未返回时直接跳过，避免请求堆积。
- 自动模式不弹全屏 loading（避免每 2.5s 闪烁），只在后台刷新结果/统计/历史。
- 注意：目前自动检测每次都正常存库（与上传一致），会产生较多历史记录，如需控制可在后续按"情绪变化才存库"策略优化。

---

## 五、实时人脸框（关键代码解析）

### 1. 状态与常量

```js
const faceTracking = ref(true);        // "实时人脸框"开关（默认开）
const faceDetectorReady = ref(false);  // 本地模型是否加载完成
const faceDetectorError = ref('');     // 模型加载失败原因
const overlayCanvasRef = ref(null);    // 叠加画布
let faceTrackingRunning = false;       // 循环是否运行中
let faceTrackingRafId = null;          // requestAnimationFrame 句柄
let lastFaceDetectTime = 0;            // 上次检测时间（用于节流）
const FACE_DETECT_INTERVAL = 120;      // 检测间隔 ms（约8fps）
```

### 2. 模型初始化

```js
const initFaceDetector = async () => {
  if (faceDetectorReady.value) return;
  try {
    await faceapi.nets.tinyFaceDetector.loadFromUri('/models');
    faceDetectorReady.value = true;
  } catch (e) {
    console.error('人脸检测模型加载失败:', e);
    faceDetectorError.value = '人脸检测模型加载失败，实时框不可用';
  }
};
```

**解析**：`loadFromUri('/models')` 从静态目录加载 tiny face detector 模型进浏览器内存（一次性，约 193KB）。加载失败时只禁用实时框功能，不影响摄像头和情绪识别。

### 3. 检测主循环（核心）

```js
const faceTrackingLoop = async () => {
  if (!faceTrackingRunning) return;                 // 停止标记
  const now = performance.now();
  if (now - lastFaceDetectTime >= FACE_DETECT_INTERVAL) {   // 120ms 节流
    lastFaceDetectTime = now;
    const video = videoRef.value;
    const canvas = overlayCanvasRef.value;
    // 全部条件满足才真正检测
    if (video && video.videoWidth && canvas && faceDetectorReady.value
        && cameraActive.value && faceTracking.value) {
      try {
        const options = new faceapi.TinyFaceDetectorOptions({
          inputSize: 320,      // 输入尺寸：把帧缩到320px，速度与精度平衡
          scoreThreshold: 0.5  // 置信度低于0.5 不算人脸
        });
        const detections = await faceapi.detectAllFaces(video, options);
        canvas.faces = detections.map(d => ({
          x: d.box.x, y: d.box.y, width: d.box.width, height: d.box.height, score: d.score
        }));
        drawFaceBoxes();
      } catch (e) {
        console.error('人脸检测失败:', e);
      }
    } else if (canvas) {
      canvas.faces = [];        // 条件不满足：清空画布
      drawFaceBoxes();
    }
  }
  faceTrackingRafId = requestAnimationFrame(faceTrackingLoop);  // 调度下一帧
};

const startFaceTracking = async () => {
  if (faceTrackingRunning) return;
  faceTrackingRunning = true;
  await initFaceDetector();     // 确保模型就绪
  lastFaceDetectTime = 0;
  faceTrackingRafId = requestAnimationFrame(faceTrackingLoop);
};

const stopFaceTracking = () => {
  faceTrackingRunning = false;
  if (faceTrackingRafId) { cancelAnimationFrame(faceTrackingRafId); faceTrackingRafId = null; }
  const canvas = overlayCanvasRef.value;
  if (canvas) { canvas.faces = []; drawFaceBoxes(); }  // 清空残留框
};
```

**解析（设计要点）**：
- **`requestAnimationFrame` 驱动**：浏览器每帧（~16ms）回调一次，与屏幕刷新同步；但人脸检测很重，用 `lastFaceDetectTime` + 120ms 节流成约 8fps，避免占满 CPU。
- **`await` 天然防重叠**：`detectAllFaces` 是异步的，`requestAnimationFrame(faceTrackingLoop)` 在本次检测完成之后才注册下一帧，因此实际速率 = `max(120ms, 单次检测耗时)`，绝不会两个检测并发跑（TensorFlow.js 也不支持并发跑同模型）。
- **结果挂 DOM 属性而非 Vue ref**：每 120ms 更新一个 Vue ref 会触发整个组件响应式重渲染，代价高；把结果存到 `canvas.faces`（普通 DOM 对象属性），只由 `drawFaceBoxes` 读取，零 Vue 开销，这是保证流畅的关键技巧。
- **条件守卫**：视频有画面（`video.videoWidth`）、模型就绪、摄像头开启、开关打开，任一不满足就清空画布，防止黑屏/未就绪时报错。

### 4. 坐标换算与画框（关键难点）

```js
// 视频以 object-fit: contain 等比缩放居中显示时，
// 把"视频原生坐标系"换算成"容器屏幕坐标系"
const getDisplayRect = (video, container) => {
  const vidW = video.videoWidth;
  const vidH = video.videoHeight;
  const cw = container.clientWidth;
  const ch = container.clientHeight;
  const scale = Math.min(cw / vidW, ch / vidH);   // contain 等比缩放系数
  return {
    offsetX: (cw - vidW * scale) / 2,             // 水平居中偏移（黑边）
    offsetY: (ch - vidH * scale) / 2,             // 垂直居中偏移
    scale
  };
};

const drawFaceBoxes = () => {
  const canvas = overlayCanvasRef.value;
  const video = videoRef.value;
  if (!canvas || !video || !canvas.parentElement) return;
  const container = canvas.parentElement;
  const cw = container.clientWidth;
  const ch = container.clientHeight;
  if (canvas.width !== cw || canvas.height !== ch) {  // 画布像素=容器像素，防拉伸
    canvas.width = cw;
    canvas.height = ch;
  }
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, cw, ch);

  const faces = canvas.faces;
  if (!faces || !faces.length || !video.videoWidth) return;

  const rect = getDisplayRect(video, container);
  ctx.lineWidth = 3;
  ctx.strokeStyle = '#22c55e';                       // 绿色描边
  ctx.fillStyle = 'rgba(34, 197, 94, 0.15)';         // 半透明填充
  ctx.font = '13px sans-serif';
  ctx.textBaseline = 'top';

  for (const f of faces) {
    const x = f.x * rect.scale + rect.offsetX;       // 原生坐标 → 屏幕坐标
    const y = f.y * rect.scale + rect.offsetY;
    const w = f.width * rect.scale;
    const h = f.height * rect.scale;
    ctx.beginPath();
    ctx.rect(x, y, w, h);
    ctx.fill();
    ctx.stroke();
    ctx.fillStyle = '#059669';
    ctx.fillText(`${Math.round(f.score * 100)}%`, x + 4, y + 4);  // 左上角置信度
    ctx.fillStyle = 'rgba(34, 197, 94, 0.15)';
  }
};
```

**解析（为什么需要坐标换算）**：
- face-api 返回的 `box` 坐标位于**视频原生分辨率**（如 1280×720）坐标系；而 `<video>` 在页面里被 CSS 缩放到容器大小，两者坐标系不同，直接画会错位。
- `getDisplayRect` 按 `object-fit: contain` 的语义计算：`scale = min(容器宽/视频宽, 容器高/视频高)` 保证等比缩放不裁剪，`offsetX/offsetY` 是居中后两侧/上下的黑边偏移。
- 画框时每个点做 `原生坐标 * scale + offset`，即"视频帧里 (x, y) 的点，在屏幕上显示于 (x*scale+offsetX, y*scale+offsetY)"，从而框精确贴合人脸。
- 每次先同步 canvas 像素尺寸与容器 CSS 尺寸，避免 canvas 被拉伸导致框模糊/错位。

> **重要注意点（object-fit 与坐标的联动）**：
> 当前 `Home.vue` 样式中 `.camera-video` 设置为 `object-fit: cover`（撑满容器、裁切边缘）。`cover` 会裁剪画面，上述基于 `contain` 的换算公式将不再严格成立，框可能出现偏移。
> 若追求"框与人脸精确对齐"，应使用 `object-fit: contain`（全画面可见、无裁剪），或为 `cover` 场景单独推导裁剪偏移公式。二者是"美观全屏"与"精确对齐"的取舍。

### 5. 开关逻辑

```js
const toggleFaceTracking = async () => {
  if (faceTracking.value) {                    // 用户打开开关
    if (!faceDetectorReady.value) {            // 模型未加载则先加载
      await initFaceDetector();
      if (!faceDetectorReady.value) {          // 加载失败则回滚开关并提示
        faceTracking.value = false;
        ElMessage.error(faceDetectorError.value || '人脸检测模型加载失败');
        return;
      }
    }
    startFaceTracking();
  } else {
    stopFaceTracking();                        // 用户关闭：停止循环并清空画布
  }
};
```

### 6. 模板（视频回显 + 叠加画布）

```html
<div v-show="cameraActive" class="camera-view">
  <video ref="videoRef" autoplay playsinline muted class="camera-video"></video>
  <canvas ref="overlayCanvasRef" class="face-overlay-canvas"></canvas>  <!-- 叠加层 -->
  <span class="camera-live-badge">● LIVE</span>
</div>
```

```css
.camera-view {
  position: relative;
  width: 100%;
  height: 100%;
  flex: 1;
  border-radius: 14px;
  overflow: hidden;         /* 圆角裁切 */
  background: #000;
  min-height: 240px;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;        /* 见上方"重要注意点" */
}

.face-overlay-canvas {
  position: absolute;       /* 绝对定位压在视频上 */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;     /* 不拦截鼠标点击 */
  z-index: 1;
}
```

**解析**：
- `video` 加 `autoplay playsinline muted`：自动播放、移动端不弹出全屏、静音（否则部分浏览器禁止带声音的自动播放）。
- `<canvas>` 与视频同容器、同尺寸，`position: absolute` 叠在上面；`pointer-events: none` 保证用户仍可点击下层按钮。
- `v-show="cameraActive"` 而非 `v-if`：让 `<video>` 元素常驻 DOM，`startCamera` 中 `nextTick()` 后即可 `srcObject` 赋值。

### 7. 生命周期联动

```js
// startCamera 成功路径里：
cameraActive.value = true;
if (faceTracking.value) startFaceTracking();   // 摄像头开启 → 自动启动实时框

// stopCamera 里：
stopAutoDetect();
stopFaceTracking();                            // 关闭摄像头 → 停止循环

// 组件卸载时（离开页面）：
onBeforeUnmount(() => {
  stopCamera();                                // 释放摄像头 + 停止所有循环
});
```

**解析**：三条路径（开启/关闭/离开页面）都串起了摄像头与实时框的生命周期，保证页面切换或组件销毁后不会残留 `requestAnimationFrame` 循环或占用摄像头设备。

---

## 六、人脸年龄识别（前端本地，关键代码解析）

### 1. 状态与常量

```js
let faceAges = [];                 // 年龄预测结果缓存，与 canvas.faces 并行（按下标对应）
let ageNetReady = false;           // ageGenderNet 是否加载成功
let lastAgeUpdateTime = 0;         // 上次年龄更新时刻
const AGE_UPDATE_INTERVAL = 600;   // 年龄更新间隔 ms（低于人脸框的120ms，避免拖慢实时性）
const MIN_AGE_FACE_SIZE = 40;      // 人脸框最小边长，过小的脸跳过年龄预测（结果不可靠且省算力）
```

**解析**：`faceAges` 是普通模块变量（非 Vue ref），因为只被 `drawFaceBoxes` 读取、不进模板，无需响应式；若用 ref 每 600ms 赋值会触发整组件重渲染，浪费性能。

### 2. 模型加载（与 tiny face detector 一起，失败不影响框）

```js
const initFaceDetector = async () => {
  if (!faceDetectorReady.value) {          // 实时框模型
    try {
      await faceapi.nets.tinyFaceDetector.loadFromUri('/models');
      faceDetectorReady.value = true;
    } catch (e) {
      console.error('人脸检测模型加载失败:', e);
      faceDetectorError.value = '人脸检测模型加载失败，实时框不可用';
      return;
    }
  }
  if (!ageNetReady) {                      // 年龄模型（单独容错）
    try {
      await faceapi.nets.ageGenderNet.loadFromUri('/models');
      ageNetReady = true;
    } catch (e) {
      console.warn('年龄模型加载失败，仅显示人脸框:', e);
    }
  }
};
```

**解析**：年龄模型与实时框模型**分开加载、分开容错**——年龄模型失败只跳过年龄文本，不影响人脸框和情绪识别。`!ageNetReady` 判断放在 tiny 加载成功之后且独立于 `faceDetectorReady`，这样即使 tiny 已加载过，再次 init 时年龄模型仍会重试。

> 关键坑：必须写 `faceapi.nets.ageGenderNet`。该包的 ESM 构建没有顶层 `ageGenderNet` 导出，写成 `faceapi.ageGenderNet` 会在 Vite 构建时报 "ageGenderNet is not exported"。

### 3. 年龄段分桶

```js
const getAgeRange = (age) => {
  if (age < 12) return '儿童';
  if (age < 20) return '青少年';
  if (age < 40) return '青年';
  if (age < 60) return '中年';
  return '老年';
};
```

**解析**：`ageGenderNet` 输出的是**连续年龄值**（`age: number`），按业务需要分桶成 5 个年龄段。分桶阈值可按产品口径随时调整。

### 4. 人脸裁剪 + 年龄预测

```js
const predictFaceAges = async (video, faces) => {
  const results = [];
  for (const f of faces) {
    if (f.width < MIN_AGE_FACE_SIZE || f.height < MIN_AGE_FACE_SIZE) {
      results.push(null);                    // 小脸跳过
      continue;
    }
    try {
      const sx = Math.max(0, Math.round(f.x));   // 坐标取整，防越界
      const sy = Math.max(0, Math.round(f.y));
      const sw = Math.min(video.videoWidth - sx, Math.round(f.width));
      const sh = Math.min(video.videoHeight - sy, Math.round(f.height));
      if (sw < 1 || sh < 1) { results.push(null); continue; }

      const crop = document.createElement('canvas');   // 按原生坐标系裁剪脸部
      crop.width = sw;
      crop.height = sh;
      const cctx = crop.getContext('2d');
      cctx.drawImage(video, sx, sy, sw, sh, 0, 0, sw, sh);

      const pred = await faceapi.nets.ageGenderNet.predictAgeAndGender(crop);
      const age = Math.round(pred.age);
      results.push({
        age,
        ageRange: getAgeRange(age),
        gender: pred.gender,                  // 'male' | 'female'
        genderCn: pred.gender === 'male' ? '男' : '女'
      });
    } catch (e) {
      console.error('年龄预测失败:', e);
      results.push(null);
    }
  }
  return results;
};
```

**解析**：
- `predictAgeAndGender` 的输入是**单张人脸图**，所以必须先按 `box` 把脸从视频原生坐标系裁剪出来，再送入模型；坐标取整并夹在 `[0, video.videoWidth/Height]` 内防止 `drawImage` 越界抛错。
- 返回 `{ age, gender, genderProbability }`：`age` 是连续值（四舍五入成整数展示），`gender` 是 `'male' | 'female'`。
- 逐张脸串行预测（`for...of` + await），避免并发挤占 TensorFlow.js 计算线程。

### 5. 接入检测主循环（节流）

```js
const detections = await faceapi.detectAllFaces(video, options);
const faces = detections.map(d => ({ x: d.box.x, y: d.box.y, width: d.box.width, height: d.box.height, score: d.score }));
const ageDue = ageNetReady && (now - lastAgeUpdateTime >= AGE_UPDATE_INTERVAL);
if (ageDue) {
  lastAgeUpdateTime = now;
  faceAges = await predictFaceAges(video, faces);   // 年龄 600ms 才重算一次
}
canvas.faces = faces;         // 框 120ms 更新，始终最新
drawFaceBoxes();
```

**解析**：人脸框每 120ms 刷新，年龄每 600ms 重算。`ageDue` 只在年龄模型就绪且间隔到点时触发预测；其余帧沿用缓存的 `faceAges`，保证框的实时性不被年龄模型拖慢。

### 6. 画框叠加年龄段文本

```js
for (let i = 0; i < faces.length; i++) {
  const f = faces[i];
  const x = f.x * rect.scale + rect.offsetX;   // 原生坐标 → 屏幕坐标
  const y = f.y * rect.scale + rect.offsetY;
  const w = f.width * rect.scale;
  const h = f.height * rect.scale;
  // ...画绿色框与置信度...

  const ageInfo = faceAges[i];                 // 按下标与框对应
  if (ageInfo) {
    const label = `${ageInfo.genderCn} · ${ageInfo.age}岁 · ${ageInfo.ageRange}`;
    ctx.font = '12px sans-serif';
    const tw = ctx.measureText(label).width;
    const lh = 22;
    let lx = x;
    let ly = y - lh - 4;                       // 默认画在框正上方
    if (ly < 4) ly = y + h + 4;                // 框顶贴边 → 移到框下方
    if (lx + tw > cw - 4) lx = Math.max(4, cw - tw - 4);
    ctx.fillStyle = 'rgba(5, 150, 105, 0.9)';  // 半透明深绿底
    ctx.fillRect(lx, ly, tw + 8, lh);
    ctx.fillStyle = '#ffffff';
    ctx.fillText(label, lx + 4, ly + 4);
  }
}
```

**解析**：`faceAges[i]` 与 `faces[i]` 按下标一一对应（`predictFaceAges` 与 `faces` 并行生成）。文本带半透明底色保证在任意画面上可读；框贴画面顶边时自动下移到框下方防止越界。若该脸年龄数据缺失（小脸/预测失败），只画框、不画文本。

### 7. 生命周期联动

```js
const stopFaceTracking = () => {
  faceTrackingRunning = false;
  if (faceTrackingRafId) { cancelAnimationFrame(faceTrackingRafId); faceTrackingRafId = null; }
  faceAges = [];                // 清空年龄缓存
  lastAgeUpdateTime = 0;
  const canvas = overlayCanvasRef.value;
  if (canvas) { canvas.faces = []; drawFaceBoxes(); }   // 清空残留框与文本
};
```

**解析**：关闭实时框 / 关闭摄像头 / 离开页面时清空 `faceAges` 缓存，避免下次打开摄像头时显示上一轮的陈旧年龄。

### 8. 年龄数据随检测存库

**需求**：实时画面上的年龄仅展示、不入库；但**拍照/自动检测**时，把主脸的性别/年龄/年龄段随请求传给后端写入 `detection_record`（`model_gender` / `model_age` / `model_range` 三列）。

```js
const getMainFaceAgeInfo = () => {
  const canvas = overlayCanvasRef.value;
  const faces = canvas?.faces || [];        // 实时框的最新人脸（原生坐标系）
  if (!faces.length) return {};
  let mainIdx = 0;
  for (let i = 1; i < faces.length; i++) {  // 取面积最大的主脸
    const a = faces[i].width * faces[i].height;
    const b = faces[mainIdx].width * faces[mainIdx].height;
    if (a > b) mainIdx = i;
  }
  const info = faceAges[mainIdx];           // 并行数组，按下标取该脸的年龄数据
  if (!info) return {};
  return { modelGender: info.genderCn, modelAge: info.age, modelRange: info.ageRange };
};
```

`runCameraDetect` 中随请求发送：

```js
const res = await detectionAPI.detect(file, getMainFaceAgeInfo());
```

**解析**：
- 主脸 = 实时框中面积最大的一张（与后端 `findMainFace` 语义一致），性别存中文 `男/女`、年龄存整数、年龄段存 `儿童/青少年/青年/中年/老年`。
- `detectionAPI.detect(file, extra)` 会把这三个值作为**可选表单字段** `modelGender/modelAge/modelRange` 附加到 multipart 请求（见 `api/index.js`）。
- Java 端 `DetectionController.detect` 接收 3 个 `@RequestParam(required=false)`，透传给 `DetectionServiceImpl` 写入记录；**上传模式**（无本地年龄数据）不传则为 NULL。
- Python 服务**零改动**，无需知道这三个字段。

---

## 七、涉及的完整改动文件

| 文件 | 改动内容 |
|---|---|
| `emotion-web/src/views/Home.vue` | 新增摄像头模式 Tab、视频回显、拍照/自动检测、实时人脸框、年龄识别、年龄数据随检测存库（逻辑+模板+样式） |
| `emotion-web/src/api/index.js` | `detectionAPI.detect` 增加 modelGender/modelAge/modelRange 三个可选表单参数 |
| `emotion-web/package.json` | 新增依赖 `@vladmandic/face-api` |
| `emotion-web/public/models/` | 新增 tiny face detector 模型（约193KB）+ age gender 模型（约430KB） |
| `emotion-web/dist/` | 构建产物（`models/` 会被拷贝进去） |
| `emotion-system/.../entity/DetectionRecord.java` | 新增 modelGender/modelAge/modelRange 三个字段 |
| `emotion-system/.../service/DetectionService.java` + `impl/DetectionServiceImpl.java` | detect 方法增加 3 个可选参数并写入记录 |
| `emotion-system/.../controller/DetectionController.java` | detect 接口新增 3 个可选 @RequestParam |

Python（FastAPI）**零改动**；Java 仅透传 3 个可选字段，上传模式不传则为 NULL。

---

## 八、验证与测试

1. 确保后端在线：Java `:8080`、Python `:8001`。
2. `emotion-web` 下执行 `npm run dev`，浏览器访问 `http://localhost:5173`（**必须 localhost 或 HTTPS**，否则 `getUserMedia` 被拒）。
3. 首页 → 「摄像头检测」Tab → 允许摄像头权限 → 画面实时回显，出现绿色实时人脸框；移动/靠近/远离，框平滑跟随且大小变化。
4. **年龄识别**：人脸框上方显示 `男/女 · XX岁 · 年龄段` 文本，随人脸更新（约 600ms 刷新一次）；多张人脸分别显示各自年龄。**框上不显示任何情绪标签。**
5. 点击「拍照检测」→ 结果卡片显示服务端情绪/置信度/建议，历史、统计、天气随之更新；查 `detection_record` 该条记录，`model_gender/model_age/model_range` 有值（摄像头模式）。
6. 开启「自动检测」→ 每 2.5s 自动更新一次结果；记录同样携带 3 个年龄字段。
7. 上传模式检测 → `model_gender/model_age/model_range` 为 NULL（正常）。
8. 关闭「实时人脸框」开关 → 框与年龄标签一并消失；关闭摄像头 → 设备释放（指示灯灭）、循环停止。

---

## 九、已知注意事项 / 后续可优化点

1. **`object-fit` 对齐问题**：当前 `.camera-video` 为 `cover`，坐标换算是按 `contain` 推导的，画面与框可能存在微小偏移；如需严格对齐请改为 `contain`（见第五节第4点）。
2. **年龄精度**：`ageGenderNet` 输出为连续年龄回归值，对小孩/老人边界估计偏弱，年龄段划分是粗略的，仅适合"大致年龄画像"场景。
3. **存储口径**：`model_gender/model_age/model_range` 来自前端本地模型估算值，服务端仅存原始值、不校验不计算；若后续要严格口径，可考虑改为 Python 服务端新增年龄/性别模型计算（需新增模型，工作量较大）。
4. **记录策略**：目前自动检测与上传一样每次都存库，实时/自动场景会产生大量历史记录；可按"情绪变化才存库"或"实时模式不存库"策略优化。
5. **帧率调优**：`FACE_DETECT_INTERVAL`、`AGE_UPDATE_INTERVAL` 与 `inputSize` 可按设备性能调整（CPU 弱可降低频率或 `inputSize: 224`）。
6. **局域网部署**：非 HTTPS 环境下摄像头不可用，生产部署需配置 HTTPS 证书。
7. **IP 摄像头（RTSP）**：浏览器 `getUserMedia` 无法直接访问 IP 摄像头，如需支持需服务端拉流（如 Python `cv2.VideoCapture('rtsp://...')` + 转 MJPEG/WebSocket 转发）。
