<template>
  <div class="page-container">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="$router.push('/orchard')">返回</el-button>
      <h2>果园详情</h2>
    </div>

    <el-card v-loading="loading" shadow="never" class="detail-card">
      <template #header>
        <span class="card-title">基本信息</span>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="field in displayFields"
          :key="field.prop"
          :label="field.label"
          :span="field.span || 1"
        >
          <!-- 文本 -->
          <template v-if="field.type === 'text'">
            {{ detail[field.prop] ?? '-' }}
          </template>

          <!-- 标签 -->
          <template v-else-if="field.type === 'tag'">
            <el-tag v-if="detail[field.prop]" :type="statusType(detail[field.prop])" effect="plain">
              {{ statusLabel(detail[field.prop]) }}
            </el-tag>
            <span v-else>-</span>
          </template>

          <!-- 日期 -->
          <template v-else-if="field.type === 'date'">
            {{ detail[field.prop] ? dayjs(detail[field.prop]).format(field.format || 'YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>

          <!-- 数字 -->
          <template v-else-if="field.type === 'number'">
            {{ detail[field.prop] ?? '-' }}
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 设备列表 -->
    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="card-title">设备列表（{{ deviceList.length }}）</span>
      </template>
      <el-table :data="deviceList" border stripe v-loading="deviceLoading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="设备名称" min-width="140" />
        <el-table-column prop="deviceCode" label="设备编号" min-width="140" />
        <el-table-column prop="type" label="设备类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="installDate" label="安装日期" width="120" />
      </el-table>
    </el-card>

    <!-- 成员列表 -->
    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="card-title">成员列表（{{ memberList.length }}）</span>
      </template>
      <el-table :data="memberList" border stripe v-loading="memberLoading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column prop="joinDate" label="加入日期" width="120" />
      </el-table>
    </el-card>

    <!-- 识别记录 -->
    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="card-title">识别记录（{{ recordList.length }}）</span>
      </template>
      <el-table :data="recordList" border stripe v-loading="recordLoading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="imageUrl" label="图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              style="width: 50px; height: 50px; border-radius: 4px; cursor: pointer"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="diseaseName" label="病害名称" min-width="120" />
        <el-table-column prop="confidence" label="置信度" width="100">
          <template #default="{ row }">
            <span v-if="row.confidence != null">{{ (row.confidence * 100).toFixed(1) }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="detectedAt" label="识别时间" width="160" />
        <el-table-column prop="handlerName" label="处理人" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import { getOrchardDetail } from '@/api/modules/orchard'

defineOptions({ name: 'OrchardDetail' })

const route = useRoute()
const loading = ref(false)
const detail = ref<Record<string, any>>({})

// 关联列表 loading
const deviceLoading = ref(false)
const memberLoading = ref(false)
const recordLoading = ref(false)

// 关联列表数据
const deviceList = ref<any[]>([])
const memberList = ref<any[]>([])
const recordList = ref<any[]>([])

/** 详情展示字段配置 */
const displayFields = computed(() => [
  { label: '果园名称', prop: 'name', type: 'text' },
  { label: '果园地址', prop: 'address', type: 'text', span: 2 },
  { label: '面积（亩）', prop: 'area', type: 'number' },
  { label: '负责人', prop: 'manager', type: 'text' },
  { label: '联系电话', prop: 'phone', type: 'text' },
  { label: '状态', prop: 'status', type: 'tag' },
  { label: '创建时间', prop: 'createdAt', type: 'date', format: 'YYYY-MM-DD HH:mm:ss' },
  { label: '备注', prop: 'remark', type: 'text', span: 2 },
])

/** 状态标签映射 */
const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '停用', type: 'danger' },
  1: { label: '启用', type: 'success' },
}

function statusType(val: number | string): string {
  return statusMap[Number(val)]?.type ?? 'info'
}

function statusLabel(val: number | string): string {
  return statusMap[Number(val)]?.label ?? String(val ?? '-')
}

/** 获取详情 */
async function fetchDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    detail.value = await getOrchardDetail(id)
  } catch {
    detail.value = {}
  } finally {
    loading.value = false
  }
}

/** 加载设备列表 */
async function fetchDevices() {
  deviceLoading.value = true
  try {
    // TODO: 替换为真实接口调用
    deviceList.value = []
  } catch {
    deviceList.value = []
  } finally {
    deviceLoading.value = false
  }
}

/** 加载成员列表 */
async function fetchMembers() {
  memberLoading.value = true
  try {
    // TODO: 替换为真实接口调用
    memberList.value = []
  } catch {
    memberList.value = []
  } finally {
    memberLoading.value = false
  }
}

/** 加载识别记录 */
async function fetchRecords() {
  recordLoading.value = true
  try {
    // TODO: 替换为真实接口调用
    recordList.value = []
  } catch {
    recordList.value = []
  } finally {
    recordLoading.value = false
  }
}

onMounted(() => {
  fetchDetail()
  fetchDevices()
  fetchMembers()
  fetchRecords()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.detail-card {
  margin-bottom: 20px;

  :deep(.el-card__header) {
    padding: 14px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
