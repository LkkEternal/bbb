<template>
  <div class="page-container">
    <div class="page-header">
      <h2>设备管理</h2>
      <el-button v-if="!userStore.isAdmin" type="primary" :icon="Plus" @click="openDialog()">
        新增设备
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="filterOrchard" placeholder="选择果园" clearable style="width: 200px" @change="handleSearch">
        <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索设备名称/编号"
        clearable
        :prefix-icon="Search"
        style="width: 250px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" :loading="syncing" @click="handleSyncAll">云端同步</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="pagedList" v-loading="loading" stripe style="width: 100%">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="name" label="设备名称" min-width="130" show-overflow-tooltip />
      <el-table-column prop="deviceCode" label="设备编号" width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'camera' ? 'primary' : 'success'" size="small">
            {{ DEVICE_TYPE_MAP[row.type] || row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <span
            class="status-dot"
            :style="{ backgroundColor: DEVICE_STATUS_MAP[row.status]?.color || '#909399' }"
          />
          {{ DEVICE_STATUS_MAP[row.status]?.label || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="orchardName" label="所属果园" width="120" show-overflow-tooltip />
      <el-table-column label="最后在线时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.lastHeartbeat) }}
        </template>
      </el-table-column>
      <el-table-column prop="model" label="型号" width="110" show-overflow-tooltip />
      <el-table-column label="操作" width="270" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleSensorDetail(row)">详情</el-button>
          <el-button v-if="!userStore.isAdmin" link type="primary" @click="openDialog('编辑', row)">编辑</el-button>
          <el-button v-if="!userStore.isAdmin" link type="warning" @click="handleSyncOne(row)">同步</el-button>
          <el-button v-if="!userStore.isAdmin" link type="danger" @click="handleDelete(row)">注销</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredList.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="onPageChange"
        @current-change="onPageChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="formData.deviceCode" placeholder="请输入设备编号" :disabled="isEditing" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="摄像头" value="camera" />
            <el-option label="传感器" value="sensor" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属果园" prop="orchardId">
          <el-select v-model="formData.orchardId" placeholder="请选择果园" style="width: 100%">
            <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="formData.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="formData.locationDesc" placeholder="请输入安装位置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 传感器详情弹窗 -->
    <el-dialog v-model="sensorDialogVisible" title="传感器数据" width="460px" destroy-on-close>
      <div v-loading="sensorLoading">
        <template v-if="sensorData && Object.keys(sensorData).length > 0">
          <div class="sensor-grid">
            <div v-for="(val, key) in sensorLabels" :key="key" class="sensor-item">
              <div class="sensor-value" :style="{ color: sensorColor(key, sensorData[key]) }">
                {{ sensorData[key] ?? '--' }}<span class="sensor-unit">{{ sensorUnit(key) }}</span>
              </div>
              <div class="sensor-label">{{ val }}</div>
            </div>
          </div>
          <div v-if="sensorData.updateTime" style="margin-top:12px;font-size:12px;color:#909399;text-align:center">
            更新时间：{{ formatTime(sensorData.updateTime) }}
          </div>
        </template>
        <el-empty v-else description="暂无传感器数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getOrchardList,
  getDeviceList,
  registerDevice,
  updateDevice,
  deleteDevice,
  syncAllDevices,
  syncDevice,
  getDeviceSensors,
} from '@/api/modules/orchard'
import { DEVICE_TYPE_MAP, DEVICE_STATUS_MAP } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

defineOptions({ name: 'DeviceList' })

const userStore = useUserStore()

// ─── 响应式状态 ───
const loading = ref(false)
const syncing = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const dialogTitle = ref('新增设备')
const keyword = ref('')
const filterOrchard = ref<number | ''>('')
const currentPage = ref(1)
const pageSize = ref(10)
const editingId = ref<number | null>(null)
const orchardList = ref<any[]>([])
const allDevices = ref<any[]>([])

const formRef = ref<FormInstance>()

const formData = reactive({
  name: '',
  deviceCode: '',
  type: 'camera',
  orchardId: undefined as number | undefined,
  model: '',
  locationDesc: '',
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  type: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  orchardId: [{ required: true, message: '请选择所属果园', trigger: 'change' }],
}

const isEditing = computed(() => !!editingId.value)

// ─── 计算属性：搜索过滤 + 分页 ───
const filteredList = computed(() => {
  let list = allDevices.value

  // 果园筛选
  if (filterOrchard.value) {
    list = list.filter(d => d.orchardId === filterOrchard.value)
  }

  // 关键词搜索（设备名称/编号）
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(
      d =>
        d.name?.toLowerCase().includes(kw) ||
        d.deviceCode?.toLowerCase().includes(kw),
    )
  }

  return list
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// ─── 工具函数 ───
function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// ─── 数据加载 ───
async function fetchOrchards() {
  try {
    const data: any = await getOrchardList()
    orchardList.value = Array.isArray(data) ? data : data.records ?? []
  } catch {
    /* ignore */
  }
}

async function fetchDevices() {
  loading.value = true
  try {
    const devices: any[] = []
    const orchards =
      filterOrchard.value
        ? orchardList.value.filter(o => o.id === filterOrchard.value)
        : orchardList.value

    for (const orchard of orchards) {
      try {
        const data: any = await getDeviceList(orchard.id)
        const list = Array.isArray(data) ? data : data.records ?? []
        list.forEach((d: any) => {
          d.orchardId = orchard.id
          d.orchardName = orchard.name
        })
        devices.push(...list)
      } catch {
        /* ignore orchard without permission */
      }
    }

    allDevices.value = devices
  } catch {
    /* ignore */
  } finally {
    loading.value = false
  }
}

// ─── 搜索 ───
function handleSearch() {
  currentPage.value = 1
  fetchDevices()
}

function onPageChange() {
  // 分页变化时只需重新计算 computed，无需额外操作
}

// ─── 新增/编辑 ───
function openDialog(title: string = '新增设备', row?: any) {
  dialogTitle.value = title
  showDialog.value = true
  editingId.value = row?.id ?? null

  if (row) {
    formData.name = row.name
    formData.deviceCode = row.deviceCode
    formData.type = row.type || 'camera'
    formData.orchardId = row.orchardId
    formData.model = row.model || ''
    formData.locationDesc = row.locationDesc || ''
  } else {
    formData.name = ''
    formData.deviceCode = ''
    formData.type = 'camera'
    formData.orchardId = undefined
    formData.model = ''
    formData.locationDesc = ''
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingId.value) {
      // 更新设备
      await updateDevice(editingId.value, {
        name: formData.name,
        model: formData.model || undefined,
        locationDesc: formData.locationDesc || undefined,
      })
      ElMessage.success('更新成功')
    } else {
      // 新增设备
      await registerDevice({
        orchardId: formData.orchardId!,
        deviceCode: formData.deviceCode,
        name: formData.name,
        type: formData.type,
        model: formData.model || undefined,
        locationDesc: formData.locationDesc || undefined,
      })
      ElMessage.success('新增成功')
    }
    showDialog.value = false
    fetchDevices()
  } catch {
    /* handled by interceptor */
  } finally {
    submitting.value = false
  }
}

// ─── 删除 ───
async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定注销设备「${row.name}」？注销后将从果园移除。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await deleteDevice(row.id)
    ElMessage.success('注销成功')
    fetchDevices()
  } catch {
    /* cancelled or error */
  }
}

// ─── 云端同步 ───
async function handleSyncAll() {
  syncing.value = true
  try {
    await syncAllDevices()
    ElMessage.success('同步成功')
    fetchDevices()
  } catch {
    /* ignore */
  } finally {
    syncing.value = false
  }
}

async function handleSyncOne(row: any) {
  try {
    await syncDevice(row.id)
    ElMessage.success('同步成功')
    fetchDevices()
  } catch {
    /* ignore */
  }
}

// ─── 传感器详情 ───
const sensorDialogVisible = ref(false)
const sensorLoading = ref(false)
const sensorData = ref<any>({})

const sensorLabels: Record<string, string> = {
  air_temp: '空气温度',
  humidity: '湿度',
  soil_temp: '土壤温度',
  moisture: '土壤含水率',
  conductivity: '电导率',
  ph: 'PH值',
  battery: '电量',
}

function sensorUnit(key: string): string {
  const units: Record<string, string> = {
    air_temp: '°C', humidity: '%', soil_temp: '°C', moisture: '%',
    conductivity: 'uS/cm', ph: '', battery: 'V',
  }
  return units[key] || ''
}

function sensorColor(key: string, val: any): string {
  if (val == null || val === '--') return '#909399'
  const n = Number(val)
  if (key === 'air_temp' || key === 'soil_temp') {
    if (n > 35 || n < 5) return '#F87171'
    if (n > 30 || n < 10) return '#FBBF24'
    return '#34D399'
  }
  if (key === 'humidity') {
    if (n > 85 || n < 20) return '#F87171'
    return '#34D399'
  }
  if (key === 'ph') {
    if (n < 6 || n > 7.5) return '#F87171'
    return '#34D399'
  }
  if (key === 'battery') {
    if (n < 10) return '#F87171'
    if (n < 11.5) return '#FBBF24'
    return '#34D399'
  }
  return '#60A5FA'
}

async function handleSensorDetail(row: any) {
  sensorDialogVisible.value = true
  sensorLoading.value = true
  sensorData.value = {}
  try {
    const data: any = await getDeviceSensors(row.id)
    sensorData.value = data || {}
  } catch {
    sensorData.value = {}
  } finally {
    sensorLoading.value = false
  }
}

// ─── 生命周期 ───
onMounted(async () => {
  await fetchOrchards()
  fetchDevices()
})
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

.sensor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.sensor-item {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}
.sensor-value {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}
.sensor-unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 2px;
}
.sensor-label {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}
</style>
