/**
 * alert-detail.js - 预警详情页逻辑（完整重写）
 * 作用：展示预警详情、防治建议、处理证据上传、标记已处理
 */

const app = getApp();

Page({
  data: {
    // 加载状态
    loading: true,

    // 预警基本信息
    alert: {
      id: null,
      title: '',
      content: '',
      summary: '',
      time: '',
      levelNum: 1,
      levelClass: 'l1',
      levelText: '低风险',
      orchardName: '',
      orchardId: null,
      // 新字段
      phenology: '',           // 物候期
      environmentText: '',     // 环境数据文本（温度+湿度）
      triggerRules: '温度+湿度+物候期+检测' // 触发规则概述
    },

    // 风险评分（0-100）
    riskScore: 0,
    progressColor: '#07c160',

    // 病害信息
    diseaseInfo: {
      name: '',
      probability: 0,
      imageUrl: ''    // 识别图片URL
    },

    // 防治建议
    suggestions: [],

    // 不处理的后果
    consequences: '',

    // 紧急程度
    urgency: '',

    // 邮件推送状态
    pushEmailStatus: 'pending',
    pushEmailStatusText: '待推送',

    // 处理相关
    showTreatmentSection: false,
    messageId: null,
    processed: false,
    treatmentImages: [],
    processing: false
  },

  onLoad(options) {
    const id = options?.id;
    if (id) {
      this.loadAlertDetail(id);
    } else {
      wx.showToast({ title: '缺少消息ID', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  /**
   * 加载预警详情
   */
  loadAlertDetail(id) {
    this.setData({ loading: true });

    app.callApi({
      path: `/api/messages/${id}`,
      method: 'GET',
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          try {
            const data = res.data.data;
            this.processAlertData(data);
          } catch (e) {
            console.error('处理预警数据异常:', e);
            wx.showToast({ title: '数据解析异常', icon: 'none' });
          }
        } else {
          wx.showToast({ title: res.data?.message || '预警数据为空', icon: 'none' });
        }
        this.setData({ loading: false });
      },
      fail: (err) => {
        console.error('获取预警详情失败:', err);
        wx.showToast({ title: '网络请求失败', icon: 'none' });
        this.setData({ loading: false });
      }
    });
  },

  /**
   * 处理预警数据（解析 extraData，构建各字段）
   */
  processAlertData(data) {
    // 解析级别
    const levelNum = data.level || 1;
    const levelClass = this.parseLevel(levelNum);
    const levelText = this.getLevelText(levelNum);

    // 计算风险评分（基于级别和置信度加权）
    let riskScore = levelNum * 30;
    let progressColor = '#07c160';
    if (levelNum === 2) {
      progressColor = '#fa8c16';
      riskScore = 60;
    } else if (levelNum === 3) {
      progressColor = '#f5222d';
      riskScore = 85;
    }

    // 解析 extraData
    let extra = {};
    if (data.extraData) {
      try {
        extra = typeof data.extraData === 'string'
          ? JSON.parse(data.extraData)
          : data.extraData;
      } catch (e) {
        console.error('解析extraData失败:', e);
      }
    }

    // === 病害信息 ===
    const diseaseName = extra.diseaseName || extra.disease_name || extra.disease || '';
    const probability = extra.probability || extra.confidence || 0;
    const probabilityPercent = probability
      ? (typeof probability === 'number' ? Math.round(probability * 100) : probability)
      : 0;
    const imageUrl = extra.imageUrl || extra.image_url || extra.image || '';

    // 如果级别是L3且没有置信度，用概率调整风险分
    if (probabilityPercent > 0) {
      riskScore = Math.min(99, Math.round(levelNum * 25 + probabilityPercent * 0.4));
    }

    // === 环境数据文本 ===
    const phenology = extra.phenology || '';
    const temperature = extra.temperature || '';
    const humidity = extra.humidity || '';
    let environmentText = '';
    if (temperature || humidity) {
      const parts = [];
      if (temperature) parts.push(`温度${temperature}℃`);
      if (humidity) parts.push(`湿度${humidity}%`);
      environmentText = parts.join(' / ');
    } else if (data.content) {
      // 从 content 中提取环境数据（降级方案）
      const envMatch = data.content.match(/温度[\s:]*([\d.]+)/);
      const humMatch = data.content.match(/湿度[\s:]*([\d.]+)/);
      if (envMatch) environmentText = '温度' + envMatch[1] + '℃';
      if (humMatch) environmentText += (environmentText ? ' / ' : '') + '湿度' + humMatch[1] + '%';
    }

    // === 触发规则概述 ===
    const triggerRulesParts = [];
    if (temperature) triggerRulesParts.push('温度');
    if (humidity) triggerRulesParts.push('湿度');
    if (phenology) triggerRulesParts.push('物候期');
    if (diseaseName) triggerRulesParts.push('检测');
    const triggerRules = triggerRulesParts.length > 0
      ? triggerRulesParts.join('+')
      : (data.ruleCode || '综合评估');

    // === 防治建议 ===
    const suggestions = this.parseAdvice(data.advice);

    // === 后果与紧急程度 ===
    const consequences = extra.consequences || extra.consequence || extra.warning 
      || this.extractFromAdvice(data.advice, '不处理的后果') || '';
    const urgency = extra.urgency || extra.urgent || extra.emergency 
      || this.extractFromAdvice(data.advice, '紧急程度') || '';

    // === 邮件推送状态 ===
    const pushEmailStatus = data.pushEmailStatus || 'pending';
    const pushEmailStatusText = this.getPushStatusText(pushEmailStatus);

    // === 处理区段 - 使用消息ID直接上传/处理 ===
    const showTreatmentSection = data.relatedType === 'detection' && data.relatedId != null;
    const messageId = data.id || null;

    // 尝试从预警消息中读取已存在的处理状态
    let processed = data.processed === true;
    let treatmentImages = [];
    if (data.treatmentImages) {
      try {
        treatmentImages = typeof data.treatmentImages === 'string'
          ? JSON.parse(data.treatmentImages)
          : data.treatmentImages;
      } catch (e) {
        console.error('解析treatmentImages失败:', e);
      }
    }
    // 补全图片URL为绝对地址（小程序<image>不支持相对路径）
    const baseUrl = app.globalData.baseUrl;
    const absImageUrl = imageUrl && !imageUrl.startsWith('http') ? baseUrl + imageUrl : imageUrl;
    const absTreatmentImages = Array.isArray(treatmentImages) 
      ? treatmentImages.map(u => u && !u.startsWith('http') ? baseUrl + u : u)
      : [];

    // 设置 alert 数据
    this.setData({
      alert: {
        id: data.id,
        title: data.title || '预警消息',
        content: data.content || data.summary || '',
        summary: data.summary || '',
        time: data.createdAt ? this.formatTime(data.createdAt) : '',
        levelNum,
        levelClass,
        levelText,
        orchardName: data.orchardName || '',
        orchardId: data.orchardId || null,
        phenology,
        environmentText,
        triggerRules
      },
      riskScore,
      progressColor,
      diseaseInfo: {
        name: diseaseName,
        probability: probabilityPercent,
        imageUrl: absImageUrl
      },
      suggestions,
      consequences,
      urgency,
      pushEmailStatus,
      pushEmailStatusText,
      showTreatmentSection,
      messageId,
      processed,
      treatmentImages: absTreatmentImages
    });
  },

  /**
   * 查看识别图片（图片预览）
   */
  viewImage() {
    const url = this.data.diseaseInfo.imageUrl;
    if (!url) {
      wx.showToast({ title: '暂无图片', icon: 'none' });
      return;
    }
    wx.previewImage({
      current: url,
      urls: [url]
    });
  },

  /**
   * 预览处理证据图片
   */
  previewTreatmentImage(e) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.previewImage({
        current: url,
        urls: this.data.treatmentImages
      });
    }
  },

  /**
   * 选择图片上传（处理证据）
   */
  chooseTreatmentImage() {
    if (!this.data.messageId) {
      wx.showToast({ title: '消息ID缺失', icon: 'none' });
      return;
    }
    const that = this;

    wx.chooseImage({
      count: 9,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePaths = res.tempFilePaths;
        wx.showLoading({ title: '上传中...' });

        // 逐个上传
        const uploadPromises = tempFilePaths.map(filePath => {
          return new Promise((resolve, reject) => {
            wx.uploadFile({
              url: `${app.globalData.baseUrl}/api/messages/${that.data.messageId}/upload-treatment-image`,
              filePath: filePath,
              name: 'file',
              header: {
                'Authorization': `Bearer ${app.globalData.accessToken}`
              },
              success: (uploadRes) => {
                try {
                  const result = JSON.parse(uploadRes.data);
                  if (result.code === 200) {
                    resolve(result.data);
                  } else {
                    reject(result.message || '上传失败');
                  }
                } catch (e) {
                  reject('解析响应失败');
                }
              },
              fail: (err) => {
                reject('上传请求失败');
              }
            });
          });
        });

        Promise.all(uploadPromises)
          .then((urls) => {
            wx.hideLoading();
            // 补全上传返回的URL为绝对地址
            const baseUrl = app.globalData.baseUrl;
            const absUrls = urls.map(u => u && !u.startsWith('http') ? baseUrl + u : u);
            const newImages = [...that.data.treatmentImages, ...absUrls];
            that.setData({ treatmentImages: newImages });
            wx.showToast({ title: `上传成功 ${urls.length} 张`, icon: 'success' });
          })
          .catch((err) => {
            wx.hideLoading();
            wx.showToast({ title: err || '上传失败', icon: 'none' });
          });
      }
    });
  },

  /**
   * 标记为已处理
   */
  markAsProcessed() {
    if (this.data.processed) {
      wx.showToast({ title: '已处理过', icon: 'none' });
      return;
    }
    if (!this.data.messageId) {
      wx.showToast({ title: '消息ID缺失', icon: 'none' });
      return;
    }

    this.setData({ processing: true });

    const accessToken = app.globalData.accessToken;
    const messageId = this.data.messageId;
    const imageUrls = this.data.treatmentImages;

    app.callApi({
      path: `/api/messages/${messageId}/process`,
      method: 'POST',
      data: { imageUrls },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          this.setData({ processed: true, processing: false });
          wx.showToast({ title: '已标记为已处理', icon: 'success' });
          // 设置全局刷新标记，返回列表页时强制刷新
          app.globalData.messageListNeedRefresh = true;
        } else {
          this.setData({ processing: false });
          wx.showToast({ title: res.data?.message || '标记失败', icon: 'none' });
        }
      },
      fail: (err) => {
        console.error('标记已处理失败:', err);
        this.setData({ processing: false });
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      }
    });
  },

  /**
   * 获取带圈数字（① ② ③ ... ⑳）
   */
  getCircleNum(index) {
    const circled = ['①','②','③','④','⑤','⑥','⑦','⑧','⑨','⑩',
                     '⑪','⑫','⑬','⑭','⑮','⑯','⑰','⑱','⑲','⑳'];
    return circled[index] || (index + 1);
  },

  /**
   * 从防治建议文本中提取指定章节内容
   */
  extractFromAdvice(advice, section) {
    if (!advice) return '';
    const markers = {
      '不处理的后果': ['不处理的后果', '不处理后果', '后果'],
      '紧急程度': ['紧急程度', '紧急']
    };
    const keywords = markers[section] || [section];
    for (const keyword of keywords) {
      const start = advice.indexOf(keyword);
      if (start < 0) continue;
      let startIdx = start + keyword.length;
      // 跳过冒号等分隔符
      while (startIdx < advice.length && (advice[startIdx] === '：' || advice[startIdx] === ':' || advice[startIdx] === ' ')) startIdx++;
      // 找下一个【或结尾
      const endIdx = advice.indexOf('【', startIdx);
      const result = endIdx > 0 ? advice.substring(startIdx, endIdx).trim() : advice.substring(startIdx).trim();
      if (result) return result;
    }
    return '';
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  // ===== 工具方法 =====

  /**
   * 解析预警级别（数字 -> 字符串）
   */
  parseLevel(level) {
    const num = parseInt(level);
    if (num === 3) return 'l3';
    if (num === 2) return 'l2';
    return 'l1';
  },

  /**
   * 获取级别文本
   */
  getLevelText(level) {
    const num = parseInt(level);
    if (num === 3) return '高风险';
    if (num === 2) return '中风险';
    return '低风险';
  },

  /**
   * 获取推送状态文本
   */
  getPushStatusText(status) {
    const map = {
      'sent': '已发送',
      'failed': '推送失败',
      'pending': '待推送'
    };
    return map[status] || '未知';
  },

  /**
   * 解析防治建议
   */
  parseAdvice(advice) {
    if (!advice) return [];

    // JSON 数组
    if (advice.startsWith('[') && advice.endsWith(']')) {
      try {
        return JSON.parse(advice);
      } catch (e) {
        console.error('解析advice JSON失败:', e);
      }
    }

    // 分号分隔
    if (advice.includes(';')) {
      return advice.split(';').filter(item => item.trim());
    }

    // 换行分隔
    if (advice.includes('\n')) {
      return advice.split('\n').filter(item => item.trim());
    }

    // 编号列表（1. xxx 2. xxx）
    const numbered = advice.match(/\d+\.\s*[^。]+。?/g);
    if (numbered && numbered.length > 1) {
      return numbered.map(s => s.trim());
    }

    // 单条
    return [advice];
  },

  /**
   * 格式化时间
   */
  formatTime(dateStr) {
    if (!dateStr) return '未知时间';
    try {
      const date = new Date(dateStr.replace(' ', 'T'));
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch (e) {
      return dateStr;
    }
  }
});
