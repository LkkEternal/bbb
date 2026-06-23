<template>
  <div class="page-container">
    <div class="page-header">
      <h2>识别记录</h2>
      <el-button type="primary" @click="$router.push('/detection/stats')">识别统计</el-button>
    </div>

    <div class="search-bar">
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
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 300px"
        @change="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
    </div>

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
      <el-table-column label="病害" width="140">
        <template #default="{ row }">
          <DiseaseTag :code="row.topClassCode" />
        </template>
      </el-table-column>
      <el-table-column label="置信度" width="100" align="center">
        <template #default="{ row }">
          {{ (row.topProbability * 100).toFixed(1) }}%
        </template>
      </el-table-column>
      <el-table-column prop="orchardName" label="果园" min-width="120" show-overflow-tooltip />
      <el-table-column prop="deviceCode" label="设备" width="120" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="识别时间" width="170" align="center" />
    </el-table>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getDetectionList } from '@/api/modules/detection'
import { DISEASE_CODE_OPTIONS } from '@/constants/disease'
import DiseaseTag from '@/components/common/DiseaseTag.vue'

const loading = ref(false)
const diseaseCode = ref('')
const dateRange = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<any[]>([])

function handleSearch() {
  currentPage.value = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
    }
    if (diseaseCode.value) params.diseaseCode = diseaseCode.value
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const data: any = await getDetectionList(params)
    list.value = Array.isArray(data) ? data : data.records ?? []
    total.value = data.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
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

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.thumb-image {
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
