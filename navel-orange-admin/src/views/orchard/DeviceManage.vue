<template>
  <div class="page-container">
    <el-page-header @back="$router.back()" content="设备管理" style="margin-bottom: 24px" />

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="showRegisterDialog = true">注册设备</el-button>
    </div>

    <el-table :data="deviceList" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="name" label="设备名称" min-width="120" />
      <el-table-column prop="deviceCode" label="设备编码" width="160" />
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
      <el-table-column prop="lastHeartbeat" label="最近心跳" width="170" align="center" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showRegisterDialog" title="注册设备" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="摄像头" value="camera" />
            <el-option label="传感器" value="sensor" />
          </el-select>
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="安装位置" prop="locationDesc">
          <el-input v-model="form.locationDesc" placeholder="请输入安装位置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleRegister">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDeviceList, registerDevice, deleteDevice } from '@/api/modules/orchard'
import { DEVICE_TYPE_MAP, DEVICE_STATUS_MAP } from '@/constants/disease'

const route = useRoute()
const orchardId = Number(route.params.id)

const loading = ref(false)
const submitting = ref(false)
const showRegisterDialog = ref(false)
const deviceList = ref<any[]>([])

const formRef = ref<FormInstance>()
const form = reactive({
  deviceCode: '',
  name: '',
  type: '',
  model: '',
  locationDesc: '',
})

const rules: FormRules = {
  deviceCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
}

async function fetchDevices() {
  loading.value = true
  try {
    const data: any = await getDeviceList(orchardId)
    deviceList.value = Array.isArray(data) ? data : data.records ?? []
  } catch {
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await registerDevice({
      deviceCode: form.deviceCode,
      orchardId,
      name: form.name,
      type: form.type,
      model: form.model || undefined,
      locationDesc: form.locationDesc || undefined,
    })
    ElMessage.success('注册成功')
    showRegisterDialog.value = false
    resetForm()
    fetchDevices()
  } catch {
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除设备「${row.name}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteDevice(row.id)
    ElMessage.success('删除成功')
    fetchDevices()
  } catch {
  }
}

function resetForm() {
  form.deviceCode = ''
  form.name = ''
  form.type = ''
  form.model = ''
  form.locationDesc = ''
}

onMounted(fetchDevices)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  justify-content: flex-end;
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
</style>
