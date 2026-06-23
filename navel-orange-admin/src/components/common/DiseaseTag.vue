<template>
  <span class="disease-tag" :style="tagStyle">
    {{ disease.name }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DISEASE_MAP } from '@/constants/disease'

const props = defineProps<{ code: string }>()

const disease = computed(() => DISEASE_MAP[props.code] || { name: props.code, color: '#9AA0A6' })

const tagStyle = computed(() => ({
  borderColor: disease.value.color,
  color: disease.value.color,
  backgroundColor: disease.value.color + '10',
}))
</script>

<style scoped lang="scss">
.disease-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
  border-radius: 6px;
  white-space: nowrap;
  line-height: 1;
}
</style>
