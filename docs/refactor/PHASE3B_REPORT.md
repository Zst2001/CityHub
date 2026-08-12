# Phase 3B：CityHub 活动查询与 Redis 缓存验证报告

## 结论

本阶段已在真实运行环境中完成 CityHub Core 的活动查询与 Redis 缓存链路验证，并提交了最小范围修复。未修改 README、秒杀 Lua/Redisson/BlockingQueue、Blog/Follow、AI 或前端。

Phase 3A 基线已确认：`e0cbeaf feat: establish CityHub activity reservation domain`，且当时 `main` 与 `origin/main` 一致。

## 本阶段代码变更

- `ActivityCategory` 的表映射由错误的 `activity_category` 更正为 schema 实际使用的 `tb_activity_category`。
- 修复 `CacheClient` 的逻辑过期写入：此前错误地把业务对象直接写入 Redis，现写入 `RedisData(data, expireTime)`，与 `queryWithLogicalExpire` 的读取协议一致。
- 为普通 `cache:activity:{id}` 写入增加 0–5 分钟随机抖动；基础 TTL 仍为 30 分钟，实际范围为 30–35 分钟。空值缓存、登录缓存和逻辑过期缓存未使用该抖动。
- `ActivityServiceImpl.queryById` 仅对普通详情缓存启用随机 TTL；逻辑过期、互斥锁和异步重建路径保留原有策略。
- 新增真实 Spring Boot 集成测试 `ActivityCacheIntegrationTest`，验证逻辑过期缓存返回旧数据并触发异步重建。

## 运行环境与数据库初始化

本机已有 MySQL 服务在 3306 运行，但没有可用的非交互式凭据，因此没有触碰该实例。为避免覆盖已有数据，本阶段创建了独立的临时 MySQL 8.0.34 容器，仅监听本机 3307；Redis 使用已存在的 `redis-vector` 容器（6379）。临时账号和凭据未写入仓库或报告。

`backend/core/src/main/resources/db/cityhub_schema.sql` 已成功导入临时 `cityhub` 库，初始化计数如下：

| 表 | 初始化记录数 |
| --- | ---: |
| `tb_activity_category` | 8 |
| `tb_activity` | 5 |
| `tb_ticket` | 5 |
| `tb_seckill_ticket` | 5 |
| `tb_reservation_order` | 0 |

Core 在 `8081` 成功启动并同时连接 MySQL、Redis 与现有 Redisson 配置。Java 17 运行旧版 MyBatis-Plus 3.4.3 时需要 JVM 参数 `--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`；未升级依赖，本次验证使用该兼容参数。

## 查询接口验证

以下请求均返回 HTTP 200 且业务 `success=true`：

| 接口 | 验证结果 |
| --- | --- |
| `GET /activity-category/list` | 返回 8 个活动分类，并写入 Redis list 缓存 |
| `GET /activity/1` | 返回活动详情，并写入 `cache:activity:1` |
| `GET /activity/of/category?categoryId=3&current=1` | 返回分类活动分页结果 |
| `GET /ticket/list/1` | 返回活动 1 对应票券及秒杀库存信息 |

## 缓存验证

### 缓存穿透

清理 `cache:activity:99999999` 后，两次请求不存在活动均返回业务失败。首次请求后 Redis 中存在空字符串标记，TTL 为 120 秒（2 分钟），第二次请求复用该标记，符合空值缓存防穿透实现。

### 缓存雪崩基础治理

分别请求活动 1、2、3 后，观测到 TTL 为 1859、1920、1860 秒，均位于 1800–2100 秒范围内且彼此不同，证明随机 TTL 已生效。

### 缓存击穿：逻辑过期、互斥锁和异步重建

执行：

```text
mvn -pl core test -Dtest=ActivityCacheIntegrationTest
```

（使用临时 MySQL/Redis 环境与上述 Java 17 兼容参数）测试通过：1 个测试、0 失败、0 错误。测试先写入已逻辑过期的 `RedisData`，调用查询后立即得到旧活动数据；随后由缓存重建线程查询 MySQL 并写回未来的逻辑过期时间。互斥锁 Key 为 `lock:activity:{id}`，算法未改造。

### MySQL 更新后删除 Redis

先预热 `cache:activity:1`，随后通过 `PUT /activity` 更新临时活动标题：

1. 更新前缓存 Key 存在；
2. MySQL 更新成功后 Key 不存在；
3. 下一次 `GET /activity/1` 返回更新值并重新写入 Key；
4. 验证结束后重新导入 Seed schema 并清理相关 Redis Key，临时数据库恢复到初始状态。

该结果与 `ActivityServiceImpl.update` 的“先 `updateById`，后 `delete(cache:activity:{id})`”策略一致。

## GEO

不适用。当前代码只定义了 `ACTIVITY_GEO_KEY` 常量；未发现 `opsForGeo`、GEO 写入或附近活动查询接口，因此未新增 GEO 功能，也未伪造验证结果。

## 验证与边界检查

- `backend` 下 `mvn -pl core clean compile -DskipTests`：通过。
- `ActivityCacheIntegrationTest`：通过。
- `git diff --check`：通过。
- 已检查变更范围：没有 README 修改；未修改秒杀核心链路、Lua、Redisson、BlockingQueue、Blog/Follow、AI 或前端。
- 敏感配置仍为环境变量占位符；未发现新增明文凭据。

## 后续运行说明

正式本地运行时，应提供可用的 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT` 环境变量；在 Java 17 与当前 MyBatis-Plus 版本组合下还需加入上述 `--add-opens` JVM 参数。该兼容要求已验证，但未在仓库内写入机器相关配置。
