# Codex 执行任务：Phase 2 项目身份规范化与工程遗留清理

## 一、任务背景

当前项目已经完成 Phase 1「基础工程基线治理」。

Phase 1 已确认：

- `backend` Maven Reactor 可以正常 `mvn clean compile`；
- `core` 与 `consultant` 均可编译；
- 敏感配置已环境变量化；
- SQL 使用关系已完成初步梳理；
- `.gitignore` 与工程卫生已经治理；
- README、业务领域、秒杀链路、AI 业务均未修改。

现在进入第二阶段。

项目最终名称确定为：

> **CityHub - 城市活动发现与预约平台**

本阶段目标是：

> **统一项目工程身份，清理明显的旧项目 / 黑马点评 / 雅鉴生活志工程级遗留，为下一阶段正式领域重构建立干净的工程命名基础。**

注意：

**本阶段依然不是业务领域重构。**

不要把 Shop 改成 Venue，不要把 Voucher 改成 Activity，不要修改数据库业务表结构。

---

# 二、本阶段核心目标

完成后，项目工程层面应统一使用：

```text
项目品牌：
CityHub

项目展示名称：
CityHub - 城市活动发现与预约平台

工程前缀：
cityhub

Java 根 package：
com.cityhub

主启动类：
CityHubApplication

AI 子模块启动类：
如当前 consultant 模块有独立 Application，
则统一为：
CityHubAiApplication
```

Maven 命名建议根据当前父子模块实际结构统一为：

```text
cityhub-parent
cityhub-core
cityhub-ai
```

但必须先读取当前 `pom.xml` 的真实父子模块结构后再修改。

不要机械增加不存在的新模块。

---

# 三、本阶段最重要的边界

## 允许修改

本阶段允许修改：

```text
项目工程名称
Maven groupId
Maven artifactId
Maven name / description

Java package 根路径
Application 启动类名称

@ComponentScan
@MapperScan
Spring 扫描路径
Mapper XML namespace
测试 package
反射 / 全限定类名引用

模块工程显示名称
部分目录名（仅在低风险且确有必要时）

旧项目工程级注释
旧品牌 Banner
启动脚本中的工程名
Nginx 中纯工程路径命名

明确无运行时依赖的 legacy SQL 文件名 / 归档位置
```

---

## 禁止修改

本阶段禁止：

### 1. README

不要修改：

```text
README.md
README*
```

README 会在项目全部完成后统一重写。

---

### 2. 业务领域

不要修改 / 重命名：

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
```

不要做：

```text
Shop -> Venue
ShopType -> ActivityCategory
Voucher -> Ticket
VoucherOrder -> ReservationOrder
Blog -> ExplorePost
```

---

### 3. API 业务路由

不要主动修改：

```text
/shop
/shop-type
/voucher
/voucher-order
/blog
/follow
```

这些属于 Phase 3 / Phase 5 领域改造。

---

### 4. 数据库业务表

不要修改：

```text
tb_shop
tb_shop_type
tb_voucher
tb_seckill_voucher
tb_voucher_order
tb_blog
tb_follow
```

不要重建数据库 schema。

数据库最终名称 `cityhub` 也暂时不在本阶段强制迁移。

后续 Phase 3 新领域模型建立时统一处理。

---

### 5. Redis 业务 Key

暂时不要把：

```text
shop
voucher
blog
```

业务 Redis Key 改成未来的新领域名称。

只允许清理纯工程品牌前缀（如果确实存在）。

---

### 6. 秒杀业务

禁止：

```text
重构 Lua
修改 VoucherOrderServiceImpl
修改 JVM 队列
补库存初始化
添加 MQ
增加幂等
增加补偿
```

---

### 7. AI 业务

禁止：

```text
修改 @Tool 的业务含义
新增 Tool
修复 reservation 业务
补 RAG
补 MCP
修改优惠券查询逻辑
```

仅允许因为 package / Application / Maven 改名导致的必要引用修复。

---

### 8. 技术栈升级

不要升级：

```text
Spring Boot
Java
MyBatis-Plus
Redisson
LangChain4j
Redis
Maven 插件
```

---

# 四、执行前：先做完整身份扫描

在修改前必须先扫描全仓库。

建议执行：

```bash
rg -n -i "hmdp|hm-dianping|HmDianPing|itheima|黑马点评|黑马程序员"
```

再执行：

```bash
rg -n -i "yjshz|YJSHZ|雅鉴|雅鉴生活志|Ya-Jian|Ya Jian"
```

以及：

```bash
find . -maxdepth 5 -type f | sort
find backend -type d | sort
```

如果 `rg` 不可用，可以使用 `grep -R`。

---

# 五、先把所有命中分类，不要直接全局替换

必须将扫描结果分为以下三类。

---

## A 类：工程身份遗留

本阶段应该处理。

例如：

```text
com.yjshz
com.itheima

YJSHZ
yjshz

HmDianPingApplication

Maven groupId
Maven artifactId
Maven name

模块名
项目 banner
启动类
纯工程注释
配置中的项目显示名
```

---

## B 类：业务领域遗留

本阶段禁止处理。

例如：

```text
Shop
Voucher
SeckillVoucher
VoucherOrder
Blog
Follow

/shop
/voucher
/blog

tb_shop
tb_voucher
```

即使它们明显来自黑马点评，也不要在本阶段修改。

---

## C 类：历史 / legacy 资料

例如：

```text
hmdp.sql
历史 SQL
旧注释
旧脚本
```

需要进一步判断：

```text
是否运行时使用
是否存在引用
是否与其他 SQL 完全重复
```

确认后才能归档或改名。

不确定则保留并记录。

---

# 六、任务 1：统一 Maven 工程身份

## 1. 阅读全部 POM

至少检查：

```text
backend/pom.xml
backend/core/pom.xml
backend/consultant/pom.xml
```

以及实际存在的其他 POM。

先输出当前：

```text
parent
groupId
artifactId
name
description
modules
dependencies 中的内部模块坐标
```

---

## 2. 根据真实结构统一 Maven 命名

目标建议：

### 父工程

```xml
<groupId>com.cityhub</groupId>
<artifactId>cityhub-parent</artifactId>
<name>CityHub</name>
```

### core

```xml
<artifactId>cityhub-core</artifactId>
<name>CityHub Core</name>
```

### consultant / AI 模块

如果当前 `consultant` 的职责确实是 AI 服务，则 Maven 工程身份统一为：

```xml
<artifactId>cityhub-ai</artifactId>
<name>CityHub AI</name>
```

但：

> 是否物理重命名 `consultant/` 目录，要结合当前 Maven 模块引用和修改风险判断。

Maven artifact 名称可以先统一为 `cityhub-ai`。

目录只有在确认修改安全、不会造成无意义风险时才改。

---

## 3. 父子模块引用同步

如果修改：

```text
groupId
artifactId
parent
```

必须同步所有：

```text
<parent>
<dependency>
<module>
```

确保 Reactor 构建正确。

---

# 七、任务 2：统一 Java 根 package

Phase 1 报告已显示 core 当前至少存在：

```text
com.yjshz
```

但不要据此假设所有模块一致。

请实际扫描：

```bash
find backend -path "*/src/main/java/*" -name "*.java"
```

统计：

```text
com.yjshz
com.itheima
com.hmdp
其他项目级 package
```

---

## 目标

业务 Java 代码最终统一根 package：

```text
com.cityhub
```

例如：

```text
com.yjshz.controller
->
com.cityhub.controller
```

如果 consultant 当前是：

```text
com.itheima.ai
```

则根据其结构转换成类似：

```text
com.cityhub.ai
```

不要为了追求层级一致把所有子 package 扁平化。

保留原本合理的：

```text
controller
service
mapper
config
utils
entity
ai
tools
```

结构。

---

# 八、package 迁移必须同步检查的内容

不能只修改 Java 文件第一行。

必须检查：

```text
import
@ComponentScan
@MapperScan
@SpringBootApplication(scanBasePackages = ...)
@Enable...
```

以及：

```text
MyBatis XML namespace
resultType
parameterType
typeAlias
```

如果 XML 中存在：

```xml
namespace="com.yjshz.mapper.XxxMapper"
```

必须同步修改。

---

## 还要搜索全限定类名

例如：

```text
Class.forName(...)
反射
Jackson type
配置文件中的 Java class
Spring factories
META-INF
```

搜索：

```bash
rg -n "com\.yjshz|com\.itheima|com\.hmdp"
```

迁移完成后，工程级旧 package 命中应尽量归零。

---

# 九、任务 3：统一 Application 启动类

扫描所有：

```text
@SpringBootApplication
```

找到真实启动类。

---

## core 主应用

统一命名为：

```text
CityHubApplication
```

文件名同步：

```text
CityHubApplication.java
```

---

## AI 模块

如果 consultant 是独立 Spring Boot 应用：

统一为：

```text
CityHubAiApplication
```

如果 consultant 并非独立启动模块，则不要为了满足名字要求新增启动类。

必须以真实架构为准。

---

## 同步修改

检查：

```text
测试中的 Application 引用
SpringBootTest classes
启动脚本
IDE 配置（如果存在且纳入仓库）
反射引用
```

---

# 十、任务 4：清理 YJSHZ / 雅鉴生活志工程身份

全仓库搜索：

```text
YJSHZ
yjshz
雅鉴生活志
雅鉴
```

根据命中语义判断。

---

## 本阶段需要修改

纯工程身份，例如：

```text
项目名
模块名
Maven name
Application 名
package
banner
服务 display name
日志中的应用名
纯工程注释
```

统一为：

```text
CityHub
cityhub
```

---

## 本阶段不要修改

如果某个字段已经属于数据库业务数据、演示数据、商铺名称、文章内容：

不要机械替换。

只修改工程身份。

---

# 十一、任务 5：清理 hmdp / itheima 工程级遗留

扫描：

```text
hmdp
hm-dianping
itheima
黑马点评
黑马程序员
```

---

## 工程级引用

如果出现在：

```text
package
Maven
Application
项目名
注释
脚本工程名
模块名
```

本阶段处理。

---

## 业务级引用

如果是：

```text
Shop
Voucher
Blog
tb_shop
```

不处理。

---

# 十二、任务 6：legacy SQL 文件处理

Phase 1 已确认：

```text
consultant 下存在两份 SHA-256 完全相同的 hmdp.sql
且未发现源码 / 配置 / 脚本的文件级运行时引用
```

本阶段重新验证该结论。

---

## 如果再次确认：

- 两份 SQL 内容完全相同；
- 无源码引用；
- 无启动脚本引用；
- 不属于 Flyway / Liquibase 自动迁移；
- 不影响当前构建与运行；

则可以进行轻量 legacy 归档。

推荐：

```text
docs/legacy/sql/
```

保留一份，例如：

```text
docs/legacy/sql/legacy_hmdp_schema.sql
```

完全重复的第二份副本可以删除。

但必须在报告中记录：

```text
原路径
SHA-256
引用检查
保留位置
删除的重复副本
```

---

## 如果不能 100% 确认安全

不要移动 / 删除。

只在报告中标记：

```text
待 Phase 3 SQL 重建后清除
```

---

# 十三、数据库名本阶段不要强制修改

即使当前数据库存在：

```text
yjshz
redis_project
hmdp
```

本阶段默认：

> **不进行数据库名迁移。**

原因：

Phase 3 会重新设计：

```text
Venue
Activity
ActivitySession
ReservationOrder
```

届时统一建立：

```text
cityhub
```

数据库更合理。

所以本阶段不要为了名字干净而重复迁移数据库。

---

# 十四、配置文件中的命名处理规则

检查：

```text
application.yml
application-*.yml
bootstrap.yml
logback.xml
```

---

## 可以修改

如果存在：

```yaml
spring:
  application:
    name: yjshz
```

可以改为类似：

```yaml
spring:
  application:
    name: cityhub-core
```

AI 独立模块：

```yaml
spring:
  application:
    name: cityhub-ai
```

---

## 不要修改

数据库 URL 中当前数据库名：

```text
jdbc:mysql://.../当前旧数据库
```

如果改动会导致当前数据库无法连接，则保持旧数据库名称。

留到 Phase 3。

---

# 十五、目录物理重命名策略

本阶段不要为了“看起来统一”大规模改目录。

例如：

```text
backend/core
backend/consultant
```

其中：

```text
core
```

可以暂时保留。

`consultant` 是否改成：

```text
ai
```

必须基于以下判断：

1. 是否会导致大量 Maven / IDE / 脚本引用变化；
2. 是否存在外部启动脚本；
3. 是否影响当前运行；
4. 修改收益是否大于风险。

---

## 建议策略

优先统一：

```text
Maven artifactId
Maven name
package
Application
spring.application.name
```

物理目录名不是本阶段强制验收项。

---

# 十六、前端处理范围

当前项目包含 frontend。

本阶段只检查前端中的**工程身份文本**。

例如：

```text
yjshz
雅鉴生活志
hmdp
黑马点评
```

---

## 可以修改

如果是：

```text
页面 title
纯品牌名称
静态站点项目名
Nginx 工程目录标识
```

可改为：

```text
CityHub
```

---

## 不要修改

前端 API：

```text
/shop
/voucher
/blog
```

不要改。

页面上的：

```text
商铺
优惠券
探店
```

业务文案也暂时不要做大规模调整。

因为 Phase 3 / Phase 5 会重新设计业务。

本阶段核心仍然是“工程身份”，不是产品 UI 重构。

---

# 十七、不要提前设计新的业务代码

严禁本阶段新增：

```text
Venue.java
Activity.java
ActivitySession.java
ReservationOrder.java
```

不要新增：

```text
VenueController
ActivityController
ReservationController
```

不要新建新业务表。

这些全部属于 Phase 3。

---

# 十八、执行顺序

严格建议按以下顺序：

```text
Step 1
记录当前目录结构

Step 2
执行当前基线：
cd backend
mvn clean compile

Step 3
全仓库身份残留扫描

Step 4
将结果分类为：
工程身份 / 业务领域 / legacy

Step 5
修改 Maven 工程身份

Step 6
执行 mvn clean compile

Step 7
迁移 Java package

Step 8
同步 Mapper XML / 扫描路径 / import

Step 9
修改 Application 名称

Step 10
修改 application display name 等工程配置

Step 11
处理明确安全的 legacy hmdp SQL

Step 12
检查前端纯工程品牌文本

Step 13
再次全仓库身份扫描

Step 14
执行 Maven compile

Step 15
检查是否误改业务 API / Entity / SQL 表

Step 16
生成报告
```

---

# 十九、每一个大步骤后都要编译

尤其：

```text
修改 Maven 坐标后
```

执行：

```bash
cd backend
mvn clean compile
```

然后 package 迁移后再次：

```bash
mvn clean compile
```

不要全部改完才第一次编译。

---

# 二十、全仓库残留检查

完成后执行：

```bash
rg -n -i "yjshz|YJSHZ|雅鉴生活志|Ya-Jian|hmdp|hm-dianping|HmDianPing|itheima|黑马点评|黑马程序员"
```

对每个剩余命中进行分类。

---

## 允许剩余的命中

例如：

```text
旧业务类中并没有显式品牌，只是 Shop/Voucher 等业务概念
docs/legacy 中的历史 SQL
本阶段明确保留的历史审计报告
```

---

## 不应剩余

例如：

```text
com.yjshz
com.itheima
Maven artifactId=yjshz
YJSHZ Application
旧项目 banner
Spring application name=yjshz
```

---

# 二十一、业务误修改专项检查

必须确认以下内容没有因为全局替换被改坏：

```text
Shop
Voucher
Blog
Follow

Controller RequestMapping
Mapper
数据库表名
Redis 业务 Key
Lua
AI Tool 的业务逻辑
```

可以使用搜索和 diff。

如果当前工作区没有 `.git`，无法使用 `git diff`：

则通过：

```text
修改文件清单
+
文本搜索
+
Maven compile
```

进行检查。

不要因为没有 Git 元数据而阻断任务。

---

# 二十二、最终 Maven 验收

必须执行：

```bash
cd backend
mvn clean compile
```

目标：

```text
CityHub parent / core / ai
全部 SUCCESS
```

具体 Reactor 显示名称以实际修改后的 Maven name 为准。

如果失败：

必须继续定位本阶段命名迁移引起的问题并修复。

不得以“后续阶段处理”作为理由留下由本阶段造成的编译失败。

---

# 二十三、本阶段交付报告

创建：

```text
docs/refactor/phase2/
```

生成：

```text
docs/refactor/phase2/PHASE2_REPORT.md
docs/refactor/phase2/IDENTITY_SCAN.md
docs/refactor/phase2/RENAME_MAPPING.md
```

---

# 二十四、IDENTITY_SCAN.md

必须记录修改前和修改后的扫描情况。

建议：

```markdown
# Identity Scan

## 修改前

### YJSHZ / 雅鉴生活志
...

### hmdp / itheima
...

## 分类

### 工程身份
...

### 业务领域
...

### legacy
...

## 修改后

### 已清除工程身份
...

### 合理保留
...

### 后续 Phase 3 处理
...
```

---

# 二十五、RENAME_MAPPING.md

建立明确映射表。

例如：

| 类型 | 原名称 | 新名称 | 状态 |
|---|---|---|---|
| 项目 | YJSHZ | CityHub | 完成 |
| groupId | com.xxx | com.cityhub | 完成 |
| package | com.yjshz | com.cityhub | 完成 |
| package | com.itheima | com.cityhub... | 完成 |
| Application | XxxApplication | CityHubApplication | 完成 |

必须根据实际代码填写。

不要虚构原名称。

---

# 二十六、PHASE2_REPORT.md

结构：

## 1. 阶段结论

回答：

```text
项目工程身份是否已统一为 CityHub
```

---

## 2. Maven 修改

列出：

```text
父工程
core
AI
```

修改前 / 修改后。

---

## 3. package 修改

统计：

```text
迁移了多少 Java 文件
原 package
新 package
```

---

## 4. Application 修改

列出真实启动类修改。

---

## 5. 配置修改

例如：

```text
spring.application.name
扫描路径
MapperScan
```

---

## 6. legacy 清理

说明：

```text
hmdp SQL 是否处理
处理依据
最终位置
```

---

## 7. 前端工程身份

说明：

```text
修改了哪些纯品牌文本
哪些业务 UI 文案刻意没动
```

---

## 8. 编译验证

表：

| 阶段 | 命令 | 结果 |
|---|---|---|
| 修改前 | ... | PASS |
| Maven 改名后 | ... | PASS |
| package 迁移后 | ... | PASS |
| 最终 | ... | PASS |

---

## 9. 当前仍保留的旧业务语义

明确列出：

```text
Shop
ShopType
Voucher
SeckillVoucher
VoucherOrder
Blog
Follow
tb_*
旧业务 API
```

并注明：

> 这是刻意保留，不属于 Phase 2 遗漏。

---

## 10. 下一阶段建议

只提出：

> **Phase 3：CityHub 新领域模型设计与数据库重建**

重点将包括：

```text
Venue
Activity
ActivitySession
ReservationOrder
```

本阶段不要提前实现。

---

# 二十七、验收标准

Phase 2 完成必须满足：

## 工程身份

```text
项目统一使用 CityHub
```

---

## Maven

```text
groupId / artifactId / name 统一规范
```

---

## Java

```text
业务源码不再使用 com.yjshz / com.itheima / com.hmdp 作为根 package
```

---

## Application

```text
主启动类统一为 CityHubApplication
AI 独立应用如存在则使用 CityHubAiApplication
```

---

## 配置

```text
Spring application name 等纯工程身份统一
```

---

## 编译

```text
backend 下 mvn clean compile PASS
```

---

## 业务

以下内容本阶段保持原业务逻辑：

```text
Shop
Voucher
Blog
Follow
秒杀
AI Tool
数据库业务表
Redis 业务 Key
API 路由
```

---

## README

```text
未修改
```

---

# 二十八、遇到不确定情况的原则

如果遇到：

```text
某个旧名称到底是工程品牌还是业务数据
某个 SQL 是否运行时使用
某个目录能否安全重命名
某个配置是否会影响启动
```

不要猜。

优先：

```text
保留
+
记录
```

而不是为了“搜不到旧名称”强行修改。

---

# 二十九、最终回复格式

完成后请输出：

```text
Phase 2 项目身份规范化完成。

1. 项目最终工程名称：
2. Maven 坐标：
3. Java 根 package：
4. 主 Application：
5. AI Application：
6. Maven compile：
7. 清理的主要旧工程身份：
8. 刻意保留的旧业务语义：
9. legacy SQL 处理情况：
10. 是否修改 README：否
11. 是否修改业务逻辑：否
12. 下一阶段建议：

详细报告：
docs/refactor/phase2/PHASE2_REPORT.md
```

如某项不存在：

```text
不存在 / 不适用
```

不要虚构。

---

# 三十、本阶段最终目标

本阶段不是为了把代码中的每一个：

```text
Shop
Voucher
Blog
```

都替换掉。

真正目标是：

> **先让整个工程在“身份层面”彻底成为 CityHub，同时保持旧业务可编译、可运行，为 Phase 3 真正重新设计城市活动领域模型创造稳定基础。**

最终应形成这样一个中间状态：

```text
CityHub
│
├── Maven 身份统一
├── package 统一
├── Application 统一
├── 工程配置统一
├── 明显 hmdp / itheima 工程身份清理
│
└── 旧业务暂时保留
    ├── Shop
    ├── Voucher
    ├── Blog
    └── Follow
```

下一阶段再正式进入：

```text
Venue
Activity
ActivitySession
ReservationOrder
```

的领域架构设计与实现。
