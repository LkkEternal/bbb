/**
 * qa.js - 智能问答页面逻辑
 * 支持：Markdown 渲染、流式输出效果、LLM 无关问题判断
 */

const app = getApp();

// ── 专业问题示例（每次随机展示） ──
const EXAMPLE_QUESTIONS = [
  '柑橘溃疡病有什么症状？',
  '脐橙黄龙病怎么检测？',
  '红蜘蛛用什么药防治？',
  '脐橙炭疽病的防治方法',
  '6月份脐橙果园要做什么农事？',
  '如何判断柑橘是否缺硼？',
  '柑橘木虱怎么防治？',
  '脐橙潜叶蛾的危害症状',
  '柑橘疮痂病和溃疡病怎么区分？',
  '脐橙树势弱是什么原因？',
  '柑橘天牛怎么防治？',
  '脐橙果实发黄是什么问题？',
  '柑橘介壳虫用什么药剂？',
  '脐橙春季修剪要点',
  '柑橘锈壁虱的最佳防治时期'
];

Page({
  data: {
    currentTab: 'text',
    inputText: '',
    aiAnswer: '',
    chatMessages: [],
    isWaiting: false
  },

  onLoad() {
    this.setData({
      chatMessages: [{
        id: 1,
        type: 'ai',
        content: '您好！我是脐橙病虫害智能助手，请问有什么可以帮助您的？',
        time: '刚刚'
      }]
    });
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ currentTab: tab, aiAnswer: '' });
  },

  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: () => {
        wx.showToast({ title: '图片识别功能开发中', icon: 'none' });
      }
    });
  },

  onInput(e) {
    this.setData({ inputText: e.detail.value });
  },

  sendMessage() {
    const text = this.data.inputText.trim();
    if (!text) {
      wx.showToast({ title: '请输入问题', icon: 'none' });
      return;
    }

    const userMsg = { id: Date.now(), type: 'user', content: text, time: '刚刚' };
    const msgs = [...this.data.chatMessages, userMsg];
    this.setData({ chatMessages: msgs, inputText: '', isWaiting: true });

    // 添加"正在输入"占位
    const typingId = Date.now() + 9999;
    this.setData({
      chatMessages: [...msgs, { id: typingId, type: 'ai', content: '...', isTyping: true }]
    });

    this.callAPI(text, typingId);
  },

  /** 判断是否柑橘/农业相关问题（关键词匹配） */
  isCitrusQuestion(text) {
    var keywords = [
      '脐橙','柑橘','桔子','橙子','柑桔','柚子','柠檬',
      '溃疡病','黄龙病','炭疽病','疮痂病','树脂病','沙皮病','脚腐病','裙腐病',
      '红蜘蛛','锈壁虱','潜叶蛾','蚜虫','介壳虫','天牛','木虱',
      '病害','虫害','防治','农药','杀菌','杀虫','施肥','修剪',
      '叶片','果实','根部','枝干','树势','果园','果树','种植','栽培',
      '缺素','缺氮','缺磷','缺钾','缺硼','缺锌','缺铁','缺镁','缺钙',
      '肥料','营养','浇水','灌溉','排水','土壤','农事',
      '怎么','什么','如何','为什么','咋办','怎么办',
    ];
    for (var i = 0; i < keywords.length; i++) {
      if (text.indexOf(keywords[i]) !== -1) return true;
    }
    return false;
  },

  callAPI(question, typingId) {
    const token = app.globalData.accessToken;

    wx.request({
      url: `${app.globalData.baseUrl}/api/knowledge/qa`,
      method: 'POST',
      data: { question, orchardId: app.globalData.currentOrchardId || null },
      header: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
      success: (res) => {
        if (res.data && res.data.code === 200 && res.data.data) {
          const d = res.data.data;
          const refs = d.references || [];

          // ── 知识库有检索结果 → 正常回答 ──
          if (refs.length > 0 && d.answer && d.model !== 'none') {
            let answer = d.answer;
            answer += '\n\n---\n📚 **参考来源**\n';
            refs.forEach((r, i) => {
              answer += `- ${r.documentTitle}（相关度 ${(r.similarity * 100).toFixed(0)}%）\n`;
            });
            this.streamText(typingId, answer);
            return;
          }

          // ── 知识库没结果 → 展示后端返回的文案（如"暂未找到相关信息"） ──
          if (this.isCitrusQuestion(question)) {
            this.finishTyping(typingId, d.answer || '不知道');
          } else {
            const example = EXAMPLE_QUESTIONS[Math.floor(Math.random() * EXAMPLE_QUESTIONS.length)];
            const reminder = `👋 您好！我是专业的脐橙病虫害检测助手，专注于解决脐橙（柑橘）种植过程中的病虫害问题。\n\n您的问题不太像脐橙相关的内容，请尝试问我一些专业的问题吧！\n\n💡 例如：**${example}**`;
            this.finishTyping(typingId, reminder);
          }
        } else {
          this.finishTyping(typingId, '抱歉，暂时无法回答，请稍后再试。');
        }
      },
      fail: () => {
        this.finishTyping(typingId, '网络开小差了，请稍后再试。');
      }
    });
  },

  /** 流式逐字显示效果 */
  streamText(typingId, fullText) {
    let idx = 0;
    const chars = fullText;
    const step = 3; // 每次显示3个字，平衡速度和流畅度

    const timer = setInterval(() => {
      idx += step;
      if (idx >= chars.length) {
        idx = chars.length;
        clearInterval(timer);
        this.finishTyping(typingId, chars, false);
        return;
      }
      // 更新消息内容（已转为 Markdown HTML）
      const msgs = [...this.data.chatMessages];
      const msg = msgs.find(m => m.id === typingId);
      if (msg) {
        msg.content = chars.substring(0, idx);
        msg.htmlContent = this.mdToHtml(msg.content);
        this.setData({ chatMessages: msgs });
      }
    }, 30);
  },

  /** 结束打字状态 */
  finishTyping(typingId, content, removeTyping = true) {
    const msgs = [...this.data.chatMessages];
    const aiMsg = {
      id: Date.now(),
      type: 'ai',
      content: content,
      htmlContent: this.mdToHtml(content),
      time: '刚刚'
    };

    if (removeTyping) {
      // 删除占位，再插入新消息
      const idx = msgs.findIndex(m => m.id === typingId);
      if (idx >= 0) msgs.splice(idx, 1);
      msgs.push(aiMsg);
    } else {
      // 替换占位为最终消息
      const idx = msgs.findIndex(m => m.id === typingId);
      if (idx >= 0) msgs[idx] = aiMsg;
    }

    this.setData({ chatMessages: msgs, isWaiting: false });
  },

  /** Markdown → HTML（适配微信 rich-text） */
  mdToHtml(text) {
    if (!text) return '';
    let html = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    // 代码块
    html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre style="background:#f5f5f5;padding:10px;border-radius:8px;font-size:13px;overflow-x:auto;margin:8px 0;"><code>$2</code></pre>');
    // 行内代码
    html = html.replace(/`([^`]+)`/g, '<code style="background:#f0f0f0;padding:2px 6px;border-radius:4px;font-size:13px;color:#e74c3c;">$1</code>');
    // 加粗
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong style="color:#d35400;">$1</strong>');
    // 斜体
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    // 链接
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" style="color:#d35400;">$1</a>');
    // 标题
    html = html.replace(/^### (.+)$/gm, '<h4 style="margin:10px 0 4px;font-size:15px;color:#222;">$1</h4>');
    html = html.replace(/^## (.+)$/gm, '<h3 style="margin:10px 0 4px;font-size:16px;color:#222;">$1</h3>');
    html = html.replace(/^# (.+)$/gm, '<h2 style="margin:10px 0 4px;font-size:17px;color:#222;">$1</h2>');
    // 引用
    html = html.replace(/^&gt; (.+)$/gm, '<blockquote style="border-left:3px solid #d35400;padding-left:12px;color:#666;margin:8px 0;">$1</blockquote>');
    // 无序列表
    html = html.replace(/^- (.+)$/gm, '<li style="margin:2px 0;">$1</li>');
    html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul style="margin:4px 0;padding-left:20px;">$&</ul>');
    // 有序列表
    html = html.replace(/^\d+\. (.+)$/gm, '<li style="margin:2px 0;">$1</li>');
    html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ol style="margin:4px 0;padding-left:20px;">$&</ol>');
    // 分隔线
    html = html.replace(/^---$/gm, '<hr style="border:none;border-top:1px solid #ddd;margin:12px 0;">');
    // 段落包裹
    const lines = html.split('\n');
    let result = '';
    let inBlock = false;
    for (const line of lines) {
      const t = line.trim();
      if (!t) { if (inBlock) { result += '</p>'; inBlock = false; } continue; }
      if (t.startsWith('<pre') || t.startsWith('<ul') || t.startsWith('<ol') ||
          t.startsWith('<blockquote') || t.startsWith('<h') || t.startsWith('<hr') ||
          t.startsWith('<li') || t.startsWith('</')) {
        result += t + '\n';
        continue;
      }
      if (!inBlock) { result += '<p style="margin:6px 0;line-height:1.7;">'; inBlock = true; }
      result += t + ' ';
    }
    if (inBlock) result += '</p>';
    return result;
  },

  /** 格式化时间 */
  formatTime(dateStr) {
    if (!dateStr) return '未知时间';
    try {
      const d = new Date(dateStr.replace(' ', 'T'));
      const n = new Date();
      const m = Math.floor((n - d) / 60000);
      if (m < 1) return '刚刚';
      if (m < 60) return `${m}分钟前`;
      if (m < 1440) return `${Math.floor(m / 60)}小时前`;
      return `${d.getMonth() + 1}/${d.getDate()}`;
    } catch (e) { return dateStr; }
  },

  goBack() { wx.navigateBack(); }
});
