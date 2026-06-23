const app = getApp()
const BASE_URL = 'http://localhost:8080/api'

function request(options) {
  return new Promise((resolve, reject) => {
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
        wx.showToast({ title: '网络错误，请检查连接', icon: 'none' })
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
