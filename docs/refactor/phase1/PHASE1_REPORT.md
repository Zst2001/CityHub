# Phase 1 基础工程基线治理报告

## 1. 本阶段结论

已完成本阶段允许范围内的基础工程基线治理：Maven 编译恢复、敏感配置环境变量化、SQL 使用关系梳理、工程卫生规则补充和报告交付。未进行领域、项目身份、秒杀或 AI 业务重构。

## 2. 编译修复

| 文件 | 原问题 | 修改 | 验证 |
|---|---|---|---|
| `backend/core/src/main/java/com/yjshz/utils/RedisConstants.java` | 导入不存在且未使用的 `org.omg.CORBA.PUBLIC_MEMBER`，现代 JDK 下 core 无法编译 | 删除该无效 import | `backend` 下 `mvn clean compile`：core SUCCESS |

该修改不改变 Redis key、业务逻辑或 API 行为。

## 3. 编译结果

| 模块 | 命令 | 结果 |
|---|---|---|
| Maven reactor（root + core + consultant） | `mvn clean compile`（工作目录 `backend`） | PASS：YJSHZ、YJSHZ Core、consultant 均 SUCCESS |
| 仓库根目录 | `mvn clean compile` | 不适用：根目录没有 POM；真实 Maven 根目录为 `backend` |

编译期间仍有 `ShopTypeServiceImpl` 未检查操作警告；该问题不阻断编译，且不属于本阶段业务治理范围，未修改。

## 4. 敏感配置修改

数据库密码从两个 application 配置中移除，并以 `DB_PASSWORD` 读取；DB 用户、URL 覆盖、Redis 主机/端口/密码也统一支持环境变量。core 的 `RedissonConfig` 读取 `spring.redis.*`，避免独立硬编码地址。新增 `.env.example` 仅含占位符，新增 `.gitignore` 防止 `.env` 和本地 profile 配置被提交。

详细内容见 [SENSITIVE_CONFIG_AUDIT.md](SENSITIVE_CONFIG_AUDIT.md)。

## 5. SQL 梳理

当前最接近 core 源码的 SQL 是 `backend/core/src/main/resources/db/yjshz.sql`。consultant 下两份 `hmdp.sql` SHA-256 完全相同，且未发现源码/配置/脚本的文件级运行时引用；它们标记为 legacy 重复副本，但因无法排除外部部署使用，本阶段未移动或删除。

详细内容见 [SQL_INVENTORY.md](SQL_INVENTORY.md)。

## 6. 工程卫生

- 新增 `.gitignore`：忽略 `target/`、构建二进制、日志、PID、IDE 文件和本地凭证/配置。
- 删除当前工作副本中的 Nginx 运行日志和 PID：`frontend/logs/access.log`、`error.log`、`nginx.pid`。这些是本地运行产物，曾包含请求路径等运行记录。
- 本次 `mvn clean compile` 会重新生成 `backend/*/target`；它们现由 `.gitignore` 排除。
- 当前副本没有 `.git` 目录，因此无法运行 `git status`、`git diff`、`git diff --check` 或确认历史 tracked 状态；这不是代码变更失败，而是工作副本缺少 Git 元数据。

## 7. 本阶段刻意未修改的内容

- README；
- package、artifactId、项目名和数据库名；
- Shop、ShopType、Voucher、SeckillVoucher、VoucherOrder、Blog、Follow 的领域模型、API、Mapper、Lua 与业务逻辑；
- 秒杀库存、JVM 队列、Redisson 下单或可靠消息链路；
- AI Tool、预约业务、RAG、`reservation` 表和优惠券订单手机号查询问题；
- Docker 完整环境与测试体系；
- 现有技术栈版本。

## 8. 仍然存在的已知问题

1. AI 预约 Mapper 依赖的 `reservation` 表未在现有 SQL 中提供。
2. AI 优惠券查询使用的 `tb_voucher_order.phone` 与主 SQL 表结构不一致。
3. SQL 初始化入口不唯一，consultant 有两份重复 legacy SQL。
4. Git 元数据不在当前工作副本，无法确认 Git 历史是否包含旧敏感配置，也无法执行索引级清理。
5. README 与实现不一致、黑马点评领域遗留、秒杀可靠性与 AI 业务完整性问题依旧存在，均按本阶段约束未处理。

详见 [known_database_issues.md](../known_database_issues.md)。

## 9. 下一阶段建议

下一阶段仅建议进行“项目身份去黑马化”：审查并统一项目名称、artifactId、package、Application、数据库工程命名和配置命名。该工作未在本阶段实施。
