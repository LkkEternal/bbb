<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h2>视频监控</h2>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="onReset">刷新</el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索设备名称"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="onReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="dataList"
      v-loading="loading"
      stripe
      style="width: 100%"
      @sort-change="onSortChange"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />

      <el-table-column prop="deviceName" label="设备名称" min-width="160" show-overflow-tooltip />

      <el-table-column prop="location" label="位置" min-width="180" show-overflow-tooltip />

      <el-table-column label="在线状态" width="110" align="center" sortable="custom" prop="onlineStatus">
        <template #default="{ row }">
          <el-tag :type="row.onlineStatus === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.onlineStatus === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="最后在线时间" width="180" align="center" sortable="custom" prop="lastOnlineTime">
        <template #default="{ row }">
          {{ formatDate(row.lastOnlineTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewVideo(row)">
            查看视频
          </el-button>
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

    <!-- 视频播放弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentDevice?.deviceName ?? '视频播放'"
      width="720px"
      destroy-on-close
      top="5vh"
    >
      <div class="video-player">
        <div class="video-placeholder">
          <el-icon :size="48" color="#909399"><VideoCamera /></el-icon>
          <p class="placeholder-text">视频流加载中...</p>
          <p class="placeholder-hint">视频流 URL：{{ currentDevice?.videoUrl ?? '-' }}</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleReconnect" :loading="reconnecting">
          重新连接
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, VideoCamera } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

defineOptions({ name: 'VideoMonitorList' })

// ─── 类型定义 ───

interface DeviceItem {
  id: number
  deviceName: string
  location: string
  onlineStatus: number // 1 在线 | 0 离线
  lastOnlineTime: string
  videoUrl: string
}

// ─── 模拟数据 ───

const MOCK_DEVICES: DeviceItem[] = [
  { id: 1, deviceName: '东区-1号摄像头', location: '东区A排3列', onlineStatus: 1, lastOnlineTime: '2025-06-21 14:32:10', videoUrl: 'rtsp://192.168.1.101:554/stream1' },
  { id: 2, deviceName: '东区-2号摄像头', location: '东区B排5列', onlineStatus: 1, lastOnlineTime: '2025-06-21 14:30:45', videoUrl: 'rtsp://192.168.1.102:554/stream1' },
  { id: 3, deviceName: '西区-1号摄像头', location: '西区C排2列', onlineStatus: 0, lastOnlineTime: '2025-06-20 09:15:30', videoUrl: 'rtsp://192.168.1.103:554/stream1' },
  { id: 4, deviceName: '西区-2号摄像头', location: '西区D排7列', onlineStatus: 1, lastOnlineTime: '2025-06-21 12:08:22', videoUrl: 'rtsp://192.168.1.104:554/stream1' },
  { id: 5, deviceName: '南区-全景摄像头', location: '南区入口', onlineStatus: 1, lastOnlineTime: '2025-06-21 14:28:55', videoUrl: 'rtsp://192.168.1.105:554/stream1' },
  { id: 6, deviceName: '北区-1号摄像头', location: '北区E排1列', onlineStatus: 0, lastOnlineTime: '2025-06-18 16:42:00', videoUrl: 'rtsp://192.168.1.106:554/stream1' },
  { id: 7, deviceName: '北区-2号摄像头', location: '北区F排4列', onlineStatus: 1, lastOnlineTime: '2025-06-21 13:55:12', videoUrl: 'rtsp://192.168.1.107:554/stream1' },
  { id: 8, deviceName: '仓库-监控摄像头', location: '仓储区东侧', onlineStatus: 1, lastOnlineTime: '2025-06-21 14:31:05', videoUrl: 'rtsp://192.168.1.108:554/stream1' },
]

// ─── 响应式状态 ───

const loading = ref(false)
const dataList = ref<DeviceItem[]>([])
const fullList = ref<DeviceItem[]>([...MOCK_DEVICES])

const searchForm = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

/** 视频弹窗 */
const dialogVisible = ref(false)
const currentDevice = ref<DeviceItem | null>(null)
const reconnecting = ref(false)

// ─── 方法 ───

/** 格式化日期 */
function formatDate(date: string | undefined | null): string {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/** 获取列表数据（模拟） */
async function fetchList() {
  loading.value = true
  try {
    // 模拟接口延迟
    await new Promise(resolve => setTimeout(resolve, 300))

    let filtered = [...fullList.value]
    if (searchForm.keyword) {
      const kw = searchForm.keyword.toLowerCase()
      filtered = filtered.filter(item => item.deviceName.toLowerCase().includes(kw))
    }

    pagination.total = filtered.length
    const start = (pagination.page - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    dataList.value = filtered.slice(start, end)
  } catch {
    dataList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/** 搜索 */
function onSearch() {
  pagination.page = 1
  fetchList()
}

/** 重置搜索 */
function onReset() {
  searchForm.keyword = ''
  pagination.page = 1
  fetchList()
}

/** 排序变化 */
function onSortChange(sort: { prop?: string; order?: string }) {
  if (!sort.prop || !sort.order) {
    // 还原默认排序（按 id 升序）
    fullList.value.sort((a, b) => a.id - b.id)
    fetchList()
    return
  }

  const order = sort.order === 'ascending' ? 1 : -1
  fullList.value.sort((a, b) => {
    const aVal = a[sort.prop as keyof DeviceItem]
    const bVal = b[sort.prop as keyof DeviceItem]
    if (typeof aVal === 'number' && typeof bVal === 'number') {
      return (aVal - bVal) * order
    }
    if (typeof aVal === 'string' && typeof bVal === 'string') {
      return aVal.localeCompare(bVal) * order
    }
    return 0
  })
  fetchList()
}

/** 分页 size 变化 */
function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

/** 分页 page 变化 */
function handleCurrentChange(page: number) {
  pagination.page = page
  fetchList()
}

/** 查看视频 */
function handleViewVideo(row: DeviceItem) {
  currentDevice.value = { ...row }
  dialogVisible.value = true
}

/** 重新连接（模拟） */
async function handleReconnect() {
  reconnecting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    ElMessage.success('重新连接成功')
  } catch {
    ElMessage.error('重新连接失败')
  } finally {
    reconnecting.value = false
  }
}

onMounted(fetchList)
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

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

// 视频播放器样式
.video-player {
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;

  .video-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 360px;
    padding: 40px;
    gap: 12px;

    .placeholder-text {
      margin: 0;
      font-size: 16px;
      color: #c0c4cc;
    }

    .placeholder-hint {
      margin: 0;
      font-size: 12px;
      color: #606266;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
