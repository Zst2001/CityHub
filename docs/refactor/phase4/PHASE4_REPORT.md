# Phase 4 CityHub 社区业务轻量适配报告

## 阶段结论

Phase 4 通过。Blog、Follow 与 Feed 保留原类名和现有 Redis 技术路线，并已完成活动体验动态的最小关联与真实集成验证。

## 社区定位与 Activity 关联

- `Blog` 保留类名，业务语义定位为活动体验动态、活动笔记和活动攻略。
- `Blog`、`tb_blog` 新增可空 `activityId/activity_id`；DDL 添加普通索引 `idx_blog_activity`。
- `POST /blog` 接受 `activityId`；非空时以 `IActivityService.getById()` 验证活动存在，不存在返回失败；未增加预约资格、活动状态或外键约束。
- 新增 `GET /blog/of/activity?activityId={id}&current={page}`，按 `activity_id` 过滤并按创建时间倒序分页。
- `cityhub_schema.sql` 加入四条 CityHub 活动体验动态 Seed，分别关联活动 1 至 4。

## 保留并验证的社区实现

- 点赞：MySQL `tb_blog.liked` 与 Redis ZSet `blog:liked:{blogId}`。修复了现有写路径少冒号、与读取路径不一致的阻断问题；点赞和取消点赞均已真实验证。
- 点赞用户：`GET /blog/likes/{id}` 真实使用 ZSet 的 `ZRANGE 0 4` 查询并返回用户；当前排序含义是最早点赞的前五位，未改造排行榜算法。
- Follow：继续使用 `tb_follow` 与 Redis Set `user:follows:{userId}`；关注和取消关注均已验证。
- 共同关注：当前实现真实存在，以 Redis Set 交集支持，已验证 A/B 共同关注 C。
- Feed：继续使用推模式 Redis ZSet `feed:{followerUserId}`，score 是毫秒时间戳，且保留 `lastId/max + offset` 滚动分页。修复了原有 Fan-out 方向错误：发布者现在向其粉丝的 Feed 写入 Blog ID，而不是向其关注对象写入。
- BlogComments：只有实体、Mapper、空 Service；没有 Controller/API，当前为未形成完整业务链，按阶段规则记为 N/A。

## 真实运行验证

独立 MySQL/Redis 环境中执行的 `CommunityFlowIntegrationTest` 使用真实 Spring Boot 随机端口 HTTP 请求、Redis Token 登录与 MySQL：

1. A 关注 B、A 和 B 共同关注 C；共同关注 API 返回 C。
2. B 发布 `activityId=1` 的活动体验动态；MySQL 保存成功，Blog 详情和活动筛选均返回该动态。
3. A 的 Feed 返回 B 新发布的动态，并有 `minTime`、`offset` 游标。
4. A 点赞后 MySQL `liked=1`、Redis ZSet 有成员、点赞用户 API 返回 A；再次调用后 MySQL 回到 0 且 ZSet 成员移除。
5. A 取消关注 B 后，`tb_follow` 与 Redis Set 的关系都被删除。
6. 无效 `activityId` 的发布请求返回业务失败。

## 工程验证

- `JAVA_TOOL_OPTIONS=--add-opens=java.base/java.lang.invoke=ALL-UNNAMED mvn clean compile -DskipTests`：CityHub、CityHub Core、CityHub AI 均 `BUILD SUCCESS`。
- 使用独立 MySQL/Redis 环境变量执行 `mvn -pl core test -Dtest=CommunityFlowIntegrationTest`：1 test，0 failures，0 errors，`BUILD SUCCESS`。
- 集成测试以 `@SpringBootTest(webEnvironment = RANDOM_PORT)` 启动了完整 CityHub Core，包含 MySQL、Redis、Redisson、异步秒杀 Consumer 和 Web 层；社区 HTTP 链路无启动/字段/Mapper 错误。
- 另以独立进程启动 Core 到 `8081`，`GET /activity/1` 返回 HTTP 200；验证后已停止该进程。

## Git 与边界

本报告在完成提交和推送后补充 commit hash 与 push 结果。

本阶段未修改 Redis 活动缓存、秒杀、Lua、Redisson、BlockingQueue、RedisIdWorker、AI、前端视觉或 README。
