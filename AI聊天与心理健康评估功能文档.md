# AI聊天 + 心理健康评估 — 功能设计文档

> 版本：v2.1
>
> 更新日期：2026-07-30
>
> 状态：评估模块已完成，闲聊模块待实现

---

## 一、功能概述

| 模块 | 说明 | 状态 |
|---|---|---|
| **心理健康评估** | 管理员上传 DOCX → AI 解析入库 → 用户选问卷 → 逐题作答 → 匹配评分规则 → 出等级+建议 | ✅ 已完成 |
| **AI 闲聊** | 心理陪伴助手，像朋友一样聊天。每日对话存 Redis，23:59 智谱总结写入 DB，次日作为上下文 | 🔧 待实现 |

---

## 二、技术架构

```
┌─────────────────────────────────┐
│  Vue3 前端                       │
│  Chat.vue: [评估] / [闲聊] 切换  │
└────────────┬────────────────────┘
             │ REST API (JWT)
┌────────────▼────────────────────┐
│  Spring Boot 后端                │
│  emotion-chat 模块               │
│                                 │
│  评估: MySQL (questionnaire/     │
│        question/grade_rule/      │
│        record) + Redis 进度      │
│                                 │
│  闲聊: Redis (当日对话)          │
│        MySQL (每日摘要)          │
│        智谱 API (glm-4.7)       │
└────────────┬────────────────────┘
             │ HTTPS
┌────────────▼────────────────────┐
│  智谱 GLM-4 API                  │
│  base-url: open.bigmodel.cn     │
│  模型: glm-4.5-air(解析)        │
│        glm-4.7(闲聊)            │
└─────────────────────────────────┘
```

---

## 三、评估完整流程（上传 → 答题 → 反馈）

### 3.1 整体链路

```
管理员上传DOCX ──→ 智谱AI解析 ──→ 入库3张表 ──→ 用户选问卷
                                                  │
                                   ┌──────────────┴──────────────┐
                                   │  逐题作答（Redis 存分数）    │
                                   │  答完 → 显示预览 + 提交按钮  │
                                   │  点击提交 → 入库最终结果      │
                                   └─────────────────────────────┘
                                              │
                                    ┌─────────▼─────────┐
                                    │  显示总分+等级+建议  │
                                    │  写入评估历史        │
                                    └───────────────────┘
```

### 3.2 阶段一：管理员上传 DOCX 并解析入库

#### 步骤

| 步骤 | 调用方 | 动作 | 数据 |
|------|--------|------|------|
| 1 | 前端 `AssessmentAdmin.vue` | 用户点击「上传DOCX解析」→ 选择 `.docx` 文件 | 10MB 限制，仅 `.docx` |
| 2 | → `POST /api/assessment/document/parse` | 后端 `DocumentController.parseDocument()` | `MultipartFile` |
| 3 | → `DocumentParseServiceImpl.extractText()` | 魔数校验（OLE2 或非 ZIP 直接拒绝）→ `POI XWPFWordExtractor` 提取纯文本 | 文本字符串 |
| 4 | → `AiExtractServiceImpl.extractFromText()` | 组装 system prompt + 文档内容 → 调智谱 `glm-4.5-air`（`RestTemplate` 60s 连接 / 600s 读取） | `AiParseResultResponse` |
| 5 | 响应前端 | 返回 JSON（问卷名称、题目列表、评分规则） | 预览数据 |
| 6 | 前端 `AssessmentAdmin.vue` | 展示解析结果预览（名称+编码+描述+题数+规则数） | |
| 7 | 用户点击 **「确认入库」** | → `POST /api/assessment/document/confirm` | `AiParseResultResponse` |
| 8 | → `DocumentParseServiceImpl.saveParsedResult()` `@Transactional` | 1) 查重（code 唯一）→ 2) INSERT `assessment_questionnaire` → 3) 遍历 `questions[]` INSERT `assessment_question` → 4) 遍历 `gradeRules[]` INSERT `assessment_grade_rule` | 3 张表 |
| 9 | 前端刷新表格 | 新问卷出现在列表中（默认启用状态） | |

#### 智谱 Prompt 设计

```
你是一位心理量表文档结构化解析专家。你所接收的文档严格遵循以下结构：

- 首行为量表名称
- 接着一段描述文字
- 然后出现表格"题目列表"，包含列：题号、题目内容、所属维度、0分、1分、2分、3分
- 接着"总分范围"
- 然后"总分区间对应的状况"表格，包含分数区间和程度判定
- 最后"建议"部分，包含每个区间的建议文字

→ 返回严格 JSON: { name, code, description, questions[], gradeRules[] }
```

每个选项固定映射：optionA=0分, optionB=1分, optionC=2分, optionD=3分, scoreA=0, scoreB=1, scoreC=2, scoreD=3。

### 3.3 阶段二：用户答题（前端 Chat.vue）

#### 3.3.1 选问卷 → 开始

```
1. 用户进入 [知心助手] → 调用 GET /api/assessment/list
   → 返回已启用问卷列表 { name, code, description, totalQuestions }
2. 用户点击某问卷卡片 → 调用 POST /api/assessment/start
    请求: { questionnaireId }
    后端:
    a. 查询问卷 + 题目（**不操作 DB 写入**）
    b. 生成临时 recordId（Redis INCR `assessment:tempIdSeq`）
    c. 写 Redis Hash assessment:progress:{userId}
       { recordId, questionnaireId, currentIndex=0, totalScore=0, startedAt }
    d. 设置 TTL = 30 分钟
    响应: { recordId, currentQuestion, totalQuestions }
3. 前端进入 answering 状态 → 显示第 1 题（进度条 1/N）
```

#### 3.3.2 逐题作答 + Redis 暂存

```
1. 用户选择 A/B/C/D → 调用 POST /api/assessment/answer
   请求: { recordId, questionId, selectedOption }
   后端:
   a. 校验 Redis 存在 + recordId 匹配
   b. 根据 option 查 score（A→scoreA, B→scoreB, C→scoreC, D→scoreD）
   c. currentIndex + 1, totalScore += score
   d. 追加分数到 Redis scores 字符串（逗号分隔, 如 "0,2,1,3"）
   e. 更新 Redis: currentIndex, totalScore, scores; 刷新 TTL
   f. 判断是否最后一题
```

| 场景 | 响应 `finished` | 响应 `allAnswered` | 前端行为 |
|------|:-:|:-:|----------|
| 还有下一题 | `false` | `null` | 加载下一题，进度 +1 |
| 最后一题答完 | `null` | `true` | 进入 `allAnswered` 状态，显示分数等级预览 + **「提交结果」** 按钮 |

#### 3.3.3 回退（上一题）

```
1. 用户点击「← 上一题」→ 调用 POST /api/assessment/prev
   请求: { recordId }
   后端:
   a. currentIndex - 1
   b. 从 scores 字符串 pop 最后一位，totalScore 减去该分
   c. 更新 Redis
   响应: 上一题的 questionInfo + progress
```

#### 3.3.4 提交结果（最终入库）

```
1. 用户在 allAnswered 状态点击 **「提交结果」** → 调用 POST /api/assessment/submit
    请求: { recordId }
    后端 @Transactional:
    a. 从 Redis 读取: totalScore, questionnaireId, startedAt
    b. 查 grade_rule 匹配: WHERE questionnaireId=? AND totalScore BETWEEN minScore AND maxScore
    c. **INSERT assessment_record**:
       userId, questionnaireId, totalScore, levelCode, levelName, adviceText,
       status='completed', startedAt, completedAt=NOW()
       （由 MySQL AUTO_INCREMENT 生成新 ID）
    d. DEL assessment:progress:{userId}
    响应: { finished: true, result: { recordId(DB自增ID), totalScore, levelCode, levelName, advice, completedAt } }
2. 前端进入 result 状态 → 显示完整结果页
```

#### 3.3.5 异常路径

| 场景 | 处理 |
|------|------|
| 用户中途退出/超时 | Redis 30 分钟过期自动删除，**DB 中无任何记录** |
| 答完未提交就退出 | Redis 过期后无法提交，需重新开始，DB 中无任何记录 |
| 提交失败 | 前端提示错误，用户可重试 submit（Redis 数据仍在） |

### 3.4 阶段三：结果展示

#### 3.4.1 结果页面

```
┌────────────────────────────────────┐
│          📊 评估完成                │
│                                    │
│     总分                           │
│      21          中重度             │
│                                    │
│  ┌──── 建议 ────────────────────┐ │
│  │ 中度倾向（15-21分）...       │ │
│  └──────────────────────────────┘ │
│                                    │
│   [重新评估]    [查看历史]         │
└────────────────────────────────────┘
```

- 等级颜色（按 levelCode）: normal=绿, mild=黄, moderate=橙, severe=红, extremely_severe=深红
- 建议文本来自 `assessment_grade_rule.advice_text`（prompt 解析时从文档"建议"部分提取）
- 历史记录弹窗显示最近 20 条（recordId + 问卷名 + 总分 + 等级 + 完成时间 + 建议预览）

#### 3.4.2 历史记录

```
GET /api/assessment/history?page=1&size=20
→ 分页返回 { records: [{ recordId, questionnaireName, questionnaireCode,
    totalScore, levelCode, levelName, adviceText, completedAt }] }
```

---

## 四、数据库设计

### 4.1 评估问卷表 `assessment_questionnaire`

```sql
CREATE TABLE assessment_questionnaire (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    name             VARCHAR(100) NOT NULL,
    code             VARCHAR(50) NOT NULL UNIQUE,
    description      VARCHAR(500) DEFAULT NULL,
    total_questions  INT NOT NULL DEFAULT 0,
    is_active        TINYINT(1) NOT NULL DEFAULT 1,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='评估问卷定义表';
```

### 4.2 题目表 `assessment_question`

```sql
CREATE TABLE assessment_question (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    questionnaire_id  BIGINT NOT NULL,
    question_index    INT NOT NULL,
    question_text     VARCHAR(500) NOT NULL,
    option_a          VARCHAR(200) NOT NULL,
    option_b          VARCHAR(200) NOT NULL,
    option_c          VARCHAR(200) NOT NULL,
    option_d          VARCHAR(200) NOT NULL,
    score_a           INT NOT NULL DEFAULT 0,
    score_b           INT NOT NULL DEFAULT 1,
    score_c           INT NOT NULL DEFAULT 2,
    score_d           INT NOT NULL DEFAULT 3,
    is_active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id) ON DELETE CASCADE
) COMMENT='问卷题目表';
```

### 4.3 评分规则表 `assessment_grade_rule`

```sql
CREATE TABLE assessment_grade_rule (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    questionnaire_id  BIGINT NOT NULL,
    min_score         INT NOT NULL,
    max_score         INT NOT NULL,
    level_code        VARCHAR(30) NOT NULL,
    level_name        VARCHAR(50) NOT NULL,
    advice_text       TEXT NOT NULL,
    sort_order        INT NOT NULL DEFAULT 0,
    is_active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id) ON DELETE CASCADE
) COMMENT='评分等级规则表';
```

### 4.4 评估记录表 `assessment_record`

> 仅在用户完成答题并点击「提交结果」后写入，中途退出不产生任何记录。

```sql
CREATE TABLE assessment_record (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT NOT NULL,
    questionnaire_id  BIGINT NOT NULL,
    total_score       INT NOT NULL DEFAULT 0,
    level_code        VARCHAR(30) DEFAULT NULL,
    level_name        VARCHAR(50) DEFAULT NULL,
    advice_text       TEXT,
    status            VARCHAR(20) NOT NULL DEFAULT 'in_progress',
    started_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at      DATETIME DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (questionnaire_id) REFERENCES assessment_questionnaire(id)
) COMMENT='用户评估记录表';
```

### 4.5 闲聊日摘要表 `chat_daily_summary`

```sql
CREATE TABLE chat_daily_summary (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    summary_date    DATE NOT NULL,
    summary         TEXT NOT NULL COMMENT '当日对话摘要（2-3句）',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, summary_date),
    INDEX idx_cleanup (summary_date)
) COMMENT='闲聊每日摘要表';
```

---

## 五、Redis 存储设计

### 5.1 闲聊记录

- **Key**: `chat:daily:{userId}:{yyyy-MM-dd}`
- **类型**: List
- **元素 JSON**:
  ```json
  { "role": "user", "content": "..." }
  { "role": "assistant", "content": "..." }
  ```
- **TTL**: 3 天（安全网，定时任务总结后即删）
- **读取**: LRANGE 取全部作为智谱上下文

### 5.2 评估进度

- **Key**: `assessment:progress:{userId}`
- **类型**: Hash
- **字段**: 
  | 字段 | 类型 | 说明 |
  |------|------|------|
  | `recordId` | String | DB 记录主键 |
  | `questionnaireId` | String | 问卷 ID |
  | `currentIndex` | String | 当前已答题数（0-based） |
  | `totalScore` | String | 累计总分 |
  | `scores` | String | 每题分数，逗号分隔，如 `"0,2,1,3"`，用于回退时 pop 最后一位 |
- **TTL**: 30 分钟
- **生命周期**:
  ```
  /start  →  创建 Hash + 30min TTL（不操作 DB）
  /answer →  更新字段, 刷新 TTL
  /prev   →  回滚 totalScore, pop scores 末位, 刷新 TTL
  /submit →  读取各字段 → INSERT assessment_record → DEL key（完成）
  TTL 过期 →  自动删除（未完成的评估 DB 中无任何记录）
  ```

---

## 六、AI 闲聊完整流程

### 6.1 用户发消息 (`POST /api/chat/send`)

```
1. 前端: Chat.vue 发 { message: "今天心情不好" }
2. 后端 ChatController: 解析 userId
3. AiChatServiceImpl:
   a. 取昨日摘要 → SELECT summary FROM chat_daily_summary
      WHERE user_id=? AND summary_date=DATE_SUB(CURDATE(),1)
   b. 取今日 Redis 全部消息 → LRANGE chat:daily:{userId}:today 0 -1
   c. 组装智谱请求:
      messages: [
        { role: "system", content: "你是一个温暖的心理陪伴助手'暖暖'。
          以下是用户昨天的聊天摘要，供参考了解背景：{摘要}" },
        ...今日历史消息（最多20条）,
        { role: "user", content: "今天心情不好" }
      ]
   d. 调智谱 API → 得到回复
   e. 将本轮问答存入 Redis → RPUSH chat:daily:{userId}:today
      {"role":"user","content":"今天心情不好"}
      {"role":"assistant","content":"能跟我说说发生了什么吗？"}
   f. 返回 { reply: "..." }
```

### 6.2 定时任务 — 每日总结 (23:50)

```
1. 扫描所有今日有 Redis 数据的 userId
2. 对每个用户:
   a. LRANGE chat:daily:{userId}:today 0 -1 → 取全部
   b. 调智谱 API（model=glm-4.7）:
      "请用2-3句话总结今日对话：用户的主要情绪、关键话题、你的建议"
   c. INSERT INTO chat_daily_summary (user_id, summary_date, summary)
      VALUES (?, CURDATE(), 摘要)
      ON DUPLICATE KEY UPDATE summary=VALUES(summary)
   d. DEL chat:daily:{userId}:today
```

### 6.3 定时任务 — 清理前天数据 (00:30)

```
DELETE FROM chat_daily_summary
WHERE summary_date <= DATE_SUB(CURDATE(), INTERVAL 2 DAY)
```

---

## 七、接口设计

### 7.1 评估接口（已完成）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/assessment/list` | 获取启用的问卷列表 |
| POST | `/api/assessment/start` | 开始评估 |
| POST | `/api/assessment/answer` | 提交答案 / 下一题 |
| POST | `/api/assessment/prev` | 回退上一题（分数回滚） |
| POST | `/api/assessment/submit` | 提交结果（最终入库） |
| GET | `/api/assessment/history` | 评估历史 |
| GET | `/api/assessment/detail/{id}` | 评估详情 |
| POST | `/api/assessment/document/parse` | 上传 DOCX 解析 |
| POST | `/api/assessment/document/confirm` | 确认入库 |

### 7.2 管理接口（已完成）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/assessment/admin/questionnaires` | 分页查询所有问卷 |
| GET | `/api/assessment/admin/questionnaire/{id}` | 问卷详情（含题目+规则） |
| PUT | `/api/assessment/admin/questionnaire/{id}/toggle-active` | 启用/禁用 |

### 7.3 闲聊接口（待实现）

#### POST /api/chat/send

**请求：**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| message | string | 是 | 用户消息 |

**响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reply": "能跟我说说发生了什么吗？我在听..."
  }
}
```

#### GET /api/chat/history

**请求参数：** page, size

**响应：**

```json
{
  "code": 200,
  "data": {
    "total": 10,
    "messages": [
      { "role": "user", "content": "...", "createdAt": "..." },
      { "role": "assistant", "content": "...", "createdAt": "..." }
    ]
  }
}
```

---

## 八、智谱 API 调用

### 8.1 配置（两套模型）

```yaml
zhipu:
  api-key: ${ZHIPU_API_KEY}
  base-url: https://open.bigmodel.cn/api/paas/v4
  model: glm-4.5-air         # 文档解析（12M 免费额度）
  chat-model: glm-4.7         # 闲聊（5M 免费额度）
```

### 8.2 闲聊 System Prompt

```
你是一个温暖贴心的心理陪伴助手，名字叫"暖暖"。
你的职责是：
1. 倾听用户的倾诉，给予理解和共情
2. 用温和的语气引导用户表达情绪
3. 识别负面情绪，给予适当的安慰和建议
4. 不做医学诊断，可建议用户寻求专业帮助
5. 回复简洁自然，像朋友聊天一样（50-100字）
```

### 8.3 摘要 System Prompt

```
请用2-3句话总结以上对话内容，包含：
1. 用户今天的主要情绪状态
2. 聊到的关键话题
3. 你给出的核心建议
要求简洁明了，不要超过100字。
```

---

## 九、已实现文件清单

### 后端 emotion-chat 模块

| 层 | 文件 |
|---|---|
| Config | `ZhipuProperties.java` |
| Controller | `AssessmentController.java`, `AssessmentAdminController.java`, `DocumentController.java` |
| Service | `AssessmentService.java`, `AiExtractService.java`, `DocumentParseService.java` |
| Service Impl | `AssessmentServiceImpl.java`, `AiExtractServiceImpl.java`, `DocumentParseServiceImpl.java` |
| Mapper | `AssessmentQuestionnaireMapper.java`, `AssessmentQuestionMapper.java`, `AssessmentGradeRuleMapper.java`, `AssessmentRecordMapper.java` |
| Entity | `AssessmentQuestionnaire.java`, `AssessmentQuestion.java`, `AssessmentGradeRule.java`, `AssessmentRecord.java` |
| DTO Request | `AssessmentStartRequest.java`, `AssessmentAnswerRequest.java`, `AssessmentPrevRequest.java` |
| DTO Response | `QuestionnaireListResponse.java`, `QuestionnaireAdminListResponse.java`, `QuestionnaireDetailResponse.java`, `AssessmentStartResponse.java`, `AssessmentAnswerResponse.java`, `AssessmentHistoryResponse.java`, `AssessmentDetailResponse.java`, `AiParseResultResponse.java` |

### 前端

| 文件 | 说明 |
|---|---|
| `Chat.vue` | 评估模式完整 UI（选卷→答题→提交→结果+历史），含 `allAnswered` 提交确认态 |
| `AssessmentAdmin.vue` | 管理员页面（分页表格 + 上传DOCX解析 + 启禁用 + 详情弹窗） |

---

## 十、待实现（闲聊模块）

| 步骤 | 内容 | 优先级 |
|---|---|---|
| 1 | 建表 `chat_daily_summary` + Entity + Mapper | P0 |
| 2 | `AiChatService` + `AiChatServiceImpl`（调智谱 + Redis 存取） | P0 |
| 3 | `ChatController`（send + history） | P0 |
| 4 | 定时任务 `ChatDailyTask`（总结 + 清理） | P0 |
| 5 | `ZhipuProperties` 加 `chatModel` + `application.yml` 加配置 | P0 |
| 6 | 前端 Chat.vue 加模式切换 + 闲聊 UI | P0 |

---

## 十一、已知注意事项

| 问题 | 影响 | 当前策略 |
|---|---|---|
| 服务重启丢失今日 Redis 数据 | 当日对话断上下文 | 接受（MVP 阶段），后续加异步落盘 |
| 23:50 总结时用户正在聊天 | 最后几条消息丢失 | 加分布式锁 + Redis TTL 3 天安全网，下次启动时补跑 |
| 每日摘要逐日衰减 | 第 N 天丢失第 1 天信息 | 后续改为保留最近 3 天摘要拼入 system prompt |
| 定时任务失败无重试 | 当日摘要丢失 | Redis 数据保留 3 天，下次启动补跑未总结日期 |

---

**文档版本：** v2.1

**最后更新：** 2026-07-30

**关联文档：** `API接口文档.md`（第8章评估模块接口）
