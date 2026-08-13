# Codex 执行任务：Phase 6B CityHub 本地环境确认 + qwen3.7-flash 真实 LLM 联调

## 一、阶段背景

项目根目录：

```text
F:\JavaProject\YJSHZ-main
```

Phase 6 当前状态：

```text
LLM_CODE_MIGRATION: PASS
TOOL_CODE: PASS
WEB_ASSISTANT_UI: PASS
REAL_LLM_INTEGRATION: BLOCKED_BY_USER_CONFIG
```

Phase 6 已完成：

```text
Shop → Activity
Voucher → Ticket
ActivityTool
Redis Chat Memory
Vue /assistant
Streaming
AbortController
/ai-api → 8084
CityHub AI Prompt
```

当前剩余任务不是继续开发新功能，而是：

> **确认本地 `.env` 配置真实生效，并完成 qwen3.7-flash 的真实 Tool Calling / Memory / 防幻觉 / 预约引导验收。**

---

# 二、用户已完成的配置

用户已经在本地 `.env` 中配置了：

```text
DB_URL
DB_USERNAME
DB_PASSWORD

REDIS_HOST
REDIS_PORT
REDIS_PASSWORD

ALIYUNCS_API_KEY

LLM_BASE_URL
LLM_MODEL_NAME
```

模型目标：

```text
LLM_MODEL_NAME=qwen3.7-flash
```

Base URL 目标：

```text
LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
```

API Key：

```text
ALIYUNCS_API_KEY
```

已经由用户填写真实值。

---

# 三、数据库密码特殊情况

用户忘记了当前本地 MySQL root 密码。

用户明确允许本阶段只尝试以下三个候选：

```text
1. 123
2. 空密码
3. 123456
```

要求：

```text
严格按照上面顺序尝试
```

一旦任意一个连接成功：

```text
立即停止
```

禁止：

```text
继续猜其他密码
密码字典
暴力尝试
修改 root 密码
重置 MySQL
删除用户
创建新管理员
```

这只是本机 CityHub 开发数据库连接确认。

---

# 四、本阶段目标

完成后必须尽量达到：

```text
.env 读取链：
PASS

qwen3.7-flash：
PASS

MySQL：
PASS

Redis：
PASS

consultant 8084：
PASS

Vue /assistant：
PASS

Activity Tool Calling：
PASS

Category Tool：
PASS

Detail Tool：
PASS

Ticket Tool：
PASS

Redis 多轮 Memory：
PASS

No Hallucination：
PASS

Reservation Guide：
PASS
```

然后更新：

```text
docs/PHASE6_REPORT.md
```

把：

```text
REAL_LLM_INTEGRATION
```

从：

```text
BLOCKED_BY_USER_CONFIG
```

更新为真实最终结果。

---

# 五、本阶段禁止事项

禁止新增：

```text
Phase 7 功能
README
Docker 正式部署
Nginx 正式部署
多 Agent
MCP
RAG 重构
新的 Tool
新的 AI 页面
新的数据库字段
新的社区功能
```

本阶段只做：

```text
配置确认
真实联调
必要的最小修复
报告更新
```

---

# 六、Git 基线

开始前执行：

```bash
git status --short
git log --oneline -n 5
git branch --show-current
git remote -v
```

先确认 Phase 6 真实代码已经存在。

如果 Phase 6 尚未最终 commit：

不要急着提交。

先完成本阶段真实 LLM 联调。

---

# 七、第一步：确认 `.env` 是否真的被加载

这是本阶段第一个关键检查点。

不要因为项目根目录存在：

```text
.env
```

就假设 Spring Boot 一定会读取。

检查：

```text
backend/consultant
```

真实启动方式和配置读取链。

重点确认：

```text
application.yml
application.properties
配置类
pom.xml
启动脚本
Docker Compose
dotenv 依赖
IDE 启动配置
```

---

# 八、确认环境变量映射

必须确认 consultant 实际读取：

```text
DB_URL
DB_USERNAME
DB_PASSWORD

REDIS_HOST
REDIS_PORT
REDIS_PASSWORD

ALIYUNCS_API_KEY

LLM_BASE_URL
LLM_MODEL_NAME
```

特别确认：

```text
LLM_MODEL_NAME=qwen3.7-flash
```

是否真的进入 LangChain4j Builder。

---

# 九、禁止打印 Secret

验证环境变量时：

允许输出：

```text
ALIYUNCS_API_KEY: PRESENT
DB_PASSWORD: PRESENT
```

禁止输出：

```text
真实 ALIYUNCS_API_KEY
真实 DB_PASSWORD
真实 REDIS_PASSWORD
```

---

# 十、如果 `.env` 当前不会自动加载

在不引入复杂依赖的前提下，选择最简单可靠的 Windows 本地开发方案。

优先顺序：

## 方案 A
如果项目已有 dotenv 支持：

```text
继续修复/沿用
```

## 方案 B
如果已有启动脚本可安全加载 `.env`：

```text
补最小脚本
```

## 方案 C
如果 Spring Boot 当前只认系统环境变量：

提供一个本地启动方式，将 `.env` 内容加载到当前进程后再启动 consultant。

---

# 十一、不要污染 Git

必须确认：

```text
.env
```

在：

```text
.gitignore
```

中。

执行：

```bash
git check-ignore -v .env
```

或者等价检查。

`.env.example` 可以提交，但只能有：

```text
安全占位值
```

---

# 十二、qwen3.7-flash 配置检查

检查 consultant 当前配置是否还硬编码：

```text
qwen-plus
```

如果仍有：

必须改成读取：

```text
LLM_MODEL_NAME
```

并以：

```text
qwen3.7-flash
```

作为当前本地目标。

---

# 十三、Base URL 配置检查

确保当前真实调用读取：

```text
LLM_BASE_URL
```

目标：

```text
https://dashscope.aliyuncs.com/compatible-mode/v1
```

不要重新切换供应商。

---

# 十四、API Key

API Key 继续只从：

```text
ALIYUNCS_API_KEY
```

读取。

禁止：

```text
复制到 application.yml
复制到 Java
复制到报告
复制到 console
```

---

# 十五、MySQL 连接确认

目标数据库：

```text
cityhub
```

用户名：

```text
root
```

DB_URL 以 `.env` 真实配置为准。

---

# 十六、MySQL 密码尝试规则

仅允许：

### 第一次

```text
DB_PASSWORD=123
```

尝试连接。

成功：

```text
停止
```

---

### 如果失败

第二次：

```text
DB_PASSWORD=
```

空密码。

成功：

```text
停止
```

---

### 如果仍失败

第三次：

```text
DB_PASSWORD=123456
```

成功：

```text
停止
```

---

### 如果三个都失败

停止数据库密码尝试。

不要再猜。

报告：

```text
MYSQL_AUTH: BLOCKED_BY_USER_PASSWORD
```

并告诉用户：

```text
123 / 空密码 / 123456 均无法连接
```

不要自行重置 root 密码。

---

# 十七、MySQL 成功后验证数据库

连接成功后至少确认：

```sql
SELECT DATABASE();
SELECT COUNT(*) FROM tb_activity;
SELECT COUNT(*) FROM tb_ticket;
SELECT COUNT(*) FROM tb_activity_category;
```

期望使用：

```text
cityhub
```

并有 Phase 5B 已建立的 Activity/Ticket 数据。

不要修改真实业务数据。

---

# 十八、Redis 验证

使用 `.env`：

```text
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
```

确认：

```text
PING → PONG
```

不要清空 Redis。

禁止：

```text
FLUSHALL
FLUSHDB
```

---

# 十九、Redis Chat Memory

确认 consultant 使用的 Redis Memory 能连接。

不要删除现有 key。

真实对话测试时使用一个新的：

```text
memoryId
```

即可。

---

# 二十、启动 consultant

完成 MySQL / Redis / LLM 配置确认后启动：

```text
consultant
```

目标端口：

```text
8084
```

以真实项目启动命令为准。

---

# 二十一、启动后检查

确认：

```text
8084 listening
```

并验证：

```text
/chat
```

真实存在。

不要仅凭进程启动成功判定 AI 成功。

---

# 二十二、LLM 模型确认

需要从安全日志或运行配置中确认：

```text
model = qwen3.7-flash
```

允许输出模型名称。

禁止输出 API Key。

---

# 二十三、启动 Core

确保：

```text
Core 8081
```

运行正常。

至少检查：

```text
/api/activity/page
/api/activity/3
/api/ticket/list/3
```

真实有数据。

---

# 二十四、启动 Web

运行：

```bash
cd web
npm run dev
```

确认：

```text
5173
```

以及：

```text
/assistant
```

可访问。

---

# 二十五、测试 1：Activity Search Tool

在真实浏览器 `/assistant` 输入：

```text
最近有什么展览？
```

要求：

```text
真实 LLM 返回
```

并且结果必须基于：

```text
tb_activity
```

中的真实数据。

预期至少可能命中：

```text
当代摄影艺术展
```

但以真实数据库为准。

---

# 二十六、测试 1 验证 Tool Calling

尽量从安全 Debug / Tool 日志确认：

```text
searchActivities
```

或：

```text
listActivitiesByCategory
```

真实被模型调用。

如果当前日志完全关闭：

允许增加：

```text
只记录 Tool 名和参数摘要
```

的安全日志。

禁止记录：

```text
API Key
完整敏感请求
供应商认证头
```

---

# 二十七、测试 2：Category Tool

输入：

```text
有什么适合亲子的活动？
```

要求：

```text
真实 Tool 查询
```

结果以真实：

```text
tb_activity_category
tb_activity
```

为准。

---

# 二十八、测试 3：Detail Tool

输入：

```text
当代摄影艺术展在哪里？
```

要求：

```text
getActivityDetail
```

或等价真实 Tool 被调用。

回答中的：

```text
区域
地址
时间
价格
```

必须匹配真实数据库。

---

# 二十九、测试 4：多轮 Redis Memory

使用同一个新 memoryId：

第一轮：

```text
最近有什么摄影展？
```

第二轮：

```text
它在哪里？
```

要求：

```text
第二轮理解“它”指第一轮返回的 Activity
```

同时确认 Redis 中：

```text
memoryId
```

对应 ChatMemory 真实存在。

不要输出完整会话内容到报告。

---

# 三十、测试 5：Ticket Tool

在前面相同上下文中：

```text
这个活动有什么票？
```

要求真实调用：

```text
getActivityTickets
```

回答：

```text
价格
Ticket
库存/限量信息
```

必须与数据库一致。

---

# 三十一、测试 6：No Hallucination

新对话或明确上下文：

```text
CityHub 有火星探险活动吗？
```

数据库不存在时：

AI 必须明确类似：

```text
当前没有查询到相关活动
```

禁止生成：

```text
虚构活动名称
虚构票价
虚构地点
```

---

# 三十二、测试 7：Reservation Guide

基于一个真实 Activity：

```text
我想预约这个活动
```

正确行为：

```text
引导用户进入 /activities/{activityId}
```

禁止：

```text
AI 声称已经预约成功
AI 直接下单
AI 写 ReservationOrder
```

---

# 三十三、Activity Link

如果 AI 当前返回：

```text
/activities/{id}
```

文本链接：

确认前端可识别或用户可点击。

如果只是纯文本且前端当前未自动链接：

允许做一个极小的 Markdown/link 处理。

不要设计：

```text
复杂 Structured Event
Tool Event UI
JSON Message Protocol
```

---

# 三十四、Streaming 验证

真实 qwen3.7-flash 请求必须验证：

```text
逐步输出
```

不是最后一次性返回。

---

# 三十五、Stop Generation

发起一个较长问题。

点击：

```text
停止
```

确认：

```text
AbortController
```

真正终止前端读取。

不用验证供应商后台是否停止计费。

---

# 三十六、New Conversation

点击：

```text
新对话
```

确认：

```text
旧消息清空
memoryId 更新
```

后续新消息使用新 memoryId。

---

# 三十七、qwen3.7-flash thinking

第一轮：

```text
不要主动新增复杂 thinking 参数
```

先使用当前 OpenAI-compatible 默认配置跑通：

```text
Tool Calling
Streaming
Memory
```

如果出现：

```text
响应明显过慢
reasoning_content 影响当前 parser
Tool Calling 与 thinking 冲突
```

再做最小兼容调整。

不要未经必要性验证就大改模型参数。

---

# 三十八、异常处理

如 qwen3.7-flash 返回：

```text
model not found
401
403
429
400 tool call error
```

记录：

```text
HTTP 状态
错误类型
```

禁止记录：

```text
API Key
Authorization Header
完整供应商请求
```

只做最小修复。

---

# 三十九、Build / Compile

配置与联调完成后：

```bash
cd web
npm run build
```

必须 PASS。

后端：

```bash
cd backend
mvn clean compile -DskipTests
```

必须 PASS。

consultant 模块：

```text
单独 compile
```

也必须 PASS。

---

# 四十、报告处理

本阶段不要新建第二份 Phase 6 报告。

直接更新：

```text
F:\JavaProject\YJSHZ-main\docs\PHASE6_REPORT.md
```

---

# 四十一、PHASE6_REPORT.md 更新要求

将原：

```text
REAL_LLM_INTEGRATION: BLOCKED_BY_USER_CONFIG
```

根据真实结果更新。

如果全部成功：

```text
REAL_LLM_INTEGRATION: PASS
```

---

# 四十二、报告新增/更新内容

记录：

```text
.env 读取：
PASS / FAIL

LLM_MODEL_NAME：
qwen3.7-flash

LLM_BASE_URL：
只记录公开 Base URL

ALIYUNCS_API_KEY：
PRESENT
```

禁止写真实值。

---

# 四十三、MySQL 报告

只能记录：

```text
MySQL：
PASS
```

或：

```text
MYSQL_AUTH: BLOCKED_BY_USER_PASSWORD
```

不要把最终成功密码写进报告。

不要在报告中写：

```text
密码是 123
```

---

# 四十四、真实 AI 测试报告

逐项：

```text
Activity Search:
PASS / FAIL

Category:
PASS / FAIL

Detail:
PASS / FAIL

Multi-turn Memory:
PASS / FAIL

Ticket:
PASS / FAIL

No Hallucination:
PASS / FAIL

Reservation Guide:
PASS / FAIL

Streaming:
PASS / FAIL

Stop:
PASS / FAIL

New Conversation:
PASS / FAIL
```

必须基于真实 LLM 测试。

---

# 四十五、如果模型测试失败

不要为了赶进度写 PASS。

报告真实记录：

```text
FAIL
```

并说明：

```text
实际错误
已排查内容
下一步建议
```

---

# 四十六、Git Secret 检查

提交前：

```bash
git status --short
git diff --check
git check-ignore -v .env
```

并检查：

```bash
git grep -n "ALIYUNCS_API_KEY"
git grep -n "sk-"
```

确保真实 Key 没进入 tracked files。

---

# 四十七、不要提交 `.env`

绝对禁止：

```text
git add .env
```

只允许：

```text
.env.example
```

安全占位配置进入 Git。

---

# 四十八、Git 提交策略

如果 Phase 6 当前代码尚未 commit：

在本次真实联调通过后统一提交：

```bash
git add .
git diff --cached --check
git commit -m "feat: integrate CityHub AI assistant"
git push
```

如果 Phase 6 已经存在该 commit：

本次只对确实新增的配置代码/报告修复做新 commit，例如：

```text
test: verify CityHub AI assistant integration
```

不要 rewrite 历史。

禁止 force push。

---

# 四十九、本阶段最终验收标准

理想结果：

```text
.env 读取：
PASS

MySQL：
PASS

Redis：
PASS

qwen3.7-flash：
PASS

consultant 8084：
PASS

Activity Search：
PASS

Category：
PASS

Detail：
PASS

Multi-turn Redis Memory：
PASS

Ticket：
PASS

No Hallucination：
PASS

Reservation Guide：
PASS

Streaming：
PASS

Stop：
PASS

New Conversation：
PASS

npm build：
PASS

Maven：
PASS

Git Secret：
PASS

PHASE6_REPORT：
已更新
```

---

# 五十、如果 MySQL 三个候选密码都失败

此时停止。

允许继续完成：

```text
.env loader
qwen3.7-flash 配置
Redis 检查
前端 build
后端 compile
```

但所有依赖真实 CityHub DB 的 Tool Calling 测试：

```text
BLOCKED_BY_MYSQL_AUTH
```

不要创建新数据库，不要重置 root。

---

# 五十一、如果 API Key 失效

如果出现：

```text
401 / 403
```

停止真实 LLM 测试。

报告：

```text
REAL_LLM_INTEGRATION: BLOCKED_BY_LLM_AUTH
```

不要尝试猜 Key、刷新 Key 或操作阿里云账号。

---

# 五十二、完成后不要进入 Phase 7

完成本阶段后先停。

只向用户汇报真实 Phase 6 最终结果。

等待用户确认后再进入：

```text
Phase 7
```

---

# 五十三、最终回复格式

完成后只输出：

```text
Phase 6B CityHub 本地配置与真实 LLM 联调完成。

1. .env 读取：
2. MySQL：
3. Redis：
4. LLM Provider：
5. LLM Model：
6. API Key：
7. consultant 8084：
8. Activity Search：
9. Category：
10. Detail：
11. Multi-turn Memory：
12. Ticket：
13. No Hallucination：
14. Reservation Guide：
15. Activity Link：
16. Streaming：
17. Stop Generation：
18. New Conversation：
19. npm run build：
20. Maven：
21. Git Secret Check：
22. PHASE6_REPORT：
23. Git commit：
24. commit hash：
25. push：
26. Phase 6 最终状态：

注意：
- 不输出数据库真实密码；
- 不输出 ALIYUNCS_API_KEY；
- 不输出 Redis 密码；
- 如果阻断，明确写 BLOCKED 原因，不伪造 PASS。
```

---

# 五十四、最终原则

本次工作不是继续开发 AI。

目标只有一个：

> **把 Phase 6 从“代码完成但真实 LLM 未验证”推进到“qwen3.7-flash + CityHub Activity Tool + Redis Memory + Streaming 已真实跑通”。**

严格限制范围。

真实通过后，Phase 6 才正式结束。
