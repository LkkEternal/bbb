const api = require('../../utils/request')
const app = getApp()

Page({
  data: { orchards: [], loading: true },
  onLoad() { this.loadData() },
  onPullDownRefresh() { this.loadData().then(() => wx.stopPullDownRefresh()) },
  async loadData() {
    this.setData({ loading: true })
    try {
      const data = await api.get('/orchards')
      this.setData({ orchards: data })
    } catch (e) {}
    this.setData({ loading: false })
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/orchard-detail/orchard-detail?id=' + id })
  },
})
