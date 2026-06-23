<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/feedback')" content="反馈审核" />

    <el-card v-if="feedback" class="detail-card">
      <div class="feedback-detail">
        <div class="detail-left">
          <el-image
            :src="feedback.imageUrl"
            fit="contain"
            class="feedback-image"
            :preview-src-list="[feedback.imageUrl]"
          />
        </div>
        <div class="detail-right">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="农户">{{ feedback.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ feedback.phone }}</el-descriptions-item>
            <el-descriptions-item label="果园">{{ feedback.orchardName }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ feedback.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ feedback.description }}</el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">AI 诊断结果</el-divider>
          <div class="ai-result">
            <DiseaseTag :code="getDiseaseCode(feedback.aiDiagnosis)" />
            <span class="confidence">置信度: {{ formatPercent(feedback.aiConfidence) }}</span>
            <span class="match-score">人机一致性: {{ formatPercent(feedback.matchScore) }}</span>
          </div>

          <div v-if="feedback.allProbabilities" class="probability-bars">
            <div v-for="(prob, code) in feedback.allProbabilities" :key="code" class="prob-item">
              <span class="prob-label">{{ code }}</span>
              <el-progress :percentage="Math.round(prob * 100)" :stroke-width="16" :text-inside="true" />
            </div>
          </div>

          <el-divider content-position="left">感官问卷</el-divider>
          <div v-if="feedback.quizQuestions" class="quiz-list">
            <div v-for="(q, i) in feedback.quizQuestions" :key="i" class="quiz-item">
              <el-icon :color="q.answer ? '#67c23a' : '#f56c6c'">
                <CircleCheckFilled v-if="q.answer" />
                <CircleCloseFilled v-else />
              </el-icon>
              <span>{{ q.question }}</span>
            </div>
          </div>

          <el-divider content-position="left">审核操作</el-divider>
          <div class="review-actions">
            <el-input v-model="reviewReason" type="textarea" :rows="3" placeholder="审核意见（驳回时必填）" />
            <div class="action-buttons">
              <el-button type="success" @click="handleReview('accept')" :loading="loading">采纳</el-button>
              <el-button type="primary" @click="handleReview('修正')" :loading="loading">修正</el-button>
              <el-button type="danger" @click="handleReview('reject')" :loading="loading">驳回</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFeedbackDetail, reviewFeedback } from '@/api/modules/feedback'
import { DISEASE_LIST } from '@/constants/disease'
import { formatPercent } from '@/utils/format'
import DiseaseTag from '@/components/common/DiseaseTag.vue'

const route = useRoute()
const router = useRouter()
const feedback = ref<any>(null)
const reviewReason = ref('')
const loading = ref(false)

function getDiseaseCode(name: string): string {
  const found = DISEASE_LIST.find(d => d.name === name)
  return found?.code || 'S1'
}

async function loadData() {
  const id = Number(route.params.id)
  try {
    feedback.value = await getFeedbackDetail(id)
  } catch {}
}

async function handleReview(action: string) {
  if (action === 'reject' && !reviewReason.value.trim()) {
    ElMessage.warning('驳回时请填写审核意见')
    return
  }
  loading.value = true
  try {
    await reviewFeedback(feedback.value.id, {
      action: action as any,
      reason: reviewReason.value || undefined,
    })
    ElMessage.success('审核成功')
    router.push('/feedback')
  } catch {} finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.detail-card {
  margin-top: 20px;
}

.feedback-detail {
  display: flex;
  gap: 24px;
}

.detail-left {
  flex-shrink: 0;
  width: 400px;
}

.feedback-image {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  border: 1px solid $border;
}

.detail-right {
  flex: 1;
}

.ai-result {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;

  .confidence, .match-score {
    font-size: 14px;
    color: $text-secondary;
  }
}

.probability-bars {
  margin-bottom: 16px;

  .prob-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .prob-label {
      width: 30px;
      font-size: 12px;
      color: $text-placeholder;
    }

    .el-progress {
      flex: 1;
    }
  }
}

.quiz-list {
  margin-bottom: 16px;

  .quiz-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 0;
    font-size: 14px;
  }
}

.review-actions {
  .action-buttons {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
