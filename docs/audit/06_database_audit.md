# 06 数据库审计

主 SQL：`backend/core/src/main/resources/db/yjshz.sql`；consultant 内有 `hmdp.sql` 与 `content/hmdp.sql` 两份重复 SQL，内容接近但并非统一唯一数据源。

| 表 | 作用 | 主键 / 重要索引 | 审计结论 |
|---|---|---|---|
| `tb_user` | 用户 | PK id；唯一 phone | 登录模型可用；样例用户数据大量保留 |
| `tb_user_info` | 用户详情 | PK user_id | 无显式外键 |
| `tb_shop` | 商铺 | PK id；`type_id`、score 等索引 | 强点评语义；核心 SQL 有索引，适合改造前参考 |
| `tb_shop_type` | 商铺分类 | PK id | 强点评语义 |
| `tb_voucher` | 代金券 | PK id | 无 shop_id 显式外键 |
| `tb_seckill_voucher` | 秒杀库存/时间 | PK voucher_id | 没有 FK；库存与 Redis 初始化未闭环 |
| `tb_voucher_order` | 券订单 | PK id | **没有 `(user_id,voucher_id)` 唯一索引**；无订单查询/支付索引 |
| `tb_blog` | 探店笔记 | PK id | shop_id/user_id 无显式索引，热门按 liked 查询可能全表排序 |
| `tb_blog_comments` | 评论 | PK id | 无 blog_id、user_id、parent_id 索引 |
| `tb_follow` | 用户关注 | PK id | 无 `(user_id,follow_user_id)` 唯一约束；可产生重复关注 |
| `tb_sign` | 签到 | PK id | 表存在但无业务代码；无用户日期唯一约束 |
| `reservation` | AI 预约 | 代码引用 | **任一提交 SQL 均未创建此表** |

所有主要业务表均未发现外键；这本身可为解耦选择，但当前也没有应用层补偿、约束或迁移脚本体系。SQL 中包含 `DROP TABLE IF EXISTS` 和大量黑马点评样例数据，不能直接作为城市活动平台的生产初始化方案。

特别不一致：consultant 的 `VoucherOrderMapper.findByPhone()` 查询 `tb_voucher_order where phone = #{phone}`，但主 SQL 的 `tb_voucher_order` 没有 `phone` 字段。AI “按手机号查已有优惠券”在当前 schema 上会失败。
