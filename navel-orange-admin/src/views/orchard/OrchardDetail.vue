<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/orchard')" content="果园详情" style="margin-bottom: 24px" />

    <div v-loading="loading">
      <el-card style="margin-bottom: 20px">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="果园名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="品种">{{ detail.variety || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ detail.address || detail.formattedAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面积">{{ detail.area ? `${detail.area} 亩` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="树龄">{{ detail.treeAge ? `${detail.treeAge} 年` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(detail.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card style="margin-bottom: 20px">
        <template #header>
          <div class="card-header">
            <span class="card-title">成员列表</span>
            <el-tag type="info" size="small">{{ members.length }} 人</el-tag>
          </div>
        </template>
        <el-table :data="members" style="width: 100%">
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
        </el-table>
      </el-card>

      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">设备列表</span>
            <el-tag type="info" size="small">{{ devices.length }} 台</el-tag>
          </div>
        </template>
        <el-table :data="devices" style="width: 100%">
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
          <el-table-column label="最近心跳" width="170" align="center">
            <template #default="{ row }">
              {{ formatTime(row.lastHeartbeat) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right" v-if="!isAdmin">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/orchard/${route.params.id}/monitor?deviceId=${row.id}`)">监控</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrchardDetail } from '@/api/modules/orchard'
import { ORCHARD_ROLE_MAP } from '@/constants/enums'
import { DEVICE_TYPE_MAP, DEVICE_STATUS_MAP } from '@/constants/disease'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const loading = ref(false)

const detail = reactive<any>({})
const members = ref<any[]>([])
const devices = ref<any[]>([])

function formatTime(time: string | undefined | null): string {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

async function fetchDetail() {
  loading.value = true
  try {
    const data: any = await getOrchardDetail(Number(route.params.id))
    Object.assign(detail, data)
    members.value = data.members ?? []
    devices.value = data.devices ?? []
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
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
