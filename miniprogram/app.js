App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: '',
    refreshTokenId: '',
    userInfo: null,
  },

  onLaunch() {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (token) {
      this.globalData.token = token
      this.globalData.refreshTokenId = wx.getStorageSync('refreshTokenId')
      this.globalData.userInfo = userInfo ? JSON.parse(userInfo) : null
    }
  },

  setAuth(data) {
    this.globalData.token = data.accessToken
    this.globalData.refreshTokenId = data.refreshTokenId
    this.globalData.userInfo = {
      id: data.id,
      phone: data.phone,
      username: data.username,
      avatarUrl: data.avatarUrl,
      roles: data.roles,
    }
    wx.setStorageSync('token', data.accessToken)
    wx.setStorageSync('refreshTokenId', data.refreshTokenId)
    wx.setStorageSync('userInfo', JSON.stringify(this.globalData.userInfo))
  },

  clearAuth() {
    this.globalData.token = ''
    this.globalData.refreshTokenId = ''
    this.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('refreshTokenId')
    wx.removeStorageSync('userInfo')
  },

  isLoggedIn() {
    return !!this.globalData.token
  },
})
