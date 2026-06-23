export const ROLE_MAP: Record<string, { name: string; color: string }> = {
  ROLE_ADMIN: { name: '管理员', color: '#F56C6C' },
  ROLE_FARMER: { name: '农户', color: '#409EFF' },
}

export const ROLE_OPTIONS = [
  { label: '管理员', value: 'ROLE_ADMIN' },
  { label: '农户', value: 'ROLE_FARMER' },
]
