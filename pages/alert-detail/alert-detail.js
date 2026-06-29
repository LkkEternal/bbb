/**
 * alert-detail.js - 预警详情页逻辑
 * 作用：展示预警详细信息和防治建议
 */

const app = getApp();

Page({
  data: {
    alert: {},
    suggestions: []
  },

  onLoad(options) {
    console.log('预警详情页 onLoad, options:', options);
    const id = options?.id;
    console.log('预警详情页 获取到的ID:', id);
    if (id) {
      // 每次加载都清空数据，确保不会显示旧数据
      this.setData({
        alert: {},
        suggestions: []
      });
      this.loadAlertDetail(id);
    } else {
      console.error('预警详情页 未获取到ID参数');
      this.setData({
        alert: {},
        suggestions: []
      });
    }
  },

  /**
   * 加载预警详情 - 调用后端接口
   */
  loadAlertDetail(id) {
    const accessToken = app.globalData.accessToken;
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/messages/${id}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('预警详情接口响应:', res.data);
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          
          // 解析扩展数据
          let extraData = {};
          if (data.extraData) {
            try {
              extraData = JSON.parse(data.extraData);
            } catch (e) {
              console.error('解析extraData失败:', e);
            }
          }
          
          const level = this.parseLevel(data.level);
          const diseaseName = extraData.diseaseName || extraData.disease_name || '';
          
          this.setData({
            alert: {
              id: data.id,
              title: data.title || this.getAlertTitle(data.messageType, diseaseName),
              content: data.content || data.summary || '',
              time: data.createdAt ? this.formatTime(data.createdAt) : '',
              level: level,
              levelClass: level,
              levelText: this.getLevelText(level),
              orchardName: data.orchardName || '',
              deviceName: extraData.deviceName || extraData.device_name || '',
              typeName: this.getTypeText(data.messageType),
              severityText: this.getSeverityText(data.level),
              diseaseName: diseaseName,
              confidence: extraData.confidence || extraData.probability || ''
            },
            suggestions: this.parseAdvice(data.advice)
          });
          
          // 标记为已读（调用后端标记已读接口）
          this.markAsRead(id);
        } else {
          console.warn('预警详情数据为空');
          this.setData({
            alert: {},
            suggestions: []
          });
        }
      },
      fail: (err) => {
        console.error('获取预警详情接口调用失败:', err);
        this.setData({
          alert: {},
          suggestions: []
        });
      }
    });
  },

  /**
   * 标记预警为已读
   */
  markAsRead(id) {
    const accessToken = app.globalData.accessToken;
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/messages/${id}/read`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('标记已读成功:', res.data);
      },
      fail: (err) => {
        console.error('标记已读失败:', err);
      }
    });
  },

  /**
   * 解析防治建议
   */
  parseAdvice(advice) {
    if (!advice) return [];
    
    // 如果是JSON数组格式
    if (advice.startsWith('[') && advice.endsWith(']')) {
      try {
        return JSON.parse(advice);
      } catch (e) {
        console.error('解析advice JSON失败:', e);
      }
    }
    
    // 如果是分号分隔的字符串
    if (advice.includes(';')) {
      return advice.split(';').filter(item => item.trim());
    }
    
    // 如果是换行分隔的字符串
    if (advice.includes('\n')) {
      return advice.split('\n').filter(item => item.trim());
    }
    
    // 默认作为单条建议
    return [advice];
  },

  /**
   * 获取预警标题
   */
  getAlertTitle(type, diseaseName) {
    if (!type && !diseaseName) return '预警消息';
    
    const typeMap = {
      'alert': `${diseaseName || '病害'}预警`,
      'DISEASE': `${diseaseName || '病害'}预警`,
      'TEMPERATURE': '温度预警',
      'HUMIDITY': '湿度预警',
      'RAIN': '降雨预警',
      'WATER': '灌溉预警',
      'SYSTEM': '系统预警',
      'calendar': '农事提醒',
      'system': '系统通知'
    };
    
    return typeMap[type] || `${diseaseName || '预警'}消息`;
  },

  /**
   * 解析预警级别
   */
  parseLevel(level) {
    if (!level) return 'l1';
    
    const levelStr = String(level).toLowerCase();
    if (levelStr === 'l3' || levelStr === 'high' || levelStr === '3' || levelStr === '高') return 'l3';
    if (levelStr === 'l2' || levelStr === 'medium' || levelStr === '2' || levelStr === '中') return 'l2';
    return 'l1';
  },

  /**
   * 获取级别文本
   */
  getLevelText(level) {
    const textMap = {
      'l1': 'L1',
      'l2': 'L2',
      'l3': 'L3'
    };
    return textMap[level] || 'L1';
  },

  /**
   * 获取类型文本
   */
  getTypeText(type) {
    const typeMap = {
      'alert': '预警消息',
      'DISEASE': '病害预警',
      'TEMPERATURE': '温度预警',
      'HUMIDITY': '湿度预警',
      'RAIN': '降雨预警',
      'WATER': '灌溉预警',
      'SYSTEM': '系统预警',
      'calendar': '农事提醒',
      'system': '系统通知'
    };
    return typeMap[type] || type || '未知类型';
  },

  /**
   * 获取严重程度文本
   */
  getSeverityText(level) {
    const levelStr = String(level).toLowerCase();
    if (levelStr === 'l3' || levelStr === 'high' || levelStr === '3' || levelStr === '高') return '高';
    if (levelStr === 'l2' || levelStr === 'medium' || levelStr === '2' || levelStr === '中') return '中';
    return '低';
  },

  /**
   * 格式化时间显示
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
      console.error('时间格式化失败:', e);
      return dateStr;
    }
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  /**
   * 处理预警
   */
  handleAlert() {
    wx.showToast({
      title: '处理功能开发中',
      icon: 'none'
    });
  },

  /**
   * 分享预警
   */
  shareAlert() {
    wx.showToast({
      title: '分享功能开发中',
      icon: 'none'
    });
  }
});