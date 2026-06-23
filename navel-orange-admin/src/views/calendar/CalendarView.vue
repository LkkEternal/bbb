<template>
  <div class="page-container">
    <h2>农事日历</h2>

    <el-card v-if="current" class="current-card" shadow="hover" :style="{ borderLeftColor: current.color || '#409eff' }">
      <div class="current-header">
        <el-tag effect="dark" size="large" :color="current.color || '#409eff'" style="border: none; color: #fff">
          当前时段
        </el-tag>
        <span class="current-period">{{ current.periodName }}</span>
        <span class="current-term" v-if="current.solarTerm">· {{ current.solarTerm }}</span>
      </div>
      <div class="current-body">
        <div class="current-row">
          <span class="label">日期范围:</span>
          <span>{{ current.startDate }} ~ {{ current.endDate }}</span>
        </div>
        <div class="current-row" v-if="current.focusPests">
          <span class="label">重点病虫害:</span>
          <span>{{ current.focusPests }}</span>
        </div>
        <div class="current-row" v-if="current.description">
          <span class="label">说明:</span>
          <span>{{ current.description }}</span>
        </div>
        <div class="current-row" v-if="current.environmentAdvice">
          <span class="label">环境建议:</span>
          <span>{{ current.environmentAdvice }}</span>
        </div>
      </div>
    </el-card>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="periodName" label="时段名称" min-width="140" />
      <el-table-column prop="solarTerm" label="节气" width="100" align="center" />
      <el-table-column label="日期范围" width="220" align="center">
        <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
      </el-table-column>
      <el-table-column prop="focusPests" label="重点病虫害" min-width="200" show-overflow-tooltip />
    </el-table>

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
import { getCalendarList, getCurrentReminder } from '@/api/modules/calendar'

interface CalendarItem {
  id: number
  periodName: string
  solarTerm: string
  startDate: string
  endDate: string
  focusPests: string
  description: string
  environmentAdvice: string
  color: string
}

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<CalendarItem[]>([])
const current = ref<CalendarItem | null>(null)

async function fetchCurrent() {
  try {
    const data: any = await getCurrentReminder()
    current.value = data ?? null
  } catch {}
}

async function fetchList() {
  loading.value = true
  try {
    const data: any = await getCalendarList({ page: currentPage.value, size: pageSize.value })
    list.value = data?.records ?? data ?? []
    total.value = data?.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCurrent()
  fetchList()
})
</script>

<style scoped lang="scss">
h2 {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 600;
}

.current-card {
  margin-bottom: 24px;
  border-left: 4px solid #409eff;
  border-radius: 8px;
}

.current-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .current-period {
    font-size: 18px;
    font-weight: 700;
    color: #303133;
  }

  .current-term {
    font-size: 15px;
    color: #909399;
  }
}

.current-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.current-row {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;

  .label {
    color: #909399;
    margin-right: 8px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
