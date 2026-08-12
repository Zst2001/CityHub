# Phase 3A-R：CityHub 轻量领域重置与迁移报告

## 1. 阶段结论

复杂的 Venue / ActivitySession 方案已在首次 Git 提交前完成撤回，CityHub Core 的正式领域基线已固定为：

```text
ActivityCategory -> Activity -> Ticket -> SeckillTicket -> ReservationOrder(ticketId)
```

本轮采用同构轻量迁移：保留原有成熟的缓存、Lua、一人一单、Redisson、RedisIDWorker、BlockingQueue 异步订单和 Redis Token 登录实现，仅将核心业务语义、类型、表名、路由、字段名和 Redis Key 从点评场景迁移到活动预约场景。

按用户要求，本轮只生成本文一份阶段报告；重置计划位于 `docs/refactor/phase3a_reset/RESET_PLAN.md`，用于记录撤回范围，不作为额外阶段报告。

## 2. 重置结果

已撤回以下复杂领域运行时代码和 DDL：

- `Venue` 的实体、Mapper、Service、Controller 和 VO；
- `ActivitySession` 的实体、Mapper、Service 和 VO；
- `/venues`、`/activities`、`/activities/{id}/sessions` 查询链路；
- `venue`、`activity_session` 表；
- `ReservationOrder.sessionId` / `session_id` 及 `(user_id, session_id)` 唯一约束；
- 仅为上述设计建立的 `cityhub_domain_v1.sql`、`cityhub_domain_seed.sql`。

静态扫描结果：`backend/core/src/main` 中没有 `Venue`、`ActivitySession`、`sessionId`、`session_id`、`/venues` 或 `/activities/{id}/sessions` 残留。

## 3. 领域、类与 API 映射

| 原点评语义 | CityHub 语义 | Core 类型 / API | 关键字段迁移 |
| --- | --- | --- | --- |
| ShopType | ActivityCategory | `ActivityCategory`、`/activity-category` | 原分类字段保留：`id`、`name`、`icon`、`sort`、时间字段 |
| Shop | Activity | `Activity`、`/activity` | `typeId -> categoryId`，`name -> title`；保留图片、区域、地址、坐标、参考价格、参与数、评论数、评分、活动时间展示字段 |
| Voucher | Ticket | `Ticket`、`/ticket` | `shopId -> activityId`；保留标题、规则、价格、类型、状态和秒杀临时字段 |
| SeckillVoucher | SeckillTicket | `SeckillTicket` | `voucherId -> ticketId`；保留库存、起止时间和时间字段 |
| VoucherOrder | ReservationOrder | `ReservationOrder`、`/reservation` | `voucherId -> ticketId`；保留现有 `payType`、`status`、时间字段 |

路由已同步迁移：`/shop-type -> /activity-category`、`/shop -> /activity`、`/voucher -> /ticket`、`/voucher-order -> /reservation`。活动、分类和票券公开读取路径仍在既有登录拦截器排除列表中；预约秒杀写接口没有被放开。

## 4. 数据库与初始化数据

`backend/core/src/main/resources/application.yaml` 的默认连接已统一为 `cityhub`。`backend/core/src/main/resources/db/cityhub_schema.sql` 已重建为适用于全新开发库的初始化脚本，核心表为：

```text
tb_activity_category
tb_activity
tb_ticket
tb_seckill_ticket
tb_reservation_order
```

其中 `tb_reservation_order` 以 `user_id + ticket_id` 建立联合唯一键。初始化 SQL 同时保留现有登录和社区代码真实依赖的 `tb_user`、`tb_user_info`、`tb_blog`、`tb_blog_comments`、`tb_follow`，并提供 8 个活动分类、5 个活动、5 个预约凭证和 5 条限量凭证示例数据。

数据库**未实际执行**：本地没有可确认安全写入的 MySQL 目标。本阶段仅完成脚本与实体的一致性检查；需要在开发库 `cityhub` 中手工执行 `cityhub_schema.sql`。

## 5. 核心技术保留检查

| 能力 | 迁移后真实实现 | 算法是否重写 |
| --- | --- | --- |
| 缓存穿透 | `CacheClient.queryWithPassThrough` 仍在；DB miss 写空值和 TTL，Activity 改用 `cache:activity:` | 否 |
| 缓存击穿 | `CacheClient.queryWithLogicalExpire` 仍使用逻辑过期、`lock:activity:`、独立线程池异步重建和旧值返回 | 否 |
| 缓存雪崩 | 原实现没有随机 TTL；本阶段未新增 | 否 |
| 缓存一致性 | `ActivityServiceImpl.update` 保持“更新 MySQL 后删除 Redis”；事务注解保留 | 否 |
| Lua | `seckill.lua` 仍按库存判断、一人一单判断、预扣库存执行；仅 `voucherId -> ticketId` | 否 |
| 一人一单 | 服务查询与数据库唯一键均从 `user_id + voucher_id` 迁移到 `user_id + ticket_id` | 否 |
| Redisson | 仍使用 `RLock`、`tryLock`、`finally unlock`，锁粒度仍为 `order:{userId}` | 否 |
| RedisIDWorker | 仍为 `ReservationOrder.id` 生成 ID | 否 |
| 异步秒杀 | 仍为 `ArrayBlockingQueue` + 单线程执行器，消息载体改为 `ReservationOrder` | 否 |
| Redis Stream | 当前代码不存在，未新增 | 不适用 |
| Redis Token 登录 | `LOGIN_USER_KEY`、RefreshTokenInterceptor、LoginInterceptor、UserHolder 均未修改 | 否 |

## 6. Maven 与 Spring 验证

执行命令：

```text
cd backend && mvn clean compile
```

结果：`BUILD SUCCESS`，`CityHub`、`CityHub Core`、`CityHub AI` 全部成功。Core 仍存在 `ActivityCategoryServiceImpl` 的未检查操作编译警告，不影响本次编译。

Spring Context **未验证**：未提供可确认安全的 MySQL/Redis 运行环境，未启动应用，未虚报启动成功。

## 7. 边界与已知问题

- Blog / Follow、登录 Token、前端视觉、README 均未修改。
- AI 深度业务未主动迁移；Consultant 模块仍保留旧 Shop/Voucher 类型与 SQL，以避免在本阶段重写 AI Tool/RAG 语义。Core 与 Consultant 均可编译。
- 当前活动缓存逻辑保持原有实现，包括既有的逻辑过期预热辅助方法；本阶段不额外修复或重新设计算法。
- 由于本仓库是首次提交前的重置，已在仓库外完成可恢复备份：`F:\JavaProject\CityHub_pre_phase3_reset_20260812.zip`。

## 8. 后续建议

下一阶段仅建议开展“活动查询与缓存业务适配验证”：在可用开发 MySQL/Redis 环境中执行初始化 SQL、启动应用、验证分类/活动/票券接口、预热活动缓存，并对缓存穿透、击穿与一致性做可观测验证。不要在该阶段引入 Venue、ActivitySession、支付、MQ 或新的预约状态机。
