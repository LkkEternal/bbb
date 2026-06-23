const DISEASE_MAP = {
  S1: { name: '健康果实', color: '#5B8C5A', type: 'healthy' },
  S2: { name: '健康叶片', color: '#5B8C5A', type: 'healthy' },
  S3: { name: '斑驳花纹', color: '#D64545', type: 'disease' },
  S4: { name: '红鼻果', color: '#D64545', type: 'disease' },
  S5: { name: '缺锌', color: '#D4A853', type: 'deficiency' },
  S6: { name: '叶脉黄化', color: '#D4A853', type: 'deficiency' },
  S7: { name: '均匀黄化', color: '#D4A853', type: 'deficiency' },
  S8: { name: '缺镁', color: '#D4A853', type: 'deficiency' },
  S9: { name: '缺硼', color: '#D4A853', type: 'deficiency' },
  S10: { name: '炭疽病', color: '#E8863A', type: 'disease' },
  S11: { name: '脂点黄斑病', color: '#E8863A', type: 'disease' },
  S12: { name: '藻斑与苔藓', color: '#9AA0A6', type: 'other' },
  S13: { name: '煤污病', color: '#9AA0A6', type: 'disease' },
  S14: { name: '溃疡病', color: '#D64545', type: 'disease' },
}

const ALERT_LEVEL = {
  1: { name: 'L1 低风险', color: '#5B8C5A' },
  2: { name: 'L2 中风险', color: '#E8863A' },
  3: { name: 'L3 高风险', color: '#D64545' },
}

function getDisease(code) {
  return DISEASE_MAP[code] || { name: code, color: '#9AA0A6', type: 'unknown' }
}

function getAlertLevel(level) {
  return ALERT_LEVEL[level] || { name: '未知', color: '#9AA0A6' }
}

function formatPercent(val) {
  return (val * 100).toFixed(1) + '%'
}

module.exports = { DISEASE_MAP, ALERT_LEVEL, getDisease, getAlertLevel, formatPercent }
