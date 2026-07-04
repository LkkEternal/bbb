<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息管理</h2>
      <div class="header-actions">
        <el-button
          v-if="selectedIds.length > 0"
          type="warning"
          :icon="Check"
          :disabled="loading"
          @click="handleBatchRead"
        >
          批量已读（{{ selectedIds.length }}）
        </el-button>
        <el-button
          v-if="selectedIds.length > 0"
          type="danger"
          :icon="Delete"
          :disabled="loading"
          @click="handleBatchDelete"
        >
          批量删除（{{ selectedIds.length }}）
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索消息标题"
        clearable
        :prefix-icon="Search"
        style="width: 240px"
        @keyup.enter="onSearch"
      />
      <el-select
        v-model="searchForm.type"
        placeholder="消息类型"
        clearable
        style="width: 140px"
        @change="onSearch"
      >
        <el-option label="系统通知" :value="1" />
        <el-option label="告警通知" :value="2" />
        <el-option label="任务提醒" :value="3" />
        <el-option label="个人消息" :value="4" />
      </el-select>
      <el-select
        v-model="searchForm.isRead"
        placeholder="阅读状态"
        clearable
        style="width: 130px"
        @change="onSearch"
      >
        <el-option label="未读" :value="false" />
        <el-option label="已读" :value="true" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="onReset">重置</el-button>
    </div>

    <!-- 消息表格 -->
    <el-table
      ref="tableRef"
      :data="dataList"
      v-loading="loading"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTagType(row.type)" effect="plain" size="small">
            {{ typeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sendTime" label="发送时间" width="170" align="center">
        <template #default="{ row }">
          {{ row.sendTime ? dayjs(row.sendTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="isRead" label="是否已读" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isRead" type="info" effect="plain" size="small">已读</el-tag>
          <el-tag v-else type="danger" effect="dark" size="small">未读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.isRead"
            link
            type="warning"
            :icon="Check"
            @click="handleMarkRead(row)"
          >
            标记已读
          </el-button>
          <el-button
            link
            type="primary"
            :icon="View"
            @click="handleViewDetail(row)"
          >
            查看
          </el-button>
          <el-button
            link
            type="danger"
            :icon="Delete"
            @click="handleDelete(row)"
          >
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="640px" destroy-on-close>
      <div v-if="detailData" class="detail-body">
        <div class="detail-header">
          <h3>{{ detailData.title }}</h3>
          <div class="detail-meta">
            <el-tag :type="typeTagType(detailData.type)" effect="plain" size="small">
              {{ typeLabel(detailData.type) }}
            </el-tag>
            <span class="meta-time">
              {{ detailData.sendTime ? dayjs(detailData.sendTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
            </span>
          </div>
        </div>
        <el-divider />
        <div class="detail-content">{{ detailData.content }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Search, Refresh, Check, Delete, View } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import {
  getMessageList,
  markRead,
  deleteMessage,
  batchDeleteMessage,
  type MessageItem,
  type MessageListParams,
} from '@/api/modules/message'

defineOptions({ name: 'MessageViewList' })

/** 类型标签映射 */
const typeMap: Record<number, { label: string; type: string }> = {
  1: { label: '系统通知', type: 'primary' },
  2: { label: '告警通知', type: 'danger' },
  3: { label: '任务提醒', type: 'warning' },
  4: { label: '个人消息', type: 'info' },
}

function typeLabel(val: number): string {
  return typeMap[val]?.label ?? '未知'
}

function typeTagType(val: number): string {
  return typeMap[val]?.type ?? 'info'
}

// ===== 状态 =====
const loading = ref(false)
const dataList = ref<MessageItem[]>([])
const selectedIds = ref<number[]>([])
const tableRef = ref()

const searchForm = reactive({
  keyword: '',
  type: undefined as number | undefined,
  isRead: undefined as boolean | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<MessageItem | null>(null)

// ===== 方法 =====

/** 获取列表 */
async function fetchList() {
  loading.value = true
  try {
    const params: MessageListParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.type !== undefined) params.type = searchForm.type
    if (searchForm.isRead !== undefined) params.isRead = searchForm.isRead

    const res = await getMessageList(params)
    dataList.value = res.list
    pagination.total = res.total
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
  searchForm.type = undefined
  searchForm.isRead = undefined
  pagination.page = 1
  fetchList()
}

/** 选中变化 */
function handleSelectionChange(rows: MessageItem[]) {
  selectedIds.value = rows.map(r => r.id)
}

/** 单条标记已读 */
async function handleMarkRead(row: MessageItem) {
  try {
    await markRead([row.id])
    ElMessage.success('已标记为已读')
    fetchList()
  } catch {
    // 错误由全局拦截器处理
  }
}

/** 批量标记已读 */
async function handleBatchRead() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要标记的消息')
    return
  }
  try {
    await markRead(selectedIds.value)
    ElMessage.success(`已标记 ${selectedIds.value.length} 条消息为已读`)
    selectedIds.value = []
    fetchList()
  } catch {
    // 错误由全局拦截器处理
  }
}

/** 单条删除 */
async function handleDelete(row: MessageItem) {
  try {
    await ElMessageBox.confirm(`确定删除消息「${row.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    await deleteMessage(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 取消或错误均静默处理
  }
}

/** 批量删除 */
async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的消息')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedIds.value.length} 条消息吗？`,
      '批量删除确认',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
    )
    await batchDeleteMessage(selectedIds.value)
    ElMessage.success(`成功删除 ${selectedIds.value.length} 条消息`)
    selectedIds.value = []
    fetchList()
  } catch {
    // 取消或错误均静默处理
  }
}

/** 查看详情 */
function handleViewDetail(row: MessageItem) {
  detailData.value = row
  detailVisible.value = true
  // 如果未读，自动标记为已读
  if (!row.isRead) {
    markRead([row.id]).then(() => {
      row.isRead = true
      fetchList()
    })
  }
}

/** 分页变化 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  fetchList()
}

onMounted(() => {
  fetchList()
})
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

  .header-actions {
    display: flex;
    gap: 8px;
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

/** 详情对话框样式 */
.detail-body {
  .detail-header {
    h3 {
      margin: 0 0 12px;
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .detail-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 13px;
      color: var(--el-text-color-secondary);

      .meta-time {
        color: var(--el-text-color-placeholder);
      }
    }
  }

  .detail-content {
    font-size: 15px;
    line-height: 1.8;
    color: var(--el-text-color-regular);
    white-space: pre-wrap;
  }
}
</style>
