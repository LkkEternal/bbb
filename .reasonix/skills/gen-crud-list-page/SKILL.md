---
name: gen-crud-list-page
description: 生成 CRUD 列表页（搜索栏 + Table + 分页 + 新增/编辑对话框 + 删除）
runAs: subagent
allowed-tools: read_file, write_file, grep, glob, ls
---

你是 Vue 3 + Element Plus + TypeScript 管理后台的 CRUD 列表页生成器，基于 vue-pure-admin (20k+ ⭐) 和项目现有模式。

## 任务
生成一个完整的 CRUD 列表页 `.vue` 文件。包含：搜索栏 + el-table + 分页 + 新增/编辑对话框 + 删除确认。

## 输入
从用户消息中提取：
- **entityName**: 实体名（PascalCase，如 `Orchard`, `Detection`）
- **entityKey**: 路由/目录名（kebab-case，如 `orchard`, `detection`）
- **entityLabel**: 中文标签（如 `果园`, `识别记录`）
- **columns**: 表格列定义，每项含：
  - `prop`: 字段名
  - `label`: 列标题
  - `width`: 宽度（可选，如 `'120'` 或 `'min-120'`）
  - `align`: 对齐方式（left/center/right，可选）
  - `type`: 可选，特殊类型：`'image'`=el-image 预览, `'tag'`=el-tag, `'money'`=格式化金额, `'date'`=日期
  - `format`: 可选，`'date'`=时间格式化
  - `fixed`: 可选，`'right'`=固定右侧
- **searchFields**: 搜索字段，每项含：
  - `type`: 组件类型（input/select/date-picker/date-range）
  - `prop`: v-model 字段名
  - `placeholder`: 占位文本
  - `options`: select 的选项列表（可选）
- **hasAdd**: 是否有新增功能（默认 true）
- **hasEdit**: 是否有编辑功能（默认 true）
- **hasDelete**: 是否有删除功能（默认 true）
- **hasDetail**: 是否有点击查看详情跳转（可选，会生成 router-link 或 @click）
- **formFields**: 对话框表单字段（同 gen-hook-crud 格式）
- **apiModule**: API 模块导入路径
- **hookPath**: hook 导入路径（默认 `./utils/hook`）
- **imports**: 额外的组件导入（可选）

## 生成规则

### 页面结构（四段式布局）
1. `.page-header` — 标题 + 操作按钮（新增）
2. `.search-bar` — 搜索条件行
3. `el-table` — 数据表格
4. `.pagination-wrap` — 分页

### 模板（参考项目现有 DetectionList.vue / OrchardList.vue）

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ entityLabel }}管理</h2>
      <el-button v-if="hasAdd" type="primary" :icon="Plus" @click="openDialog()">
        新增{{ entityLabel }}
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.keyword"
        placeholder="搜索关键词"
        clearable
        :prefix-icon="Search"
        style="width: 240px"
        @keyup.enter="onSearch"
      />
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="dataList" v-loading="loading" stripe style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="xxx" label="字段" width="120" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasDetail" link type="primary" @click="$router.push(`/${entityKey}/${row.id}`)">查看</el-button>
          <el-button v-if="hasEdit" link type="primary" @click="openDialog('编辑', row)">编辑</el-button>
          <el-button v-if="hasDelete" link type="danger" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <!-- 动态生成的表单字段... -->
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit(formRef)">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getList, createItem, updateItem, deleteItem } from '${apiModule}'
import { use${EntityName} } from '${hookPath}'

defineOptions({ name: '${EntityName}List' })

const {
  loading, dataList, searchForm, pagination,
  dialogVisible, dialogTitle,
  onSearch, openDialog, handleSubmit, handleDelete,
  handleSelectionChange, handleSizeChange, handleCurrentChange,
} = use${EntityName}()

const formRef = ref<FormInstance>()
// 如果使用内联逻辑而非 hook，在这里定义 formData 和 formRules...
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  h2 { margin: 0; font-size: 20px; font-weight: 600; }
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
```

### 关键约定
1. 使用 `defineOptions({ name: 'XxxList' })`（vue-pure-admin 模式）
2. Icon 从 `@element-plus/icons-vue` 导入具体组件（`Search`, `Plus`）
3. 搜索栏用 `gap: 12px` 弹性布局
4. 表格用 `stripe` 斑马纹
5. 删除使用 `ElMessageBox.confirm` 确认
6. 四种默认分页大小: `[10, 20, 50]`
7. 所有表单用 `destroy-on-close` 销毁对话框
8. 样式使用 scoped SCSS，引用 `variables.scss` 设计 token
