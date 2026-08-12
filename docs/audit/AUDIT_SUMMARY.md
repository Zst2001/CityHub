# 审计摘要

## 1. 一句话结论

当前项目可作为“城市文化活动发现与智能预约平台”的二次重构基础，但不适合直接作为差异化实习项目展示：其黑马点评遗留极高，README 存在多项未实现声明，且构建、数据模型和秒杀可靠性存在阻断问题。

## 2. 当前真实技术栈

Spring Boot（core 2.7.18 / consultant 3.5.0）、Java 8/17 双模块、Maven、MySQL、Redis、MyBatis-Plus、Redisson、Lua、Spring MVC/WebFlux、LangChain4j、DashScope OpenAI-compatible 模型、Redis 会话记忆、基础 RAG Retriever 配置、Nginx 静态代理。

未证实：Kafka、RabbitMQ、Caffeine、业务 AOP 限流、SpringTask 自动关单、支付并发闭环、完整 Docker 环境。

## 3. 当前已证实核心业务

手机验证码 Redis Token 登录原型；商铺/分类/券查询与创建；Redis 缓存穿透处理；Redis Lua 秒杀资格校验与 JVM 队列异步落库原型；图文笔记、点赞、关注与 Feed 原型；基于 LangChain4j 的流式 AI 对话、Redis 短期记忆、商家/优惠券查询和到店预约工具原型。

## 4. README 与代码不一致

- Kafka：README 声称使用，POM 与源码均无证据。
- Caffeine 二级缓存：README 声称使用，POM 与源码均无证据。
- 滑动窗口限流、AOP、自定义注解：未找到实现。
- SpringTask 自动关单、支付乐观锁、订单生命周期：未找到实现。
- backend README 声称 core 用 WebClient/REST 调 AI、含 `IAiConsultantService`/健康检查；源码未找到。
- backend README 将 consultant 记为 Spring Boot 2.7.18，实际 consultant POM 是 Spring Boot 3.5.0。
- AI/RAG 有基础代码，但预约表缺失、券订单手机号查询字段不存在，不能宣称完整端到端能力。

## 5. 黑马点评遗留程度

**极高**：Shop/Voucher/SeckillVoucher/VoucherOrder/Blog/Follow、`tb_*` SQL、`hmdp.sql`、`redis_project`、`com.itheima`、`/shop` 等路由和前端请求都保留明显痕迹。

## 6. 当前最严重的 5 个问题

1. 根 Maven 编译失败：`RedisConstants.java` 导入不存在的 CORBA 包。
2. 秒杀可靠性不足：Redis 库存初始化未找到、JVM 队列易丢、异常后仍可能落单、无补偿。
3. 数据库与 AI 工具不一致：`reservation` 表未提供；按手机号查券引用不存在的 `tb_voucher_order.phone`。
4. README 技术亮点与实际代码不一致，存在简历失信风险。
5. 明文 MySQL root 密码、无测试、无完整部署（Nginx 指向缺失 `html/yjshz`）与显著黑马点评遗留。

## 7. 最值得保留的 5 项技术实现

1. Redis Token 拦截器与 `UserHolder` 请求上下文模式。
2. `CacheClient` 的缓存空值/逻辑过期设计思路（需修复后复用）。
3. Redis ID 生成器思路。
4. Lua 原子资格校验与 Redisson 可重入锁的技术选型（需重建业务流程）。
5. LangChain4j 流式会话、Redis memory 与 `@Tool` 调用框架。

## 8. 建议删除 / 重写 / 重构

重写 README、SQL 初始化体系、所有 Shop/Voucher 领域 API、预约/订单模型和 AI 工具安全边界；重构秒杀可靠性、缓存实现、Feed 逻辑和双模块边界；清理重复 hmdp SQL、运行日志与 `com.itheima` 坐标。通用 Redis/Lua/AI 框架可保留，但不应原样复用存在缺陷的实现。

## 9. 城市文化活动平台改造可行性

**适合**。活动发现、内容社区、限量名额和 AI 助手与现有能力有概念映射；但需要中等重构，而非更名换皮。

## 10. 推荐改造路线

1. P0：修复构建、密钥与文档/SQL基线。
2. P1：重建活动/场馆/场次/预约订单领域和数据库约束。
3. P2：实现可靠限量预约、权限、测试与工程治理。
4. P3：完成认证绑定 AI 工具、真实 RAG、完整部署和性能验证。

## 11. 最终判断

### 问题 A

值得继续改造成实习项目吗？**值得，但仅作为重构底座，不能按当前状态投递。**

### 问题 B

改造规模？**中等重构**：保留一部分通用基础设施和 AI 原型，重写领域、数据、关键链路和项目展示。

### 问题 C

最应优先完成的第一件事？**先建立可构建、无明文敏感信息、README/SQL 与实际代码一致的 P0 基线。**

详细报告：

- [01_project_overview.md](01_project_overview.md)
- [02_feature_audit.md](02_feature_audit.md)
- [03_hmdp_legacy_audit.md](03_hmdp_legacy_audit.md)
- [04_readme_code_gap.md](04_readme_code_gap.md)
- [05_engineering_quality.md](05_engineering_quality.md)
- [06_database_audit.md](06_database_audit.md)
- [07_resume_risk_audit.md](07_resume_risk_audit.md)
- [08_city_activity_refactor_feasibility.md](08_city_activity_refactor_feasibility.md)
- [09_refactor_priority.md](09_refactor_priority.md)
