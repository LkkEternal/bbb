<template>
  <div class="page-container welcome-page">
    <!-- 顶部欢迎区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">欢迎使用</div>
        <h1 class="hero-title">脐橙智慧种植监测平台</h1>
        <p class="hero-desc">
          一站式管理你的脐橙果园，实时监测环境数据、智能识别病虫害、
          科学管理农事活动，让种植更高效、更智能。
        </p>
        <el-button
          type="primary"
          size="large"
          :icon="Plus"
          class="hero-btn"
          @click="handleCreateOrchard"
        >
          创建我的果园
        </el-button>
      </div>
      <div class="hero-illustration">
        <el-icon class="hero-icon"><Orange /></el-icon>
      </div>
    </div>

    <!-- 功能介绍 -->
    <div class="features-section">
      <h2 class="section-title">核心功能</h2>
      <p class="section-subtitle">四大模块助力脐橙种植全周期智慧化管理</p>

      <el-row :gutter="24" class="features-grid">
        <el-col
          v-for="feature in features"
          :key="feature.title"
          :xs="24"
          :sm="12"
          :lg="6"
        >
          <el-card
            shadow="hover"
            class="feature-card"
            :body-style="{ padding: '28px 24px' }"
          >
            <div class="feature-icon" :style="{ background: feature.bg }">
              <el-icon :size="28" :color="feature.color">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 引导创建果园 -->
    <div class="cta-section">
      <el-card shadow="never" class="cta-card">
        <h2 class="cta-title">准备好了吗？</h2>
        <p class="cta-desc">
          创建你的第一个果园，开启智慧种植之旅。只需填写基本信息，
          即可快速完成果园注册，开始享受全方位的种植管理服务。
        </p>
        <el-button
          type="primary"
          size="large"
          :icon="Plus"
          round
          @click="handleCreateOrchard"
        >
          立即创建果园
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Orange, Monitor, WarningFilled, Calendar, Setting } from '@element-plus/icons-vue'

defineOptions({ name: 'WelcomeView' })

interface Feature {
  title: string
  desc: string
  icon: object
  bg: string
  color: string
}

const features: Feature[] = [
  {
    title: '环境监测',
    desc: '实时采集温湿度、光照、土壤等环境数据，智能预警异常变化，随时随地掌握果园微气候。',
    icon: Monitor,
    bg: 'linear-gradient(135deg, #e8f5e9, #c8e6c9)',
    color: '#2e7d32',
  },
  {
    title: '病虫害识别',
    desc: '基于 AI 图像识别技术，拍照即可快速诊断脐橙病害，提供精准防治建议与用药方案。',
    icon: WarningFilled,
    bg: 'linear-gradient(135deg, #fff3e0, #ffe0b2)',
    color: '#e65100',
  },
  {
    title: '农事日历',
    desc: '根据脐橙物候期与节气自动生成农事建议，科学安排施肥、修剪、灌溉等种植活动。',
    icon: Calendar,
    bg: 'linear-gradient(135deg, #e3f2fd, #bbdefb)',
    color: '#1565c0',
  },
  {
    title: '智能管理',
    desc: '统一管理果园信息、设备、成员与识别记录，数据可视化分析，辅助种植决策。',
    icon: Setting,
    bg: 'linear-gradient(135deg, #f3e5f5, #e1bee7)',
    color: '#6a1b9a',
  },
]

function handleCreateOrchard() {
  // TODO: 跳转至创建果园页面
  // this.$router.push('/orchard/create')
}
</script>

<style scoped lang="scss">
.welcome-page {
  max-width: 1100px;
  margin: 0 auto;
}

// ===== 顶部欢迎区域 =====

.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  padding: 48px 0 32px;
}

.hero-content {
  flex: 1;
}

.hero-badge {
  display: inline-block;
  padding: 4px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-radius: 20px;
  margin-bottom: 16px;
}

.hero-title {
  margin: 0 0 16px;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.25;
  color: var(--el-text-color-primary);
}

.hero-desc {
  margin: 0 0 28px;
  font-size: 16px;
  line-height: 1.7;
  color: var(--el-text-color-secondary);
  max-width: 520px;
}

.hero-btn {
  font-size: 16px;
  padding: 12px 32px;
  border-radius: 8px;
}

.hero-illustration {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 180px;
  background: linear-gradient(135deg, var(--el-color-primary-light-9), var(--el-color-primary-light-7));
  border-radius: 50%;
}

.hero-icon {
  font-size: 80px;
  color: var(--el-color-primary);
}

// ===== 功能介绍 =====

.features-section {
  padding: 32px 0 40px;
}

.section-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  color: var(--el-text-color-primary);
}

.section-subtitle {
  margin: 0 0 32px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.features-grid {
  .el-col {
    margin-bottom: 24px;
  }
}

.feature-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  transition: transform 0.25s, box-shadow 0.25s;
  cursor: default;

  &:hover {
    transform: translateY(-4px);
  }
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  margin-bottom: 18px;
}

.feature-title {
  margin: 0 0 10px;
  font-size: 17px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.feature-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--el-text-color-secondary);
}

// ===== 引导创建果园 =====

.cta-section {
  padding: 8px 0 48px;
}

.cta-card {
  text-align: center;
  padding: 48px 24px;
  background: linear-gradient(135deg, var(--el-color-primary-light-9), #f0f9ff);
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 16px;
}

.cta-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.cta-desc {
  margin: 0 auto 28px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--el-text-color-secondary);
  max-width: 500px;
}
</style>
