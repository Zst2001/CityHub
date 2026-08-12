# Codex 执行任务：Phase 1 基础工程基线治理

## 一、任务背景

当前项目“雅鉴生活志”已经完成一次代码审计。

后续目标是将该项目重构为：

> **城市文化活动发现与智能预约平台**

但本阶段暂时不进行业务领域重构。

根据审计结果，当前项目存在以下基础问题：

1. 根 Maven 构建存在编译阻断；
2. 存在明文数据库密码等敏感配置风险；
3. SQL 文件较杂，当前实际初始化入口与 legacy SQL 关系需要梳理；
4. 仓库中存在运行日志、构建产物、本地配置等工程卫生问题；
5. 当前仍存在大量黑马点评业务遗留，但这一轮暂时不处理；
6. README 与实际实现不一致，但 README 本轮不修改，后续项目全部重构完成后统一重写。

本阶段目标不是“升级项目功能”，而是先建立一个稳定、干净、安全的工程基线。

---

# 二、本阶段必须遵守的原则

## 1. 只做基础工程治理

本次只允许处理：

- Maven / Java 编译阻断；
- 敏感配置治理；
- SQL 使用情况梳理与轻量归档；
- `.gitignore` / 日志 / 构建产物 / 本地配置等仓库卫生问题；
- 为上述修改做必要验证；
- 输出修改报告。

---

## 2. 明确禁止事项

本阶段禁止：

### 禁止修改 README

不要：

```text
修改 README.md
重写 README
补充技术亮点
删除 README 中未实现内容
```

README 后续整体项目完成后统一重写。

---

### 禁止业务领域重构

不要修改或重命名：

```text
Shop
ShopType
Voucher
SeckillVoucher
VoucherOrder
Blog
Follow
```

不要进行：

```text
Shop -> Venue
Voucher -> ActivityTicket
VoucherOrder -> ReservationOrder
Blog -> ExplorePost
```

这一轮完全不做。

---

### 禁止项目身份重构

本轮不要进行：

```text
com.hmdp -> com.citymuse
com.itheima -> 新 package
HmDianPingApplication -> 新名称
artifactId 修改
项目名修改
数据库名整体修改
```

这些属于下一阶段任务。

---

### 禁止秒杀链路重构

不要修改：

```text
Lua 秒杀核心业务
VoucherOrderServiceImpl
JVM BlockingQueue / 异步下单架构
库存初始化逻辑
Redisson 下单逻辑
```

除非某个代码点直接导致“当前项目无法编译”，否则不要顺手修复。

当前秒杀可靠性问题后续会在新预约领域建立后统一重写。

---

### 禁止 AI 业务重构

不要：

```text
新增 Tool
修改 AI 预约业务
补 reservation 表
修 VoucherOrder 手机号业务
补 RAG
加 MCP
新增 LangChain4j 功能
```

AI 模块本阶段只允许处理“敏感配置”。

---

### 禁止技术栈升级

不要主动：

```text
Spring Boot 2.7 -> 3.x
Java 8 -> 17
Java 17 -> 21
MyBatis-Plus 大版本升级
Redis 客户端升级
Redisson 升级
LangChain4j 升级
Maven 大规模依赖升级
```

本阶段目标是：

> 在当前技术栈下恢复稳定构建，而不是做升级迁移。

---

# 三、任务 1：修复 Maven / Java 编译阻断

## 目标

让当前项目能够在不修改业务语义的前提下正常执行 Maven 编译。

审计中已发现一个明确问题：

```text
RedisConstants.java
```

存在一个无效 CORBA 相关 import，导致 Maven 构建失败。

但不要只机械删除这一处后就结束。

请先实际执行：

```bash
git status
mvn clean compile
```

如果根目录不是直接可构建 Maven 项目，请根据真实目录结构判断。

如果存在多个 Maven 模块，例如：

```text
core
consultant
```

请分别验证：

```bash
mvn clean compile
```

或存在 Maven Wrapper 时使用：

```bash
./mvnw clean compile
```

---

## 修复规则

只允许修复：

- 不存在 / 无效 import；
- 明显拼写错误；
- 当前源码与当前 JDK 不兼容且属于低风险编译问题；
- 当前模块配置中明显导致无法编译的最小问题。

不要借此机会重构代码。

---

## 每解决一个编译问题，都记录

```text
文件：
问题：
原因：
最小修改：
是否影响业务：
```

最终目标：

```text
mvn clean compile
```

在可以正常构建的 Maven 模块中通过。

---

## 如果某个模块因为外部 API Key、MySQL、Redis 未启动而无法通过 test

注意：

```text
compile
```

和：

```text
test
```

要区分。

本阶段第一目标是：

```text
compile PASS
```

如果测试需要外部环境而失败，不要为了让测试通过而删除测试或修改业务。

在报告中说明即可。

---

# 四、任务 2：敏感配置治理

## 目标

仓库中不得继续硬编码真实的：

```text
数据库密码
Redis 密码
API Key
Token Secret
模型密钥
```

---

## 第一步：全仓库扫描

请搜索：

```text
password
passwd
pwd
api-key
apikey
api_key
secret
token
dashscope
mysql
redis
root
sk-
```

并结合配置文件判断是否真的属于敏感信息。

重点检查：

```text
application.yml
application.yaml
application.properties
application-*.yml
.env
Java 常量
测试配置
SQL
Docker / shell
AI consultant 模块
```

不要把普通字符串误判成密钥。

---

## 第二步：配置环境变量化

根据项目实际配置方式修改。

例如 MySQL 可改为：

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
```

Redis 示例：

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
```

AI 示例：

```yaml
dashscope:
  api-key: ${DASHSCOPE_API_KEY:}
```

以上只是示例。

必须根据当前项目真实的配置路径和 Spring Boot 版本修改，不要盲目照抄。

---

## 第三步：保留合理默认值

允许对以下非敏感配置提供本地默认值：

```text
host=localhost
port=6379
username=root
数据库地址
服务端口
```

但：

```text
真实密码
真实 API Key
```

不能提供真实默认值。

---

## 第四步：示例配置

根据项目结构选择最合适的方案：

### 方案 A

新增：

```text
.env.example
```

### 方案 B

新增：

```text
application-local.example.yml
```

### 方案 C

如果多模块各自配置不同，可分别提供：

```text
core/...example...
consultant/...example...
```

原则：

> 示例文件中只能有占位符，不得包含真实凭证。

例如：

```text
DB_PASSWORD=your_password
DASHSCOPE_API_KEY=your_api_key
```

---

## 第五步：`.gitignore`

确保真实本地配置不会被提交。

根据实际情况补充：

```gitignore
.env
.env.*
!.env.example

application-local.yml
application-local.yaml
```

不要错误忽略正式公共配置文件。

---

## 重要限制

如果 Git 历史中已经存在密钥：

本阶段不要擅自执行：

```text
git filter-repo
BFG
强制 push
重写 Git history
```

只在报告中指出：

> Git 历史可能仍存在敏感信息，需要人工轮换密钥并在后续单独处理历史清理。

如果发现真实 API Key，应明确提醒用户：

> 建议立即在对应平台轮换 / 作废旧 Key。

---

# 五、任务 3：SQL 使用情况梳理

## 本阶段的目标不是重构数据库

当前后续会重新设计：

```text
Venue
Activity
ActivitySession
ReservationOrder
```

所以本阶段禁止重写：

```text
tb_shop
tb_voucher
tb_voucher_order
```

不要为了当前旧业务补表。

---

## 只做下面四件事

### 1. 列出所有 SQL 文件

例如：

```bash
find . -iname "*.sql"
```

输出：

```text
文件路径
文件大小
主要表
是否重复
是否被文档/脚本引用
是否明显 legacy
```

---

### 2. 判断当前项目真正依赖哪套数据库结构

结合：

```text
Mapper
@TableName
SQL
application 配置
README 仅作为辅助
```

判断：

- 哪份 SQL 最接近当前代码实际使用；
- 哪些 SQL 是旧版本；
- 哪些 SQL 是重复副本；
- 哪些 SQL 无法确认。

所有判断以源码为主。

---

### 3. SQL 只允许轻量归档

如果存在明确重复或 legacy SQL：

优先考虑建立：

```text
sql/
  legacy/
```

或项目已有 SQL 目录下合理的：

```text
legacy/
```

再将**能够确定已经废弃**的 SQL 移入。

但必须非常谨慎：

> 如果无法确定是否仍被使用，就不要移动。

禁止直接删除 SQL。

---

### 4. 记录已知数据库问题

例如审计已经发现：

```text
AI 预约依赖 reservation 表，但当前 SQL 未提供
VoucherOrder 手机号查询引用不存在字段
```

本阶段不要修复。

请建立：

```text
docs/refactor/known_database_issues.md
```

或项目已有 docs 结构下合理的位置。

记录：

```text
问题
证据
当前影响
为何本阶段不修
后续阶段建议
```

这类问题留给后续领域重构统一处理。

---

# 六、任务 4：Git / 工程卫生治理

## 目标

清理明显不应进入 Git 仓库的本地产物。

---

## 检查

```text
target/
build/

*.class
*.jar
*.log

logs/
log/

.idea/
.vscode/
*.iml

.DS_Store
Thumbs.db

tmp/
temp/

.env
本地配置
运行时生成文件
```

---

## `.gitignore`

根据当前项目已有 `.gitignore` 做增量完善。

不要机械覆盖用户已有规则。

---

## 对已经 tracked 的垃圾文件

先运行：

```bash
git ls-files
```

确认哪些已经被 Git 跟踪。

如果只是：

```text
target
log
IDE 配置
明显本地产物
```

可以从 Git 索引中移除。

但不要擅自删除业务文件。

---

## 特别注意

如果日志中可能包含：

```text
密码
API Key
用户手机号
Token
请求信息
```

需要记录为安全风险。

---

# 七、本阶段禁止处理的已知问题

即使你在代码里看到，也不要修改：

## 1. 黑马点评业务遗留

```text
hmdp
Shop
Voucher
Blog
Follow
tb_shop
/shop
```

留给下一阶段。

---

## 2. README 与代码不一致

包括：

```text
Kafka
Caffeine
滑动窗口限流
AOP
SpringTask
支付乐观锁
```

本轮不改 README。

---

## 3. 秒杀可靠性

包括：

```text
JVM Queue
库存初始化
异常补偿
消息可靠性
```

本阶段不处理。

---

## 4. AI 业务不完整

包括：

```text
reservation 表
VoucherOrder.phone
AI 预约
业务 Tool
```

只记录，不修。

---

## 5. 无测试

本阶段不要为所有业务补测试。

只允许：

- 执行已有测试；
- 为本阶段非常小的工具性修改补必要测试（如果确实有价值）。

不要大规模建立测试体系。

---

## 6. Docker

本阶段不要新增完整 Docker Compose。

---

# 八、执行顺序

严格按照：

```text
Step 1
记录当前 git status

Step 2
执行 Maven compile，记录失败

Step 3
最小化修复编译阻断

Step 4
重新 compile 验证

Step 5
扫描敏感配置

Step 6
环境变量化 + example 配置 + gitignore

Step 7
SQL 清单和使用关系梳理

Step 8
工程卫生清理

Step 9
再次执行 compile

Step 10
查看 git diff

Step 11
检查是否误修改业务

Step 12
生成报告
```

---

# 九、修改后的验证

至少执行：

```bash
git status
git diff --check
```

Maven：

```bash
mvn clean compile
```

根据实际模块分别执行。

如成本合理，可以执行：

```bash
mvn test
```

但测试失败要区分：

```text
代码失败
外部环境未配置
数据库未启动
Redis 未启动
API Key 未提供
```

不要为了“全绿”而修改不属于本阶段的问题。

---

# 十、必须检查“业务行为是否被误改”

完成后搜索：

```bash
git diff
```

确认没有不必要修改：

```text
Controller 业务
Service 业务
Mapper SQL 业务
Lua 业务逻辑
AI Tool 业务逻辑
Entity 字段
API 路径
```

如果有误改，请回退。

---

# 十一、最终交付文件

请创建：

```text
docs/refactor/phase1/
```

生成：

```text
docs/refactor/phase1/PHASE1_REPORT.md
docs/refactor/phase1/SQL_INVENTORY.md
docs/refactor/phase1/SENSITIVE_CONFIG_AUDIT.md
docs/refactor/known_database_issues.md
```

---

# 十二、PHASE1_REPORT.md 内容

必须包含：

## 1. 本阶段结论

```text
是否完成基础工程基线治理
```

---

## 2. 编译修复

表格：

| 文件 | 原问题 | 修改 | 验证 |
|---|---|---|---|

---

## 3. 编译结果

分别列：

```text
模块
命令
结果
```

---

## 4. 敏感配置修改

列出：

```text
原配置文件
敏感字段
新的环境变量名
示例配置位置
```

不要在报告里再次打印真实密钥。

---

## 5. SQL 梳理

总结：

```text
当前主要 SQL
legacy SQL
重复 SQL
无法确认 SQL
```

详细内容链接：

```text
SQL_INVENTORY.md
```

---

## 6. 工程卫生

说明：

```text
.gitignore 新增规则
移除的 tracked 垃圾文件
保留但需要后续人工处理的文件
```

---

## 7. 本阶段刻意没有修改的内容

明确写：

```text
README
package
项目名
业务领域
秒杀
AI 业务
Docker
测试体系
```

避免误以为遗漏。

---

## 8. 仍然存在的已知问题

根据当前实际代码列出。

---

## 9. 下一阶段建议

下一阶段只建议：

> 项目身份去黑马化

例如：

```text
项目名
artifactId
package
Application
数据库工程命名
配置命名
```

不要在本阶段直接实现。

---

# 十三、SENSITIVE_CONFIG_AUDIT.md

包含：

```text
扫描范围
发现的敏感配置类型
修改位置
环境变量
是否存在 Git 历史风险
是否建议轮换凭证
```

禁止输出真实密码/API Key。

---

# 十四、SQL_INVENTORY.md

表格：

| SQL 文件 | 主要表 | 与当前源码匹配程度 | 是否重复 | 当前建议 |
|---|---|---|---|---|

当前建议仅允许：

```text
保留
legacy 归档
无法确认，暂不处理
```

本阶段不要写：

```text
重建为 Activity
改成 Venue
```

这些属于后续设计。

---

# 十五、known_database_issues.md

至少记录审计中已经发现但本阶段不修的：

```text
reservation 表缺失
VoucherOrder 手机号查询字段不一致
```

并继续基于实际代码补充其他类似问题。

格式：

```markdown
## 问题名称

### 证据
...

### 当前影响
...

### 本阶段处理
不修复

### 原因
后续领域模型将整体重构，当前打补丁会产生返工。

### 后续建议
Phase 3 领域重构时统一处理。
```

---

# 十六、最终验收标准

本阶段完成必须尽量满足：

## 构建

```text
当前可构建 Maven 模块 compile PASS
```

如果确有外部不可控阻断，必须说明。

---

## 安全

```text
当前工作区代码中无已识别真实数据库密码
无已识别真实 API Key
```

---

## 配置

```text
开发者能够通过环境变量恢复本地配置
```

---

## SQL

```text
知道当前项目依赖哪些 SQL
知道哪些属于 legacy / 重复 / 未确认
没有进行领域表重写
```

---

## Git

```text
.gitignore 合理
仓库不继续跟踪明显构建产物和运行垃圾
```

---

## 业务

```text
业务 API 行为没有主动变化
```

---

# 十七、遇到不确定情况时的处理原则

如果某个修改存在明显风险，例如：

```text
不知道 SQL 是否仍在使用
不知道配置是否由 IDE 注入
不知道某个日志文件是否业务需要
不知道某个 dependency 是否可删除
```

不要猜。

采用：

```text
保留现状
+
记录到报告
```

本阶段优先保证：

> **修改范围可控、容易回滚、不影响旧业务。**

---

# 十八、最终 Codex 回复格式

完成后请输出：

```text
Phase 1 基础工程基线治理完成。

1. Maven compile：
2. 共修复编译问题：
3. 敏感配置治理：
4. SQL 状态：
5. Git / 工程卫生：
6. 是否修改业务代码：
7. 是否修改 README：否
8. 仍存在的主要问题：
9. 下一阶段建议：

详细报告：
docs/refactor/phase1/PHASE1_REPORT.md
```

如果没有完成某项，不要写“完成”。

明确写：

```text
未完成 / 阻断原因
```

---

# 十九、本阶段核心目标再次强调

本轮不是业务重构。

本轮不是把项目包装成城市活动平台。

本轮不是增加技术亮点。

本轮只做：

> **BUILDABLE + SAFE CONFIG + SQL CLARITY + CLEAN REPOSITORY**

先将开源项目整理成一个稳定的工程基线，再进入下一阶段的项目身份和领域重构。
