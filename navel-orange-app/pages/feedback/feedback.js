const app = getApp();

const STATUS_MAP = {
  0: { text: '待审核', cls: 'pending' },
  1: { text: '已采纳', cls: 'accepted' },
  2: { text: '已修正', cls: 'revised' },
  3: { text: '已驳回', cls: 'rejected' }
};

const TYPE_MAP = {
  'manual_diagnosis': '拍照诊断',
  'issue_report': '问题反馈'
};

Page({
  data: {
    loading: true,
    list: [],
    page: 1,
    size: 20,
    total: 0,
    hasMore: false,
    previewVisible: false,
    previewUrl: ''
  },

  onLoad() {
    this.loadFeedbackList();
  },

  formatRecord(r) {
    const status = STATUS_MAP[r.reviewStatus] || { text: '未知', cls: 'pending' };
    const raw = r.imageUrls;
    let urlArr = Array.isArray(raw) ? raw : [];
    if (typeof raw === 'string' && raw) {
      urlArr = raw.split(',').map(s => s.trim()).filter(Boolean);
    }
    // 补全图片URL为绝对地址（小程序<image>不支持相对路径）
    const baseUrl = app.globalData.baseUrl;
    const images = urlArr.map(url => ({ 
      url: url && !url.startsWith('http') ? baseUrl + url : url, 
      failed: false 
    }));
    return {
      ...r,
      imageUrls: urlArr,
      images: images,
      reviewStatusText: status.text,
      reviewStatusClass: status.cls,
      feedbackTypeText: TYPE_MAP[r.feedbackType] || r.feedbackType || '其他',
      showImages: images.length > 0
    };
  },

  loadFeedbackList() {
    app.callApi({
      path: '/api/miniapp/feedbacks',
      method: 'GET',
      data: {
        page: this.data.page,
        size: this.data.size
      },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          const records = (data.records || []).map(r => this.formatRecord(r));
          this.setData({
            list: this.data.page === 1 ? records : [...this.data.list, ...records],
            total: data.total || 0,
            hasMore: data.page < data.pages,
            loading: false
          });
        } else {
          this.setData({ loading: false });
        }
      },
      fail: () => {
        this.setData({ loading: false });
      }
    });
  },

  loadMore() {
    if (!this.data.hasMore) return;
    this.setData({ page: this.data.page + 1 }, () => {
      this.loadFeedbackList();
    });
  },

  goBack() {
    wx.navigateBack();
  },

  previewImage(e) {
    const cardIdx = e.currentTarget.dataset.cardIndex;
    const imgIdx = e.currentTarget.dataset.imgIndex;
    const card = this.data.list[cardIdx];
    if (card && card.images) {
      const currentUrl = card.images[imgIdx].url;
      if (currentUrl) {
        this.setData({
          previewUrl: currentUrl,
          previewVisible: true
        });
      }
    }
  },

  closePreview() {
    this.setData({ previewVisible: false });
  },

  noop() {},

  /**
   * 跳转到反馈提交页面
   */
  goToSubmit() {
    wx.navigateTo({
      url: '/pages/feedback-submit/feedback-submit'
    });
  },

  onImageError(e) {
    const cardIdx = e.currentTarget.dataset.cardIndex;
    const imgIdx = e.currentTarget.dataset.imgIndex;
    const path = `list[${cardIdx}].images[${imgIdx}].failed`;
    this.setData({ [path]: true });
  }
});