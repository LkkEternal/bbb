<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h2>知识库搜索</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="输入关键词搜索知识库…"
        clearable
        size="large"
        :prefix-icon="Search"
        style="max-width: 600px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" size="large" :icon="Search" @click="onSearch">
        搜索
      </el-button>
      <el-button size="large" :icon="Refresh" @click="onReset">
        重置
      </el-button>
    </div>

    <!-- 搜索结果统计 -->
    <div v-if="!loading && searched" class="result-meta">
      共找到 <strong>{{ pagination.total }}</strong> 条结果
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
      <el-skeleton :rows="3" animated style="margin-top: 16px" />
      <el-skeleton :rows="3" animated style="margin-top: 16px" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="searched && dataList.length === 0" class="empty-wrap">
      <el-empty description="未找到相关结果，请尝试其他关键词" />
    </div>

    <!-- 搜索结果卡片列表 -->
    <div v-else-if="dataList.length > 0" class="result-list">
      <el-card
        v-for="item in dataList"
        :key="item.id"
        shadow="hover"
        class="result-card"
      >
        <div class="card-body">
          <!-- 标题 -->
          <h3 class="card-title">{{ item.title }}</h3>

          <!-- 摘要 -->
          <p class="card-summary">{{ item.summary }}</p>

          <!-- 底部元信息 -->
          <div class="card-meta">
            <el-tag
              :type="scoreTagType(item.score)"
              size="small"
              effect="plain"
            >
              相似度 {{ formatScore(item.score) }}
            </el-tag>
            <span class="source-info">
              <el-icon :size="14"><Document /></el-icon>
              来源：{{ item.source }}
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 初始状态（未搜索过） -->
    <div v-else class="initial-hint">
      <el-icon :size="48" color="#c0c4cc"><Search /></el-icon>
      <p>输入关键词搜索知识库</p>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-wrap">
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
import { ref, reactive } from 'vue'
import { Search, Refresh, Document } from '@element-plus/icons-vue'
import {
  searchKnowledge,
  type KnowledgeItem,
  type KnowledgeSearchParams,
} from '@/api/modules/knowledge'

defineOptions({ name: 'KnowledgeSearch' })

// ─── 状态 ───

const loading = ref(false)
/** 是否执行过搜索（用于区分初始态与空结果） */
const searched = ref(false)
const dataList = ref<KnowledgeItem[]>([])

const searchForm = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// ─── 工具函数 ───

/** 相似度 → el-tag type */
function scoreTagType(score: number): string {
  if (score >= 0.8) return 'success'
  if (score >= 0.6) return 'primary'
  if (score >= 0.4) return 'warning'
  return 'info'
}

/** 格式化相似度显示 */
function formatScore(score: number): string {
  return `${(score * 100).toFixed(1)}%`
}

// ─── 数据加载 ───

async function fetchList() {
  loading.value = true
  searched.value = true
  try {
    const params: KnowledgeSearchParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword

    const res = await searchKnowledge(params)
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

/** 重置 */
function onReset() {
  searchForm.keyword = ''
  pagination.page = 1
  searched.value = false
  dataList.value = []
  pagination.total = 0
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

.result-meta {
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}

.loading-wrap {
  max-width: 800px;
}

.empty-wrap {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.initial-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #c0c4cc;
  user-select: none;

  p {
    margin-top: 16px;
    font-size: 16px;
  }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-card {
  border-radius: 8px;
  transition: transform 0.15s ease;

  &:hover {
    transform: translateY(-1px);
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.card-summary {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 4px;
  flex-wrap: wrap;
}

.source-info {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
