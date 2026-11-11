<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElPagination } from 'element-plus'
import { alertAPI } from '@/api'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const loadAlerts = async () => {
  loading.value = true
  try {
    const res = await alertAPI.getMessages({ page: page.value, size: size.value })
    list.value = res.data.data?.records || []
    total.value = res.data.data?.total || 0
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取预警消息失败')
  } finally {
    loading.value = false
  }
}

const markRead = async (msg) => {
  try {
    await alertAPI.markRead(msg.id)
    ElMessage.success('已标记为已读')
    loadAlerts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handlePageChange = (val) => {
  page.value = val
  loadAlerts()
}

onMounted(loadAlerts)
</script>

<template>
  <div class="alert-page">
    <main class="main-content">
      <div class="card alert-card">
        <div class="card-header">
          <h2 class="card-title">全部预警</h2>
          <div class="header-right">
            <span class="card-subtitle">共 {{ total }} 条</span>
            <button @click="loadAlerts" class="refresh-btn">刷新</button>
          </div>
        </div>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>规则名称</th>
                <th>城市</th>
                <th>日期区间</th>
                <th>预警内容</th>
                <th>生效状态</th>
                <th>已读状态</th>
                <th>触发时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="msg in list" :key="msg.id">
                <td class="rule-name">{{ msg.ruleName || '-' }}</td>
                <td>{{ msg.cityName || msg.location || '-' }}</td>
                <td class="date-range">{{ msg.fxDateStart }} ~ {{ msg.fxDateEnd }}</td>
                <td class="content-cell">{{ msg.alertContent || '-' }}</td>
                <td>
                  <span class="status-tag" :class="{ active: msg.active === 1 }">
                    {{ msg.active === 1 ? '生效' : '已解除' }}
                  </span>
                </td>
                <td>
                  <span class="status-tag" :class="{ read: msg.status === 1 }">
                    {{ msg.status === 1 ? '已读' : '未读' }}
                  </span>
                </td>
                <td class="time-cell">{{ msg.createdAt || '-' }}</td>
                <td>
                  <button
                    v-if="msg.status !== 1"
                    @click="markRead(msg)"
                    class="action-btn"
                  >标记已读</button>
                  <span v-else class="muted">-</span>
                </td>
              </tr>
              <tr v-if="!loading && !list.length">
                <td colspan="8" class="empty-row">
                  <span class="empty-icon">🔔</span> 暂无预警消息
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination-container" v-if="total > size">
          <ElPagination
            :current-page="page"
            :page-size="size"
            :total="total"
            @current-change="handlePageChange"
            layout="total, prev, pager, next, jumper"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.alert-page {
  min-height: 100vh;
  background: #f1f5f9;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px 24px 24px var(--page-offset);
}

.alert-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 28px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 18px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e1b4b;
}

.card-subtitle {
  font-size: 13px;
  color: #94a3b8;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-btn {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.refresh-btn:hover {
  background: #eef2ff;
  border-color: #c7d2fe;
  color: #6366f1;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.data-table th {
  background: #f8fafc;
  color: #64748b;
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
}

.data-table tbody tr:hover {
  background: #fafafa;
}

.rule-name {
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
}

.date-range {
  white-space: nowrap;
}

.content-cell {
  max-width: 320px;
  color: #475569;
  line-height: 1.6;
}

.time-cell {
  white-space: nowrap;
  color: #64748b;
  font-size: 13px;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  background: #f1f5f9;
  color: #64748b;
}

.status-tag.active {
  background: #dcfce7;
  color: #15803d;
}

.status-tag.read {
  background: #e0e7ff;
  color: #4338ca;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 8px;
  background: #eef2ff;
  color: #6366f1;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.action-btn:hover {
  background: #e0e7ff;
}

.muted {
  color: #cbd5e1;
}

.empty-row {
  text-align: center;
  padding: 48px 16px;
  color: #94a3b8;
  font-size: 14px;
}

.empty-icon {
  font-size: 28px;
  margin-right: 8px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
