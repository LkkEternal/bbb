const api = require('../../utils/request')
Page({
  data: { feedbacks: [], loading: true },
  onLoad() { this.loadData() },
  onPullDownRefresh() { this.loadData().then(() => wx.stopPullDownRefresh()) },
  async loadData() {
    this.setData({ loading: true })
    try {
      const data = await api.get('/admin/feedbacks')
      this.setData({ feedbacks: data.records || [] })
    } catch (e) {}
    this.setData({ loading: false })
  },
  goDetail(e) {
    wx.navigateTo({ url: '/pages/feedback-detail/feedback-detail?id=' + e.currentTarget.dataset.id })
  },
  statusText(s) { return ['待审核','已采纳','已修正','已驳回'][s] || '未知' },
})
