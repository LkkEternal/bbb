const app = getApp();

Page({
  data: {
    avatarUrl: '',
    avatarText: '👤',
    username: '用户',
    phone: '未绑定手机号',

    info: {},

    showInfoModal: false,
    roleName: '',

    showPasswordModal: false,
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',

    showNotifyModal: false,
    smsEnabled: false,
    pushEnabled: false
  },

  onLoad() {
    this.loadProfile();
  },

  onShow() {
    this.loadProfile();
    this.loadUserFromGlobal();
  },

  loadUserFromGlobal() {
    const userInfo = app.globalData.userInfo;
    if (userInfo) {
      this.setData({
        username: userInfo.username || '用户',
        phone: userInfo.phone || '未绑定手机号'
      });
    }
  },

  loadProfile() {
    const token = app.globalData.accessToken;
    if (!token) {
      return;
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/user/profile`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          const roleName = data.roles && data.roles.length > 0 ? data.roles[0].name : '农户';

          this.setData({
            avatarUrl: data.avatarUrl || '',
            username: data.username || '用户',
            phone: data.phone || '未绑定手机号',
            info: data,
            roleName: roleName,
            smsEnabled: data.smsEnabled || false,
            pushEnabled: data.pushEnabled || false
          });
        }
      },
      fail: () => {}
    });
  },

  changeAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];
        wx.showLoading({ title: '上传中...' });

        wx.uploadFile({
          url: `${app.globalData.baseUrl}/api/user/avatar`,
          filePath: tempFilePath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${app.globalData.accessToken}`
          },
          success: (uploadRes) => {
            wx.hideLoading();
            const result = JSON.parse(uploadRes.data);
            if (result.code === 200) {
              this.setData({ avatarUrl: result.data });
              wx.showToast({ title: '头像更新成功', icon: 'success' });
            } else {
              wx.showToast({ title: result.message || '上传失败', icon: 'none' });
            }
          },
          fail: () => {
            wx.hideLoading();
            wx.showToast({ title: '上传失败', icon: 'none' });
          }
        });
      }
    });
  },

  showPersonalInfo() {
    this.loadProfile();
    this.setData({ showInfoModal: true });
  },

  hideInfoModal() {
    this.setData({ showInfoModal: false });
  },

  showChangePassword() {
    this.setData({
      showPasswordModal: true,
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
  },

  hidePasswordModal() {
    this.setData({ showPasswordModal: false });
  },

  onOldPasswordInput(e) {
    this.setData({ oldPassword: e.detail.value });
  },

  onNewPasswordInput(e) {
    this.setData({ newPassword: e.detail.value });
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value });
  },

  submitChangePassword() {
    const { oldPassword, newPassword, confirmPassword } = this.data;

    if (!oldPassword || !newPassword || !confirmPassword) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }

    if (newPassword.length < 6 || newPassword.length > 20) {
      wx.showToast({ title: '新密码需6-20位', icon: 'none' });
      return;
    }

    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    wx.request({
      url: `${app.globalData.baseUrl}/api/user/password`,
      method: 'PUT',
      header: {
        'Authorization': `Bearer ${app.globalData.accessToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        oldPassword,
        newPassword,
        confirmPassword
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data && res.data.code === 200) {
          wx.showToast({ title: '密码修改成功，请重新登录', icon: 'success' });
          this.setData({ showPasswordModal: false });
          setTimeout(() => {
            app.clearUserInfo();
            wx.redirectTo({ url: '/pages/login/login' });
          }, 1500);
        } else {
          wx.showToast({ title: res.data.message || '修改失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },

  showNotificationSettings() {
    this.setData({ showNotifyModal: true });
  },

  hideNotifyModal() {
    this.setData({ showNotifyModal: false });
  },

  updateNotificationSettings() {
    wx.request({
      url: `${app.globalData.baseUrl}/api/user/notification-settings`,
      method: 'PUT',
      header: {
        'Authorization': `Bearer ${app.globalData.accessToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        smsEnabled: this.data.smsEnabled,
        pushEnabled: this.data.pushEnabled
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          wx.showToast({ title: '设置已保存', icon: 'success' });
        }
      },
      fail: () => {
        wx.showToast({ title: '保存失败', icon: 'none' });
      }
    });
  },

  onSmsSwitch(e) {
    this.setData({ smsEnabled: e.detail.value }, () => {
      this.updateNotificationSettings();
    });
  },

  onPushSwitch(e) {
    this.setData({ pushEnabled: e.detail.value }, () => {
      this.updateNotificationSettings();
    });
  },

  goToFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback'
    });
  },

  onLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '退出中...' });
          app.logout().then(() => {
            wx.hideLoading();
            wx.showToast({ title: '退出成功', icon: 'success' });
            setTimeout(() => {
              wx.redirectTo({ url: '/pages/login/login' });
            }, 1500);
          }).catch(() => {
            wx.hideLoading();
            wx.redirectTo({ url: '/pages/login/login' });
          });
        }
      }
    });
  }
});