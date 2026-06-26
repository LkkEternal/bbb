<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-row :gutter="16" align="middle">
        <el-col :span="8">
          <el-input v-model="keyword" placeholder="搜索手机号/昵称" clearable @keyup.enter="loadData">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadData" :loading="loading">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="userList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" :type="role.code === 'ROLE_ADMIN' ? 'danger' : undefined" size="small">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="170">
          <template #default="{ row }">
            {{ formatTime(row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row)">详情</el-button>
            <el-button link type="primary" @click="openRoleDialog(row)">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ selectedUser?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRoleId" style="width: 100%">
            <el-option v-for="opt in ROLE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value === 'ROLE_ADMIN' ? 2 : 1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRole" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="用户详情" width="520px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ detailUser?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailUser?.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailUser?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailUser?.status === 1 ? 'success' : 'danger'" size="small">
            {{ detailUser?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色" :span="2">
          <el-tag v-for="role in detailUser?.roles" :key="role.id" :type="role.code === 'ROLE_ADMIN' ? 'danger' : undefined" size="small" style="margin-right: 4px">
            {{ role.name }}
          </el-tag>
          <span v-if="!detailUser?.roles?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(detailUser?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ formatTime(detailUser?.lastLoginAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ detailUser?.lastLoginIp || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, updateUserStatus, assignRole } from '@/api/modules/user'
import { ROLE_OPTIONS } from '@/constants/roles'
import dayjs from 'dayjs'

const loading = ref(false)
const assignLoading = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const userList = ref<any[]>([])
const roleDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedUser = ref<any>(null)
const detailUser = ref<any>(null)
const selectedRoleId = ref<number>(1)

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

async function loadData() {
  loading.value = true
  try {
    const data: any = await getUserList({ page: page.value, size: pageSize.value, keyword: keyword.value || undefined })
    userList.value = data.records || []
    total.value = data.total || 0
  } catch {} finally {
    loading.value = false
  }
}

async function handleStatusChange(row: any) {
  try {
    await updateUserStatus(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

function openRoleDialog(row: any) {
  selectedUser.value = row
  selectedRoleId.value = row.roles?.[0]?.code === 'ROLE_ADMIN' ? 2 : 1
  roleDialogVisible.value = true
}

function openDetailDialog(row: any) {
  detailUser.value = row
  detailDialogVisible.value = true
}

async function handleAssignRole() {
  assignLoading.value = true
  try {
    await assignRole(selectedUser.value.id, selectedRoleId.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    loadData()
  } catch {} finally {
    assignLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.filter-card {
  margin-bottom: 16px;
}

.table-card {
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
