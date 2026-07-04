<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/feedback')">返回</el-button>
      <h2>反馈审核详情</h2>
    </div>

    <!-- 基本信息 -->
    <el-card v-loading="loading" shadow="never" class="detail-card">
      <template #header>
        <span class="card-title">反馈信息</span>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="field in displayFields"
          :key="field.prop"
          :label="field.label"
          :span="field.span || 1"
        >
          <!-- 文本 -->
          <template v-if="field.type === 'text'">
            {{ detail[field.prop] ?? '-' }}
          </template>

          <!-- 标签 -->
          <template v-else-if="field.type === 'tag'">
            <el-tag v-if="detail[field.prop] != null" :type="statusType(detail[field.prop])" effect="plain">
              {{ statusLabel(detail[field.prop]) }}
            </el-tag>
            <span v-else>-</span>
          </template>

          <!-- 日期 -->
          <template v-else-if="field.type === 'date'">
            {{ detail[field.prop] ? dayjs(detail[field.prop]).format(field.format || 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>

          <!-- 图片 -->
          <template v-else-if="field.type === 'image'">
            <div v-if="detail[field.prop] && detail[field.prop].length" class="image-list">
              <el-image
                v-for="(img, idx) in detail[field.prop]"
                :key="idx"
                :src="img"
                style="width: 80px; height: 80px; border-radius: 4px; cursor: pointer"
                :preview-src-list="detail[field.prop]"
                fit="cover"
                class="feedback-image"
              />
            </div>
            <span v-else>-</span>
          </template>

          <!-- 长文本 -->
          <template v-else-if="field.type === 'longtext'">
            <div class="long-text">{{ detail[field.prop] ?? '-' }}</div>
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 审核操作 -->
    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="card-title">审核操作</span>
      </template>

      <div class="review-section">
        <!-- 当前审核状态 -->
        <div class="review-status-bar">
          <span class="review-label">当前状态：</span>
          <el-tag v-if="detail.status != null" :type="statusType(detail.status)" effect="plain" size="large">
            {{ statusLabel(detail.status) }}
          </el-tag>
        </div>

        <!-- 待审核时可操作 -->
        <template v-if="detail.status === FEEDBACK_STATUS.PENDING">
          <el-divider />

          <el-form :model="reviewForm" label-width="90px" class="review-form">
            <el-form-item label="审核备注">
              <el-input
                v-model="reviewForm.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入审核备注（选填）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>

          <div class="review-actions">
            <el-button
              type="success"
              :icon="Check"
              :loading="approving"
              size="large"
              @click="handleApprove"
            >
              审核通过
            </el-button>
            <el-button
              type="danger"
              :icon="Close"
              :loading="rejecting"
              size="large"
              @click="handleReject"
            >
              驳回
            </el-button>
          </div>
        </template>

        <!-- 已审核时仅展示审核人信息 -->
        <template v-else-if="detail.status === FEEDBACK_STATUS.APPROVED || detail.status === FEEDBACK_STATUS.REJECTED">
          <el-descriptions :column="2" border class="review-result">
            <el-descriptions-item label="审核人">
              {{ detail.reviewer ?? '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ detail.reviewedAt ? dayjs(detail.reviewedAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="审核备注" :span="2">
              {{ detail.reviewRemark ?? '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-card>

    <!-- 驳回原因对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回反馈"
      width="480px"
      destroy-on-close
    >
      <el-form :model="rejectForm" label-width="90px">
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, Close } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import {
  getFeedbackDetail,
  approveFeedback,
  rejectFeedback,
  type FeedbackDetail,
} from '@/api/modules/feedback'

defineOptions({ name: 'FeedbackReview' })

// ─── 常量 ───

/** 反馈审核状态枚举 */
const FEEDBACK_STATUS = {
  PENDING: 0,     // 待审核
  APPROVED: 1,    // 审核通过
  REJECTED: 2,    // 已驳回
} as const

/** 状态标签映射 */
const statusMap: Record<number, { label: string; type: string }> = {
  [FEEDBACK_STATUS.PENDING]: { label: '待审核', type: 'warning' },
  [FEEDBACK_STATUS.APPROVED]: { label: '审核通过', type: 'success' },
  [FEEDBACK_STATUS.REJECTED]: { label: '已驳回', type: 'danger' },
}

// ─── 响应式状态 ───

const route = useRoute()

const loading = ref(false)
const approving = ref(false)
const rejecting = ref(false)

const detail = ref<FeedbackDetail>({} as FeedbackDetail)

const rejectDialogVisible = ref(false)

const reviewForm = ref({
  remark: '',
})

const rejectForm = ref({
  reason: '',
})

// ─── 展示字段 ───

const displayFields = [
  { label: '反馈编号', prop: 'id', type: 'text' },
  { label: '用户名称', prop: 'username', type: 'text' },
  { label: '联系电话', prop: 'phone', type: 'text' },
  { label: '邮箱', prop: 'email', type: 'text' },
  { label: '反馈分类', prop: 'categoryName', type: 'tag' },
  { label: '反馈内容', prop: 'content', type: 'longtext', span: 2 },
  { label: '反馈图片', prop: 'images', type: 'image', span: 2 },
  { label: '提交时间', prop: 'createdAt', type: 'date', format: 'YYYY-MM-DD HH:mm:ss' },
  { label: '审核状态', prop: 'status', type: 'tag' },
]

// ─── 工具函数 ───

function statusType(val: number | string): string {
  return statusMap[Number(val)]?.type ?? 'info'
}

function statusLabel(val: number | string): string {
  return statusMap[Number(val)]?.label ?? String(val ?? '-')
}

// ─── 数据加载 ───

async function fetchDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    detail.value = await getFeedbackDetail(id)
  } catch {
    detail.value = {} as FeedbackDetail
  } finally {
    loading.value = false
  }
}

// ─── 审核操作 ───

/** 审核通过 */
async function handleApprove() {
  try {
    await ElMessageBox.confirm('确认审核通过该反馈？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    })
  } catch {
    return
  }

  approving.value = true
  try {
    await approveFeedback({
      id: detail.value.id,
      remark: reviewForm.value.remark || undefined,
    })
    ElMessage.success('审核通过成功')
    await fetchDetail()
  } catch {
    // 静默处理
  } finally {
    approving.value = false
  }
}

/** 打开驳回对话框 */
function handleReject() {
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

/** 确认驳回 */
async function confirmReject() {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  rejecting.value = true
  try {
    await rejectFeedback({
      id: detail.value.id,
      reason: rejectForm.value.reason.trim(),
    })
    ElMessage.success('已驳回该反馈')
    rejectDialogVisible.value = false
    await fetchDetail()
  } catch {
    // 静默处理
  } finally {
    rejecting.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.detail-card {
  margin-bottom: 20px;

  :deep(.el-card__header) {
    padding: 14px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
  }
}

// 图片列表
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feedback-image {
  border: 1px solid var(--el-border-color-lighter);
  transition: border-color 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
  }
}

// 长文本
.long-text {
  white-space: pre-wrap;
  line-height: 1.7;
  max-height: 200px;
  overflow-y: auto;
  padding: 4px 0;
}

// 审核区域
.review-section {
  padding: 4px 0;
}

.review-status-bar {
  display: flex;
  align-items: center;
  gap: 12px;

  .review-label {
    font-size: 14px;
    color: #606266;
  }
}

.review-form {
  margin-top: 8px;
  max-width: 560px;
}

.review-actions {
  display: flex;
  gap: 16px;
  margin-top: 20px;
}

.review-result {
  margin-top: 4px;
}
</style>
