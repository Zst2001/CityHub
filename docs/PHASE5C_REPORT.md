# Phase 5C CityHub 活动社区与用户基础页面报告

## 结论

**通过。** CityHub 已形成“浏览体验 → 登录 → 点赞/关注 → Following Feed → 发布活动体验 → 个人动态”的轻量社区链路。未修改 README、缓存、Lua、Redisson、BlockingQueue、秒杀或 AI。

## 基线与后端最小调整

- Phase 5B 真实提交：`8c827e0 feat: build CityHub activity discovery experience`。
- Blog 查询层的 `isBlogLiked()` 原本已经对 `UserHolder` 为 `null` 安全返回，未发生 NPE；阻断点是 MVC 登录拦截器。
- 仅开放纯读 Blog 路由：`/blog/hot`、`/blog/of/activity`、`/blog/likes/**`、`/blog/{id}`；写 Blog、Like、Follow、Following Feed 和我的动态仍要求登录。
- 前端真实发布暴露旧表 `tb_blog.shop_id NOT NULL` 与 CityHub `activityId` 发布语义冲突。`BlogServiceImpl.saveBlog()` 仅在缺省时补 `shopId=0`，保持历史列兼容；未修改 Blog、Follow、Redis ZSet 或 fan-out 架构。
- 不需要新增“按用户查询 Blog”接口：已有受认证保护的 `GET /blog/of/me`，Profile 直接复用。

## 前端交付

- `/community?tab=hot`：未登录可浏览热门动态；正式单列 Feed、Loading/Empty/Error、加载更多与发布入口。
- `/community?tab=following`：URL 保持 Tab 状态；未登录展示携带 redirect 的登录引导；登录后调用真实 `lastId + offset` Following Feed。
- `BlogCard`：头像、昵称、时间、关联 Activity 跳转、正文摘要、Blog 图片或 Activity 封面降级、点赞与 Follow 按钮；没有图片时不强制 fallback 图。
- 点赞/取消点赞复用 `PUT /blog/like/{id}`；未登录转登录，已登录局部更新 `isLike/liked`。
- Follow/Unfollow 复用 `PUT /follow/{id}/{isFollow}` 和状态读取；本人不显示 Follow。
- 发布体验使用 Dialog，必须选择真实 Activity，校验标题/正文，提交 `activityId/title/content/images=''`。
- `/profile` 仍受 requiresAuth：展示真实 UserDTO 头像/昵称和 `GET /blog/of/me` 的我的动态；UserDTO 不含手机号，因此不伪造电话资料。
- Activity Detail 的体验预览改为复用同一个只读 `BlogCard`。由于 Blog 读路由已开放，未登录也能看到对应 Activity 的体验。

## 真实联调结果

临时环境：MySQL Docker `cityhub-phase3b-mysql`（3307）、Redis Docker（6379）、Core（8081）、Vite（5173）。临时登录码与浏览器测试脚本未写入仓库。

| 验证项 | 结果 | 实际证据 |
| --- | --- | --- |
| 未登录 Hot Blog | PASS | 浏览器 `/community?tab=hot` 渲染 5 张 BlogCard；`GET /blog/hot` 成功 |
| 未登录 Activity Blog | PASS | `GET /blog/of/activity?activityId=1` 成功；详情预览不再依赖登录 |
| Like / Unlike | PASS | Blog 1：`liked 0 → 1 → 0`，`isLike false → true → false` |
| Follow / Unfollow | PASS | 用户 A(7) 对 B(8) `true → false → true` 的接口状态均正确 |
| 发布 Blog | PASS | 浏览器 Dialog 选择真实 Activity、填写标题正文后成功；发布者 Profile 显示 1 条我的动态 |
| Feed fan-out | PASS | A(7) 关注 B(8)；B 发布关联 Activity 5 的 Blog 5；A 的 `/blog/of/follow` 第一项为 Blog 5 |
| Feed minTime / offset | PASS | 首批返回 Blog `[8,7]`、`minTime=1786597137667`、`offset=1`；用其查询下一批返回 Blog `[5]`，证明真实滚动游标链路 |
| Activity Detail Preview | PASS | B 发布 Blog 5 后，`GET /blog/of/activity?activityId=5` 命中 Blog 5；详情页复用 BlogCard |
| Following 页面 | PASS | 登录浏览器 `/community?tab=following` 显示 1 条关注动态 |
| Profile | PASS | 登录浏览器 `/profile` 显示资料 Header 与我的动态 |
| 1440 / 768 / 390 | PASS | Community 各尺寸均无横向溢出；1440 单列居中 Feed，移动端卡片满宽 |
| `npm run dev` | PASS | Vite 5173 真实浏览器联调 |
| `npm run build` | PASS | Vite production build 成功；仅保留既有 Element Plus 大 chunk 非阻断警告 |
| Maven | PASS | `mvn clean compile -DskipTests` 全 reactor 成功；兼容修复后 `mvn -pl core compile -DskipTests` 再次成功 |

## 已知限制

1. 当前 Blog 接口没有 Activity 标题联表字段；前端一次加载 12 条 Activity 建立映射，避免 BlogCard 的 N+1 请求。
2. Following Feed 的后端批大小为 2，前端按返回的 `minTime/offset` 做“加载更多”，不伪装普通页码。
3. 不做图片上传、评论、共同关注 UI、用户详情、编辑资料、预约列表或社区搜索。
4. 真实临时数据库保留了联调 Blog/用户记录，未进入源码或 Git；生产数据应在独立环境初始化。

## Git 交付

- 分支：`main`
- 功能提交：`feat: build CityHub community experience`
- 提交 hash 与 push 结果在最终交付时以实际 Git 输出为准。
