<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预警规则</h2>
    </div>

    <el-table :data="list" v-loading="loading" style="width: 100%">
      <el-table-column prop="ruleCode" label="规则编码" width="120" />
      <el-table-column prop="name" label="规则名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="风险等级" width="120" align="center">
        <template #default="{ row }">
          <el-tag
            v-if="ALERT_LEVEL_MAP[(row as RuleItem).level]"
            :color="ALERT_LEVEL_MAP[(row as RuleItem).level].color"
            effect="dark"
            size="small"
            style="border: none"
          >
            {{ ALERT_LEVEL_MAP[(row as RuleItem).level].label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="触发条件" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ buildConditionSummary(row as RuleItem) }}
        </template>
      </el-table-column>
      <el-table-column label="启用" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="(row as RuleItem).enabled"
            @change="handleToggle(row as RuleItem)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row as RuleItem)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" title="编辑规则" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="editForm" :rules="editRules" label-width="110px">
        <el-form-item label="最低温度(℃)" prop="tempMin">
          <el-input-number v-model="editForm.tempMin" :controls="false" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最高温度(℃)" prop="tempMax">
          <el-input-number v-model="editForm.tempMax" :controls="false" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低湿度(%)" prop="humidityMin">
          <el-input-number v-model="editForm.humidityMin" :controls="false" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最高湿度(%)" prop="humidityMax">
          <el-input-number v-model="editForm.humidityMax" :controls="false" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="病害类型">
          <el-select
            v-model="editForm.diseaseCodes"
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
        <el-form-item label="检测次数">
          <el-input-number v-model="editForm.detectionCount" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="检测天数">
          <el-input-number v-model="editForm.detectionDays" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="冷却时间(h)">
          <el-input-number v-model="editForm.cooldownHours" :min="1" :precision="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getAlertRules, updateAlertRule, toggleAlertRule } from '@/api/modules/alert'
import { ALERT_LEVEL_MAP, DISEASE_CODE_OPTIONS } from '@/constants/disease'

interface RuleItem {
  id: number
  ruleCode: string
  name: string
  level: number
  enabled: boolean
  tempMin?: number
  tempMax?: number
  humidityMin?: number
  humidityMax?: number
  diseaseCode?: string
  detectionCount?: number
  detectionDays?: number
  cooldownHours?: number
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref<RuleItem[]>([])
const editingId = ref<number>(0)

const formRef = ref<FormInstance>()
const editForm = reactive({
  tempMin: undefined as number | undefined,
  tempMax: undefined as number | undefined,
  humidityMin: undefined as number | undefined,
  humidityMax: undefined as number | undefined,
  diseaseCodes: [] as string[],
  detectionCount: undefined as number | undefined,
  detectionDays: undefined as number | undefined,
  cooldownHours: undefined as number | undefined,
})

const editRules: FormRules = {
  tempMin: [
    {
      validator: (_rule, value, callback) => {
        if (value != null && (value < -10 || value > 50)) {
          callback(new Error('温度范围 -10 ~ 50℃'))
        } else if (editForm.tempMin != null && editForm.tempMax != null && editForm.tempMin > editForm.tempMax) {
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
        if (value != null && (value < -10 || value > 50)) {
          callback(new Error('温度范围 -10 ~ 50℃'))
        } else if (editForm.tempMin != null && editForm.tempMax != null && editForm.tempMax < editForm.tempMin) {
          callback(new Error('最高温度不能小于最低温度'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
  humidityMin: [
    {
      validator: (_rule, value, callback) => {
        if (value != null && (value < 0 || value > 100)) {
          callback(new Error('湿度范围 0 ~ 100%'))
        } else if (editForm.humidityMin != null && editForm.humidityMax != null && editForm.humidityMin > editForm.humidityMax) {
          callback(new Error('最低湿度不能大于最高湿度'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
  humidityMax: [
    {
      validator: (_rule, value, callback) => {
        if (value != null && (value < 0 || value > 100)) {
          callback(new Error('湿度范围 0 ~ 100%'))
        } else if (editForm.humidityMin != null && editForm.humidityMax != null && editForm.humidityMax < editForm.humidityMin) {
          callback(new Error('最高湿度不能小于最低湿度'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
}

function buildConditionSummary(row: RuleItem): string {
  const parts: string[] = []
  if (row.tempMin != null || row.tempMax != null) {
    parts.push(`温度 ${row.tempMin ?? '-'}~${row.tempMax ?? '-'}℃`)
  }
  if (row.humidityMin != null || row.humidityMax != null) {
    parts.push(`湿度 ${row.humidityMin ?? '-'}~${row.humidityMax ?? '-'}%`)
  }
  if (row.diseaseCode) {
    const codes = row.diseaseCode.split(',').filter(Boolean)
    const names = codes.map(c => DISEASE_CODE_OPTIONS.find(o => o.value === c)?.label ?? c)
    parts.push(`病害 ${names.join('、')}`)
  }
  if (row.detectionCount) parts.push(`${row.detectionCount}次/${row.detectionDays ?? 1}天`)
  if (row.cooldownHours) parts.push(`冷却${row.cooldownHours}h`)
  return parts.join('；') || '-'
}

async function fetchList() {
  loading.value = true
  try {
    const data: any = await getAlertRules({ page: currentPage.value, size: pageSize.value })
    list.value = Array.isArray(data) ? data : data.records ?? []
    total.value = data.total ?? list.value.length
  } catch {
  } finally {
    loading.value = false
  }
}

function openEdit(row: RuleItem) {
  editingId.value = row.id
  editForm.tempMin = row.tempMin
  editForm.tempMax = row.tempMax
  editForm.humidityMin = row.humidityMin
  editForm.humidityMax = row.humidityMax
  editForm.diseaseCodes = row.diseaseCode ? row.diseaseCode.split(',').filter(Boolean) : []
  editForm.detectionCount = row.detectionCount
  editForm.detectionDays = row.detectionDays
  editForm.cooldownHours = row.cooldownHours
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      ...editForm,
      diseaseCode: editForm.diseaseCodes.join(','),
    }
    delete (payload as any).diseaseCodes
    await updateAlertRule(editingId.value, payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch {
  } finally {
    submitting.value = false
  }
}

async function handleToggle(row: RuleItem) {
  try {
    await toggleAlertRule(row.id)
    row.enabled = !row.enabled
    ElMessage.success(row.enabled ? '已启用' : '已停用')
  } catch {
  }
}

onMounted(fetchList)
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
