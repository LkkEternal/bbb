/**
 * welcome.js - 欢迎引导页面逻辑
 * 作用：新用户注册后引导创建果园，提供新手指南
 */

const app = getApp();

Page({
  data: {
    // 当前步骤：guide-新手指南, create-创建果园
    currentStep: 'guide',
    
    // 来源：register-注册, login-登录（未创建果园）
    from: 'register',
    
    // 是否允许跳过（从登录跳转过来时不允许跳过）
    canSkip: true,
    
    // 欢迎提示文案
    welcomeTitle: '欢迎使用智能脐橙监测平台',
    welcomeDesc: '本平台帮助您实时监测脐橙病虫害，让种植更智慧、更简单。',
    
    // 新手指南步骤
    guideStep: 0,
    guideList: [
      {
        icon: 'gem-o',
        title: '欢迎使用智能脐橙监测平台',
        desc: '本平台帮助您实时监测脐橙病虫害，让种植更智慧、更简单。'
      },
      {
        icon: 'phone-o',
        title: '智能识别病虫害',
        desc: '通过边缘设备拍照，AI自动识别14类病虫害，及时发现潜在风险。'
      },
      {
        icon: 'fire-o',
        title: '环境数据监测',
        desc: '实时监测果园温湿度、土壤等环境数据，结合天气预报提供预警。'
      },
      {
        icon: 'bell',
        title: '智能预警推送',
        desc: '当检测到病虫害风险时，系统会自动推送预警消息，让您及时处理。'
      },
      {
        icon: 'notes-o',
        title: '知识库与问答',
        desc: '丰富的病虫害防治知识库，AI智能问答为您提供专业建议。'
      }
    ],
    
    // 创建果园表单
    orchardName: '',
    address: '',
    area: '',
    treeAge: '',
    variety: '纽荷尔（Newhall）',
    
    // 品种选项
    varietyOptions: ['纽荷尔（Newhall）', '朋娜（Ponkan）', '华盛顿脐橙（Washington Navel）', '赣南早脐橙', '自定义'],
    varietyIndex: 0,
    
    // 是否自定义品种
    isCustomVariety: false,
    customVariety: '',
    
    // 提交状态
    submitting: false
  },

  onLoad(options) {
    // 检查是否是从注册页面跳转过来
    const from = options.from || 'register';
    
    // 根据来源设置不同的提示文案
    let welcomeTitle = '欢迎使用智能脐橙监测平台';
    let welcomeDesc = '本平台帮助您实时监测脐橙病虫害，让种植更智慧、更简单。';
    
    if (from === 'login') {
      welcomeTitle = '欢迎回来';
      welcomeDesc = '您还没有创建果园，请先创建果园开始使用。';
    }
    
    this.setData({ 
      from: from,
      welcomeTitle: welcomeTitle,
      welcomeDesc: welcomeDesc,
      // 从登录跳转过来时不允许跳过，必须创建果园
      canSkip: from !== 'login'
    });
  },

  /**
   * 下一步指南
   */
  nextGuide() {
    const { guideStep, guideList } = this.data;
    if (guideStep < guideList.length - 1) {
      this.setData({ guideStep: guideStep + 1 });
    } else {
      // 指南完成，进入创建果园步骤
      this.setData({ currentStep: 'create' });
    }
  },

  /**
   * 上一步指南
   */
  prevGuide() {
    const { guideStep } = this.data;
    if (guideStep > 0) {
      this.setData({ guideStep: guideStep - 1 });
    }
  },

  /**
   * 跳过指南
   */
  skipGuide() {
    if (!this.data.canSkip) {
      // 从登录跳转过来，不允许跳过，直接进入创建果园步骤
      this.setData({ currentStep: 'create' });
      return;
    }
    this.setData({ currentStep: 'create' });
  },

  /**
   * 输入果园名称
   */
  onNameInput(e) {
    this.setData({ orchardName: e.detail.value });
  },

  /**
   * 输入地址
   */
  onAddressInput(e) {
    this.setData({ address: e.detail.value });
  },

  /**
   * 输入面积
   */
  onAreaInput(e) {
    this.setData({ area: e.detail.value });
  },

  /**
   * 输入树龄
   */
  onTreeAgeInput(e) {
    this.setData({ treeAge: e.detail.value });
  },

  /**
   * 选择品种
   */
  onVarietyChange(e) {
    const index = e.detail.value;
    const selectedVariety = this.data.varietyOptions[index];
    
    if (selectedVariety === '自定义') {
      this.setData({
        varietyIndex: index,
        isCustomVariety: true,
        variety: '',
        customVariety: ''
      });
    } else {
      this.setData({
        varietyIndex: index,
        isCustomVariety: false,
        variety: selectedVariety,
        customVariety: ''
      });
    }
  },

  /**
   * 输入自定义品种
   */
  onCustomVarietyInput(e) {
    this.setData({ 
      customVariety: e.detail.value,
      variety: e.detail.value
    });
  },

  /**
   * 提交创建果园
   */
  submitCreate() {
    const { orchardName, address, area, treeAge, variety, submitting } = this.data;
    
    // 防止重复提交
    if (submitting) return;
    
    // 验证果园名称
    if (!orchardName.trim()) {
      wx.showToast({ title: '请输入果园名称', icon: 'none' });
      return;
    }
    
    if (orchardName.length > 50) {
      wx.showToast({ title: '果园名称最长50字', icon: 'none' });
      return;
    }
    
    // 验证地址
    if (!address.trim()) {
      wx.showToast({ title: '请输入果园地址', icon: 'none' });
      return;
    }
    
    // 构建请求数据
    const data = {
      name: orchardName.trim(),
      formattedAddress: address.trim()
    };
    
    // 可选字段
    if (area && !isNaN(parseFloat(area))) {
      data.areaMu = parseFloat(area);
    }
    if (treeAge && !isNaN(parseInt(treeAge))) {
      data.treeAge = parseInt(treeAge);
    }
    if (variety) {
      data.variety = variety;
    }
    
    this.setData({ submitting: true });
    wx.showLoading({ title: '创建中...' });
    
    const accessToken = app.globalData.accessToken;
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/orchards`,
      method: 'POST',
      data: data,
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data && res.data.code === 200) {
          wx.showToast({ title: '创建成功', icon: 'success' });
          
          // 跳转到首页
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/index/index' });
          }, 1500);
        } else {
          const errorMessages = {
            2001: '果园不存在',
            2002: '无权操作该果园'
          };
          wx.showToast({
            title: errorMessages[res.data.code] || res.data.message || '创建失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      },
      complete: () => {
        this.setData({ submitting: false });
      }
    });
  },

  /**
   * 跳过创建，直接进入首页（仅从注册跳转时可用）
   */
  skipCreate() {
    if (!this.data.canSkip) {
      wx.showToast({
        title: '请先创建果园',
        icon: 'none'
      });
      return;
    }
    
    wx.showModal({
      title: '提示',
      content: '跳过创建果园后，您可以在"我的果园"页面随时创建。确定跳过吗？',
      success: (res) => {
        if (res.confirm) {
          wx.redirectTo({ url: '/pages/index/index' });
        }
      }
    });
  },

  /**
   * 返回上一步
   */
  backToGuide() {
    this.setData({ currentStep: 'guide', guideStep: this.data.guideList.length - 1 });
  }
});