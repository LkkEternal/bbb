<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
      {{ item.meta?.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.meta?.title && r.path !== '/')
  return matched
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

:deep(.el-breadcrumb) {
  font-size: 13px;

  .el-breadcrumb__inner {
    color: $text-secondary;

    &.is-link {
      color: $text-secondary;
      font-weight: 400;

      &:hover {
        color: $primary;
      }
    }
  }

  .el-breadcrumb__item:last-child .el-breadcrumb__inner {
    color: $text-primary;
    font-weight: 500;
  }

  .el-breadcrumb__separator {
    color: $border;
    font-weight: 300;
  }
}
</style>
