# Codex 执行任务：Phase 3B CityHub 活动发现与查询链路

## 一、任务背景

当前项目已经完成：

- Phase 1：基础工程基线治理；
- Phase 2：项目身份规范化；
- Phase 3A：CityHub 核心领域模型与数据库骨架。

当前工程身份：

> **CityHub - 城市活动发现与预约平台**

Phase 3A 已经建立以下新领域：

```text
Venue
ActivityCategory
Activity
ActivitySession
ReservationOrder
```

并继续复用现有：

```text
User
```

已经存在对应：

```text
Entity
Mapper
cityhub_domain_v1.sql
```

并且满足：

```text
Venue 1:N Activity
ActivityCategory 1:N Activity
Activity 1:N ActivitySession
User 1:N ReservationOrder
ActivitySession 1:N ReservationOrder
```

其中：

```text
capacity
remainingQuota
bookingStartTime
bookingEndTime
```

全部属于 `ActivitySession`。

本阶段开始第一次真正实现 CityHub 新业务的“读链路”。

---

# 二、本阶段的核心目标

Phase 3B 定义为：

> **CityHub 活动发现与查询链路**

最终需要支持：

```text
进入 CityHub
    ↓
查看活动分类
    ↓
浏览活动列表
    ↓
查看活动详情
    ↓
查看场馆信息
    ↓
查看活动可预约场次
```

本阶段只做查询。

不做预约写入。

---

# 三、本阶段必须实现的接口

至少实现以下 6 个核心接口。

---

## 1. 活动分类列表

```http
GET /activity-categories
```

要求：

- 只返回有效分类；
- 默认按 `sort ASC`；
- 不返回无关字段；
- 使用 VO 返回。

建议返回字段：

```text
id
name
icon
sort
```

---

## 2. 场馆列表

```http
GET /venues
```

第一版支持：

```text
city
```

筛选。

例如：

```http
GET /venues?city=上海
```

要求：

- 只返回有效场馆；
- city 参数为空时返回可用场馆列表；
- 暂时不做 Redis GEO；
- 暂时不做距离排序；
- 暂时不做复杂地图搜索。

---

## 3. 场馆详情

```http
GET /venues/{id}
```

返回：

```text
id
name
description
city
district
address
longitude
latitude
coverUrl
phone
status
```

要求：

- 场馆不存在时返回明确业务错误；
- 无效/禁用场馆是否允许查看，请结合当前状态语义选择合理方案；
- 第一版优先只展示 ACTIVE 场馆。

---

## 4. 活动分页列表

核心接口：

```http
GET /activities
```

建议支持：

```text
page
size
categoryId
venueId
city
```

示例：

```http
GET /activities?page=1&size=10&categoryId=2&city=上海
```

要求：

- 默认 `page = 1`；
- 默认 `size = 10`；
- `size` 建议限制最大值，例如 `50`；
- 只查询 `PUBLISHED` 活动；
- 默认按 `publish_time DESC`；
- 支持按 categoryId 筛选；
- 支持按 venueId 筛选；
- 支持按 city 筛选；
- 不做全文搜索；
- 不做 ES；
- 不做推荐算法；
- 不做 Redis 缓存；
- 不做热度排序。

---

## 5. 活动详情

```http
GET /activities/{id}
```

要求不能直接返回 `Activity Entity`。

必须使用：

```text
ActivityDetailVO
```

至少包含：

```text
activity 基础信息
category 信息
venue 信息
```

建议结构类似：

```json
{
  "id": 1001,
  "title": "城市影像艺术展",
  "subtitle": "...",
  "description": "...",
  "coverUrl": "...",
  "organizer": "...",
  "publishTime": "...",
  "category": {
    "id": 1,
    "name": "展览"
  },
  "venue": {
    "id": 10,
    "name": "城市艺术中心",
    "city": "上海",
    "district": "...",
    "address": "..."
  }
}
```

不要把 Session 列表塞进该接口。

---

## 6. 活动场次列表

```http
GET /activities/{id}/sessions
```

要求：

- 按 `start_time ASC`；
- 返回 `ActivitySessionVO`；
- 至少返回：

```text
id
activityId
startTime
endTime
bookingStartTime
bookingEndTime
capacity
remainingQuota
status
```

本阶段只展示场次。

不要判断：

```text
当前用户是否已预约
是否有预约资格
Redis 库存
Lua
锁
```

---

# 四、DTO / VO 设计

本阶段开始正式建立接口层的数据对象。

不要直接把 Entity 暴露给前端。

至少建议新增：

```text
ActivityPageQuery
```

以及：

```text
VenueVO
ActivityCategoryVO
ActivityListVO
ActivityDetailVO
ActivitySessionVO
```

如果当前项目已有：

```text
dto
vo
query
```

目录规范，请沿用。

不要新建复杂 DDD 目录。

---

## ActivityPageQuery

建议字段：

```text
Integer page
Integer size
Long categoryId
Long venueId
String city
```

要求：

- 处理空值；
- page < 1 时合理校验；
- size <= 0 时合理校验；
- size 最大值做限制；
- 不要把排序字段开放给前端任意传入，避免无意义复杂化。

---

# 五、ActivityListVO

活动列表不要只返回 Activity。

建议至少包括：

```text
id
title
subtitle
coverUrl
organizer
publishTime

categoryId
categoryName

venueId
venueName
city
district
```

如果根据当前前端展示需求需要少量补充，可以增加。

不要返回：

```text
description 全文
无关状态内部字段
大量内部时间
```

---

# 六、避免 N+1 查询

这是本阶段的重要工程要求。

活动分页列表需要同时展示：

```text
Activity
Venue
ActivityCategory
```

禁止采用：

```text
先查 10 个 Activity
for 每个 Activity：
    查询一次 Venue
    查询一次 Category
```

禁止形成典型：

```text
1 + N + N
```

查询。

---

## 推荐方案

在：

```text
ActivityMapper
```

新增自定义分页查询。

通过：

```sql
activity
LEFT JOIN venue
LEFT JOIN activity_category
```

一次返回：

```text
ActivityListVO
```

要求：

- 分页正确；
- 条件筛选正确；
- 动态 SQL 安全；
- 不拼接用户输入 SQL；
- 兼容当前 MyBatis/MyBatis-Plus 版本。

---

# 七、ActivityDetail 查询

活动详情同样建议一次联表获取：

```text
Activity
+
Venue
+
ActivityCategory
```

映射：

```text
ActivityDetailVO
```

不要在 Service 里：

```text
select Activity
select Venue
select Category
```

做三次独立查询，除非结合当前代码结构确实有合理原因。

优先一条联表 SQL。

---

# 八、ActivitySession 查询

Session 是 Activity 的子资源。

建议不要新增：

```text
ActivitySessionController
```

本阶段可直接在：

```text
ActivityController
```

提供：

```http
GET /activities/{id}/sessions
```

Service 可独立：

```text
IActivitySessionService
ActivitySessionServiceImpl
```

Mapper 可直接使用已有 BaseMapper 查询。

如果需要自定义 SQL，可以添加，但不要过度设计。

---

# 九、Service 设计

建议新增：

```text
IVenueService
VenueServiceImpl

IActivityCategoryService
ActivityCategoryServiceImpl

IActivityService
ActivityServiceImpl

IActivitySessionService
ActivitySessionServiceImpl
```

如果当前项目命名不使用 `I` 前缀，请遵循真实代码风格。

不要机械创建重复风格。

---

## VenueService

负责：

```text
场馆列表
场馆详情
```

---

## ActivityCategoryService

负责：

```text
有效分类列表
```

---

## ActivityService

负责：

```text
活动分页
活动详情
```

这是本阶段主要 Service。

---

## ActivitySessionService

负责：

```text
某活动的场次列表
```

---

# 十、Controller 设计

建议新增：

```text
VenueController
ActivityCategoryController
ActivityController
```

其中：

### VenueController

```text
GET /venues
GET /venues/{id}
```

### ActivityCategoryController

```text
GET /activity-categories
```

### ActivityController

```text
GET /activities
GET /activities/{id}
GET /activities/{id}/sessions
```

---

# 十一、统一返回结构

优先复用当前已有：

```text
Result
```

或项目现有统一返回结构。

不要重新新增第二套：

```text
ApiResponse
ResponseResult
CommonResult
```

避免重复。

---

# 十二、异常处理

请先检查当前项目是否已有：

```text
GlobalExceptionHandler
@ControllerAdvice
BusinessException
```

---

## 如果已有

优先复用。

---

## 如果没有

本阶段允许新增最小化：

```text
BusinessException
GlobalExceptionHandler
```

只处理当前必要问题，例如：

```text
活动不存在
场馆不存在
参数非法
```

不要设计：

```text
上百个错误码
复杂国际化
复杂异常层次
```

---

# 十三、公开接口与登录拦截器

当前项目已有：

```text
Redis Token
UserHolder
登录拦截器
```

本阶段必须检查：

```text
MvcConfig
Interceptor
LoginInterceptor
RefreshTokenInterceptor
```

或实际对应配置。

---

## 目标

活动浏览接口应允许未登录用户访问：

```text
GET /activity-categories
GET /venues
GET /venues/{id}
GET /activities
GET /activities/{id}
GET /activities/{id}/sessions
```

---

## 重要

不要为了方便直接永久放开未来所有：

```text
/activities/**
```

写操作。

当前 Phase 3B 只有 GET。

请根据现有拦截器能力做最小、合理处理。

如果现有拦截器只能基于路径：

可以在报告中明确：

```text
当前仅有查询接口，因此暂时排除这些路径；
未来管理写接口需重新细化权限策略。
```

---

# 十四、数据库实际落地

Phase 3A 只生成了：

```text
cityhub_domain_v1.sql
```

但没有在数据库执行。

Phase 3B 开始真实查询，因此必须处理数据库落地问题。

---

## 优先策略

如果 Codex 当前环境能够安全访问开发 MySQL：

在当前开发数据库中执行：

```text
cityhub_domain_v1.sql
```

新增：

```text
venue
activity_category
activity
activity_session
reservation_order
```

不要：

```text
DROP DATABASE
DROP 旧表
删除旧业务
```

---

## 如果 Codex 环境不能安全访问数据库

不要虚报执行成功。

必须：

1. 保持 SQL 文件；
2. 生成 seed SQL；
3. 在最终报告里明确：

```text
未实际执行数据库；
需要用户手动执行：
cityhub_domain_v1.sql
cityhub_domain_seed.sql
```

并给出命令或执行顺序。

---

# 十五、开发测试数据

本阶段建议新增：

```text
backend/core/src/main/resources/db/cityhub_domain_seed.sql
```

用于新领域查询测试。

建议数据规模：

```text
5 个 ActivityCategory
3 个 Venue
8~10 个 Activity
每个 Activity 2~4 个 ActivitySession
```

---

## 示例分类

可使用：

```text
展览
音乐
市集
讲座
手作
```

---

## 示例场馆

可使用：

```text
城市艺术中心
青年文化空间
滨江剧场
```

---

## 示例活动

可以设计：

```text
城市影像艺术展
夏夜爵士音乐会
青年创意市集
AI 与城市生活讲座
陶艺体验工作坊
```

可继续补足至 8~10 个。

---

## Seed 要求

- 数据合理；
- venue/category/activity/session 关联正确；
- 不插入 ReservationOrder；
- 不写真实用户数据；
- 不影响旧表；
- 避免重复执行造成明显冲突，如果方便可采用安全 insert 方式；
- 不为了幂等过度增加复杂 SQL。

---

# 十六、分页实现

项目当前使用 MyBatis-Plus。

优先沿用现有分页能力。

不要引入：

```text
PageHelper
新分页框架
```

---

## 分页返回

优先使用当前已有分页返回风格。

如果没有，可返回：

```text
records
total
current
size
pages
```

但不要重复造复杂分页类。

---

# 十七、状态过滤

必须基于 Phase 3A 已定义状态语义。

---

## ActivityCategory

查询：

```text
ACTIVE
```

---

## Venue

查询：

```text
ACTIVE
```

---

## Activity

查询：

```text
PUBLISHED
```

---

## ActivitySession

第一版可返回活动下的可展示场次。

请根据状态语义至少排除明显：

```text
CANCELLED
FINISHED
```

或结合当前时间判断合理展示范围。

不要实现预约资格逻辑。

---

# 十八、场次展示时间逻辑

第一版建议：

```text
start_time >= 当前时间
```

优先只展示未来场次。

如果实现此限制，请在报告中说明。

不要在 Phase 3B 里做复杂：

```text
抢票状态机
自动开场
自动关闭
定时任务
```

---

# 十九、缓存本阶段禁止

即使旧项目已有：

```text
CacheClient
Redis
逻辑过期
```

本阶段也不要把新 Activity 查询接入 Redis。

保持：

```text
Controller
↓
Service
↓
MySQL
```

原因：

先建立正确查询链路。

缓存优化后续再做。

---

# 二十、GEO 本阶段禁止

Venue 已经有：

```text
longitude
latitude
```

但本阶段不要：

```text
Redis GEO
附近活动
距离排序
```

可留给后续增强。

---

# 二十一、后台管理本阶段禁止

不要实现：

```text
POST /activities
PUT /activities
DELETE /activities

POST /venues
PUT /venues
DELETE /venues
```

测试数据使用 SQL。

---

# 二十二、预约本阶段禁止

不要实现：

```text
ReservationOrderService
ReservationOrderController
POST /reservations
DELETE /reservations/{id}
GET /reservations/me
```

不要实现：

```text
库存扣减
库存恢复
事务预约
重新预约
```

这些属于 Phase 3C。

---

# 二十三、Redis / 高并发禁止

本阶段不要：

```text
Redis 预扣库存
Lua
MQ
JVM Queue
Redisson
Kafka
RabbitMQ
```

---

# 二十四、社区和 AI 禁止

不要：

```text
重构 Blog
重构 Follow
Feed
点赞

AI Tool
RAG
MCP
```

---

# 二十五、旧业务禁止删除

必须保留旧：

```text
Shop
ShopType
Voucher
SeckillVoucher
VoucherOrder
Blog
BlogComments
Follow
```

本阶段只新增新业务查询链路。

---

# 二十六、README 禁止修改

本阶段仍然不要修改：

```text
README*
```

README 最终整体项目完成后统一重写。

---

# 二十七、建议实现顺序

严格建议：

```text
Step 1
阅读 Phase 3A 新实体、Mapper、DDL

Step 2
检查当前 Controller / Service / Result / Exception / Interceptor 风格

Step 3
设计 DTO / VO

Step 4
实现 ActivityCategory 查询

Step 5
实现 Venue 列表与详情

Step 6
实现 Activity 联表分页查询

Step 7
实现 Activity 联表详情查询

Step 8
实现 ActivitySession 查询

Step 9
配置公开访问

Step 10
准备 seed SQL

Step 11
执行 Maven compile

Step 12
若数据库环境可用，执行 DDL + Seed

Step 13
若项目可启动，做 API Smoke Test

Step 14
生成单份 Phase 3B 报告
```

---

# 二十八、关键 SQL 要求

## Activity 分页

优先一条联表分页 SQL：

```text
activity
JOIN/LEFT JOIN venue
JOIN/LEFT JOIN activity_category
```

映射：

```text
ActivityListVO
```

---

## Activity 详情

优先一条联表 SQL：

```text
activity
+
venue
+
activity_category
```

映射：

```text
ActivityDetailVO
```

---

## ActivitySession

可以使用 MyBatis-Plus 条件查询：

```text
activity_id = ?
ORDER BY start_time ASC
```

---

# 二十九、N+1 验收

最终必须明确检查：

活动列表是否存在：

```text
for activity:
    venueMapper.selectById(...)
    categoryMapper.selectById(...)
```

如果存在：

必须重构为联表查询。

---

# 三十、接口参数安全

不要使用字符串拼接构造：

```text
ORDER BY
WHERE
```

所有条件必须：

```text
MyBatis 参数绑定
```

避免 SQL 注入风险。

---

# 三十一、分页 size 限制

建议：

```text
1 <= size <= 50
```

如果用户传：

```text
size > 50
```

可以：

```text
截断为 50
```

或：

```text
返回参数错误
```

任选合理方案，但在报告中说明。

---

# 三十二、API Smoke Test

如果项目具备可启动条件：

至少验证：

```text
GET /activity-categories
GET /venues
GET /venues/{id}
GET /activities?page=1&size=10
GET /activities/{id}
GET /activities/{id}/sessions
```

---

## 还需验证筛选

至少：

```text
categoryId
venueId
city
```

---

## 还需验证异常

例如：

```text
不存在的 venue id
不存在的 activity id
非法 page/size
```

---

# 三十三、如果项目无法启动

不要为了完成 Smoke Test 擅自修复：

```text
旧 Shop
旧 Voucher
旧 AI
旧数据库
```

如果启动被旧业务外部依赖阻断：

记录：

```text
阻断原因
```

并至少完成：

```text
compile
静态接口检查
SQL 检查
```

不要虚报 API 已验证。

---

# 三十四、Maven 验收

必须执行：

```bash
cd backend
mvn clean compile
```

要求：

```text
CityHub
CityHub Core
CityHub AI
全部 SUCCESS
```

---

# 三十五、本阶段只生成一份报告

用户明确要求：

> **Phase 3B 最终只生成一份报告。**

不要拆分生成：

```text
API_DESIGN.md
TEST_REPORT.md
QUERY_DESIGN.md
```

所有内容统一汇总到：

```text
docs/refactor/phase3b/PHASE3B_REPORT.md
```

---

# 三十六、PHASE3B_REPORT.md 必须包含

## 1. 阶段结论

说明：

```text
活动发现与查询链路是否完成
```

---

## 2. 新增 Controller

列出：

```text
文件
接口
职责
```

---

## 3. 新增 Service

列出：

```text
接口
实现类
职责
```

---

## 4. DTO / VO

列出新增对象及用途。

---

## 5. Activity 查询设计

明确说明：

```text
是否使用联表
是否避免 N+1
分页方式
筛选条件
排序
```

---

## 6. Activity Detail

说明：

```text
Activity
Venue
Category
```

如何组合。

---

## 7. Session 查询

说明：

```text
状态过滤
时间过滤
排序
```

---

## 8. 公开访问配置

说明：

```text
哪些路径允许未登录访问
修改了哪些 Interceptor / MVC 配置
```

---

## 9. Seed 数据

说明：

```text
文件路径
数据数量
```

---

## 10. 数据库执行结果

明确写：

```text
DDL 是否实际执行
Seed 是否实际执行
```

如果没有执行：

```text
未执行
原因
用户需要做什么
```

---

## 11. API Smoke Test

表格：

| API | 是否验证 | 结果 |
|---|---|---|

如果没实际启动：

必须写：

```text
未验证
```

---

## 12. Maven Compile

记录：

```text
命令
结果
```

---

## 13. 旧业务影响

必须回答：

```text
是否修改 Shop：否
是否修改 Voucher：否
是否修改旧秒杀：否
是否修改 Blog：否
是否修改 AI：否
是否修改 README：否
```

如果确实因为公共拦截器修改产生影响：

需要说明影响范围。

---

## 14. 当前遗留问题

列出实际存在的问题。

不要脑补。

---

## 15. 下一阶段建议

只建议：

> **Phase 3C：MySQL 基线预约链路**

包括：

```text
创建预约
我的预约
取消预约
重新预约
库存扣减
库存恢复
事务
唯一约束
```

不要提前实现。

---

# 三十七、验收标准

Phase 3B 通过必须满足：

## API

存在：

```text
GET /activity-categories
GET /venues
GET /venues/{id}
GET /activities
GET /activities/{id}
GET /activities/{id}/sessions
```

---

## 分页

Activity 支持：

```text
page
size
categoryId
venueId
city
```

---

## 查询

活动列表：

```text
Activity + Venue + Category
```

不能 N+1。

---

## 详情

必须使用：

```text
ActivityDetailVO
```

---

## Session

可以查询未来/可展示场次。

---

## DTO / VO

不能直接把 Entity 全量返回前端。

---

## 登录

浏览查询接口支持未登录访问。

---

## 数据

存在：

```text
cityhub_domain_seed.sql
```

---

## 编译

```text
mvn clean compile PASS
```

---

## 业务边界

没有实现：

```text
预约
Redis
Lua
MQ
支付
社区
AI
```

---

## README

未修改。

---

# 三十八、最终 Codex 回复格式

完成后请输出：

```text
Phase 3B CityHub 活动发现与查询链路完成。

1. 新增查询接口：
2. 新增 Controller：
3. 新增 Service：
4. DTO / VO：
5. Activity 分页与联表设计：
6. 是否避免 N+1：
7. 场次查询规则：
8. 公开访问配置：
9. Seed SQL：
10. DDL / Seed 是否实际执行：
11. API Smoke Test：
12. Maven compile：
13. 是否修改旧业务：否
14. 是否修改 README：否
15. 下一阶段建议：

详细报告：
docs/refactor/phase3b/PHASE3B_REPORT.md
```

未完成的项必须明确写：

```text
未完成 / 未验证 / 阻断原因
```

不要虚报。

---

# 三十九、本阶段最终目标

Phase 3B 不是为了增加技术栈。

它的真正目标是：

> **让 Phase 3A 建立的新领域第一次形成完整、清晰、可查询的 CityHub 业务读链路。**

完成后应形成：

```text
用户
 │
 ▼
ActivityCategory
 │
 ▼
Activity List
 │
 ├───────────────┐
 ▼               ▼
Venue          Activity Detail
                  │
                  ▼
            ActivitySession
```

之后 Phase 3C 再正式实现：

```text
选择场次
↓
创建预约
↓
我的预约
↓
取消 / 重新预约
```

最后 Phase 4 再演进为：

```text
Redis + Lua + 异步高并发预约
```
