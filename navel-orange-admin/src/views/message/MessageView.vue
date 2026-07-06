<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消息中心</h2>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filterProcessed" @change="handleFilterChange">
        <el-radio-button :value="undefined">全部 <span class="count-badge">{{ counts.total ?? 0 }}</span></el-radio-button>
        <el-radio-button :value="true">已处理 <span class="count-badge">{{ counts.processed ?? 0 }}</span></el-radio-button>
        <el-radio-button :value="false">未处理 <span class="count-badge">{{ counts.unprocessed ?? 0 }}</span></el-radio-button>
      </el-radio-group>
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
            <el-tag v-if="item.processed" type="success" size="small" effect="plain">已处理</el-tag>
            <el-tag v-else type="warning" size="small" effect="plain">未处理</el-tag>
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
          <div v-if="detail.processed" class="detail-treatment">
            <el-divider />
            <div class="treatment-header">
              <span class="title-badge">处理状态</span>
              <el-tag type="success" size="small" effect="dark">已处理</el-tag>
            </div>
            <div v-if="treatmentImageList.length > 0" class="treatment-images">
              <el-image
                v-for="(url, idx) in treatmentImageList"
                :key="idx"
                :src="url"
                fit="cover"
                class="treatment-image-item"
                :preview-src-list="treatmentImageList"
              />
            </div>
            <p v-else class="treatment-empty">暂无处理图片</p>
          </div>
          <div v-else class="detail-unprocessed">
            <el-divider />
            <el-alert title="请前往小程序处理该预警" type="warning" :closable="false" show-icon />
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMessageList, getMessageDetail, getMessageCounts } from '@/api/modules/message'
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
  processed: boolean
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
  processed: boolean
  treatmentImages: string
  createdAt: string
}

const loading = ref(false)
const detailLoading = ref(false)
const filterProcessed = ref<boolean | undefined>(undefined)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const counts = ref({ total: 0, processed: 0, unprocessed: 0 })
const list = ref<MessageItem[]>([])
const showDetail = ref(false)
const detail = ref<MessageDetail | null>(null)

const treatmentImageList = computed(() => {
  if (!detail.value?.treatmentImages) return []
  try {
    return JSON.parse(detail.value.treatmentImages)
  } catch {
    return []
  }
})

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

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (filterProcessed.value !== undefined) params.processed = filterProcessed.value
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

async function fetchCounts() {
  try {
    const data: any = await getMessageCounts()
    counts.value = data
  } catch {}
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

onMounted(() => {
  fetchList()
  fetchCounts()
})
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/assets/styles/variables.scss' as *;

.filter-bar {
  margin-bottom: 20px;

  .count-badge {
    display: inline-block;
    min-width: 18px;
    height: 18px;
    line-height: 18px;
    text-align: center;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    background: rgba(0,0,0,0.15);
    border-radius: 9px;
    padding: 0 5px;
    margin-left: 4px;
    vertical-align: middle;
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
    border-color: color.adjust($border, $lightness: -8%);
    transform: translateY(-1px);
  }

  &.unread {
    background: $accent-light;
    border-color: color.adjust($accent, $lightness: 25%);
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

// 处理状态
.detail-treatment {
  .treatment-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;

    .title-badge {
      display: inline-block;
      font-size: 14px;
      font-weight: 600;
      color: $primary;
      background: rgba($primary, 0.08);
      padding: 4px 12px;
      border-radius: 6px;
    }
  }

  .treatment-images {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .treatment-image-item {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid $border;
  }

  .treatment-empty {
    font-size: 13px;
    color: $text-placeholder;
    margin: 0;
  }
}

.detail-unprocessed {
  :deep(.el-alert) {
    border-radius: 8px;
  }
}
</style>
