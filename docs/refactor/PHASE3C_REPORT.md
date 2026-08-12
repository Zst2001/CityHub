# Phase 3C：CityHub 限量活动预约 / 秒杀链路验证报告

## 结论

Phase 3C 通过。已在独立临时 MySQL 8（本机 3307）和真实 Redis 容器上执行真实 Spring Boot 集成测试；测试未 Mock Redis、Lua、Redisson、队列或 MySQL。

本阶段只修复了真实运行阻断点：Redis 秒杀状态初始化、Lua 一人一单 Key 分隔符、AOP 代理暴露及其运行依赖。未改造既有 RedisIdWorker、Lua 预扣、ArrayBlockingQueue、单线程 Consumer、Redisson、Blog/Follow、AI、前端或 README。

## 秒杀真实链路审计

| 项目 | 真实实现 |
| --- | --- |
| API | `POST /reservation/seckill/{ticketId}` |
| 登录 | `/reservation/**` 未在 `MvcConfig` 白名单中；`LoginInterceptor` 对无 Token 请求返回 401 |
| 用户上下文 | `RefreshTokenInterceptor` 从 `login:token:{token}` Hash 载入 `UserHolder` |
| 订单 ID | `RedisIDWorker.nextId("seckill:stock:")` 生成，`ReservationOrder.id` 使用该值，不使用 MySQL 自增 |
| Lua | `backend/core/src/main/resources/seckill.lua`；参数为 `ticketId`、`userId` |
| Redis 库存 | `seckill:stock:{ticketId}` |
| 一人一单 | `seckill:order:{ticketId}` Set，成员为 `userId` |
| Lua 返回 | `0` 成功、`1` 库存不足、`2` 重复预约 |
| 队列 | `ArrayBlockingQueue<ReservationOrder>`，容量 `1,048,576`，生产者使用 `add()` |
| Consumer | `@PostConstruct` 提交到单线程 `SECKILL_ORDER_EXECUTOR`；`take()` 后消费。循环内捕获异常、记录错误并继续运行 |
| Redisson | `getLock("order:{userId}")`，用户粒度；`tryLock()` 失败直接返回，`finally` 中 `unlock()` |
| MySQL 扣减 | `UPDATE tb_seckill_ticket SET stock = stock - 1 WHERE ticket_id = ? AND stock > 0`，仅成功时插入订单 |
| MySQL 防重 | `tb_reservation_order` 的 `UNIQUE(user_id, ticket_id)`；Consumer 另有查询防重 |

当前入口未校验 `beginTime/endTime`，因此本阶段没有伪造时间窗口验证，也未擅自新增状态机。

## 运行期修复

1. 新增 `SeckillStockInitializer`：启动时由 `tb_seckill_ticket` 回填 Redis 库存 Key，并由已有 `tb_reservation_order` 回填一人一单 Set。这使 Lua 的状态与当前开发数据库一致。
2. 修正 Lua Set Key 为 `seckill:order:{ticketId}`，避免无分隔符的 Key 语义不清。
3. `CityHubApplication` 启用 `@EnableAspectJAutoProxy(exposeProxy = true)`；现有 `AopContext.currentProxy()` 可在 HTTP 秒杀调用中获得事务代理。
4. 增加项目所缺失的 `aspectjweaver` 依赖，仅用于上述既有 Spring AOP 调用链。

## 真实集成测试

新增 `SeckillReservationIntegrationTest`，以随机端口启动 Spring Boot，通过真实 `/user/login` 获取 Token（测试仅在 Redis 写入验证码），再通过 HTTP 调用秒杀接口。测试完成后清理订单与相关 Redis Key，并将临时 schema 重置回 Seed 状态。

执行：

```text
mvn -pl core test -Dtest=SeckillReservationIntegrationTest
```

测试结果：1 个测试，0 failures，0 errors。

| 场景 | 实际结果 | 状态 |
| --- | --- | --- |
| 未登录 | API 返回 HTTP 401 | PASS |
| 正常预约 | Redis 3→2，MySQL 3→2，异步落 1 条订单，返回 RedisIdWorker 订单 ID | PASS |
| 重复预约 | 同一 Token 第二次被 Lua 拒绝，DB `user_id + ticket_id` 仍为 1 | PASS |
| 库存不足 | 初始 2，3 个用户请求：2 成功、1 失败；Redis/MySQL 均为 0 | PASS |
| 多用户并发 | 初始 10，20 个不同用户并发：10 成功、10 失败；Redis/MySQL 均为 0，订单 10 条 | PASS |
| 同用户并发 | 初始 10，同一 Token 并发 10 次：仅 1 个成功资格、最终 1 条订单；Redis/MySQL 均为 9 | PASS |
| 超卖 / 负库存 | 各场景中 Redis、MySQL 均未小于 0；订单数未超过初始库存 | PASS |
| 重复订单 | 每轮检查 `GROUP BY user_id, ticket_id HAVING COUNT(*) > 1`，0 行 | PASS |

测试日志还确认了：Lua 成功后由 `ArrayBlockingQueue` 进入单线程 Consumer，Consumer 使用 Redisson 锁后执行 MySQL 条件扣减与插入；`icr:seckill:stock::{yyyy:MM:dd}` ID 序列 Key 被创建，证明 RedisIdWorker 实际执行。

## Redis 初始化与清理

启动初始化器将每个 `tb_seckill_ticket.stock` 写入 `seckill:stock:{ticketId}`，并按订单表重建 `seckill:order:{ticketId}` 成员集合。每个测试场景仅删除测试票券 1 的库存 Key 与 Set、仅删除该票券订单、仅重置该票券 MySQL 库存；没有清空 Redis 或数据库全量数据。

结束后临时数据库已重置：票券 1 MySQL 库存为 100、订单数为 0；Redis 测试秒杀 Key 已删除。

## 已知限制

- `ArrayBlockingQueue` 是 JVM 内存队列；进程崩溃时尚未消费的订单消息会丢失。
- Lua 已预扣 Redis 库存后，如 MySQL 扣减或插入失败，当前仅记录错误并保持 Consumer 存活；未实现 Redis 自动补偿、重试队列或可靠消息机制。
- 队列使用 `add()`：队列满时会抛异常，当前不会静默丢失，但也没有补偿。
- 当前版本未在秒杀入口校验 `beginTime/endTime`。

这些限制按本阶段约束保留，未升级为 Redis Stream 或 MQ。

## 工程验证

- `mvn -pl core test-compile -DskipTests`：通过。
- `mvn -pl core test -Dtest=SeckillReservationIntegrationTest`：通过。
- Java 17 + 当前 MyBatis-Plus 3.4.3 使用 `--add-opens=java.base/java.lang.invoke=ALL-UNNAMED` 运行；未升级依赖版本。
- Spring Boot 集成测试上下文成功启动 Tomcat、MySQL、Redis、Redisson 和异步 Consumer。
- 暂存/提交前将再次执行 `mvn clean compile`、`git diff --check` 与敏感信息检查。

## Git 基线

- Phase 3A：`e0cbeaf feat: establish CityHub activity reservation domain`
- Phase 3B：`3354b2d feat: verify CityHub activity cache flow`（当前本地相对 `origin/main` 领先，上一阶段 GitHub 网络推送未完成）
- Phase 3C 提交与推送结果将在本报告提交后的最终交付中补充。
