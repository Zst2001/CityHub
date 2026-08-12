# Phase 2 CityHub 项目身份规范化报告

## 结论

本阶段已将工程身份统一为 **CityHub - 城市活动发现与预约平台**。Maven 坐标、Java 根 package、启动类、Spring 应用名、纯工程目录标识和可确认无运行时引用的 legacy SQL 已完成迁移；未修改 README、业务领域、数据库业务结构、API 路由、Redis 业务 Key、秒杀或 AI 工具业务逻辑。

## 执行前身份扫描与分类

| 命中类别 | 修改前位置 | 分类 | 本阶段处理 |
|---|---|---|---|
| `com.yjshz` | core 全部 Java 包、consultant 全部 Java 包、YAML type alias、Mapper XML | 工程身份 | 迁移为 `com.cityhub` |
| `com.itheima` | `backend/consultant/pom.xml` groupId | 工程身份 | 改为 `com.cityhub` |
| `yjshz` / `YJSHZ` | 父/子 Maven artifact、name、core Application、Spring name、上传/Nginx 工程目录、SQL 文件名/元数据 | 工程身份 | 迁移为 CityHub/cityhub；数据库连接名未改 |
| `hmdp.sql` | consultant resources 下两份相同 SQL | Legacy 资料 | 核验后归档单份，删除重复副本 |
| Shop、Voucher、Blog、Follow、`tb_*`、`/shop` 等 | Java 业务代码、SQL、路由、Redis Key | 业务领域 | 保持不变 |
| AI 中商家/优惠券/预约指令 | system prompt / tools | AI 业务 | 仅替换工程品牌文本，未改变工具或业务范围 |

## Maven 身份迁移

| 项目 | 修改前 | 修改后 |
|---|---|---|
| parent | `com.yjshz:yjshz`，名称 `YJSHZ` | `com.cityhub:cityhub-parent`，名称 `CityHub` |
| core | `yjshz-core` / `YJSHZ Core` | `cityhub-core` / `CityHub Core` |
| AI module | `com.itheima:consultant` / `consultant` | `com.cityhub:cityhub-ai` / `CityHub AI` |

目录仍保留 `backend/core` 和 `backend/consultant`：没有明确收益足以抵消 Maven/IDE/启动脚本风险，符合本阶段低风险策略。

### 验证

Maven 坐标迁移后，在 `backend` 执行 `mvn clean compile` 成功：Reactor 显示 CityHub、CityHub Core、CityHub AI 全部 SUCCESS。

## Java package、Application 与配置迁移

| 项目 | 修改前 | 修改后 |
|---|---|---|
| core Java 根 package | `com.yjshz` | `com.cityhub` |
| AI Java 根 package | `com.yjshz.consultant` | `com.cityhub.consultant` |
| core 启动类 | `YJSHZApplication` | `CityHubApplication` |
| AI 启动类 | `ConsultantApplication` | `CityHubAiApplication` |
| Mapper/组件扫描 | `com.yjshz...` | `com.cityhub...` |
| MyBatis XML/type aliases | `com.yjshz...` | `com.cityhub...` |
| Spring application name | `yjshz-core` | `cityhub-core`；AI 新增 `cityhub-ai` |

`backend/core/src/main/java/com/cityhub/CityHubApplication.java` 的 `@MapperScan` 和 `backend/consultant/src/main/java/com/cityhub/consultant/CityHubAiApplication.java` 的 `scanBasePackages` / `@MapperScan` 已同步更新。

### 验证

package 与 Application 迁移后执行 `mvn clean compile` 成功；随后 legacy SQL 归档和工程目录标识调整后再次执行，仍全部 SUCCESS。

## 工程品牌与 legacy 清理

- `SystemConstants.IMAGE_UPLOAD_DIR` 与 `frontend/conf/nginx.conf` 的纯工程资源目录由 `yjshz` 改为 `cityhub`；未触碰 API 反代配置。
- consultant AI 演示页面 title/品牌提示和 `system.txt` 的提供方名称由旧品牌改为 CityHub；商家、优惠券、预约等业务说明未改。
- consultant 的两份 `hmdp.sql` 在 Phase 1 已记录为 SHA-256 相同；本阶段再次确认 Java、YAML、POM、启动脚本没有文件级引用，且工程未使用 Flyway/Liquibase。保留副本归档到 `docs/legacy/sql/legacy_hmdp_schema.sql`，移除 `backend/consultant/src/main/resources/content/hmdp.sql` 重复副本。
- core 初始化 SQL 文件从 `yjshz.sql` 改名为 `cityhub_schema.sql`，没有变更业务表/字段/数据；其头部 Source Schema 展示名称改为 `cityhub`。运行时数据源仍为原 `redis_project`，按约束未改。

## 最终残留检查

在排除 README、历史审计报告和 `docs/legacy` 归档资料后，运行时/构建源中不再有 `com.yjshz`、`com.itheima`、YJSHZ、hmdp 等工程身份命中。保留的 Shop/Voucher/Blog/Follow、`tb_*`、`/shop`、`/voucher`、`/blog`、Redis `seckill`/`blog` key 都属于本阶段禁止修改的业务语义。

## 验证结果

| 步骤 | 命令 | 结果 |
|---|---|---|
| 基线 | `backend` 下 `mvn clean compile` | PASS |
| Maven 坐标迁移后 | `backend` 下 `mvn clean compile` | PASS |
| package/Application 迁移后 | `backend` 下 `mvn clean compile` | PASS |
| legacy/目录标识调整后 | `backend` 下 `mvn clean compile` | PASS |

编译仍提示 `ShopTypeController` 存在未检查操作警告；不阻断构建，且不是本阶段命名迁移引入的问题，未处理。

当前工作副本不含 `.git`，`git status` 与 `git diff --check` 分别返回 128 和 129，无法获得 Git diff；已用修改文件范围、旧身份全文搜索、受保护业务标识搜索和多次 Maven compile 代替差异校验。

## 本阶段明确未修改

- README 文件；
- Shop、ShopType、Voucher、SeckillVoucher、VoucherOrder、Blog、BlogComments、Follow、User 的业务模型与字段；
- 数据库业务表、数据库连接 schema、SQL 业务内容；
- `/shop`、`/voucher`、`/voucher-order`、`/blog`、`/follow` 等 API 路由；
- Redis 业务 Key、Lua、秒杀链路；
- AI Tool 的参数、SQL 查询、预约/RAG 业务逻辑；
- 依赖与技术栈版本。

## 后续建议

Phase 3 再进行领域模型与数据库重构；届时统一设计场馆、活动、场次、预约订单和正式数据库迁移入口，同时处理 Phase 1 已记录的预约表与订单查询字段不一致问题。
