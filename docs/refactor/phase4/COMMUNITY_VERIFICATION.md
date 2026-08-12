# CityHub 社区真实验证矩阵

环境：独立 Docker MySQL 8（host `3307`）与 Redis（host `6379`）；先按 `backend/core/src/main/resources/db/cityhub_schema.sql` 重建社区测试表。执行 `CommunityFlowIntegrationTest` 时通过真实 Spring Boot 随机端口 HTTP、MySQL 和 Redis 验证。

| 场景 | 预期 | 实际 | 状态 |
| --- | --- | --- | --- |
| 发布活动动态 | 保存 `activityId=1` | `POST /blog` 成功，`tb_blog.activity_id=1` | PASS |
| 无效 Activity | 不保存动态 | `activityId=999999` 返回业务失败 | PASS |
| Blog 详情 | 正常返回新动态 | `GET /blog/{id}` 返回新 Blog | PASS |
| Activity 相关动态 | 仅返回 `activity_id=1` | `GET /blog/of/activity?activityId=1&current=1` 返回结果均为 1 且包含新 Blog | PASS |
| 点赞 | `liked +1`、ZSet 有用户 | MySQL 从 0 到 1，`ZSCORE blog:liked:{id} {userId}` 非空 | PASS |
| 取消点赞 | `liked -1`、ZSet 删除用户 | MySQL 回到 0，`ZSCORE` 为 null | PASS |
| 点赞用户 | 可查询当前点赞用户 | `GET /blog/likes/{id}` 返回点赞用户 | PASS |
| Follow | 新增 DB 与 Redis Set 关系 | A 关注 B，`tb_follow` 有一条关系 | PASS |
| Unfollow | 删除 DB 与 Redis Set 关系 | A 取消关注 B，关系数为 0，Set 不含 B | PASS |
| 共同关注 | 返回共同关注 C | A、B 均关注 C，`GET /follow/follow/common/{B}` 返回 C | PASS |
| Feed | 粉丝能看到被关注者新动态 | B 发布后，A 的 `GET /blog/of/follow` 返回新 Blog | PASS |
| Feed 滚动分页 | 返回分页游标 | Feed 响应包含非空 `minTime` 与 `offset` | PASS |
| 评论 | 非阻断项 | 无 BlogComments Controller/API，未实现完整链路 | N/A |
