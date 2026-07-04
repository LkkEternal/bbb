<template>
  <div class="page-container">
    <div class="page-header">
      <h2>果园管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增果园</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索果园名称/地址"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="dataList" v-loading="loading" stripe style="width: 100%">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="name" label="果园名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="地址" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.address || row.formattedAddress || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="面积(亩)" width="100" align="center">
        <template #default="{ row }">
          {{ row.area ?? row.areaMu ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="responsiblePerson" label="负责人" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.roleType && ORCHARD_ROLE_MAP[row.roleType]" :color="ORCHARD_ROLE_MAP[row.roleType].color" effect="dark" size="small" style="border: none">
            {{ ORCHARD_ROLE_MAP[row.roleType].label }}
          </el-tag>
          <span v-else>{{ row.responsiblePerson || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/orchard/${row.id}`)">详情</el-button>
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
        :page-sizes="pagination.pageSizes"
        :layout="pagination.layout"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="果园名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入果园名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="详细地址" prop="formattedAddress">
          <el-input v-model="formData.formattedAddress" placeholder="请输入详细地址" maxlength="200" />
        </el-form-item>
        <el-form-item label="面积(亩)" prop="areaMu">
          <el-input-number v-model="formData.areaMu" :min="0" :precision="1" :step="0.1" style="width: 100%" placeholder="请输入面积" />
        </el-form-item>
        <el-form-item label="负责人" prop="responsiblePerson">
          <el-input v-model="formData.responsiblePerson" placeholder="请输入负责人姓名" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getOrchardList, createOrchard, updateOrchard, deleteOrchard } from '@/api/modules/orchard'
import type { OrchardListVO, CreateOrchardDTO, UpdateOrchardDTO } from '@/api/modules/orchard'
import { ORCHARD_ROLE_MAP } from '@/constants/enums'
import dayjs from 'dayjs'

defineOptions({ name: 'OrchardList' })

// ─── 类型 ───
interface FormData {
  name: string
  formattedAddress: string
  areaMu: number | undefined
  responsiblePerson: string
}

// ─── 响应式状态 ───
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增果园')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const dataList = ref<OrchardListVO[]>([])
const fullList = ref<OrchardListVO[]>([])

const searchForm = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [10, 20, 50],
  layout: 'total, sizes, prev, pager, next, jumper',
})

const formData = reactive<FormData>({
  name: '',
  formattedAddress: '',
  areaMu: undefined,
  responsiblePerson: '',
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入果园名称', trigger: 'blur' }],
  formattedAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

// ─── 工具函数 ───
function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function resetForm() {
  formData.name = ''
  formData.formattedAddress = ''
  formData.areaMu = undefined
  formData.responsiblePerson = ''
  editingId.value = null
  formRef.value?.clearValidate()
}

// ─── 搜索 & 分页 ───
function onSearch() {
  pagination.page = 1
  applyFilter()
}

function applyFilter() {
  const kw = searchForm.keyword.trim().toLowerCase()
  if (!kw) {
    dataList.value = [...fullList.value]
  } else {
    dataList.value = fullList.value.filter(
      item =>
        item.name.toLowerCase().includes(kw) ||
        (item.address || item.formattedAddress || '').toLowerCase().includes(kw),
    )
  }
  pagination.total = dataList.value.length
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
}

function handleCurrentChange(page: number) {
  pagination.page = page
}

// ─── 数据获取 ───
async function fetchList() {
  loading.value = true
  try {
    const data: any = await getOrchardList()
    fullList.value = Array.isArray(data) ? data : data.records ?? []
    applyFilter()
  } catch {
    ElMessage.error('获取果园列表失败')
  } finally {
    loading.value = false
  }
}

// ─── 新增 / 编辑对话框 ───
function openDialog(title: string = '新增', row?: OrchardListVO) {
  resetForm()
  if (title === '编辑' && row) {
    dialogTitle.value = '编辑果园'
    editingId.value = row.id
    formData.name = row.name
    formData.formattedAddress = row.address || row.formattedAddress || ''
    formData.areaMu = row.areaMu ?? row.area ?? undefined
    formData.responsiblePerson = (row as any).responsiblePerson || ''
  } else {
    dialogTitle.value = '新增果园'
  }
  dialogVisible.value = true
}

// ─── 提交 ───
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (editingId.value) {
      // 编辑
      const payload: UpdateOrchardDTO = {
        name: formData.name,
        formattedAddress: formData.formattedAddress,
        areaMu: formData.areaMu,
      }
      await updateOrchard(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      // 新增
      const payload: CreateOrchardDTO = {
        name: formData.name,
        formattedAddress: formData.formattedAddress,
        areaMu: formData.areaMu,
      }
      await createOrchard(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch {
    // 后端已处理错误消息
  } finally {
    submitting.value = false
  }
}

// ─── 删除 ───
async function handleDelete(row: OrchardListVO) {
  try {
    await ElMessageBox.confirm(`确定删除果园「${row.name}」？删除后数据不可恢复。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteOrchard(row.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // 用户取消或操作失败
  }
}

// ─── 生命周期 ───
onMounted(async () => {
  await fetchList()
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
</style>
