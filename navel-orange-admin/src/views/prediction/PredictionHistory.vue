<template>
  <div class="page-container">
    <div class="page-header">
      <h2>风险预测</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleTriggerPredict">
          触发预测
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="果园">
          <el-select v-model="searchForm.orchardId" placeholder="选择果园" clearable style="width:200px">
            <el-option
              v-for="o in orchardOptions"
              :key="o.id"
              :label="o.name"
              :value="o.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="searchForm.riskLevel" placeholder="全部等级" clearable style="width:140px">
            <el-option v-for="(info, key) in RISK_LEVEL_MAP" :key="key" :label="info.label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="searchForm.triggerType" placeholder="全部" clearable style="width:140px">
            <el-option label="每日定时" value="scheduled" />
            <el-option label="检测爆发" value="detection_burst" />
            <el-option label="高发期" value="seasonal" />
            <el-option label="手动触发" value="manual" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe highlight-current-row>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orchardName" label="果园" min-width="130" />
      <el-table-column label="风险等级" width="100" align="center">
        <template #default="{ row }">
          <el-tag :color="row.riskColor" effect="dark" size="small">
            {{ row.riskLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="概率分布" width="220">
        <template #default="{ row }">
          <div class="prob-bar">
            <div
              v-for="(p, i) in row.probabilities"
              :key="i"
              class="prob-segment"
              :style="{ width: (p * 100) + '%', backgroundColor: probColors[i] }"
              :title="`${['无风险','低风险','中风险','高风险'][i]}: ${(p * 100).toFixed(0)}%`"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="触发方式" width="110">
        <template #default="{ row }">
          <el-tag :type="triggerTagType(row.triggerType)" size="small">
            {{ triggerLabel(row.triggerType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="triggerDetail" label="触发详情" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="预测时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="预测详情" width="720px" top="5vh">
      <template v-if="detail">
        <div class="detail-header">
          <el-tag :color="detail.riskColor" effect="dark" size="large" class="risk-badge">
            {{ detail.riskLabel }}
          </el-tag>
          <span class="detail-score">风险评分: {{ (detail.riskScore * 100).toFixed(1) }}%</span>
          <span class="detail-time">{{ detail.createdAt }}</span>
        </div>

        <div class="detail-section">
          <h4>概率分布</h4>
          <div class="prob-bar large">
            <div
              v-for="(p, i) in detail.probabilities"
              :key="i"
              class="prob-segment"
              :style="{ width: (p * 100) + '%', backgroundColor: probColors[i] }"
            />
          </div>
          <div class="prob-labels">
            <span v-for="(p, i) in detail.probabilities" :key="i" class="prob-label">
              {{ ['无风险','低风险','中风险','高风险'][i] }}: {{ (p * 100).toFixed(1) }}%
            </span>
          </div>
        </div>

        <div class="detail-section" v-if="detail.topFactors && detail.topFactors.length">
          <h4>Top 风险因子</h4>
          <el-table :data="detail.topFactors" size="small">
            <el-table-column label="特征" min-width="200">
              <template #default="{ row }">{{ transFeature(row.feature) }}</template>
            </el-table-column>
            <el-table-column prop="value" label="当前值" width="100">
              <template #default="{ row }">{{ row.value.toFixed(4) }}</template>
            </el-table-column>
            <el-table-column prop="contribution" label="贡献值" width="100">
              <template #default="{ row }">
                <span :style="{ color: row.contribution > 0 ? '#f56c6c' : '#67c23a' }">
                  {{ row.contribution > 0 ? '+' : '' }}{{ row.contribution.toFixed(4) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="direction" label="方向" width="80">
              <template #default="{ row }">
                <el-tag :type="row.direction === 'positive' ? 'danger' : 'success'" size="small">
                  {{ row.direction === 'positive' ? '增加风险' : '降低风险' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section" v-if="detail.llmAnalysis">
          <h4>AI 分析报告</h4>
          <div class="llm-content" v-html="marked.parse(detail.llmAnalysis)"></div>
        </div>

        <div class="detail-section" v-if="detail.suggestions">
          <h4>防治建议</h4>
          <div class="suggestion-content">{{ detail.suggestions }}</div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import {
  Search, Plus,
} from '@element-plus/icons-vue'
import {
  getPredictionHistory,
  getLatestPrediction,
  triggerPrediction,
  parsePrediction,
  RISK_LEVEL_MAP,
  type RiskPredictionVO,
  type PredictionDetail,
} from '@/api/modules/prediction'
import { getOrchardList } from '@/api/modules/orchard'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

// 特征名称中文映射
const FEATURE_NAMES: Record<string, string> = {
  heat_humid_days_7d_ge3: '高温高湿≥3天',
  heat_humid_days_7d: '高温高湿天数',
  disease_diversity_14d: '病害多样性',
  disease_risk_index: '病害三角风险指数',
  detection_freq_S14_7d: '溃疡病检测频次',
  detection_freq_S10_7d: '炭疽病检测频次',
  detection_freq_S3_S4_7d: '黄龙病检测频次',
  dpi_S14_7d: '溃疡病压力指数(7d)',
  dpi_S14_14d: '溃疡病压力指数(14d)',
  dpi_S10_7d: '炭疽病压力指数(7d)',
  dpi_S10_14d: '炭疽病压力指数(14d)',
  dpi_S3_S4_7d: '黄龙病压力指数(7d)',
  dpi_S3_S4_14d: '黄龙病压力指数(14d)',
  days_since_last_S14: '距上次溃疡病天数',
  days_since_last_S10: '距上次炭疽病天数',
  days_since_last_S3_S4: '距上次黄龙病天数',
  temp_mean_7d: '平均气温(7d)',
  temp_max_7d: '最高气温(7d)',
  temp_min_7d: '最低气温(7d)',
  temp_std_7d: '气温标准差(7d)',
  temp_mean_14d: '平均气温(14d)',
  humidity_mean_7d: '平均湿度(7d)',
  humidity_max_7d: '最高湿度(7d)',
  humidity_min_7d: '最低湿度(7d)',
  humidity_mean_14d: '平均湿度(14d)',
  precip_cumul_7d: '累积降雨(7d)',
  precip_cumul_14d: '累积降雨(14d)',
  precip_days_7d: '降雨天数(7d)',
  wind_mean_7d: '平均风速(7d)',
  soil_temp_mean_7d: '土壤温度(7d)',
  soil_moisture_mean_7d: '土壤含水率(7d)',
  soil_ph_mean_7d: '土壤PH值(7d)',
  soil_conductivity_mean_7d: '土壤电导率(7d)',
  th_index_7d: '温湿综合指数(7d)',
  temp_humidity_interact: '温湿交互项',
  forecast_temp_mean_3d: '预报气温均值(3d)',
  forecast_humidity_mean_3d: '预报湿度均值(3d)',
  forecast_pop_mean_3d: '预报降水概率(3d)',
  forecast_precip_cumul_3d: '预报累积雨量(3d)',
  forecast_precip_days_3d: '预报降雨天数(3d)',
  phenology_春梢萌发期: '物候-春梢萌发期',
  phenology_开花期: '物候-开花期',
  phenology_果实膨大期: '物候-果实膨大期',
  phenology_果实着色期: '物候-果实着色期',
  phenology_成熟采收期: '物候-成熟采收期',
}

function transFeature(name: string): string {
  return FEATURE_NAMES[name] || name
}

const userStore = useUserStore()

const probColors = ['#67c23a', '#e6a23c', '#f56c6c', '#c03636']

// ── 搜索 ──
const searchForm = ref({
  orchardId: undefined as number | undefined,
  riskLevel: undefined as number | undefined,
  triggerType: undefined as string | undefined,
})

const orchardOptions = ref<{ id: number; name: string }[]>([])

// ── 表格数据 ──
const loading = ref(false)
const tableData = ref<(PredictionDetail & { orchardName?: string })[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

// ── 详情弹窗 ──
const detailVisible = ref(false)
const detail = ref<PredictionDetail | null>(null)

function triggerLabel(type: string) {
  const map: Record<string, string> = {
    scheduled: '每日定时',
    detection_burst: '检测爆发',
    seasonal: '高发期',
    manual: '手动触发',
  }
  return map[type] || type
}

function triggerTagType(type: string) {
  const map: Record<string, string> = {
    scheduled: '',
    detection_burst: 'warning',
    seasonal: 'danger',
    manual: 'info',
  }
  return map[type] || ''
}

async function fetchData() {
  loading.value = true
  try {
    const orchards: any[] = await getOrchardList() as any
    const orchardMap = new Map(orchards.map((o: any) => [o.id, o.name]))
    const allData: (PredictionDetail & { orchardName?: string })[] = []

    // 如果选了特定果园 → 查全部历史记录
    if (searchForm.value.orchardId) {
      try {
        const history: any[] = await getPredictionHistory({
          orchardId: searchForm.value.orchardId,
          limit: 200,
        }) as any
        for (const vo of history || []) {
          if (!vo || !vo.id) continue
          const parsed = parsePrediction(vo)
          allData.push({ ...parsed, orchardName: orchardMap.get(vo.orchardId) || '' })
        }
      } catch { /* ignore */ }
    } else {
      // 未选果园 → 每个果园只看最新一条
      for (const o of orchards) {
        try {
          const vo: any = await getLatestPrediction(o.id)
          if (!vo || !vo.id) continue
          const parsed = parsePrediction(vo)
          allData.push({ ...parsed, orchardName: o.name })
        } catch { /* ignore */ }
      }
    }

    // 过滤
    let filtered = allData
    if (searchForm.value.riskLevel !== undefined) {
      filtered = filtered.filter(d => d.riskLevel === searchForm.value.riskLevel)
    }
    if (searchForm.value.triggerType) {
      filtered = filtered.filter(d => d.triggerType === searchForm.value.triggerType)
    }
    if (searchForm.value.orchardId) {
      filtered = filtered.filter(d => d.orchardId === searchForm.value.orchardId)
    }

    // 按时间排序
    filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

    total.value = filtered.length
    const start = (page.value - 1) * pageSize.value
    tableData.value = filtered.slice(start, start + pageSize.value)
  } catch (e) {
    console.error('获取预测数据失败:', e)
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  page.value = 1
  await fetchData()
}

function handleReset() {
  searchForm.value = { orchardId: undefined, riskLevel: undefined, triggerType: undefined }
  page.value = 1
  fetchData()
}

function handleViewDetail(row: PredictionDetail) {
  detail.value = row
  detailVisible.value = true
}

async function handleTriggerPredict() {
  if (!searchForm.value.orchardId) {
    ElMessage.warning('请先选择一个果园')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认对所选果园(ID=${searchForm.value.orchardId})触发预测？`,
      '确认触发',
      { type: 'info' }
    )
    await triggerPrediction(searchForm.value.orchardId, 'manual')
    ElMessage.success('预测触发成功')
    await fetchData()
  } catch {
    // cancelled
  }
}

onMounted(async () => {
  try {
    const list: any[] = await getOrchardList() as any
    orchardOptions.value = list.map(o => ({ id: o.id, name: o.name }))
  } catch { /* ignore */ }
  await fetchData()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.prob-bar {
  display: flex;
  height: 14px;
  border-radius: 7px;
  overflow: hidden;
  background: #f0f0f0;

  &.large {
    height: 24px;
    border-radius: 12px;
  }
}

.prob-segment {
  transition: width 0.3s;
  min-width: 2px;
}

.prob-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 12px;
  color: #666;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  .risk-badge {
    font-size: 16px;
    padding: 6px 16px;
  }

  .detail-score {
    font-size: 14px;
    color: #666;
  }

  .detail-time {
    font-size: 12px;
    color: #999;
    margin-left: auto;
  }
}

.detail-section {
  margin-bottom: 20px;

  h4 {
    margin: 0 0 10px;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    padding-left: 10px;
    border-left: 3px solid #409eff;
  }
}

.llm-content {
  background: #f9fafb;
  border-radius: 8px;
  padding: 14px 20px;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}
.llm-content :deep(h1), .llm-content :deep(h2),
.llm-content :deep(h3), .llm-content :deep(h4) {
  margin: 16px 0 8px; color: #1a1a1a;
}
.llm-content :deep(p) { margin: 8px 0; }
.llm-content :deep(ul), .llm-content :deep(ol) { padding-left: 20px; margin: 8px 0; }
.llm-content :deep(li) { margin: 4px 0; }
.llm-content :deep(strong) { color: #1a1a1a; }
.llm-content :deep(hr) { margin: 16px 0; border: none; border-top: 1px solid #e5e5e5; }

.suggestion-content {
  background: #f9fafb;
  border-radius: 8px;
  padding: 14px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  color: #333;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
