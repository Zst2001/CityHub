# Codex 执行任务：Phase 6 CityHub AI 顾问迁移与业务适配

## 一、阶段背景

项目：**CityHub - 城市活动发现与预约平台**

当前已经完成：
- Phase 1：工程治理
- Phase 2：CityHub 工程身份规范化
- Phase 3A-R：核心领域迁移
- Phase 3B：Activity + Redis 缓存
- Phase 3C：限量预约 / 秒杀
- Phase 4：Blog / Follow / Feed 后端社区适配
- Phase 5A：Vue 3 Web 工程骨架、Router、Axios、Pinia、登录、Design System
- Phase 5B：首页、Activity List、Activity Detail、Ticket / Reservation
- Phase 5C：Community、Like、Follow、Following Feed、Publish Blog、Profile

当前 Core Web 已基本完整。Phase 6 只处理 AI 顾问。

目标：

```text
backend/consultant 旧独立 AI
→ CityHub 业务语义迁移
→ Vue /assistant
→ LangChain4j Tool Calling
→ Redis Chat Memory
→ Streaming
→ Activity Detail 引导预约
```

---

## 二、本阶段定位

Phase 6 不是重新做一个大模型项目，而是把现有 consultant 迁移成真正理解 CityHub 业务的 AI 活动顾问。

重点：

```text
LangChain4j
Tool Calling
Redis Chat Memory
Streaming
Vue /assistant
Activity / Ticket Business Tools
```

最终用户链：

```text
进入 /assistant
→ 问“最近有什么展览？”
→ LLM 调用 Activity Tool
→ 返回真实 Activity
→ 继续问“它在哪里？”
→ Redis Memory 保持上下文
→ 问“有什么票？”
→ Ticket Tool
→ 问“我想预约”
→ AI 引导进入 /activities/:id
→ 使用 Phase 5B 现有预约链
```

---

## 三、本阶段禁止事项

禁止：

```text
多 Agent
Supervisor / Planner Agent
MCP
复杂 RAG 重构
知识库管理后台
AI 直接预约
AI 直接支付/退款
历史会话列表
语音输入
图片输入
联网搜索
模型切换
Prompt 管理后台
Token 统计后台
复杂 SSE 协议重构
Nginx 正式部署
README 修改
```

禁止修改：

```text
缓存核心
Lua
Redisson
BlockingQueue
RedisIdWorker
Feed
Like
Follow
Activity/Ticket 核心架构
```

Phase 7 再统一清理和部署。

---

## 四、Git 基线

开始前执行：

```bash
git status --short
git log --oneline -n 10
git branch --show-current
git remote -v
```

要求：
- 当前分支 `main`；
- working tree clean；
- `main == origin/main`；
- 记录 Phase 5C 的真实 commit hash。

如果报告记录和 Git 不一致，以真实 `git log --oneline` 为准。

---

## 五、执行顺序

本阶段内部按：

```text
6-1 consultant 代码与 LLM 配置审计
→ 6-2 Shop/Voucher → Activity/Ticket 领域迁移
→ 6-3 Tool + Prompt + Redis Memory 验证
→ 6-4 Vue /assistant + Streaming
→ 6-5 前后端联调 + Responsive + Build
```

仍然是一个阶段、一个最终 commit、一个报告。

---

## 六、6-1：完整审计 consultant

先完整扫描：

```text
backend/consultant
```

重点确认：

```text
ChatController
AI Service / Assistant Interface
LangChain4j Config
Streaming Model / Chat Model
ChatMemory
Redis Chat Memory
Tools
POJO / DTO
Mapper
Service
Retriever / RAG
Prompt
application.yml / application-*.yml
static/index.html
```

全局搜索：

```text
Shop
shop
Voucher
voucher
VoucherOrder
优惠券
店铺
商户
探店
团购
点评
```

在唯一报告中说明：
- 哪些是真正运行代码；
- 哪些是 Tool；
- 哪些是 Mapper / Entity；
- 哪些是 Prompt；
- 哪些只是死代码；
- 哪些是用户可见旧语义。

禁止先做盲目字符串全局替换。

---

## 七、LLM 配置审计与 API Key 安全

先读取真实：

```text
application.yml
application-dev.yml
AI Config
LangChain4j 配置类
```

确认：

```text
LLM Provider
Base URL
Model Name
API Key 来源
Streaming 配置
Tool Calling 支持方式
```

不要根据提示词猜模型供应商。

### API Key 安全规则

真实 API Key 禁止：
- 写入 Java；
- 写入 application.yml；
- 写入 README；
- 写入报告；
- 写入 Git；
- 打印完整值。

如当前存在明文 Secret，迁移为环境变量。

优先沿用项目现有变量名；只有当前配置混乱时才统一成类似：

```text
LLM_API_KEY
LLM_BASE_URL
LLM_MODEL_NAME
```

允许在 `.env.example` 或配置示例中只记录变量名和空值，不得记录真实 Key。

### 如果用户尚未配置真实 API Key

不得伪造真实模型测试。

允许继续完成：
- consultant 代码迁移；
- Tool 代码；
- Vue `/assistant`；
- Proxy；
- Build / Compile。

报告必须标记：

```text
LLM_CODE_MIGRATION: PASS
TOOL_CODE: PASS
WEB_ASSISTANT_UI: PASS
REAL_LLM_INTEGRATION: BLOCKED_BY_USER_CONFIG
```

并准确告诉用户还需设置哪些环境变量。

禁止自动切换其他 LLM Provider，禁止擅自安装 Ollama。

---

## 八、Secret 安全扫描

真实 LLM 联调前至少执行：

```bash
git grep -n "sk-"
git grep -n "api-key"
git grep -n "apikey"
git grep -n "dashscope"
```

结合真实项目判断是否存在敏感值。

如发现明文 Secret，先迁移环境变量，再继续。

---

## 九、6-2：AI 领域语义迁移

核心映射：

```text
Shop → Activity
Voucher → Ticket
SeckillVoucher → SeckillTicket
VoucherOrder → ReservationOrder
```

目标：AI 运行路径和用户可见回复不再使用：

```text
店铺
商户
探店
优惠券
团购券
Shop
Voucher
```

重点迁移：
- Tool 名称；
- Tool Description；
- System Prompt；
- AI DTO / POJO；
- Mapper；
- Service；
- 用户回复文案；
- 旧静态页面用户可见文案。

---

## 十、数据库访问迁移

如果 consultant 当前直接查数据库，迁移到真实 CityHub：

```text
tb_activity
tb_ticket
tb_seckill_ticket
tb_reservation_order
tb_blog（如需要）
```

不要继续查询：

```text
tb_shop
tb_voucher
tb_voucher_order
```

如果 consultant 当前已经通过 Core API 查询业务，则沿用现有方式。

不要为了“微服务化”重新设计调用链。

---

## 十一、Tool 数量控制

最终核心 Tool 控制在约 4~5 个。

优先：

### 1. `searchActivities`
按关键词查询真实 Activity。

支持：

```text
摄影
音乐
市集
亲子
```

### 2. `listActivitiesByCategory`
按真实 ActivityCategory 查询。

支持：

```text
有什么展览？
有什么适合亲子的活动？
最近有什么演出？
```

### 3. `getActivityDetail`
输入 `activityId`，返回真实字段，例如：

```text
title
category
openHours
area
address
avgPrice
score/sold（仅必要时）
```

禁止虚构不存在的：

```text
startTime
endTime
venueId
```

### 4. `getActivityTickets`
输入 `activityId`，返回真实 Ticket / SeckillTicket 信息，例如：

```text
标题
价格
类型
规则
库存/限量信息（真实可得时）
```

### 5. `getActivityExperiences`（可选）
如果低成本，可查询该 Activity 的少量 Blog 体验摘要。

如果会明显扩大范围，可不做，不作为 Phase 6 阻断项。

禁止堆十几个 Tool。

---

## 十二、AI 不直接预约

Phase 6 禁止让 AI 自己调用：

```text
POST /reservation/seckill/**
```

也禁止 AI 直接写 ReservationOrder。

AI 只做：

```text
发现
查询
解释
推荐
预约引导
```

当用户说：

```text
我想预约这个
帮我报名
```

AI 应确认 Activity，并引导进入：

```text
/activities/:id
```

真正预约继续复用 Phase 5B：

```text
Login
→ Reservation
→ Lua
→ Redis
→ BlockingQueue
→ Redisson
→ MySQL
```

---

## 十三、System Prompt 重写

AI 身份固定为：

> **CityHub AI 活动顾问**

Prompt 必须明确：
1. 帮助用户发现城市文化活动；
2. 查询真实 Activity；
3. 查询真实 Ticket / 名额；
4. 必要时参考 Activity Blog；
5. 用户决定预约时引导进入 Activity Detail；
6. 活动名称、时间、地点、价格、Ticket、库存等事实必须优先调用 CityHub Tool；
7. 数据库查不到时明确说没有查询到；
8. 禁止凭模型记忆编造活动、票价、地点和库存。

不要塞复杂人格、Chain of Thought、长篇营销话术。

---

## 十四、6-3：Redis Chat Memory

必须保留现有：

```text
memoryId
Redis Chat Memory
```

不要退化成无上下文聊天。

Vue 前端使用 `localStorage` 保存：

```text
cityhub_ai_memory_id
```

如果不存在则生成 UUID。

每次聊天携带相同 memoryId。

---

## 十五、新对话

提供：

```text
新对话
```

行为：

```text
清空前端消息
→ 生成新 memoryId
→ 后续使用新的 Redis Chat Memory
```

不要求历史会话列表，也不要求清理旧 Redis 会话。

---

## 十六、多轮上下文必须真实验证

真实 LLM 可用时测试：

```text
用户：最近有什么摄影展？
AI：返回真实活动

用户：它在哪里？
AI：正确理解“它”指上一轮 Activity
```

---

## 十七、必须验证不虚构

测试：

```text
CityHub 有火星探险活动吗？
```

若数据库不存在：

```text
当前没有查询到相关活动
```

禁止编造活动。

---

## 十八、RAG 边界

审计当前 Retriever / RAG。

如果现有 RAG 已真实工作且对 CityHub 有价值：
- 保留。

如果只是旧 demo、失效或无业务价值：
- 不为 Phase 6 专门重构。

Phase 6 主故事保持：

```text
LangChain4j
+ Tool Calling
+ Redis Chat Memory
+ Streaming
```

不要扩成复杂 RAG 项目。

---

## 十九、6-4：Vue 主站新增 /assistant

正式新增：

```text
/assistant
```

建立：

```text
AssistantView.vue
```

按需要拆：

```text
AssistantWelcome.vue
ChatMessage.vue
ChatComposer.vue
SuggestedPrompt.vue
```

不要过度组件化。

禁止 iframe。

禁止直接跳 `http://localhost:8084`。

---

## 二十、Header / Mobile Drawer

主导航增加：

```text
AI 顾问
```

Desktop Header 与 Mobile Drawer 都要有。

跳：

```text
/assistant
```

---

## 二十一、AI 页面视觉

继续沿用 CityHub：

```text
暖白
深墨
墨绿
橙棕
```

不要做：
- ChatGPT 仿站；
- 科技蓝紫渐变；
- 机器人 3D；
- 赛博朋克。

页面建议：

```text
CityHub AI 顾问
找活动、问票券、规划你的城市周末。

Suggested Prompts
Chat Messages
Chat Composer
```

---

## 二十二、Suggested Prompts

空会话提供 3~4 个：

```text
这个周末有什么值得去的活动？
有哪些适合亲子的活动？
最近有什么展览？
夏日爵士音乐会有什么票？
```

点击后直接发送。

---

## 二十三、复用旧 AI 页面成熟交互

从旧 `static/index.html` 迁移并保留：

```text
Streaming
AbortController
Auto Scroll
Textarea Auto Resize
Clear / New Conversation
请求错误状态
```

不要完全重写这些已工作逻辑。

旧 AI 独立 Dark Mode 不迁移，除非整个 CityHub 已经有全局 Dark Mode。

---

## 二十四、AI API Proxy

Core 已使用：

```text
/api → 8081
```

AI 新增：

```text
/ai-api → 8084
```

修改 `vite.config.js`，使：

```text
/ai-api/chat
→ http://127.0.0.1:8084/chat
```

正确移除 `/ai-api` 前缀。

Phase 6 不做正式 Nginx 部署；Phase 7 再同步 Nginx。

---

## 二十五、Chat API

优先保留当前真实可用：

```text
GET /chat?message=&memoryId=
```

以及现有 Streaming 行为。

不要仅为了 REST 风格重写聊天协议。

如果真实联调暴露 URL 长度、编码问题，再做最小修复。

---

## 二十六、Streaming UI

必须：
- AI 回复逐步显示；
- 生成中避免重复 submit；
- 提供停止生成；
- 使用 AbortController；
- Auto Scroll；
- textarea 自适应；
- 请求失败有用户友好提示。

错误提示例如：

```text
AI 顾问暂时无法响应，请稍后重试。
```

不要把 500、供应商异常堆栈、API Key 信息展示给用户。

---

## 二十七、登录状态

AI 页面不要求登录。

未登录也可使用。

若已登录，可读取 `userStore.user` 显示轻量欢迎语，但不要让登录身份决定 memoryId。

---

## 二十八、Activity Link

当 AI 明确识别出 Activity 时，尽量提供：

```text
查看活动 →
```

跳：

```text
/activities/:id
```

优先轻量实现。

不要设计复杂 Tool Event Renderer / Structured Chat Protocol。

如果当前后端无法低成本稳定提供 activityId，可合理降级，但不要为此扩大协议设计。

---

## 二十九、6-5：真实联调环境

真实验证时启动：

```text
MySQL
Redis
Core 8081
Consultant 8084
Vite 5173
```

---

## 三十、真实 LLM 测试

如果用户已配置真实 LLM API，至少验证：

### Activity Search

```text
最近有什么展览？
```

### Category

```text
有什么适合亲子的活动？
```

### Detail

```text
当代摄影艺术展在哪里？
```

### Multi-turn

```text
最近有什么摄影展？
它在哪里？
```

### Ticket

```text
这个活动有什么票？
```

### No Hallucination

```text
CityHub 有火星探险活动吗？
```

### Reservation Guide

```text
我想预约这个活动
```

最后一项必须引导进入 Activity Detail，而不是 AI 自己下单。

---

## 三十一、如果真实 API Key 仍缺失

不得伪造 PASS。

报告明确：

```text
LLM_CODE_MIGRATION: PASS/FAIL
TOOL_CODE: PASS/FAIL
WEB_ASSISTANT_UI: PASS/FAIL
REAL_LLM_INTEGRATION: BLOCKED_BY_USER_CONFIG
```

并告诉用户需要设置哪些环境变量名。

---

## 三十二、Responsive

至少检查：

```text
1440
768
390
```

Mobile 重点：
- Chat 不横向溢出；
- 消息气泡正常；
- Composer 可用；
- Stop 可点击；
- Header Drawer 有 AI 顾问入口。

---

## 三十三、旧 static AI 页面处理

当 `/assistant` 完成后，检查：

```text
backend/consultant/src/main/resources/static/index.html
```

不允许最终长期维护两套完整 AI UI。

优先：
- 不再需要则删除；
- 若 Spring 根路径依赖，则改成简单说明或重定向提示。

报告写明最终处理方式。

---

## 三十四、旧语义扫描

仅针对 consultant 运行路径搜索：

```text
Shop
Voucher
优惠券
店铺
商户
探店
团购
雅鉴生活志
YJSHZ
```

历史 docs / legacy 归档不作为 Phase 6 删除目标。

不要顺手清整个 Core 的所有历史残留。

---

## 三十五、Build / Compile

前端：

```bash
cd web
npm run dev
npm run build
```

必须 PASS。

后端：

```bash
cd backend
mvn clean compile -DskipTests
```

必须 PASS。

同时单独确认 AI module compile；模块名以真实 Maven 配置为准。

---

## 三十六、唯一报告

本阶段只生成：

```text
F:\JavaProject\YJSHZ-main\docs\PHASE6_REPORT.md
```

禁止多个 audit / verification / AI report。

---

## 三十七、PHASE6_REPORT.md 必须包含

1. Phase 6 是否通过
2. Phase 5C 真实 Git commit
3. consultant 原始架构
4. LLM Provider / Base URL / Model Name 配置方式
5. 使用哪些环境变量（禁止写真实 Key）
6. 是否发现并清理明文 Secret
7. Shop → Activity 实际迁移
8. Voucher → Ticket 实际迁移
9. Reservation 语义处理
10. 最终 Tool 清单
11. System Prompt 规则
12. Redis Chat Memory
13. memoryId / New Conversation
14. `/assistant`
15. Header AI Entry
16. Suggested Prompts
17. Streaming / Stop / Auto Scroll / Textarea
18. `/ai-api -> 8084`
19. Activity Search Test
20. Category Test
21. Detail Test
22. Multi-turn Test
23. Ticket Test
24. No Hallucination Test
25. Reservation Guide Test
26. Activity Link
27. 1440 / 768 / 390
28. 旧 static AI 页处理
29. consultant 旧语义清理结果
30. npm build
31. Maven compile
32. 已知限制
33. Git commit / hash / branch / push

若缺真实 LLM 配置，准确记录 `BLOCKED_BY_USER_CONFIG`。

---

## 三十八、Git 安全检查

提交前：

```bash
git status --short
git diff --check
```

绝不提交：
- 真实 API Key
- `.env`
- `.env.local`
- node_modules
- dist
- 模型日志
- 请求日志
- 临时 token
- 临时测试数据

可以提交安全的 `.env.example`，但只能包含变量名和空值。

---

## 三十九、Git commit

完成并验证后：

```bash
git add .
git diff --cached --check
git diff --cached --stat
git commit -m "feat: integrate CityHub AI assistant"
git push
```

禁止 force push。

如果因为用户尚未提供真实 LLM API 而无法完成关键真实联调：
- 不得伪造完成；
- 报告说明阻断；
- 是否提交“代码已完成但待配置”的状态，以代码是否稳定可编译为准。

---

## 四十、Phase 6 核心验收

当真实 LLM API 已配置时，应尽量满足：

```text
/assistant：PASS
主导航 AI：PASS
CityHub 统一视觉：PASS
Streaming：PASS
Stop Generation：PASS
New Conversation：PASS
memoryId：PASS
Redis Chat Memory：PASS
Activity Search Tool：PASS
Category Tool：PASS
Activity Detail Tool：PASS
Ticket Tool：PASS
Multi-turn：PASS
No Hallucination：PASS
Reservation Guide：PASS
Activity Link：PASS 或合理降级
Shop 用户可见语义：清理
Voucher 用户可见语义：清理
1440：PASS
768：PASS
390：PASS
npm run build：PASS
Maven：PASS
Git：commit + push
```

---

## 四十一、不作为 Phase 6 阻断项

本阶段不要求：
- AI 直接预约
- 支付
- 退款
- 多 Agent
- MCP
- 复杂 RAG
- 知识库后台
- 历史会话列表
- 模型切换
- 语音
- 图片输入
- 联网搜索
- Prompt 后台
- Dark Mode

---

## 四十二、下一阶段

Phase 6 完成后进入：

> **Phase 7：最终工程清理、部署与求职包装**

Phase 7 再处理：
- Blog.shopId / tb_blog.shop_id
- unused like.lua
- RedisIdWorker 业务前缀
- 旧命名最终扫描
- Nginx
- Docker / 启动方式
- 环境变量
- 最终测试
- README
- GitHub 项目首页
- 简历项目描述
- 技术亮点
- 面试话术

Phase 6 不提前做。

---

## 四十三、最终回复格式

完成后只输出：

```text
Phase 6 CityHub AI 顾问迁移与业务适配完成。

1. Phase 5C Git：
2. consultant 架构：
3. LLM Provider：
4. LLM 环境变量：
5. Secret 安全：
6. Shop → Activity：
7. Voucher → Ticket：
8. Reservation 语义：
9. Activity Search Tool：
10. Category Tool：
11. Activity Detail Tool：
12. Ticket Tool：
13. Experience Tool：
14. System Prompt：
15. Redis Chat Memory：
16. memoryId：
17. New Conversation：
18. /assistant：
19. Header AI Entry：
20. Streaming：
21. Stop Generation：
22. Suggested Prompts：
23. AI Proxy：
24. Activity Search Test：
25. Multi-turn Test：
26. Ticket Test：
27. No Hallucination Test：
28. Reservation Guide Test：
29. Activity Link：
30. Desktop：
31. Tablet：
32. Mobile：
33. npm run build：
34. Maven：
35. 旧 static AI 页：
36. 旧语义清理：
37. Git commit：
38. commit hash：
39. push：
40. 已知限制：
41. 下一阶段：

唯一报告：
F:\JavaProject\YJSHZ-main\docs\PHASE6_REPORT.md
```

---

## 四十四、最终原则

Phase 6 不追求 AI 功能数量。

真正目标：

```text
自然语言
→ LangChain4j
→ Tool Calling
→ CityHub 真实 Activity/Ticket 数据
→ Redis Chat Memory
→ Streaming
→ Activity Detail
→ Reservation
```

把现有“能聊天的旧 AI 页面”变成真正属于 CityHub、可以在实习面试中清晰讲解的城市活动 AI 顾问。
