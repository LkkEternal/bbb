Component({
  properties: {
    currentPage: {
      type: String,
      value: ''
    }
  },

  data: {
    tabList: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        icon: '🏠'
      },
      {
        pagePath: '/pages/orchard/orchard',
        text: '果园',
        icon: '🌲'
      },
      {
        pagePath: '/pages/qa/qa',
        text: '问答',
        icon: '🤖'
      },
      {
        pagePath: '/pages/message/message',
        text: '消息',
        icon: '💬'
      },
      {
        pagePath: '/pages/profile/profile',
        text: '我的',
        icon: '👤'
      }
    ]
  },

  methods: {
    switchTab(e) {
      const path = e.currentTarget.dataset.path;
      if (path === this.properties.currentPage) {
        return;
      }
      wx.redirectTo({
        url: path
      });
    }
  }
});