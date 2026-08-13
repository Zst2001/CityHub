# Phase 7 CityHub 最终工程清理、部署与 GitHub 包装报告

## 1. 最终结论

`PHASE7: PASS`

本阶段没有新增业务能力。运行路径已完成旧语义清理、配置统一、Docker Compose/Nginx 部署和真实回归；项目处于功能冻结状态。

## 2. Git 基线

- 开始时分支：`main`，与 `origin/main` 对齐。
- Phase 6 最后提交：`78ce564 fix: support Qwen streaming tool calls`。
- 旧的用户提示词文件保持未纳入本次提交；`.env` 也始终被忽略。

## 3. 运行路径遗留清理

| 项目 | 结论 | 依据/处理 |
| --- | --- | --- |
| `Blog.shopId` | PASS | 已删除实体字段与 `saveBlog()` 的 `shopId=0` 兼容写入；活动动态只使用 `activityId`。 |
| `tb_blog.shop_id` | PASS | `cityhub_schema.sql` 已删除字段与 seed；现有开发 CityHub 数据库已执行 `ALTER TABLE tb_blog DROP COLUMN shop_id`。 |
| `like.lua` | PASS | 已确认 Java 仅保留废弃注释引用；删除脚本、加载器和废弃代码。当前点赞使用 Redis ZSet。 |
| RedisIdWorker 前缀 | PASS | ReservationOrder ID 改用 `reservation-order`；时间戳与 sequence 算法未变。 |
| Shop/Voucher/YJSHZ/雅鉴生活志/redis_project | PASS | 运行代码、SQL seed、部署配置与 Web 不再保留旧业务语义；`docs/legacy` 与历史阶段文档作为归档保留。 |
| 旧 Windows Nginx | PASS | 删除无运行入口的 `frontend/` 二进制分发目录；正式部署改由 `web/` + Linux Nginx。 |
| Consultant RAG/Embedding | PASS | 删除未被运行路径使用的 easy-rag、PDF parser、Redis embedding starter 和 embedding 配置；保留 LangChain4j、ActivityTool、Redis Chat Memory、Qwen streaming tool calling。 |

预约 Lua、Redis 预扣库存、`ArrayBlockingQueue`、Redisson、RedisIdWorker 与异步消费者均保留，未被重构。

## 4. 配置与本地启动

- `.env.example` 统一覆盖 MySQL/Redis/DashScope/模型变量，全部为安全占位值。
- 根 `.env` 已被 `.gitignore` 忽略；本地实际值未写入文档、Git 或镜像。
- 保留 `backend/consultant/run-local.ps1`，并新增同等最小化的 `backend/core/run-local.ps1`，两者从根 `.env` 加载。
- Docker 中所有配置经 Compose 环境变量注入，不依赖 Windows PowerShell 启动脚本。

## 5. Docker 与 Nginx

根 `docker-compose.yml` 定义五个服务：

| 服务 | 容器端口 | 说明 |
| --- | --- | --- |
| `cityhub-mysql` | 3306 | MySQL 8.0，命名 volume，初始化 `cityhub_schema.sql`，healthcheck。 |
| `cityhub-redis` | 6379 | Redis 7，AOF 与命名 volume，healthcheck。 |
| `cityhub-core` | 8081 | Core API；为现有 MyBatis-Plus 与 Java 17 的反射兼容，加入最小 `--add-opens` 参数。 |
| `cityhub-consultant` | 8084 | Qwen/Tool Calling/Redis Chat Memory。 |
| `cityhub-web` | 80（宿主 8088） | Vue 静态产物与 Nginx。 |

Nginx 已验证：

- `try_files $uri $uri/ /index.html` 支持 SPA 深链接刷新；
- `/api/` 去前缀代理到 Core；
- `/ai-api/` 去前缀代理到 Consultant；
- AI 代理设置 `proxy_buffering off`、`proxy_cache off`、HTTP/1.1 和 300 秒读取超时，流式输出未被缓冲。

Docker 构建：Web 仍在镜像中 `npm ci && npm run build`；Java 镜像消费先由宿主机 Maven 验证生成的 jar，以规避 Docker builder 访问 Maven Central 的网络不稳定性。`docker compose build` 已成功。

## 6. Docker/Nginx 真实 E2E

以下均通过 `http://127.0.0.1:8088`（Nginx 入口）完成，而非绕过代理：

| 验收项 | 结果 |
| --- | --- |
| Home / Activity List / Search / Detail / Ticket | PASS |
| Login：验证码、login、`/user/me`、`authorization: <token>` | PASS |
| Reservation Normal | PASS |
| Reservation Duplicate | PASS |
| Reservation No Stock | PASS |
| Community Hot / Activity Blog | PASS |
| Follow → 发布 Activity Blog → Following Feed | PASS |
| Like / Unlike | PASS |
| Activity cache 查询 | PASS |
| AI Activity Search | PASS |
| AI Multi-turn Redis Memory | PASS |
| AI Ticket | PASS |
| AI No Hallucination（火星活动） | PASS |
| AI Reservation Guide（`/activities/3`） | PASS |
| AI Streaming through Nginx | PASS |
| AI Stop / New Conversation | PASS |
| 1440px / 390px | PASS |
| Vue 深链接刷新：`/activities`、`/activities/3`、`/community`、`/profile`、`/assistant` | PASS |

本地 Maven 多类集成测试曾暴露共享 MySQL/Redis 下多个 Spring 上下文并行竞争的问题，表现为异步订单/Follow 清理互相干扰；这不影响上述 Compose 隔离、串行真实回归。最终报告以实际 Docker/Nginx 回归结果为准。

## 7. 构建与安全检查

| 检查 | 结果 |
| --- | --- |
| `mvn -f backend/pom.xml clean compile -DskipTests` | PASS |
| `mvn -f backend/consultant/pom.xml compile -DskipTests` | PASS |
| `npm --prefix web run build` | PASS（仅 Vite 大 bundle 提示，无构建失败） |
| `docker compose config --quiet` | PASS |
| `docker compose build` | PASS |
| `git diff --check` | PASS |
| `.env` ignore | PASS |
| Secret scan | PASS；仅环境变量名和占位符，没有真实 API Key/数据库/Redis 密码。 |

## 8. README 与 GitHub 展示

- 根 `README.md` 已在验证完成后重写，只描述真实的技术栈、部署方式、功能和已知取舍；已删除 Kafka、RabbitMQ、Caffeine 等不实表述。
- 添加四张 Docker/Nginx 实测桌面截图：首页、活动详情、社区和 AI 顾问，位于 `docs/images/`。
- 删除已过期的 `backend/README.md`，避免同仓库出现相互矛盾的旧项目说明。

## 9. 已知限制

- 限量预约的异步落库是单 JVM `BlockingQueue`，没有 MQ/Redis Stream，不适合作为多实例生产方案。
- AI 依赖用户本地配置的 DashScope API Key；缺失时 Consultant 无法完成模型调用。
- Docker Java 镜像依赖宿主机先生成 jar；这是当前网络环境下避免 Docker builder Maven 网络波动的明确工程取舍。

## 10. Git

- Phase 7 主提交：`35b8ee704036a646cd3fc34bc74bd4a799cd95ba`，信息为 `chore: finalize CityHub for deployment`；报告与截图精简提交：`22884fdf89c38e452cc3ab0dfb6ea6a578022d0c`。
- 已正常推送到 `origin/main`，未使用 force push。
- 除三个用户提供且未追踪的历史提示词文件外，工作树干净；它们不属于本阶段，也未包含在提交中。
