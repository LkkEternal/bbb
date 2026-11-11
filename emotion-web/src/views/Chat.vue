<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElDialog } from 'element-plus';
import { assessmentAPI } from '@/api';

const questionnaires = ref([]);
const historyRecords = ref([]);
const currentStep = ref('select');
const currentQuestion = ref(null);
const assessmentProgress = ref({ current: 0, total: 0 });
const assessmentResult = ref(null);
const currentRecordId = ref(null);
const selectedOption = ref('');
const isTyping = ref(false);
const submitting = ref(false);
const showHistory = ref(false);

const levelMap = {
  normal: { name: '正常', color: '#10b981', bg: '#d1fae5' },
  mild: { name: '轻度', color: '#f59e0b', bg: '#fef3c7' },
  moderate: { name: '中度', color: '#f97316', bg: '#ffedd5' },
  severe: { name: '中重度', color: '#ef4444', bg: '#fee2e2' },
  extremely_severe: { name: '重度', color: '#b91c1c', bg: '#fecaca' }
};

const loadQuestionnaires = async () => {
  try {
    const res = await assessmentAPI.getList();
    questionnaires.value = res.data.data.questionnaires;
  } catch {
    ElMessage.error('加载问卷列表失败');
  }
};

const loadHistory = async () => {
  try {
    const res = await assessmentAPI.getHistory({ page: 1, size: 20 });
    historyRecords.value = res.data.data.records;
  } catch {
    // silently fail
  }
};

const selectQuestionnaire = async (q) => {

  currentStep.value = 'starting';
  isTyping.value = true;
  try {
    const res = await assessmentAPI.start({ questionnaireId: q.id });
    const data = res.data.data;
    currentRecordId.value = data.recordId;
    currentStep.value = 'answering';
    currentQuestion.value = data.currentQuestion;
    assessmentProgress.value = { current: 1, total: data.totalQuestions };
    selectedOption.value = '';
  } catch (e) {
    ElMessage.error(e.message || '开始评估失败');
    currentStep.value = 'select';
  } finally {
    isTyping.value = false;
  }
};

const canGoBack = computed(() =>
  currentStep.value === 'answering' && assessmentProgress.value.current > 1 && !isTyping.value
);

const goBack = async () => {
  if (isTyping.value) return;
  isTyping.value = true;
  try {
    const res = await assessmentAPI.prev({ recordId: currentRecordId.value });
    const data = res.data.data;
    currentQuestion.value = data.currentQuestion;
    assessmentProgress.value = data.progress;
    selectedOption.value = '';
  } catch (e) {
    ElMessage.error(e.message || '回退失败');
  } finally {
    isTyping.value = false;
  }
};

const chooseOption = async (label) => {
  if (isTyping.value) return;
  selectedOption.value = label;
  isTyping.value = true;
  try {
    const res = await assessmentAPI.answer({
      recordId: currentRecordId.value,
      questionId: currentQuestion.value.questionId,
      selectedOption: label
    });
    isTyping.value = false;
    if (res.data.data.allAnswered) {
      assessmentResult.value = res.data.data.result;
      currentStep.value = 'allAnswered';
      loadHistory();
    } else if (res.data.data.finished) {
      assessmentResult.value = res.data.data.result;
      currentStep.value = 'result';
      loadHistory();
    } else {
      currentQuestion.value = res.data.data.currentQuestion;
      assessmentProgress.value = res.data.data.progress;
      selectedOption.value = '';
    }
  } catch (e) {
    ElMessage.error(e.message || '提交答案失败');
    selectedOption.value = '';
    isTyping.value = false;
  }
};

const handleSubmit = async () => {
  submitting.value = true;
  try {
    const res = await assessmentAPI.submit({ recordId: currentRecordId.value });
    assessmentResult.value = res.data.data.result;
    currentStep.value = 'result';
    loadHistory();
  } catch (e) {
    ElMessage.error(e.message || '提交失败');
  } finally {
    submitting.value = false;
  }
};

const resetAssessment = () => {
  currentStep.value = 'select';
  currentQuestion.value = null;
  assessmentProgress.value = { current: 0, total: 0 };
  assessmentResult.value = null;
  currentRecordId.value = null;
  selectedOption.value = '';
};

const getLevelStyle = (code) => {
  const l = levelMap[code] || { color: '#6b7280', bg: '#f3f4f6' };
  return { color: l.color, background: l.bg };
};

onMounted(() => {
  loadQuestionnaires();
  loadHistory();
});
</script>

<template>
  <div class="chat-page">
    <main class="chat-container">
      <div class="page-header">
        <h2>知心助手</h2>
        <button v-if="historyRecords.length > 0" @click="showHistory = true" class="btn-history">
          历史记录
        </button>
      </div>

      <div v-if="currentStep === 'select'" class="intro-card">
        <div class="intro-header">
          <div class="intro-header-left">
            <div class="intro-badge">
              <span class="badge-icon">🧠</span>
            </div>
            <div class="intro-title-group">
              <h1 class="intro-title">知心助手</h1>
              <p class="intro-subtitle">采用国际标准心理测评量表，科学评估您的心理健康状态</p>
            </div>
          </div>
        </div>

        <div class="intro-features">
          <div class="feature-item">
            <div class="feature-icon-wrap icon-violet">
              <span>📋</span>
            </div>
            <div class="feature-body">
              <div class="feature-name">专业量表</div>
              <div class="feature-desc">科学权威</div>
            </div>
          </div>
          <div class="feature-divider"></div>
          <div class="feature-item">
            <div class="feature-icon-wrap icon-emerald">
              <span>🔒</span>
            </div>
            <div class="feature-body">
              <div class="feature-name">隐私保护</div>
              <div class="feature-desc">仅自己可见</div>
            </div>
          </div>
          <div class="feature-divider"></div>
          <div class="feature-item">
            <div class="feature-icon-wrap icon-amber">
              <span>⚡</span>
            </div>
            <div class="feature-body">
              <div class="feature-name">快速便捷</div>
              <div class="feature-desc">随时在线测评</div>
            </div>
          </div>
          <div class="feature-divider"></div>
          <div class="feature-item">
            <div class="feature-icon-wrap icon-rose">
              <span>📊</span>
            </div>
            <div class="feature-body">
              <div class="feature-name">详细报告</div>
              <div class="feature-desc">多维度分析</div>
            </div>
          </div>
        </div>

        <div class="intro-disclaimer">
          <span class="disclaimer-label">免责声明</span>
          <span class="disclaimer-text">测评结果仅供参考，不能替代专业医生的诊断。如有困惑可拨打</span>
          <span class="disclaimer-hotline">全国统一心理援助热线：12356</span>
        </div>
      </div>

      <div v-if="currentStep === 'select'" class="questionnaire-list">
        <div
          v-for="q in questionnaires"
          :key="q.id"
          class="questionnaire-card"
          @click="selectQuestionnaire(q)"
        >
          <div class="q-icon">📋</div>
          <div class="q-info">
            <div class="q-header">
              <span class="q-name">{{ q.name }}</span>
              <span class="q-code">{{ q.code }}</span>
            </div>
            <p class="q-desc">{{ q.description || '暂无描述' }}</p>
            <span class="q-count">共 {{ q.totalQuestions }} 题 · 约 {{ Math.ceil(q.totalQuestions * 0.5) }} 分钟</span>
          </div>
        </div>
      </div>

      <div v-if="currentStep === 'starting'" class="status-tip">
        <div class="typing-dots">
          <span></span><span></span><span></span>
        </div>
        <span>正在加载题目...</span>
      </div>

      <div v-if="currentStep === 'answering' || currentStep === 'allAnswered' || currentStep === 'result'" class="chat-messages">
        <div v-if="currentStep === 'answering'" class="question-panel">
          <div class="progress-section">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (assessmentProgress.current / assessmentProgress.total * 100) + '%' }"></div>
            </div>
            <span class="progress-text">{{ assessmentProgress.current }} / {{ assessmentProgress.total }}</span>
          </div>

          <div class="nav-actions">
            <button :disabled="!canGoBack" @click="goBack" class="btn-nav">
              ← 上一题
            </button>
            <button @click="resetAssessment" class="btn-nav btn-back-list">
              返回列表
            </button>
          </div>

          <div class="question-body">
            <div class="q-number">第 {{ currentQuestion.questionIndex }} 题</div>
            <div class="q-text">{{ currentQuestion.questionText }}</div>
            <div class="q-hint">请根据您的真实情况选择，不要过度思考，第一直觉往往最准确</div>
          </div>

          <div class="options-list">
            <button
              v-for="opt in currentQuestion.options"
              :key="opt.label"
              class="option-btn"
              :class="{ selected: selectedOption === opt.label, loading: isTyping }"
              :disabled="isTyping"
              @click="chooseOption(opt.label)"
            >
              <span class="opt-label">{{ opt.label }}</span>
              <span class="opt-text">{{ opt.text }}</span>
            </button>
          </div>

          <div v-if="isTyping" class="typing-indicator">
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
            <span>正在提交...</span>
          </div>
        </div>

        <div v-if="currentStep === 'allAnswered' && assessmentResult" class="result-panel">
          <div class="result-header">
            <span class="result-icon">✅</span>
            <span>答题完成</span>
          </div>

          <div class="result-score-row">
            <div class="score-item">
              <span class="score-label">总分</span>
              <span class="score-value">{{ assessmentResult.totalScore }}</span>
            </div>
            <div class="score-divider"></div>
            <div class="score-item">
              <span class="score-label">等级</span>
              <span class="score-level" :style="getLevelStyle(assessmentResult.levelCode)">
                {{ assessmentResult.levelName }}
              </span>
            </div>
          </div>

          <div class="result-actions">
            <button @click="handleSubmit" :disabled="submitting" class="btn-primary">
              {{ submitting ? '提交中...' : '提交结果' }}
            </button>
            <button @click="resetAssessment" class="btn-secondary">返回列表</button>
          </div>
        </div>

        <div v-if="currentStep === 'result' && assessmentResult" class="result-panel">
          <div class="result-header">
            <span class="result-icon">📊</span>
            <span>评估完成</span>
          </div>

          <div class="result-score-row">
            <div class="score-item">
              <span class="score-label">总分</span>
              <span class="score-value">{{ assessmentResult.totalScore }}</span>
            </div>
            <div class="score-divider"></div>
            <div class="score-item">
              <span class="score-label">等级</span>
              <span class="score-level" :style="getLevelStyle(assessmentResult.levelCode)">
                {{ assessmentResult.levelName }}
              </span>
            </div>
          </div>

          <div class="result-advice">
            <div class="advice-header">💡 建议</div>
            <p class="advice-text">{{ assessmentResult.advice }}</p>
          </div>

          <div class="result-actions">
            <button @click="resetAssessment" class="btn-primary">重新评估</button>
            <button @click="resetAssessment" class="btn-secondary">返回问卷列表</button>
            <button @click="showHistory = true" class="btn-secondary">查看历史</button>
          </div>
        </div>
      </div>

      <div v-if="currentStep === 'select' && questionnaires.length === 0" class="empty-tip">
        <p>暂无可用的评估问卷</p>
      </div>
    </main>

    <ElDialog v-model="showHistory" title="评估历史" width="680px" :close-on-click-modal="true">
      <div v-if="historyRecords.length === 0" class="history-empty">暂无评估记录</div>
      <div v-else class="history-list">
        <div
          v-for="r in historyRecords"
          :key="r.recordId"
          class="history-item"
        >
          <div class="history-left">
            <div class="history-name">{{ r.questionnaireName || r.questionnaireCode }}</div>
            <div class="history-time">{{ r.completedAt }}</div>
            <div v-if="r.adviceText" class="history-advice">{{ r.adviceText }}</div>
          </div>
          <div class="history-right">
            <span class="history-score">{{ r.totalScore }} 分</span>
            <span class="history-level" :style="getLevelStyle(r.levelCode)">{{ r.levelName }}</span>
          </div>
        </div>
      </div>
    </ElDialog>
  </div>
</template>

<style scoped>
.chat-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e0e7ff 0%, #fce7f3 100%);
  padding-left: var(--page-offset);
}

.chat-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.btn-history {
  padding: 8px 18px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  color: #6366f1;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-history:hover {
  border-color: #6366f1;
  background: #f5f3ff;
}

/* Intro Card */
.intro-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(99, 102, 241, 0.1), 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  margin-bottom: 20px;
  border: 1px solid rgba(99, 102, 241, 0.08);
}

.intro-header {
  padding: 24px 28px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.06) 0%, rgba(118, 75, 162, 0.04) 100%);
  border-bottom: 1px solid #f1f5f9;
}

.intro-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.intro-badge {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
  flex-shrink: 0;
}

.badge-icon {
  font-size: 26px;
}

.intro-title-group {
  flex: 1;
}

.intro-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e1b4b;
  margin: 0 0 4px;
  letter-spacing: 0.5px;
}

.intro-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.intro-features {
  display: flex;
  align-items: stretch;
  padding: 16px 8px;
}

.feature-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 12px;
  transition: background 0.25s ease;
}

.feature-item:hover {
  background: #f8fafc;
}

.feature-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.feature-item:hover .feature-icon-wrap {
  transform: scale(1.08);
}

.icon-violet {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  color: #7c3aed;
}

.icon-emerald {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
}

.icon-amber {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.icon-rose {
  background: linear-gradient(135deg, #ffe4e6 0%, #fecdd3 100%);
  color: #e11d48;
}

.feature-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.feature-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
}

.feature-desc {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

.feature-divider {
  width: 1px;
  background: linear-gradient(180deg, transparent 0%, #e2e8f0 30%, #e2e8f0 70%, transparent 100%);
  margin: 8px 0;
}

.intro-disclaimer {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 14px 28px;
  background: #fafbfc;
  border-top: 1px solid #f1f5f9;
  font-size: 12px;
  line-height: 1.6;
}

.disclaimer-label {
  font-weight: 600;
  color: #6b7280;
  padding: 2px 8px;
  background: #f3f4f6;
  border-radius: 4px;
  font-size: 11px;
}

.disclaimer-text {
  color: #9ca3af;
}

.disclaimer-hotline {
  font-weight: 700;
  color: #6366f1;
  padding: 2px 8px;
  background: #eef2ff;
  border-radius: 4px;
  letter-spacing: 0.3px;
}

/* Questionnaire Cards */
.questionnaire-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.questionnaire-card {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 22px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.questionnaire-card:hover {
  border-color: #6366f1;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.12);
  transform: translateY(-2px);
}

.q-icon {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;
}

.q-info {
  flex: 1;
  min-width: 0;
}

.q-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.q-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.q-code {
  font-size: 11px;
  background: #e0e7ff;
  color: #6366f1;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.q-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.q-count {
  font-size: 12px;
  color: #9ca3af;
}

/* Status Tip */
.status-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  justify-content: center;
  color: #6b7280;
  font-size: 14px;
}

/* Question Panel */
.chat-messages {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.question-panel {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 4px;
  transition: width 0.4s ease;
}

.progress-text {
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  white-space: nowrap;
}

.nav-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.btn-nav {
  padding: 6px 16px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 8px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-nav:hover:not(:disabled) {
  border-color: #6366f1;
  color: #6366f1;
  background: #f5f3ff;
}

.btn-nav:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-back-list {
  margin-left: auto;
}

.question-body {
  margin-bottom: 24px;
}

.q-number {
  font-size: 12px;
  font-weight: 600;
  color: #6366f1;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.q-text {
  font-size: 18px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.6;
  margin-bottom: 10px;
}

.q-hint {
  font-size: 13px;
  color: #9ca3af;
  line-height: 1.5;
  padding: 10px 14px;
  background: #f0fdf4;
  border-radius: 8px;
  border-left: 3px solid #86efac;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  font-size: 15px;
  width: 100%;
}

.option-btn:hover:not(:disabled) {
  border-color: #6366f1;
  background: #f5f3ff;
}

.option-btn.selected {
  border-color: #6366f1;
  background: #eef2ff;
}

.option-btn.loading {
  opacity: 0.6;
  cursor: not-allowed;
}

.opt-label {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e0e7ff;
  color: #6366f1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.option-btn.selected .opt-label {
  background: #6366f1;
  color: white;
}

.opt-text {
  color: #374151;
  font-weight: 500;
}

/* Result Panel */
.result-panel {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.result-header {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.result-header .result-icon {
  font-size: 28px;
}

.result-score-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  margin-bottom: 28px;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.score-label {
  font-size: 13px;
  color: #6b7280;
}

.score-value {
  font-size: 40px;
  font-weight: 800;
  color: #1f2937;
  line-height: 1;
}

.score-divider {
  width: 1px;
  height: 50px;
  background: #e5e7eb;
}

.score-level {
  font-size: 18px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 20px;
}

.result-advice {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  text-align: left;
  margin-bottom: 28px;
}

.advice-header {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.advice-text {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-primary {
  padding: 12px 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
}

.btn-secondary {
  padding: 12px 36px;
  background: white;
  color: #6366f1;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  border-color: #6366f1;
  background: #f5f3ff;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
  padding: 12px 18px;
  background: #f3f4f6;
  border-radius: 10px;
  color: #6b7280;
  font-size: 13px;
  width: fit-content;
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* Empty */
.empty-tip {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
  font-size: 15px;
}

/* History Dialog */
.history-empty {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
  font-size: 14px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  background: #f9fafb;
  border-radius: 12px;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f3f4f6;
}

.history-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.history-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.history-time {
  font-size: 12px;
  color: #9ca3af;
}

.history-advice {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-left: auto;
}

.history-score {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  white-space: nowrap;
}

.history-level {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
  white-space: nowrap;
}
</style>