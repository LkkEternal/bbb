/**
 * orchard-detail.js - 果园详情页逻辑
 * 作用：展示果园详细信息，包括设备管理、环境数据图表、病害历史和成员信息
 */

const app = getApp();

Page({
  data: {
    orchardId: null,
    orchard: {},
    location: '',
    deviceStats: {
      online: 0,
      offline: 0,
      total: 0
    },
    members: [],
    devices: [],
    detectionStats: {
      totalCount: 0,
      alertCount: 0,
      diseaseDistribution: []
    },
    detections: [],
    detectionImageUrls: [],
    detectionPage: 1,
    detectionSize: 5,
    hasMoreDetections: true,
    colors: ['#ff4d4f', '#ff7d00', '#faad14', '#52c41a', '#165dff', '#722ed1', '#13c2c2', '#eb2f96'],
    loading: true
  },

  onLoad(options) {
    const id = options.id;
    if (id) {
      this.setData({ orchardId: id });
      this.loadOrchardDetail(id);
      // 环境趋势和识别统计会在 loadOrchardDetail 中调用
    }
  },

  /**
   * 加载果园详情 - 调用后端接口
   */
  loadOrchardDetail(id) {
    const accessToken = app.globalData.accessToken;
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/orchards/${id}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          let location = data.location || '';
          
          // 解析 POINT 格式的经纬度
          if (location && location.includes('POINT')) {
            const match = location.match(/POINT\(([\d.]+)\s+([\d.]+)\)/);
            if (match) {
              location = `${match[1]},${match[2]}`;
            }
          }
          
          console.log('果园详情数据:', data);
          console.log('果园经纬度:', location);
          
          console.log('设备原始数据:', data.devices);
          // 处理设备数据
          const devices = (data.devices || []).map(device => ({
            id: device.id,
            deviceCode: device.deviceCode || device.device_code,
            name: device.name || device.deviceCode || '未命名设备',
            type: device.type || 'sensor',
            status: device.status,
            isCamera: (device.type || '').toLowerCase() === 'camera'
          }));
          console.log('映射后设备数据:', JSON.stringify(devices));
          
          const onlineCount = devices.filter(d => d.status === 'online' || d.status === 1).length;
          
          // 处理成员数据
          console.log('成员原始数据:', data.members);
          const members = (data.members || []).map(member => {
            const roleType = (member.roleType || member.role || member.role_type || 'viewer').toLowerCase();
            const roleName = roleType === 'owner' || roleType === '园主' ? '园主'
              : roleType === 'manager' || roleType === '管理员' || roleType === '管理' ? '管理员'
              : roleType === 'viewer' || roleType === '查看者' || roleType === '查看' ? '只读者'
              : roleType;
            return {
              userId: member.userId || member.id || member.user_id,
              username: member.username || member.name || member.userName || '未知',
              phone: member.phone || member.phoneNumber || '未绑定手机',
              roleType,
              roleName
            };
          });
          console.log('映射后成员:', JSON.stringify(members));
          
          this.setData({
            orchard: {
              id: data.id,
              name: data.name,
              address: data.formattedAddress || data.address,
              areaMu: data.areaMu || data.area_mu || data.area,
              treeAge: data.treeAge || data.tree_age,
              variety: data.variety
            },
            location: location,
            members: members,
            devices: devices,
            deviceStats: {
              online: onlineCount,
              offline: devices.length - onlineCount,
              total: devices.length
            },
            loading: false
          });
          
          // 加载识别统计数据和识别记录
          this.loadDetectionStats();
          this.loadDetections();
        } else {
          console.error('获取果园详情失败:', res.data);
          this.setData({
            orchard: {},
            members: [],
            devices: [],
            deviceStats: { online: 0, offline: 0, total: 0 },
            loading: false
          });
        }
      },
      fail: (err) => {
        console.error('获取果园详情接口调用失败:', err);
        this.setData({
          orchard: {},
          members: [],
          devices: [],
          deviceStats: { online: 0, offline: 0, total: 0 },
          loading: false
        });
      }
    });
  },

  /**
   * 加载识别记录 - 调用识别记录接口
   */
  loadDetections() {
    const accessToken = app.globalData.accessToken;
    const orchardId = this.data.orchardId;
    const page = this.data.detectionPage;
    const size = this.data.detectionSize;
    
    console.log('调用识别记录接口，orchardId:', orchardId, 'page:', page, 'size:', size);
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/detections`,
      method: 'GET',
      data: {
        orchardId: orchardId,
        page: page,
        size: size
      },
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('识别记录接口响应:', res.data);
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          const records = data.records || [];
          const total = data.total || 0;
          
          // 格式化识别记录
          const formattedRecords = records.map(item => ({
            id: item.id,
            imageUrl: item.imageUrl || '',
            diseaseName: item.topClassName || '未知病害',
            confidencePercent: item.topProbability ? (item.topProbability * 100).toFixed(0) : 0,
            deviceName: item.deviceName || item.deviceCode || '未知设备',
            time: item.createdAt || ''
          }));
          
          // 合并数据
          const existingDetections = this.data.detections;
          const newDetections = page === 1 ? formattedRecords : existingDetections.concat(formattedRecords);
          
          // 提取图片URL用于预览
          const imageUrls = newDetections.map(item => item.imageUrl).filter(url => url);
          
          this.setData({
            detections: newDetections,
            detectionImageUrls: imageUrls,
            hasMoreDetections: newDetections.length < total
          });
        } else {
          console.log('识别记录数据为空');
          if (page === 1) {
            this.setData({
              detections: [],
              detectionImageUrls: [],
              hasMoreDetections: false
            });
          }
        }
      },
      fail: (err) => {
        console.error('获取识别记录接口调用失败:', err);
        if (page === 1) {
          this.setData({
            detections: [],
            detectionImageUrls: [],
            hasMoreDetections: false
          });
        }
      }
    });
  },

  /**
   * 加载更多识别记录
   */
  loadMoreDetections() {
    this.setData({
      detectionPage: this.data.detectionPage + 1
    });
    this.loadDetections();
  },

  /**
   * 预览图片
   */
  previewImage(e) {
    const currentUrl = e.currentTarget.dataset.url;
    const urls = e.currentTarget.dataset.urls || [currentUrl];
    
    wx.previewImage({
      current: currentUrl,
      urls: urls
    });
  },

  /**
   * 加载识别统计数据 - 调用识别统计接口
   */
  loadDetectionStats() {
    const accessToken = app.globalData.accessToken;
    const orchardId = this.data.orchardId;
    
    // 计算日期范围：近5天
    const today = new Date();
    const endDate = today.toISOString().split('T')[0]; // 格式：yyyy-MM-dd
    
    const startDateObj = new Date(today);
    startDateObj.setDate(startDateObj.getDate() - 4); // 5天前
    const startDate = startDateObj.toISOString().split('T')[0];
    
    console.log('调用识别统计接口，orchardId:', orchardId, 'startDate:', startDate, 'endDate:', endDate);
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/detections/stats`,
      method: 'GET',
      data: {
        orchardId: orchardId,
        startDate: startDate,
        endDate: endDate
      },
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('识别统计接口响应:', res.data);
        if (res.data && res.data.code === 200 && res.data.data) {
          const data = res.data.data;
          this.setData({
            detectionStats: {
              totalCount: data.totalCount || 0,
              alertCount: data.alertCount || 0,
              diseaseDistribution: data.diseaseDistribution || []
            }
          });
        } else {
          console.log('识别统计数据为空');
          this.setData({
            detectionStats: {
              totalCount: 0,
              alertCount: 0,
              diseaseDistribution: []
            }
          });
        }
      },
      fail: (err) => {
        console.error('获取识别统计接口调用失败:', err);
        this.setData({
          detectionStats: {
            totalCount: 0,
            alertCount: 0,
            diseaseDistribution: []
          }
        });
      }
    });
  },

  /**
   * 格式化时间显示
   */
  formatTime(dateStr) {
    if (!dateStr) return '刚刚';
    
    try {
      const date = new Date(dateStr.replace(' ', 'T'));
      const now = new Date();
      const diff = now.getTime() - date.getTime();
      const minutes = Math.floor(diff / 60000);
      
      if (minutes < 1) return '刚刚';
      if (minutes < 60) return `${minutes}分钟前`;
      if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`;
      return `${Math.floor(minutes / 1440)}天前`;
    } catch (e) {
      console.error('时间格式化失败:', e);
      return dateStr;
    }
  },

  /**
   * 跳转到视频监控页面
   */
  goVideoMonitor(e) {
    const deviceId = e.currentTarget.dataset.deviceId;
    wx.navigateTo({
      url: `/pages/video-monitor/video-monitor?deviceId=${deviceId}&orchardId=${this.data.orchardId}`
    });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  /**
   * 编辑果园
   */
  editOrchard() {
    wx.showToast({
      title: '编辑功能开发中',
      icon: 'none'
    });
  }
});