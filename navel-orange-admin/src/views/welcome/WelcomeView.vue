<template>
  <div class="welcome-page">
    <div class="welcome-content">
      <!-- 欢迎区 -->
      <div class="welcome-hero">
        <div class="hero-icon">🍊</div>
        <h1 class="hero-title">欢迎使用脐橙监测系统</h1>
        <p class="hero-desc">智能果园管理，从创建第一个果园开始</p>
        <el-button type="primary" size="large" class="cta-btn" @click="$router.push('/orchard?action=create')">
          <el-icon><Plus /></el-icon>
          创建我的第一个果园
        </el-button>
      </div>

      <!-- 新手指南 -->
      <div class="guide-card">
        <h3 class="card-title">
          <el-icon><Guide /></el-icon>
          新手指南
        </h3>
        <div class="guide-steps">
          <div class="step-item">
            <div class="step-num">1</div>
            <div class="step-info">
              <div class="step-name">创建果园</div>
              <div class="step-desc">填写果园名称、地址等基本信息</div>
            </div>
          </div>
          <div class="step-item">
            <div class="step-num">2</div>
            <div class="step-info">
              <div class="step-name">添加IoT设备</div>
              <div class="step-desc">绑定华为云IoTDA平台的传感器设备</div>
            </div>
          </div>
          <div class="step-item">
            <div class="step-num">3</div>
            <div class="step-info">
              <div class="step-name">查看病害识别</div>
              <div class="step-desc">AI自动识别脐橙病虫害并生成报告</div>
            </div>
          </div>
          <div class="step-item">
            <div class="step-num">4</div>
            <div class="step-info">
              <div class="step-name">接收预警通知</div>
              <div class="step-desc">风险预警实时推送，提供防治建议</div>
            </div>
          </div>
        </div>
        <p class="guide-tip">创建果园后即可使用天气、预警、设备管理等全部功能</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Guide } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.welcome-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

.welcome-content {
  max-width: 720px;
  width: 100%;
}

// ── 欢迎区 ────────────────────────────────────────────
.welcome-hero {
  text-align: center;
  margin-bottom: 40px;

  .hero-icon {
    font-size: 56px;
    margin-bottom: 16px;
  }

  .hero-title {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    margin: 0 0 12px 0;
  }

  .hero-desc {
    font-size: 16px;
    color: $text-secondary;
    margin: 0 0 32px 0;
  }

  .cta-btn {
    height: 48px;
    padding: 0 32px;
    font-size: 16px;
    border-radius: 12px;
  }
}

// ── 新手指南 ────────────────────────────────────────────
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  margin: 0 0 20px 0;

  .el-icon {
    color: $primary;
  }
}

.guide-card {
  background: $card;
  border: 1px solid $border;
  border-radius: $radius-lg;
  padding: 24px;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, #7FA87E);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-info {
  flex: 1;
}

.step-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 2px;
}

.step-desc {
  font-size: 12px;
  color: $text-secondary;
  line-height: 1.5;
}

.guide-tip {
  font-size: 12px;
  color: $text-placeholder;
  margin: 0;
  padding-top: 16px;
  border-top: 1px solid $border;
}
</style>
