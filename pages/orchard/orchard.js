/**
 * orchard.js - 果园列表页逻辑
 * 作用：展示用户的果园列表，点击进入果园详情，支持创建果园
 */

const app = getApp();

Page({
  data: {
    orchardList: [],
    loading: true,
    // 创建果园相关
    showCreateModal: false,
    orchardName: '',
    address: '',
    area: '',
    treeAge: '',
    variety: '纽荷尔（Newhall）',
    varietyOptions: ['纽荷尔（Newhall）', '朋娜（Ponkan）', '华盛顿脐橙（Washington Navel）', '赣南早脐橙', '自定义'],
    varietyIndex: 0,
    isCustomVariety: false,
    customVariety: '',
    submitting: false
  },

  onLoad(options) {
    this.loadOrchardList();
  },

  onShow() {
    this.loadOrchardList();
  },

  /**
   * 加载果园列表 - 调用后端接口
   */
  loadOrchardList() {
    const accessToken = app.globalData.accessToken;
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/orchards`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          // 转换后端返回的数据格式
          const orchards = res.data.data.map(item => {
            // 根据角色类型获取角色名称和样式类
            let role = '查看';
            let roleClass = 'viewer';
            let bgGradient = 'linear-gradient(135deg, #86909C, #5E6B76)';
            
            if (item.roleType === 'owner') {
              role = '园主';
              roleClass = 'owner';
              bgGradient = 'linear-gradient(135deg, #5B8C5A, #3D6B3D)';
            } else if (item.roleType === 'manager') {
              role = '管理';
              roleClass = 'manager';
              bgGradient = 'linear-gradient(135deg, #FFA940, #FF7D00)';
            }
            
            return {
              id: item.id,
              name: item.name || '未知果园',
              location: item.formattedAddress || item.address || '地址未填写',
              area: item.areaMu ? item.areaMu.toString() : (item.area ? item.area.toString() : '0'),
              age: item.treeAge ? item.treeAge.toString() : '0',
              devices: item.deviceCount ? item.deviceCount.toString() : '0',
              role: role,
              roleClass: roleClass,
              bgGradient: bgGradient
            };
          });
          
          this.setData({
            orchardList: orchards,
            loading: false
          });
        } else {
          console.error('获取果园列表失败:', res.data);
          this.setData({ loading: false });
        }
      },
      fail: (err) => {
        console.error('获取果园列表接口调用失败:', err);
        this.setData({ loading: false });
      }
    });
  },

  /**
   * 跳转到果园详情页
   */
  goToOrchardDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/orchard-detail/orchard-detail?id=${id}`
    });
  },

  /**
   * 显示创建果园弹窗
   */
  showCreate() {
    this.setData({
      showCreateModal: true,
      orchardName: '',
      address: '',
      area: '',
      treeAge: '',
      variety: '纽荷尔（Newhall）',
      varietyIndex: 0,
      isCustomVariety: false,
      customVariety: ''
    });
  },

  /**
   * 隐藏创建果园弹窗
   */
  hideCreate() {
    this.setData({ showCreateModal: false });
  },

  /**
   * 阻止事件冒泡
   */
  stopPropagation() {
    // 阻止点击弹窗内容时关闭弹窗
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
          
          // 关闭弹窗并刷新列表
          this.setData({ showCreateModal: false });
          this.loadOrchardList();
        } else {
          wx.showToast({
            title: res.data.message || '创建失败',
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
  }
});