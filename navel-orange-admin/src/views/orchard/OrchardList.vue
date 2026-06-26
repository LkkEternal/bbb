<template>
  <div class="page-container">
    <div class="page-header">
      <h2>果园管理</h2>
      <el-button v-if="!userStore.isAdmin" type="primary" :icon="Plus" @click="showAddDialog = true">新增果园</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索果园名称/地址"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
    </div>

      <el-table
      :data="pagedList"
      v-loading="loading"
      style="width: 100%"
      highlight-current-row
    >
      <el-table-column prop="name" label="果园名称" min-width="120" show-overflow-tooltip />
      <el-table-column label="地址" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.address || row.formattedAddress || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="area" label="面积(亩)" width="100" align="center" />
      <el-table-column prop="treeAge" label="树龄(年)" width="100" align="center" />
      <el-table-column prop="variety" label="品种" width="100" align="center" />
      <el-table-column prop="deviceCount" label="设备数" width="80" align="center" />
      <el-table-column label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            v-if="row.roleType && ORCHARD_ROLE_MAP[row.roleType]"
            :color="ORCHARD_ROLE_MAP[row.roleType].color"
            effect="dark"
            size="small"
            style="border: none"
          >
            {{ ORCHARD_ROLE_MAP[row.roleType].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/orchard/${row.id}`)">详情</el-button>
          <el-button v-if="!userStore.isAdmin" link type="primary" @click="$router.push(`/orchard/${row.id}/devices`)">设备</el-button>
          <el-button v-if="!userStore.isAdmin" link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="!userStore.isAdmin && row.roleType === 'owner'" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredList.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>

    <!-- 新增果园对话框 -->
    <el-dialog v-model="showAddDialog" title="新增果园" width="520px" destroy-on-close>
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="90px">
        <el-form-item label="果园名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入果园名称" />
        </el-form-item>
        <el-form-item label="详细地址" prop="formattedAddress">
          <el-input v-model="addForm.formattedAddress" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="面积(亩)" prop="areaMu">
          <el-input-number v-model="addForm.areaMu" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="树龄(年)" prop="treeAge">
          <el-input-number v-model="addForm.treeAge" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="addForm.variety" placeholder="请输入品种" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑果园对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑果园" width="520px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="果园名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入果园名称" />
        </el-form-item>
        <el-form-item label="详细地址" prop="formattedAddress">
          <el-input v-model="editForm.formattedAddress" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="面积(亩)">
          <el-input-number v-model="editForm.areaMu" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="树龄(年)">
          <el-input-number v-model="editForm.treeAge" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="品种">
          <el-input v-model="editForm.variety" placeholder="请输入品种" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getOrchardList, createOrchard, updateOrchard, deleteOrchard } from '@/api/modules/orchard'
import { ORCHARD_ROLE_MAP } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

interface OrchardItem {
  id: number
  name: string
  address: string
  formattedAddress: string
  area: number
  areaMu: number
  treeAge: number
  variety: string
  deviceCount: number
  roleType: string
  createdAt: string
}

const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const list = ref<OrchardItem[]>([])
const editingId = ref<number>(0)

const addFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

const addForm = reactive({
  name: '',
  formattedAddress: '',
  areaMu: undefined as number | undefined,
  treeAge: undefined as number | undefined,
  variety: '',
})

const editForm = reactive({
  name: '',
  formattedAddress: '',
  areaMu: undefined as number | undefined,
  treeAge: undefined as number | undefined,
  variety: '',
})

const addRules: FormRules = {
  name: [{ required: true, message: '请输入果园名称', trigger: 'blur' }],
  formattedAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

const editRules: FormRules = {
  name: [{ required: true, message: '请输入果园名称', trigger: 'blur' }],
  formattedAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  const kw = keyword.value.toLowerCase()
  return list.value.filter(
    item => item.name.toLowerCase().includes(kw) || (item.address || item.formattedAddress || '').toLowerCase().includes(kw),
  )
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function handleSearch() {
  currentPage.value = 1
}

function handleEdit(row: any) {
  editingId.value = row.id
  editForm.name = row.name
  editForm.formattedAddress = row.address || row.formattedAddress || ''
  editForm.areaMu = row.areaMu || row.area
  editForm.treeAge = row.treeAge
  editForm.variety = row.variety || ''
  showEditDialog.value = true
}

async function fetchList() {
  loading.value = true
  try {
    const data: any = await getOrchardList()
    list.value = Array.isArray(data) ? data : data.records ?? []
  } catch {
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createOrchard({
      name: addForm.name,
      formattedAddress: addForm.formattedAddress,
      areaMu: addForm.areaMu,
      treeAge: addForm.treeAge,
      variety: addForm.variety,
    })
    ElMessage.success('创建成功')
    userStore.setHasOrchard(true)
    router.push('/orchard-dashboard')
  } catch {
  } finally {
    submitting.value = false
  }
}

async function handleUpdate() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateOrchard(editingId.value, {
      name: editForm.name,
      formattedAddress: editForm.formattedAddress,
      areaMu: editForm.areaMu,
      treeAge: editForm.treeAge,
      variety: editForm.variety,
    })
    ElMessage.success('更新成功')
    showEditDialog.value = false
    fetchList()
  } catch {
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除果园「${row.name}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteOrchard(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
  }
}

function resetAddForm() {
  addForm.name = ''
  addForm.formattedAddress = ''
  addForm.areaMu = undefined
  addForm.treeAge = undefined
  addForm.variety = ''
}

onMounted(async () => {
  await fetchList()
  if (route.query.action === 'create') {
    showAddDialog.value = true
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table) {
  cursor: pointer;
}
</style>
