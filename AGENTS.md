# 脐橙病虫害监测系统 — 管理后台前端

Vue 3 + TypeScript + Element Plus + Pinia + UnoCSS + SCSS 管理后台。

## Project

农业物联网管理后台，管理果园、IoT 设备、病虫害识别记录、预警规则、知识库 (RAG)、农事日历、用户反馈、消息推送和系统用户/角色/日志。

- **入口**: `navel-orange-admin/src/main.ts`
- **路由**: `src/router/index.ts`
- **API 基础**: `src/api/request.ts` (Axios 实例，含 JWT 自动刷新)
- **Mock**: `src/mock/intercept.ts` (开发环境 axios-mock-adapter)

## Commands

```sh
cd navel-orange-admin
pnpm dev          # 开发服务器 → http://localhost:3000
pnpm build        # 生产构建 (vue-tsc -b && vite build)
pnpm preview      # 预览构建产物
```

后端启动见项目根目录 `start-dev.bat` / `start-dev.sh`。

## Architecture

```
src/
├── api/modules/     # API 调用模块，每个业务一个文件
├── assets/styles/   # SCSS 全局变量 + 全局样式
├── components/      # 布局组件 + 公共业务组件
│   ├── layout/      # AppLayout / Sidebar / Header / Breadcrumb
│   ├── charts/      # ECharts 图表组件
│   ├── common/      # 通用业务组件 (DiseaseTag 等)
│   └── map/         # 地图相关
├── composables/     # Vue 组合式函数
├── constants/       # 枚举/常量 (病害类型、角色等)
├── mock/modules/    # Mock JSON 数据，文件名对应模块
├── router/          # 路由配置 (含导航守卫)
├── stores/          # Pinia store (app.ts + user.ts)
├── utils/           # 工具函数 (auth/format/validate)
└── views/           # 页面视图，按模块分目录
```

## Conventions

### 命名
- **API 函数**: `camelCase` 导出具名函数 (`getDetectionList`, `createOrchard`)
- **Vue 文件**: `PascalCase` (`OrchardList.vue`, `DashboardView.vue`)
- **目录**: `kebab-case` (`views/orchard/`, `api/modules/`)
- **路由 name**: `PascalCase` (`OrchardList`, `UserManage`)
- **Pinia stores**: `useXxxStore` 模式 (`useUserStore`)

### API 模块模式
```ts
// src/api/modules/<module>.ts
import request from '../request'

export function getList(params: XxxParams) {
  return request.get('/path', { params })
}
export function createItem(data: XxxCreate) {
  return request.post('/path', data)
}
export function updateItem(id: number, data: XxxUpdate) {
  return request.put(`/path/${id}`, data)
}
export function deleteItem(id: number) {
  return request.delete(`/path/${id}`)
}
```

### 视图组件模式
- `<script setup lang="ts">` + `<template>` + `<style scoped lang="scss">`
- 状态管理: `ref()` / `reactive()` 本地状态; 跨组件用 Pinia
- 样式: scoped SCSS，利用 `variables.scss` 中的设计 token
- 布局: `page-container` → `page-header` → `search-bar` → table → `pagination-wrap` 四段式
- 搜索框中搜索按钮用 `@element-plus/icons-vue` 的 `Search` icon
- 分页: el-pagination 标准配置 (total/sizes/prev/pager/next/jumper, background)
- 新增对话框: el-dialog + el-form + el-button(cancel/confirm)
- 删除确认: ElMessageBox.confirm

### 路由配置
```ts
{
  path: 'module',
  name: 'ModuleName',
  component: () => import('@/views/module/ModuleName.vue'),
  meta: {
    title: '模块名',
    icon: 'IconName',        // 导航显示用
    hidden: true,            // 隐藏导航项
    roles: ['ROLE_ADMIN'],   // 权限控制
  },
}
```

### CSS
- 全局设计 token 在 `src/assets/styles/variables.scss`
- 全局样式在 `src/assets/styles/global.scss`
- 组件级样式用 `<style scoped lang="scss">`
- UnoCSS 已配置，可用于布局辅助类

## Notes

- 后端是 Spring Boot 3，统一响应格式 `{ code, message, data }`
- Axios 拦截器自动处理 `code===200` 提取 data，非 200 弹错误提示
- JWT 自动刷新逻辑在 `request.ts` 拦截器中

## Custom Skills

本项目的 Reasonix AI skills（位于 `.reasonix/skills/`）：

| Skill | 说明 |
|---|---|
| `gen-api-module` | 生成 Axios API 调用模块 |
| `gen-hook-crud` | 生成 vue-pure-admin 风格 CRUD Hook |
| `gen-crud-list-page` | 生成 CRUD 列表页（搜索+Table+分页+对话框） |
| `gen-detail-page` | 生成详情页 |
| `gen-route-config` | 生成路由配置 |

用法: `run_skill({ name: "skill-name", arguments: "描述" })` 或 `/skill-name`
- todo: 补充测试命令
