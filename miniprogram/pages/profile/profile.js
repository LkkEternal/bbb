const app = getApp()
Page({
  data: { userInfo: null, isLoggedIn: false },
  onShow() {
    const info = app.globalData.userInfo
    this.setData({ userInfo: info, isLoggedIn: !!info })
  },
  goLogin() { wx.navigateTo({ url: '/pages/login/login' }) },
  goCalendar() { wx.navigateTo({ url: '/pages/calendar/calendar' }) },
  goFeedback() { wx.navigateTo({ url: '/pages/feedback/feedback' }) },
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录？',
      success: (res) => {
        if (res.confirm) {
          app.clearAuth()
          this.setData({ userInfo: null, isLoggedIn: false })
          wx.showToast({ title: '已退出' })
        }
      },
    })
  },
})
