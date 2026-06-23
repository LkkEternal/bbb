export const FEEDBACK_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '待审核', color: '#E6A23C' },
  1: { label: '已采纳', color: '#67C23A' },
  2: { label: '已修正', color: '#409EFF' },
  3: { label: '已驳回', color: '#F56C6C' },
}

export const KNOWLEDGE_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '待处理', color: '#E6A23C' },
  1: { label: '处理中', color: '#409EFF' },
  2: { label: '已完成', color: '#67C23A' },
  3: { label: '处理失败', color: '#F56C6C' },
}

export const ORCHARD_ROLE_MAP: Record<string, { label: string; color: string }> = {
  owner: { label: '园主', color: '#F56C6C' },
  manager: { label: '管理员', color: '#409EFF' },
  viewer: { label: '查看者', color: '#909399' },
}
