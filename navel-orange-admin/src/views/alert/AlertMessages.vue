<template>
  <div class="page-container">
    <el-page-header @back="$router.push('/alert/rules')">
      <template #content>
        <span class="page-title">预警消息</span>
      </template>
    </el-page-header>

    <el-table :data="list" v-loading="loading" stripe style="width: 100%; margin-top: 20px">
      <el-table-column label="等级" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.level)" effect="dark" size="small">
            L{{ row.level }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
      <el-table-column label="内容" min-width="200">
        <template #default="{ row }">
          <span class="content-ellipsis">{{ row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orchardName" label="果园" width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="70" align="center">
        <template #default="{ row }">
          <span v-if="!row.read" class="unread-dot"></span>
          <span v-else class="read-text">已读</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="170" align="center" />
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!(row as MessageItem).read"
            link
            type="primary"
            @click="handleMarkRead(row as MessageItem)"
          >
            标为已读
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessageList, markAsRead } from '@/api/modules/message'

interface MessageItem {
  id: number
  level: number
  title: string
  content: string
  orchardName: string
  read: boolean
  createdAt: string
}

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<MessageItem[]>([])

function getLevelType(level: number) {
  if (level === 3) return 'danger'
  if (level === 2) return 'warning'
  return 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data: any = await getMessageList({ page: currentPage.value, size: pageSize.value })
    list.value = Array.isArray(data) ? data : data.records ?? []
    total.value = data.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
}

async function handleMarkRead(row: MessageItem) {
  try {
    await markAsRead(row.id)
    row.read = true
    ElMessage.success('已标记为已读')
  } catch {
  }
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.page-title {
  font-size: 18px;
  font-weight: 600;
}

.content-ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.read-text {
  font-size: 12px;
  color: #c0c4cc;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
