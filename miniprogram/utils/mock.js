module.exports = {
  'GET /miniapp/dashboard': {
    code: 200,
    data: {
      statistics: { orchardCount: 3, deviceOnline: 7, deviceTotal: 9, todayDetections: 42, activeAlerts: 3 },
      currentWeather: { temperature: 28.5, humidity: 74, weatherDesc: '多云', windSpeed: 3.9 },
      riskLevel: { level: 2, name: '中风险', description: '近期高温高湿，溃疡病风险较高' },
      recentAlerts: [
        { id: 40001, title: '溃疡病高风险预警', level: 3, orchardName: '张家果园', createdAt: '06-17 08:00', read: false },
        { id: 40002, title: '炭疽病中风险预警', level: 2, orchardName: '李家果园', createdAt: '06-16 16:00', read: false },
        { id: 40003, title: '设备离线通知', level: 1, orchardName: '王家基地', createdAt: '06-16 10:00', read: true },
      ],
      recentDetections: [
        { id: 1, diseaseName: '溃疡病', diseaseCode: 'S14', confidence: 0.92, orchardName: '张家果园', time: '07:30' },
        { id: 2, diseaseName: '健康果实', diseaseCode: 'S1', confidence: 0.98, orchardName: '张家果园', time: '07:00' },
        { id: 3, diseaseName: '炭疽病', diseaseCode: 'S10', confidence: 0.85, orchardName: '李家果园', time: '06:30' },
      ],
    },
  },

  'GET /orchards': {
    code: 200,
    data: [
      { id: 2001, name: '张家果园', formattedAddress: '江西省赣州市信丰县嘉定镇', areaMu: 50.5, treeAge: 8, variety: '纽荷尔脐橙', deviceCount: 5, roleType: 'owner' },
      { id: 2002, name: '李家果园', formattedAddress: '江西省赣州市信丰县大塘埠镇', areaMu: 30.2, treeAge: 5, variety: '纽荷尔脐橙', deviceCount: 3, roleType: 'owner' },
      { id: 2003, name: '王家基地', formattedAddress: '江西省赣州市安远县版石镇', areaMu: 120.0, treeAge: 12, variety: '纽荷尔脐橙', deviceCount: 8, roleType: 'manager' },
    ],
  },

  'GET /miniapp/alerts': {
    code: 200,
    data: {
      records: [
        { id: 40001, title: '溃疡病高风险预警', content: '张家果园近期高温高湿，溃疡病风险等级提升为L3。', level: 3, orchardName: '张家果园', read: false, createdAt: '2026-06-17 08:00' },
        { id: 40002, title: '炭疽病中风险预警', content: '李家果园检测到炭疽病，环境条件适宜发病。', level: 2, orchardName: '李家果园', read: false, createdAt: '2026-06-16 16:00' },
        { id: 40003, title: '设备离线通知', content: '王家基地温湿度传感器已离线超过8小时。', level: 1, orchardName: '王家基地', read: true, createdAt: '2026-06-16 10:00' },
        { id: 40004, title: '黄龙病早期预警', content: '张家果园检测到斑驳花纹(S3)症状，疑似黄龙病早期。', level: 3, orchardName: '张家果园', read: true, createdAt: '2026-06-15 09:00' },
      ],
      total: 4,
    },
  },

  'GET /admin/calendar': {
    code: 200,
    data: {
      records: [
        { id: 1, name: '春梢萌发期', solarTerm: '惊蛰', startDate: '02-19', endDate: '03-05', focusPests: '疮痂病、红蜘蛛、蚜虫' },
        { id: 2, name: '花蕾-开花期', solarTerm: '清明', startDate: '03-20', endDate: '04-20', focusPests: '蓟马、花蕾蛆' },
        { id: 3, name: '谢花-幼果期', solarTerm: '谷雨', startDate: '04-20', endDate: '05-20', focusPests: '溃疡病、炭疽病' },
        { id: 4, name: '夏梢期', solarTerm: '芒种', startDate: '05-21', endDate: '07-07', focusPests: '潜叶蛾、柑橘木虱', current: true },
        { id: 5, name: '果实膨大期', solarTerm: '大暑', startDate: '07-07', endDate: '09-07', focusPests: '日灼、红蜘蛛' },
        { id: 6, name: '秋梢期', solarTerm: '白露', startDate: '09-07', endDate: '10-08', focusPests: '潜叶蛾、木虱' },
        { id: 7, name: '果实成熟期', solarTerm: '霜降', startDate: '10-08', endDate: '12-07', focusPests: '炭疽病（蒂腐）' },
      ],
    },
  },

  'POST /knowledge/search': {
    code: 200,
    data: {
      results: [
        { id: 1, documentTitle: '赣南脐橙溃疡病防治技术规程', content: '溃疡病防治应以预防为主，综合防治。在新梢展叶期和幼果期，每隔7-10天喷施一次铜制剂...', score: 0.92 },
        { id: 2, documentTitle: '脐橙病虫害绿色防控技术', content: '溃疡病的生物防治可使用枯草芽孢杆菌等微生物制剂，配合铜制剂使用效果更佳...', score: 0.85 },
      ],
    },
  },

  'POST /knowledge/qa': {
    code: 200,
    data: {
      question: '脐橙溃疡病怎么防治？',
      answer: '脐橙溃疡病防治应采取综合措施：\n\n1. 农业防治：加强栽培管理，合理施肥，增强树势；及时修剪病枝病叶，冬季清园。\n\n2. 化学防治：新梢展叶期和幼果期，每隔7-10天喷施铜制剂（如波尔多液、氢氧化铜）。\n\n3. 生物防治：可使用枯草芽孢杆菌等微生物制剂辅助防治。',
      sources: [
        { documentTitle: '赣南脐橙溃疡病防治技术规程', score: 0.92 },
      ],
    },
  },

  'GET /admin/feedbacks': {
    code: 200,
    data: {
      records: [
        { id: 90001, username: '果农张三', orchardName: '张家果园', imageUrl: '', description: '叶子上出现黄褐色斑点', aiDiagnosis: '溃疡病', aiConfidence: 0.85, matchScore: 1.0, status: 0, createdAt: '2026-06-16 14:00' },
        { id: 90002, username: '果农李四', orchardName: '李家果园', imageUrl: '', description: '果实表面有凹陷的病斑', aiDiagnosis: '炭疽病', aiConfidence: 0.78, matchScore: 0.8, status: 0, createdAt: '2026-06-15 10:00' },
      ],
      total: 2,
    },
  },
}
