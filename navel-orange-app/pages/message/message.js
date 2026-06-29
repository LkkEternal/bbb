const app = getApp();

const LEVEL_MAP = { 3: 'high', 2: 'medium', 1: 'low' };
const FILTER_MAP = { all: undefined, unread: 0, read: 1 };

Page({
  data: {
    loading: true, list: [], page: 1, size: 20,
    total: 0, totalAll: 0, hasMore: false, filter: 'all', unreadCount: 0
  },

  onLoad() {
    this.loadUnreadCount();
    this.loadList(true);
  },

  onShow() {
    this.loadUnreadCount();
    this.loadList(true);
  },

  loadUnreadCount() {
    const token = app.globalData.accessToken;
    if (!token) return;
    wx.request({
      url: `${app.globalData.baseUrl}/api/messages/unread-count`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          this.setData({ unreadCount: res.data.data.total || 0 });
        }
      }
    });
  },

  loadList(reset) {
    const token = app.globalData.accessToken;
    if (!token) { this.setData({ loading: false }); return; }
    if (reset) this.setData({ page: 1, list: [] });
    this.setData({ loading: true });

    const params = { page: this.data.page, size: this.data.size };
    const rs = FILTER_MAP[this.data.filter];
    if (rs !== undefined) params.readStatus = rs;

    wx.request({
      url: `${app.globalData.baseUrl}/api/messages`,
      method: 'GET', data: params,
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          const records = (d.records || []).map(r => ({
            id: r.id, title: r.title || '消息',
            summary: r.summary || '',
            level: LEVEL_MAP[r.level] || 'low',
            readStatus: r.readStatus || 0,
            orchardName: r.orchardName || '',
            time: r.createdAt ? this.formatTime(r.createdAt) : ''
          }));
          const extra = {};
          if (this.data.filter === 'all') extra.totalAll = d.total || 0;
          this.setData({
            list: this.data.page === 1 ? records : [...this.data.list, ...records],
            total: d.total || 0,
            hasMore: d.page < d.pages,
            loading: false,
            ...extra
          });
        } else { this.setData({ loading: false }); }
      },
      fail: () => { this.setData({ loading: false }); }
    });
  },

  switchFilter(e) {
    const f = e.currentTarget.dataset.filter;
    if (f === this.data.filter) return;
    this.setData({ filter: f }, () => this.loadList(true));
  },

  loadMore() {
    if (!this.data.hasMore) return;
    this.setData({ page: this.data.page + 1 }, () => this.loadList(false));
  },

  markAllRead() {
    wx.showModal({
      title: '全部已读',
      content: '确定将所有消息标记为已读？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.baseUrl}/api/messages/read-all`,
            method: 'PUT',
            header: { 'Authorization': `Bearer ${app.globalData.accessToken}` },
            success: (res) => {
              if (res.data && res.data.code === 200) {
                wx.showToast({ title: '已全部标记', icon: 'success' });
                this.loadUnreadCount();
                this.loadList(true);
              }
            }
          });
        }
      }
    });
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/message-detail/message-detail?id=${e.currentTarget.dataset.id}` });
  },

  goBack() {
    if (getCurrentPages().length > 1) wx.navigateBack();
    else wx.redirectTo({ url: '/pages/index/index' });
  },

  formatTime(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr.replace(' ', 'T'));
    const diff = Date.now() - date.getTime();
    const min = Math.floor(diff / 60000);
    if (min < 1) return '刚刚';
    if (min < 60) return `${min}分钟前`;
    if (min < 1440) return `${Math.floor(min / 60)}小时前`;
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
});