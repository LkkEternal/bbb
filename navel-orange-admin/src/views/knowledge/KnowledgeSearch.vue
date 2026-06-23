<template>
  <div class="page-container">
    <div class="page-header">
      <h2>知识检索</h2>
    </div>

    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="输入关键词搜索知识库文档..."
        size="large"
        clearable
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" :loading="searching">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div class="results-section" v-if="searchResults.length > 0">
      <h3>搜索结果</h3>
      <div class="result-list">
        <el-card
          v-for="(item, index) in searchResults"
          :key="index"
          class="result-card"
        >
          <div class="result-header">
            <span class="result-title">{{ item.documentTitle }}</span>
            <div class="result-score">
              <span class="score-label">相关度</span>
              <el-progress
                :percentage="Math.round(item.score * 100)"
                :stroke-width="8"
                :color="getScoreColor(item.score)"
                style="width: 120px"
              />
            </div>
          </div>
          <div class="result-snippet">{{ item.content }}</div>
        </el-card>
      </div>
    </div>

    <el-divider />

    <div class="qa-section">
      <h3>智能问答</h3>
      <div class="qa-input">
        <el-input
          v-model="question"
          type="textarea"
          :rows="3"
          placeholder="输入关于脐橙病虫害的问题..."
          resize="none"
        />
        <el-button
          type="primary"
          :loading="asking"
          :disabled="!question.trim()"
          @click="handleAsk"
          style="margin-top: 12px"
        >
          提问
        </el-button>
      </div>

      <el-card v-if="answer" class="answer-card">
        <template #header>
          <div class="answer-header">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            <span>回答</span>
          </div>
        </template>
        <div class="answer-content">{{ answer.answer }}</div>
        <div class="answer-sources" v-if="answer.sources?.length">
          <span class="sources-label">参考来源:</span>
          <el-tag
            v-for="(source, i) in answer.sources"
            :key="i"
            size="small"
            type="info"
            class="source-tag"
          >
            {{ source }}
          </el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search, ChatDotRound } from '@element-plus/icons-vue'
import { searchKnowledge, askQuestion } from '@/api/modules/knowledge'

interface SearchResult {
  documentTitle: string
  content: string
  score: number
}

interface QaAnswer {
  answer: string
  sources: string[]
}

const searchQuery = ref('')
const searching = ref(false)
const searchResults = ref<SearchResult[]>([])

const question = ref('')
const asking = ref(false)
const answer = ref<QaAnswer | null>(null)

function getScoreColor(score: number) {
  if (score >= 0.8) return '#5B8C5A'
  if (score >= 0.5) return '#E8863A'
  return '#D64545'
}

async function handleSearch() {
  if (!searchQuery.value.trim()) return
  searching.value = true
  try {
    const data: any = await searchKnowledge(searchQuery.value)
    searchResults.value = data?.records ?? data ?? []
  } catch {
  } finally {
    searching.value = false
  }
}

async function handleAsk() {
  if (!question.value.trim()) return
  asking.value = true
  answer.value = null
  try {
    const data: any = await askQuestion(question.value)
    answer.value = data
  } catch {
  } finally {
    asking.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

h3 {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.search-section {
  max-width: 800px;
  margin-bottom: 32px;
}

.results-section {
  margin-bottom: 24px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;

  .result-title {
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
  }

  .result-score {
    display: flex;
    align-items: center;
    gap: 8px;

    .score-label {
      font-size: 12px;
      color: $text-placeholder;
    }
  }
}

.result-snippet {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
}

.qa-section {
  max-width: 800px;
}

.qa-input {
  margin-bottom: 20px;
}

.answer-card {
  margin-top: 16px;
}

.answer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
}

.answer-content {
  font-size: 14px;
  color: $text-primary;
  line-height: 1.8;
  white-space: pre-wrap;
}

.answer-sources {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid $divider;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;

  .sources-label {
    font-size: 13px;
    color: $text-placeholder;
  }

  .source-tag {
    cursor: default;
  }
}
</style>
