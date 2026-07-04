<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-header">
      <h2>农事日历管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">
        新增农事事件
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索事件标题"
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
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />

      <el-table-column prop="eventTitle" label="事件标题" min-width="160" show-overflow-tooltip />

      <el-table-column prop="phenology" label="物候期" width="120" align="center" />

      <el-table-column prop="solarTerm" label="节气" width="100" align="center" />

      <el-table-column label="开始时间" width="160" align="center">
        <template #default="{ row }">
          {{ row.startTime ? dayjs(row.startTime).format('YYYY-MM-DD HH:mm') : '-' }}
        </template>
      </el-table-column>

      <el-table-column label="结束时间" width="160" align="center">
        <template #default="{ row }">
          {{ row.endTime ? dayjs(row.endTime).format('YYYY-MM-DD HH:mm') : '-' }}
        </template>
      </el-table-column>

      <el-table-column prop="farmingAdvice" label="农事建议" min-width="180" show-overflow-tooltip />

      <el-table-column prop="relatedDisease" label="关联病害" width="140" show-overflow-tooltip />

      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="openDialog('编辑', row)">
            编辑
          </el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">
            删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="620px"
      destroy-on-close
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="事件标题" prop="eventTitle">
          <el-input
            v-model="formData.eventTitle"
            placeholder="请输入事件标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="物候期" prop="phenology">
              <el-input
                v-model="formData.phenology"
                placeholder="如：开花期"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节气" prop="solarTerm">
              <el-input
                v-model="formData.solarTerm"
                placeholder="如：惊蛰"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="农事建议" prop="farmingAdvice">
          <el-input
            v-model="formData.farmingAdvice"
            type="textarea"
            :rows="3"
            placeholder="请输入农事建议"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="关联病害" prop="relatedDisease">
          <el-input
            v-model="formData.relatedDisease"
            placeholder="请输入关联病害名称，多个用逗号分隔"
            maxlength="200"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit(formRef)">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

import {
  getCalendarList,
  createCalendarEvent,
  updateCalendarEvent,
  deleteCalendarEvent,
  type CalendarEvent,
  type CalendarEventForm,
} from '@/api/modules/calendar'

defineOptions({ name: 'CalendarViewList' })

// ===== 响应式状态 =====

const loading = ref(false)
const submitting = ref(false)
const dataList = ref<CalendarEvent[]>([])
const selectedIds = ref<number[]>([])

const searchForm = reactive({
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增农事事件')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const formData = reactive<CalendarEventForm>({
  eventTitle: '',
  phenology: '',
  solarTerm: '',
  startTime: '',
  endTime: '',
  farmingAdvice: '',
  relatedDisease: '',
})

// ===== 表单校验规则 =====

const formRules: FormRules = {
  eventTitle: [{ required: true, message: '请输入事件标题', trigger: 'blur' }],
}

// ===== 数据加载 =====

/** 获取列表数据 */
async function fetchList() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (searchForm.keyword) {
      Object.assign(params, { keyword: searchForm.keyword })
    }

    const res = await getCalendarList(params)
    dataList.value = res.list ?? []
    pagination.total = res.total ?? 0
  } catch {
    dataList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// ===== 搜索 / 重置 =====

function onSearch() {
  pagination.page = 1
  fetchList()
}

function onReset() {
  searchForm.keyword = ''
  pagination.page = 1
  fetchList()
}

// ===== 分页 =====

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  fetchList()
}

// ===== 多选 =====

function handleSelectionChange(rows: CalendarEvent[]) {
  selectedIds.value = rows.map(r => r.id)
}

// ===== 新增 / 编辑对话框 =====

function openDialog(action: '新增' | '编辑' = '新增', row?: CalendarEvent) {
  if (action === '编辑' && row) {
    dialogTitle.value = '编辑农事事件'
    editingId.value = row.id
    formData.eventTitle = row.eventTitle
    formData.phenology = row.phenology ?? ''
    formData.solarTerm = row.solarTerm ?? ''
    formData.startTime = row.startTime ?? ''
    formData.endTime = row.endTime ?? ''
    formData.farmingAdvice = row.farmingAdvice ?? ''
    formData.relatedDisease = row.relatedDisease ?? ''
  } else {
    dialogTitle.value = '新增农事事件'
    editingId.value = null
    resetFormData()
  }
  dialogVisible.value = true
}

function resetFormData() {
  formData.eventTitle = ''
  formData.phenology = ''
  formData.solarTerm = ''
  formData.startTime = ''
  formData.endTime = ''
  formData.farmingAdvice = ''
  formData.relatedDisease = ''
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

/** 提交表单 */
async function handleSubmit(formEl: FormInstance | undefined) {
  if (!formEl) return
  try {
    await formEl.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (editingId.value !== null) {
      await updateCalendarEvent(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createCalendarEvent(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // 错误由全局拦截器处理
  } finally {
    submitting.value = false
  }
}

// ===== 删除 =====

async function handleDelete(row: CalendarEvent) {
  try {
    await ElMessageBox.confirm(
      `确定删除农事事件「${row.eventTitle}」吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )
    await deleteCalendarEvent(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 取消或错误均静默处理
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

.dialog-form {
  .el-row {
    margin-bottom: 0;
  }
}
</style>
