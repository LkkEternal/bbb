/**
 * login.js - 登录页面（密码登录 + 短信验证码登录）
 * 参考设计稿：状态栏 + Logo 欢迎回来 + Tab切换 + 记住我 + 协议勾选
 */
const app = getApp();

Page({
  data: {
    // 登录类型：'password'（密码登录）或 'sms'（短信登录）
    loginType: 'sms',
    // 用户输入的手机号
    phone: '',
    // 用户输入的密码
    password: '',
    // 用户输入的验证码
    smsCode: '',
    // 是否显示密码（true=明文，false=密文）
    showPassword: false,
    // 是否同意用户协议
    agreed: false,
    // 验证码按钮是否禁用（倒计时期间禁用）
    smsDisabled: false,
    // 验证码按钮文字（显示"获取验证码"或倒计时秒数）
    smsButtonText: '获取验证码'
  },

  /**
   * onLoad - 页面加载
   */
  onLoad(options) {
    // 适配自定义状态栏高度
    const systemInfo = wx.getSystemInfoSync();
    const statusBarHeight = systemInfo.statusBarHeight || 44;
    this.setData({
      'statusBarHeight': statusBarHeight
    });

    // 如果已登录，自动跳过登录页
    if (app.globalData.isLoggedIn && app.globalData.accessToken) {
      this.checkOrchardAndRedirect(app.globalData.accessToken);
    }
  },

  /**
   * 切换登录方式
   */
  switchLoginType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      loginType: type,
      password: '',
      smsCode: ''
    });
  },

  /**
   * 监听手机号输入
   */
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  /**
   * 监听密码输入
   */
  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  /**
   * 监听验证码输入
   */
  onSmsCodeInput(e) {
    this.setData({ smsCode: e.detail.value });
  },

  /**
   * 切换密码显示状态
   */
  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword });
  },

  /**
   * 切换协议勾选状态
   */
  toggleAgreement() {
    this.setData({ agreed: !this.data.agreed });
  },

  /**
   * 查看用户协议
   */
  onViewAgreement() {
    wx.showToast({ title: '查看用户协议', icon: 'none' });
  },

  /**
   * 查看隐私政策
   */
  onViewPrivacy() {
    wx.showToast({ title: '查看隐私政策', icon: 'none' });
  },

  /**
   * 发送验证码
   */
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
        scene: 'login'
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.code === 200) {
          wx.showToast({ title: '验证码已发送', icon: 'success' });
          this.startSmsCountdown();
        } else {
          wx.showToast({ title: res.data.message || '发送失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      }
    });
  },

  /**
   * 验证码倒计时
   */
  startSmsCountdown() {
    let count = 60;
    this.setData({ smsDisabled: true, smsButtonText: `${count}s` });

    const timer = setInterval(() => {
      count--;
      if (count <= 0) {
        clearInterval(timer);
        this.setData({ smsDisabled: false, smsButtonText: '获取验证码' });
      } else {
        this.setData({ smsButtonText: `${count}s` });
      }
    }, 1000);
  },

  /**
   * 登录按钮点击
   */
  onLogin() {
    const { loginType, phone, password, smsCode, agreed } = this.data;

    console.log('[登录] 点击登录按钮', { loginType, phone, agreed });

    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }

    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    if (!agreed) {
      console.log('[登录] 协议未勾选，拦截');
      wx.showToast({ title: '请阅读并同意用户协议', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中...' });

    if (loginType === 'password') {
      if (!password.trim()) {
        wx.hideLoading();
        wx.showToast({ title: '请输入密码', icon: 'none' });
        return;
      }
      console.log('[登录] 发起密码登录请求');
      this.loginByPassword(phone, password);
    } else {
      if (!smsCode.trim()) {
        wx.hideLoading();
        wx.showToast({ title: '请输入验证码', icon: 'none' });
        return;
      }
      if (smsCode.length !== 6) {
        wx.hideLoading();
        wx.showToast({ title: '请输入6位验证码', icon: 'none' });
        return;
      }
      console.log('[登录] 发起短信登录请求');
      this.loginBySms(phone, smsCode);
    }
  },

  /**
   * 密码登录
   */
  loginByPassword(phone, password) {
    const url = `${app.globalData.baseUrl}/api/auth/login/password`;
    console.log('[登录] 请求地址:', url);
    console.log('[登录] 请求数据:', { phone, password: '***' });

    wx.request({
      url: url,
      method: 'POST',
      header: {
        'Content-Type': 'application/json'
      },
      data: { phone, password },
      success: (res) => {
        wx.hideLoading();
        console.log('[登录] 后端返回:', res.data);
        if (res.data.code === 200) {
          this.handleLoginSuccess(res.data.data);
        } else {
          const errorMessages = {
            1003: '密码错误',
            1004: '账号已被禁用',
            404: '账号不存在'
          };
          wx.showToast({
            title: errorMessages[res.data.code] || res.data.message || '登录失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('[登录] 请求失败:', err);
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      }
    });
  },

  /**
   * 短信登录
   */
  loginBySms(phone, smsCode) {
    const url = `${app.globalData.baseUrl}/api/auth/login/sms`;
    console.log('[登录] 请求地址:', url);
    console.log('[登录] 请求数据:', { phone, smsCode });

    wx.request({
      url: url,
      method: 'POST',
      header: {
        'Content-Type': 'application/json'
      },
      data: { phone, smsCode },
      success: (res) => {
        wx.hideLoading();
        console.log('[登录] 后端返回:', res.data);
        if (res.data.code === 200) {
          this.handleLoginSuccess(res.data.data);
        } else {
          const errorMessages = {
            1001: '验证码错误或已过期',
            404: '账号不存在'
          };
          wx.showToast({
            title: errorMessages[res.data.code] || res.data.message || '登录失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('[登录] 请求失败:', err);
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      }
    });
  },

  /**
   * 处理登录成功
   */
  handleLoginSuccess(data) {
    const userInfo = {
      id: data.id,
      phone: data.phone,
      username: data.username,
      avatarUrl: data.avatarUrl,
      roles: data.roles,
      lastLoginIp: data.lastLoginIp
    };
    app.setUserInfo(userInfo);
    app.setToken(data.accessToken, data.refreshTokenId, data.expiresIn);

    wx.showToast({ title: '登录成功', icon: 'success' });
    this.checkOrchardAndRedirect(data.accessToken);
  },

  /**
   * 检查果园并跳转
   */
  checkOrchardAndRedirect(accessToken) {
    wx.request({
      url: `${app.globalData.baseUrl}/api/orchards`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          wx.redirectTo({ url: '/pages/index/index' });
        } else {
          wx.redirectTo({ url: '/pages/welcome/welcome?from=login' });
        }
      },
      fail: () => {
        wx.showModal({
          title: '提示',
          content: '获取果园信息失败，请检查网络后重试',
          showCancel: false,
          success: () => {
            wx.redirectTo({ url: '/pages/index/index' });
          }
        });
      }
    });
  },

  /**
   * 跳转到注册页面
   */
  goToRegister() {
    wx.navigateTo({ url: '/pages/register/register?from=login' });
  }
});
