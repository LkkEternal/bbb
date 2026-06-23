<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span style="font-weight: 600; font-size: 16px;">角色管理</span>
      </template>
      <el-table :data="roles">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="角色编码" width="180" />
        <el-table-column prop="name" label="角色名称" width="120" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span style="font-weight: 600; font-size: 16px;">权限说明</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="管理员 (ROLE_ADMIN)">
          管理用户、导入文献、查看所有果园、调整规则、审核反馈、查看审计日志
        </el-descriptions-item>
        <el-descriptions-item label="农户 (ROLE_FARMER)">
          查看自己的果园数据、历史预警、知识库检索、提交反馈
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
const roles = [
  { id: 1, code: 'ROLE_FARMER', name: '农户', description: '脐橙种植农户，可查看自己的果园数据和预警信息' },
  { id: 2, code: 'ROLE_ADMIN', name: '管理员', description: '系统管理员，拥有全部管理权限' },
]
</script>
