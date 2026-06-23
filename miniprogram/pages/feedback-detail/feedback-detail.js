Page({
  data: { feedback: null },
  onLoad(options) {
    const id = options.id
    this.loadDetail(id)
  },
  async loadDetail(id) {
    const api = require('../../utils/request')
    try {
      const data = await api.get('/admin/feedbacks/' + id)
      this.setData({ feedback: data })
    } catch (e) {}
  },
})
