<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="7">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索操作人 / 操作模块"
            clearable
            @keyup.enter="onSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select
            v-model="searchForm.operationType"
            placeholder="操作类型"
            clearable
            style="width: 100%"
            @change="onSearch"
          >
            <el-option label="全部" value="" />
            <el-option label="登录" value="login" />
            <el-option label="注册" value="register" />
            <el-option label="退出登录" value="logout" />
            <el-option label="创建" value="create" />
            <el-option label="更新" value="update" />
            <el-option label="删除" value="delete" />
            <el-option label="导出" value="export" />
            <el-option label="上传" value="upload" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="onSearch" :loading="loading">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading" stripe style="width: 100%" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="operator" label="操作人" width="120" show-overflow-tooltip />
        <el-table-column prop="module" label="操作模块" width="130" show-overflow-tooltip />
        <el-table-column label="操作类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.operationType)" effect="plain" size="small">
              {{ getActionLabel(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="操作详情" min-width="240" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP 地址" width="150" />
        <el-table-column label="操作时间" width="175" align="center">
          <template #default="{ row }">
            {{ formatTime(row.operateTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getOperationLogs, type OperationLogVO } from '@/api/modules/operationLog'
import dayjs from 'dayjs'

defineOptions({ name: 'OperationLogList' })

// ─── 状态 ───
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const logList = ref<OperationLogVO[]>([])

const searchForm = reactive({
  keyword: '',
  operationType: '',
})

// ─── 工具函数 ───
function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const actionTypeMap: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
  login: 'primary',
  register: 'success',
  logout: 'info',
  create: 'success',
  update: 'warning',
  delete: 'danger',
  export: 'info',
  upload: 'success',
}

const actionLabelMap: Record<string, string> = {
  login: '登录',
  register: '注册',
  logout: '退出登录',
  create: '创建',
  update: '更新',
  delete: '删除',
  export: '导出',
  upload: '上传',
}

function getActionType(action: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  return actionTypeMap[action] || 'info'
}

function getActionLabel(action: string): string {
  return actionLabelMap[action] || action
}

// ─── 搜索 & 分页 ───
function onSearch() {
  page.value = 1
  loadData()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.operationType = ''
  page.value = 1
  loadData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  page.value = 1
  loadData()
}

// ─── 数据获取 ───
async function loadData() {
  loading.value = true
  try {
    const data = await getOperationLogs({
      page: page.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      operationType: searchForm.operationType || undefined,
    })
    logList.value = data.records || []
    total.value = data.total || 0
  } catch {
    logList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ─── 生命周期 ───
onMounted(loadData)
</script>

<style scoped lang="scss">
.page-container {
  padding: 0;
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
