<template>
  <div class="login-landing">
    <!-- 背景 -->
    <div class="bg" ref="bgRef"></div>
    <div class="orb o1"></div>
    <div class="orb o2"></div>
    <div class="vignette"></div>

    <!-- 浮动几何 -->
    <div class="floats">
      <span class="f1"></span><span class="f2"></span><span class="f3"></span><span class="f4"></span><span class="f5"></span>
    </div>

    <!-- 光标光晕 -->
    <div class="cursor-glow" ref="glowRef"></div>

    <!-- 顶栏 -->
    <div class="header">
      <div class="site-name">Navel Orange Guard</div>
      <button class="btn-login" @click="openDrawer">登录</button>
    </div>

    <!-- 主内容 -->
    <div class="main">
      <div class="rule">
        <span class="line"></span>
        <span class="tag">SMART AGRICULTURE</span>
        <span class="line"></span>
      </div>

      <h1>
        脐橙病虫害<br>
        <span class="g">监测系统</span>
      </h1>

      <p class="sub">
        <span ref="typingRef"></span><span class="cursor"></span>
      </p>

      <div class="metrics">
        <div class="m"><div class="val"><span class="hl">AI</span></div><div class="lbl">智能病害识别</div></div>
        <div class="m"><div class="val"><span class="hl">IoT</span></div><div class="lbl">物联网实时监测</div></div>
        <div class="m"><div class="val"><span class="hl">24h</span></div><div class="lbl">全天候守护</div></div>
      </div>

      <div class="cards" ref="cardRow">
        <div v-for="(item, idx) in cardItems" :key="idx"
          class="card"
          :class="{ wave: waveIdx === idx }"
          @mousemove="onCardMove($event, idx)"
          @mouseleave="onCardLeave(idx)">
          <div class="icon"><span class="sq"></span></div>
          <div class="nm">{{ item.name }}</div>
          <div class="dc">{{ item.desc }}</div>
        </div>
      </div>

      <div class="cta">
        <button class="primary" @click="openDrawer">进入系统</button>
        <button class="ghost" @click="handleLearnMore">了解更多</button>
      </div>
    </div>

    <div class="v">NAVEL ORANGE GUARD v2.0</div>

    <!-- 右侧登录提示 -->
    <div class="login-hint" @click="openDrawer">
      <div class="indicator"></div>
      <div class="tab">登 录</div>
    </div>

    <!-- 遮罩 -->
    <transition name="fade">
      <div v-if="drawerOpen" class="mask" @click="closeDrawer"></div>
    </transition>

    <!-- 侧滑登录面板 -->
    <transition name="drawer">
      <div v-if="drawerOpen" class="drawer">
        <button class="x" @click="closeDrawer">&#10005;</button>
        <div class="fb">
          <div class="top-light"></div>
          <div class="form-logo"><img src="/logo/672041ffe148a7b5195cd57214d35bb4.png" alt="logo" /></div>
          <div class="head">
            <h3>{{ greeting }}</h3>
            <p class="ps">登录以管理您的果园</p>
          </div>
          <div class="fld">
            <span class="pre-icon">&#9670;</span>
            <input type="text" placeholder="手机号" v-model="phone">
          </div>
          <div class="fld">
            <span class="pre-icon">&#9670;</span>
            <input :type="pwdVisible ? 'text' : 'password'" placeholder="密码" v-model="password">
            <span class="pre-icon eye" @click="pwdVisible = !pwdVisible">{{ pwdVisible ? '&#9678;' : '&#9670;' }}</span>
          </div>
          <div class="row">
            <label><input type="checkbox" v-model="remember"> 记住我</label>
            <a href="#">忘记密码？</a>
          </div>
          <div class="go-wrap">
            <button class="go" :class="{ loading: loginLoading }"
              :disabled="loginLoading" @click="handleLogin">
              {{ loginLoading ? '登录中...' : '登 录' }}
            </button>
          </div>
          <p class="ft"><a href="#">还没有账号？立即注册</a></p>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// ── 问候语 ──
const h = new Date().getHours()
let greet = '欢迎回来'
if (h < 6) greet = '夜深了'
else if (h < 9) greet = '早上好'
else if (h < 12) greet = '上午好'
else if (h < 14) greet = '中午好'
else if (h < 18) greet = '下午好'
else if (h < 21) greet = '傍晚好'
else greet = '晚上好'
const greeting = ref(greet)

// ── 表单 ──
const phone = ref('')
const password = ref('')
const remember = ref(true)
const pwdVisible = ref(false)
const drawerOpen = ref(false)
const loginLoading = ref(false)

function openDrawer() { drawerOpen.value = true }
function closeDrawer() {
  drawerOpen.value = false
  loginLoading.value = false
}

async function handleLogin() {
  if (!phone.value || !password.value) return
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (password.value.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  loginLoading.value = true
  try {
    await userStore.loginByPwd(phone.value, password.value)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // Axios 拦截器统一处理错误提示
  } finally {
    loginLoading.value = false
  }
}

function handleLearnMore() {
  ElMessage.info('更多功能展示开发中，敬请期待')
}

// ── 卡片数据 ──

// ── 卡片数据 ──
const cardItems = [
  { name: 'AI 病害识别', desc: '秒级识别多种病虫害' },
  { name: 'IoT 实时监控', desc: '土壤 · 气象 · 生长全感知' },
  { name: '气象环境采集', desc: '精准预报，主动防灾' },
  { name: '预警精准推送', desc: '分级告警，直达手机' },
]

// ── 卡片波浪 ──
const waveIdx = ref(0)
let waveTimer: ReturnType<typeof setInterval> | null = null

// ── 卡片 3D 倾斜 ──
const cardTilts = ref<{ rx: number; ry: number }[]>(
  cardItems.map(() => ({ rx: 0, ry: 0 }))
)

function onCardMove(e: MouseEvent, idx: number) {
  const card = (e.currentTarget as HTMLElement)
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const cx = rect.width / 2
  const cy = rect.height / 2
  card.style.transform = `perspective(500px) rotateX(${(y - cy) / cy * -8}deg) rotateY(${(x - cx) / cx * 8}deg) scale3d(1.02,1.02,1.02)`
}

function onCardLeave(idx: number) {
  const cards = document.querySelectorAll('.card')
  if (cards[idx]) {
    (cards[idx] as HTMLElement).style.transform = 'perspective(500px) rotateX(0) rotateY(0) scale3d(1,1,1)'
  }
}

// ── 打字效果 ──
const typingRef = ref<HTMLElement | null>(null)
function startTyping() {
  const text = '用 AI 守护每一棵脐橙树'
  let i = 0
  const fn = () => {
    if (i <= text.length && typingRef.value) {
      typingRef.value.textContent = text.substring(0, i)
      i++
      setTimeout(fn, 80 + Math.random() * 40)
    }
  }
  setTimeout(fn, 900)
}

// ── 星尘 ──
let starTimer: ReturnType<typeof setInterval> | null = null
function spawnStar() {
  const container = document.createElement('div')
  container.className = 'stardust'
  container.style.left = Math.random() * 100 + '%'
  container.style.top = (60 + Math.random() * 35) + '%'
  const n = 1 + Math.floor(Math.random() * 3)
  const clrs = ['rgba(165,214,167,0.4)', 'rgba(255,255,255,0.2)', 'rgba(92,184,92,0.3)']
  for (let i = 0; i < n; i++) {
    const p = document.createElement('span')
    p.style.background = clrs[Math.floor(Math.random() * clrs.length)]
    p.style.left = (Math.random() - 0.5) * 40 + 'px'
    p.style.animationDuration = (2 + Math.random() * 2) + 's'
    container.appendChild(p)
  }
  document.body.appendChild(container)
  setTimeout(() => container.remove(), 4000)
}

// ── 鼠标视差 ──
const bgRef = ref<HTMLElement | null>(null)
const glowRef = ref<HTMLElement | null>(null)

function onMouseMove(e: MouseEvent) {
  if (bgRef.value) {
    const x = (e.clientX / window.innerWidth - 0.5) * 2
    const y = (e.clientY / window.innerHeight - 0.5) * 2
    bgRef.value.style.transform = `translate(${x * -8}px, ${y * -8}px) scale(1.05)`
  }
  if (glowRef.value) {
    glowRef.value.style.left = e.clientX + 'px'
    glowRef.value.style.top = e.clientY + 'px'
    glowRef.value.style.opacity = '1'
  }
}

function onMouseLeave() {
  if (glowRef.value) glowRef.value.style.opacity = '0'
}

// ── 键盘 ESC ──
function onKeyDown(e: KeyboardEvent) {
  if (e.key === 'Escape') closeDrawer()
}

// ── 生命周期 ──
onMounted(() => {
  startTyping()
  waveTimer = setInterval(() => {
    waveIdx.value = (waveIdx.value + 1) % cardItems.length
  }, 2200)
  starTimer = setInterval(spawnStar, 800)
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseleave', onMouseLeave)
  document.addEventListener('keydown', onKeyDown)
})

onUnmounted(() => {
  if (waveTimer) clearInterval(waveTimer)
  if (starTimer) clearInterval(starTimer)
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseleave', onMouseLeave)
  document.removeEventListener('keydown', onKeyDown)
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.login-landing {
  width: 100%; height: 100%; position: relative; overflow: hidden;
  background: #0a180a;
}

/* 背景 */
.bg {
  position: absolute; inset: -40px;
  background: url('/upload-bg.png') center / cover no-repeat;
  filter: brightness(0.5) saturate(0.9);
  transition: transform 0.08s ease-out;
}

.orb {
  position: absolute; border-radius: 50%; filter: blur(120px); pointer-events: none;
}
.orb.o1 {
  width: 800px; height: 800px; top: -20%; left: -10%;
  background: radial-gradient(circle, rgba(61, 139, 64, 0.12), transparent 70%);
  animation: o1 14s ease-in-out infinite alternate;
}
.orb.o2 {
  width: 500px; height: 500px; bottom: -10%; right: 5%;
  background: radial-gradient(circle, rgba(255, 183, 77, 0.08), transparent 70%);
  animation: o2 12s ease-in-out infinite alternate;
}
@keyframes o1 { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(60px, -80px) scale(1.1); } }
@keyframes o2 { 0% { transform: translate(0, 0) scale(1); } 100% { transform: translate(-40px, 60px) scale(1.15); } }

.vignette {
  position: absolute; inset: 0; pointer-events: none;
  background: radial-gradient(ellipse at 50% 40%, transparent 35%, rgba(10, 24, 10, 0.25) 60%, rgba(10, 24, 10, 0.5) 100%);
}

/* 浮动几何 */
.floats {
  position: absolute; inset: 0; pointer-events: none; z-index: 2; overflow: hidden;
}
.floats span {
  position: absolute; display: block; border: 1px solid rgba(255, 255, 255, 0.015);
  animation: floatDrift linear infinite;
}
.floats .f1 { width: 60px; height: 60px; border-radius: 50%; left: 15%; top: 60%; animation-duration: 25s; opacity: 0.1; }
.floats .f2 { width: 40px; height: 40px; left: 75%; top: 20%; animation-duration: 30s; opacity: 0.06; }
.floats .f3 { width: 30px; height: 30px; left: 85%; top: 70%; animation-duration: 22s; opacity: 0.05; }
.floats .f4 { width: 50px; height: 50px; left: 25%; top: 25%; animation-duration: 28s; opacity: 0.05; }
.floats .f5 { width: 20px; height: 20px; left: 60%; top: 80%; animation-duration: 20s; opacity: 0.08; }
@keyframes floatDrift {
  0% { transform: translateY(0) rotate(0deg); opacity: 0.05; }
  25% { transform: translateY(-80px) rotate(120deg); opacity: 0.1; }
  50% { transform: translateY(-40px) rotate(240deg); opacity: 0.05; }
  75% { transform: translateY(-120px) rotate(360deg); opacity: 0.08; }
  100% { transform: translateY(0) rotate(720deg); opacity: 0.05; }
}

/* 光标光晕 */
.cursor-glow {
  position: fixed; width: 500px; height: 500px; border-radius: 50%;
  background: radial-gradient(circle, rgba(92, 184, 92, 0.03), transparent 70%);
  pointer-events: none; z-index: 1; transform: translate(-50%, -50%);
  transition: opacity 0.3s; opacity: 0;
}

/* 星尘 */
:global(.stardust) {
  position: fixed; pointer-events: none; z-index: 0;
}
:global(.stardust span) {
  position: absolute; width: 3px; height: 3px; border-radius: 50%;
  animation: dustUp linear forwards;
}
@keyframes dustUp {
  0% { transform: translateY(0) scale(1); opacity: 0; }
  20% { opacity: 0.6; }
  80% { opacity: 0.2; }
  100% { transform: translateY(-200px) translateX(30px) scale(0); opacity: 0; }
}

/* 顶栏 */
.header {
  position: absolute; top: 0; left: 0; right: 0; z-index: 5;
  display: flex; justify-content: space-between; align-items: center;
  padding: 28px 48px; animation: hdrIn 0.8s ease;
}
@keyframes hdrIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.header .site-name {
  font-size: 14px; color: #fff; font-weight: 500; letter-spacing: 3px;
}
.header .site-name::before {
  content: ''; display: inline-block; width: 10px; height: 10px;
  background: #5CB85C; border-radius: 50%; margin-right: 10px; vertical-align: middle;
  box-shadow: 0 0 12px rgba(92, 184, 92, 0.2);
  animation: pulseDot 2s ease-in-out infinite;
}
@keyframes pulseDot { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
.header .btn-login {
  padding: 10px 32px; background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 6px;
  color: #fff; font-size: 14px; font-weight: 500;
  cursor: pointer; transition: all 0.3s; letter-spacing: 1px;
}
.header .btn-login:hover { background: rgba(255, 255, 255, 0.12); border-color: rgba(255, 255, 255, 0.15); }

/* 主内容 */
.main {
  position: relative; z-index: 3; width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  text-align: center; padding: 40px;
}
.main .rule { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.main .rule .line { width: 32px; height: 1px; background: #5CB85C; opacity: 0.3; }
.main .rule .tag { font-size: 12px; color: #A5D6A7; letter-spacing: 6px; font-weight: 500; }
.main h1 {
  font-size: 72px; font-weight: 700; color: #fff; line-height: 1.04; letter-spacing: -2px;
}
.main h1 .g {
  background: linear-gradient(135deg, #A5D6A7 20%, #FFCC80 80%);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
  background-size: 200% 200%; animation: shimmer 4s ease-in-out infinite;
}
@keyframes shimmer { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.main .sub {
  font-size: 18px; color: rgba(255, 255, 255, 0.5); letter-spacing: 8px;
  font-weight: 300; margin-top: 10px; min-height: 28px;
}
.main .sub .cursor {
  display: inline-block; width: 2px; height: 20px;
  background: #5CB85C; margin-left: 4px; vertical-align: middle;
  animation: blink 0.8s step-end infinite;
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.main .metrics { display: flex; gap: 56px; margin-top: 40px; }
.main .metrics .m { text-align: center; }
.main .metrics .m .val {
  font-size: 40px; font-weight: 200; color: #fff;
  line-height: 1; margin-bottom: 8px; letter-spacing: 4px;
}
.main .metrics .m .val .hl {
  font-weight: 200; letter-spacing: 6px;
  background: linear-gradient(135deg, #A5D6A7, #81C784);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.main .metrics .m .lbl {
  font-size: 12px; color: rgba(255, 255, 255, 0.35); letter-spacing: 3px; font-weight: 300;
}

.main .cards { display: flex; gap: 12px; margin-top: 40px; perspective: 1000px; }
.main .cards .card {
  flex: 0 0 160px; padding: 24px 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.04); border-radius: 10px;
  text-align: center; cursor: default; position: relative; overflow: hidden;
  transition: all 0.15s ease-out; transform-style: preserve-3d;
}
.main .cards .card::after {
  content: ''; position: absolute; inset: 0; border-radius: 10px;
  background: linear-gradient(135deg, transparent 30%, rgba(92, 184, 92, 0.02) 50%, transparent 70%);
  transform: translateX(-100%); transition: transform 0.5s;
}
.main .cards .card.wave::after { transform: translateX(100%); }
.main .cards .card .icon {
  width: 40px; height: 40px; margin: 0 auto 12px;
  background: rgba(92, 184, 92, 0.06); border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
}
.main .cards .card:hover .icon { background: rgba(92, 184, 92, 0.1); }
.main .cards .card .icon .sq {
  width: 12px; height: 12px; border: 2px solid #5CB85C; border-radius: 3px; opacity: 0.4;
}
.main .cards .card:hover .icon .sq { opacity: 0.8; transform: rotate(90deg); }
.main .cards .card .nm { font-size: 14px; color: #fff; font-weight: 500; margin-bottom: 4px; }
.main .cards .card .dc { font-size: 12px; color: rgba(255, 255, 255, 0.5); font-weight: 300; }

.main .cta { margin-top: 44px; display: flex; gap: 16px; align-items: center; }
.main .cta .primary {
  padding: 18px 64px; background: #5CB85C; border: none; border-radius: 8px;
  color: #fff; font-size: 17px; font-weight: 600; cursor: pointer;
  transition: all 0.3s; letter-spacing: 3px;
  box-shadow: 0 4px 24px rgba(92, 184, 92, 0.15);
}
.main .cta .primary:hover {
  background: #4CAF50; transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(92, 184, 92, 0.25);
}
.main .cta .ghost {
  padding: 18px 24px; background: transparent; border: none;
  color: rgba(255, 255, 255, 0.25); font-size: 15px; font-weight: 400;
  cursor: pointer; transition: color 0.3s;
}
.main .cta .ghost:hover { color: rgba(255, 255, 255, 0.5); }

/* 右侧登录提示 */
.login-hint {
  position: fixed; right: 0; top: 50%; z-index: 8;
  transform: translateY(-50%); cursor: pointer;
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
  display: flex; align-items: center;
}
.login-hint:hover { right: 8px; }
.login-hint .tab {
  writing-mode: vertical-rl; padding: 16px 8px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.02); border-right: none;
  border-radius: 6px 0 0 6px; font-size: 12px;
  color: rgba(255, 255, 255, 0.08); letter-spacing: 3px;
  transition: all 0.3s; backdrop-filter: blur(8px);
}
.login-hint:hover .tab { color: rgba(255, 255, 255, 0.2); background: rgba(255, 255, 255, 0.04); border-color: rgba(255, 255, 255, 0.04); }
.login-hint .indicator {
  width: 3px; height: 24px; background: #5CB85C; border-radius: 2px;
  animation: indPulse 1.5s ease-in-out infinite; margin-right: 4px;
}
@keyframes indPulse { 0%, 100% { opacity: 0.15; } 50% { opacity: 0.4; } }

/* 遮罩 */
.mask {
  position: fixed; inset: 0; z-index: 99; background: rgba(0, 0, 0, 0.15);
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.4s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 侧滑面板 */
.drawer {
  position: fixed; top: 0; right: 0; bottom: 0; z-index: 100;
  width: 480px; background: #f8f6f2;
  display: flex; align-items: center; justify-content: center;
  padding: 48px;
}
.drawer-enter-active { transition: right 0.6s cubic-bezier(0.34, 1.56, 0.64, 1); }
.drawer-leave-active { transition: right 0.3s ease-in; }
.drawer-enter-from { right: -480px; }
.drawer-leave-to { right: -480px; }

.drawer .x {
  position: absolute; top: 28px; right: 28px; width: 36px; height: 36px;
  border: none; background: none; color: #999; font-size: 20px;
  cursor: pointer; transition: all 0.3s; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.drawer .x:hover { color: #333; background: rgba(0, 0, 0, 0.02); }

.fb { width: 100%; max-width: 360px; position: relative; }
.fb::before {
  content: ''; position: absolute; top: -20px; left: -20px; right: -20px; bottom: -20px;
  border: 1px solid rgba(92, 184, 92, 0.02); border-radius: 16px; pointer-events: none;
  animation: borderPulse 4s ease-in-out infinite;
}
@keyframes borderPulse { 0%, 100% { opacity: 0.3; transform: scale(1); } 50% { opacity: 0.6; transform: scale(1.005); } }

.fb .top-light {
  position: absolute; top: -1px; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent 0%, rgba(92, 184, 92, 0.03) 20%, rgba(92, 184, 92, 0.08) 50%, rgba(92, 184, 92, 0.03) 80%, transparent 100%);
  border-radius: 2px; animation: lightSweep 3s ease-in-out infinite;
}
@keyframes lightSweep { 0% { transform: scaleX(0.3); opacity: 0.3; } 50% { transform: scaleX(1); opacity: 1; } 100% { transform: scaleX(0.3); opacity: 0.3; } }

.fb .form-logo {
  width: 112px; height: 112px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(61, 139, 64, 0.04); border-radius: 20px;
  margin: 0 auto 24px; display: flex; align-items: center; justify-content: center;
  padding: 8px;
}
.fb .form-logo img {
  width: 100%; height: 100%; object-fit: contain;
}
.fb .head { margin-bottom: 28px; text-align: center; }
.fb h3 { font-size: 22px; font-weight: 700; color: #1a2e1a; margin-bottom: 4px; }
.fb .ps { font-size: 14px; color: #8a9a8a; margin-bottom: 0; }

.fb .fld { position: relative; margin-bottom: 14px; }
.fb .fld .pre-icon {
  position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
  color: #ccc; font-size: 14px; pointer-events: none; z-index: 2; line-height: 1;
}
.fb .fld .pre-icon.eye {
  left: auto; right: 14px; cursor: pointer; font-size: 10px; color: #bbb; pointer-events: auto;
}
.fb .fld input {
  width: 100%; padding: 16px 16px 16px 44px;
  background: #fff; border: 1px solid #e0dcd4; border-radius: 8px;
  color: #1a2e1a; font-size: 15px; outline: none;
}
.fb .fld input::placeholder { color: #bbb; }
.fb .fld input:focus { border-color: #5CB85C; box-shadow: 0 0 0 3px rgba(92, 184, 92, 0.06); }

.fb .row {
  display: flex; justify-content: space-between; margin: 4px 0 24px; font-size: 14px;
}
.fb .row label { display: flex; align-items: center; gap: 8px; color: #666; cursor: pointer; }
.fb .row input[type="checkbox"] { accent-color: #5CB85C; width: 16px; height: 16px; }
.fb .row a { color: #999; text-decoration: none; }
.fb .row a:hover { color: #5CB85C; }

.fb .go {
  width: 100%; padding: 16px; background: #5CB85C; border: none; border-radius: 8px;
  color: #fff; font-size: 16px; font-weight: 600; cursor: pointer;
  transition: all 0.3s; letter-spacing: 2px;
  box-shadow: 0 4px 20px rgba(92, 184, 92, 0.15);
}
.fb .go:hover { background: #4CAF50; box-shadow: 0 6px 28px rgba(92, 184, 92, 0.25); transform: translateY(-1px); }
.fb .go:active { transform: scale(0.98); }
.fb .go.loading { background: #8a9a8a; pointer-events: none; }
.fb .go.done { background: #5CB85C; pointer-events: none; }

.fb .ft { text-align: center; margin-top: 20px; font-size: 14px; color: #999; }
.fb .ft a { color: #666; text-decoration: none; }
.fb .ft a:hover { color: #5CB85C; }

.v {
  position: absolute; bottom: 28px; left: 48px;
  font-size: 11px; color: rgba(255, 255, 255, 0.25); z-index: 2; letter-spacing: 2px;
}
</style>
