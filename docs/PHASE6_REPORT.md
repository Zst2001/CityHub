# Phase 6 CityHub AI 顾问：最终联调验收报告

## 最终结论

`REAL_LLM_INTEGRATION: BLOCKED_BY_STREAMING_TOOL_CALL_COMPATIBILITY`

Phase 6C 已定位并修正此前连接错实例的问题：CityHub 的真实数据在 Docker MySQL `cityhub-phase3b-mysql` 的宿主端口 **3307**，而不是本机 `mysqld` 监听的 3306。顾问服务可用现有 CityHub 应用账号连接 3307 并启动至 8084，Redis 与 LLM 环境变量也均可用。

但对真实 `qwen3.7-flash` 进行流式 Tool Calling 时，当前固定的 LangChain4j `1.0.1-beta6` OpenAI starter 在解析提供方返回的流式 Tool Call 参数处抛出 `JsonParseException`，`/chat` 返回 HTTP 500。因此不能将活动搜索、分类、详情、票券、多轮记忆、反幻觉、预约引导、Stop 标记为通过。本阶段未升级依赖、未切换模型、未伪造验收结果。

## MySQL 实例定位

| 项目 | 结论 | 依据 |
| --- | --- | --- |
| Docker MySQL | PASS | `cityhub-phase3b-mysql`（MySQL 8.0.34）运行中 |
| 宿主端口 | 3307 | `3306/tcp -> 0.0.0.0:3307` |
| 3306 | 非 CityHub 容器 | 本机 `mysqld` 监听 |
| 3307 | CityHub Docker 实例 | Docker backend / WSL 转发监听 |
| 数据库 | `cityhub` | 容器环境标记 `MYSQL_DATABASE=cityhub`，且 schema 存在 |
| 核心表与数据 | PASS | `tb_activity`、`tb_ticket`、`tb_reservation_order` 存在；活动 12 条、票券 12 条 |
| 凭据来源 | located | Docker 容器环境包含 root credential 配置；未输出、未提交任何值 |

已严格在**确认 3307 为正确实例之后**，按 `123 → 空密码 → 123456` 尝试 root 认证；三次均失败后立即停止，未猜测其他密码、未重置账号、未删除容器或卷。

现有 `cityhub` 应用账号可经 `127.0.0.1:3307` 只读访问 CityHub 表。本地被忽略的 `.env` 已调整为此已验证连接（`DB_URL` 指向 3307；账号及密码未记录在版本库）。

**Phase 6B 根因：** `DB_URL` 指向了错误的 MySQL 实例/端口（3306）；同时 Docker root 认证不属于允许的三个候选值。顾问运行不需要为此重置 root，已使用现有最小权限应用账号。

## 配置与服务

| 检查项 | 结果 | 说明 |
| --- | --- | --- |
| `.env` 由 `run-local.ps1` 加载 | PASS | 仅载入当前 PowerShell 子进程，未打印 Secret |
| `.env` Git 忽略 | PASS | `.gitignore` 的 `.env` 规则命中 |
| `ALIYUNCS_API_KEY` | PRESENT | 仅检查变量存在，不读取或输出值 |
| LLM 模型 | PASS | chat / streaming 均使用 `LLM_MODEL_NAME=qwen3.7-flash` |
| LLM API configuration | PRESENT | DashScope OpenAI-compatible endpoint 已配置 |
| Redis | PASS | 使用现有本地配置 `PING → PONG` |
| Core 8081 | PASS | 联调时处于监听状态 |
| Web 5173 | PASS | 联调时处于监听状态 |
| consultant 8084 | PASS（启动） | 经 `backend/consultant/run-local.ps1` 实际启动并监听 |

## 真实 Agent 验收

| 检查项 | 结果 | 真实证据 |
| --- | --- | --- |
| Activity Search | FAIL | 首次真实请求出现流式响应；后续 Tool Call 解析异常导致 HTTP 500 |
| Category | FAIL | HTTP 500，未伪造分类结果 |
| Detail | FAIL | HTTP 500，未伪造详情结果 |
| Multi-turn Memory | BLOCKED | Redis ChatMemory 代码和 Redis 均可用，但无法完成真实 Tool Call 首轮 |
| Ticket | FAIL | HTTP 500，未伪造票券/库存结果 |
| No Hallucination | BLOCKED | System Prompt 与空结果 Tool 逻辑已实现，但真实会话无法完成 |
| Reservation Guide | BLOCKED | 系统提示仅引导 `/activities/{id}`，不具备下单 Tool；真实回复未能完成 |
| Activity Link | PASS（前端实现） | `/activities/{id}` 已以安全转义后链接渲染；实际模型回复受上述阻断 |
| Streaming transport | PASS | 真实 `/chat` 首次响应分为多个 HTTP 流式分块 |
| Streaming Tool Calling | FAIL | 当前 SDK 在解析 Tool Call delta 时异常 |
| Stop Generation | BLOCKED | `AbortController.abort()` 已存在，但无法完成稳定的真实长响应回归 |
| New Conversation | PASS（前端实现） | 清空消息并生成新的 `cityhub_ai_memory_id` |

### 已定位的兼容性错误

真实请求的服务端日志显示：

```text
JsonParseException: Unexpected character ('('): was expecting comma
... ChatCompletionChoice.Builder["delta"]
```

异常出现在 LangChain4j OpenAI streaming SSE 对 Tool Call delta 的解析阶段，发生在真实提供方响应之后，而非 DataSource、Redis 或 API Key 认证阶段。官方 DashScope 文档说明 Qwen3.7 Flash 是混合思考模型，并提供 `enable_thinking` 控制；当前项目固定的 `langchain4j-open-ai-spring-boot-starter:1.0.1-beta6` 配置没有可安全透传该提供方扩展参数的现有配置项。本阶段受“不升级现有技术栈”约束，未进行依赖升级或模型参数重构。

## 本次最小代码调整

- `ActivityTool`：新增仅记录 Tool 名称的安全日志；不记录用户参数、SQL 结果、密码或 API Key。
- `AssistantView.vue`：将模型输出中的 `/activities/{id}` 经过 HTML 转义后渲染为站内链接，避免把未可信模型内容直接作为 HTML。

未修改 README、秒杀、Lua、Redisson、BlockingQueue、Blog/Follow 或 AI 功能范围之外的业务。

## 构建与安全验证

| 命令/检查 | 结果 |
| --- | --- |
| `backend/consultant: mvn clean compile -DskipTests` | PASS |
| `backend: mvn clean compile -DskipTests` | PASS（parent / core / consultant） |
| `web: npm run build` | PASS |
| `git diff --check` | PASS |
| `git check-ignore -v .env` | PASS |
| 已跟踪文件 API Key 检查 | PASS：仅变量占位/示例，不含真实值 |
| 已跟踪文件常见密钥前缀检查 | 已检查；命中为非 Secret 文本，未发现真实 API Key |

## 后续处理建议（不属于本阶段）

若要解除 `REAL_LLM_INTEGRATION` 阻断，应在获得用户授权后，针对 qwen3.7-flash 的流式 Function Calling 选择与 DashScope 响应格式兼容的 LangChain4j 版本或专用客户端，并重新执行本报告中的所有真实 Agent 验收项。不得仅凭静态代码将这些项改为 PASS。

## Git

- 分支：`main`
- Phase 6C 验证提交：`test: verify CityHub AI assistant integration`
- `.env` 未被暂存或提交；用户提供的 Phase6C 提示词保持未跟踪。
- 推送：首次普通 `git push origin main` 因 GitHub 连接被重置失败；未执行 force push。后续仅允许重试普通推送。
