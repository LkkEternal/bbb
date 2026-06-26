<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成员管理</h2>
      <el-button type="primary" :icon="Plus" @click="showAddDialog = true">添加成员</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="selectedOrchard" placeholder="选择果园" style="width: 200px" @change="handleOrchardChange">
        <el-option v-for="o in orchardList" :key="o.id" :label="o.name" :value="o.id" />
      </el-select>
    </div>

    <el-table :data="memberList" v-loading="loading" style="width: 100%">
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="phone" label="手机号" width="140">
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

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showAddDialog" title="添加成员" width="450px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" placeholder="请输入用户ID" style="width: 100%" />
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
import { Plus } from '@element-plus/icons-vue'
import { getOrchardList, getOrchardDetail, addMember, removeMember } from '@/api/modules/orchard'
import { ORCHARD_ROLE_MAP } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const selectedOrchard = ref<number | ''>('')
const orchardList = ref<any[]>([])
const memberList = ref<any[]>([])
const currentOrchardRole = ref('')

const formRef = ref<FormInstance>()
const form = reactive({
  userId: undefined as number | undefined,
  roleType: 'viewer',
})

const rules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

// 是否可以添加管理员（只有园主可以）
const canAddManager = computed(() => {
  return currentOrchardRole.value === 'owner'
})

// 判断是否可以移除该成员
function canRemoveMember(member: any): boolean {
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

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

async function fetchOrchards() {
  try {
    const data: any = await getOrchardList()
    orchardList.value = Array.isArray(data) ? data : data.records ?? []
    if (orchardList.value.length > 0 && !selectedOrchard.value) {
      selectedOrchard.value = orchardList.value[0].id
      handleOrchardChange()
    }
  } catch {}
}

async function handleOrchardChange() {
  if (!selectedOrchard.value) return
  loading.value = true
  try {
    const data: any = await getOrchardDetail(Number(selectedOrchard.value))
    memberList.value = data.members ?? []
    // 获取当前用户在该果园的角色
    const currentMember = memberList.value.find((m: any) => m.userId === userStore.userInfo?.id)
    currentOrchardRole.value = currentMember?.roleType || ''
  } catch {} finally {
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
    handleOrchardChange()
  } catch {} finally {
    submitting.value = false
  }
}

async function handleRemove(member: any) {
  try {
    await ElMessageBox.confirm(`确定移除成员「${member.username}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await removeMember(Number(selectedOrchard.value), member.userId)
    ElMessage.success('移除成功')
    handleOrchardChange()
  } catch {}
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
</style>
