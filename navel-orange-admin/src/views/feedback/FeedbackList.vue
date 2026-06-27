<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户反馈</h2>
      <div class="header-actions">
        <el-select
          v-model="statusFilter"
          placeholder="筛选状态"
          clearable
          style="width: 160px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="(val, key) in FEEDBACK_STATUS_MAP"
            :key="key"
            :label="val.label"
            :value="Number(key)"
          />
        </el-select>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出训练集
        </el-button>
      </div>
    </div>

    <div class="feedback-grid" v-loading="loading">
      <div v-for="item in list" :key="item.id" class="feedback-card">
        <div class="card-image">
          <el-image
            :src="item.imageUrl"
            fit="cover"
            :preview-src-list="item.imageUrls || [item.imageUrl]"
            preview-teleported
          />
        </div>
        <div class="card-info">
          <div class="info-item">
            <span class="label">用户</span>
            <span class="value">{{ item.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">果园</span>
            <span class="value">{{ item.orchardName || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">描述</span>
            <span class="value description">{{ item.description || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态</span>
            <el-tag
              :color="FEEDBACK_STATUS_MAP[item.status]?.color"
              effect="dark"
              size="small"
              style="border: none"
            >
              {{ FEEDBACK_STATUS_MAP[item.status]?.label }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">时间</span>
            <span class="value">{{ item.createdAt }}</span>
          </div>
          <div class="card-actions">
            <el-button link type="primary" @click="$router.push(`/feedback/${item.id}`)">详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-text" v-if="!loading && list.length === 0">暂无数据</div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFeedbackList, exportDataset } from '@/api/modules/feedback'
import { FEEDBACK_STATUS_MAP } from '@/constants/enums'
import { ElMessage } from 'element-plus'

interface FeedbackItem {
  id: number
  imageUrl: string
  imageUrls: string[]
  username: string
  orchardName: string
  description: string
  status: number
  createdAt: string
}

const loading = ref(false)
const statusFilter = ref<number | ''>('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<FeedbackItem[]>([])

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const data: any = await getFeedbackList(params)
    list.value = data?.records ?? data ?? []
    total.value = data?.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  currentPage.value = 1
  fetchList()
}

async function handleExport() {
  try {
    const response = await exportDataset()
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `dataset_${new Date().toISOString().slice(0, 10)}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.feedback-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.feedback-card {
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .card-image {
    width: 100%;
    height: 180px;

    .el-image {
      width: 100%;
      height: 100%;
    }
  }

  .card-info {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;

    .info-item {
      display: flex;
      align-items: flex-start;
      font-size: 13px;
      line-height: 1.4;

      .label {
        color: #909399;
        min-width: 36px;
        flex-shrink: 0;
      }

      .value {
        color: #303133;
        word-break: break-all;
      }

      .description {
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }

    .card-actions {
      margin-top: auto;
      display: flex;
      justify-content: flex-end;
      padding-top: 8px;
      border-top: 1px solid #f0f0f0;
    }
  }
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .feedback-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .feedback-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .feedback-grid {
    grid-template-columns: 1fr;
  }
}
</style>
