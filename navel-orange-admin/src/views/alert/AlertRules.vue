<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预警规则管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增预警规则</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索规则名称"
        clearable
        :prefix-icon="Search"
        style="width: 300px"
        @keyup.enter="onSearch"
      />
      <el-select
        v-model="searchForm.diseaseCode"
        placeholder="病害类型"
        clearable
        style="width: 200px"
        @change="onSearch"
      >
        <el-option
          v-for="opt in DISEASE_CODE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-select
        v-model="searchForm.level"
        placeholder="风险等级"
        clearable
        style="width: 140px"
        @change="onSearch"
      >
        <el-option
          v-for="(m, k) in ALERT_LEVEL_MAP"
          :key="k"
          :label="m.label"
          :value="Number(k)"
        />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="dataList" v-loading="loading" stripe style="width: 100%">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="name" label="规则名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="病害类型" width="180">
        <template #default="{ row }">
          <template v-if="row.diseaseCodes?.length">
            <el-tag
              v-for="code in row.diseaseCodes"
              :key="code"
              :color="DISEASE_MAP[code]?.color || '#909399'"
              effect="dark"
              size="small"
              style="border: none; margin-right: 4px"
            >
              {{ DISEASE_MAP[code]?.name || code }}
            </el-tag>
          </template>
          <span v-else-if="row.diseaseCode">
            <el-tag
              :color="DISEASE_MAP[row.diseaseCode]?.color || '#909399'"
              effect="dark"
              size="small"
              style="border: none"
            >
              {{ DISEASE_MAP[row.diseaseCode]?.name || row.diseaseCode }}
            </el-tag>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="风险等级" width="120" align="center">
        <template #default="{ row }">
          <el-tag
            v-if="ALERT_LEVEL_MAP[row.level]"
            :color="ALERT_LEVEL_MAP[row.level].color"
            effect="dark"
            size="small"
            style="border: none"
          >
            {{ ALERT_LEVEL_MAP[row.level].label }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="触发条件" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ buildConditionSummary(row) }}
        </template>
      </el-table-column>
      <el-table-column label="通知方式" width="160">
        <template #default="{ row }">
          <template v-if="row.notifyMethods?.length">
            <el-tag
              v-for="m in row.notifyMethods"
              :key="m"
              :type="notifyMethodType(m)"
              effect="plain"
              size="small"
              style="margin-right: 4px"
            >
              {{ notifyMethodLabel(m) }}
            </el-tag>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            :loading="row._toggling"
            @change="handleToggle(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog('编辑', row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="pagination.pageSizes"
        :layout="pagination.layout"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入规则名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="病害类型" prop="diseaseCodes">
          <el-select
            v-model="formData.diseaseCodes"
            multiple
            placeholder="可多选"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="opt in DISEASE_CODE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最低温度(℃)" prop="tempMin">
              <el-input-number v-model="formData.tempMin" :controls="false" :precision="1" :min="-10" :max="50" placeholder="自动" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高温度(℃)" prop="tempMax">
              <el-input-number v-model="formData.tempMax" :controls="false" :precision="1" :min="-10" :max="50" placeholder="自动" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最低湿度(%)" prop="humidityMin">
              <el-input-number v-model="formData.humidityMin" :controls="false" :precision="1" :min="0" :max="100" placeholder="自动" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高湿度(%)" prop="humidityMax">
              <el-input-number v-model="formData.humidityMax" :controls="false" :precision="1" :min="0" :max="100" placeholder="自动" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="风险等级" prop="level">
              <el-select v-model="formData.level" placeholder="选择风险等级" style="width: 100%">
                <el-option
                  v-for="(m, k) in ALERT_LEVEL_MAP"
                  :key="k"
                  :label="m.label"
                  :value="Number(k)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="冷却时间(h)" prop="cooldownHours">
              <el-input-number v-model="formData.cooldownHours" :min="1" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检测次数" prop="detectionCount">
              <el-input-number v-model="formData.detectionCount" :min="1" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测天数" prop="detectionDays">
              <el-input-number v-model="formData.detectionDays" :min="1" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="通知方式" prop="notifyMethods">
          <el-select
            v-model="formData.notifyMethods"
            multiple
            placeholder="可多选"
            clearable
            style="width: 100%"
          >
            <el-option label="微信" value="wechat" />
            <el-option label="短信" value="sms" />
            <el-option label="邮件" value="email" />
            <el-option label="应用内" value="app" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  toggleAlertRule,
} from '@/api/modules/alert'
import { ALERT_LEVEL_MAP, DISEASE_CODE_OPTIONS, DISEASE_MAP } from '@/constants/disease'

defineOptions({ name: 'AlertRulesList' })

// ─── 类型 ───
interface RuleItem {
  id: number
  ruleCode: string
  name: string
  level: number
  enabled: boolean
  diseaseCode?: string
  diseaseCodes?: string[]
  tempMin?: number
  tempMax?: number
  humidityMin?: number
  humidityMax?: number
  detectionCount?: number
  detectionDays?: number
  cooldownHours?: number
  notifyMethods?: string[]
  _toggling?: boolean
}

interface FormData {
  name: string
  diseaseCodes: string[]
  level: number | undefined
  tempMin: number | undefined
  tempMax: number | undefined
  humidityMin: number | undefined
  humidityMax: number | undefined
  detectionCount: number | undefined
  detectionDays: number | undefined
  cooldownHours: number | undefined
  notifyMethods: string[]
  enabled: boolean
}

// ─── 通知方式映射 ───
const NOTIFY_METHOD_MAP: Record<string, { label: string; type: string }> = {
  wechat: { label: '微信', type: 'success' },
  sms: { label: '短信', type: 'primary' },
  email: { label: '邮件', type: 'warning' },
  app: { label: '应用内', type: 'info' },
}

function notifyMethodType(method: string): string {
  return NOTIFY_METHOD_MAP[method]?.type ?? 'info'
}

function notifyMethodLabel(method: string): string {
  return NOTIFY_METHOD_MAP[method]?.label ?? method
}

// ─── 触发条件摘要 ───
function buildConditionSummary(row: RuleItem): string {
  const parts: string[] = []
  if (row.tempMin != null || row.tempMax != null) {
    parts.push(`温度 ${row.tempMin ?? '-'}~${row.tempMax ?? '-'}℃`)
  }
  if (row.humidityMin != null || row.humidityMax != null) {
    parts.push(`湿度 ${row.humidityMin ?? '-'}~${row.humidityMax ?? '-'}%`)
  }
  const codes = row.diseaseCodes?.length ? row.diseaseCodes : (row.diseaseCode ? row.diseaseCode.split(',').filter(Boolean) : [])
  if (codes.length) {
    const names = codes.map(c => DISEASE_MAP[c]?.name ?? c)
    parts.push(`病害 ${names.join('、')}`)
  }
  if (row.detectionCount) parts.push(`${row.detectionCount}次/${row.detectionDays ?? 1}天`)
  if (row.cooldownHours) parts.push(`冷却${row.cooldownHours}h`)
  return parts.join('；') || '-'
}

// ─── 响应式状态 ───
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增预警规则')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const dataList = ref<RuleItem[]>([])

const searchForm = reactive({
  keyword: '',
  diseaseCode: '',
  level: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  pageSizes: [10, 20, 50],
  layout: 'total, sizes, prev, pager, next, jumper',
})

const formData = reactive<FormData>({
  name: '',
  diseaseCodes: [],
  level: undefined,
  tempMin: undefined,
  tempMax: undefined,
  humidityMin: undefined,
  humidityMax: undefined,
  detectionCount: undefined,
  detectionDays: undefined,
  cooldownHours: undefined,
  notifyMethods: [],
  enabled: true,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  tempMin: [
    {
      validator: (_rule, value, callback) => {
        if (value != null && formData.tempMax != null && value > formData.tempMax) {
          callback(new Error('最低温度不能大于最高温度'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
  tempMax: [
    {
      validator: (_rule, value, callback) => {
        if (value != null && formData.tempMin != null && value < formData.tempMin) {
          callback(new Error('最高温度不能小于最低温度'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
}

// ─── 重置表单 ───
function resetForm() {
  formData.name = ''
  formData.diseaseCodes = []
  formData.level = undefined
  formData.tempMin = undefined
  formData.tempMax = undefined
  formData.humidityMin = undefined
  formData.humidityMax = undefined
  formData.detectionCount = undefined
  formData.detectionDays = undefined
  formData.cooldownHours = undefined
  formData.notifyMethods = []
  formData.enabled = true
  editingId.value = null
  formRef.value?.clearValidate()
}

// ─── 搜索 & 分页 ───
function onSearch() {
  pagination.page = 1
  fetchList()
}

function handleSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchList()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  fetchList()
}

// ─── 数据获取 ───
async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.pageSize,
    }
    if (searchForm.keyword.trim()) params.keyword = searchForm.keyword.trim()
    if (searchForm.diseaseCode) params.diseaseCode = searchForm.diseaseCode
    if (searchForm.level != null) params.level = searchForm.level

    const data: any = await getAlertRules(params)
    const records = Array.isArray(data) ? data : data.records ?? []
    // 将 diseaseCode 字符串转换为数组，便于展示
    dataList.value = records.map((item: any) => ({
      ...item,
      diseaseCodes: item.diseaseCodes ?? (item.diseaseCode ? item.diseaseCode.split(',').filter(Boolean) : []),
      _toggling: false,
    }))
    pagination.total = data.total ?? records.length
  } catch {
    ElMessage.error('获取预警规则列表失败')
  } finally {
    loading.value = false
  }
}

// ─── 新增 / 编辑对话框 ───
function openDialog(title: string = '新增', row?: RuleItem) {
  resetForm()
  if (title === '编辑' && row) {
    dialogTitle.value = '编辑预警规则'
    editingId.value = row.id
    formData.name = row.name
    formData.level = row.level
    formData.tempMin = row.tempMin
    formData.tempMax = row.tempMax
    formData.humidityMin = row.humidityMin
    formData.humidityMax = row.humidityMax
    formData.detectionCount = row.detectionCount
    formData.detectionDays = row.detectionDays
    formData.cooldownHours = row.cooldownHours
    formData.diseaseCodes = row.diseaseCodes?.length
      ? [...row.diseaseCodes]
      : (row.diseaseCode ? row.diseaseCode.split(',').filter(Boolean) : [])
    formData.notifyMethods = row.notifyMethods ? [...row.notifyMethods] : []
    formData.enabled = row.enabled
  } else {
    dialogTitle.value = '新增预警规则'
  }
  dialogVisible.value = true
}

// ─── 提交 ───
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload: any = {
      name: formData.name,
      diseaseCode: formData.diseaseCodes.join(','),
      level: formData.level,
      tempMin: formData.tempMin,
      tempMax: formData.tempMax,
      humidityMin: formData.humidityMin,
      humidityMax: formData.humidityMax,
      detectionCount: formData.detectionCount,
      detectionDays: formData.detectionDays,
      cooldownHours: formData.cooldownHours,
      notifyMethods: formData.notifyMethods,
      enabled: formData.enabled,
    }

    if (editingId.value) {
      await updateAlertRule(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createAlertRule(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch {
    // 后端已处理错误消息
  } finally {
    submitting.value = false
  }
}

// ─── 删除 ───
async function handleDelete(row: RuleItem) {
  try {
    await ElMessageBox.confirm(`确定删除预警规则「${row.name}」？删除后不可恢复。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteAlertRule(row.id)
    ElMessage.success('删除成功')
    await fetchList()
  } catch {
    // 用户取消或操作失败
  }
}

// ─── 启用/禁用切换 ───
async function handleToggle(row: RuleItem) {
  row._toggling = true
  try {
    await toggleAlertRule(row.id)
    row.enabled = !row.enabled
    ElMessage.success(row.enabled ? '已启用' : '已停用')
  } catch {
    // 后端已处理错误消息
  } finally {
    row._toggling = false
  }
}

// ─── 生命周期 ───
onMounted(() => {
  fetchList()
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
</style>
