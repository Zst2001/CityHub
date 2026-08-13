# Phase 6 CityHub AI 顾问迁移与业务适配报告

## 结论

**代码迁移通过；Phase 6B 已确认本地模型配置与 Redis，但 CityHub MySQL root 认证阻断了 consultant 启动和真实 LLM Tool Calling。**

| 项目 | 结果 |
| --- | --- |
| LLM_CODE_MIGRATION | PASS |
| TOOL_CODE | PASS |
| WEB_ASSISTANT_UI | PASS |
| REAL_LLM_INTEGRATION | BLOCKED_BY_MYSQL_AUTH |

Phase 6B 确认根目录 `.env` 包含 `ALIYUNCS_API_KEY`、`LLM_BASE_URL` 和 `LLM_MODEL_NAME=qwen3.7-flash`；8084 未启动是因为 MySQL root 仅获授权尝试的三个候选密码均无法认证。没有伪造模型输出或切换其他 Provider。主站在此状态将 502 收敛为“AI 顾问暂时无法响应，请稍后重试。”

## Phase 6B 本地真实联调补验收

- `.env`：存在、全部必需变量名存在，且 `.gitignore` 已忽略；Spring Boot 默认不会自动加载根 `.env`，因此新增 `backend/consultant/run-local.ps1`。该脚本仅在当前 PowerShell 进程读取 `.env` 并启动 consultant，不打印或提交 Secret。
- LLM：`application.yml` 的 chat 与 streaming `base-url` 改为 `LLM_BASE_URL`，模型名改为 `LLM_MODEL_NAME`（默认 `qwen3.7-flash`）；本地 `.env` 实测目标模型为 `qwen3.7-flash`、公开 Base URL 为 DashScope compatible endpoint、API Key 为 PRESENT（未输出值）。
- MySQL：依授权顺序仅尝试 root 密码 `123`、空、`123456`，三次均无法连接 `127.0.0.1:3306/cityhub`，随后立即停止；未继续猜测、未重置任何账号或数据。状态为 `MYSQL_AUTH: BLOCKED_BY_USER_PASSWORD`。
- Redis：通过 `.env` 的连接参数执行 PING，结果 PONG；没有清空或删除 Redis 数据。
- 服务：Core 8081 与 Vite 5173 已监听；consultant 8084 因上述 MySQL 认证阻断未启动。
- 真实 Tool Calling、摄影展多轮 Redis Memory、Ticket、防幻觉、预约引导、真实 Streaming 和 Stop：全部 `BLOCKED_BY_MYSQL_AUTH`。这些检查依赖 consultant 成功连接 CityHub MySQL，未作伪造验证。
- New Conversation、`memoryId` localStorage、AbortController、textarea 自适应和无 8084 的友好错误 UI 已在 Phase 6 前端验证；本轮未能将其标记为真实 LLM 流式 PASS。

## 基线与 consultant 原始架构

- Phase 5C 真实提交：`b2e7c78 feat: build CityHub community experience`。
- 原 consultant：Spring Boot 3.5、LangChain4j OpenAI-compatible starter、Qwen Plus streaming、Redis ChatMemory、Redis embedding/RAG、MyBatis 直查 `tb_shop/tb_voucher/tb_voucher_order`，并有独立 Vue CDN + Tailwind 静态聊天页。
- 原 API：`GET /chat?message=&memoryId=` 返回 `Flux<String>`，已保留。
- RAG 是旧 demo：启动会加载旧 content 并依赖 embedding；本阶段将其从 AI Service 运行链路移除，不重构或删除依赖。Redis ChatMemory、Tool Calling 与 Streaming 保留。

## LLM 配置与 Secret 安全

- Provider：DashScope OpenAI-compatible；Base URL 为 `https://dashscope.aliyuncs.com/compatible-mode/v1`；Chat/Streaming 模型 `qwen-plus`，embedding 模型 `text-embedding-v3`。
- 环境变量：`ALIYUNCS_API_KEY`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。
- 未发现提交的真实 API Key。`git grep` 命中仅为 `${ALIYUNCS_API_KEY}`、公开 Base URL 与 `.env.example` 示例值。
- 关闭 LangChain4j 的 request/response 日志及 debug 级日志，避免请求内容或供应商交互被日志记录。
- consultant 数据库默认地址由旧 `redis_project` 改为 `cityhub`。

## CityHub 语义与 Tool

- 已移除 consultant 运行路径中的 Shop/Voucher/VoucherOrder/到店 Reservation Tool、Mapper、Service 与 POJO；`CityHubAiApplication` 不再扫描旧 Mapper。
- 新增唯一 `ActivityTool`（4 个只读 Tool）：
  1. `searchActivities(keyword)`：真实 `tb_activity` 搜索；
  2. `listActivitiesByCategory(categoryName)`：真实 `tb_activity_category` 分类；
  3. `getActivityDetail(activityId)`：时间、区域、地址、价格等真实字段；
  4. `getActivityTickets(activityId)`：`tb_ticket` + `tb_seckill_ticket` 的价格、规则、库存。
- AI 不持有 `POST /reservation/seckill/**`、不写 `ReservationOrder`。System Prompt 要求用户预约时引导 `/activities/{activityId}`。
- 新 System Prompt 强制活动、时间、地点、价格、Ticket、库存走 Tool；无结果必须说明“当前没有查询到相关活动”，禁止虚构。

## Memory、Streaming 与 Web

- Redis `RedisChatMemoryStore` 仍按 `memoryId` 保存 1 天，`MessageWindowChatMemory` 保留 20 条窗口；空会话安全返回空列表。
- Vue `/assistant` 以 localStorage 保存 `cityhub_ai_memory_id`；“新对话”清空前端消息并生成新的 UUID，不删除历史 Redis 会话。
- 复用并迁移成熟交互：fetch streaming、`AbortController` 停止生成、自动滚动、textarea 自适应、生成中防重复提交、友好错误状态。
- 建立 `/ai-api -> 8084` Vite Proxy，并移除 `/ai-api` 前缀，兼容现有 `/chat` 协议。
- Desktop Header 和 Mobile Drawer 已加入“AI 顾问”。
- 提供四条建议问题；页面为 CityHub 暖白/深墨/墨绿/橙棕视觉。没有 iframe、Dark Mode 或第二套完整 UI。
- 旧 `consultant/static/index.html` 已替换为到 Vue `/assistant` 的简短迁移说明，不再维护旧独立聊天 UI。

## 验证结果

| 验证项 | 结果 | 依据 |
| --- | --- | --- |
| Activity Tool 数据基础 | PASS | MySQL：摄影展 id=3、时间/区域/价格真实存在；Ticket 3 库存 120 |
| Activity Search / Category / Detail / Ticket LLM 结果 | BLOCKED_BY_MYSQL_AUTH | API Key 与 qwen3.7-flash 配置已确认；consultant 无法连接 CityHub MySQL，未启动真实 Tool Calling |
| 多轮上下文 | BLOCKED_BY_MYSQL_AUTH | Redis PING 通过、Memory 代码保留；consultant 未能启动真实模型会话 |
| 不虚构测试 | BLOCKED_BY_MYSQL_AUTH | System Prompt 和空结果 Tool 代码已实现；无真实模型不可断言输出 |
| 预约引导测试 | BLOCKED_BY_MYSQL_AUTH | Prompt 规定只引导详情页，真实模型不可运行 |
| `/assistant` | PASS | 浏览器真实渲染 CityHub AI 顾问与 4 条建议问题 |
| 502 用户体验 | PASS | 无 8084 时浏览器显示友好错误，不显示堆栈/Key |
| 1440 / 768 / 390 | PASS | 三档无横向溢出；390px Drawer 含 AI 顾问 |
| `npm run build` | PASS | Vite production build 成功；保留既有 Element Plus 大 chunk 非阻断警告 |
| consultant `mvn clean compile -DskipTests` | PASS | 仅 6 个 CityHub AI 必要源文件编译成功 |
| backend `mvn clean compile -DskipTests` | PASS | parent/core/AI 全 reactor 成功 |

## 后续真实 LLM 验证前提

请向用户确认 CityHub MySQL root 的正确本地密码（或将 `.env` 改为可认证的本地数据库用户）后，运行 `backend/consultant/run-local.ps1`。随后再验证：摄影展搜索→“它在哪里”多轮、亲子分类、Ticket、火星探险空结果、预约详情页引导与 Redis memory key。

## 已知限制

1. 真实 LLM API 未配置，以上模型行为均不能标记 PASS。
2. AI 回复中的 `/activities/{id}` 是 Prompt 级轻量链接文本，未扩展为复杂 structured event 协议。
3. 本阶段不做 AI 直接预约、支付、历史会话、模型切换、图片/语音输入或 RAG 重构。

## Git

- 分支：`main`
- 提交：`feat: integrate CityHub AI assistant`
- 提交 hash 与 push 结果由最终交付中的实际 Git 输出记录。
