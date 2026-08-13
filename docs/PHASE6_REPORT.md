# Phase 6 CityHub AI 顾问最终验收报告

## 最终结论

`REAL_LLM_INTEGRATION: PASS`

CityHub AI 顾问现正式使用 `qwen3.7-flash`、DashScope OpenAI-compatible API、Redis Chat Memory 与真实 CityHub MySQL 数据。原生 **Streaming Tool Calling** 已修复：模型真实选择 Tool，`ActivityTool` 查询 Docker MySQL 的 `cityhub` 数据库，Tool 结果回传模型后以流式响应输出。

本阶段未采用 fallback、未切换模型、未添加 Tool、未用正则修复 JSON 或吞异常伪造成功。

## Phase 6C 基线与原始复现

| 项目 | 结果 |
| --- | --- |
| Phase 6C 基线提交 | `6f50963 test: verify CityHub AI assistant integration` |
| 原始版本 | LangChain4j starter / reactor / community `1.0.1-beta6`，但 core / open-ai 解析为 `1.0.1` |
| 原始异常 | `JsonParseException: Unexpected character ('(')` |
| 失败位置 | `ChatCompletionChoice.Builder["delta"]` 的流式 Tool Call SSE 解析 |
| 触发条件 | `qwen3.7-flash` + DashScope OpenAI-compatible + Streaming Tool Calling |

## Direct Provider 诊断（绕过 LangChain4j）

所有直连请求只在当前进程读取 `.env`；原始请求、Authorization Header 与 Key 均未写入文件或 Git。

| 检查 | 结果 | 脱敏证据 |
| --- | --- | --- |
| 非流式 Function Calling | PASS | 返回一个 `get_activity_detail` Tool Call，arguments 可解析为合法 JSON |
| 普通 Streaming | PASS | 82 个 SSE 内容事件，最终 `finish_reason=stop` |
| Streaming Tool Calling | PASS | `enable_thinking=false` 下原始 SSE 正常结束为 `finish_reason=tool_calls` |
| 原始 Tool Call SSE | PASS | 首帧有完整 tool call id 与 function name；后续 chunks 仅分段 arguments，拼接为合法 `{"activityId": 3}` |
| Provider 判断 | PASS | Provider 原始响应合法，问题不属于 DashScope 或 Tool Schema |

因此问题定位为旧 LangChain4j 的 OpenAI Streaming SSE Parser / Tool Call ID 累积兼容层，而非 MySQL、Redis、API Key 或 qwen3.7-flash Function Calling 能力。

## 兼容修复

### 依赖统一

原依赖同时存在 beta starter 与正式 core/open-ai 的混用。现通过 `langchain4j-bom:1.15.1` 统一主版本：

| 模块 | 修复后版本 |
| --- | --- |
| `langchain4j` / `langchain4j-core` / `langchain4j-open-ai` | `1.15.1` |
| OpenAI / Spring / Reactor / RAG starter | `1.15.1-beta25` |
| community Redis starter | `1.15.0-beta25`（该社区模块独立发布线） |
| Spring Boot / Java | 3.5.0 / 17（未升级） |

`mvn dependency:tree` 已确认 LangChain4j 主模块不再解析到旧 1.0.1-beta6。

### Qwen 模型配置

新版 Builder 实际提供 `accumulateToolCallId(Boolean)` 与 `customParameters(Map)`，因此新增小范围 Qwen 模型配置：

- Chat / Streaming 模型均从同一 `LLM_BASE_URL`、`ALIYUNCS_API_KEY`、`LLM_MODEL_NAME=qwen3.7-flash` 环境变量读取；
- 两者均透传 `enable_thinking=false`；
- Streaming 模型使用 `accumulateToolCallId(false)`，避免把 Qwen 首帧完整 id 与后续空 id 错误拼接；
- 关闭请求与响应日志，避免记录模型交互或 Secret；
- 改用新版 `AiServices.builder(...).tools(activityTool)` 显式装配，确保四个既有只读 Tool 真正进入请求；
- 显式绑定 `/chat` 的 `memoryId` / `message` 请求参数，固定 Tomcat URI UTF-8；
- 排除升级后被激活、但不属于运行链路的旧 Redis embedding store 自动配置；Redis Chat Memory 仍保留。

最终架构：`NATIVE_STREAMING_TOOL_CALLING`，未采用“非流式 Tool Orchestration + 流式最终回答” fallback。

## 真实 Agent 验收

| 验收项 | 结果 | 真实依据 |
| --- | --- | --- |
| Activity Search | PASS | `listActivitiesByCategory` / `searchActivities` 安全日志出现，模型返回真实展览与 `/activities/3` |
| Category | PASS | `listActivitiesByCategory` 真实执行，返回亲子活动与真实活动链接 |
| Detail | PASS | `getActivityDetail` 真实执行，活动 3 的地址、区域、时间与价格来自数据库 |
| Multi-turn Memory | PASS | 同一 `memoryId` 下“摄影展 → 它在哪里”正确指向活动 3；Redis key 存在且 TTL 为正 |
| Ticket | PASS | `getActivityTickets` 真实执行，返回真实票券、价格、规则与库存 |
| No Hallucination | PASS | “火星探险活动”返回“当前没有查询到相关活动”，没有编造地点、票价或库存 |
| Reservation Guide | PASS | 仅引导 `/activities/3` 使用既有预约流程；无 Reservation 写操作 |
| Activity Link | PASS | Vue 先 HTML 转义模型内容，再将 `/activities/{id}` 安全渲染为站内可点击链接 |
| Streaming | PASS | 真实 AI 请求均产生多个可见分块；原生 Tool Calling 路径稳定 HTTP 200 |
| Stop Generation | PASS | 浏览器真实流中 Stop 控件出现并触发既有 `AbortController.abort()` |
| New Conversation | PASS | 点击后消息数归零，`cityhub_ai_memory_id` 更换 |

安全日志仅记录 `CityHub AI tool invoked: <toolName>`，不记录 Tool 参数、查询结果、API Key、数据库密码或 Redis 密码。

## 运行环境

| 项目 | 结果 |
| --- | --- |
| CityHub MySQL | PASS：Docker `cityhub-phase3b-mysql`，宿主端口 3307，数据库 `cityhub` |
| Redis | PASS：`PING → PONG`；Chat Memory key 验证存在 |
| Core 8081 | PASS |
| Web 5173 | PASS |
| consultant 8084 | PASS：通过 `backend/consultant/run-local.ps1` 启动 |
| LLM API configuration | PRESENT：不记录值 |
| 正式模型 | `qwen3.7-flash` |

## 构建、页面与安全检查

| 检查 | 结果 |
| --- | --- |
| `backend/consultant: mvn clean compile -DskipTests` | PASS |
| `backend: mvn clean compile -DskipTests` | PASS（parent / core / consultant） |
| `web: npm run build` | PASS |
| `/`, `/activities`, `/community`, `/profile`, `/assistant` | 未发现本次依赖升级导致的前端构建回归；`/assistant` 经 Playwright 实测 |
| `git diff --check` | PASS |
| `.env` Git 忽略 | PASS |
| Git Secret 检查 | PASS：仅环境变量占位与公开配置文本，未发现真实 Key |

## 变更范围

- consultant Maven BOM 与 LangChain4j 兼容升级；
- Qwen Chat / Streaming 模型最小配置；
- 新版 AI Service 显式 Tool 装配；
- `/chat` 参数绑定与 UTF-8 URI 配置；
- 旧、未使用 Redis embedding store 自动配置的隔离。

未修改 README、Activity/Community/Reservation 业务模型、秒杀、Lua、Redisson、BlockingQueue、AI Tool 数量或 Phase 7 内容。

## Git

本报告和兼容修复将以 `fix: support Qwen streaming tool calls` 提交。`.env`、诊断日志及用户提供的 Phase6C/Phase6D 提示词均不纳入暂存；仅允许普通 push，禁止 force push。
