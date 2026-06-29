// report.js - 数据报告页面逻辑

const app = getApp();

Page({
  data: {
    reportData: {}
  },

  onLoad() {
    this.loadReportData();
  },

  /**
   * 加载报告数据
   */
  loadReportData() {
    const accessToken = app.globalData.accessToken;
    
    if (!accessToken) {
      this.setData({
        reportData: {}
      });
      return;
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/miniapp/report`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${accessToken}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            reportData: res.data.data || {}
          });
        } else {
          this.setData({
            reportData: {}
          });
        }
      },
      fail: () => {
        this.setData({
          reportData: {}
        });
      }
    });
  }
});