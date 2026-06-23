const app = getApp()
const BASE_URL = 'http://localhost:8080/api'

// 自动判断是否使用 mock：token 以 mock_ 开头，或 useMock 标志
function shouldUseMock() {
  const token = app.globalData.token || ''
  return app.globalData.useMock || token.startsWith('mock_')
}

function request(options) {
  return new Promise((resolve, reject) => {
    // Mock 模式
    if (shouldUseMock()) {
      const mockData = require('./mock.js')
      const key = options.url
      const data = mockData[key]
      if (data) {
        setTimeout(() => resolve(data), 150)
        return
      }
      // 没有对应 mock 数据时返回空
      setTimeout(() => resolve(null), 50)
      return
    }

    // 真实请求
    const token = app.globalData.token
    const header = {
      'Content-Type': 'application/json',
      ...options.header,
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      success(res) {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data)
        } else if (res.statusCode === 401) {
          app.clearAuth()
          wx.navigateTo({ url: '/pages/login/login' })
          reject(new Error('未登录'))
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message))
        }
      },
      fail(err) {
        // 网络失败时自动降级到 mock
        const mockData = require('./mock.js')
        const data = mockData[options.url]
        if (data) {
          setTimeout(() => resolve(data), 150)
          return
        }
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      },
    })
  })
}

module.exports = {
  request,
  get: (url, data, options) => request({ url, data, method: 'GET', ...options }),
  post: (url, data, options) => request({ url, data, method: 'POST', ...options }),
  put: (url, data, options) => request({ url, data, method: 'PUT', ...options }),
  del: (url, data, options) => request({ url, data, method: 'DELETE', ...options }),
}
