/**
 * detection-detail.js - 检测详情页逻辑（匹配alert-detail风格）
 * 展示检测详情、防治建议、处理证据上传、标记已处理
 */

const app = getApp();

Page({
  data: {
    // 加载状态
    loading: true,

    // 检测基本信息
    detection: {},

    // 邮件推送状态
    pushEmailStatus: 'pending',
    pushEmailStatusText: '待推送',

    // 处理相关
    messageId: null,
    processed: false,
    treatmentImages: [],
    processing: false
  },

  onLoad(options) {
    if (options && options.id) {
      this.loadDetectionDetail(options.id);
    }
  },

  /**
   * 加载检测详情
   */
  loadDetectionDetail(id) {
    this.setData({ loading: true });
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/detections/${id}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          // 邮件推送状态
          const pushEmailStatus = d.pushEmailStatus || 'pending';
          const pushEmailStatusText = this.getPushStatusText(pushEmailStatus);

          // 处理相关
          const messageId = d.messageId || d.id || null;
          const processed = d.processed === true;
          let treatmentImages = [];
          if (d.treatmentImages) {
            try {
              treatmentImages = typeof d.treatmentImages === 'string'
                ? JSON.parse(d.treatmentImages)
                : d.treatmentImages;
            } catch (e) {
              console.error('解析treatmentImages失败:', e);
            }
          }

          this.setData({
            detection: {
              diseaseName: d.diseaseName || '',
              probability: d.topProbability ? Math.round(d.topProbability * 100) : 0,
              deviceTimestamp: d.deviceTimestamp || '',
              time: d.createdAt ? this.formatTime(d.createdAt) : '',
              imageUrl: d.imageUrl || '',
              advice: d.advice || '',
              preventionGuide: d.preventionGuide || '',
              urgency: d.urgency || '',
              consequences: d.consequences || ''
            },
            pushEmailStatus,
            pushEmailStatusText,
            messageId,
            processed,
            treatmentImages: Array.isArray(treatmentImages) ? treatmentImages : [],
            loading: false
          });
        } else {
          wx.showToast({ title: '获取检测详情失败', icon: 'none' });
          this.setData({ loading: false });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
        this.setData({ loading: false });
      }
    });
  },

  /**
   * 预览检测图片
   */
  previewImage() {
    if (this.data.detection.imageUrl) {
      wx.previewImage({ urls: [this.data.detection.imageUrl] });
    }
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
      wx.showToast({ title: '检测ID缺失', icon: 'none' });
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
              url: `${app.globalData.baseUrl}/api/detections/${that.data.messageId}/upload-treatment-image`,
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
            const newImages = [...that.data.treatmentImages, ...urls];
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
      wx.showToast({ title: '检测ID缺失', icon: 'none' });
      return;
    }

    this.setData({ processing: true });

    const accessToken = app.globalData.accessToken;
    const messageId = this.data.messageId;
    const imageUrls = this.data.treatmentImages;

    wx.request({
      url: `${app.globalData.baseUrl}/api/detections/${messageId}/process`,
      method: 'POST',
      data: { imageUrls },
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          this.setData({ processed: true });
          wx.showToast({ title: '已标记为已处理', icon: 'success' });
          // 设置全局刷新标记，返回列表页时强制刷新
          app.globalData.detectionListNeedRefresh = true;
        } else {
          wx.showToast({ title: res.data?.message || '标记失败', icon: 'none' });
        }
      },
      fail: (err) => {
        console.error('标记已处理失败:', err);
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      },
      complete: () => {
        this.setData({ processing: false });
      }
    });
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
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  /**
   * 格式化时间
   */
  formatTime(time) {
    if (!time) return '-';
    const d = new Date(time);
    const Y = d.getFullYear();
    const M = String(d.getMonth() + 1).padStart(2, '0');
    const D = String(d.getDate()).padStart(2, '0');
    const h = String(d.getHours()).padStart(2, '0');
    const m = String(d.getMinutes()).padStart(2, '0');
    return `${Y}-${M}-${D} ${h}:${m}`;
  }
});
