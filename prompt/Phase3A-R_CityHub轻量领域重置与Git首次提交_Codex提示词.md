# Codex 执行任务：Phase 3A-R CityHub 轻量领域基线重置 + Git 首次提交

## 一、任务背景

当前项目已经完成：

- Phase 1：基础工程基线治理；
- Phase 2：CityHub 项目身份规范化。

当前正式项目名称：

> **CityHub - 城市活动发现与预约平台**

当前 GitHub 仓库：

```text
https://github.com/Zst2001/CityHub
```

本地已经建立 Git 仓库，但目前 **尚未进行第一次 commit**。

---

# 二、当前存在的架构冲突

在最新 Phase 3 方案确定前，本地源码中已经提前实现了一套更复杂的领域模型，包括：

```text
Venue
Activity
ActivitySession
ReservationOrder(userId + sessionId)
```

以及：

```text
/venues
/activities
/activities/{id}/sessions
```

等查询链路。

但现在正式决定：

> **不再采用 Venue / ActivitySession 复杂领域模型。**

原因：

1. 当前项目目标是优先完成；
2. 用户已经熟悉黑马点评的 Redis、缓存、一人一单、Lua、Redisson、异步秒杀等实现；
3. 不希望因为重新设计 ActivitySession / Reservation 体系而大幅改写已经掌握的技术链；
4. 第一版 CityHub 只需要关键业务能跑、前端展示完整、面试时能完整解释。

因此，Phase 3 正式架构重新确定为“轻量迁移方案”。

---

# 三、Phase 3 正式唯一领域基线

后续只采用：

```text
ShopType        -> ActivityCategory
Shop            -> Activity

Voucher         -> Ticket
SeckillVoucher  -> SeckillTicket
VoucherOrder    -> ReservationOrder
```

核心结构：

```text
ActivityCategory
       │
       ▼
    Activity
       │
       ▼
     Ticket
       │
       ▼
 SeckillTicket
       │
       ▼
ReservationOrder
       │
       ▼
      User
```

社区：

```text
Blog
BlogComments
Follow
```

暂时保持原样。

---

# 四、最重要的技术约束

本次重置的目的不是重新实现黑马点评核心技术，而是尽可能保留用户已经熟悉的实现。

以下逻辑原则上必须保留：

```text
缓存穿透：
缓存空值

缓存击穿：
逻辑过期 + 互斥锁 + 独立线程异步重建 + 热点预热

缓存雪崩：
TTL 随机值（如果当前代码已经真实实现）
如当前未实现，不在本阶段强行新增，只记录

缓存一致性：
先更新 MySQL，再删除 Redis
TTL 兜底

Redis 登录：
验证码 + Token + Redis Hash + ThreadLocal + 双拦截器

RedisIdWorker：
继续使用

一人一单：
userId + ticketId

Lua：
库存判断 + 一人一单 + 预扣库存

Redisson：
保留当前用户级分布式锁实现

异步秒杀：
保留当前真实实现
BlockingQueue 或 Redis Stream
不要更换消息中间件
```

禁止新增：

```text
Kafka
RabbitMQ
RocketMQ
复杂 DDD
Venue
ActivitySession
支付
退款
核销
复杂预约状态机
```

---

# 五、Git 策略：当前没有第一次 commit，先保护工作区

## 重要原则

当前错误/废弃的 Venue / ActivitySession Phase 3 实现：

> **不要作为 CityHub 的第一次正式 Git commit 提交到 main。**

因为当前还没有 commit，可以利用这一点让仓库首个正式版本更加干净。

---

# 六、Git Step 1：确认仓库状态

任务开始后先执行：

```bash
git rev-parse --show-toplevel
git status --short
git branch --show-current
git remote -v
```

确认：

1. 当前目录确实位于 CityHub Git 工作区；
2. `.git` 存在；
3. 当前尚无正式提交；
4. `origin` 是否已配置。

检查是否存在 HEAD：

```bash
git rev-parse --verify HEAD
```

如果返回失败且提示当前没有 commit，这是预期状态。

---

# 七、Git Step 2：检查远程仓库

期望远程：

```text
https://github.com/Zst2001/CityHub.git
```

如果没有 `origin`：

```bash
git remote add origin https://github.com/Zst2001/CityHub.git
```

如果已经存在 `origin`：

```bash
git remote get-url origin
```

### 如果 URL 正确

继续。

### 如果 URL 不同

不要自动覆盖。

停止 Git remote 修改，并在最终报告中说明。

---

## 检查远程是否已有分支

执行：

```bash
git ls-remote --heads origin
```

如果没有任何 head：

说明远程没有需要合并的分支，可以在最终完成后执行首次 push。

如果远程已经存在 `main` / `master`：

禁止：

```bash
git push --force
git push -f
```

先记录情况，后续只在安全确认后处理。

---

# 八、Git Step 3：修改前做仓库外备份

因为没有历史 commit 可以回退，本阶段在任何删除 / 覆盖当前 Phase 3 代码之前，必须做一次 **仓库外备份**。

先：

```bash
REPO_ROOT="$(git rev-parse --show-toplevel)"
echo "$REPO_ROOT"
```

在仓库父目录创建备份，例如：

```bash
cd "$(dirname "$REPO_ROOT")"
tar --exclude='.git' -czf CityHub_pre_phase3_reset.tar.gz "$(basename "$REPO_ROOT")"
```

Windows 环境如果无法使用 `tar`：

使用 PowerShell：

```powershell
Compress-Archive -Path .\CityHub\* -DestinationPath .\CityHub_pre_phase3_reset.zip
```

要求：

- 备份文件必须放在仓库外；
- 不要加入 Git；
- 最终报告记录备份路径；
- 不要把密码、API Key 等重新复制进仓库。

---

# 九、正式修改前：识别当前 Phase 3 代码

在删除任何文件之前，完整分析当前：

```text
Venue
Activity
ActivitySession
ReservationOrder
```

相关代码。

至少搜索：

```bash
rg -n "\bVenue\b|\bActivitySession\b|\bsessionId\b|session_id|/venues|/activities/.*/sessions"
```

并阅读：

```text
Entity
Controller
Service
ServiceImpl
Mapper
Mapper XML
SQL
DTO
VO
前端 API
AI 引用
```

如果存在：

```text
docs/refactor/phase3a
docs/refactor/phase3b
```

等旧 Phase 3 报告，也要阅读，用于判断哪些文件属于当前已经放弃的 Phase 3 实现。

---

# 十、生成 RESET_PLAN.md 后才能删除

先创建：

```text
docs/refactor/phase3a_reset/RESET_PLAN.md
```

记录：

## A. 当前需要撤回 / 重写

例如：

```text
Venue*
ActivitySession*
当前 sessionId ReservationOrder
/venues
/activities/{id}/sessions
venue / activity_session SQL
```

必须填写真实文件路径。

## B. Phase 1 / Phase 2 必须保留

例如：

```text
CityHub Maven 坐标
com.cityhub
CityHubApplication
CityHubAiApplication
.env.example
敏感配置环境变量化
.gitignore
工程卫生治理
```

## C. 当前旧 Shop / Voucher 核心实现是否仍存在

分别回答：

```text
ShopType：
Shop：
Voucher：
SeckillVoucher：
VoucherOrder：
```

并记录实际文件。

## D. 轻量迁移后的目标

```text
ActivityCategory
Activity
Ticket
SeckillTicket
ReservationOrder(ticketId)
```

确认没有误删 Phase 1 / Phase 2 的风险后再开始执行。

---

# 十一、如果旧 Shop / Voucher 源码已经不存在

不要凭记忆重新创造完整业务。

先搜索：

```text
docs/legacy
历史 SQL
当前旧业务副本
旧 Phase 报告
项目中仍存在的引用
```

如果能够从当前仓库真实材料恢复：

可以继续。

如果旧 Shop / Voucher 关键实现已经彻底不存在，且无法安全恢复：

> 停止本阶段业务重构，不要猜测实现。

在终端明确报告需要用户确认。

---

# 十二、当前复杂 Phase 3 的撤回范围

正式轻量迁移不再使用：

```text
Venue
ActivitySession
```

以及以：

```text
userId + sessionId
```

为核心的 ReservationOrder 模型。

需要撤回：

```text
Venue Entity / Controller / Service / Mapper / XML

ActivitySession Entity / Controller / Service / Mapper / XML

仅为 Venue / ActivitySession 新增的 DTO / VO

/venues

/activities/{id}/sessions

venue 表
activity_session 表

ReservationOrder.sessionId / session_id

userId + sessionId 唯一约束
```

---

# 十三、撤回时禁止误删

不要误删 Phase 1 / Phase 2：

```text
com.cityhub
CityHubApplication
CityHubAiApplication

cityhub-parent
cityhub-core
cityhub-ai

环境变量配置
.env.example
.gitignore

docs/refactor/phase1
docs/refactor/phase2
```

不要修改 README。

---

# 十四、轻量领域迁移：ShopType -> ActivityCategory

目标：

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

以真实源码命名风格为准。

数据库：

```text
tb_shop_type
->
tb_activity_category
```

API：

```text
/shop-type
->
/activity-category
```

字段尽量保留：

```text
id
name
icon
sort
createTime
updateTime
```

---

# 十五、轻量领域迁移：Shop -> Activity

目标：

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

---

## Activity 字段原则

尽量从 Shop 直接迁移：

```text
typeId
-> categoryId

name
-> title
```

其他当前已有字段尽量保留：

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

业务语义：

```text
avgPrice  = 参考价格 / 活动平均费用
sold      = 已预约 / 已参与数
comments  = 评论数
score     = 活动评分
openHours = 活动举办时间 / 开放时间
```

不要重新引入：

```text
Venue
ActivitySession
venueId
sessionId
```

---

# 十六、Ticket：Voucher 轻量迁移

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

核心：

```text
shopId
->
activityId
```

其他字段尽量保持当前结构：

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

Ticket 含义：

> CityHub 活动预约凭证。

第一版不设计复杂支付票务体系。

---

# 十七、SeckillTicket：SeckillVoucher 轻量迁移

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

关键字段：

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

---

# 十八、ReservationOrder：VoucherOrder 轻量迁移

当前复杂 Phase 3 的：

```text
ReservationOrder(userId + sessionId)
```

不再采用。

正式目标：

```text
VoucherOrder
->
ReservationOrder
```

字段核心：

```text
id
userId
ticketId
createTime
updateTime
```

如果旧 VoucherOrder 存在当前业务依赖的额外字段：

```text
status
payType
```

等：

不要为了简洁主动删除。

---

## 一人一单

正式恢复为：

```text
userId + ticketId
```

业务语义：

> 一个用户针对同一个限量活动预约凭证只能预约一次。

---

# 十九、数据库迁移

正式数据库名：

```text
cityhub
```

核心表：

```text
tb_activity_category
tb_activity
tb_ticket
tb_seckill_ticket
tb_reservation_order
```

继续保留：

```text
tb_user
tb_blog
tb_blog_comments
tb_follow
```

以及 Blog / Follow 仍然真实依赖的表。

删除当前废弃 Phase 3 独有：

```text
venue
activity_session
```

相关建表。

---

# 二十、数据库初始化文件

继续维护：

```text
backend/core/src/main/resources/db/cityhub_schema.sql
```

要求：

- SQL 与新 Entity / Mapper 一致；
- 数据库名统一为 cityhub；
- 不新增 Flyway；
- 不新增 Liquibase；
- 第一版直接重新初始化数据库即可。

---

# 二十一、示例业务数据

ActivityCategory 可以使用：

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

Activity 少量示例：

```text
城市青年创意市集
夏日爵士音乐会
当代摄影艺术展
周末陶艺体验课
城市文化讲座
```

不要大量造数据。

---

# 二十二、API 路由

正式统一：

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

方法：

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

---

# 二十三、缓存实现：保留方案，只改 Activity 语义

如果存在：

```text
CACHE_SHOP_KEY
CACHE_SHOP_TTL
SHOP_GEO_KEY
```

迁移：

```text
CACHE_ACTIVITY_KEY
CACHE_ACTIVITY_TTL
ACTIVITY_GEO_KEY
```

Redis 实际 key：

```text
cache:shop:
-> cache:activity:
```

如当前代码确实这样命名。

---

## 禁止改变

```text
CacheClient
RedisData
缓存空值
逻辑过期
互斥锁
异步重建缓存
TTL
```

---

# 二十四、缓存穿透必须保持

```text
Redis miss
↓
DB miss
↓
写空值 + TTL
```

不要换成 Bloom Filter。

---

# 二十五、缓存击穿必须保持

```text
逻辑过期
+
互斥锁
+
独立线程重建
+
返回旧值
```

不要重写算法。

---

# 二十六、缓存雪崩

如果当前已有：

```text
随机 TTL
```

必须保留。

如果当前实际上还没实现：

不要为了 Phase 3A 新增复杂代码。

在报告中标记：

```text
当前代码未实现随机 TTL，后续 Phase 3B 补齐。
```

---

# 二十七、缓存一致性

保持：

```text
更新 MySQL
↓
删除 Redis
```

如原有：

```text
@Transactional
```

继续保留。

---

# 二十八、Lua / 一人一单 / 异步秒杀

只做语义兼容：

```text
voucherId
-> ticketId

VoucherOrder
-> ReservationOrder
```

Lua 核心必须保留：

```text
判断库存
判断一人一单
预扣库存
记录用户
```

---

## 当前异步实现

先检查实际代码属于：

```text
BlockingQueue
Redis Stream
其他
```

保留现有实现。

禁止换：

```text
Kafka
RabbitMQ
RocketMQ
```

---

# 二十九、Redisson

保留：

```text
RLock
tryLock
finally unlock
```

以及当前锁粒度。

不要重新设计。

---

# 三十、RedisIdWorker

继续使用：

```text
RedisIdWorker
```

生成：

```text
ReservationOrder.id
```

---

# 三十一、登录完全不动

禁止修改：

```text
User
UserDTO
UserHolder

sendCode
login

LoginInterceptor
RefreshTokenInterceptor

Redis Token
ThreadLocal
```

---

# 三十二、Blog / Follow 完全不动

本阶段不要：

```text
Blog -> Post
BlogComments -> PostComment
```

不要改：

```text
点赞
Feed
共同关注
```

---

# 三十三、AI 模块

只处理因 Java 类型重命名导致的：

```text
compile error
```

不要主动重构：

```text
AI Tool
RAG
预约语义
Prompt
```

如果旧 Tool 暂时仍然使用旧业务语义但可以编译：

先保留，后续 AI 阶段统一改。

---

# 三十四、前端

只修：

```text
API 地址变化导致的必要请求路径
```

本阶段不要进行：

```text
页面美化
首页重做
CSS 大改
活动详情 UI 重做
```

前端视觉后续单独处理。

---

# 三十五、执行过程中的编译节点

开始：

```bash
cd backend
mvn clean compile
```

确认当前基线。

---

## 撤回 Venue / ActivitySession 后

执行：

```bash
mvn clean compile
```

如果此时因为还未完成轻量领域迁移而必然失败：

记录具体原因，立即继续迁移，不要进行无关修复。

---

## Activity 迁移完成后

```bash
mvn clean compile
```

---

## Ticket / ReservationOrder 迁移后

```bash
mvn clean compile
```

---

## 最终

```bash
mvn clean compile
```

必须 PASS。

---

# 三十六、运行验证

如果 MySQL / Redis 环境可用：

启动：

```text
CityHubApplication
```

至少验证 Spring Context。

如果环境不可用：

记录原因即可。

---

# 三十七、最终旧架构残留扫描

执行：

```bash
rg -n "\bVenue\b|\bActivitySession\b|\bsessionId\b|session_id|/venues|/activities/.*/sessions"
```

运行时代码中原则上不应继续保留废弃核心架构。

历史：

```text
README
docs
legacy report
```

可保留。

---

## 旧黑马实体扫描

执行：

```bash
rg -n "\bShopType\b|\bShop\b|\bVoucher\b|\bSeckillVoucher\b|\bVoucherOrder\b"
```

运行时代码中核心业务应尽量完成新语义迁移。

README / docs / legacy 可保留。

---

# 三十八、核心逻辑保护报告

创建：

```text
docs/refactor/phase3a/CORE_LOGIC_PRESERVATION_CHECK.md
```

必须逐项填写：

```text
缓存穿透
缓存击穿
缓存雪崩
缓存一致性
Redis Token
Lua
一人一单
Redisson
RedisIdWorker
异步下单
Redis Stream（如存在）
```

对于每一项：

```text
迁移前真实实现
迁移后真实实现
是否改变算法
涉及文件
```

禁止为了通过验收伪造“已实现”。

---

# 三十九、Phase 3A 报告

生成：

```text
docs/refactor/phase3a/PHASE3A_REPORT.md
docs/refactor/phase3a/DOMAIN_RENAME_MAPPING.md
docs/refactor/phase3a/DATABASE_MAPPING.md
docs/refactor/phase3a/CORE_LOGIC_PRESERVATION_CHECK.md
```

报告必须明确：

```text
Venue / ActivitySession 是否已撤回
ReservationOrder 是否恢复 ticketId
Maven compile 是否 PASS
Spring 是否启动
数据库是否 cityhub
核心缓存 / 秒杀算法是否保持
```

---

# 四十、Git 首次提交前检查

只有 Phase 3A 代码验收通过后，才开始第一次正式 Git commit。

先：

```bash
git status --short
```

确认：

- `.env` 未被跟踪；
- 真实密码 / API Key 未被加入；
- target / log / IDE 文件未被加入；
- 仓库外备份没有被加入。

---

## 敏感信息快速检查

至少搜索：

```bash
rg -n -i "password|api[_-]?key|secret|sk-[A-Za-z0-9]"
```

结合内容判断。

不要因为变量名：

```text
DB_PASSWORD
```

是合法环境变量就误删。

重点确认没有真实值。

---

# 四十一、Git staging

执行：

```bash
git add .
git status --short
```

由于这是首次 commit，可以使用：

```bash
git diff --cached --check
git diff --cached --stat
```

检查即将提交的完整内容。

如果发现：

```text
.env
真实密钥
target
logs
IDE 文件
仓库外备份
```

立即停止 commit 并修复。

---

# 四十二、Git 首次 commit

只有：

```text
mvn clean compile PASS
敏感信息检查 PASS
staged diff 检查 PASS
```

后才执行：

```bash
git commit -m "feat: establish CityHub activity reservation domain"
```

如果 Git 用户信息未配置：

不要随意填写假的身份。

报告需要用户配置：

```bash
git config user.name "你的 GitHub 用户名"
git config user.email "你的 GitHub 邮箱"
```

然后再 commit。

---

# 四十三、Git 主分支

commit 成功后：

```bash
git branch -M main
```

---

# 四十四、Git push

再次确认：

```bash
git remote -v
git ls-remote --heads origin
```

如果远程为空且 origin 正确：

```bash
git push -u origin main
```

---

## 如果认证失败

不要修改凭证文件。

不要把 Personal Access Token 写进：

```text
脚本
YAML
README
.env.example
```

只报告：

```text
GitHub authentication failed
```

并保留本地 commit。

---

## 如果远程已有 commit

禁止：

```bash
git push --force
git push -f
```

停止 push，报告远程状态。

---

# 四十五、Git 完成后验证

执行：

```bash
git status
git log --oneline -n 5
git remote -v
```

目标：

```text
working tree clean
```

如果 push 成功：

报告：

```text
branch: main
remote: origin
commit: <hash>
push: success
```

---

# 四十六、不要求创建 GitHub README

即使远程当前为空：

本阶段仍然：

> **不要重写 README。**

README 在整个项目完成后统一编写。

---

# 四十七、最终验收标准

## 架构

正式使用：

```text
ActivityCategory
Activity
Ticket
SeckillTicket
ReservationOrder(ticketId)
```

不再使用：

```text
Venue
ActivitySession
ReservationOrder(sessionId)
```

---

## 核心技术

不主动重写：

```text
缓存空值
逻辑过期
缓存一致性
Redis Token
Lua
一人一单
Redisson
RedisIdWorker
异步秒杀
```

---

## 数据库

```text
cityhub
```

核心表：

```text
tb_activity_category
tb_activity
tb_ticket
tb_seckill_ticket
tb_reservation_order
```

---

## 编译

```text
backend/mvn clean compile
PASS
```

---

## Git

在条件允许时：

```text
首次 commit 完成
main 分支
origin 指向：
https://github.com/Zst2001/CityHub.git
首次 push 成功
```

如果认证 / 远程冲突导致不能 push：

必须保留本地 commit，并明确报告，不得 force push。

---

# 四十八、最终回复格式

完成后输出：

```text
Phase 3A-R 轻量领域基线重置完成。

【架构】
1. Venue / ActivitySession：
2. Activity：
3. ActivityCategory：
4. Ticket：
5. SeckillTicket：
6. ReservationOrder：
7. ReservationOrder 核心字段：

【核心技术】
8. 缓存穿透：
9. 缓存击穿：
10. 缓存雪崩：
11. 缓存一致性：
12. Lua：
13. 一人一单：
14. Redisson：
15. RedisIdWorker：
16. 异步秒杀：
17. Redis Token 登录：

【验证】
18. Maven compile：
19. Spring 启动：
20. 数据库：

【Git】
21. 修改前仓库外备份：
22. Git commit：
23. commit hash：
24. branch：
25. origin：
26. push：

【未修改】
27. Blog / Follow：
28. README：
29. 前端视觉：
30. AI 深度业务：

详细报告：
docs/refactor/phase3a/PHASE3A_REPORT.md
```

---

# 四十九、最终原则

当前项目的目标不是把所有黑马点评实现彻底隐藏。

目标是：

> **用 CityHub 的活动发现与预约业务承载用户已经熟悉的 Redis、高并发、缓存和秒杀技术，优先形成一个能运行、能展示、能讲清楚的实习项目。**

因此：

```text
领域语义可以迁移
核心算法不要随意重写
业务不要过度复杂
前端后续重点美化
项目完成优先于架构完美
```
