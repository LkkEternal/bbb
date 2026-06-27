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
            :preview-src-list="feedback.imageUrls || [feedback.imageUrl]"
          />
        </div>
        <div class="detail-right">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="农户">{{ feedback.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ feedback.phone }}</el-descriptions-item>
            <el-descriptions-item label="果园">{{ feedback.orchardName }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ feedback.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ feedback.description }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :color="FEEDBACK_STATUS_MAP[feedback.status]?.color" effect="dark" size="small" style="border: none">
                {{ FEEDBACK_STATUS_MAP[feedback.status]?.label }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="训练集标签">{{ feedback.datasetTag || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">审核操作</el-divider>
          <div class="review-actions">
            <el-input v-model="reviewReason" type="textarea" :rows="3" placeholder="审核意见（驳回时必填）" />
            <div class="action-buttons">
              <el-button type="success" @click="handleReview('accept')" :loading="loading">采纳</el-button>
              <el-button type="primary" @click="handleReview('revise')" :loading="loading">修正</el-button>
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
import { FEEDBACK_STATUS_MAP } from '@/constants/enums'

const route = useRoute()
const router = useRouter()
const feedback = ref<any>(null)
const reviewReason = ref('')
const loading = ref(false)

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
      comment: reviewReason.value || undefined,
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

.review-actions {
  .action-buttons {
    margin-top: 16px;
    display: flex;
    gap: 12px;
  }
}
</style>
