const api = require('../../utils/request')
Page({
  data: { alerts: [], loading: true },
  onLoad() { this.loadData() },
  onPullDownRefresh() { this.loadData().then(() => wx.stopPullDownRefresh()) },
  async loadData() {
    this.setData({ loading: true })
    try {
      const data = await api.get('/miniapp/alerts')
      this.setData({ alerts: data.records || [] })
    } catch (e) {}
    this.setData({ loading: false })
  },
})
