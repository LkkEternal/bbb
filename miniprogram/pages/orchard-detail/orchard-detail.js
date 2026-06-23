Page({
  data: {
    orchard: null,
    members: [],
    devices: [],
  },
  onLoad(options) {
    const id = options.id
    this.loadDetail(id)
  },
  async loadDetail(id) {
    const api = require('../../utils/request')
    try {
      const data = await api.get('/orchards/' + id)
      this.setData({ orchard: data, members: data.members || [], devices: data.devices || [] })
    } catch (e) {}
  },
  goDevices() {
    // navigate to device list
  },
})
