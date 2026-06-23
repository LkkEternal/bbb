const { getAlertLevel } = require('../../utils/disease')

Component({
  properties: {
    alert: { type: Object, value: {} },
  },
  data: { levelColor: '#5B8C5A', levelName: '' },
  observers: {
    'alert.level'(val) {
      const l = getAlertLevel(val)
      this.setData({ levelColor: l.color, levelName: l.name })
    },
  },
})
