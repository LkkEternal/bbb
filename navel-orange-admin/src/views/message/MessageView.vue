<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息中心</h2>
      <el-button v-if="!userStore.isAdmin" type="primary" :disabled="unreadCount === 0" @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>

    <div class="filter-bar" v-if="!userStore.isAdmin">
      <el-radio-group v-model="filterType" @change="handleFilterChange">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="unread">未读</el-radio-button>
      </el-radio-group>
      <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount }} 条未读</span>
    </div>

    <div class="message-list" v-loading="loading">
      <div
        v-for="item in list"
        :key="item.id"
        class="message-card"
        :class="{ unread: item.readStatus === 0 }"
        @click="handleClickMessage(item)"
      >
        <div class="level-border" :style="{ backgroundColor: getLevelColor(item.level) }"></div>
        <div class="message-body">
          <div class="message-header">
            <el-tag :type="getLevelTagType(item.level)" size="small" effect="dark">
              L{{ item.level }}
            </el-tag>
            <span class="message-title">{{ item.title }}</span>
            <span class="message-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <div class="message-content">{{ item.summary || item.content }}</div>
          <div class="message-meta">
            <span v-if="item.orchardName">果园: {{ item.orchardName }}</span>
            <el-tag v-if="item.readStatus === 0" type="danger" size="small" effect="plain">未读</el-tag>
            <el-tag v-else type="info" size="small" effect="plain">已读</el-tag>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && list.length === 0" description="暂无消息" />
    </div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <!-- 消息详情对话框 -->
    <el-dialog v-model="showDetail" title="消息详情" width="560px" destroy-on-close>
      <div v-loading="detailLoading" class="message-detail">
        <template v-if="detail">
          <div class="detail-header">
            <el-tag :type="getLevelTagType(detail.level)" size="small" effect="dark">
              L{{ detail.level }}
            </el-tag>
            <span class="detail-type">{{ getMessageTypeLabel(detail.messageType) }}</span>
          </div>
          <h3 class="detail-title">{{ detail.title }}</h3>
          <div class="detail-meta">
            <span v-if="detail.orchardName">果园: {{ detail.orchardName }}</span>
            <span>{{ formatTime(detail.createdAt) }}</span>
          </div>
          <el-divider />
          <div class="detail-content">
            <h4>预警内容</h4>
            <p>{{ detail.content }}</p>
          </div>
          <div v-if="detail.advice" class="detail-suggestion">
            <h4>防治建议</h4>
            <p>{{ detail.advice }}</p>
          </div>
          <div v-if="detail.ruleCode" class="detail-rule">
            <span class="label">规则编码:</span>
            <span>{{ detail.ruleCode }}</span>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button
          v-if="detail && detail.readStatus === 1 && !userStore.isAdmin"
          :loading="markingUnread"
          @click="handleMarkUnread"
        >
          标记未读
        </el-button>
        <el-button
          v-if="detail && detail.readStatus === 0 && !userStore.isAdmin"
          type="primary"
          :loading="markingRead"
          @click="handleMarkRead"
        >
          标记已读
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageList, getMessageDetail, getUnreadCount, markAsRead, markAsUnread, markAllAsRead } from '@/api/modules/message'
import { ALERT_LEVEL_MAP } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

interface MessageItem {
  id: number
  title: string
  content: string
  summary: string
  level: number
  orchardName: string
  readStatus: number
  createdAt: string
}

interface MessageDetail {
  id: number
  messageType: string
  title: string
  summary: string
  content: string
  advice: string
  level: number
  orchardId: number
  orchardName: string
  ruleCode: string
  readStatus: number
  createdAt: string
}

const loading = ref(false)
const detailLoading = ref(false)
const markingRead = ref(false)
const markingUnread = ref(false)
const filterType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const unreadCount = ref(0)
const list = ref<MessageItem[]>([])
const showDetail = ref(false)
const detail = ref<MessageDetail | null>(null)

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function getLevelColor(level: number) {
  return ALERT_LEVEL_MAP[level]?.color || '#909399'
}

function getLevelTagType(level: number) {
  if (level === 3) return 'danger' as const
  if (level === 2) return 'warning' as const
  return 'info' as const
}

function getMessageTypeLabel(type: string) {
  const map: Record<string, string> = {
    alert: '预警消息',
    calendar: '农事日历',
    system: '系统通知',
  }
  return map[type] || type
}

async function fetchUnreadCount() {
  try {
    const data: any = await getUnreadCount()
    unreadCount.value = data?.count ?? data ?? 0
  } catch {}
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (filterType.value === 'unread') params.readStatus = 0
    const data: any = await getMessageList(params)
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

async function handleClickMessage(item: MessageItem) {
  // 获取消息详情（不自动标记已读）
  detailLoading.value = true
  showDetail.value = true
  detail.value = null

  try {
    const data: any = await getMessageDetail(item.id, false)
    detail.value = data
  } catch {
    showDetail.value = false
  } finally {
    detailLoading.value = false
  }
}

async function handleMarkRead() {
  if (!detail.value) return
  markingRead.value = true
  try {
    await markAsRead(detail.value.id)
    detail.value.readStatus = 1
    ElMessage.success('已标记为已读')

    // 更新列表状态
    const listItem = list.value.find(item => item.id === detail.value?.id)
    if (listItem && listItem.readStatus === 0) {
      listItem.readStatus = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch {} finally {
    markingRead.value = false
  }
}

async function handleMarkUnread() {
  if (!detail.value) return
  markingUnread.value = true
  try {
    await markAsUnread(detail.value.id)
    detail.value.readStatus = 0
    ElMessage.success('已标记为未读')

    // 更新列表状态
    const listItem = list.value.find(item => item.id === detail.value?.id)
    if (listItem && listItem.readStatus === 1) {
      listItem.readStatus = 0
      unreadCount.value += 1
    }
  } catch {} finally {
    markingUnread.value = false
  }
}

async function handleMarkAllRead() {
  try {
    await ElMessageBox.confirm('确认全部已读？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await markAllAsRead()
    ElMessage.success('已全部标为已读')
    unreadCount.value = 0
    list.value.forEach(item => {
      item.readStatus = 1
    })
  } catch {}
}

onMounted(() => {
  fetchList()
  if (!userStore.isAdmin) {
    fetchUnreadCount()
  }
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .unread-badge {
    font-size: 13px;
    color: $danger;
    font-weight: 500;
  }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  display: flex;
  border-radius: $radius-lg;
  background: $card;
  border: 1px solid $border;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;

  &:hover {
    box-shadow: $shadow-sm;
    border-color: darken($border, 8%);
    transform: translateY(-1px);
  }

  &.unread {
    background: $accent-light;
    border-color: lighten($accent, 25%);
  }
}

.level-border {
  width: 4px;
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  padding: 16px 20px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;

  .message-title {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .message-time {
    font-size: 12px;
    color: $text-placeholder;
    flex-shrink: 0;
  }
}

.message-content {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: $text-placeholder;
}

// 消息详情样式
.message-detail {
  min-height: 200px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;

  .detail-type {
    font-size: 13px;
    color: $text-secondary;
  }
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: $text-primary;
  margin: 0 0 12px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: $text-secondary;
}

.detail-content,
.detail-suggestion {
  margin-bottom: 16px;

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
    margin: 0 0 8px 0;
  }

  p {
    font-size: 14px;
    color: $text-secondary;
    line-height: 1.8;
    margin: 0;
    white-space: pre-wrap;
  }
}

.detail-suggestion {
  background: #f6f8fa;
  padding: 12px 16px;
  border-radius: 8px;

  h4 {
    color: $primary;
  }
}

.detail-rule {
  font-size: 13px;
  color: $text-secondary;

  .label {
    font-weight: 500;
    margin-right: 8px;
  }
}
</style>
