<template>
  <div class="page-container">
    <div class="page-header">
      <h2>设备管理</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Refresh" :loading="syncing" @click="handleSyncAll">云端同步设备</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 已注册设备 -->
      <el-tab-pane label="已注册设备" name="registered">
        <div class="search-bar">
          <el-select v-model="filterOrchard" placeholder="选择果园" clearable style="width: 200px">
            <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
          <el-input
            v-model="keyword"
            placeholder="搜索设备名称/编码"
            clearable
            :prefix-icon="Search"
            style="width: 250px"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        </div>

        <el-table :data="registeredDevices" v-loading="loading" style="width: 100%">
          <el-table-column prop="name" label="设备名称" min-width="120" />
          <el-table-column prop="deviceCode" label="设备编码" width="160" />
          <el-table-column prop="orchardName" label="所属果园" width="120" />
          <el-table-column label="类型" width="100" align="center">
            <template #default="{ row }">
              {{ DEVICE_TYPE_MAP[row.type] || row.type }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="status-dot" :style="{ backgroundColor: DEVICE_STATUS_MAP[row.status]?.color || '#909399' }" />
              {{ DEVICE_STATUS_MAP[row.status]?.label || '未知' }}
            </template>
          </el-table-column>
          <el-table-column prop="model" label="型号" width="120" />
          <el-table-column prop="locationDesc" label="安装位置" min-width="140" show-overflow-tooltip />
          <el-table-column label="最近心跳" width="170" align="center">
            <template #default="{ row }">
              {{ formatTime(row.lastHeartbeat) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <template v-if="!userStore.isAdmin">
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="warning" @click="handleSyncOne(row)">同步</el-button>
                <el-button link type="danger" @click="handleDelete(row)">注销</el-button>
              </template>
              <span v-else class="text-placeholder">-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- IoTDA云端设备 -->
      <el-tab-pane label="IoTDA云端设备" name="iotda">
        <el-table :data="iotdaDevices" v-loading="iotdaLoading" style="width: 100%">
          <el-table-column prop="deviceId" label="设备ID" min-width="180" show-overflow-tooltip />
          <el-table-column prop="deviceName" label="设备名称" width="150" show-overflow-tooltip />
          <el-table-column prop="productName" label="产品名称" width="150" show-overflow-tooltip />
          <el-table-column label="设备标识码" width="150" align="center">
            <template #default="{ row }">
              {{ row.deviceId?.split('_')[1] || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.registered" type="success" size="small">已注册</el-tag>
              <el-tag v-else type="warning" size="small">未注册</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <template v-if="!userStore.isAdmin">
                <el-button
                  v-if="!row.registered"
                  link
                  type="primary"
                  @click="handleBind(row)"
                >
                  绑定到果园
                </el-button>
                <span v-else class="text-placeholder">已绑定</span>
              </template>
              <span v-else class="text-placeholder">{{ row.registered ? '已绑定' : '未注册' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑设备对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑设备" width="450px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" label-width="90px">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="editForm.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="editForm.locationDesc" placeholder="请输入安装位置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleUpdate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定设备对话框 -->
    <el-dialog v-model="showBindDialog" title="绑定设备到果园" width="520px" destroy-on-close>
      <el-form ref="bindFormRef" :model="bindForm" :rules="bindRules" label-width="90px">
        <el-form-item label="设备信息">
          <span>{{ bindDevice?.deviceName }} ({{ bindDevice?.deviceId }})</span>
        </el-form-item>
        <el-form-item label="所属果园" prop="orchardId">
          <el-select v-model="bindForm.orchardId" placeholder="请选择果园" style="width: 100%">
            <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="bindForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="bindForm.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="摄像头" value="camera" />
            <el-option label="传感器" value="sensor" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="bindForm.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="bindForm.locationDesc" placeholder="请输入安装位置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBindDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleBindConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getOrchardList,
  getDeviceList,
  getIotdaDevices,
  registerDevice,
  updateDevice,
  syncAllDevices,
  syncDevice,
  deleteDevice
} from '@/api/modules/orchard'
import { DEVICE_TYPE_MAP, DEVICE_STATUS_MAP } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

const activeTab = ref('registered')
const loading = ref(false)
const iotdaLoading = ref(false)
const syncing = ref(false)
const submitting = ref(false)
const showEditDialog = ref(false)
const showBindDialog = ref(false)
const keyword = ref('')
const filterOrchard = ref<number | ''>('')
const registeredDevices = ref<any[]>([])
const iotdaDevices = ref<any[]>([])
const orchardList = ref<any[]>([])
const editingDeviceId = ref<number>(0)
const bindDevice = ref<any>(null)

const editFormRef = ref<FormInstance>()
const bindFormRef = ref<FormInstance>()

const editForm = reactive({
  name: '',
  model: '',
  locationDesc: '',
})

const bindForm = reactive({
  orchardId: undefined as number | undefined,
  name: '',
  type: 'camera',
  model: '',
  locationDesc: '',
})

const bindRules: FormRules = {
  orchardId: [{ required: true, message: '请选择果园', trigger: 'change' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
}

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function getStatusType(status: string): 'success' | 'info' | 'warning' | 'danger' {
  const map: Record<string, 'success' | 'info' | 'warning' | 'danger'> = {
    ONLINE: 'success',
    OFFLINE: 'info',
    INACTIVE: 'warning',
  }
  return map[status] || 'info'
}

function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    ONLINE: '在线',
    OFFLINE: '离线',
    INACTIVE: '未激活',
  }
  return map[status] || status || '-'
}

function handleSearch() {
  fetchRegisteredDevices()
}

function handleTabChange(tab: string | number) {
  if (tab === 'registered') {
    fetchRegisteredDevices()
  } else {
    fetchIotdaDevices()
  }
}

async function fetchOrchards() {
  try {
    const data: any = await getOrchardList()
    orchardList.value = Array.isArray(data) ? data : data.records ?? []
  } catch {}
}

async function fetchRegisteredDevices() {
  loading.value = true
  try {
    const allDevices: any[] = []
    const orchards = filterOrchard.value
      ? orchardList.value.filter(o => o.id === filterOrchard.value)
      : orchardList.value

    for (const orchard of orchards) {
      try {
        const data: any = await getDeviceList(orchard.id)
        const devices = Array.isArray(data) ? data : data.records ?? []
        devices.forEach((d: any) => {
          d.orchardId = orchard.id
          d.orchardName = orchard.name
        })
        allDevices.push(...devices)
      } catch {}
    }

    if (keyword.value) {
      const kw = keyword.value.toLowerCase()
      registeredDevices.value = allDevices.filter(d =>
        d.name?.toLowerCase().includes(kw) || d.deviceCode?.toLowerCase().includes(kw)
      )
    } else {
      registeredDevices.value = allDevices
    }
  } catch {} finally {
    loading.value = false
  }
}

async function fetchIotdaDevices() {
  iotdaLoading.value = true
  try {
    const data: any = await getIotdaDevices()
    iotdaDevices.value = Array.isArray(data) ? data : []
  } catch {} finally {
    iotdaLoading.value = false
  }
}

async function handleSyncAll() {
  syncing.value = true
  try {
    await syncAllDevices()
    ElMessage.success('同步成功')
    fetchRegisteredDevices()
  } catch {} finally {
    syncing.value = false
  }
}

async function handleSyncOne(row: any) {
  try {
    await syncDevice(row.id)
    ElMessage.success('同步成功')
    fetchRegisteredDevices()
  } catch {}
}

function handleEdit(row: any) {
  editingDeviceId.value = row.id
  editForm.name = row.name
  editForm.model = row.model || ''
  editForm.locationDesc = row.locationDesc || ''
  showEditDialog.value = true
}

async function handleUpdate() {
  submitting.value = true
  try {
    await updateDevice(editingDeviceId.value, {
      name: editForm.name,
      model: editForm.model || undefined,
      locationDesc: editForm.locationDesc || undefined,
    })
    ElMessage.success('更新成功')
    showEditDialog.value = false
    fetchRegisteredDevices()
  } catch {} finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定注销设备「${row.name}」？注销后将从果园移除。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDevice(row.id)
    ElMessage.success('注销成功')
    fetchRegisteredDevices()
  } catch {}
}

function handleBind(row: any) {
  bindDevice.value = row
  bindForm.name = row.deviceName || row.name || ''
  bindForm.type = 'camera'
  bindForm.model = ''
  bindForm.locationDesc = ''
  bindForm.orchardId = orchardList.value.length === 1 ? orchardList.value[0].id : undefined
  showBindDialog.value = true
}

async function handleBindConfirm() {
  const valid = await bindFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await registerDevice({
      orchardId: bindForm.orchardId!,
      deviceCode: bindDevice.value.deviceId,
      name: bindForm.name,
      type: bindForm.type,
      model: bindForm.model || undefined,
      locationDesc: bindForm.locationDesc || undefined,
    })
    ElMessage.success('绑定成功')
    showBindDialog.value = false
    fetchIotdaDevices()
    fetchRegisteredDevices()
  } catch {} finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchOrchards()
  if (userStore.isAdmin) {
    activeTab.value = 'iotda'
    fetchIotdaDevices()
  } else {
    fetchRegisteredDevices()
  }
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

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

.text-placeholder {
  color: #909399;
  font-size: 12px;
}
</style>
