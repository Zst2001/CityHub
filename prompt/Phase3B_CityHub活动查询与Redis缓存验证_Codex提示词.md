# Codex 执行任务：Phase 3B 活动查询与 Redis 缓存运行验证

## 一、任务背景

当前项目：

> **CityHub - 城市活动发现与预约平台**

已经完成：

- Phase 1：基础工程基线治理；
- Phase 2：CityHub 工程身份规范化；
- Phase 3A-R：轻量领域重置与迁移。

Phase 3A-R 已确认正式核心领域基线为：

```text
ActivityCategory
      ↓
Activity
      ↓
Ticket
      ↓
SeckillTicket
      ↓
ReservationOrder(ticketId)
```

并且：

```text
Venue
ActivitySession
ReservationOrder(sessionId)
```

复杂模型已经撤回。

当前 Maven 编译已经通过。

---

# 二、当前已知真实状态

根据 Phase 3A-R 报告：

## 已经完成

```text
ActivityCategory
Activity
Ticket
SeckillTicket
ReservationOrder(ticketId)

数据库初始化脚本：
backend/core/src/main/resources/db/cityhub_schema.sql

数据库默认名称：
cityhub

Maven compile：
PASS
```

---

## 当前缓存真实实现

### 缓存穿透

已经存在：

```text
CacheClient.queryWithPassThrough
```

采用：

```text
DB miss
↓
Redis 写空值 + TTL
```

---

### 缓存击穿

已经存在：

```text
CacheClient.queryWithLogicalExpire
```

采用：

```text
逻辑过期
+
互斥锁
+
独立线程池异步重建
+
返回旧值
```

---

### 缓存一致性

Activity 更新仍采用：

```text
更新 MySQL
↓
删除 Redis
```

---

### 缓存雪崩

**当前实际代码没有实现随机 TTL。**

Phase 3B 需要补齐：

> **基础 TTL + 随机偏移**

这是本阶段唯一允许新增的缓存治理算法。

---

# 三、本阶段核心目标

Phase 3B 不再做大规模领域修改。

本阶段目标是把：

```text
能编译
```

推进到：

```text
数据库能初始化
↓
Spring Boot 能启动
↓
核心 Activity API 能请求
↓
Redis 缓存真实生效
↓
缓存穿透可验证
↓
缓存击穿方案可验证
↓
缓存雪崩随机 TTL 已补齐
↓
缓存一致性可验证
↓
GEO 附近活动在原功能存在时可用
```

核心关键词：

> **RUN + VERIFY + OBSERVE**

---

# 四、本阶段禁止事项

禁止新增或重构：

```text
Venue
ActivitySession

支付
退款
核销
复杂预约状态机

Kafka
RabbitMQ
RocketMQ

Caffeine
Sentinel
Redis Cluster
复杂限流

Blog -> Post
Follow 重构

AI Tool 深度业务迁移
RAG
MCP

前端视觉大改

README 重写

秒杀核心链路重写
Lua 重写
Redisson 重写
BlockingQueue -> Redis Stream
```

---

# 五、Git 任务：先完成 Phase 3A 基线收尾

当前仓库：

```text
https://github.com/Zst2001/CityHub
```

本阶段开始前必须检查：

```bash
git status --short
git log --oneline -n 5
git branch --show-current
git remote -v
```

---

## 情况 A：Phase 3A 已有正式 commit

如果已经存在类似：

```text
feat: establish CityHub activity reservation domain
```

或其他明确对应 Phase 3A 的 commit：

不要重复提交。

继续 Phase 3B。

---

## 情况 B：仍然没有任何 commit

如果：

```bash
git rev-parse --verify HEAD
```

失败，说明 Phase 3A 尚未提交。

此时先执行：

```bash
cd backend
mvn clean compile
```

确认 PASS。

再检查敏感文件：

```bash
git status --short
```

重点确认没有：

```text
.env
真实数据库密码
真实 API Key
target/
logs/
IDE 临时文件
仓库外备份
```

然后：

```bash
git add .
git diff --cached --check
git diff --cached --stat
```

确认没有明显问题后执行：

```bash
git commit -m "feat: establish CityHub activity reservation domain"
git branch -M main
```

如果远程为空且认证正常：

```bash
git push -u origin main
```

禁止 force push。

---

# 六、任务 1：检查本地运行环境

先不要修改代码。

检查：

```text
Java
Maven
MySQL
Redis
```

---

## Java / Maven

执行：

```bash
java -version
mvn -version
```

确认与当前项目要求兼容。

---

## MySQL

确认本机是否存在可用开发 MySQL。

可以使用实际项目配置的：

```text
DB_HOST
DB_PORT
DB_USERNAME
DB_PASSWORD
```

不要把密码写入代码或报告。

---

## Redis

确认：

```bash
redis-cli ping
```

如果当前 Redis 不在默认地址，则根据环境变量使用真实地址验证。

预期：

```text
PONG
```

---

# 七、外部环境不可用时的原则

如果 MySQL 或 Redis 没有安装 / 没有启动：

不要改业务代码绕过。

明确报告：

```text
MySQL unavailable
Redis unavailable
```

如果属于可以安全启动的本地已有服务：

可以启动。

如果需要用户提供密码、安装软件或进行高风险系统修改：

停止相关运行验证并说明。

---

# 八、任务 2：创建并初始化 cityhub 数据库

使用：

```text
backend/core/src/main/resources/db/cityhub_schema.sql
```

---

## 第一步：静态检查

执行前先确认：

```text
数据库名
表名
字段名
Entity @TableName
Mapper
SQL
```

一致。

必须至少确认：

```text
tb_activity_category
tb_activity

tb_ticket
tb_seckill_ticket
tb_reservation_order

tb_user
tb_user_info
tb_blog
tb_blog_comments
tb_follow
```

---

## 第二步：创建开发数据库

数据库：

```text
cityhub
```

如果 SQL 本身已经：

```sql
CREATE DATABASE
USE cityhub
```

按实际脚本执行。

否则使用安全开发账号创建。

---

## 第三步：执行 SQL

例如：

```bash
mysql -u <user> -p cityhub < backend/core/src/main/resources/db/cityhub_schema.sql
```

具体命令按本地环境调整。

禁止在命令或报告中打印真实密码。

---

# 九、数据库执行后验证

检查：

```sql
SHOW TABLES;
```

以及：

```sql
SELECT COUNT(*) FROM tb_activity_category;
SELECT COUNT(*) FROM tb_activity;
SELECT COUNT(*) FROM tb_ticket;
SELECT COUNT(*) FROM tb_seckill_ticket;
```

确认初始化示例数据存在。

---

# 十、数据库问题处理边界

如果 SQL 因 Phase 3A 重命名造成：

```text
字段不存在
索引错误
外键 / 唯一键错误
INSERT 字段不匹配
```

属于本阶段必须修复。

---

## 但不要趁机做

```text
表结构大优化
复杂索引设计
数据库范式重构
新增 Venue / Session
```

目标只是：

> 当前 Entity / Mapper / SQL 能真实运行。

---

# 十一、任务 3：启动 CityHub Core

配置环境变量。

例如：

```text
DB_USERNAME
DB_PASSWORD
DB_URL
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
```

使用 Phase 1 已建立的环境变量配置。

---

## 启动方式

根据当前项目实际配置：

```bash
cd backend/core
mvn spring-boot:run
```

或：

```bash
cd backend
mvn -pl core spring-boot:run
```

以真实可用方式为准。

---

# 十二、Spring 启动验收

必须确认：

```text
Spring Context 启动成功
MySQL 连接成功
Redis 连接成功
Mapper 初始化成功
没有表不存在错误
没有 Bean 冲突
```

记录：

```text
启动命令
端口
启动结果
```

---

# 十三、如果 Spring 启动失败

按以下优先级处理：

```text
1. Phase 3A 类型 / Mapper / SQL 迁移错误
2. 配置错误
3. 数据库缺表 / 字段不一致
4. Redis 配置
5. 其他当前代码真实问题
```

只修阻断当前活动查询和缓存验证的问题。

不要顺手重构无关模块。

---

# 十四、任务 4：建立 API Smoke Test 清单

创建：

```text
docs/refactor/phase3b/PHASE3B_REQUESTS.http
```

如果项目 / IDE 不适合 `.http` 文件，可以生成：

```text
docs/refactor/phase3b/API_SMOKE_COMMANDS.md
```

保存可复现的：

```text
curl
HTTP 请求
```

---

# 十五、必须验证的 ActivityCategory API

根据当前真实 Controller 路由验证。

至少：

```text
GET /activity-category/list
```

或当前实际等价接口。

目标：

```text
HTTP 200
返回初始化活动分类
```

---

# 十六、必须验证的 Activity API

根据实际 Controller 验证：

## Activity 详情

例如：

```text
GET /activity/{id}
```

---

## Activity 分类列表

例如：

```text
GET /activity/of/category
```

或真实等价接口。

---

## Activity GEO

如果旧“附近商铺”已经迁移为“附近活动”：

验证：

```text
按 category + current + x + y
```

附近活动查询。

如果当前实际没有对应路由：

不要为了 Phase 3B 新造复杂 API。

在报告中说明。

---

# 十七、必须验证的 Ticket 公开查询

根据实际 Controller：

例如：

```text
GET /ticket/list/{activityId}
```

验证：

```text
Activity
↓
Ticket
```

关联查询正常。

本阶段不要触发秒杀写接口。

---

# 十八、缓存验证前先观察 Redis

使用：

```bash
redis-cli
```

或实际 Redis 客户端。

在测试 Activity 详情前：

检查相关：

```text
cache:activity:
lock:activity:
```

Key。

---

# 十九、任务 5：验证缓存穿透

目标：

> 验证不存在 Activity ID 时，会写入 Redis 空值，并设置短 TTL。

---

## 验证流程

选择一个数据库明确不存在的：

```text
activityId
```

例如：

```text
99999999
```

但必须先通过数据库确认不存在。

---

### Step 1

删除对应 Redis Key：

```text
cache:activity:<id>
```

---

### Step 2

请求：

```text
GET /activity/<不存在ID>
```

---

### Step 3

检查：

```text
Redis 是否出现 cache:activity:<id>
value 是否为空值语义
TTL 是否为短 TTL
```

---

### Step 4

再次请求同一个不存在 ID。

观察：

```text
第二次是否直接命中 Redis 空值
```

---

# 二十、缓存穿透验收

报告必须记录：

```text
不存在 Activity ID
第一次请求结果
Redis Key
Redis Value
Redis TTL
第二次请求结果
```

不要打印敏感数据。

---

# 二十一、任务 6：补齐随机 TTL 缓解缓存雪崩

这是 Phase 3B 唯一要求新增的缓存算法。

当前代码没有随机 TTL。

---

## 设计原则

按照用户学习资料：

> 给不同缓存 Key 的 TTL 添加随机值，降低大量 Key 同一时刻失效的概率。

第一版只做这个。

不要新增：

```text
Redis Cluster
Caffeine
复杂限流
```

---

# 二十二、随机 TTL 实现要求

先阅读：

```text
CacheClient
ActivityServiceImpl
RedisConstants
```

判断正常 Activity 缓存写入点。

---

## 推荐实现

使用类似：

```text
base TTL + random jitter
```

例如概念：

```java
long ttl = baseTtl + ThreadLocalRandom.current().nextLong(0, jitter + 1);
```

但具体单位必须与现有：

```text
TimeUnit
Duration
```

保持一致。

---

# 二十三、随机 TTL 的边界

不要破坏：

```text
NULL_TTL
LOGIN TTL
验证码 TTL
逻辑过期时间
秒杀 TTL
```

本阶段优先只针对：

> **普通 Activity 详情缓存**

增加随机偏移。

---

## 特别注意逻辑过期

逻辑过期方案的：

```text
expireTime
```

属于业务逻辑时间。

不要为了雪崩机械随机化逻辑过期算法。

雪崩随机 TTL 主要作用于：

```text
Redis 真实 TTL 的普通缓存 Key
```

---

# 二十四、随机 TTL 配置

推荐增加简单常量：

```text
CACHE_ACTIVITY_TTL
CACHE_ACTIVITY_TTL_JITTER
```

具体值结合当前代码。

不要硬编码到业务 Service 多处。

---

# 二十五、随机 TTL 验证

连续缓存多个 Activity：

```text
Activity A
Activity B
Activity C
```

检查：

```text
TTL A
TTL B
TTL C
```

应存在合理差异。

不要要求每次都一定不同，但应在配置范围内随机。

---

# 二十六、任务 7：验证缓存击穿逻辑

现有：

```text
CacheClient.queryWithLogicalExpire
```

不得重写。

---

## 本阶段目标

验证该实现：

```text
逻辑过期
互斥锁
异步重建
返回旧值
```

仍可用于 Activity。

---

# 二十七、先判断 Activity 正式查询路径是否使用逻辑过期

检查：

```text
ActivityServiceImpl.queryById / queryActivityById
```

当前真实路径到底使用：

```text
queryWithPassThrough
```

还是：

```text
queryWithLogicalExpire
```

---

## 不要为了同时展示两个方案而强行混合

如果正式 Activity 详情目前采用：

```text
queryWithPassThrough
```

则不要直接重写主接口为复杂混合算法。

可以：

```text
保留 queryWithLogicalExpire
保留热点 Activity 预热方法
```

并通过：

```text
现有测试辅助方法
开发期验证代码
受控预热调用
```

验证逻辑过期实现。

---

# 二十八、如果逻辑过期当前已经是正式 Activity 查询路径

则：

1. 预热一个 Activity；
2. 手动让 logical expireTime 过期；
3. 保持 Redis Key 存在；
4. 请求 Activity；
5. 验证立即返回旧值；
6. 验证异步线程重建；
7. 检查新 logical expireTime。

---

# 二十九、如果无法安全做并发验证

不要为了证明击穿创建复杂压测框架。

至少验证：

```text
逻辑过期 Key
锁 Key
异步重建
最终缓存更新
```

并在报告中说明：

```text
未进行高并发压测
```

JMeter 留到项目收尾。

---

# 三十、任务 8：验证缓存一致性

目标：

> Activity 更新后，先更新 MySQL，再删除对应 Redis 缓存。

---

## 验证流程

选择一个测试 Activity。

### Step 1

请求详情，建立：

```text
cache:activity:<id>
```

---

### Step 2

通过现有 Activity 更新接口修改：

```text
title
```

或其他安全测试字段。

如果更新接口需要登录：

使用合法测试用户 Token。

---

### Step 3

确认：

```text
MySQL 数据已更新
Redis cache:activity:<id> 被删除
```

---

### Step 4

再次请求详情。

确认：

```text
重新从 DB 加载
Redis 重建
返回新数据
```

---

# 三十一、禁止为了缓存一致性增加复杂方案

不要新增：

```text
Canal
MQ 补偿
延迟双删
分布式事务
```

保持当前：

```text
update DB
↓
delete Redis
↓
TTL 兜底
```

即可。

---

# 三十二、任务 9：验证 GEO 附近活动（仅在现有功能存在时）

如果 Phase 3A 已经迁移：

```text
Shop GEO
->
Activity GEO
```

检查：

```text
ACTIVITY_GEO_KEY
Activity.x
Activity.y
```

---

## 首先确认 GEO 数据是否初始化

如果 Redis 中没有：

```text
Activity GEO
```

而当前项目有现成的 GEO 初始化 / 测试方法：

使用现有方法迁移后验证。

---

## 如果没有自动初始化

允许增加一个：

> **低风险的开发期初始化方法 / 测试辅助**

把已有 Activity 的坐标写入 Redis GEO。

但不要开发复杂后台同步任务。

---

## 验证

根据一个已知经纬度请求附近活动。

确认：

```text
距离排序
分页
Activity 信息
distance 字段（如当前 VO 支持）
```

与原实现一致。

---

# 三十三、任务 10：Activity 缓存预热

因为逻辑过期方案依赖预热：

检查当前已有的：

```text
saveShop2Redis
```

迁移后是否类似：

```text
saveActivity2Redis
```

如果存在：

验证：

```text
数据库 Activity
↓
RedisData
↓
expireTime
↓
cache:activity:<id>
```

能够正确写入。

---

# 三十四、不新增后台预热系统

第一版只需要：

```text
手动预热辅助方法
测试辅助
```

不需要：

```text
定时任务
后台管理按钮
Kafka
CDC
```

---

# 三十五、日志与可观测性

为了验证缓存流程，可以在必要位置增加少量：

```text
debug / info
```

日志。

例如：

```text
activity cache miss
activity cache rebuild
```

但不要：

```text
每次 Redis 操作大量打印
打印用户敏感信息
```

验证完成后保留合理日志即可。

---

# 三十六、测试记录

创建：

```text
docs/refactor/phase3b/API_SMOKE_TEST.md
docs/refactor/phase3b/CACHE_VERIFICATION.md
docs/refactor/phase3b/PHASE3B_REPORT.md
```

---

# 三十七、API_SMOKE_TEST.md

至少记录：

| 接口 | 请求 | 结果 | 状态 |
|---|---|---|---|
| ActivityCategory | ... | ... | PASS/FAIL |
| Activity Detail | ... | ... | PASS/FAIL |
| Activity List | ... | ... | PASS/FAIL |
| Ticket List | ... | ... | PASS/FAIL |
| Nearby Activity | ... | ... | PASS/FAIL/N/A |

不要记录真实密码、Cookie、敏感 Token。

Token 可以写：

```text
<TEST_TOKEN>
```

---

# 三十八、CACHE_VERIFICATION.md

必须分节记录：

## 1. 缓存穿透

```text
方案：缓存空值
是否真实验证：
证据：
```

---

## 2. 缓存击穿

```text
方案：逻辑过期 + 互斥锁 + 异步重建
当前正式路径：
验证方式：
结果：
```

---

## 3. 缓存雪崩

```text
方案：正常 Activity Cache TTL + 随机偏移
新增文件：
随机范围：
验证结果：
```

---

## 4. 缓存一致性

```text
方案：更新 DB -> 删除 Redis
是否真实验证：
```

---

## 5. GEO

```text
是否存在：
是否验证：
```

---

# 三十九、Phase 3B 代码修改边界

本阶段允许修改：

```text
Activity 查询相关 bug
Activity Mapper / SQL 不一致
ActivityCategory 查询 bug
Ticket 查询 bug
Activity Redis Key bug

Activity 正常缓存随机 TTL

Activity GEO 必要初始化辅助

测试 / HTTP 请求文件
少量验证日志
```

---

## 本阶段禁止修改

```text
SeckillTicket 核心业务
ReservationOrder 秒杀链路
Lua
Redisson
BlockingQueue
RedisIdWorker

Blog
Follow

AI

前端视觉

README
```

除非某个未修改模块直接阻断应用启动，并且可以最小修复。

---

# 四十、Maven 验证

修改完成后：

```bash
cd backend
mvn clean compile
```

必须：

```text
BUILD SUCCESS
```

---

# 四十一、Spring 再启动验证

完成所有修改后再次启动 CityHub Core。

确认：

```text
Spring Context
MySQL
Redis
Activity Mapper
Ticket Mapper
```

仍正常。

---

# 四十二、Git Phase 3B 提交

Phase 3B 验收通过后：

```bash
git status --short
git diff --check
```

检查修改。

---

## 敏感信息检查

确认没有：

```text
真实 DB 密码
真实 Redis 密码
真实 API Key
测试 Token
数据库 dump 中的敏感数据
```

---

## 提交

```bash
git add .
git diff --cached --check
git diff --cached --stat
```

确认后：

```bash
git commit -m "feat: verify CityHub activity cache flow"
```

---

## Push

如果：

```text
origin
main
```

正常：

```bash
git push
```

禁止 force push。

---

# 四十三、Phase 3B 验收标准

必须尽量满足：

## 数据库

```text
cityhub_schema.sql
真实导入成功
```

---

## 应用

```text
CityHub Core 启动成功
```

---

## Activity

```text
ActivityCategory 查询 PASS
Activity 详情 PASS
Activity 列表 / 分类查询 PASS
Ticket 查询 PASS
```

---

## Redis

```text
缓存穿透：
真实验证 PASS

缓存击穿：
逻辑过期链路可验证

缓存雪崩：
随机 TTL 已实现并验证

缓存一致性：
更新 DB -> 删除缓存验证 PASS
```

---

## GEO

如果当前实现存在：

```text
附近活动 PASS
```

如果不存在：

```text
N/A
```

不要为了验收强行新增复杂 GEO 架构。

---

## 编译

```text
mvn clean compile
PASS
```

---

## Git

```text
Phase 3A baseline commit 已存在
Phase 3B commit 已完成
push 成功（认证允许时）
```

---

# 四十四、本阶段不验收

不要把以下内容作为 Phase 3B 阻断项：

```text
秒杀是否完整跑通
BlockingQueue 可靠性
Redis Stream
高并发压测
Blog
Feed
AI
前端视觉
Docker
README
```

---

# 四十五、下一阶段

Phase 3B 通过后：

> **Phase 3C：限量活动预约 / 秒杀链路运行验证**

主要验证：

```text
Ticket
SeckillTicket

Redis 秒杀库存

Lua：
库存判断
一人一单
预扣库存

ReservationOrder

Redisson

RedisIdWorker

BlockingQueue 异步下单
```

Phase 3B 不提前执行。

---

# 四十六、最终回复格式

完成后输出：

```text
Phase 3B 活动查询与缓存验证完成。

【Git 基线】
1. Phase 3A commit：
2. Phase 3A push：

【运行环境】
3. MySQL：
4. Redis：
5. cityhub_schema.sql：
6. CityHub Core：

【API】
7. ActivityCategory：
8. Activity Detail：
9. Activity List：
10. Ticket：
11. Nearby Activity：

【缓存】
12. 缓存穿透：
13. 缓存击穿：
14. 缓存雪崩随机 TTL：
15. 缓存一致性：
16. 逻辑过期预热：

【验证】
17. Maven compile：
18. Spring 启动：

【Git Phase 3B】
19. commit：
20. commit hash：
21. push：

【未修改】
22. 秒杀核心：
23. Blog / Follow：
24. AI：
25. 前端视觉：
26. README：

详细报告：
docs/refactor/phase3b/PHASE3B_REPORT.md
```

---

# 四十七、最终原则

Phase 3B 不追求复杂。

目标只有：

> **让 CityHub 的活动查询和 Redis 缓存真正运行起来，并确保用户已经学习的缓存穿透、击穿、雪崩、一致性方案与实际代码对应。**

优先：

```text
能运行
能验证
能解释
```

而不是：

```text
架构复杂
技术栈多
功能堆叠
```
