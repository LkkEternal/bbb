<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h2>预警消息</h2>
      <div class="header-actions">
        <el-button
          v-if="hasUnread"
          :icon="Check"
          @click="handleMarkAllRead"
        >
          全部标为已读
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索标题 / 果园名称"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="onSearch"
      />
      <el-select
        v-model="searchForm.level"
        placeholder="预警等级"
        clearable
        style="width: 140px"
        @change="onSearch"
      >
        <el-option
          v-for="lv in LEVEL_OPTIONS"
          :key="lv.value"
          :label="lv.label"
          :value="lv.value"
        />
      </el-select>
      <el-select
        v-model="searchForm.readStatus"
        placeholder="处理状态"
        clearable
        style="width: 130px"
        @change="onSearch"
      >
        <el-option label="未读" :value="0" />
        <el-option label="已读" :value="1" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="onReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      :data="dataList"
      v-loading="loading"
      stripe
      style="width: 100%"
      @sort-change="onSortChange"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />

      <el-table-column label="预警等级" width="110" align="center" sortable="custom" prop="level">
        <template #default="{ row }">
          <el-tag
            :color="ALERT_LEVEL_MAP[(row as MessageItem).level]?.color"
            effect="dark"
            size="small"
            style="border: none; color: #fff"
          >
            {{ ALERT_LEVEL_MAP[(row as MessageItem).level]?.label ?? '未知' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />

      <el-table-column prop="orchardName" label="相关果园" width="140" show-overflow-tooltip />

      <el-table-column label="触发时间" width="180" align="center" sortable="custom" prop="createdAt">
        <template #default="{ row }">
          {{ formatDate((row as MessageItem).createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="处理状态" width="100" align="center">
        <template #default="{ row }">
          <span v-if="!(row as MessageItem).readStatus" class="unread-badge">
            <span class="unread-dot"></span>
            未读
          </span>
          <span v-else class="read-text">已读</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="170" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewDetail(row as MessageItem)">
            查看详情
          </el-button>
          <el-button
            v-if="!(row as MessageItem).readStatus"
            link
            type="warning"
            @click="handleMarkRead(row as MessageItem)"
          >
            标记已读
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

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      :title="detailItem?.title ?? '消息详情'"
      size="520px"
      destroy-on-close
    >
      <template v-if="detailItem">
        <div class="detail-section">
          <div class="detail-row">
            <span class="detail-label">预警等级</span>
            <el-tag
              :color="ALERT_LEVEL_MAP[detailItem.level]?.color"
              effect="dark"
              size="small"
              style="border: none; color: #fff"
            >
              {{ ALERT_LEVEL_MAP[detailItem.level]?.label ?? '未知' }}
            </el-tag>
          </div>
          <div class="detail-row">
            <span class="detail-label">相关果园</span>
            <span>{{ detailItem.orchardName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">触发时间</span>
            <span>{{ formatDate(detailItem.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">处理状态</span>
            <span v-if="!detailItem.readStatus" class="unread-status">未读</span>
            <span v-else class="read-status">已读</span>
          </div>
          <div class="detail-row" v-if="detailItem.ruleCode">
            <span class="detail-label">关联规则</span>
            <span>{{ detailItem.ruleCode }}</span>
          </div>
        </div>

        <el-divider />

        <div class="detail-content">
          <h4 class="content-title">消息内容</h4>
          <p class="content-body">{{ detailItem.summary ?? detailItem.content ?? '暂无内容' }}</p>
        </div>

        <div class="detail-content" v-if="detailItem.controlSuggestion">
          <h4 class="content-title">防控建议</h4>
          <p class="content-body suggestion">{{ detailItem.controlSuggestion }}</p>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailItem && !detailItem.readStatus"
          type="primary"
          @click="handleMarkReadFromDetail"
        >
          标记已读
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type TableInstance } from 'element-plus'
import { Search, Refresh, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getMessageList,
  markAsRead,
  markAllAsRead,
  getMessageDetail,
  type MessageListParams,
  type MessageListVO,
  type MessageDetailVO,
} from '@/api/modules/message'
import { ALERT_LEVEL_MAP } from '@/constants/disease'

defineOptions({ name: 'AlertMessagesList' })

// ─── 类型 ───
type MessageItem = MessageListVO

// ─── 常量 ───
const LEVEL_OPTIONS = Object.entries(ALERT_LEVEL_MAP).map(([value, item]) => ({
  label: item.label,
  value: Number(value),
}))

// ─── 响应式状态 ───
const loading = ref(false)
const dataList = ref<MessageItem[]>([])
const tableRef = ref<TableInstance>()

const searchForm = reactive({
  keyword: '',
  level: undefined as number | undefined,
  readStatus: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [10, 20, 50],
  layout: 'total, sizes, prev, pager, next, jumper' as const,
})

// 排序
const sortField = ref<string>('')
const sortOrder = ref<string>('')

// 详情抽屉
const detailVisible = ref(false)
const detailItem = ref<MessageDetailVO | null>(null)
const detailLoading = ref(false)

// ─── 计算属性 ───
const hasUnread = computed(() => dataList.value.some(item => !item.readStatus))

// ─── 方法 ───

/** 格式化日期 */
function formatDate(date: string | undefined): string {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/** 获取列表数据 */
async function fetchList() {
  loading.value = true
  try {
    const params: MessageListParams = {
      page: pagination.page,
      size: pagination.pageSize,
    }
    // 搜索条件
    if (searchForm.level) params.level = searchForm.level
    if (searchForm.readStatus !== undefined) params.readStatus = searchForm.readStatus
    // keyword 由后端支持（若后端不支持则传了也没影响）
    const query: Record<string, any> = { ...params }
    if (searchForm.keyword) query.keyword = searchForm.keyword
    if (sortField.value) {
      query.sortField = sortField.value
      query.sortOrder = sortOrder.value
    }

    const data: any = await getMessageList(query)
    dataList.value = Array.isArray(data) ? data : data.records ?? []
    pagination.total = data.total ?? dataList.value.length
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
  searchForm.level = undefined
  searchForm.readStatus = undefined
  pagination.page = 1
  fetchList()
}

/** 排序变化 */
function onSortChange(sort: { prop?: string; order?: string }) {
  if (sort.prop && sort.order) {
    sortField.value = sort.prop
    sortOrder.value = sort.order === 'ascending' ? 'asc' : 'desc'
  } else {
    sortField.value = ''
    sortOrder.value = ''
  }
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

/** 查看详情 */
async function handleViewDetail(row: MessageItem) {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const detail = await getMessageDetail(row.id)
    detailItem.value = detail
    // 如果后端标记了已读，同步更新列表中的状态
    if (detail.readStatus && !row.readStatus) {
      row.readStatus = 1
    }
  } catch {
    detailItem.value = {
      id: row.id,
      messageType: 'alert',
      title: row.title,
      content: row.summary ?? '',
      level: row.level,
      orchardId: row.orchardId,
      orchardName: row.orchardName,
      ruleCode: row.ruleCode ?? '',
      readStatus: row.readStatus,
      controlSuggestion: '',
      createdAt: row.createdAt,
    }
  } finally {
    detailLoading.value = false
  }
}

/** 标记单条已读 */
async function handleMarkRead(row: MessageItem) {
  try {
    await markAsRead(row.id)
    row.readStatus = 1
    ElMessage.success('已标记为已读')
  } catch {
    // 静默处理
  }
}

/** 从详情抽屉标记已读 */
async function handleMarkReadFromDetail() {
  if (!detailItem.value) return
  try {
    await markAsRead(detailItem.value.id)
    detailItem.value.readStatus = 1
    // 同步列表
    const target = dataList.value.find(item => item.id === detailItem.value!.id)
    if (target) target.readStatus = 1
    ElMessage.success('已标记为已读')
  } catch {
    // 静默处理
  }
}

/** 全部标为已读 */
async function handleMarkAllRead() {
  try {
    const res: any = await markAllAsRead()
    const count = res.markedCount ?? 0
    dataList.value.forEach(item => { item.readStatus = 1 })
    ElMessage.success(`已标记全部已读（共 ${count} 条）`)
  } catch {
    // 静默处理
  }
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

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

// 未读标识
.unread-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: $text-primary;

  .unread-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: $danger;
  }
}

.read-text {
  font-size: 13px;
  color: $text-placeholder;
}

// 详情抽屉样式
.detail-section {
  padding: 0 4px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .detail-label {
    flex-shrink: 0;
    width: 80px;
    font-size: 14px;
    color: $text-secondary;
  }
}

.unread-status {
  color: $danger;
  font-weight: 500;
}

.read-status {
  color: $text-placeholder;
}

.detail-content {
  padding: 0 4px;

  .content-title {
    margin: 0 0 10px;
    font-size: 15px;
    font-weight: 600;
  }

  .content-body {
    margin: 0;
    font-size: 14px;
    line-height: 1.7;
    color: $text-primary;
    white-space: pre-wrap;

    &.suggestion {
      color: $warning;
      background-color: rgba($warning, 0.06);
      padding: 12px 16px;
      border-radius: 6px;
    }
  }
}
</style>
