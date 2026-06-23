const api = require('../../utils/request')
Page({
  data: {
    keyword: '',
    question: '',
    searchResults: [],
    qaResult: null,
    searching: false,
    asking: false,
    activeTab: 'search',
  },
  onInput(e) { this.setData({ keyword: e.detail.value }) },
  onQuestionInput(e) { this.setData({ question: e.detail.value }) },
  switchTab(e) { this.setData({ activeTab: e.currentTarget.dataset.tab }) },

  async doSearch() {
    if (!this.data.keyword.trim()) return
    this.setData({ searching: true, qaResult: null })
    try {
      const data = await api.post('/knowledge/search', { query: this.data.keyword })
      this.setData({ searchResults: data.results || [] })
    } catch (e) {}
    this.setData({ searching: false })
  },

  async doAsk() {
    if (!this.data.question.trim()) return
    this.setData({ asking: true, searchResults: [] })
    try {
      const data = await api.post('/knowledge/qa', { question: this.data.question })
      this.setData({ qaResult: data })
    } catch (e) {}
    this.setData({ asking: false })
  },
})
