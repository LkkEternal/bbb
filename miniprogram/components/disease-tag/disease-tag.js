const { getDisease } = require('../../utils/disease')

Component({
  properties: { code: { type: String, value: '' } },
  data: { name: '', color: '', bgColor: '' },
  observers: {
    code(val) {
      const d = getDisease(val)
      this.setData({ name: d.name, color: d.color, bgColor: d.color + '10' })
    },
  },
})
