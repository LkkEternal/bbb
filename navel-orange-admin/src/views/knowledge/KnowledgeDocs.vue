<template>
  <div class="page-container">
    <div class="page-header">
      <h2>文献管理</h2>
      <el-upload
        :show-file-list="false"
        :before-upload="handleUpload"
        accept=".pdf,.doc,.docx,.txt,.md"
      >
        <el-button type="primary" :icon="UploadFilled">上传文档</el-button>
      </el-upload>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索文献标题"
        clearable
        :prefix-icon="Search"
        style="width: 320px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="文件类型" width="100" align="center">
        <template #default="{ row }">
          <div class="file-type-cell">
            <el-icon :size="18"><Document /></el-icon>
            <span>{{ (row.fileType || '-').toUpperCase() }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" width="100" align="center">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="uploadBy" label="上传人" width="120" align="center">
        <template #default="{ row }">{{ row.uploadBy || '-' }}</template>
      </el-table-column>
      <el-table-column label="上传时间" width="170" align="center">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="处理状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag
            :color="KNOWLEDGE_STATUS_MAP[row.status]?.color"
            effect="dark"
            size="small"
            style="border: none"
          >
            {{ KNOWLEDGE_STATUS_MAP[row.status]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <!-- 待处理 -> 处理 -->
          <el-button
            v-if="row.status === 0"
            link
            type="primary"
            @click="handleProcess(row)"
          >
            处理
          </el-button>
          <!-- 处理失败 -> 重试 -->
          <el-button
            v-if="row.status === 3"
            link
            type="warning"
            @click="handleRetry(row)"
          >
            重试
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="fetchList"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, UploadFilled, Document } from '@element-plus/icons-vue'
import { getDocumentList, uploadDocument, deleteDocument, processDocument } from '@/api/modules/knowledge'
import { KNOWLEDGE_STATUS_MAP } from '@/constants/enums'
import { formatFileSize } from '@/utils/format'
import dayjs from 'dayjs'

defineOptions({ name: 'KnowledgeDocsList' })

// ─── 类型 ───
interface DocItem {
  id: number
  title: string
  fileType: string
  fileSize: number
  uploadBy?: string
  status: number
  createdAt: string
}

// ─── 响应式状态 ───
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<DocItem[]>([])

const searchForm = reactive({
  keyword: '',
})

// ─── 工具函数 ───
function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// ─── 搜索 & 分页 ───
function onSearch() {
  currentPage.value = 1
  fetchList()
}

function onSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  fetchList()
}

// ─── 数据获取 ───
async function fetchList() {
  loading.value = true
  try {
    const params: { page: number; size: number; keyword?: string } = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (searchForm.keyword.trim()) {
      params.keyword = searchForm.keyword.trim()
    }
    const data: any = await getDocumentList(params)
    list.value = data?.records ?? data ?? []
    total.value = data?.total ?? list.value.length
  } catch {
    // 静默处理
  } finally {
    loading.value = false
  }
}

// ─── 上传 ───
async function handleUpload(file: File) {
  try {
    await uploadDocument(file)
    ElMessage.success('上传成功')
    fetchList()
  } catch {
    ElMessage.error('上传失败')
  }
  return false
}

// ─── 处理 ───
async function handleProcess(row: DocItem) {
  row.status = 1 // 立即显示"处理中"，给用户即时反馈
  try {
    await processDocument(row.id)
    ElMessage.success('处理完成')
  } catch {
    ElMessage.error('处理失败')
  }
  fetchList() // 刷新获取真实状态
}

// ─── 重试（处理失败时）───
async function handleRetry(row: DocItem) {
  row.status = 1 // 立即显示"处理中"
  try {
    await processDocument(row.id)
    ElMessage.success('重试成功，处理完成')
  } catch {
    ElMessage.error('重试失败')
  }
  fetchList()
}

// ─── 删除 ───
async function handleDelete(row: DocItem) {
  try {
    await ElMessageBox.confirm(`确定删除文献「${row.title}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDocument(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 用户取消或操作失败
  }
}

// ─── 生命周期 ───
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
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.file-type-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: $text-secondary;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
