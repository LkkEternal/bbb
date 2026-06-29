/**
 * qa.js - 智能问答页面逻辑
 * 作用：实现图文求索（图片识别）和在线问答功能
 */

const app = getApp();

Page({
  data: {
    // 当前选项卡：image-图文求索，text-在线问答
    currentTab: 'image',
    
    // 输入框内容
    inputText: '',
    
    // 图文求索页面的AI回答
    aiAnswer: '',
    
    // 对话消息列表（在线问答页面）
    chatMessages: [
      {
        id: 1,
        type: 'ai',
        content: '您好！我是脐橙病虫害智能助手，请问有什么可以帮助您的？',
        time: '刚刚'
      }
    ]
  },

  onLoad() {
    // 页面加载时初始化数据
  },

  /**
   * 切换选项卡
   */
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      currentTab: tab,
      aiAnswer: '' // 切换选项卡时清空回答
    });
  },

  /**
   * 选择图片上传
   */
  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];
        console.log('选择图片:', tempFilePath);
        
        // 显示加载提示
        wx.showLoading({
          title: '识别中...'
        });
        
        // 调用真实识别接口（如果有的话）
        // 目前没有真实接口，显示提示
        setTimeout(() => {
          wx.hideLoading();
          wx.showToast({
            title: '图片识别功能开发中',
            icon: 'none'
          });
        }, 1000);
      },
      fail: (err) => {
        console.error('选择图片失败:', err);
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 处理图片识别结果
   */
  handleImageRecognition(imagePath) {
    // 没有真实接口，显示提示
    wx.showToast({
      title: '图片识别功能开发中',
      icon: 'none'
    });
  },

  /**
   * 输入框输入事件
   */
  onInput(e) {
    this.setData({
      inputText: e.detail.value
    });
  },

  /**
   * 发送消息
   */
  sendMessage() {
    const inputText = this.data.inputText.trim();
    
    if (!inputText) {
      wx.showToast({
        title: '请输入问题',
        icon: 'none'
      });
      return;
    }
    
    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: inputText,
      time: '刚刚'
    };
    
    this.setData({
      chatMessages: this.data.chatMessages.concat(userMessage),
      inputText: ''
    });
    
    // 显示加载提示
    wx.showLoading({
      title: '思考中...',
      mask: true
    });
    
    // 调用AI问答接口
    this.callAIAPI(inputText);
  },

  /**
   * 调用AI问答接口
   */
  callAIAPI(question) {
    const accessToken = app.globalData.accessToken;
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/knowledge/qa`,
      method: 'POST',
      data: {
        question: question,
        orchardId: app.globalData.currentOrchardId || null
      },
      header: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading();
        console.log('AI回答:', res.data);
        
        if (res.data && res.data.code === 200 && res.data.data) {
          const answer = res.data.data.answer || res.data.data.content || '暂无相关信息';
          
          const aiMessage = {
            id: Date.now() + 1,
            type: 'ai',
            content: answer,
            time: '刚刚'
          };
          
          this.setData({
            chatMessages: this.data.chatMessages.concat(aiMessage)
          });
        } else {
          // 使用默认回答
          this.showDefaultAnswer(question);
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('AI问答接口调用失败:', err);
        // 使用默认回答
        this.showDefaultAnswer(question);
      }
    });
  },

  /**
   * 显示默认回答（接口失败时使用）
   */
  showDefaultAnswer(question) {
    const aiMessage = {
      id: Date.now() + 1,
      type: 'ai',
      content: '抱歉，暂时无法回答您的问题，请稍后再试。',
      time: '刚刚'
    };
    
    this.setData({
      chatMessages: this.data.chatMessages.concat(aiMessage)
    });
  },

  /**
   * 格式化时间
   */
  formatTime(dateStr) {
    if (!dateStr) return '未知时间';
    
    try {
      const date = new Date(dateStr.replace(' ', 'T'));
      const now = new Date();
      const diff = now.getTime() - date.getTime();
      const minutes = Math.floor(diff / 60000);
      const hours = Math.floor(diff / 3600000);
      const days = Math.floor(diff / 86400000);
      
      if (minutes < 1) return '刚刚';
      if (minutes < 60) return `${minutes}分钟前`;
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      
      return `${date.getMonth() + 1}/${date.getDate()}`;
    } catch (e) {
      return dateStr;
    }
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  }
});