const api = require('../../utils/request')
Page({
  data: { periods: [], current: null, loading: true },
  onLoad() { this.loadData() },
  async loadData() {
    this.setData({ loading: true })
    try {
      const data = await api.get('/admin/calendar')
      const periods = data.records || []
      const current = periods.find(p => p.current) || periods[3]
      this.setData({ periods, current })
    } catch (e) {}
    this.setData({ loading: false })
  },
})
