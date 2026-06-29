// recognition.js - 自动识别页面逻辑

const app = getApp();

Page({
  data: {
    selectedImage: '',
    recognizing: false,
    recognitionResult: null
  },

  onLoad() {
    // 页面加载
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
        this.setData({
          selectedImage: res.tempFilePaths[0],
          recognitionResult: null
        });
      },
      fail: () => {
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 取消选择
   */
  cancelImage() {
    this.setData({
      selectedImage: '',
      recognitionResult: null
    });
  },

  /**
   * 开始识别
   */
  startRecognition() {
    if (!this.data.selectedImage) {
      wx.showToast({
        title: '请先选择图片',
        icon: 'none'
      });
      return;
    }

    this.setData({ recognizing: true });

    // 调用真实识别接口（如果有的话）
    // 目前没有真实接口，显示空结果
    setTimeout(() => {
      this.setData({
        recognizing: false,
        recognitionResult: null
      });
      wx.showToast({
        title: '识别功能开发中',
        icon: 'none'
      });
    }, 1000);
  },

  /**
   * 关闭结果弹窗
   */
  closeResult() {
    this.setData({
      recognitionResult: null
    });
  },

  /**
   * 查看详情
   */
  viewDetail() {
    wx.showToast({
      title: '病害详情功能开发中',
      icon: 'none'
    });
    this.closeResult();
  }
});