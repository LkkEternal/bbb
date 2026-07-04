<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>识别记录</h2>
      <div class="header-right">
        <el-select
          v-if="!userStore.isAdmin"
          v-model="selectedOrchard"
          placeholder="选择果园"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
          @change="handleSearch"
        />
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(61, 139, 64, 0.1)">
          <el-icon :size="24" color="#3D8B40"><DataAnalysis /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalCount ?? 0 }}</div>
          <div class="stat-label">总识别次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(255, 140, 66, 0.1)">
          <el-icon :size="24" color="#FF8C42"><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.alertCount ?? 0 }}</div>
          <div class="stat-label">预警次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(69, 130, 196, 0.1)">
          <el-icon :size="24" color="#4582C4"><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ avgPerDay }}</div>
          <div class="stat-label">日均识别</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索病害名称 / 果园"
        clearable
        :prefix-icon="Search"
        style="width: 260px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="diseaseCode"
        placeholder="病害类型"
        clearable
        style="width: 200px"
        @change="handleSearch"
      >
        <el-option
          v-for="opt in DISEASE_CODE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="图片" width="70" align="center">
        <template #default="{ row }">
          <el-image
            :src="row.imageUrl"
            fit="cover"
            class="thumb-image"
            :preview-src-list="[row.imageUrl]"
          />
        </template>
      </el-table-column>
      <el-table-column label="病害名称" width="140">
        <template #default="{ row }">
          <DiseaseTag :code="row.topClassCode" />
        </template>
      </el-table-column>
      <el-table-column label="置信度" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="confidenceLevel(row.topProbability)"
            size="small"
            effect="plain"
          >
            {{ (row.topProbability * 100).toFixed(1) }}%
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orchardName" label="果园" min-width="130" show-overflow-tooltip />
      <el-table-column prop="deviceCode" label="设备" width="120" show-overflow-tooltip />
      <el-table-column label="识别时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleDetail(row)">
            详情
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
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
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="识别详情" width="560px" destroy-on-close>
      <div v-if="detailRow" class="detail-body">
        <div class="detail-image-wrap">
          <el-image
            :src="detailRow.imageUrl"
            fit="contain"
            class="detail-image"
            :preview-src-list="[detailRow.imageUrl]"
          />
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="记录 ID">{{ detailRow.id }}</el-descriptions-item>
          <el-descriptions-item label="病害名称">
            <DiseaseTag :code="detailRow.topClassCode" />
          </el-descriptions-item>
          <el-descriptions-item label="置信度">
            {{ (detailRow.topProbability * 100).toFixed(1) }}%
          </el-descriptions-item>
          <el-descriptions-item label="果园">{{ detailRow.orchardName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备编码">{{ detailRow.deviceCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ detailRow.deviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备识别时间">{{ formatTime(detailRow.deviceTimestamp) }}</el-descriptions-item>
          <el-descriptions-item label="服务端接收时间">{{ formatTime(detailRow.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DataAnalysis, Warning, TrendCharts } from '@element-plus/icons-vue'
import { getDetectionList, getDetectionStats, deleteDetection } from '@/api/modules/detection'
import { getOrchardList } from '@/api/modules/orchard'
import { DISEASE_CODE_OPTIONS } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import DiseaseTag from '@/components/common/DiseaseTag.vue'
import dayjs from 'dayjs'

defineOptions({ name: 'DetectionStats' })

const userStore = useUserStore()

// ── 搜索状态 ──
const keyword = ref('')
const diseaseCode = ref('')
const dateRange = ref<string[]>([])
const selectedOrchard = ref<number | ''>('')

// ── 列表状态 ──
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])

// ── 统计数据 ──
const stats = ref<any>({})
const orchardList = ref<any[]>([])
const days = ref(30)

const avgPerDay = computed(() => {
  const totalCount = stats.value.totalCount ?? 0
  return days.value > 0 ? (totalCount / days.value).toFixed(1) : '0'
})

// ── 详情对话框 ──
const detailVisible = ref(false)
const detailRow = ref<any>(null)

// ── 工具函数 ──
function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function confidenceLevel(probability: number): string {
  if (probability >= 0.9) return 'success'
  if (probability >= 0.7) return 'warning'
  return 'danger'
}

// ── 数据加载 ──
function getDateParams() {
  const endDate = dateRange.value?.[1] || dayjs().format('YYYY-MM-DD')
  const startDate = dateRange.value?.[0] || dayjs().subtract(29, 'day').format('YYYY-MM-DD')
  return { startDate, endDate }
}

async function fetchStats() {
  try {
    const { startDate, endDate } = getDateParams()
    const params: any = { startDate, endDate }
    if (selectedOrchard.value) params.orchardId = selectedOrchard.value
    const data: any = await getDetectionStats(params)
    stats.value = data
  } catch { /* ignore */ }
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (selectedOrchard.value) params.orchardId = selectedOrchard.value
    if (diseaseCode.value) params.diseaseCode = diseaseCode.value
    if (keyword.value) params.keyword = keyword.value
    const { startDate, endDate } = getDateParams()
    params.startDate = startDate
    params.endDate = endDate

    const data: any = await getDetectionList(params)
    list.value = Array.isArray(data) ? data : data.records ?? []
    total.value = data.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
}

async function fetchOrchards() {
  if (userStore.isAdmin) return
  try {
    const data: any = await getOrchardList()
    orchardList.value = Array.isArray(data) ? data : data.records ?? []
  } catch { /* ignore */ }
}

// ── 事件处理 ──
function handleSearch() {
  currentPage.value = 1
  fetchList()
  fetchStats()
}

function handleReset() {
  keyword.value = ''
  diseaseCode.value = ''
  dateRange.value = []
  currentPage.value = 1
  fetchList()
  fetchStats()
}

function handleDetail(row: any) {
  detailRow.value = row
  detailVisible.value = true
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要删除该识别记录吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await deleteDetection(row.id)
    ElMessage.success('删除成功')
    fetchList()
    fetchStats()
  } catch {
    // 取消或失败都不处理
  }
}

// ── 初始化 ──
onMounted(async () => {
  await fetchOrchards()
  await Promise.all([fetchList(), fetchStats()])
})
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

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

// 统计概览
.stats-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: $card;
  border-radius: 12px;
  box-shadow: $shadow-sm;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    line-height: 1;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 13px;
    color: $text-secondary;
  }
}

// 搜索栏
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

// 表格
.thumb-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
}

// 分页
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

// 详情对话框
.detail-body {
  .detail-image-wrap {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;

    .detail-image {
      width: 280px;
      height: 200px;
      border-radius: 8px;
      border: 1px solid $border;
      object-fit: contain;
      background: #fafafa;
    }
  }
}
</style>
