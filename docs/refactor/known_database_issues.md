# 已知数据库问题

本文件记录已由源码与 SQL 对照确认、但不属于 Phase 1 基础工程基线治理范围的问题。本阶段不修改业务表、Mapper 或 AI 工具逻辑。

## AI 预约依赖的 `reservation` 表缺失

### 证据

- `backend/consultant/src/main/java/com/yjshz/consultant/mapper/ReservationMapper.java` 使用 `insert into reservation(...)` 和 `select * from reservation ...`。
- 当前三个提交的 SQL 文件（`backend/core/src/main/resources/db/yjshz.sql`、consultant 下两份 `hmdp.sql`）均未包含 `CREATE TABLE reservation`。

### 当前影响

AI 工具调用预约创建或查询时，目标库若未在外部手工创建该表，将发生 SQL 表不存在错误。

### 本阶段处理

不修复。

### 原因

该表属于后续领域模型重构范围；在旧业务模型上补表会产生返工。

### 后续建议

Phase 3 领域重构时统一设计预约及其关联表，并以版本化迁移脚本交付。

## AI 按手机号查询优惠券的字段与主 SQL 不一致

### 证据

- `backend/consultant/src/main/java/com/yjshz/consultant/mapper/VoucherOrderMapper.java` 查询 `tb_voucher_order where phone = #{phone}`。
- `backend/core/src/main/resources/db/yjshz.sql` 的 `tb_voucher_order` 仅定义 `id`、`user_id`、`voucher_id`、支付/状态/时间字段，没有 `phone` 列。

### 当前影响

按手机号查询用户优惠券的 AI 工具在主 SQL 初始化的 schema 上会发生列不存在错误。

### 本阶段处理

不修复。

### 原因

这是 AI 业务与旧订单模型的契约问题，Phase 1 禁止修改 AI、VoucherOrder 和数据库业务结构。

### 后续建议

后续领域重构时以认证用户身份和订单关联关系重新设计查询，不直接以手机号作为订单字段查询条件。

## 多份 SQL 初始化文件缺少唯一入口

### 证据

- `backend/core/src/main/resources/db/yjshz.sql` 定义当前 core 实体映射的主要 `tb_*` 表。
- consultant 下 `hmdp.sql` 与 `content/hmdp.sql` SHA-256 完全相同。
- 当前 Java、YAML、POM、脚本中没有发现对这些 SQL 文件的运行时引用；数据库连接均指向同一 schema。

### 当前影响

开发者无法仅通过工程配置判断应执行哪一份初始化脚本，容易产生 schema 漂移。

### 本阶段处理

不移动、不删除 SQL；详见 `docs/refactor/phase1/SQL_INVENTORY.md`。

### 原因

无法仅凭静态源码证明 consultant 的重复文件在外部部署流程中不再使用。Phase 1 仅记录，避免误删。

### 后续建议

领域重构前确定单一、版本化的数据库迁移入口，并归档明确废弃的旧脚本。
