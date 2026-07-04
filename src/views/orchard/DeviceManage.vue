<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2>设备管理</h2>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">
        新增设备
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="设备名称 / 编号"
        clearable
        :prefix-icon="Search"
        style="width: 220px"
        @keyup.enter="onSearch"
      />
      <el-select
        v-model="searchForm.type"
        placeholder="设备类型"
        clearable
        style="width: 140px"
        @change="onSearch"
      >
        <el-option label="全部类型" value="" />
        <el-option label="传感器" value="传感器" />
        <el-option label="摄像头" value="摄像头" />
        <el-option label="灌溉设备" value="灌溉设备" />
        <el-option label="气象站" value="气象站" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-select
        v-model="searchForm.status"
        placeholder="设备状态"
        clearable
        style="width: 130px"
        @change="onSearch"
      >
        <el-option label="全部状态" value="" />
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="onReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table
      :data="dataList"
      v-loading="loading"
      stripe
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="name" label="设备名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="deviceCode" label="设备编号" min-width="150" show-overflow-tooltip />
      <el-table-column prop="type" label="设备类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTagType(row.type)" effect="plain" size="small">
            {{ row.type }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
            {{ row.status === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="installDate" label="安装时间" width="180" align="center">
        <template #default="{ row }">
          {{ row.installDate ? dayjs(row.installDate).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog('编辑', row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入设备名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="formData.deviceCode" placeholder="请输入设备编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="传感器" value="传感器" />
            <el-option label="摄像头" value="摄像头" />
            <el-option label="灌溉设备" value="灌溉设备" />
            <el-option label="气象站" value="气象站" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">在线</el-radio>
            <el-radio :value="0">离线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="安装时间" prop="installDate">
          <el-date-picker
            v-model="formData.installDate"
            type="datetime"
            placeholder="选择安装时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit(formRef)">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, ArrowLeft, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import {
  getDeviceList,
  createDevice,
  updateDevice,
  deleteDevice,
  type DeviceItem,
  type DeviceFormData,
} from '@/api/modules/device'

defineOptions({ name: 'DeviceManageList' })

const route = useRoute()
const router = useRouter()

/** 从路由获取果园ID */
const orchardId = computed(() => Number(route.params.orchardId || route.query.orchardId || 0))

// ====== 搜索 ======
const searchForm = reactive({
  keyword: '',
  type: '',
  status: '' as number | '',
})

// ====== 表格数据 ======
const loading = ref(false)
const dataList = ref<DeviceItem[]>([])
const selectedIds = ref<number[]>([])

// ====== 分页 ======
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// ====== 对话框 ======
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const submitting = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const defaultFormData: DeviceFormData = {
  name: '',
  deviceCode: '',
  type: '',
  status: 1,
  installDate: '',
  orchardId: 0,
  remark: '',
}

const formData = reactive<DeviceFormData>({ ...defaultFormData })

/** 表单校验规则 */
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { max: 50, message: '设备名称不能超过50个字符', trigger: 'blur' },
  ],
  deviceCode: [
    { required: true, message: '请输入设备编号', trigger: 'blur' },
    { max: 50, message: '设备编号不能超过50个字符', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' },
  ],
  status: [
    { required: true, message: '请选择设备状态', trigger: 'change' },
  ],
  installDate: [
    { required: true, message: '请选择安装时间', trigger: 'change' },
  ],
}

/** 设备类型标签颜色 */
function typeTagType(type: string): string {
  const map: Record<string, string> = {
    '传感器': 'primary',
    '摄像头': 'success',
    '灌溉设备': 'warning',
    '气象站': 'info',
    '其他': '',
  }
  return map[type] ?? ''
}

// ====== 数据获取 ======
async function fetchData() {
  if (!orchardId.value) {
    ElMessage.warning('缺少果园ID')
    return
  }
  loading.value = true
  try {
    const res = await getDeviceList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      orchardId: orchardId.value,
      keyword: searchForm.keyword || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
    })
    dataList.value = res.list
    pagination.total = res.total
  } catch {
    dataList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// ====== 搜索与重置 ======
function onSearch() {
  pagination.page = 1
  fetchData()
}

function onReset() {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
  pagination.page = 1
  fetchData()
}

// ====== 分页 ======
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchData()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  fetchData()
}

// ====== 多选 ======
function handleSelectionChange(selection: DeviceItem[]) {
  selectedIds.value = selection.map(item => item.id)
}

// ====== 对话框 ======
function openDialog(title: string = '新增设备', row?: DeviceItem) {
  if (title === '编辑' && row) {
    editingId.value = row.id
    dialogTitle.value = '编辑设备'
    Object.assign(formData, {
      name: row.name,
      deviceCode: row.deviceCode,
      type: row.type,
      status: row.status,
      installDate: row.installDate,
      orchardId: row.orchardId,
      remark: row.remark ?? '',
    })
  } else {
    editingId.value = null
    dialogTitle.value = '新增设备'
    Object.assign(formData, { ...defaultFormData, orchardId: orchardId.value })
  }
  dialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

async function handleSubmit(formEl: FormInstance | undefined) {
  if (!formEl) return
  const valid = await formEl.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingId.value) {
      await updateDevice(editingId.value, { ...formData })
      ElMessage.success('设备更新成功')
    } else {
      await createDevice({ ...formData })
      ElMessage.success('设备新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误由全局拦截处理
  } finally {
    submitting.value = false
  }
}

// ====== 删除 ======
async function handleDelete(row: DeviceItem) {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备「${row.name}」吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' },
    )
    await deleteDevice(row.id)
    ElMessage.success('设备已删除')
    fetchData()
  } catch {
    // 取消删除或错误
  }
}

// ====== 返回 ======
function goBack() {
  router.back()
}

onMounted(() => {
  fetchData()
})
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

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
