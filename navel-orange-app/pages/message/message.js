const app = getApp();

const LEVEL_MAP = { 3: 'high', 2: 'medium', 1: 'low' };

Page({
  data: {
    loading: true, list: [], page: 1, size: 20,
    total: 0, hasMore: false,
    filter: 'all',
    counts: { total: 0, processed: 0, unprocessed: 0 },
    currentTab: 'alert',
    detections: [],
    detectionPage: 1,
    detectionSize: 20,
    detectionTotal: 0,
    hasMoreDetections: false,
    detectionLoading: false,
    detectionFilter: 'all',
    detectionCounts: { total: 0, processed: 0, unprocessed: 0 },
    filteredDetections: []
  },

  onLoad(options) {
    // 支持从首页跳转指定 Tab
    if (options && options.tab === 'detection') {
      this.setData({ currentTab: 'detection' });
    }
    this.loadCounts();
    this.loadList(true);
  },

  onShow() {
    // 从详情页返回时强制刷新
    if (app.globalData.messageListNeedRefresh) {
      app.globalData.messageListNeedRefresh = false;
      this.setData({ page: 1, list: [] });
    }
    if (app.globalData.detectionListNeedRefresh) {
      app.globalData.detectionListNeedRefresh = false;
      this.loadDetections(true);
    }
    this.loadCounts();
    this.loadList(true);
    // 切换到识别检测页面时加载数据
    if (this.data.currentTab === 'detection' && this.data.detections.length === 0) {
      this.loadDetections(true);
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === this.data.currentTab) return;
    this.setData({ currentTab: tab });
    if (tab === 'detection' && this.data.detections.length === 0) {
      this.loadDetections(true);
    }
  },

  loadDetections(reset) {
    const token = app.globalData.accessToken;
    if (!token) return;
    if (reset) this.setData({ detectionPage: 1, detections: [] });
    this.setData({ detectionLoading: true });

    const params = { page: this.data.detectionPage, size: this.data.detectionSize };

    wx.request({
      url: `${app.globalData.baseUrl}/api/detections`,
      method: 'GET',
      data: params,
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          const records = (d.records || []).map(r => ({
            id: r.id,
            diseaseName: r.topClassName || r.deviceName || '未知',
            probability: r.topProbability ? Math.round(r.topProbability * 100) : 0,
            time: r.createdAt || r.deviceTimestamp || '',
            imageUrl: r.imageUrl || '',
            orchardName: r.orchardName || '',
            processed: r.processed === true
          }));
          const combined = reset ? records : [...this.data.detections, ...records];
          // 已处理的排在下面
          combined.sort((a, b) => {
            if (a.processed !== b.processed) return a.processed ? 1 : -1;
            return 0;
          });
          this.setData({
            detections: combined,
            detectionTotal: d.total || records.length,
            hasMoreDetections: d.page < d.pages,
            detectionLoading: false
          }, () => this.updateDetectionFilterData());
        } else {
          this.setData({ detectionLoading: false });
        }
      },
      fail: () => this.setData({ detectionLoading: false })
    });
  },

  loadMoreDetections() {
    if (!this.data.hasMoreDetections || this.data.detectionLoading) return;
    this.setData({ detectionPage: this.data.detectionPage + 1 }, () => this.loadDetections(false));
  },

  updateDetectionFilterData() {
    const all = this.data.detections;
    const processed = all.filter(d => d.processed);
    const unprocessed = all.filter(d => !d.processed);
    const filter = this.data.detectionFilter;
    let filtered = all;
    if (filter === 'processed') filtered = processed;
    else if (filter === 'unprocessed') filtered = unprocessed;
    this.setData({
      filteredDetections: filtered,
      detectionCounts: { total: all.length, processed: processed.length, unprocessed: unprocessed.length }
    });
  },

  switchDetectionFilter(e) {
    const dfilter = e.currentTarget.dataset.dfilter;
    if (dfilter === this.data.detectionFilter) return;
    this.setData({ detectionFilter: dfilter }, () => this.updateDetectionFilterData());
  },

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter;
    if (filter === this.data.filter) return;
    this.setData({ filter }, () => {
      this.loadCounts();
      this.loadList(true);
    });
  },

  loadCounts() {
    const token = app.globalData.accessToken;
    if (!token) return;
    wx.request({
      url: `${app.globalData.baseUrl}/api/messages/counts`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          this.setData({ counts: res.data.data });
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
    if (this.data.filter === 'processed') {
      params.processed = true;
    } else if (this.data.filter === 'unprocessed') {
      params.processed = false;
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/messages`,
      method: 'GET', data: params,
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          const records = (d.records || []).map(r => ({
            id: r.id, title: r.title || '预警',
            summary: r.summary || '',
            level: LEVEL_MAP[r.level] || 'low',
            time: r.createdAt ? this.formatTime(r.createdAt) : '',
            processed: r.processed === true,
            orchardName: r.orchardName || '',
            pushEmailStatus: r.pushEmailStatus || 'pending',
            levelLabel: ({3:'L3 高风险',2:'L2 中风险',1:'L1 低风险'})[r.level] || 'L1 低风险'
          }));
          const combined = reset ? records : [...this.data.list, ...records];
          // 已处理的排在下面
          combined.sort((a, b) => {
            if (a.processed !== b.processed) return a.processed ? 1 : -1;
            return 0;
          });
          this.setData({
            list: combined,
            total: d.total || 0,
            hasMore: d.page < d.pages,
            loading: false
          });
        } else { this.setData({ loading: false }); }
      },
      fail: () => { this.setData({ loading: false }); }
    });
  },

  loadMore() {
    if (!this.data.hasMore) return;
    this.setData({ page: this.data.page + 1 }, () => this.loadList(false));
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/alert-detail/alert-detail?id=${id}` });
  },

  goToDetectionDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detection-detail/detection-detail?id=${id}` });
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