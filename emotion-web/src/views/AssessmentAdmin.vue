<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElPagination } from 'element-plus';
import { documentAPI, assessmentAdminAPI } from '@/api';

const questionnaires = ref([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);

const file = ref(null);
const parsed = ref(null);
const uploading = ref(false);
const confirming = ref(false);
const fileInput = ref(null);


const showDetailDialog = ref(false);
const detailData = ref(null);
const detailLoading = ref(false);
const detailTab = ref('questions');

const statusMap = {
  0: '禁用',
  1: '启用'
};

const loadList = async () => {
  loading.value = true;
  try {
    const res = await assessmentAdminAPI.getList({ page: page.value, size: pageSize.value });
    const data = res.data.data;
    questionnaires.value = data.questionnaires;
    total.value = data.total;
  } catch (e) {
    ElMessage.error(e.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const handleToggle = async (q) => {
  const action = q.isActive === 1 ? '禁用' : '启用';
  try {
    await ElMessageBox.confirm(`确认${action}问卷「${q.name}」？${action === '禁用' ? '禁用后用户端将不可见' : ''}`, '提示');
  } catch {
    return;
  }
  try {
    const res = await assessmentAdminAPI.toggleActive(q.id);
    ElMessage.success(res.data.message || '操作成功');
    loadList();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
};

const handleShowDetail = async (q) => {
  showDetailDialog.value = true;
  detailLoading.value = true;
  detailTab.value = 'questions';
  try {
    const res = await assessmentAdminAPI.getDetail(q.id);
    detailData.value = res.data.data;
  } catch (e) {
    ElMessage.error(e.message || '加载详情失败');
    showDetailDialog.value = false;
  } finally {
    detailLoading.value = false;
  }
};

const handlePageChange = (p) => {
  page.value = p;
  loadList();
};

// Upload & Parse
const handleFileChange = (e) => {
  const f = e.target.files?.[0];
  if (f) {
    if (!f.name.toLowerCase().endsWith('.docx')) {
      ElMessage.error('仅支持 .docx 格式');
      return;
    }
    if (f.size > 10 * 1024 * 1024) {
      ElMessage.error('文件超过 10MB 限制');
      return;
    }
    file.value = f;
    parsed.value = null;
    doParse(f);
  }
};

const doParse = async (f) => {
  uploading.value = true;
  try {
    const res = await documentAPI.parse(f);
    parsed.value = res.data.data;
    ElMessage.success('解析成功');
  } catch (e) {
    ElMessage.error(e.message || '解析失败');
    file.value = null;
  } finally {
    uploading.value = false;
  }
};

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认将以上数据保存到数据库？', '确认入库', {
      confirmButtonText: '确认保存',
      cancelButtonText: '取消',
      type: 'info'
    });
  } catch {
    return;
  }
  confirming.value = true;
  try {
    await documentAPI.confirm(parsed.value);
    ElMessage.success('问卷保存成功');
    file.value = null;
    parsed.value = null;
    loadList();
  } catch (e) {
    ElMessage.error(e.message || '保存失败');
  } finally {
    confirming.value = false;
  }
};

const triggerUpload = () => {
  if (!uploading.value && !file.value) {
    fileInput.value?.click();
  }
};

const resetUpload = () => {
  file.value = null;
  parsed.value = null;
  if (fileInput.value) fileInput.value.value = '';
};

onMounted(loadList);
</script>

<template>
  <div class="admin-page">
    <main class="admin-content">
      <div class="main-panel">
        <div class="tab-content">
          <div class="panel-header">
            <h2>问卷管理</h2>
            <div class="header-actions">
              <button @click="triggerUpload" class="search-btn">
                {{ uploading || file ? '上传中...' : '上传DOCX解析' }}
              </button>
            </div>
          </div>

          <input ref="fileInput" type="file" accept=".docx" hidden @change="handleFileChange" />

          <!-- Upload Section -->
          <div v-if="file || uploading || parsed" class="upload-section">
            <div class="file-info-row">
              <span class="file-name">{{ file?.name }}</span>
              <span class="file-size" v-if="file">({{ (file.size / 1024).toFixed(1) }} KB)</span>
              <button v-if="!uploading" @click="resetUpload" class="btn-remove">移除</button>
            </div>

            <div v-if="uploading" class="loading-row"><div class="spinner"></div><span>正在解析...</span></div>

            <div v-if="parsed && !uploading" class="preview-box">
              <div class="preview-meta">
                <span><b>名称：</b>{{ parsed.name }}</span>
                <span><b>编码：</b><code>{{ parsed.code }}</code></span>
                <span v-if="parsed.description"><b>描述：</b>{{ parsed.description }}</span>
              </div>
              <p class="preview-stat">题目 {{ parsed.questions?.length || 0 }} 题 · 评分规则 {{ parsed.gradeRules?.length || 0 }} 条</p>
              <div class="preview-actions">
                <button @click="resetUpload" class="action-btn">重新选择</button>
                <button @click="handleConfirm" :disabled="confirming" class="action-btn detail-btn" style="border:none;">
                  {{ confirming ? '保存中...' : '确认入库' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Table -->
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>名称</th>
                  <th>编码</th>
                  <th>描述</th>
                  <th>题数</th>
                  <th>状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td colspan="8" style="padding: 40px; color: #9ca3af;">加载中...</td>
                </tr>
                <tr v-for="q in questionnaires" :key="q.id">
                  <td>{{ q.id }}</td>
                  <td style="font-weight:600; color:#1f2937;">{{ q.name }}</td>
                  <td><code style="background:#e0e7ff; color:#6366f1; padding:2px 6px; border-radius:4px; font-size:12px;">{{ q.code }}</code></td>
                  <td style="max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">{{ q.description || '-' }}</td>
                  <td>{{ q.totalQuestions }}</td>
                  <td>
                    <span class="status-tag" :class="q.isActive === 1 ? 'active' : ''">
                      {{ statusMap[q.isActive] || '未知' }}
                    </span>
                  </td>
                  <td style="font-size:13px; color:#6b7280;">{{ q.createdAt }}</td>
                  <td>
                    <div class="action-group">
                      <button class="action-btn detail-btn" @click="handleShowDetail(q)">详情</button>
                      <button
                        class="action-btn"
                        :class="q.isActive === 1 ? 'danger' : ''"
                        @click="handleToggle(q)"
                      >
                        {{ q.isActive === 1 ? '禁用' : '启用' }}
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="!loading && questionnaires.length === 0">
                  <td colspan="8" style="padding: 40px; color: #9ca3af;">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pagination-container">
            <ElPagination
              background
              layout="total, prev, pager, next, jumper"
              :total="total"
              :page-size="pageSize"
              :current-page="page"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </main>

    <!-- Detail Modal -->
    <div v-if="showDetailDialog" class="modal-overlay" @click.self="showDetailDialog = false">
      <div class="modal-panel">
        <div class="modal-header">
          <div class="modal-title">
            <span class="modal-title-icon">📋</span>
            <h3>{{ detailData?.name || '问卷详情' }}</h3>
          </div>
          <button class="modal-close" @click="showDetailDialog = false">✕</button>
        </div>

        <div class="modal-body" v-loading="detailLoading">
          <div v-if="detailData">
            <!-- Info Cards -->
            <div class="info-grid">
              <div class="info-card">
                <span class="info-label">编码</span>
                <span class="info-value"><code>{{ detailData.code }}</code></span>
              </div>
              <div class="info-card">
                <span class="info-label">状态</span>
                <span class="status-tag" :class="detailData.isActive === 1 ? 'active' : ''">
                  {{ statusMap[detailData.isActive] || '未知' }}
                </span>
              </div>
              <div class="info-card">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ detailData.createdAt }}</span>
              </div>
              <div class="info-card">
                <span class="info-label">更新时间</span>
                <span class="info-value">{{ detailData.updatedAt }}</span>
              </div>
            </div>
            <div v-if="detailData.description" class="desc-box">
              <span class="desc-label">描述</span>
              <p class="desc-text">{{ detailData.description }}</p>
            </div>

            <!-- Tabs -->
            <div class="modal-tabs">
              <button :class="{ active: detailTab === 'questions' }" @click="detailTab = 'questions'">
                📝 题目（{{ detailData.questions?.length || 0 }}）
              </button>
              <button :class="{ active: detailTab === 'rules' }" @click="detailTab = 'rules'">
                📊 评分规则（{{ detailData.gradeRules?.length || 0 }}）
              </button>
            </div>

            <!-- Questions Tab -->
            <div v-if="detailTab === 'questions' && detailData?.questions" class="tab-scroll">
              <div v-for="q in detailData.questions" :key="q.id" class="question-card" :class="{ inactive: q.isActive !== 1 }">
                <div class="q-header">
                  <span class="q-badge">{{ q.questionIndex }}</span>
                  <span class="q-text">{{ q.questionText }}</span>
                  <span v-if="q.isActive !== 1" class="q-disabled">已禁用</span>
                </div>
                <div class="q-options">
                  <span class="q-opt">A. {{ q.optionA }} <em>{{ q.scoreA }}分</em></span>
                  <span class="q-opt">B. {{ q.optionB }} <em>{{ q.scoreB }}分</em></span>
                  <span class="q-opt">C. {{ q.optionC }} <em>{{ q.scoreC }}分</em></span>
                  <span class="q-opt">D. {{ q.optionD }} <em>{{ q.scoreD }}分</em></span>
                </div>
              </div>
            </div>

            <!-- Grade Rules Tab -->
            <div v-if="detailTab === 'rules' && detailData?.gradeRules" class="tab-scroll">
              <div class="rules-grid">
                <div
                  v-for="r in detailData.gradeRules"
                  :key="r.id"
                  class="rule-card"
                  :class="{ inactive: r.isActive !== 1 }"
                >
                  <div class="rule-score">{{ r.minScore }} ~ {{ r.maxScore }} 分</div>
                  <div class="rule-level">{{ r.levelName }}</div>
                  <div class="rule-code"><code>{{ r.levelCode }}</code></div>
                  <p class="rule-advice">{{ r.adviceText }}</p>
                  <div class="rule-status">
                    <span class="status-tag" :class="r.isActive === 1 ? 'active' : ''">
                      {{ r.isActive === 1 ? '启用' : '禁用' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  padding: 24px 24px 24px var(--page-offset);
}

.admin-content {
  height: calc(100vh - 48px);
}

.main-panel {
  height: 100%;
  overflow-y: auto;
}

.tab-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-btn {
  padding: 10px 20px;
  background: #6366f1;
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.search-btn:hover {
  background: #4f46e5;
}

/* Upload */
.upload-section {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.file-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.file-name { font-size: 14px; font-weight: 600; color: #1f2937; }
.file-size { font-size: 12px; color: #9ca3af; }

.btn-remove {
  padding: 6px 16px;
  background: #fee2e2;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  color: #dc2626;
  cursor: pointer;
  margin-left: auto;
}
.btn-remove:hover { background: #fecaca; }

.loading-row {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  padding: 16px;
  color: #6366f1;
  font-size: 14px;
}
.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid #e0e7ff;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.preview-box { padding: 16px; background: white; border-radius: 10px; border: 1px solid #e5e7eb; }
.preview-meta { display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 8px; font-size: 13px; color: #374151; }
.preview-meta code { background: #e0e7ff; color: #6366f1; padding: 1px 6px; border-radius: 3px; font-size: 12px; }
.preview-stat { font-size: 12px; color: #9ca3af; margin: 0 0 12px; }
.preview-actions { display: flex; gap: 10px; }

/* Table */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 14px;
  text-align: center;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
  color: #374151;
}

.data-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #fee2e2;
  color: #dc2626;
  width: fit-content;
}

.info-card .status-tag {
  align-self: flex-start;
}

.status-tag.active {
  background: #dcfce7;
  color: #166534;
}

.action-group {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  background: #f3f4f6;
  color: #374151;
}

.action-btn:hover {
  opacity: .8;
}

.action-btn.danger {
  background: #fee2e2;
  color: #dc2626;
}

.detail-btn {
  background: #e0e7ff !important;
  color: #4f46e5 !important;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 6vh;
  z-index: 1000;
}

.modal-panel {
  width: 680px;
  max-height: 88vh;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-title-icon {
  font-size: 22px;
}

.modal-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  background: #e5e7eb;
  color: #1f2937;
}

.modal-body {
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.info-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 10px;
}

.info-label {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: #1f2937;
}

.info-value code {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.desc-box {
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 10px;
  margin-bottom: 16px;
}

.desc-label {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: block;
  margin-bottom: 6px;
}

.desc-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  margin: 0;
}

.modal-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.modal-tabs button {
  padding: 10px 20px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  flex: 1;
}

.modal-tabs button:hover {
  border-color: #c7d2fe;
  color: #4f46e5;
}

.modal-tabs button.active {
  color: #4f46e5;
  background: #eef2ff;
  border-color: #c7d2fe;
}

.tab-scroll {
  max-height: 400px;
  overflow-y: auto;
}

/* Questions */
.question-card {
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 10px;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.question-card:hover { border-color: #e5e7eb; }
.question-card.inactive { opacity: .5; }

.q-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.q-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.q-text {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  line-height: 1.4;
}

.q-disabled {
  font-size: 11px;
  background: #f3f4f6;
  color: #9ca3af;
  padding: 2px 8px;
  border-radius: 4px;
}

.q-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  padding-left: 40px;
}

.q-opt {
  font-size: 12px;
  color: #374151;
  background: white;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  line-height: 1.4;
}
.q-opt em {
  font-style: normal;
  color: #4f46e5;
  font-weight: 600;
  margin-left: 4px;
}

/* Grade Rules */
.rules-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.rule-card {
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid transparent;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.rule-card:hover { border-color: #e5e7eb; }
.rule-card.inactive { opacity: .5; }

.rule-score {
  font-size: 16px;
  font-weight: 700;
  color: #4f46e5;
}

.rule-level {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.rule-code code {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.rule-advice {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  margin: 4px 0 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rule-status {
  margin-top: auto;
  padding-top: 6px;
}
</style>