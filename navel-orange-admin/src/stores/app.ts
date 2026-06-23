import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const currentOrchardId = ref<number | null>(null)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setCurrentOrchard(id: number | null) {
    currentOrchardId.value = id
  }

  return {
    sidebarCollapsed,
    currentOrchardId,
    toggleSidebar,
    setCurrentOrchard,
  }
})
