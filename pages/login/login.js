/**
 * login.js - 登录页面逻辑文件
 * 作用：处理登录页面的用户交互和API调用
 * 新手理解：这是登录页面的"灵魂"，控制页面的各种行为
 */

// 获取小程序实例（用于访问全局数据）
const app = getApp();

// 页面配置对象
Page({
  /**
   * data - 页面数据（MVVM模式中的Model）
   * 说明：页面上显示的数据都在这里定义，通过{{变量名}}绑定到WXML
   */
  data: {
    // 登录类型：'password'（密码登录）或 'sms'（短信登录）
    loginType: 'password',
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
   * onLoad - 生命周期函数：页面加载
   * 说明：页面刚打开时调用，适合做初始化工作
   */
  onLoad(options) {
    // 页面加载时的初始化逻辑（目前不需要特殊处理）
  },

  /**
   * 切换登录方式
   * @param {Object} e - 点击事件对象
   * 说明：点击登录方式标签时调用，从事件对象中获取data-type参数
   */
  switchLoginType(e) {
    // 从事件对象中获取自定义属性data-type的值
    const type = e.currentTarget.dataset.type;
    this.setData({
      loginType: type,  // 更新登录方式
      password: '',     // 清空密码输入框
      smsCode: ''       // 清空验证码输入框
    });
  },

  /**
   * 监听手机号输入
   * @param {Object} e - 输入事件对象
   * 说明：手机号输入框内容改变时自动调用，更新phone变量
   */
  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value  // e.detail.value是输入框的当前值
    });
  },

  /**
   * 监听密码输入
   * @param {Object} e - 输入事件对象
   * 说明：密码输入框内容改变时自动调用
   */
  onPasswordInput(e) {
    this.setData({
      password: e.detail.value
    });
  },

  /**
   * 监听验证码输入
   * @param {Object} e - 输入事件对象
   * 说明：验证码输入框内容改变时自动调用
   */
  onSmsCodeInput(e) {
    this.setData({
      smsCode: e.detail.value
    });
  },

  /**
   * 切换密码显示状态
   * 说明：点击密码框右侧的眼睛图标时调用，切换密码明文/密文显示
   */
  togglePassword() {
    // !this.data.showPassword 表示取反（true变false，false变true）
    this.setData({
      showPassword: !this.data.showPassword
    });
  },

  /**
   * 切换协议勾选状态
   * 说明：点击协议勾选框时调用
   */
  toggleAgreement() {
    this.setData({
      agreed: !this.data.agreed
    });
  },

  /**
   * 发送验证码
   * 说明：点击"获取验证码"按钮时调用，向后端发送短信验证码请求
   */
  sendSmsCode() {
    // 从页面数据中获取手机号
    const { phone } = this.data;
    
    // 验证1：检查手机号是否为空
    if (!phone.trim()) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'  // none表示不显示图标
      });
      return;  // 提前退出，不继续执行
    }

    // 验证2：检查手机号格式是否正确（正则表达式）
    // /^1[3-9]\d{9}$/ 表示：以1开头，第二位是3-9，后面跟9位数字
    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(phone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none'
      });
      return;
    }

    // 显示加载提示（告诉用户正在发送）
    wx.showLoading({
      title: '发送中...'
    });

    // 调用后端API发送验证码
    wx.request({
      // API地址：后端基础地址 + 接口路径
      url: `${app.globalData.baseUrl}/api/auth/sms/send`,
      method: 'POST',  // HTTP方法：POST表示提交数据
      data: {
        phone: phone,
        scene: 'login'  // 场景：login表示登录场景
      },
      // 请求成功时的回调函数
      success: (res) => {
        wx.hideLoading();  // 隐藏加载提示
        
        // 后端返回code=200表示成功
        if (res.data.code === 200) {
          wx.showToast({
            title: '验证码已发送',
            icon: 'success'  // success显示成功图标
          });
          // 启动60秒倒计时
          this.startSmsCountdown();
        } else {
          // 失败时显示后端返回的错误信息
          wx.showToast({
            title: res.data.message || '发送失败',
            icon: 'none'
          });
        }
      },
      // 请求失败时的回调函数（网络错误等）
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 验证码倒计时
   * 说明：发送验证码成功后调用，实现60秒倒计时功能
   */
  startSmsCountdown() {
    let count = 60;  // 倒计时秒数
    
    // 立即更新按钮状态：禁用+显示60秒
    this.setData({
      smsDisabled: true,
      smsButtonText: `${count}s`
    });

    // setInterval：每隔1000毫秒（1秒）执行一次回调函数
    const timer = setInterval(() => {
      count--;  // 每秒减1
      
      if (count <= 0) {
        // 倒计时结束
        clearInterval(timer);  // 清除定时器
        this.setData({
          smsDisabled: false,  // 重新启用按钮
          smsButtonText: '获取验证码'  // 恢复按钮文字
        });
      } else {
        // 更新倒计时显示
        this.setData({
          smsButtonText: `${count}s`
        });
      }
    }, 1000);
  },

  /**
   * 登录按钮点击
   * 说明：点击登录按钮时调用，进行登录验证和API调用
   */
  onLogin() {
    // 从页面数据中解构需要的变量
    const { loginType, phone, password, smsCode, agreed } = this.data;
    
    // 验证1：检查手机号是否为空
    if (!phone.trim()) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      });
      return;
    }

    // 验证2：检查手机号格式是否正确
    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(phone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none'
      });
      return;
    }

    // 验证3：检查是否同意用户协议
    if (!agreed) {
      wx.showToast({
        title: '请阅读并同意用户协议',
        icon: 'none'
      });
      return;
    }

    // 显示登录加载提示
    wx.showLoading({
      title: '登录中...'
    });

    // 根据登录方式调用不同的登录方法
    if (loginType === 'password') {
      // 密码登录：检查密码是否为空
      if (!password.trim()) {
        wx.hideLoading();
        wx.showToast({
          title: '请输入密码',
          icon: 'none'
        });
        return;
      }
      // 调用密码登录API
      this.loginByPassword(phone, password);
    } else {
      // 短信登录：检查验证码是否为空
      if (!smsCode.trim()) {
        wx.hideLoading();
        wx.showToast({
          title: '请输入验证码',
          icon: 'none'
        });
        return;
      }

      // 检查验证码是否为6位
      if (smsCode.length !== 6) {
        wx.hideLoading();
        wx.showToast({
          title: '请输入6位验证码',
          icon: 'none'
        });
        return;
      }
      // 调用短信登录API
      this.loginBySms(phone, smsCode);
    }
  },

  /**
   * 密码登录（调用后端API）
   * @param {string} phone - 用户手机号
   * @param {string} password - 用户密码
   */
  loginByPassword(phone, password) {
    wx.request({
      // API地址：密码登录接口
      url: `${app.globalData.baseUrl}/api/auth/login/password`,
      method: 'POST',
      data: {
        phone: phone,
        password: password
      },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data.code === 200) {
          // 登录成功，处理返回的数据
          this.handleLoginSuccess(res.data.data);
        } else {
          // 根据后端返回的错误码显示对应的错误信息
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
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 短信登录（调用后端API）
   * @param {string} phone - 用户手机号
   * @param {string} smsCode - 短信验证码
   */
  loginBySms(phone, smsCode) {
    wx.request({
      // API地址：短信登录接口
      url: `${app.globalData.baseUrl}/api/auth/login/sms`,
      method: 'POST',
      data: {
        phone: phone,
        smsCode: smsCode
      },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data.code === 200) {
          // 登录成功，处理返回的数据
          this.handleLoginSuccess(res.data.data);
        } else {
          // 根据后端返回的错误码显示对应的错误信息
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
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 处理登录成功
   * @param {Object} data - 后端返回的用户数据
   * 说明：登录成功后保存用户信息和Token，检查是否有果园，决定跳转目标
   */
  handleLoginSuccess(data) {
    // 构建用户信息对象
    const userInfo = {
      id: data.id,              // 用户ID
      phone: data.phone,        // 用户手机号
      username: data.username,  // 用户昵称
      avatarUrl: data.avatarUrl, // 用户头像
      roles: data.roles,        // 用户角色（如农户、管理员）
      lastLoginIp: data.lastLoginIp // 上次登录IP
    };

    // 保存用户信息到全局
    app.setUserInfo(userInfo);
    // 保存Token到全局（包含expiresIn用于计算过期时间）
    app.setToken(data.accessToken, data.refreshTokenId, data.expiresIn);

    // 显示登录成功提示
    wx.showToast({
      title: '登录成功',
      icon: 'success'
    });

    // 检查用户是否有果园，决定跳转目标
    this.checkOrchardAndRedirect(data.accessToken);
  },

  /**
   * 检查果园并跳转
   * @param {string} accessToken - 用户的访问令牌
   * 说明：登录成功后检查用户是否有果园，没有则跳转到引导页
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
          // 有果园，跳转到首页
          wx.redirectTo({
            url: '/pages/index/index'
          });
        } else {
          // 没有果园，跳转到欢迎引导页面（必须创建果园）
          wx.redirectTo({
            url: '/pages/welcome/welcome?from=login'
          });
        }
      },
      fail: () => {
        // 网络错误时提示用户
        wx.showModal({
          title: '提示',
          content: '获取果园信息失败，请检查网络后重试',
          showCancel: false,
          success: () => {
            // 提示后仍然跳转到首页，让用户可以重试
            wx.redirectTo({
              url: '/pages/index/index'
            });
          }
        });
      }
    });
  },

  /**
   * 跳转到注册页面
   * 说明：点击"立即注册"时调用
   */
  goToRegister() {
    // navigateTo：跳转到新页面（保留返回按钮）
    wx.navigateTo({
      url: '/pages/register/register'
    });
  },

  /**
   * 跳转到忘记密码页面
   * 说明：点击"忘记密码？"时调用（功能待开发）
   */
  goToForgotPassword() {
    wx.showToast({
      title: '忘记密码功能开发中',
      icon: 'none'
    });
  },

  /**
   * 微信登录
   * 说明：点击微信图标时调用，使用微信小程序登录API
   */
  onWechatLogin() {
    // 调用微信登录API获取code
    wx.login({
      success: (res) => {
        // res.code是微信返回的临时登录凭证
        if (res.code) {
          // 将code发送给后端，后端用code换取用户信息
          wx.request({
            url: `${app.globalData.baseUrl}/api/auth/login/wechat`,
            method: 'POST',
            data: {
              code: res.code
            },
            success: (response) => {
              if (response.data.code === 200) {
                // 登录成功，处理用户数据
                this.handleLoginSuccess(response.data.data);
              } else {
                wx.showToast({
                  title: response.data.message || '微信登录失败',
                  icon: 'none'
                });
              }
            },
            fail: () => {
              wx.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
              });
            }
          });
        } else {
          wx.showToast({
            title: '获取微信登录码失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '微信登录失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * QQ登录
   * 说明：点击QQ图标时调用（功能待开发）
   */
  onQQLogin() {
    wx.showToast({
      title: 'QQ登录功能开发中',
      icon: 'none'
    });
  },

  /**
   * onReady - 生命周期函数：页面初次渲染完成
   */
  onReady() {},

  /**
   * onShow - 生命周期函数：页面显示
   */
  onShow() {},

  /**
   * onHide - 生命周期函数：页面隐藏
   */
  onHide() {},

  /**
   * onUnload - 生命周期函数：页面卸载
   */
  onUnload() {},

  /**
   * onPullDownRefresh - 下拉刷新事件
   */
  onPullDownRefresh() {},

  /**
   * onReachBottom - 上拉触底事件
   */
  onReachBottom() {},

  /**
   * onShareAppMessage - 分享功能
   */
  onShareAppMessage() {}
});