const api = require('../../utils/request')
const app = getApp()

Page({
  data: {
    stats: {},
    weather: {},
    riskLevel: {},
    recentAlerts: [],
    recentDetections: [],
    loading: true,
  },
  onLoad() {
    if (!app.isLoggedIn()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.loadData()
  },
  onPullDownRefresh() { this.loadData().then(() => wx.stopPullDownRefresh()) },
  async loadData() {
    this.setData({ loading: true })
    try {
      const data = await api.get('/miniapp/dashboard')
      this.setData({
        stats: data.statistics,
        weather: data.currentWeather,
        riskLevel: data.riskLevel,
        recentAlerts: data.recentAlerts,
        recentDetections: data.recentDetections,
      })
    } catch (e) {}
    this.setData({ loading: false })
  },
  goAlerts() { wx.switchTab({ url: '/pages/alert/alert' }) },
  goOrchard() { wx.switchTab({ url: '/pages/orchard/orchard' }) },
})
