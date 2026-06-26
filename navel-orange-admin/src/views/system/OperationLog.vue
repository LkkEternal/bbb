<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-select v-model="filters.action" placeholder="操作类型" clearable>
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
          <el-button type="primary" @click="loadData" :loading="loading">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="action" label="操作" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">{{ getActionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourceType" label="资源类型" width="100" />
        <el-table-column prop="resourceId" label="资源ID" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOperationLogs } from '@/api/modules/user'
import dayjs from 'dayjs'

const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const logList = ref<any[]>([])
const filters = reactive({ action: '' })

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function getActionType(action: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    login: 'primary',
    register: 'success',
    logout: 'info',
    create: 'success',
    update: 'warning',
    delete: 'danger',
    export: 'info',
    upload: 'success',
  }
  return map[action] || 'info'
}

function getActionLabel(action: string): string {
  const map: Record<string, string> = {
    login: '登录',
    register: '注册',
    logout: '退出登录',
    create: '创建',
    update: '更新',
    delete: '删除',
    export: '导出',
    upload: '上传',
  }
  return map[action] || action
}

async function loadData() {
  loading.value = true
  try {
    const data: any = await getOperationLogs({
      page: page.value,
      size: pageSize.value,
      action: filters.action || undefined,
    })
    logList.value = data.records || []
    total.value = data.total || 0
  } catch {} finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
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
