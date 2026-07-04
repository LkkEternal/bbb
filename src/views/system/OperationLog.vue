<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索操作人 / 操作模块"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="onReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="dataList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />

      <el-table-column prop="operator" label="操作人" width="130" />

      <el-table-column prop="module" label="操作模块" width="140" />

      <el-table-column prop="operationType" label="操作类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="operationTypeTag(row.operationType)" effect="plain" size="small">
            {{ row.operationType }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="detail" label="操作详情" min-width="240" show-overflow-tooltip />

      <el-table-column prop="ip" label="IP 地址" width="150" align="center" />

      <el-table-column label="操作时间" width="180" align="center">
        <template #default="{ row }">
          {{ row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import {
  getOperationLogList,
  type OperationLogItem,
  type OperationLogListParams,
} from '@/api/modules/operationLog'

defineOptions({ name: 'OperationLogList' })

/** 操作类型 → el-tag type 映射 */
const OPERATION_TYPE_TAG: Record<string, string> = {
  '新增': 'success',
  '创建': 'success',
  '编辑': 'warning',
  '更新': 'warning',
  '删除': 'danger',
  '查询': 'info',
  '导出': 'primary',
  '登录': '',
  '登出': 'info',
}

function operationTypeTag(type: string): string {
  return OPERATION_TYPE_TAG[type] ?? ''
}

// ===== 状态 =====

const loading = ref(false)
const dataList = ref<OperationLogItem[]>([])

const searchForm = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// ===== 方法 =====

/** 获取列表数据 */
async function fetchList() {
  loading.value = true
  try {
    const params: OperationLogListParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword

    const res = await getOperationLogList(params)
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
  pagination.page = 1
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
</style>
