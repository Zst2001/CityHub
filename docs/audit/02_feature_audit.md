# 02 功能审计

## 核心领域模型

| 实体/模型 | 表 | Controller / Service | 已证实作用 | 黑马点评关联 |
|---|---|---|---|---|
| User / UserInfo | `tb_user` / `tb_user_info` | `UserController` / `UserServiceImpl` | 手机验证码登录、用户资料读取 | 高 |
| Shop / ShopType | `tb_shop` / `tb_shop_type` | `ShopController`、`ShopTypeController` | 商铺详情、名称/类型分页、类型列表 | 极高 |
| Voucher / SeckillVoucher | `tb_voucher` / `tb_seckill_voucher` | `VoucherController` / `VoucherServiceImpl` | 普通券、秒杀券创建和店铺券列表 | 极高 |
| VoucherOrder | `tb_voucher_order` | `VoucherOrderController` / `VoucherOrderServiceImpl` | 秒杀下单及异步写库尝试 | 极高 |
| Blog / BlogComments | `tb_blog` / `tb_blog_comments` | `BlogController` / `BlogServiceImpl` | 探店笔记、点赞、热门、Feed；评论实体无公开业务实现 | 高 |
| Follow | `tb_follow` | `FollowController` / `FollowServiceImpl` | 关注、取消、共同关注 | 高 |
| Reservation（consultant POJO） | `reservation` | 无独立 HTTP Controller；AI Tool | AI 创建/查询到店预约 | 新增但 SQL 缺失 |

## 用户体系

`UserController.code` 调用 `UserServiceImpl.sendCode()`：校验手机号后生成六位随机码，写 Redis `login:code:{phone}` 两分钟；代码仅 `log.debug(code)`，没有短信通道。`login()` 校验 Redis 验证码，不存在用户则注册，生成 UUID，保存到 Redis Hash，返回 token。`RefreshTokenInterceptor` 从 `authorization` 读取 token 并刷新 TTL；`LoginInterceptor` 校验 `UserHolder`。

未实现：登出（Controller 直接返回“功能未完成”）、密码登录实际校验、角色/商家权限、验证码发送通道、Token 撤销。`PasswordEncoder` 从未被 `UserServiceImpl` 调用。

## 商铺、查询与缓存

已实现：`GET /shop/{id}`、按类型/名称分页，`GET /shop-type/list`，以及 `PUT /shop` 后先更新 DB 再删除 `cache:shop:{id}`。`CacheClient.queryWithPassThrough()` 在实际 `ShopServiceImpl.queryById()` 中使用，缓存空串两分钟以防穿透。

部分实现：`CacheClient.queryWithLogicalExpire()` 与 `ShopServiceImpl.queryWithMutex()` 存在，但 `queryById()` 未调用它们。前者另有缺陷：重载的 `CacheClient.set(key, object, expireSeconds)` 创建 `RedisData` 却把原 `object` 写入 Redis，不能生成逻辑过期结构。无 Caffeine，不能称二级缓存。

未实现：Redis GEO 查询。虽定义 `SHOP_GEO_KEY`，没有 `opsForGeo` 调用；没有附近店铺 API。无缓存预热、缓存雪崩随机 TTL 策略。

## 秒杀与优惠券

`VoucherServiceImpl.addSeckillVoucher()` 用事务写 `tb_voucher` 与 `tb_seckill_voucher`。`VoucherOrderServiceImpl.seckillVoucher()` 执行 `seckill.lua`：读取 `seckill:stock:{voucherId}`，库存大于 0 且用户不在 `seckill:order{voucherId}` Set 时，递减 Redis 库存并登记用户；随后将订单放入 JVM `ArrayBlockingQueue`，单线程消费者在事务方法中写 MySQL。

关键限制：没有看到创建秒杀券后向 Redis 初始化 `seckill:stock:{voucherId}` 的代码；Lua 的库存键可能为 `null`，`tonumber(nil) <= 0` 会报错。异步队列不是 Kafka/MQ，服务重启会丢单，没有重试、死信或补偿。MySQL 无 `(user_id, voucher_id)` 唯一索引。`createVoucherOrder1()` 发现重复订单或扣库存失败只记录日志，仍会继续 `save(voucherOrder)`；`handleVoucherOrder()` 锁失败后也继续调用并在 finally 无条件 `unlock()`。因此不可描述为可靠的“严格防超卖、一人一单”。

## Blog / Feed / 关注

已实现：发布 `Blog`、DB 原子增减点赞、Redis ZSet 点赞列表、热门按 `liked` 分页、关注/共同关注、发布后将笔记 ID 写入 Feed ZSet、滚动分页读取 Feed。

一致性问题：同一点赞 key 在不同方法中不一致：`likeBlog()`/`isBlogLiked()` 用 `blog:liked` + id（缺少分隔符），`queryBlogLikes()` 用 `blog:liked:` + id。因此实际点赞后“是否点赞”和点赞排行榜读取不同 Redis key。`saveBlog()` 查询的是“谁关注当前作者”的记录，却映射 `Follow::getFollowUserId`，写入关注对象的收件箱而非粉丝收件箱，Fan-out 方向错误。`like.lua` 存在且加载，但实际 Lua 调用及异步同步 DB 的代码已整体注释。

未实现：评论 Controller/Service 业务、签到 API、Bitmap/HyperLogLog、GEO、内容审核。

## AI 咨询与预约

已实现：consultant 的 `/chat` 返回流式 `Flux`；`RedisChatMemoryStore` 以 memoryId 保存聊天历史一天；`ConsultantService` 配置 DashScope、RAG retriever 与三个 `@Tool`。`ReservationTool` 可按姓名、手机号、时间、商家名写/查预约。

不能确认完整可用：`ReservationMapper` 写 `reservation` 表，而三个 SQL 文件均未建该表；`VoucherOrderMapper.findByPhone()` 查询 `tb_voucher_order.phone`，但主 SQL 的该表没有 `phone` 列；查询商铺为空时会 NPE；工具接口没有身份绑定，按任意手机号读取预约/券；chat 使用 GET 风格 `@RequestMapping`，无参数校验或访问控制。
