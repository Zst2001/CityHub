# CityHub 社区业务审计

审计范围为 `backend/core` 当前源码、`RedisConstants`、`cityhub_schema.sql` 与仓库内前端文件检索结果；结论不依赖黑马点评教程推测。

| 项目 | 当前真实实现 |
| --- | --- |
| Blog 发布 | `POST /blog`，请求体为 `Blog`；控制器从 `UserHolder` 写入 `userId`，`BlogServiceImpl.saveBlog()` 保存后向粉丝 Feed 投递。 |
| Blog 详情 | `GET /blog/{id}`；返回 Blog、作者昵称/头像及当前用户 `isLike`。 |
| 热门 Blog | `GET /blog/hot?current=1`；按 MySQL `liked` 倒序分页。 |
| 点赞/取消点赞 | `PUT /blog/like/{id}`；同一接口根据 Redis ZSet 是否已有当前用户切换状态。MySQL 执行 `liked ± 1`，Redis 使用 ZSet。 |
| 点赞用户 | `GET /blog/likes/{id}`；调用 `ZRANGE blog:liked:{id} 0 4`，再查 `tb_user`，因此当前顺序是点赞时间从早到晚的前五位。 |
| 关注/取关 | `PUT /follow/{id}/{isFollow}`；`true` 写 `tb_follow` 和 Redis Set，`false` 删除二者。 |
| 是否关注 | `GET /follow/or/not/{id}`；查询 `tb_follow`。 |
| 共同关注 | 存在：`GET /follow/follow/common/{id}`；`SINTER user:follows:{currentUserId} user:follows:{id}` 后查询用户信息。 |
| Feed | `GET /blog/of/follow?lastId={max}&offset={offset}`；发布者查询其粉丝，再将 Blog ID 写入每个粉丝的 Feed。 |
| Feed 模式/结构 | 推模式（fan-out on write），Redis ZSet，Key 为 `feed:{followerUserId}`，member 是 Blog ID，score 为 `System.currentTimeMillis()`。 |
| Feed 滚动分页 | 支持：`reverseRangeByScoreWithScores(key, 0, max, offset, 2)`，响应 `ScrollResult` 的 `minTime` 与 `offset`。 |
| BlogComments | 仅存在 Entity、Mapper、`IBlogCommentsService` 和空的 `BlogCommentsServiceImpl`；没有 Controller/API，未形成完整评论业务链。 |
| Blog 字段 | `id`、历史兼容的 `shopId`、`activityId`、`userId`、非持久化作者 `icon/name/isLike`、`title/images/content/liked/comments/createTime/updateTime`。 |
| 与 Activity 的关联 | 本阶段新增可空 `activityId` ↔ `tb_activity.id` 的应用层校验（无外键）；新增活动动态查询。 |
| Redis 社区 Key | 点赞 `blog:liked:{blogId}`；关注集合 `user:follows:{userId}`；Feed `feed:{userId}`。 |

前端检索：仓库当前未包含可运行的前端业务源码；未对前端视觉或社区页面作任何修改。

`like.lua` 文件仍存在，但 `BlogServiceImpl` 对它的执行代码已被注释；当前真实点赞路径是 MySQL 更新加 Redis ZSet 操作，而非 Lua。
