<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h2>用户反馈管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索反馈标题 / 用户"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="onSearch"
      />
      <el-select
        v-model="searchForm.status"
        placeholder="处理状态"
        clearable
        style="width: 140px"
        @change="onSearch"
      >
        <el-option
          v-for="opt in STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="onReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="dataList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />

      <el-table-column prop="title" label="反馈标题" min-width="180" show-overflow-tooltip />

      <el-table-column prop="username" label="用户" width="120" show-overflow-tooltip />

      <el-table-column label="提交时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="处理状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="plain" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewDetail(row)">
            查看详情
          </el-button>
          <el-button
            v-if="row.status === FEEDBACK_STATUS.PENDING"
            link
            type="warning"
            @click="handleReview(row)"
          >
            审核
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import {
  getFeedbackList,
  deleteFeedback,
  type FeedbackItem,
  type FeedbackListParams,
} from '@/api/modules/feedback'

defineOptions({ name: 'FeedbackList' })

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

/** 状态筛选选项 */
const STATUS_OPTIONS = Object.entries(statusMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value),
}))

// ─── 响应式状态 ───

const router = useRouter()

const loading = ref(false)
const dataList = ref<FeedbackItem[]>([])

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [10, 20, 50],
  layout: 'total, sizes, prev, pager, next, jumper' as const,
})

// ─── 工具函数 ───

function statusType(val: number | string): string {
  return statusMap[Number(val)]?.type ?? 'info'
}

function statusLabel(val: number | string): string {
  return statusMap[Number(val)]?.label ?? String(val ?? '-')
}

/** 格式化日期 */
function formatDate(date: string | undefined | null): string {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// ─── 数据加载 ───

async function fetchList() {
  loading.value = true
  try {
    const params: FeedbackListParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.status !== undefined) params.status = searchForm.status

    const res = await getFeedbackList(params)
    dataList.value = res.list ?? []
    pagination.total = res.total ?? 0
  } catch {
    dataList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/** 搜索 */
function onSearch() {
  pagination.page = 1
  fetchList()
}

/** 重置搜索 */
function onReset() {
  searchForm.keyword = ''
  searchForm.status = undefined
  pagination.page = 1
  fetchList()
}

/** 分页 size 变化 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

/** 分页 page 变化 */
function handleCurrentChange(page: number) {
  pagination.page = page
  fetchList()
}

// ─── 操作 ───

/** 查看详情 */
function handleViewDetail(row: FeedbackItem) {
  router.push(`/feedback/${row.id}`)
}

/** 审核（跳转审核页） */
function handleReview(row: FeedbackItem) {
  router.push(`/feedback/${row.id}`)
}

/** 删除 */
async function handleDelete(row: FeedbackItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.username}」的反馈「${row.title}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  try {
    await deleteFeedback(row.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // 静默处理
  }
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

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
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
