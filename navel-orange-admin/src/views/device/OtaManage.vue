<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>OTA 固件升级</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">新增固件包</el-button>
      </div>
    </div>

    <!-- 固件包列表 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span>固件包管理</span>
      </template>
      <el-table :data="packageList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="fileName" label="文件名" min-width="180" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize((row as FirmwarePackageVO).fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTag((row as FirmwarePackageVO).status)" size="small">
              {{ statusLabel((row as FirmwarePackageVO).status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openUpgradeDialog(row as FirmwarePackageVO)">
              升级
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchPackages"
          @current-change="fetchPackages"
        />
      </div>
    </el-card>

    <!-- 新增固件包对话框 -->
    <el-dialog v-model="createVisible" title="新增固件包" width="500px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="文件名" prop="fileName">
          <el-input v-model="createForm.fileName" placeholder="如 test_01.bin" />
        </el-form-item>
        <el-form-item label="版本号" prop="version">
          <el-input v-model="createForm.version" placeholder="如 v1.0.1" />
        </el-form-item>
        <el-form-item label="文件大小" prop="fileSize">
          <el-input-number v-model="createForm.fileSize" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下载 URL" prop="fileUrl">
          <el-input v-model="createForm.fileUrl" type="textarea" :rows="2" placeholder="OBS 文件下载 URL" />
        </el-form-item>
        <el-form-item label="MD5">
          <el-input v-model="createForm.md5" placeholder="文件校验（可选）" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="版本更新说明" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="createForm.status">
            <el-radio :value="0">待发布</el-radio>
            <el-radio :value="1">已发布</el-radio>
            <el-radio :value="2">已废弃</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 升级确认对话框 -->
    <el-dialog v-model="upgradeVisible" title="OTA 升级确认" width="420px">
      <el-form label-width="80px">
        <el-form-item label="固件版本">
          <el-tag>{{ upgradeTarget?.version }}</el-tag>
        </el-form-item>
        <el-form-item label="固件文件">
          <span>{{ upgradeTarget?.fileName }}</span>
        </el-form-item>
        <el-form-item label="设备 ID">
          <el-input v-model="upgradeDeviceId" placeholder="输入设备 ID" />
        </el-form-item>
      </el-form>
      <el-alert type="warning" :closable="false" show-icon style="margin-top: 12px">
        <template #title>
          确认向设备 #{{ upgradeDeviceId || '?' }} 下发固件 {{ upgradeTarget?.version }} 升级命令？
        </template>
      </el-alert>
      <template #footer>
        <el-button @click="upgradeVisible = false">取消</el-button>
        <el-button type="warning" :loading="upgrading" @click="submitUpgrade">确认升级</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { createPackage, getPackageList, upgradeDevice, type FirmwarePackageVO } from '@/api/modules/ota'

// ── 状态 ──

const loading = ref(false)
const packageList = ref<FirmwarePackageVO[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20 })

// 新增对话框
const createVisible = ref(false)
const createFormRef = ref<FormInstance>()
const submitting = ref(false)
const createForm = reactive({
  fileName: '',
  version: '',
  fileSize: 0,
  fileUrl: '',
  md5: '',
  description: '',
  status: 0,
})

const createRules: FormRules = {
  fileName: [{ required: true, message: '请输入文件名', trigger: 'blur' }],
  version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
  fileSize: [{ required: true, message: '请输入文件大小', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请输入下载 URL', trigger: 'blur' }],
}

// 升级对话框
const upgradeVisible = ref(false)
const upgradeTarget = ref<FirmwarePackageVO | null>(null)
const upgradeDeviceId = ref('')
const upgrading = ref(false)

// ── 方法 ──

function formatSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return `${size.toFixed(1)} ${units[i]}`
}

function statusLabel(status: number): string {
  return ['待发布', '已发布', '已废弃'][status] || '未知'
}

function statusTag(status: number): 'info' | 'success' | 'danger' {
  return (['info', 'success', 'danger'] as const)[status] ?? 'info'
}

async function fetchPackages() {
  loading.value = true
  try {
    const res = await getPackageList({ page: query.page, size: query.size })
    packageList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e: any) {
    ElMessage.error('获取固件包列表失败: ' + (e.message || ''))
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  createForm.fileName = ''
  createForm.version = ''
  createForm.fileSize = 0
  createForm.fileUrl = ''
  createForm.md5 = ''
  createForm.description = ''
  createForm.status = 0
  createVisible.value = true
}

async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createPackage({ ...createForm })
    ElMessage.success('新增固件包成功')
    createVisible.value = false
    fetchPackages()
  } catch (e: any) {
    ElMessage.error('新增失败: ' + (e.message || ''))
  } finally {
    submitting.value = false
  }
}

function openUpgradeDialog(pkg: FirmwarePackageVO) {
  upgradeTarget.value = pkg
  upgradeDeviceId.value = ''
  upgradeVisible.value = true
}

async function submitUpgrade() {
  if (!upgradeTarget.value || !upgradeDeviceId.value) {
    ElMessage.warning('请输入设备 ID')
    return
  }
  upgrading.value = true
  try {
    const res = await upgradeDevice(Number(upgradeDeviceId.value), {
      packageId: upgradeTarget.value.id,
    })
    ElMessage.success('升级命令已下发')
    ElMessageBox.alert(
      `命令 ID: ${res.data.commandId}<br>结果码: ${res.data.resultCode}`,
      '下发结果',
      { dangerouslyUseHTMLString: true },
    )
    upgradeVisible.value = false
  } catch (e: any) {
    ElMessage.error('下发失败: ' + (e.message || ''))
  } finally {
    upgrading.value = false
  }
}

onMounted(fetchPackages)
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  h2 { margin: 0; }
}
.section-card {
  margin-bottom: 16px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
