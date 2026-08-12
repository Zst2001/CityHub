# Codex 执行任务：Phase 3C 限量活动预约 / 秒杀链路真实运行验证

## 一、阶段背景

项目：**CityHub - 城市活动发现与预约平台**

当前已完成：
- Phase 1：基础工程治理
- Phase 2：CityHub 工程身份规范化
- Phase 3A-R：轻量领域迁移
- Phase 3B：Activity 查询与 Redis 缓存真实验证

当前正式领域：

```text
ActivityCategory -> Activity -> Ticket -> SeckillTicket -> ReservationOrder(ticketId)
```

本阶段只验证并修通限量活动预约链：

```text
Client
 -> 登录 Token
 -> ReservationOrderController
 -> seckillTicket(ticketId)
 -> RedisIdWorker
 -> Lua
    - 判断库存
    - 判断一人一单
    - Redis 预扣库存
    - 记录用户
 -> ReservationOrder
 -> ArrayBlockingQueue
 -> 单线程异步消费者
 -> Redisson 用户锁
 -> MySQL 扣库存
 -> INSERT tb_reservation_order
```

目标不是重新设计秒杀架构，而是证明当前实现真实可运行、并发下不超卖、不重复下单。

---

## 二、核心约束

### 必须保留

```text
RedisIdWorker
Lua
一人一单
Redisson
ArrayBlockingQueue
单线程异步消费者
Redis Token 登录
```

### 禁止新增或重构

```text
Venue
ActivitySession
支付
退款
核销
复杂订单状态机
Redis Stream
Kafka
RabbitMQ
RocketMQ
JMeter 大规模压测
Blog / Follow 重构
AI 重构
前端视觉重构
Docker 扩展
README 重写
依赖大版本升级
```

本阶段原则：

> 70% 运行验证 + 20% 修真实 Bug + 10% 测试辅助，禁止大规模重构。

---

# 三、任务 0：Git 基线检查

仓库：

```text
https://github.com/Zst2001/CityHub
```

先执行：

```bash
git status --short
git log --oneline -n 10
git branch --show-current
git remote -v
```

已知 Phase 3A 应至少存在：

```text
e0cbeaf feat: establish CityHub activity reservation domain
```

检查 Phase 3B 是否已有类似：

```text
feat: verify CityHub activity cache flow
```

的 commit。

## 如果 Phase 3B 已提交并 push

直接继续 Phase 3C。

## 如果 Phase 3B 尚未提交

先执行：

```bash
cd backend
mvn clean compile
cd ..
git status --short
git diff --check
git add .
git diff --cached --check
git diff --cached --stat
```

确认没有：

```text
.env
真实 DB 密码
真实 Redis 密码
真实 API Key
target/
logs/
测试 Token
临时数据库文件
```

然后：

```bash
git commit -m "feat: verify CityHub activity cache flow"
git push
```

禁止 force push。

---

# 四、任务 1：审计当前秒杀真实实现

修改代码之前完整阅读：

```text
ReservationOrderController
ReservationOrderService
ReservationOrderServiceImpl

Ticket
SeckillTicket
ReservationOrder

TicketMapper
SeckillTicketMapper
ReservationOrderMapper

RedisConstants
RedisIdWorker
RedissonConfig

seckill.lua
BlockingQueue 定义
异步消费者启动逻辑

LoginInterceptor
RefreshTokenInterceptor
UserHolder
```

以及相关：

```text
Mapper XML
cityhub_schema.sql
application.yaml
```

创建：

```text
docs/refactor/phase3c/SECKILL_FLOW_AUDIT.md
```

必须基于真实代码回答：

1. 秒杀 API 路径是什么？
2. 是否要求登录？
3. ticketId 如何进入 Service？
4. orderId 是否由 RedisIdWorker 生成？
5. Lua 脚本真实路径是什么？
6. Lua 参数有哪些？
7. Redis 库存 Key 格式是什么？
8. 一人一单 Key / Set 格式是什么？
9. Lua 返回值分别代表什么？
10. Lua 成功后如何进入 BlockingQueue？
11. Queue 类型是什么？
12. Queue capacity 是多少？
13. 使用 add / offer / put 哪一种？
14. Consumer 在哪里启动？
15. Consumer 是否单线程？
16. Redisson Lock Key 是什么？
17. MySQL 如何扣库存？
18. MySQL 如何防重复订单？
19. 是否存在 UNIQUE(user_id, ticket_id)？
20. Consumer 遇到异常后是否继续运行？

不要凭提示词猜实现。

---

# 五、任务 2：准备真实测试环境

优先复用 Phase 3B 已验证方案。

如果本机 3306 MySQL 凭据不明确，不要触碰。

可以继续使用独立临时 MySQL，例如：

```text
MySQL 8
localhost:3307
database: cityhub
```

Redis 使用已确认安全的开发 Redis。

凭据只通过环境变量提供，不写入仓库，不写入报告。

使用：

```text
backend/core/src/main/resources/db/cityhub_schema.sql
```

重新初始化临时 cityhub。

验证：

```sql
SELECT COUNT(*) FROM tb_ticket;
SELECT COUNT(*) FROM tb_seckill_ticket;
SELECT COUNT(*) FROM tb_reservation_order;
```

---

# 六、任务 3：准备可测试的 SeckillTicket

至少找一个：

```text
Ticket + SeckillTicket
```

满足：

```text
stock > 0
beginTime <= 当前时间
endTime > 当前时间
```

如果 Seed 时间不适合当前测试：

允许只修改**临时测试环境**的 beginTime / endTime / stock。

不要为了测试引入新业务模型。

---

# 七、任务 4：检查 Redis 秒杀库存初始化

这是本阶段高优先级。

先查当前代码：

> `tb_seckill_ticket.stock` 是如何加载到 Redis 的？

搜索：

```text
SECKILL_STOCK
opsForValue
seckill stock
ticket stock
```

## 如果已有初始化逻辑

直接复用并验证。

## 如果不存在

允许增加一个最小开发期初始化辅助：

```text
查询 tb_seckill_ticket
 -> ticketId + stock
 -> 写入 Lua 当前真实使用的 Redis stock Key
```

只做开发期初始化。

禁止新增：

```text
定时同步
后台管理系统
CDC
MQ
复杂补偿
```

---

# 八、测试状态重置

每个集成测试前，只针对测试 ticketId / test user：

```text
MySQL：
- 删除测试用户的 ReservationOrder
- 重置 tb_seckill_ticket.stock

Redis：
- 重置秒杀 stock
- 清理一人一单 Set / Key
- 清理相关测试 Key
```

禁止清空未知 Redis 或 MySQL 全库。

---

# 九、任务 5：准备真实测试用户和 Token

不要绕过 LoginInterceptor。

优先走项目真实：

```text
验证码
 -> login API
 -> Token
 -> UserHolder
```

如果没有真实短信服务：

允许在测试 Redis 中直接写验证码，然后调用真实 login API 获取 Token。

禁止：

```text
硬编码 userId
关闭登录拦截器
修改登录逻辑
```

至少准备：

```text
User A
User B
User C
```

并为并发测试准备更多用户。

---

# 十、任务 6：正常预约验证

准备：

```text
ticketId = 测试限量 Ticket
Redis stock = 10
MySQL stock = 10
ReservationOrder = 0
```

调用真实秒杀预约 API，例如：

```text
POST /reservation/...
```

具体以 Controller 实际路径为准。

携带真实测试 Token。

预期：

```text
API success=true
返回 orderId（如果当前接口返回）

Redis stock：10 -> 9

MySQL tb_seckill_ticket.stock：10 -> 9

tb_reservation_order：
新增 1 条

user_id 正确
ticket_id 正确
order.id 正确
```

等待异步消费者完成后再检查 DB。

---

# 十一、RedisIdWorker 验证

确认：

```text
ReservationOrder.id
```

真实来源于：

```text
RedisIdWorker.nextId(...)
```

而不是 MySQL AUTO_INCREMENT。

不要重写 RedisIdWorker。

---

# 十二、任务 7：一人一单验证

同一个：

```text
User A + ticketId
```

连续预约两次。

预期：

```text
第一次：SUCCESS
第二次：FAIL
```

数据库：

```sql
SELECT COUNT(*)
FROM tb_reservation_order
WHERE user_id = ?
  AND ticket_id = ?;
```

结果必须：

```text
1
```

同时检查 Redis 一人一单 Set / Key，第二次请求应在 Lua 阶段被拒绝。

---

# 十三、一人一单三层保护

必须记录当前真实情况：

```text
第一层：Lua 一人一单
第二层：Redisson 用户级锁
第三层：UNIQUE(user_id, ticket_id)
```

不要新增第四套复杂方案。

---

# 十四、任务 8：库存不足验证

准备：

```text
stock = 2
```

同步设置：

```text
Redis stock = 2
MySQL stock = 2
```

用三个不同用户各请求一次。

预期最终：

```text
成功 = 2
失败 = 1

Redis stock = 0
MySQL stock = 0

ReservationOrder = 2
```

绝不允许：

```text
stock < 0
ReservationOrder > 2
```

---

# 十五、任务 9：未登录验证

不带 Token 调用秒杀预约 API。

预期：

```text
被登录拦截器拒绝
```

不要为了测试放开接口。

---

# 十六、任务 10：秒杀时间边界

先检查当前代码是否真实判断：

```text
beginTime
endTime
```

如果有：

验证：

```text
未开始 -> FAIL
进行中 -> 可预约
已结束 -> FAIL
```

如果没有：

不要擅自增加复杂状态机。

报告：

```text
当前入口未实现 beginTime/endTime 校验
```

即可。

---

# 十七、任务 11：多用户小规模并发

这不是性能压测，只验证正确性。

建议：

```text
stock = 10
20 个不同用户
20 个并发请求
```

优先使用：

```text
JUnit 集成测试
ExecutorService
CountDownLatch
```

不要引入 JMeter / Gatling / Locust。

最终必须检查：

```text
成功 = 10
失败 = 10

Redis stock = 0
MySQL stock = 0

新增 ReservationOrder = 10

不存在负库存
不存在超卖
不存在同 userId + ticketId 重复订单
```

如果有非业务异常导致请求失败，应据实记录，不伪造成业务失败。

---

# 十八、任务 12：同用户并发

准备：

```text
User A
ticket stock >= 10
User A 未预约过
```

同一个 Token 并发请求 10 次。

预期：

```text
成功资格 <= 1
最终 ReservationOrder = 1
```

重点观察：

```text
Lua 一人一单
Redisson 用户锁
MySQL UNIQUE
```

---

# 十九、任务 13：Redisson 验证

不要改 Redisson。

只确认当前真实：

```text
RLock
tryLock
finally unlock
```

记录：

```text
Lock Key
Lock 粒度
获取锁失败怎么处理
unlock 在哪里执行
```

当前如果仍为：

```text
order:{userId}
```

则保持。

---

# 二十、任务 14：MySQL 库存扣减安全性

检查异步下单时：

```text
tb_seckill_ticket
```

库存如何扣减。

最低正确要求应类似：

```sql
UPDATE tb_seckill_ticket
SET stock = stock - 1
WHERE ticket_id = ?
  AND stock > 0;
```

或 MyBatis-Plus 等价条件更新。

必须检查 affectedRows。

如果库存扣减失败：

不得继续 INSERT ReservationOrder。

如果当前缺少 `stock > 0` 条件：

允许最小修复。

不要扩展成复杂乐观锁系统。

---

# 二十一、任务 15：BlockingQueue 真实链路

检查：

```text
ArrayBlockingQueue
```

记录：

```text
capacity
消息类型
producer
consumer
consumer thread count
```

正常预约时验证：

```text
Lua SUCCESS
 -> ReservationOrder 入 Queue
 -> Consumer 取出
 -> Redisson
 -> MySQL stock
 -> INSERT order
```

---

# 二十二、Queue 写入方式

确认当前使用：

```text
add
offer
put
```

哪一种。

如果是 `offer()`：

检查 false 是否处理。

如果是 `add()`：

记录队列满可能抛异常。

如果是 `put()`：

记录阻塞风险。

本阶段最低要求：

> Queue 写入失败不能静默丢失。

如果安全补偿需要大改，不要强行实现，只记录风险。

---

# 二十三、任务 16：Consumer 异常处理

检查消费者主循环。

最低要求：

> 一条订单失败不能导致整个消费者线程永久退出。

如果当前异常会导致 Runnable 结束：

允许做最小修复：

```text
循环内部 catch
记录 error log
继续消费
```

不要新增 DLQ / MQ / Retry Framework。

---

# 二十四、DB 下单失败的一致性限制

专门检查：

```text
Lua SUCCESS
Redis stock 已扣
Queue 已入
DB update / insert FAIL
```

当前怎么处理。

本阶段不要求实现：

```text
自动 Redis 库存补偿
重试队列
Dead Letter Queue
可靠消息事务
Redis Stream
MQ
```

只要求：

```text
异常不静默
消费者不中断
有错误日志
报告明确记录限制
```

---

# 二十五、任务 17：新增真实集成测试

推荐新增：

```text
SeckillReservationIntegrationTest.java
```

具体路径按当前 package。

优先真实依赖：

```text
MySQL
Redis
Lua
Redisson
BlockingQueue
```

不要把核心依赖全部 Mock 后声称秒杀验证成功。

至少覆盖：

```text
正常预约
一人一单
库存不足
多用户并发
同用户并发
```

---

# 二十六、异步测试要求

API / Service 返回成功不等于 MySQL 已落单。

测试必须：

```text
轮询数据库
+
有限超时
```

等待异步 Consumer 完成。

不要无限 sleep。

---

# 二十七、并发测试最终数据检查

必须记录：

```text
初始 stock
请求数
成功数
失败数
Redis final stock
MySQL final stock
ReservationOrder count
duplicate order count
```

超卖检查：

```text
Redis stock >= 0
MySQL stock >= 0
订单数 <= 初始库存
```

重复订单检查：

```sql
SELECT user_id, ticket_id, COUNT(*) AS cnt
FROM tb_reservation_order
GROUP BY user_id, ticket_id
HAVING COUNT(*) > 1;
```

结果应为：

```text
0 rows
```

---

# 二十八、测试文档

创建：

```text
docs/refactor/phase3c/SECKILL_VERIFICATION.md
```

至少记录：

| 场景 | 预期 | 实际 | 状态 |
|---|---|---|---|
| 正常预约 | 1 单 | ... | PASS/FAIL |
| 重复预约 | 第二次失败 | ... | PASS/FAIL |
| 库存不足 | 不超卖 | ... | PASS/FAIL |
| 未登录 | 拒绝 | ... | PASS/FAIL |
| 多用户并发 | 成功数=库存 | ... | PASS/FAIL |
| 同用户并发 | 最多 1 单 | ... | PASS/FAIL |

---

# 二十九、本阶段允许的最小修复

允许：

```text
Redis 秒杀 stock 初始化缺失
ticketId 迁移遗漏
Entity / Mapper / SQL 小错误
Lua Key 语义错误
Queue 入队失败未检测
Consumer 异常导致线程退出
MySQL stock > 0 条件缺失
集成测试辅助
少量必要日志
```

---

# 三十、本阶段禁止的修复

禁止：

```text
BlockingQueue -> Redis Stream
新增 Kafka/RabbitMQ
重写 Lua
重写 Redisson
复杂库存补偿
复杂事务消息
新增订单状态机
支付/退款
新增 ActivitySession
```

---

# 三十一、已知限制允许保留

如果当前 BlockingQueue 存在：

```text
JVM 宕机后内存消息丢失
```

允许保留。

报告写清：

> 当前第一版采用 JVM BlockingQueue 做异步削峰，适合单体项目，但消息不持久化；后续可升级 Redis Stream 或 MQ 提升可靠性。

不要本阶段升级。

---

# 三十二、Maven / Test / Spring 验证

代码修改后：

```bash
cd backend
mvn clean compile
```

要求：

```text
CityHub
CityHub Core
CityHub AI
BUILD SUCCESS
```

集成测试：

```bash
mvn -pl core test -Dtest=SeckillReservationIntegrationTest
```

如果 Java 17 + 当前 MyBatis-Plus 仍需 Phase 3B 已验证参数：

```text
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED
```

继续使用，不升级依赖。

最终再次启动 CityHub Core，确认：

```text
MySQL
Redis
Redisson
Lua
异步 Consumer
```

均无启动异常。

---

# 三十三、Git Phase 3C

完成后：

```bash
git status --short
git diff --check
```

确认没有：

```text
真实密码
API Key
测试 Token
临时 DB 文件
target
logs
```

然后：

```bash
git add .
git diff --cached --check
git diff --cached --stat
git commit -m "test: verify CityHub seckill reservation flow"
git push
```

禁止 force push。

---

# 三十四、本阶段交付文件

创建：

```text
docs/refactor/phase3c/
```

生成：

```text
PHASE3C_REPORT.md
SECKILL_FLOW_AUDIT.md
SECKILL_VERIFICATION.md
```

如新增测试：

```text
SeckillReservationIntegrationTest.java
```

---

# 三十五、PHASE3C_REPORT.md 必须包含

## 1. 阶段结论

Phase 3C 是否通过。

## 2. 秒杀真实链路

```text
Controller
Service
RedisIdWorker
Lua
Queue
Consumer
Redisson
MySQL
```

## 3. Redis 初始化

```text
stock 如何初始化
一人一单 Key 如何初始化/清理
```

## 4. 正常预约

```text
初始库存
Redis 最终库存
MySQL 最终库存
订单数
```

## 5. 一人一单

```text
第一次请求
第二次请求
最终订单数
```

## 6. 库存不足

```text
请求数
成功数
失败数
最终库存
```

## 7. 多用户并发

```text
初始 stock
并发数
成功数
失败数
Redis stock
MySQL stock
订单数
```

## 8. 同用户并发

```text
并发请求数
成功资格数
最终订单数
```

## 9. Redisson

```text
Lock Key
Lock 粒度
tryLock
unlock
```

## 10. BlockingQueue

```text
capacity
add/offer/put
consumer count
异常处理
```

## 11. MySQL

```text
扣库存条件
UNIQUE(user_id, ticket_id)
```

## 12. RedisIdWorker

是否真实用于 ReservationOrder。

## 13. 已知限制

至少说明：

```text
BlockingQueue 非持久化
DB 失败是否补偿
是否存在可靠消息机制
```

必须据实填写。

## 14. 工程验证

```text
mvn clean compile
integration test
Spring startup
```

## 15. Git

```text
Phase 3B commit
Phase 3C commit
commit hash
branch
origin
push
```

---

# 三十六、最终验收标准

Phase 3C 通过应满足：

```text
正常预约 PASS

同 userId + ticketId：
最多 1 条 ReservationOrder

库存不足：
正确拒绝

Redis stock >= 0
MySQL stock >= 0

订单数 <= 初始库存

多用户并发：
无超卖

同用户并发：
最终 1 单

Lua：
真实执行库存判断 + 一人一单 + 预扣库存

RedisIdWorker：
真实用于 orderId

BlockingQueue：
真实 producer -> consumer -> DB

Redisson：
真实执行 RLock / tryLock / unlock

MySQL：
库存不会负数
存在 UNIQUE(user_id, ticket_id)

Maven：
BUILD SUCCESS

Integration Test：
PASS

Spring：
启动成功

Git：
commit + push
```

---

# 三十七、不作为阻断项

以下不要求：

```text
Redis Stream
MQ
消息持久化
自动库存补偿
大规模压测
支付
退款
核销
取消订单
GEO
Blog
AI
前端
Docker
README
```

---

# 三十八、最终回复格式

完成后输出：

```text
Phase 3C 限量活动预约 / 秒杀链路验证完成。

【Git 基线】
1. Phase 3B commit：
2. Phase 3B push：

【秒杀链路】
3. 秒杀 API：
4. RedisIdWorker：
5. Lua：
6. Redis stock key：
7. 一人一单 key：
8. BlockingQueue：
9. Consumer：
10. Redisson：
11. MySQL 扣库存：

【运行验证】
12. 正常预约：
13. 重复预约：
14. 库存不足：
15. 未登录：
16. 秒杀时间：
17. 多用户并发：
18. 同用户并发：
19. 超卖：
20. 重复订单：

【一致性 / 限制】
21. Queue 满处理：
22. Consumer 异常：
23. DB 失败：
24. BlockingQueue 非持久化限制：

【工程验证】
25. Maven：
26. Integration Test：
27. Spring：

【Git Phase 3C】
28. commit：
29. commit hash：
30. branch：
31. push：

【未修改】
32. Redis Stream / MQ：
33. Blog / Follow：
34. AI：
35. 前端：
36. README：

详细报告：
docs/refactor/phase3c/PHASE3C_REPORT.md
```

---

# 三十九、最终原则

本阶段不追求“秒杀架构最先进”。

真正目标：

> **证明 CityHub 当前 Redis + Lua + 一人一单 + RedisIdWorker + Redisson + BlockingQueue 异步预约链路真实可运行，并发下不超卖、不重复下单，而且每一步都能与用户已经掌握的面试知识对应。**

优先完成项目，后续有时间再提升可靠性。
