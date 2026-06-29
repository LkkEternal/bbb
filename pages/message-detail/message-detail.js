const app = getApp();

const LEVEL_MAP = {
  3: { cls: 'high', text: 'L3 高风险' },
  2: { cls: 'medium', text: 'L2 中风险' },
  1: { cls: 'low', text: 'L1 低风险' }
};

Page({
  data: {
    loading: true, id: null,
    title: '', content: '', advice: '',
    levelClass: '', levelText: '',
    orchardName: '', createdAt: '',
    readStatus: 0, readTime: ''
  },

  onLoad(options) {
    const id = parseInt(options.id);
    this.setData({ id });
    this.loadDetail(id);
  },

  loadDetail(id) {
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/messages/${id}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          const lv = LEVEL_MAP[d.level] || LEVEL_MAP[1];
          this.setData({
            title: d.title || '',
            content: d.content || d.summary || '',
            advice: d.advice || '',
            levelClass: lv.cls,
            levelText: lv.text,
            orchardName: d.orchardName || '',
            createdAt: d.createdAt || '',
            readStatus: d.readStatus || 0,
            readTime: d.readTime || '',
            loading: false
          });

        } else {
          this.setData({ loading: false });
        }
      },
      fail: () => { this.setData({ loading: false }); }
    });
  },

  toggleRead() {
    const id = this.data.id;
    const isRead = this.data.readStatus === 1;
    const url = isRead
      ? `${app.globalData.baseUrl}/api/messages/${id}/unread`
      : `${app.globalData.baseUrl}/api/messages/${id}/read`;
    wx.request({
      url, method: 'PUT',
      header: { 'Authorization': `Bearer ${app.globalData.accessToken}` },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          this.setData({ readStatus: isRead ? 0 : 1 });
          wx.showToast({ title: isRead ? '已标记未读' : '已标记已读', icon: 'success' });
        }
      }
    });
  },

  goBack() {
    wx.navigateBack();
  }
});