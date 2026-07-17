/**
 * feedback-submit.js - 用户反馈提交页
 * 功能：选择果园、反馈类型、填写描述、上传图片、提交反馈
 */

const app = getApp();

const TYPE_OPTIONS = [
  { label: '问题反馈', value: 'issue_report' },
  { label: '拍照诊断', value: 'manual_diagnosis' }
];

Page({
  data: {
    // 果园选择
    orchardList: [],
    orchardIndex: -1,
    orchardText: '请选择果园',

    // 反馈类型
    feedbackType: 'issue_report',
    feedbackTypeText: '问题反馈',

    // 描述
    description: '',

    // 图片（只保留1张，wx.uploadFile 一次只能上传一张）
    image: '',

    // 提交状态
    submitting: false
  },

  onLoad() {
    this.loadOrchards();
  },

  /**
   * 加载果园列表
   */
  loadOrchards() {
    app.callApi({
      path: '/api/orchards',
      method: 'GET',
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          this.setData({ orchardList: res.data.data });
        }
      }
    });
  },

  /**
   * 果园选择器变更
   */
  onOrchardChange(e) {
    const index = e.detail.value;
    const orchard = this.data.orchardList[index];
    this.setData({
      orchardIndex: index,
      orchardText: orchard.name
    });
  },

  /**
   * 选择反馈类型
   */
  onTypeTap() {
    const that = this;
    wx.showActionSheet({
      itemList: TYPE_OPTIONS.map(t => t.label),
      success(res) {
        const option = TYPE_OPTIONS[res.tapIndex];
        if (option) {
          that.setData({
            feedbackType: option.value,
            feedbackTypeText: option.label
          });
        }
      }
    });
  },

  /**
   * 描述输入
   */
  onDescriptionInput(e) {
    this.setData({ description: e.detail.value });
  },

  /**
   * 选择图片
   */
  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({ image: res.tempFilePaths[0] });
      }
    });
  },

  /**
   * 移除图片
   */
  removeImage() {
    this.setData({ image: '' });
  },

  /**
   * 预览图片
   */
  previewImage() {
    if (this.data.image) {
      wx.previewImage({
        current: this.data.image,
        urls: [this.data.image]
      });
    }
  },

  /**
   * 提交反馈（调用已有的 POST /api/miniapp/feedback 接口）
   */
  submitFeedback() {
    // 校验
    if (this.data.orchardIndex < 0) {
      wx.showToast({ title: '请选择果园', icon: 'none' });
      return;
    }
    if (!this.data.image) {
      wx.showToast({ title: '请选择一张图片', icon: 'none' });
      return;
    }

    const that = this;
    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });

    const orchardId = this.data.orchardList[this.data.orchardIndex].id || 
                      this.data.orchardList[this.data.orchardIndex].orchardId;
    const accessToken = app.globalData.accessToken || wx.getStorageSync('accessToken');

    wx.uploadFile({
      url: `${app.globalData.baseUrl}/api/miniapp/feedback`,
      filePath: this.data.image,
      name: 'images',
      formData: {
        orchardId: orchardId,
        description: this.data.description,
        feedbackType: this.data.feedbackType
      },
      header: {
        'Authorization': `Bearer ${accessToken}`
      },
      success: (res) => {
        wx.hideLoading();
        try {
          const result = JSON.parse(res.data);
          if (result.code === 200) {
            wx.showToast({ title: '提交成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          } else {
            wx.showToast({ title: result.message || '提交失败', icon: 'none' });
            that.setData({ submitting: false });
          }
        } catch (e) {
          wx.showToast({ title: '解析响应失败', icon: 'none' });
          that.setData({ submitting: false });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络请求失败', icon: 'none' });
        that.setData({ submitting: false });
      }
    });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  }
});
