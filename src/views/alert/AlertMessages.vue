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
            :color="ALERT_LEVEL_MAP[row.level]?.color"
            effect="dark"
            size="small"
            style="border: none; color: #fff"
          >
            {{ ALERT_LEVEL_MAP[row.level]?.label ?? '未知' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />

      <el-table-column prop="orchardName" label="相关果园" width="140" show-overflow-tooltip />

      <el-table-column label="触发时间" width="180" align="center" sortable="custom" prop="createdAt">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="处理状态" width="100" align="center">
        <template #default="{ row }">
          <span v-if="!row.isRead" class="unread-badge">
            <span class="unread-dot"></span>
            未读
          </span>
          <span v-else class="read-text">已读</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="170" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewDetail(row)">
            查看详情
          </el-button>
          <el-button
            v-if="!row.isRead"
            link
            type="warning"
            @click="handleMarkRead(row)"
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
            <span v-if="!detailItem.isRead" class="unread-status">未读</span>
            <span v-else class="read-status">已读</span>
          </div>
          <div class="detail-row" v-if="detailItem.typeName">
            <span class="detail-label">消息类型</span>
            <span>{{ detailItem.typeName }}</span>
          </div>
        </div>

        <el-divider />

        <div class="detail-content">
          <h4 class="content-title">消息内容</h4>
          <p class="content-body">{{ detailItem.content || '暂无内容' }}</p>
        </div>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailItem && !detailItem.isRead"
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
import { ElMessage } from 'element-plus'
import { Search, Refresh, Check } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getMessageList,
  markRead,
  type MessageItem,
  type MessageListParams,
} from '@/api/modules/message'

defineOptions({ name: 'AlertMessagesList' })

// ─── 常量 ───

/** 预警等级颜色映射 */
const ALERT_LEVEL_MAP: Record<number, { label: string; color: string }> = {
  1: { label: 'L1 低风险', color: '#5B8C5A' },
  2: { label: 'L2 中风险', color: '#E8863A' },
  3: { label: 'L3 高风险', color: '#D64545' },
}

const LEVEL_OPTIONS = Object.entries(ALERT_LEVEL_MAP).map(([value, item]) => ({
  label: item.label,
  value: Number(value),
}))

// ─── 响应式状态 ───

const loading = ref(false)
const dataList = ref<MessageItem[]>([])

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

// (排序参数保留给后端扩展使用)

/** 详情抽屉 */
const detailVisible = ref(false)
const detailItem = ref<MessageItem | null>(null)

// ─── 计算属性 ───

const hasUnread = computed(() => dataList.value.some(item => !item.isRead))

// ─── 方法 ───

/** 格式化日期 */
function formatDate(date: string | undefined | null): string {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/** 获取列表数据 */
async function fetchList() {
  loading.value = true
  try {
    const params: MessageListParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.level) params.type = searchForm.level
    if (searchForm.readStatus !== undefined) params.isRead = searchForm.readStatus === 1

    const res = await getMessageList(params)
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
  searchForm.level = undefined
  searchForm.readStatus = undefined
  pagination.page = 1
  fetchList()
}

/** 排序变化 */
function onSortChange(_sort: { prop?: string; order?: string }) {
  // 当前 API 暂不支持后端排序，保留前端排序回调以备后续扩展
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
function handleViewDetail(row: MessageItem) {
  detailItem.value = { ...row }
  detailVisible.value = true
}

/** 标记单条已读 */
async function handleMarkRead(row: MessageItem) {
  try {
    await markRead([row.id])
    row.isRead = true
    ElMessage.success('已标记为已读')
  } catch {
    // 静默处理
  }
}

/** 从详情抽屉标记已读 */
async function handleMarkReadFromDetail() {
  if (!detailItem.value) return
  try {
    await markRead([detailItem.value.id])
    detailItem.value.isRead = true
    // 同步列表
    const target = dataList.value.find(item => item.id === detailItem.value!.id)
    if (target) target.isRead = true
    ElMessage.success('已标记为已读')
  } catch {
    // 静默处理
  }
}

/** 全部标为已读 */
async function handleMarkAllRead() {
  try {
    const unreadIds = dataList.value
      .filter(item => !item.isRead)
      .map(item => item.id)
    if (unreadIds.length === 0) {
      ElMessage.info('没有未读消息')
      return
    }
    await markRead(unreadIds)
    dataList.value.forEach(item => { item.isRead = true })
    ElMessage.success(`已标记全部已读（共 ${unreadIds.length} 条）`)
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
  color: #303133;

  .unread-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #D64545;
  }
}

.read-text {
  font-size: 13px;
  color: #C0C4CC;
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
    color: #909399;
  }
}

.unread-status {
  color: #D64545;
  font-weight: 500;
}

.read-status {
  color: #C0C4CC;
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
    color: #303133;
    white-space: pre-wrap;
  }
}
</style>
