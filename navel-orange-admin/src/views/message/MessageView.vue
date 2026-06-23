<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息中心</h2>
      <el-button type="primary" :disabled="unreadCount === 0" @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>

    <div class="filter-bar">
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
        :class="{ unread: !item.read }"
        @click="handleRead(item)"
      >
        <div class="level-border" :style="{ backgroundColor: getLevelColor(item.level) }"></div>
        <div class="message-body">
          <div class="message-header">
            <el-tag :type="getLevelTagType(item.level)" size="small" effect="dark">
              L{{ item.level }}
            </el-tag>
            <span class="message-title">{{ item.title }}</span>
            <span class="message-time">{{ item.createdAt }}</span>
          </div>
          <div class="message-content">{{ item.content }}</div>
          <div class="message-meta">
            <span v-if="item.orchardName">果园: {{ item.orchardName }}</span>
            <el-tag v-if="!item.read" type="danger" size="small">未读</el-tag>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessageList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/modules/message'
import { ALERT_LEVEL_MAP } from '@/constants/disease'

interface MessageItem {
  id: number
  title: string
  content: string
  level: number
  orchardName: string
  read: boolean
  createdAt: string
}

const loading = ref(false)
const filterType = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const unreadCount = ref(0)
const list = ref<MessageItem[]>([])

function getLevelColor(level: number) {
  return ALERT_LEVEL_MAP[level]?.color || '#909399'
}

function getLevelTagType(level: number) {
  if (level === 3) return 'danger' as const
  if (level === 2) return 'warning' as const
  return 'info' as const
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
    if (filterType.value === 'unread') params.read = false
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

async function handleRead(item: MessageItem) {
  if (item.read) return
  try {
    await markAsRead(item.id)
    item.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {}
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标为已读')
    unreadCount.value = 0
    list.value.forEach(item => (item.read = true))
  } catch {}
}

onMounted(() => {
  fetchList()
  fetchUnreadCount()
})
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
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .unread-badge {
    font-size: 13px;
    color: #f56c6c;
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
  border-radius: 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;

  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border-color: #d9d9d9;
  }

  &.unread {
    background: #fdf6ec;
    border-color: #f5dab1;
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
    color: #303133;
    flex: 1;
  }

  .message-time {
    font-size: 12px;
    color: #909399;
    flex-shrink: 0;
  }
}

.message-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
