# 01 项目全景审计

审计范围：仓库内源码、Maven POM、YAML、Lua、SQL、Nginx 配置和已提交前端运行日志；未修改业务代码或数据库。

## 工程结构

| 模块 | 实际配置 | 启动类 | 结论 |
|---|---|---|---|
| `backend/core` | Java 8，Spring Boot 2.7.18，Maven | `com.yjshz.YJSHZApplication` | 本地生活/黑马点评核心业务模块，端口 8081 |
| `backend/consultant` | Java 17，Spring Boot 3.5.0，独立 parent POM | `com.yjshz.consultant.ConsultantApplication` | AI 对话与工具调用模块，端口 8084 |
| `frontend` | Nginx Windows 二进制与少量静态文件 | 无前端源码构建配置 | Nginx 监听 8083，反代 `/api` 到 core；配置的 `html/yjshz` 根目录未在当前仓库文件清单中找到 |

证据：`backend/pom.xml` 声明两个 Maven modules；`backend/core/pom.xml` 继承根 POM；`backend/consultant/pom.xml` 自行继承 `spring-boot-starter-parent:3.5.0`，并非根 POM统一管理。

## 真实技术栈（依赖 + 配置 + 调用三项核验）

| 能力 | 结论 | 证据 |
|---|---|---|
| Spring MVC / WebFlux | 已实现 | core 使用 `spring-boot-starter-web`；consultant 使用 `spring-boot-starter-webflux`，`ChatController` 返回 `Flux<String>` |
| MySQL | 已配置并调用 | 两模块 YAML 均配置 `redis_project`；MyBatis-Plus Mapper/Service 被调用 |
| Redis | 已实现 | core 的登录、缓存、点赞、关注、Feed、秒杀均使用 `StringRedisTemplate` |
| MyBatis-Plus | 已实现 | core/consultant POM 均有依赖，实体使用 `@TableName`、Mapper 继承 `BaseMapper` |
| Redisson | 已实现但仅单节点配置 | `core/pom.xml` + `RedissonConfig` + 秒杀订单 `RLock` |
| Lua | 已实现部分调用 | `seckill.lua`、`like.lua`、`unlock.lua`；实际加载秒杀、点赞、解锁脚本 |
| Kafka / RabbitMQ | 未发现 | 所有 POM 无依赖，源码无 producer/consumer/listener |
| Caffeine | 未发现 | POM 无依赖，源码无 `Cache`/`Caffeine` 调用 |
| AOP 业务切面 / 限流 | 未发现 | 仅使用 Spring AOP 的 `AopContext.currentProxy()`；无 `@Aspect`、限流注解或限流 Lua |
| 登录 Token | 已实现 Redis Token，不是 JWT | `UserServiceImpl.login()` 保存 `login:token:{uuid}` Hash；拦截器读取 `authorization` |
| LangChain4j / 阿里百炼 | 已实现 | consultant POM 的 LangChain4j 依赖；`application.yml` 指向 DashScope 兼容接口 |
| Function Calling | 已实现基础工具调用 | `ConsultantService` 声明 `tools = {shopTool,reservationTool,voucherTool}`，工具方法标注 `@Tool` |
| RAG / Redis 向量库 | 配置和 Bean 存在，资料灌入未发现 | `CommonConfig.contentRetriever()` 构建 `EmbeddingStoreContentRetriever`；未发现文档加载、切分、写向量库流程 |
| Docker | 仅 Redis Stack Compose，非完整环境 | `backend/docker-compose.yml` 仅启动 Redisearch/ReJSON，不含 MySQL、两个后端和前端 |
| 自动化测试 | 未发现 | 无 `src/test` 源文件；0 个单测/集成测试 |

## 构建核验

执行了只读命令 `mvn -q -DskipTests compile`（目录：`backend`），结果失败。失败点：`core/src/main/java/com/yjshz/utils/RedisConstants.java` 第 3 行导入 `org.omg.CORBA.PUBLIC_MEMBER`；当前 Maven 所用 JDK 不含 CORBA 包。该 import 在该类中未被使用。

因此，**当前提交状态不能在本审计环境完成根项目编译**。这不是对 JDK 8 环境可否编译的推断；仓库也没有 Maven Toolchains 或可执行的统一 JDK 固定配置来保证构建环境。
