# Codex 任务：雅鉴生活志项目代码审计与重构前评估

## 一、任务背景

我准备将当前本地项目“雅鉴生活志”作为 Java 后端实习简历项目的代码基础。

该项目来源于开源项目，并且与“黑马点评 / hm-dianping”存在较明显的继承关系。后续我计划对它进行较大幅度的业务与工程重构，使其不再表现为简单的“黑马点评换皮项目”。

目前拟定的新业务方向为：

> **城市文化活动发现与智能预约平台**

核心业务设想包括：

- 城市展览、音乐现场、市集、艺术空间、手作活动等内容发现；
- 活动 / 场馆详情查询；
- 热门活动缓存；
- 限量活动预约 / 抢票；
- 用户关注、内容发布、点赞等社区功能；
- AI 城市活动助手；
- 后续可能接入 Function Calling，让 AI 查询活动、查询余票、查询预约、创建预约等。

但是在正式开始代码重构前，我需要先对当前项目做一次**完整、客观、基于代码事实的审计**。

---

# 二、本次任务的核心原则

## 1. 本阶段只审计，不修改

本次任务：

- **不要修改任何业务代码**
- **不要重构**
- **不要重命名**
- **不要删除文件**
- **不要新增依赖**
- **不要修改数据库**
- **不要自动修复问题**

只允许：

1. 阅读代码；
2. 分析代码；
3. 必要时运行只读性质的命令；
4. 输出审计报告。

如果为了确认项目是否能编译，需要运行 Maven 编译或测试，可以执行，但不要主动修改代码来“让它通过”。

---

## 2. 必须以实际代码为准

不要根据 README 推测项目已经实现了某项功能。

所有结论必须区分：

### A. 已经真实实现

必须能够在以下内容中找到明确证据：

- pom.xml
- Java 源码
- application.yml / properties
- SQL
- resources
- 前端代码（如果仓库包含）
- 测试代码

### B. README 声称实现，但代码中未找到完整实现

例如：

- README 写了 Kafka，但 pom.xml 没有 Kafka 依赖；
- README 写了 Caffeine，但代码没有 Caffeine Cache；
- README 写了 LangChain4j，但没有相关依赖或 Service；
- README 写了 AI 智能客服，但代码只是接口占位。

此类情况必须明确标记：

> **“文档宣称存在，但当前代码未验证 / 未实现 / 实现不完整”**

### C. 无法确认

如果证据不足，不要猜测。

直接写：

> 未从当前仓库中找到足够证据确认。

---

# 三、第一阶段：建立项目全景

首先扫描整个项目。

请重点阅读：

```text
pom.xml

README*
application.yml
application-*.yml
application.properties

src/main/java
src/main/resources
src/test

数据库 SQL 文件

Dockerfile
docker-compose.yml
compose.yml

前端目录（若存在）
```

输出项目基本结构。

至少回答：

1. 项目使用什么 Java / JDK 版本？
2. Spring Boot 版本是什么？
3. Maven / Gradle？
4. 当前 package 根路径是什么？
5. Application 启动类是什么？
6. 项目 artifactId / name 是什么？
7. 数据库是什么？
8. Redis 是否存在？
9. 是否使用 Redisson？
10. 是否存在 MQ？
11. 是否存在 Kafka？
12. 是否存在 RabbitMQ？
13. 是否存在 Caffeine？
14. 是否存在 Lua Script？
15. 是否存在 AOP？
16. 是否存在限流？
17. 是否存在分布式锁？
18. 是否存在 JWT / Session / Token 登录体系？
19. 是否存在 LangChain4j？
20. 是否存在 AI / LLM 相关代码？
21. 是否存在 Function Calling？
22. 是否存在 RAG？
23. 是否存在向量数据库？
24. 是否存在 Docker 配置？
25. 是否存在自动化测试？

不要只看依赖。

必须结合：

> 依赖 + 配置 + 实际调用代码

综合判断。

---

# 四、第二阶段：审计当前领域模型

扫描以下目录：

```text
entity
domain
model
po
dto
vo
controller
service
service/impl
mapper
repository
```

根据实际项目结构调整。

整理当前所有核心业务实体。

建议形成表格：

| 当前实体 | 数据库表 | Controller | Service | 核心作用 | 黑马点评关联程度 |
|---|---|---|---|---|---|
| Shop | tb_shop | ShopController | ShopService | 商铺信息 | 高 |
| Voucher | ... | ... | ... | ... | 高 |

重点检查是否存在：

```text
Shop
ShopType

Voucher
SeckillVoucher
VoucherOrder

Blog
BlogComments

Follow

User

Sign

RedisData
```

以及其他实体。

---

# 五、第三阶段：黑马点评遗留痕迹专项扫描

这是本次审计最重要的部分之一。

请对整个仓库搜索：

```text
hmdp
hm-dianping
HmDianPing

黑马
点评
商铺
shop

voucher
seckillVoucher
voucherOrder

blog
follow
```

以及你认为明显来自黑马点评的：

- 类名；
- 包名；
- Redis Key；
- SQL 表名；
- Controller 路由；
- Service；
- Mapper；
- 注释；
- README；
- Maven artifact；
- application 配置；
- 前端接口；
- 前端文案；
- 测试类。

输出：

## 黑马点评遗留清单

建议格式：

| 类型 | 文件 | 当前内容 | 严重程度 | 后续是否必须修改 |
|---|---|---|---|---|
| package | xxx | com.hmdp | 极高 | 是 |
| Application | xxx | HmDianPingApplication | 极高 | 是 |
| Maven | pom.xml | hm-dianping | 极高 | 是 |
| Entity | xxx | Shop | 高 | 是 |

严重程度：

- 极高：面试官 / GitHub 浏览几秒即可发现；
- 高：深入看代码容易发现；
- 中：存在明显黑马业务语义；
- 低：属于通用技术实现，可保留。

特别注意：

> **不要把 Redis、Redisson、Lua、缓存等通用技术本身视为黑马点评痕迹。**

需要区分：

### 通用实现

例如：

```text
CacheClient
RedisIdWorker
Distributed Lock
Lua
Cache Aside
```

这些可以继续使用。

### 强业务绑定

例如：

```text
ShopController
VoucherOrderServiceImpl
SeckillVoucher
Blog
tb_shop
tb_voucher
```

这些属于需要重构的业务语义。

---

# 六、第四阶段：逐功能审计

请根据 Controller + Service + Mapper + SQL，梳理当前真正已经实现的功能。

例如：

## 1. 用户体系

检查：

- 登录；
- 注册；
- 短信验证码；
- Token；
- Session；
- Redis 会话；
- 登录拦截器；
- 权限校验；
- 用户信息；
- 注销。

给出实际流程。

---

## 2. 商铺 / 内容查询

检查：

- 商铺查询；
- 分类查询；
- 地理位置查询；
- GEO；
- 分页；
- Redis 缓存；
- 缓存穿透；
- 缓存击穿；
- 缓存雪崩；
- 逻辑过期；
- 缓存更新。

---

## 3. 秒杀 / 优惠券

重点分析：

```text
Voucher
SeckillVoucher
VoucherOrder
Lua
Redis
Redisson
事务
异步线程
消息队列
```

回答：

1. 秒杀库存放在哪里？
2. 一人一单如何实现？
3. 是否真的使用 Lua？
4. Lua 中具体做了什么？
5. 是否存在超卖防护？
6. 是否有分布式锁？
7. 是否采用 Redis 预扣库存？
8. 下单是否同步？
9. 是否异步写 MySQL？
10. 异步是线程池还是 MQ？
11. 如果是 MQ，具体是什么 MQ？
12. 是否存在消息可靠性处理？
13. 是否存在重复消费 / 幂等处理？

---

## 4. Blog / Feed

检查：

- 发布内容；
- 点赞；
- 点赞排行榜；
- 关注；
- Feed 流；
- 推模式；
- 拉模式；
- Scroll 分页；
- Redis ZSet。

说明实际实现情况。

---

## 5. GEO

检查是否存在：

```text
Redis GEO
附近商铺
距离查询
```

如果存在，说明代码流程。

---

## 6. 签到 / UV

检查：

```text
Bitmap
HyperLogLog
签到
UV
```

是否真实存在。

---

## 7. 限流

重点检查：

- 是否存在滑动窗口；
- 固定窗口；
- Token Bucket；
- Sentinel；
- AOP；
- Redis Lua；
- IP 限流；
- 用户限流；
- 接口限流。

如果 README 声称存在但没有实际代码，要明确指出。

---

# 七、第五阶段：README 与真实代码一致性审计

逐项阅读 README 的“技术亮点”。

建立表：

| README 声称能力 | 是否真实实现 | 证据 | 完整程度 | 风险 |
|---|---|---|---|---|
| Redis | 是 | xxx | 完整 | 低 |
| Caffeine | 否/待确认 | 未找到依赖 | 无 | 高 |
| Kafka | ... | ... | ... | ... |
| LangChain4j | ... | ... | ... | ... |
| Function Calling | ... | ... | ... | ... |

尤其检查：

```text
Redis
Redisson
Lua
Caffeine
Kafka
RabbitMQ
MySQL
MyBatis-Plus
AOP
限流
LangChain4j
阿里云百炼
Function Calling
RAG
Docker
```

最终给出：

### README 可信度结论

例如：

```text
已实现：6 项
部分实现：2 项
未发现实现：3 项
```

---

# 八、第六阶段：工程质量审计

从“实习简历项目”的角度检查当前代码。

重点看：

## 1. 分层

Controller / Service / Mapper 是否清晰。

---

## 2. Controller 是否过重

是否存在大量业务逻辑直接写在 Controller。

---

## 3. Service 是否过重

是否出现：

```text
一个 ServiceImpl 几百上千行
```

---

## 4. DTO / VO

是否：

- Entity 直接作为请求参数；
- Entity 直接返回前端；
- DTO / VO 缺失。

---

## 5. 统一返回

是否存在：

```text
Result
Response
R
```

统一响应。

---

## 6. 异常处理

是否存在：

```text
GlobalExceptionHandler
@ControllerAdvice
```

---

## 7. 参数校验

是否使用：

```text
@Valid
@NotNull
@NotBlank
```

---

## 8. 日志

检查：

```text
Slf4j
log.info
log.error
```

是否合理。

---

## 9. 配置安全

重点检查是否存在：

```text
数据库密码
Redis 密码
API Key
AK/SK
Token Secret
```

直接写进仓库。

如果存在，标记为：

> **严重工程风险**

---

## 10. Redis Key 管理

查看 Redis Key 是否：

- 散落；
- 魔法字符串；
- 有统一常量管理；
- 命名是否规范。

---

## 11. 事务

检查：

```text
@Transactional
```

位置是否合理。

特别注意：

- self invocation；
- private 方法事务；
- 异步事务；
- Redis + MySQL 一致性。

---

## 12. 并发

重点检查：

- ThreadLocal；
- Executor；
- CompletableFuture；
- 线程池；
- new Thread；
- 分布式锁；
- synchronized。

---

## 13. 测试

统计：

```text
单元测试数量
集成测试数量
```

判断是否具有有效测试。

---

## 14. Docker

检查：

- Dockerfile；
- Compose；
- MySQL；
- Redis；
- backend；
- frontend。

判断是否能形成：

```bash
docker compose up -d
```

启动的完整环境。

---

# 九、第七阶段：依赖与技术栈真实性

解析 pom.xml。

输出依赖分类：

```text
Spring
Database
Redis
ORM
MQ
Cache
AI
Testing
Utils
Security
Observability
```

重点判断：

### 依赖是否真正使用

例如：

```text
pom.xml 引入 Kafka
```

但 Java 中没有任何：

```text
KafkaTemplate
@KafkaListener
```

则标记：

> 引入但未实际使用。

反过来：

README 写 Kafka，但 pom.xml 都没有：

> 文档描述与代码明显不一致。

---

# 十、第八阶段：数据库审计

阅读全部 SQL。

整理：

```text
表名
作用
主键
重要字段
重要索引
外键
唯一索引
```

尤其分析：

## 秒杀 / 预约类业务

例如是否存在：

```text
user_id + voucher_id
```

唯一索引。

如果“一人一单”完全依赖 Redis，而 MySQL 没有唯一索引兜底，也要指出。

---

## 索引

检查：

- 常用查询字段是否有索引；
- 联合索引设计；
- 是否存在明显全表扫描风险。

只做代码层面的合理性判断，不要虚构真实线上数据规模。

---

# 十一、第九阶段：项目是否适合作为实习简历项目

请以：

> **Java 后端实习面试官**

视角评估。

分别打分 1~10：

| 维度 | 分数 |
|---|---:|
| Java 后端技术覆盖 | |
| Redis 深度 | |
| 高并发场景 | |
| 数据库设计 | |
| 工程规范 | |
| 项目完整度 | |
| 面试可讲性 | |
| 原创 / 差异化程度 | |
| GitHub 展示质量 | |
| 当前简历可用度 | |

然后给出：

### 当前最大的 5 个优点

### 当前最大的 10 个问题

### 最容易被面试官质疑的 10 个地方

---

# 十二、第十阶段：评估改造成“城市文化活动发现与智能预约平台”的可行性

目标业务为：

> **城市文化活动发现与智能预约平台**

请根据当前实际代码判断以下映射是否合理。

```text
Shop
→ Venue / ActivityVenue

ShopType
→ ActivityCategory

Voucher
→ ActivityTicket

SeckillVoucher
→ LimitedTicket / LimitedQuota

VoucherOrder
→ ReservationOrder

Blog
→ ExplorePost

Follow
→ Follow
```

针对每个模块给出：

| 当前模块 | 新业务 | 复用程度 | 修改成本 | 建议 |
|---|---|---:|---:|---|
| Shop | Venue | 高 | 中 | 重命名+字段调整 |

复用程度：

- 高；
- 中；
- 低。

修改成本：

- 小；
- 中；
- 大。

---

# 十三、重点判断哪些技术代码可以保留

请把代码分为三类。

## A. 建议直接保留

例如可能包括：

```text
Redis 缓存工具
Redis ID Worker
Redisson 配置
Lua
统一返回
部分 Utils
```

必须根据实际代码确认。

---

## B. 可以保留但需要改造

例如：

```text
VoucherOrderService
ShopService
BlogService
```

---

## C. 建议重新设计

例如：

```text
领域模型
数据库表
API 路径
AI 模块
README
```

---

# 十四、后续重构优先级

最后根据代码实际情况给出推荐重构顺序。

使用：

```text
P0
P1
P2
P3
```

例如：

## P0：去黑马身份

```text
com.hmdp
HmDianPingApplication
artifactId
数据库
README
```

## P1：领域模型重构

## P2：技术能力补齐

## P3：AI 与工程增强

但不要直接照搬上述示例。

必须根据审计结果重新制定。

---

# 十五、必须特别检查的“简历风险”

请重点判断项目是否存在以下情况：

### 风险 1

README 写了，但代码没有。

### 风险 2

技术名词很多，但没有实际业务场景。

### 风险 3

关键性能数据没有测试依据。

例如：

```text
QPS 1000
提升 80%
响应时间下降 60%
```

如果代码仓库没有：

```text
JMeter
benchmark
测试报告
日志
```

则不能将其当成事实。

### 风险 4

代码仍然明显属于黑马点评。

### 风险 5

AI 功能只是 Demo 或接口调用，没有真正与业务结合。

### 风险 6

项目可以运行，但工程规范较差。

### 风险 7

简历描述与代码证据不一致。

---

# 十六、审计中禁止的行为

不要：

1. 为了让项目显得更好而脑补功能；
2. 把 README 当作事实；
3. 把“引入依赖”等同于“实现功能”；
4. 为了符合目标架构而修改代码；
5. 直接开始 CityMuse 重构；
6. 自动添加 Kafka；
7. 自动添加 LangChain4j；
8. 自动添加 Caffeine；
9. 自动新增 Docker；
10. 自动修改数据库；
11. 自动更名 package；
12. 自动生成新的业务代码。

这一轮只有：

> **READ → VERIFY → AUDIT → REPORT**

---

# 十七、允许执行的验证命令

可以根据项目情况执行：

```bash
find .
tree

grep
rg

mvn dependency:tree
mvn test
mvn compile

git status
git log --oneline -n 20
```

如果 Maven Wrapper 存在：

```bash
./mvnw test
./mvnw compile
```

但不要因为编译失败而擅自修改源码。

---

# 十八、最终必须生成的文件

请在项目根目录创建：

```text
docs/audit/
```

并生成：

```text
docs/audit/01_project_overview.md
docs/audit/02_feature_audit.md
docs/audit/03_hmdp_legacy_audit.md
docs/audit/04_readme_code_gap.md
docs/audit/05_engineering_quality.md
docs/audit/06_database_audit.md
docs/audit/07_resume_risk_audit.md
docs/audit/08_city_activity_refactor_feasibility.md
docs/audit/09_refactor_priority.md

docs/audit/AUDIT_SUMMARY.md
```

---

# 十九、AUDIT_SUMMARY.md 格式

最终汇总必须控制在方便人工阅读的范围内。

结构：

# 1. 一句话结论

例如：

> 当前项目可以作为二次重构基础，但暂时不适合直接作为差异化实习项目展示。

必须根据代码实际情况写。

---

# 2. 当前真实技术栈

只列代码确认存在的。

---

# 3. 当前核心业务

只列代码确认存在的。

---

# 4. README 与代码不一致项

列重点。

---

# 5. 黑马点评遗留程度

使用：

```text
低 / 中 / 高 / 极高
```

并说明原因。

---

# 6. 当前项目最大的 5 个问题

按严重程度排序。

---

# 7. 最值得保留的 5 个技术实现

---

# 8. 建议删除 / 重写 / 重构的部分

---

# 9. 城市文化活动平台改造可行性

给出：

```text
非常适合
适合
一般
不建议
```

四档之一。

---

# 10. 推荐改造路线

最多分 4 个阶段。

---

# 11. 最终判断

回答：

### 问题 A

这个项目是否值得继续改造成实习简历项目？

### 问题 B

如果改，预计是：

```text
轻量换皮
中等重构
大规模重构
接近重写
```

哪一档？

### 问题 C

最应该优先完成的第一件事是什么？

---

# 二十、证据要求

报告中的关键结论尽量附：

```text
文件路径
类名
方法名
配置项
依赖名
SQL 表
```

例如：

```text
证据：
src/main/java/com/hmdp/service/impl/VoucherOrderServiceImpl.java
seckillVoucher(...)
```

不要只写：

> 项目使用了 Lua。

而应该写：

> 项目存在 Lua 秒杀脚本，调用位置为 XXX，脚本位于 XXX，负责 XXX。

---

# 二十一、最终输出要求

完成后不要只说“审计完成”。

请在终端最终回复中给我一个简洁摘要：

```text
审计完成。

1. 当前项目真实完成度：
2. 黑马点评遗留程度：
3. README 与代码一致性：
4. 是否适合继续重构：
5. 推荐重构规模：
6. 最严重的三个问题：
7. 最值得保留的三个技术模块：
8. 下一步最优先工作：

详细报告：
docs/audit/AUDIT_SUMMARY.md
```

---

# 二十二、最终目标

这次任务不是把项目“包装得看起来厉害”。

真正目标是：

> **先搞清楚当前项目到底有什么、缺什么、哪些是真的、哪些只是 README 描述、哪些明显属于黑马点评遗留，再决定后续怎样进行业务重构和工程增强。**

审计必须客观。

宁可指出项目目前不完善，也不要虚构其已经实现某些能力。
