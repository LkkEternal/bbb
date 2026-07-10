export interface DiseaseInfo {
  code: string
  name: string
  type: 'healthy' | 'disease' | 'deficiency' | 'other'
  severity: 1 | 2 | 3
  color: string
  category: string
}

export const DISEASE_MAP: Record<string, DiseaseInfo> = {
  S1: { code: 'S1', name: '健康果实', type: 'healthy', severity: 1, color: '#5B8C5A', category: '健康' },
  S2: { code: 'S2', name: '健康叶片', type: 'healthy', severity: 1, color: '#5B8C5A', category: '健康' },
  S3: { code: 'S3', name: '斑驳花纹', type: 'disease', severity: 3, color: '#D64545', category: '黄龙病' },
  S4: { code: 'S4', name: '红鼻果', type: 'disease', severity: 3, color: '#D64545', category: '黄龙病' },
  S5: { code: 'S5', name: '缺锌', type: 'deficiency', severity: 1, color: '#D4A853', category: '缺素症' },
  S6: { code: 'S6', name: '叶脉黄化', type: 'deficiency', severity: 1, color: '#D4A853', category: '缺素症' },
  S7: { code: 'S7', name: '均匀黄化', type: 'deficiency', severity: 1, color: '#D4A853', category: '缺素症' },
  S8: { code: 'S8', name: '缺镁', type: 'deficiency', severity: 1, color: '#D4A853', category: '缺素症' },
  S9: { code: 'S9', name: '缺硼', type: 'deficiency', severity: 1, color: '#D4A853', category: '缺素症' },
  S10: { code: 'S10', name: '炭疽病', type: 'disease', severity: 2, color: '#E8863A', category: '真菌病害' },
  S11: { code: 'S11', name: '脂点黄斑病', type: 'disease', severity: 2, color: '#E8863A', category: '真菌病害' },
  S12: { code: 'S12', name: '藻斑与苔藓', type: 'other', severity: 1, color: '#9AA0A6', category: '藻类' },
  S13: { code: 'S13', name: '煤污病', type: 'disease', severity: 1, color: '#9AA0A6', category: '真菌病害' },
  S14: { code: 'S14', name: '溃疡病', type: 'disease', severity: 2, color: '#D64545', category: '细菌病害' },
  VeinYel: { code: 'VeinYel', name: '叶脉黄化', type: 'disease', severity: 1, color: '#D4A853', category: '缺素症' },
}

export const DISEASE_LIST = Object.values(DISEASE_MAP)

export const DISEASE_CODE_OPTIONS = DISEASE_LIST.map(d => ({
  label: `${d.code} - ${d.name}`,
  value: d.code,
}))

export const ALERT_LEVEL_MAP: Record<number, { label: string; color: string }> = {
  1: { label: 'L1 低风险', color: '#5B8C5A' },
  2: { label: 'L2 中风险', color: '#E8863A' },
  3: { label: 'L3 高风险', color: '#D64545' },
}

export const DEVICE_TYPE_MAP: Record<string, string> = {
  camera: '摄像头',
  sensor: '传感器',
}

export const DEVICE_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '离线', color: '#9AA0A6' },
  1: { label: '在线', color: '#5B8C5A' },
}
