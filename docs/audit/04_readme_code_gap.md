# 04 README 与代码一致性审计

根 `README.md` 在技术栈中声称 Lua + Kafka、Caffeine、订单生命周期、滑动窗口限流；`backend/README.md` 又声称模块 REST 调用、`IAiConsultantService`、WebClient、Actuator 和不同端口。以下以仓库代码为准。

| README 声称 | 实现状态 | 证据 | 完整度 / 风险 |
|---|---|---|---|
| Spring Boot + MySQL + Redis + MyBatis-Plus | 是 | POM、YAML、实体 Mapper Service | 核心存在；构建当前失败 |
| Lua 秒杀 | 部分 | `seckill.lua` + `VoucherOrderServiceImpl` | 有原子预扣，但库存初始化/可靠消费缺失 |
| Kafka 异步创建订单 | 否 | POM 无 Kafka；源码无 Kafka API | 文档与代码明显不一致，高风险 |
| Caffeine + Redis 二级缓存 | 否 | 无 Caffeine 依赖或调用 | 文档与代码明显不一致，高风险 |
| 缓存空值、互斥锁、逻辑过期 | 部分 | `CacheClient` 存在；实际仅走穿透方案 | 逻辑过期未接入且写法有缺陷 |
| SpringTask 自动关闭未支付订单 | 否 | 无 `@Scheduled`、订单关闭代码 | 未实现 |
| 乐观锁解决支付竞争 | 否 | 无支付 API、Mapper 或状态更新 | 未实现 |
| 滑动窗口限流 / Redis+AOP+注解 | 否 | 无 `@Aspect`、自定义限流注解、限流脚本 | 未实现 |
| LangChain4j / 阿里百炼流式客服 | 是 | consultant POM/YAML、`ChatController`、`ConsultantService` | 基础链路存在，运行依赖外部 Key/Redis/DB |
| Redis 会话记忆 | 是 | `RedisChatMemoryStore` | 基础实现，空值读取缺少保护 |
| Function Calling 查询与预约 | 部分 | 三个 `@Tool` | 预约表和券查询 SQL 不匹配，无法确认端到端可用 |
| RAG | 部分 | `ContentRetriever` Bean 和 Redis embedding starter | 未发现知识入库流程或资料 |
| 两模块 REST/WebClient/健康检查 | 否 | core 无 WebClient、IAiConsultantService、`/ai/*`；consultant POM 无 actuator | backend README 不一致 |
| consultant 为 Spring Boot 2.7.18 | 否 | `backend/consultant/pom.xml` 实际继承 `spring-boot-starter-parent:3.5.0` | backend README 不一致 |

README 可信度结论：已证实 6 项（Spring/Web、MySQL、Redis、MyBatis-Plus、Redisson/Lua 部分、AI 流式记忆）；部分实现 4 项（缓存、秒杀异步、Function Calling、RAG）；未发现或明显不一致 7 项（Kafka、Caffeine、定时关单、支付乐观锁、限流、模块 REST 调用、完整 Docker）。性能指标、QPS 或提升比例均未提供 JMeter/Benchmark/测试报告，不应作为事实写入简历。
