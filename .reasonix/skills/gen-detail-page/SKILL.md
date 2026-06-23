---
name: gen-detail-page
description: 生成详情页（el-card + el-descriptions + 返回导航）
runAs: subagent
allowed-tools: read_file, write_file, grep, glob, ls
---

你是 Vue 3 + Element Plus + TypeScript 管理后台的详情页生成器。

## 任务
根据输入，生成一个实体详情展示页 `.vue` 文件。

## 输入
从用户消息中提取：
- **entityName**: 实体名（PascalCase，如 `Orchard`, `Detection`）
- **entityKey**: 路由/目录名（kebab-case）
- **entityLabel**: 中文标签
- **fields**: 展示字段列表，每项含：
  - `label`: 显示标签
  - `prop`: 数据字段名
  - `type`: 显示类型（`text`/`image`/`tag`/`date`/`money`）
  - `format`: 可选，日期格式等
- **apiDetailFn**: 获取详情 API 函数名
- **backRoute**: 返回列表的路由路径

## 生成规则

### 模板

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="$router.push('${backRoute}')">返回</el-button>
      <h2>{{ entityLabel }}详情</h2>
    </div>

    <el-card v-loading="loading" shadow="never">
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="field in displayFields" :key="field.prop"
          :label="field.label" :span="field.span || 1">
          <!-- 按 type 渲染不同内容 -->
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDetail } from '@/api/modules/${entityKey}'

defineOptions({ name: '${EntityName}Detail' })

const route = useRoute()
const loading = ref(false)
const detail = ref<any>({})

async function fetchDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    detail.value = await getDetail(id)
  } catch {
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  h2 { margin: 0; font-size: 20px; font-weight: 600; }
}
</style>
```

### 关键约定
1. 使用 `el-descriptions` 展示详情（替代手写样式表格）
2. 头部有 `返回` 按钮导航回列表页
3. 使用 `el-card` 包裹内容
4. 通过 `route.params.id` 获取 ID
5. `defineOptions({ name: 'XxxDetail' })` 命名组件
