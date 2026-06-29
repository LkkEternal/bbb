/**
 * app.js - 微信小程序入口文件
 * 作用：全局配置管理，包括用户状态、Token管理、API地址配置等
 * 新手理解：这是整个小程序的"大脑"，管理全局共享的数据和方法
 */

// 创建小程序实例
App({
  /**
   * onLaunch - 小程序启动时触发
   * 说明：小程序第一次打开时执行，只执行一次
   */
  onLaunch() {
    // 初始化登录状态（从本地存储恢复之前的登录信息）
    this.initLoginStatus();
    console.log('小程序启动，登录状态:', this.globalData.isLoggedIn ? '已登录' : '未登录');
  },

  /**
   * globalData - 全局共享数据
   * 说明：小程序中所有页面都可以访问这些数据
   */
  globalData: {
    // 后端服务器地址（修改这里可以切换开发/生产环境）
    // 本机调试
    baseUrl: 'http://127.0.0.1:8080',
    //baseUrl: 'http://10.168.82.246:8080',
    // 用户信息对象（登录后存储用户的id、手机号、昵称等）
    userInfo: null,
    // 是否登录（true表示已登录，false表示未登录）
    isLoggedIn: false,
    // JWT访问令牌（用于API认证，登录成功后由后端返回）
    accessToken: '',
    // 刷新令牌ID（用于刷新accessToken）
    refreshTokenId: '',
    // Token过期时间戳（用于判断是否需要刷新）
    tokenExpireTime: 0
  },

  /**
   * 设置用户信息
   * @param {Object} userInfo - 用户信息对象
   * 说明：登录成功后调用，保存用户信息到全局和本地存储
   */
  setUserInfo(userInfo) {
    // 更新全局数据
    this.globalData.userInfo = userInfo;
    this.globalData.isLoggedIn = true;
    // 保存到本地存储（防止小程序重启后登录状态丢失）
    wx.setStorageSync('userInfo', userInfo);
  },

  /**
   * 设置Token
   * @param {string} accessToken - JWT访问令牌
   * @param {string} refreshTokenId - 刷新令牌ID
   * @param {number} expiresIn - Token有效期（秒），可选
   * 说明：登录成功后调用，保存Token到全局和本地存储
   */
  setToken(accessToken, refreshTokenId, expiresIn) {
    // 更新全局数据
    this.globalData.accessToken = accessToken;
    this.globalData.refreshTokenId = refreshTokenId;
    
    // 计算Token过期时间（当前时间 + 有效期秒数）
    if (expiresIn) {
      this.globalData.tokenExpireTime = Date.now() + expiresIn * 1000;
    }
    
    // 保存到本地存储
    wx.setStorageSync('accessToken', accessToken);
    wx.setStorageSync('refreshTokenId', refreshTokenId);
    wx.setStorageSync('tokenExpireTime', this.globalData.tokenExpireTime);
  },

  /**
   * 清除用户信息（退出登录）
   * 说明：退出登录时调用，清除所有用户相关数据
   */
  clearUserInfo() {
    // 清空全局数据
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
    this.globalData.accessToken = '';
    this.globalData.refreshTokenId = '';
    // 清除本地存储
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('accessToken');
    wx.removeStorageSync('refreshTokenId');
  },

  /**
   * 初始化登录状态（从本地存储恢复）
   * 说明：小程序启动时调用，检查是否有保存的登录状态
   */
  initLoginStatus() {
    // 从本地存储读取Token和用户信息
    const accessToken = wx.getStorageSync('accessToken');
    const refreshTokenId = wx.getStorageSync('refreshTokenId');
    const userInfo = wx.getStorageSync('userInfo');
    const tokenExpireTime = wx.getStorageSync('tokenExpireTime');
    
    // 如果有Token和用户信息，说明之前登录过
    if (accessToken && userInfo) {
      // 检查Token是否过期（预留：可以在这里添加Token刷新逻辑）
      const isTokenValid = !tokenExpireTime || Date.now() < tokenExpireTime;
      
      if (isTokenValid) {
        this.globalData.accessToken = accessToken;
        this.globalData.refreshTokenId = refreshTokenId;
        this.globalData.userInfo = userInfo;
        this.globalData.tokenExpireTime = tokenExpireTime;
        this.globalData.isLoggedIn = true;
        console.log('登录状态已恢复');
      } else {
        // Token已过期，尝试刷新Token（预留）
        console.log('Token已过期，需要重新登录');
        this.clearUserInfo();
      }
    }
  },

  /**
   * 检查是否已登录
   * @returns {boolean} 是否已登录
   */
  isLoggedIn() {
    return this.globalData.isLoggedIn && this.globalData.accessToken;
  },

  /**
   * 刷新Token
   * @returns {Promise} 返回Promise对象
   * 说明：当accessToken过期时，使用refreshTokenId获取新的accessToken
   */
  refreshToken() {
    return new Promise((resolve, reject) => {
      const refreshTokenId = this.globalData.refreshTokenId || wx.getStorageSync('refreshTokenId');
      
      if (!refreshTokenId) {
        reject(new Error('没有refreshTokenId'));
        return;
      }

      wx.request({
        url: `${this.globalData.baseUrl}/api/auth/refresh`,
        method: 'POST',
        data: {
          refreshTokenId: refreshTokenId
        },
        success: (res) => {
          if (res.data.code === 200) {
            const data = res.data.data;
            // 更新Token
            this.setToken(data.accessToken, data.refreshTokenId, data.expiresIn);
            resolve(data);
          } else {
            // 刷新失败，清除登录状态
            this.clearUserInfo();
            reject(new Error(res.data.message || 'Token刷新失败'));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },

  /**
   * 退出登录
   * @returns {Promise} 返回Promise对象
   * 说明：调用后端接口使refreshToken失效，然后清除本地登录状态
   */
  logout() {
    return new Promise((resolve, reject) => {
      const refreshTokenId = this.globalData.refreshTokenId || wx.getStorageSync('refreshTokenId');
      const accessToken = this.globalData.accessToken || wx.getStorageSync('accessToken');
      
      // 如果没有登录信息，直接返回成功
      if (!accessToken) {
        this.clearUserInfo();
        resolve();
        return;
      }

      wx.request({
        url: `${this.globalData.baseUrl}/api/auth/logout`,
        method: 'POST',
        header: {
          'Authorization': `Bearer ${accessToken}`
        },
        data: {
          refreshTokenId: refreshTokenId
        },
        success: (res) => {
          // 无论后端返回什么，都清除本地登录状态
          this.clearUserInfo();
          resolve(res.data);
        },
        fail: () => {
          // 即使请求失败，也清除本地登录状态
          this.clearUserInfo();
          resolve();
        }
      });
    });
  },

  /**
   * 带自动刷新的请求封装
   * @param {Object} options - wx.request的参数
   * @param {number} retryCount - 重试次数（内部使用）
   * 说明：如果请求返回401，自动尝试刷新Token后重试
   */
  requestWithAuth(options, retryCount = 0) {
    const accessToken = this.globalData.accessToken || wx.getStorageSync('accessToken');
    
    // 添加Authorization头
    options.header = options.header || {};
    if (accessToken) {
      options.header['Authorization'] = `Bearer ${accessToken}`;
    }

    // 保存原始的success和fail回调
    const originalSuccess = options.success;
    const originalFail = options.fail;

    options.success = (res) => {
      // 如果返回401且未重试过，尝试刷新Token
      if (res.data.code === 401 && retryCount === 0) {
        this.refreshToken().then(() => {
          // Token刷新成功，重试请求
          this.requestWithAuth(options, 1);
        }).catch(() => {
          // Token刷新失败，跳转到登录页
          wx.redirectTo({ url: '/pages/login/login' });
        });
      } else if (originalSuccess) {
        originalSuccess(res);
      }
    };

    options.fail = (err) => {
      if (originalFail) {
        originalFail(err);
      }
    };

    wx.request(options);
  }
})