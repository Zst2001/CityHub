# 03 黑马点评遗留审计

结论：**遗留程度：极高**。核心领域、表结构、路由、Redis key、样例数据和前端请求均仍是黑马点评语义；只替换 package 为 `com.yjshz` 并新增独立 AI 模块不足以形成新的业务项目。

| 类型 | 文件/位置 | 当前内容 | 严重度 | 后续必须处理 |
|---|---|---|---|---|
| SQL 文件名 | `backend/consultant/src/main/resources/hmdp.sql`、`content/hmdp.sql` | 两份同源的 hmdp SQL | 极高 | 是 |
| SQL 模型 | `core/.../db/yjshz.sql` | `tb_shop`、`tb_voucher`、`tb_seckill_voucher`、`tb_blog`、`tb_follow` 等 | 极高 | 是 |
| 应用数据源 | 两模块 YAML | 数据库名 `redis_project` | 高 | 是 |
| Maven 坐标 | `consultant/pom.xml` | `groupId` 仍为 `com.itheima` | 极高 | 是 |
| 业务 API | core Controllers | `/shop`、`/voucher`、`/voucher-order`、`/blog`、`/follow` | 高 | 是 |
| Java 领域 | core entity/service | Shop、Voucher、SeckillVoucher、VoucherOrder、Blog、Follow | 极高 | 是 |
| Redis key | `RedisConstants` | `cache:shop:`、`shop:geo:`、`seckill:stock:`、`blog:liked:` | 高 | 是（业务语义） |
| 前端运行证据 | `frontend/logs/access.log` | 大量 `/api/shop`、`/api/blog`、`/api/follow` 请求 | 高 | 是 |
| AI 工具 | consultant tools/mapper | 工具说明与查询仍为商家、优惠券、到店消费 | 高 | 是 |

可保留的通用技术思路：`Result` 统一响应、Redis Token 拦截、Redis ID、缓存穿透/逻辑过期的设计意图、Redisson、Lua 原子操作、ZSet Feed。它们不是黑马点评痕迹；但现有实现质量须先修复后再复用。
