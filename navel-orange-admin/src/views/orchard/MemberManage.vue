<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成员管理</h2>
      <el-button
        v-if="canAddMember"
        type="primary"
        :icon="Plus"
        @click="showAddDialog = true"
      >
        添加成员
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select
        v-model="selectedOrchard"
        placeholder="选择果园"
        clearable
        style="width: 220px"
        @change="handleOrchardChange"
      >
        <el-option
          v-for="o in orchardList"
          :key="o.id"
          :label="o.name"
          :value="o.id"
        />
      </el-select>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名/手机号"
        clearable
        :prefix-icon="Search"
        style="width: 240px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="pagedList" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
      <el-table-column label="手机号" width="140">
        <template #default="{ row }">
          {{ row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" width="120" align="center">
        <template #default="{ row }">
          <el-tag
            v-if="ORCHARD_ROLE_MAP[row.roleType]"
            :color="ORCHARD_ROLE_MAP[row.roleType].color"
            effect="dark"
            size="small"
            style="border: none"
          >
            {{ ORCHARD_ROLE_MAP[row.roleType].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="加入时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.joinedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canRemoveMember(row)"
            link
            type="danger"
            @click="handleRemove(row)"
          >
            移除
          </el-button>
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showAddDialog" title="添加成员" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number
            v-model="form.userId"
            :min="1"
            placeholder="请输入用户ID"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleType">
          <el-select v-model="form.roleType" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="manager" :disabled="!canAddManager" />
            <el-option label="查看者" value="viewer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getOrchardList, getOrchardDetail, addMember, removeMember } from '@/api/modules/orchard'
import { ORCHARD_ROLE_MAP } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

defineOptions({ name: 'MemberManageList' })

interface OrchardMember {
  userId: number
  username: string
  phone?: string
  roleType: string
  joinedAt: string
}

interface OrchardItem {
  id: number
  name: string
}

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const selectedOrchard = ref<number | ''>('')
const orchardList = ref<OrchardItem[]>([])
const memberList = ref<OrchardMember[]>([])
const currentOrchardRole = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const formRef = ref<FormInstance>()
const form = reactive({
  userId: undefined as number | undefined,
  roleType: 'viewer',
})

const rules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

// ─── 计算属性 ───

/** 当前用户在该果园是否有添加成员的权限 */
const canAddMember = computed(() => {
  return currentOrchardRole.value === 'owner' || currentOrchardRole.value === 'manager'
})

/** 是否可以添加管理员（只有园主可以） */
const canAddManager = computed(() => {
  return currentOrchardRole.value === 'owner'
})

/** 搜索过滤后的成员列表 */
const filteredList = computed(() => {
  if (!searchKeyword.value) return memberList.value
  const kw = searchKeyword.value.toLowerCase()
  return memberList.value.filter(
    m => m.username.toLowerCase().includes(kw) || (m.phone || '').includes(kw),
  )
})

/** 分页后的成员列表 */
const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// ─── 方法 ───

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

/** 判断是否可以移除该成员 */
function canRemoveMember(member: OrchardMember): boolean {
  const myRole = currentOrchardRole.value
  const memberRole = member.roleType

  // 园主可以移除任何人（除了自己）
  if (myRole === 'owner') {
    return member.userId !== userStore.userInfo?.id
  }

  // 管理员只能移除查看者
  if (myRole === 'manager') {
    return memberRole === 'viewer'
  }

  return false
}

function onSearch() {
  currentPage.value = 1
}

function handleSizeChange() {
  currentPage.value = 1
}

function handleCurrentChange() {
  // 分页切换，无需额外操作
}

async function fetchOrchards() {
  try {
    const data: any = await getOrchardList()
    orchardList.value = Array.isArray(data) ? data : data.records ?? []
    if (orchardList.value.length > 0 && !selectedOrchard.value) {
      selectedOrchard.value = orchardList.value[0].id
      await handleOrchardChange()
    }
  } catch {
    /* ignore */
  }
}

async function handleOrchardChange() {
  if (!selectedOrchard.value) {
    memberList.value = []
    return
  }
  loading.value = true
  currentPage.value = 1
  try {
    const data: any = await getOrchardDetail(Number(selectedOrchard.value))
    memberList.value = data.members ?? []
    // 获取当前用户在该果园的角色
    const currentMember = memberList.value.find(
      (m: any) => m.userId === userStore.userInfo?.id,
    )
    currentOrchardRole.value = currentMember?.roleType || ''
  } catch {
    /* ignore */
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !selectedOrchard.value) return

  submitting.value = true
  try {
    await addMember(Number(selectedOrchard.value), {
      userId: form.userId!,
      roleType: form.roleType,
    })
    ElMessage.success('添加成功')
    showAddDialog.value = false
    form.userId = undefined
    form.roleType = 'viewer'
    await handleOrchardChange()
  } catch {
    /* ignore */
  } finally {
    submitting.value = false
  }
}

async function handleRemove(member: OrchardMember) {
  try {
    await ElMessageBox.confirm(
      `确定移除成员「${member.username}」？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await removeMember(Number(selectedOrchard.value), member.userId)
    ElMessage.success('移除成功')
    await handleOrchardChange()
  } catch {
    /* ignore */
  }
}

onMounted(fetchOrchards)
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
