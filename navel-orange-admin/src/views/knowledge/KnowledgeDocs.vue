<template>
  <div class="page-container">
    <div class="page-header">
      <h2>文献管理</h2>
      <el-upload
        :show-file-list="false"
        :before-upload="handleUpload"
        accept=".pdf,.doc,.docx,.txt,.md"
      >
        <el-button type="primary" :icon="Upload">上传文档</el-button>
      </el-upload>
    </div>

    <el-table :data="list" v-loading="loading" style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="fileName" label="文件名" min-width="160" show-overflow-tooltip />
      <el-table-column label="文件类型" width="100" align="center">
        <template #default="{ row }">
          <div class="file-type-cell">
            <el-icon :size="18"><Document /></el-icon>
            <span>{{ getFileExt(row.fileName) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" width="100" align="center">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="chunkCount" label="分块数" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
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
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status === 1 || row.status === 2" @click="handleProcess(row)">
            处理
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document } from '@element-plus/icons-vue'
import { getDocumentList, uploadDocument, deleteDocument, processDocument } from '@/api/modules/knowledge'
import { KNOWLEDGE_STATUS_MAP } from '@/constants/enums'
import { formatFileSize } from '@/utils/format'

interface DocItem {
  id: number
  title: string
  fileName: string
  fileSize: number
  chunkCount: number
  status: number
}

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<DocItem[]>([])

function getFileExt(name: string) {
  return name?.split('.').pop()?.toUpperCase() || '-'
}

async function fetchList() {
  loading.value = true
  try {
    const data: any = await getDocumentList({ page: currentPage.value, size: pageSize.value })
    list.value = data?.records ?? data ?? []
    total.value = data?.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
}

async function handleUpload(file: File) {
  try {
    await uploadDocument(file)
    ElMessage.success('上传成功')
    fetchList()
  } catch {}
  return false
}

async function handleProcess(row: any) {
  row.status = 1  // 立即显示"处理中"，给用户即时反馈
  try {
    await processDocument(row.id)
    ElMessage.success('处理完成')
  } catch {
    ElMessage.error('处理失败')
  }
  fetchList()  // 刷新获取真实状态
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除文档「${row.title}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDocument(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {}
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
