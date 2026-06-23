<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户反馈</h2>
      <el-select
        v-model="statusFilter"
        placeholder="筛选状态"
        clearable
        style="width: 160px"
        @change="handleFilterChange"
      >
        <el-option
          v-for="(val, key) in FEEDBACK_STATUS_MAP"
          :key="key"
          :label="val.label"
          :value="Number(key)"
        />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="图片" width="80" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            fit="cover"
            class="thumb-img"
            :preview-src-list="[row.imageUrl]"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="AI诊断" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.aiDiagnosis" type="warning" size="small" effect="plain">
            {{ row.aiDiagnosis }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="置信度" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.aiConfidence != null">{{ (row.aiConfidence * 100).toFixed(1) }}%</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column label="匹配度" width="150" align="center">
        <template #default="{ row }">
          <el-progress
            v-if="row.matchScore != null"
            :percentage="Math.round(row.matchScore * 100)"
            :stroke-width="8"
            :color="getMatchColor(row.matchScore)"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :color="FEEDBACK_STATUS_MAP[row.status]?.color"
            effect="dark"
            size="small"
            style="border: none"
          >
            {{ FEEDBACK_STATUS_MAP[row.status]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户" width="120" align="center" />
      <el-table-column prop="createdAt" label="提交时间" width="170" align="center" />
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/feedback/${row.id}`)">详情</el-button>
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
import { getFeedbackList } from '@/api/modules/feedback'
import { FEEDBACK_STATUS_MAP } from '@/constants/enums'

interface FeedbackItem {
  id: number
  imageUrl: string
  aiDiagnosis: string
  aiConfidence: number
  description: string
  matchScore: number
  status: number
  username: string
  createdAt: string
}

const loading = ref(false)
const statusFilter = ref<number | ''>('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<FeedbackItem[]>([])

function getMatchColor(score: number) {
  if (score >= 0.8) return '#67c23a'
  if (score >= 0.5) return '#e6a23c'
  return '#f56c6c'
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const data: any = await getFeedbackList(params)
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

onMounted(fetchList)
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

.thumb-img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
