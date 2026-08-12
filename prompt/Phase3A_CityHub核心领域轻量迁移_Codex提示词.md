# Codex 执行任务：Phase 3A 核心领域轻量迁移与数据库重命名

## 一、任务背景

当前项目已经完成：

- Phase 1：基础工程基线治理；
- Phase 2：项目身份规范化。

当前工程名称统一为：

> **CityHub - 城市活动发现与预约平台**

当前 Maven、package、Application 等工程身份已经统一为 CityHub。

现在进入 Phase 3A。

本阶段目标不是重写整个系统，而是：

> **以最小改动成本，将黑马点评原有“商铺 / 优惠券 / 秒杀订单”核心业务语义迁移为 CityHub 的“活动 / 活动预约凭证 / 限量预约订单”，并最大程度保留已经熟悉和可讲解的 Redis、缓存、Lua、一人一单、Redisson、异步秒杀等实现。**

---

# 二、本阶段总体原则

## 1. 业务语义改名，核心技术实现尽量不动

本阶段核心映射固定为：

```text
ShopType        -> ActivityCategory
Shop            -> Activity

Voucher         -> Ticket
SeckillVoucher  -> SeckillTicket
VoucherOrder    -> ReservationOrder
```

同时同步迁移：

```text
Entity
Mapper
Service
ServiceImpl
Controller
Mapper XML
数据库表
相关 DTO / VO
相关方法名
相关 API 路由
```

## 2. 不做复杂领域重构

本阶段明确不要引入：

```text
Venue
ActivitySession
复杂 Reservation 生命周期
支付
退款
核销
复杂订单状态机
```

CityHub 第一版只需要：

```text
活动发现
活动详情
普通预约凭证
限量预约
预约订单
```

优先完成项目。

---

# 三、必须保持不变的核心技术逻辑

以下技术实现是用户已经学习和准备面试的重点。

本阶段禁止为了“重构更漂亮”而重新设计。

## 1. 缓存穿透

保持现有基于“缓存空值”的方案。

核心逻辑保持：

```text
请求 Activity
    ↓
查询 Redis
    ↓
缓存命中
    ├── 正常数据 -> 返回
    └── 空值     -> 直接返回不存在
    ↓
缓存未命中
    ↓
查询 MySQL
    ├── 存在 -> 写 Redis + TTL -> 返回
    └── 不存在 -> 写空值 + TTL
```

不要替换成：

```text
Bloom Filter
第三方缓存框架
复杂网关防护
```

可以保留原有 CacheClient 实现，仅做：

```text
Shop 泛型 / key / 方法语义
->
Activity
```

相关迁移。

## 2. 缓存击穿

保持当前“逻辑过期 + 互斥锁 + 独立线程异步重建 + 热点数据预热”的方案。

核心逻辑保持：

```text
热点 Activity 缓存
      ↓
Redis 中保存逻辑过期时间
      ↓
发现逻辑过期
      ↓
尝试获取互斥锁
      ↓
成功
      ├── 独立线程查 DB
      ├── 重建缓存
      └── 释放锁
      ↓
当前请求先返回旧数据
```

不要改成：

```text
简单 TTL
强制同步重建
Caffeine
复杂分布式缓存框架
```

如果原项目已有“双检”逻辑，继续保留。

## 3. 缓存雪崩

项目实际代码仍优先使用：

```text
TTL + 随机值
```

来降低大量 Key 同时失效概率。

不要为了本阶段新增：

```text
Redis Cluster
Sentinel
多级缓存
完整限流系统
```

这些可以作为面试扩展，不要求本项目当前实现。

## 4. 缓存与数据库一致性

保持：

```text
先更新 MySQL
再删除 Redis
```

并保留：

```text
@Transactional
TTL 兜底
```

如果当前代码已经采用此模式，只做 Activity 语义迁移，不修改策略。

## 5. 一人一单

保持现有核心思路。

原：

```text
userId + voucherId
```

迁移为：

```text
userId + ticketId
```

业务语义：

> 一个用户针对同一个限量预约 Ticket 只能生成一条 ReservationOrder。

禁止重新设计为复杂：

```text
ActivitySession + User
ReservationPolicy
QuotaRule
```

## 6. Lua 秒杀

保持现有 Lua 核心逻辑：

```text
检查库存
检查一人一单
扣减 Redis 库存
记录用户预约资格
```

仅做必要语义迁移：

```text
voucherId -> ticketId
```

以及 Redis Key / 变量名的必要同步。

不要重写 Lua 算法。

## 7. Redisson

如果当前数据库异步下单流程中仍使用 Redisson 处理一人一单并发问题：

继续保留。

例如锁粒度仍按：

```text
userId
```

或当前实际代码中的用户级锁设计。

不要主动移除 Redisson，也不要换成：

```text
synchronized
数据库悲观锁
复杂分布式事务
```

## 8. RedisIdWorker

继续保留现有：

```text
RedisIdWorker
```

仅让其生成：

```text
ReservationOrder.id
```

不需要重新实现 Snowflake 或 UUID。

## 9. 异步秒杀

本阶段不要重写异步链路。

如果当前项目是：

```text
Lua
↓
JVM BlockingQueue
↓
异步下单
```

则保持。

如果当前项目实际已经使用：

```text
Redis Stream
Consumer Group
XACK
Pending List
```

则保持当前 Stream 实现。

不要新增：

```text
Kafka
RabbitMQ
RocketMQ
```

本阶段以“现有可运行实现”为准。

---

# 四、本阶段明确不修改的模块

以下模块保持原样：

```text
User
Blog
BlogComments
Follow
登录体系
Redis Token
UserHolder
登录拦截器
Refresh Token 拦截器
Feed
点赞
共同关注
AI Tool
AI 预约
RAG
```

其中：

```text
Blog / Follow
```

后续 Phase 4 单独迁移。

AI 后续单独适配。

---

# 五、任务 1：执行前建立当前核心领域清单

在修改前，请先读取当前实际源码。

至少扫描：

```text
backend/core/src/main/java/com/cityhub/entity
backend/core/src/main/java/com/cityhub/controller
backend/core/src/main/java/com/cityhub/service
backend/core/src/main/java/com/cityhub/service/impl
backend/core/src/main/java/com/cityhub/mapper

backend/core/src/main/resources
backend/core/src/main/resources/mapper
backend/core/src/main/resources/db
```

以及真实存在的：

```text
Lua
DTO
VO
Constants
```

先记录真实映射，不要假设所有类都存在。

---

# 六、任务 2：迁移 ShopType -> ActivityCategory

迁移：

```text
ShopType
->
ActivityCategory
```

同步：

```text
ShopTypeController
-> ActivityCategoryController

IShopTypeService / ShopTypeService
-> IActivityCategoryService / ActivityCategoryService

ShopTypeServiceImpl
-> ActivityCategoryServiceImpl

ShopTypeMapper
-> ActivityCategoryMapper
```

具体是否带 `I` 前缀，以当前项目实际命名风格为准。

数据库表：

```text
tb_shop_type
->
tb_activity_category
```

字段尽量保持原样：

```text
id
name
icon
sort
create_time
update_time
```

API：

```text
/shop-type
->
/activity-category
```

---

# 七、任务 3：迁移 Shop -> Activity

迁移：

```text
Shop
->
Activity
```

同步：

```text
ShopController
-> ActivityController

IShopService / ShopService
-> IActivityService / ActivityService

ShopServiceImpl
-> ActivityServiceImpl

ShopMapper
-> ActivityMapper
```

数据库：

```text
tb_shop
->
tb_activity
```

字段迁移原则：

```text
typeId
->
categoryId

name
->
title
```

以下字段如当前存在，可优先保留：

```text
id
images
area
address
x
y
avgPrice
sold
comments
score
openHours
createTime
updateTime
```

业务解释：

```text
avgPrice  -> 活动平均费用 / 参考价格
sold      -> 已预约 / 已参与数量
comments  -> 评论数量
score     -> 活动评分
openHours -> 活动开放 / 举办时间展示
```

不要为了字段语义完美而增加大量新字段。

本阶段默认不要新增：

```text
Venue
ActivitySession
```

---

# 八、任务 4：迁移 Voucher -> Ticket

迁移：

```text
Voucher
->
Ticket
```

同步：

```text
VoucherController
-> TicketController

IVoucherService / VoucherService
-> ITicketService / TicketService

VoucherServiceImpl
-> TicketServiceImpl

VoucherMapper
-> TicketMapper
```

数据库：

```text
tb_voucher
->
tb_ticket
```

核心字段：

```text
shopId
->
activityId
```

其他字段尽量保留：

```text
id
title
subTitle
rules
payValue
actualValue
type
status
createTime
updateTime
```

Ticket 业务语义：

> CityHub 中活动的预约凭证。

不要设计复杂票务 / 支付体系。

---

# 九、任务 5：迁移 SeckillVoucher -> SeckillTicket

迁移：

```text
SeckillVoucher
->
SeckillTicket
```

数据库：

```text
tb_seckill_voucher
->
tb_seckill_ticket
```

字段：

```text
voucherId
->
ticketId
```

保持：

```text
stock
beginTime
endTime
createTime
updateTime
```

核心逻辑不变。

---

# 十、任务 6：迁移 VoucherOrder -> ReservationOrder

迁移：

```text
VoucherOrder
->
ReservationOrder
```

同步：

```text
VoucherOrderController
-> ReservationOrderController

IVoucherOrderService / VoucherOrderService
-> IReservationOrderService / ReservationOrderService

VoucherOrderServiceImpl
-> ReservationOrderServiceImpl

VoucherOrderMapper
-> ReservationOrderMapper
```

数据库：

```text
tb_voucher_order
->
tb_reservation_order
```

第一版核心字段：

```text
id
userId
ticketId
createTime
updateTime
```

如果旧表还有：

```text
payType
status
```

等字段，不要机械删除；根据当前业务依赖保留。

---

# 十一、一人一单相关迁移

所有订单业务中的：

```text
voucherId
```

迁移为：

```text
ticketId
```

例如：

```text
seckillVoucher(voucherId)
->
seckillTicket(ticketId)
```

关键逻辑必须继续：

```text
库存判断
一人一单判断
Redis 预扣库存
Lua 原子性
异步生成 ReservationOrder
```

---

# 十二、任务 7：数据库重命名为 cityhub

当前旧数据库连接名如果仍为：

```text
redis_project
```

则改为：

```text
cityhub
```

同步：

```text
application.yml
application-*.yml
.env.example
cityhub_schema.sql
```

不要新增：

```text
Flyway
Liquibase
```

直接维护：

```text
cityhub_schema.sql
```

即可。

数据库表迁移：

```text
tb_shop_type
-> tb_activity_category

tb_shop
-> tb_activity

tb_voucher
-> tb_ticket

tb_seckill_voucher
-> tb_seckill_ticket

tb_voucher_order
-> tb_reservation_order
```

保留：

```text
tb_user
tb_blog
tb_blog_comments
tb_follow
```

等社区业务依赖表。

---

# 十三、初始化数据迁移

将旧：

```text
商铺类型
商铺
优惠券
秒杀券
```

示例数据改成 CityHub 场景。

ActivityCategory 示例：

```text
展览
音乐
市集
演出
讲座
手作
体育
亲子
```

Activity 示例：

```text
城市青年创意市集
夏日爵士音乐会
当代摄影艺术展
周末陶艺体验课
城市文化讲座
```

只保留足够支持开发和测试的数据，不要大量造数据。

---

# 十四、任务 8：API 路由同步迁移

建议：

```text
/shop-type
-> /activity-category

/shop
-> /activity

/voucher
-> /ticket

/voucher-order
-> /reservation
```

方法名：

```text
queryShopById
-> queryActivityById

queryShopByType
-> queryActivityByCategory

updateShop
-> updateActivity

seckillVoucher
-> seckillTicket
```

只改语义，不改核心流程。

---

# 十五、前端处理范围

Phase 3A 不做视觉重构。

只同步因 API 路由修改导致的必要请求路径。

不要大规模修改：

```text
CSS
主题
首页布局
详情页视觉
社区 UI
个人中心
```

---

# 十六、任务 9：缓存语义迁移

实际存在时：

```text
CACHE_SHOP_KEY
-> CACHE_ACTIVITY_KEY

CACHE_SHOP_TTL
-> CACHE_ACTIVITY_TTL

SHOP_GEO_KEY
-> ACTIVITY_GEO_KEY
```

Redis key 如：

```text
cache:shop:
->
cache:activity:
```

可以同步迁移。

但以下实现不改：

```text
CacheClient
RedisData
缓存空值
逻辑过期
互斥锁
线程池重建
TTL
随机 TTL
```

---

# 十七、任务 10：GEO 轻量迁移

如果当前存在：

```text
附近商铺
```

迁移为：

```text
附近活动
```

继续使用：

```text
Activity.x
Activity.y
Redis GEO
```

算法不重写。

---

# 十八、任务 11：Lua 与异步订单只做必要兼容修改

Lua 允许：

```text
voucherId
-> ticketId
```

以及相关库存 key 语义迁移。

禁止：

```text
重写脚本
改变一人一单算法
改消息结构设计
改库存判断算法
```

BlockingQueue / Redis Stream 中：

```text
VoucherOrder
-> ReservationOrder

voucherId
-> ticketId
```

仅做类型兼容迁移。

---

# 十九、Redisson 迁移边界

如存在：

```text
RLock
tryLock
```

只同步相关实体 / 方法名。

锁的：

```text
粒度
key 设计
tryLock 流程
finally unlock
```

不要重构。

---

# 二十、User / 登录模块绝对不动

禁止修改：

```text
User
UserDTO
UserHolder
LoginInterceptor
RefreshTokenInterceptor
sendCode
login
logout
```

Redis Token 方案保持。

---

# 二十一、Blog / Follow 不动

本阶段不要：

```text
Blog -> Post
BlogComments -> PostComment
```

不要修改：

```text
Feed
点赞
共同关注
```

这些留到 Phase 4。

---

# 二十二、AI 模块处理规则

如果 core Entity / Mapper 改名导致 AI 无法编译：

只做类型兼容迁移：

```text
Shop -> Activity
Voucher -> Ticket
VoucherOrder -> ReservationOrder
```

以及相应 Mapper 类型。

不要主动重写：

```text
system prompt
AI Tool 业务能力
RAG
预约流程
复杂 SQL
```

除非不改就无法编译。

---

# 二十三、执行顺序

严格建议：

```text
Step 1
cd backend
mvn clean compile

Step 2
生成旧核心领域依赖清单

Step 3
ShopType -> ActivityCategory
Shop -> Activity

Step 4
mvn clean compile

Step 5
Voucher -> Ticket
SeckillVoucher -> SeckillTicket
VoucherOrder -> ReservationOrder

Step 6
同步 Mapper / Service / Controller / XML / Lua 必要字段 / 异步订单类型

Step 7
mvn clean compile

Step 8
修改 cityhub_schema.sql

Step 9
修改数据库连接名为 cityhub

Step 10
同步前端必要 API 路径

Step 11
mvn clean compile

Step 12
扫描旧实体名称

Step 13
检查 User / Blog / Follow / 缓存算法 / 登录 / Lua / Redisson 是否误改

Step 14
生成报告
```

---

# 二十四、禁止机械全局替换

禁止：

```text
shop
全局替换
activity
```

必须按：

```text
Java symbol
SQL table
API route
Redis key
```

逐层迁移。

---

# 二十五、Maven 编译验收

最终必须：

```bash
cd backend
mvn clean compile
```

并且：

```text
CityHub
CityHub Core
CityHub AI
```

全部 SUCCESS。

---

# 二十六、运行验证

如果 MySQL / Redis 环境可用：

尝试启动：

```text
CityHubApplication
```

至少验证 Spring Context 正常启动。

如果外部环境不可用：

记录原因即可，不要为了启动成功修改业务。

---

# 二十七、数据库验收

新的：

```text
cityhub_schema.sql
```

至少包含：

```text
tb_activity_category
tb_activity
tb_ticket
tb_seckill_ticket
tb_reservation_order
```

并继续保留：

```text
tb_user
tb_blog
tb_follow
```

等现有业务需要表。

---

# 二十八、Phase 3A 完成目标

```text
CityHub Core
│
├── User
├── ActivityCategory
├── Activity
├── Ticket
├── SeckillTicket
├── ReservationOrder
│
├── Blog
├── BlogComments
├── Follow
│
├── Redis Token
├── CacheClient
├── RedisIdWorker
├── Redisson
├── Lua
└── 异步预约
```

Blog / Follow 仍是旧语义，属于刻意保留。

---

# 二十九、本阶段明确不要求

不要扩大到：

```text
缓存功能完整测试
秒杀压测
Redis Stream 完整可靠性验证
前端页面美化
AI 业务完全迁移
Blog / Follow 改造
Docker
单元测试体系
JMeter
README
简历
```

---

# 三十、本阶段交付文件

创建：

```text
docs/refactor/phase3a/
```

生成：

```text
PHASE3A_REPORT.md
DOMAIN_RENAME_MAPPING.md
DATABASE_MAPPING.md
CORE_LOGIC_PRESERVATION_CHECK.md
```

---

# 三十一、CORE_LOGIC_PRESERVATION_CHECK.md 必须检查

## 缓存穿透

```text
是否仍为缓存空值
核心逻辑是否变化
```

## 缓存击穿

```text
是否仍为逻辑过期
是否仍使用互斥锁
是否仍异步重建
```

## 缓存雪崩

```text
当前是否实际存在随机 TTL
```

如果原代码没有：

明确写：

```text
当前实际代码未实现，Phase 3A 未新增。
```

不要伪造。

## 缓存一致性

```text
是否仍为更新 DB -> 删除缓存
```

## 一人一单

```text
是否仅从 voucherId 迁移为 ticketId
```

## Lua

```text
库存判断是否保留
一人一单是否保留
预扣库存是否保留
```

## Redisson

```text
是否保留
锁粒度是否变化
```

## RedisIdWorker

```text
是否保留
```

## 异步秒杀

```text
当前实际实现：
BlockingQueue / Redis Stream / 其他

Phase 3A 是否仅做类型迁移
```

## 登录

```text
Redis Token 是否未修改
双拦截器是否未修改
```

---

# 三十二、PHASE3A_REPORT.md 结构

必须包含：

```text
1. 阶段结论
2. 核心领域迁移
3. 数据库迁移
4. API 迁移
5. 核心技术保持情况
6. Maven 验证
7. Spring 启动验证
8. 本阶段未修改内容
9. 已知问题
10. 下一阶段建议
```

下一阶段只建议：

> **Phase 3B：活动查询与缓存业务适配验证**

主要验证：

```text
活动分类
活动列表
活动详情
附近活动
Activity Redis 缓存
缓存穿透
缓存击穿
缓存一致性
```

本阶段不要提前实施 Phase 3B。

---

# 三十三、验收标准

Phase 3A 通过必须满足：

```text
ShopType / Shop
->
ActivityCategory / Activity

Voucher / SeckillVoucher / VoucherOrder
->
Ticket / SeckillTicket / ReservationOrder

数据库：
cityhub + 新核心表名

mvn clean compile：
PASS

核心技术未主动重写：
缓存空值
逻辑过期
一人一单
Lua
Redisson
RedisIdWorker
异步下单
Redis Token

Blog / Follow：
保持不动

README：
不修改
```

---

# 三十四、最终目标再次强调

本阶段关键词：

> **轻量迁移，而不是重写。**

最终希望实现：

```text
商铺 + 优惠券 + 秒杀
        ↓
活动 + 预约凭证 + 限量预约
```

同时保留用户已经熟悉并准备面试的：

```text
缓存穿透
缓存击穿
缓存雪崩
缓存一致性
Redis Token
RedisIdWorker
Lua
一人一单
Redisson
异步秒杀
Redis Stream（如果当前已有）
```

优先保证：

> **项目尽快完成、关键业务能跑、核心技术能讲清楚。**
