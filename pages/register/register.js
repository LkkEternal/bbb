/**
 * register.js - 注册页面逻辑文件
 * 作用：处理用户注册的交互和API调用
 */

const app = getApp();

Page({
  data: {
    phone: '',
    username: '',
    password: '',
    confirmPassword: '',
    smsCode: '',
    showPassword: false,
    showConfirmPassword: false,
    agreed: false,
    smsDisabled: false,
    smsButtonText: '获取验证码'
  },

  onLoad(options) {},

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value });
  },

  onSmsCodeInput(e) {
    this.setData({ smsCode: e.detail.value });
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword });
  },

  toggleConfirmPassword() {
    this.setData({ showConfirmPassword: !this.data.showConfirmPassword });
  },

  toggleAgreement() {
    this.setData({ agreed: !this.data.agreed });
  },

  sendSmsCode() {
    const { phone } = this.data;
    
    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }

    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '发送中...' });

    wx.request({
      url: `${app.globalData.baseUrl}/api/auth/sms/send`,
      method: 'POST',
      data: {
        phone: phone,
        scene: 'register'
      },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data.code === 200) {
          wx.showToast({ title: '验证码已发送', icon: 'success' });
          this.startSmsCountdown();
        } else {
          wx.showToast({
            title: res.data.message || '发送失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      }
    });
  },

  startSmsCountdown() {
    let count = 60;
    this.setData({
      smsDisabled: true,
      smsButtonText: `${count}s`
    });

    const timer = setInterval(() => {
      count--;
      if (count <= 0) {
        clearInterval(timer);
        this.setData({
          smsDisabled: false,
          smsButtonText: '获取验证码'
        });
      } else {
        this.setData({ smsButtonText: `${count}s` });
      }
    }, 1000);
  },

  onRegister() {
    const { phone, username, password, confirmPassword, smsCode, agreed } = this.data;
    
    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }

    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    if (!username.trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }

    if (username.length < 2 || username.length > 20) {
      wx.showToast({ title: '用户名需要2-20位', icon: 'none' });
      return;
    }

    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }

    if (password.length < 6 || password.length > 20) {
      wx.showToast({ title: '密码需要6-20位', icon: 'none' });
      return;
    }

    if (password !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }

    if (!smsCode.trim()) {
      wx.showToast({ title: '请输入验证码', icon: 'none' });
      return;
    }

    if (smsCode.length !== 6) {
      wx.showToast({ title: '请输入6位验证码', icon: 'none' });
      return;
    }

    if (!agreed) {
      wx.showToast({ title: '请阅读并同意用户协议', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '注册中...' });

    wx.request({
      url: `${app.globalData.baseUrl}/api/auth/register`,
      method: 'POST',
      data: {
        phone: phone,
        username: username,
        password: password,
        smsCode: smsCode
      },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data.code === 200) {
          wx.showToast({ title: '注册成功', icon: 'success' });
          this.handleRegisterSuccess(res.data.data);
        } else {
          const errorMessages = {
            1001: '验证码错误或已过期',
            1002: '验证码发送过于频繁',
            1005: '手机号已注册'
          };
          wx.showToast({
            title: errorMessages[res.data.code] || res.data.message || '注册失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      }
    });
  },

  handleRegisterSuccess(data) {
    const userInfo = {
      id: data.id,
      phone: data.phone,
      username: data.username,
      avatarUrl: data.avatarUrl || '',
      roles: data.roles || [],
      lastLoginIp: data.lastLoginIp || ''
    };

    app.setUserInfo(userInfo);
    app.setToken(data.accessToken, data.refreshTokenId, data.expiresIn);

    wx.showToast({ title: '注册成功', icon: 'success' });

    // 跳转到欢迎引导页面
    setTimeout(() => {
      wx.redirectTo({ url: '/pages/welcome/welcome?from=register' });
    }, 1500);
  },

  goToLogin() {
    wx.navigateBack();
  },

  onReady() {},
  onShow() {},
  onHide() {},
  onUnload() {},
  onPullDownRefresh() {},
  onReachBottom() {},
  onShareAppMessage() {}
});