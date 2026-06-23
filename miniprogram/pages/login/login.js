const api = require('../../utils/request')
const app = getApp()

Page({
  data: {
    phone: '',
    password: '',
    smsCode: '',
    loginType: 'password',
    loading: false,
    cooldown: 0,
  },
  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },
  onSmsInput(e) { this.setData({ smsCode: e.detail.value }) },
  switchType(e) { this.setData({ loginType: e.currentTarget.dataset.type }) },

  // 跳过登录，直接进入体验模式
  skipLogin() {
    app.setAuth({
      id: 99999,
      phone: '13800000000',
      username: '体验用户',
      avatarUrl: null,
      roles: [{ id: 1, code: 'ROLE_FARMER', name: '农户' }],
      accessToken: 'mock_token_skip',
      refreshTokenId: 'mock_rt_skip',
      expiresIn: 72000,
    })
    wx.switchTab({ url: '/pages/index/index' })
  },

  async sendSms() {
    if (!this.data.phone || this.data.phone.length !== 11) {
      wx.showToast({ title: '请输入正确手机号', icon: 'none' }); return
    }
    try {
      await api.post('/auth/sms/send', { phone: this.data.phone, scene: 'login' })
      wx.showToast({ title: '验证码已发送' })
      this.setData({ cooldown: 60 })
      this._timer = setInterval(() => {
        const c = this.data.cooldown - 1
        this.setData({ cooldown: c })
        if (c <= 0) clearInterval(this._timer)
      }, 1000)
    } catch (e) {}
  },

  async doLogin() {
    const { phone, password, smsCode, loginType } = this.data
    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' }); return
    }
    this.setData({ loading: true })
    try {
      let data
      if (loginType === 'password') {
        data = await api.post('/auth/login/password', { phone, password: password || '123456' })
      } else {
        data = await api.post('/auth/login/sms', { phone, smsCode: smsCode || '000000' })
      }
      if (data) {
        app.setAuth(data)
        wx.showToast({ title: '登录成功' })
        setTimeout(() => wx.switchTab({ url: '/pages/index/index' }), 300)
      }
    } catch (e) {
      // 接口失败时自动用 mock 数据进入
      this.skipLogin()
    }
    this.setData({ loading: false })
  },
})
