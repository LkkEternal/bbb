const app = getApp();

const LEVEL_MAP = {
  3: { icon: '🔴', cls: 'high', text: '高风险' },
  2: { icon: '🟠', cls: 'medium', text: '中风险' },
  1: { icon: '🟢', cls: 'low', text: '低风险' }
};

let envChartData = null;

function initChart(canvas, width, height, dpr) {
  const echarts = getApp().echarts;
  if (!echarts || !envChartData) return;
  const chart = echarts.init(canvas, null, { width, height, devicePixelRatio: dpr });
  canvas.setChart(chart);

  const option = {
    tooltip: { trigger: 'axis' },
      legend: { data: ['温度', '湿度'], bottom: 0 },
    grid: { left: '10%', right: '12%', top: 30, bottom: 60 },
    xAxis: {
      type: 'category',
      data: envChartData.dates,
      axisLabel: { fontSize: 10, color: '#999' },
      axisLine: { lineStyle: { color: '#eee' } },
      axisTick: { alignWithLabel: true }
    },
    yAxis: [
      { type: 'value', name: '℃', min: 0, max: 40, splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }, axisLabel: { fontSize: 10, color: '#999' } },
      { type: 'value', name: '%', min: 0, max: 100, splitLine: { show: false }, axisLabel: { fontSize: 10, color: '#999' } }
    ],
    series: [
      { name: '温度', type: 'line', smooth: true, data: envChartData.temp, yAxisIndex: 0, symbol: 'circle', symbolSize: 6, itemStyle: { color: '#FF6B6B' }, areaStyle: { color: 'rgba(255, 107, 107, 0.1)' } },
      { name: '湿度', type: 'line', smooth: true, data: envChartData.humidity, yAxisIndex: 1, symbol: 'circle', symbolSize: 6, itemStyle: { color: '#4FC3F7' }, areaStyle: { color: 'rgba(79, 195, 247, 0.1)' } }
    ]
  };

  chart.setOption(option);
  return chart;
}

Page({
  data: {
    loading: true,
    currentOrchard: '',
    currentOrchardId: null,
    currentLocation: '',
    orchardList: [],
    noOrchardTip: '',
    unreadCount: 0,

    riskIcon: '',
    riskLevelClass: '',
    riskLevelName: '',
    riskScore: 0,
    riskDescription: '',
    todayDetections: 0,
    alertCount: 0,
    deviceStatus: '-',

    diseaseRanking: [],

    hasEnvData: false,
    ec: { lazyLoad: true },

    recentAlerts: [],
    diseaseHistory: [],
    riskFactors: [],
    riskSuggestions: [],
    currentReminder: null,
    upcomingReminder: null,
    hasL3Alert: false
  },

  onLoad() {
    this.loadUserInfo();
    this.loadOrchardList();
  },

  onShow() {
    this.loadUserInfo();
    if (this.data.orchardList.length > 0) {
      this.loadDashboard();
    } else {
      this.loadOrchardList();
    }
  },

  loadUserInfo() {
    this.setData({
      userInfo: app.globalData.userInfo || {},
      isLoggedIn: !!app.globalData.isLoggedIn,
      accessToken: app.globalData.accessToken
    });
  },

  loadOrchardList() {
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/orchards`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const orchards = res.data.data.map(item => {
            let location = item.location || '';
            if (location && location.includes('POINT')) {
              const m = location.match(/POINT\(([\d.]+)\s+([\d.]+)\)/);
              if (m) location = `${m[1]},${m[2]}`;
            }
            return {
              id: item.id,
              name: item.name || '未知果园',
              area: item.areaMu ? `${item.areaMu}亩` : '0亩',
              location
            };
          });
          if (orchards.length > 0) {
            // 优先使用本地持久化的选择
            const savedId = wx.getStorageSync('selectedOrchardId');
            const saved = savedId ? orchards.find(o => o.id === savedId) : null;
            const first = saved || orchards[0];
            this.setData({
              orchardList: orchards,
              currentOrchard: first.name,
              currentOrchardId: first.id,
              currentLocation: first.location || '',
              noOrchardTip: ''
            });
            app.globalData.currentOrchardId = this.data.currentOrchardId;
            this.loadDashboard();
          } else {
            wx.redirectTo({ url: '/pages/welcome/welcome?from=login' });
          }
        } else {
          wx.redirectTo({ url: '/pages/welcome/welcome?from=login' });
        }
      },
      fail: () => {
        wx.showModal({
          title: '提示',
          content: '获取果园信息失败，请检查网络后重试',
          showCancel: false
        });
      }
    });
  },

  showOrchardPicker() {
    const names = this.data.orchardList.map(i => i.name);
    wx.showActionSheet({
      itemList: names,
      success: (res) => {
        const sel = this.data.orchardList[res.tapIndex];
        this.setData({
          currentOrchard: sel.name,
          currentOrchardId: sel.id,
          currentLocation: sel.location || ''
        });
        // 持久化选择
        wx.setStorageSync('selectedOrchardId', sel.id);
        wx.setStorageSync('selectedOrchardName', sel.name);
        app.globalData.currentOrchardId = this.data.currentOrchardId;
        wx.showLoading({ title: '加载中...' });
        setTimeout(() => {
          this.loadDashboard();
          setTimeout(() => wx.hideLoading(), 500);
        }, 100);
      }
    });
  },

  loadDashboard() {
    const token = app.globalData.accessToken;
    if (!token) {
      this.setData({ loading: false });
      this.updateDisplayData({});
      return;
    }

    this.setData({ loading: true });

    const params = {};
    if (this.data.currentOrchardId) {
      params.orchardId = this.data.currentOrchardId;
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/miniapp/dashboard`,
      method: 'GET',
      data: params,
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          const data = res.data.data || {};
          this.setData({ dashboard: data, loading: false });
          this.updateDisplayData(data);
          if (this.data.currentOrchardId) {
            this.loadEnvironmentTrend(this.data.currentOrchardId);
            this.loadDiseaseHistory(this.data.currentOrchardId);
            this.loadCurrentReminder(this.data.currentOrchardId);
            this.loadRiskAssessment(this.data.currentOrchardId);
          }
        } else {
          this.setData({ dashboard: {}, loading: false });
          this.updateDisplayData({});
        }
      },
      fail: () => {
        this.setData({ dashboard: {}, loading: false });
        this.updateDisplayData({});
      }
    });
  },

  calcRiskScore(riskClass, alertCount, todayDetections, deviceOnline, deviceTotal) {
    let base = 18;
    if (riskClass === 'medium') base = 48;
    else if (riskClass === 'high') base = 78;

    const deviceRate = deviceTotal > 0 ? deviceOnline / deviceTotal : 0;
    const bonus = Math.min(todayDetections, 12)
      + Math.min(alertCount * 5, 15)
      + Math.round(deviceRate * 10);
    return Math.min(base + bonus, 100);
  },

  getRiskDescription(riskClass, alertCount) {
    if (riskClass === 'high') {
      if (alertCount > 0) return '⚠️ 当前存在病虫害预警，建议立即查看并采取防治措施';
      return '⚠️ 今日识别数量较多，病虫害风险较高，请加强巡查';
    }
    if (riskClass === 'medium') {
      return '⚠️ 近期气候条件适宜病虫害发生，请加强监测频率';
    }
    return '✅ 果园整体状况良好，建议保持日常监测';
  },

  updateDisplayData(data) {
    const stats = data.statistics || {};
    const alerts = data.recentAlerts || [];
    const diseases = data.diseaseDistribution || [];

    const todayDetections = stats.todayDetections || 0;
    const alertCount = alerts.length;
    const deviceOnline = stats.deviceOnline || 0;
    const deviceTotal = stats.deviceTotal || 0;

    let riskIcon = '🟢';
    let riskClass = 'low';
    let riskName = '低风险';

    if (alertCount > 0 || todayDetections >= 10) {
      riskIcon = '🔴';
      riskClass = 'high';
      riskName = '高风险';
    } else if (todayDetections > 0) {
      riskIcon = '🟠';
      riskClass = 'medium';
      riskName = '中风险';
    }

    const riskScore = this.calcRiskScore(riskClass, alertCount, todayDetections, deviceOnline, deviceTotal);
    const riskDescription = this.getRiskDescription(riskClass, alertCount);
    const deviceStatus = deviceTotal > 0 ? `${deviceOnline}/${deviceTotal}` : '-';

    const totalDisease = diseases.reduce((s, d) => s + (d.value || 0), 0);
    const diseaseRanking = diseases.map(d => {
      const count = d.value || 0;
      const percent = totalDisease > 0 ? (count / totalDisease * 100) : 0;
      return {
        name: d.name || '未知',
        count,
        percent: percent.toFixed(1),
        barWidth: percent
      };
    });

    const recentAlerts = alerts.slice(0, 3).map(a => {
      const lv = LEVEL_MAP[a.level] || LEVEL_MAP[1];
      return {
        id: a.id,
        title: a.title || '预警通知',
        orchardName: a.orchardName || this.data.currentOrchard || '',
        time: a.createdAt ? this.formatTime(a.createdAt) : '',
        levelClass: lv.cls,
        levelIcon: lv.icon,
        levelLabel: lv.text,
        processed: a.processed || false,
        pushEmailStatus: a.pushEmailStatus || 'pending'
      };
    });

    const hasL3Alert = alerts.some(a => a.level === 3);

    this.setData({
      riskIcon,
      riskLevelClass: riskClass,
      riskLevelName: riskName,
      riskScore,
      riskDescription,
      todayDetections,
      alertCount,
      deviceStatus,
      diseaseRanking,
      recentAlerts,
      hasL3Alert
    });
  },

  formatTime(dateStr) {
    if (!dateStr) return '未知';
    const date = new Date(dateStr.replace(' ', 'T'));
    const diff = Date.now() - date.getTime();
    const min = Math.floor(diff / 60000);
    if (min < 1) return '刚刚';
    if (min < 60) return `${min}分钟前`;
    if (min < 1440) return `${Math.floor(min / 60)}小时前`;
    return `${Math.floor(min / 1440)}天前`;
  },

  loadEnvironmentTrend(orchardId) {
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/miniapp/orchards/${orchardId}/environment/trend?days=7`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          envChartData = {
            dates: d.dates || [],
            temp: d.temperature || [],
            humidity: d.humidity || []
          };
          this.setData({ hasEnvData: true }, () => {
            wx.nextTick(() => {
              const comp = this.selectComponent('#trend-chart');
              if (comp) comp.init(initChart);
            });
          });
        }
      }
    });
  },

  loadDiseaseHistory(orchardId) {
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/miniapp/orchards/${orchardId}/detections?page=1&size=3`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const records = res.data.data.records || [];
          this.setData({
            diseaseHistory: records.map(r => {
              const prob = r.probability || 0;
              const percent = Math.round(prob * 100);
              return {
                diseaseName: r.diseaseName || '未知病害',
                probabilityPercent: percent,
                riskClass: percent > 90 ? 'high' : percent > 70 ? 'medium' : '',
                deviceName: r.deviceName || '',
                detectedAt: r.detectedAt ? this.formatTime(r.detectedAt) : ''
              };
            })
          });
        }
      }
    });
  },

  loadCurrentReminder(orchardId) {
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/miniapp/calendar/current`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          this.setData({
            currentReminder: d.current || null,
            upcomingReminder: d.upcoming || null
          });
        }
      }
    });
  },

  loadRiskAssessment(orchardId) {
    const token = app.globalData.accessToken;
    wx.request({
      url: `${app.globalData.baseUrl}/api/alert/risk?orchardId=${orchardId}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          this.setData({
            riskFactors: d.factors || [],
            riskSuggestions: d.suggestions || []
          });
        }
      }
    });
  },

  goToDiseaseHistory() {
    const id = this.data.currentOrchardId;
    if (id) wx.navigateTo({ url: `/pages/detections/detections?id=${id}` });
  },

  goToAlerts() {
    wx.navigateTo({ url: '/pages/message/message?tab=alert' });
  },

  goToDetections() {
    wx.navigateTo({ url: '/pages/message/message?tab=detection' });
  },

  goToAlertDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/alert-detail/alert-detail?id=${id}` });
  }
});