/**
 * alert.js - 预警消息页逻辑
 * 作用：展示预警消息列表，支持筛选
 * 
 * 后端接口：/api/miniapp/alerts（小程序专用，带权限控制）
 * - GET /api/miniapp/alerts - 获取预警列表（分页）
 *   - level: 预警等级（1=L1 / 2=L2 / 3=L3）
 *   - page: 页码，默认1
 *   - size: 每页数量，默认20
 */

const app = getApp();

Page({
  data: {
    alerts: [],
    currentFilter: 'all',
    filterOptions: [
      { label: '全部', value: 'all' },
      { label: 'L1', value: 'l1' },
      { label: 'L2', value: 'l2' },
      { label: 'L3', value: 'l3' },
      { label: '未读', value: 'unread' }
    ],
    loading: true,
    page: 1,
    size: 20,
    hasMore: true
  },

  onLoad() {
    this.loadAlertList();
  },

  onShow() {
    // 页面显示时刷新列表
    this.setData({ page: 1, hasMore: true });
    this.loadAlertList();
  },

  /**
   * 加载预警列表 - 调用后端接口 /api/miniapp/alerts
   */
  loadAlertList() {
    const accessToken = app.globalData.accessToken;
    
    // 根据当前筛选条件构建请求参数
    const params = this.buildParams();
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/miniapp/alerts`,
      method: 'GET',
      data: params,
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('预警列表接口响应:', res.data);
        if (res.data && res.data.code === 200 && res.data.data) {
          const pageResult = res.data.data;
          const records = pageResult.records || pageResult;
          
          // 如果是第一页，直接替换数据；否则追加
          const newAlerts = records.map(item => this.formatAlertItem(item));
          
          this.setData({ 
            alerts: this.data.page === 1 ? newAlerts : this.data.alerts.concat(newAlerts),
            hasMore: pageResult.totalElements > this.data.alerts.length + newAlerts.length,
            loading: false 
          });
        } else {
          console.warn('预警列表数据为空');
          this.setData({ 
            alerts: [],
            loading: false 
          });
        }
      },
      fail: (err) => {
        console.error('获取预警列表接口调用失败:', err);
        this.setData({ 
          alerts: [],
          loading: false 
        });
      }
    });
  },

  /**
   * 构建请求参数
   */
  buildParams() {
    const filter = this.data.currentFilter;
    const params = {
      page: this.data.page,
      size: this.data.size
    };
    
    // 根据筛选条件设置参数
    if (filter === 'l1') {
      params.level = 1;
    } else if (filter === 'l2') {
      params.level = 2;
    } else if (filter === 'l3') {
      params.level = 3;
    } else if (filter === 'unread') {
      params.readStatus = 0;
    }
    
    return params;
  },

  /**
   * 格式化预警数据
   */
  formatAlertItem(item) {
    const level = this.parseLevel(item.level);
    
    return {
      id: item.id,
      title: item.title || item.messageTitle || '预警消息',
      description: item.content || item.messageContent || '暂无描述',
      level: level,
      levelClass: level,
      levelText: this.getLevelText(level),
      isRead: item.read === true || item.readStatus === 1 || item.isRead,
      time: item.createdAt ? this.formatTime(item.createdAt) : (item.sendTime || item.time || '未知时间'),
      type: item.type,
      orchardId: item.orchardId,
      orchardName: item.orchardName
    };
  },

  /**
   * 解析预警级别
   * 后端返回：1=L1 / 2=L2 / 3=L3
   */
  parseLevel(level) {
    if (!level) return 'l1';
    
    const levelNum = parseInt(level);
    if (levelNum === 3) return 'l3';
    if (levelNum === 2) return 'l2';
    return 'l1';
  },

  /**
   * 获取级别显示文本
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
   * 切换筛选条件
   */
  changeFilter(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({ 
      currentFilter: value,
      page: 1,
      hasMore: true
    });
    this.loadAlertList();
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
   * 跳转到预警详情页
   */
  goToAlertDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/alert-detail/alert-detail?id=${id}`
    });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.setData({ page: 1, hasMore: true });
    this.loadAlertList(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 });
      this.loadAlertList();
    }
  }
});