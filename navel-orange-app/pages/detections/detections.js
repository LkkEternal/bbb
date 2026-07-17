const app = getApp();

Page({
  data: {
    orchardId: null,
    records: [],
    page: 1,
    size: 20,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.setData({ orchardId: id });
      this.loadDetections(1);
    }
  },

  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true, records: [] });
    this.loadDetections(1, () => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      const nextPage = this.data.page + 1;
      this.loadDetections(nextPage);
    }
  },

  loadDetections(page, callback) {
    const orchardId = this.data.orchardId;
    const size = this.data.size;

    this.setData({ loading: true });

    app.callApi({
      path: `/api/miniapp/orchards/${orchardId}/detections`,
      method: 'GET',
      data: { page, size },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          const newRecords = (data.records || []).map(r => {
            const prob = r.probability || 0;
            const percent = Math.round(prob * 100);
            return { ...r, probabilityPercent: percent, riskClass: percent > 90 ? 'high' : percent > 70 ? 'medium' : '' };
          });
          const records = page === 1 ? newRecords : this.data.records.concat(newRecords);
          const total = data.total || 0;

          this.setData({
            records,
            page,
            hasMore: records.length < total,
            loading: false
          });
        } else {
          this.setData({ loading: false });
        }
      },
      fail: () => {
        this.setData({ loading: false });
      },
      complete: () => {
        if (callback) callback();
      }
    });
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    if (url) {
      wx.previewImage({ urls: [url] });
    }
  },

  onImageError(e) {
    e.target.dataset || console.log('图片加载失败');
  },

  goBack() {
    wx.navigateBack();
  }
});
