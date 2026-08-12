# SQL 使用情况清单

审计方法：列举 `*.sql` 源文件；以 `@TableName`、Mapper、YAML 数据源、Java/配置/脚本对 SQL 文件名的引用为依据。`target/classes` 中的 SQL 为构建复制产物，已由 `.gitignore` 排除，不列作源码 SQL。

| SQL 文件 | 主要表 | 与当前源码匹配程度 | 是否重复 | 当前建议 |
|---|---|---:|---|---|
| `backend/core/src/main/resources/db/yjshz.sql` | `tb_user`、`tb_user_info`、`tb_shop`、`tb_shop_type`、`tb_voucher`、`tb_seckill_voucher`、`tb_voucher_order`、`tb_blog`、`tb_blog_comments`、`tb_follow`、`tb_sign` | 高：core 的实体 `@TableName` 与 Mapper 大多对应这些表 | 否：与 consultant SQL 内容不同 | 保留，当前最接近 core 源码的初始化结构 |
| `backend/consultant/src/main/resources/hmdp.sql` | `tb_*` 点评业务表 | 中：consultant 查询 `tb_shop`、`tb_voucher`、`tb_voucher_order`，但 `reservation` 不在文件内 | 是：与下行文件 SHA-256 相同 | legacy 记录，暂不移动 |
| `backend/consultant/src/main/resources/content/hmdp.sql` | 与上一文件相同 | 中：同上 | 是：与 `resources/hmdp.sql` 完全相同 | legacy 重复副本，暂不移动 |

## 当前实际依赖关系判断

1. 两模块 YAML 都连接同一 MySQL schema `redis_project`（名称未在本阶段改动）。
2. core 的实体、Mapper XML 与 SQL 表结构最直接匹配 `db/yjshz.sql`；例如 `Shop` → `tb_shop`、`VoucherOrder` → `tb_voucher_order`。
3. consultant 直接访问相同的 `tb_shop`、`tb_voucher`、`tb_voucher_order`，因此可部分依赖任一包含这些表的旧 SQL；但没有源码、配置、容器或启动脚本在运行时引用某一个 SQL 文件，无法确认真实外部初始化入口。
4. consultant 两份 hmdp SQL 是字节内容完全一致的重复文件。虽然强烈呈 legacy 特征，本阶段遵循“无法确认外部使用即不移动”的约束。

## 已知结构问题

详见 [known_database_issues.md](../known_database_issues.md)：缺失 `reservation` 表、`tb_voucher_order.phone` 查询字段不一致、SQL 初始化入口不唯一。
