# Codex 执行任务：Phase 5C CityHub 活动社区 + 用户基础页面

## 一、阶段背景

项目：**CityHub - 城市活动发现与预约平台**

当前已经完成：
- Phase 1：工程治理
- Phase 2：CityHub 工程身份规范化
- Phase 3A-R：核心领域迁移
- Phase 3B：Activity + Redis 缓存
- Phase 3C：限量预约 / 秒杀
- Phase 4：社区后端 Blog / Follow / Feed 适配
- Phase 5A：Vue 3 工程骨架、Router、Axios、Pinia、登录、Design System
- Phase 5B：首页、Activity List、Activity Detail、Ticket / Reservation UI

当前前端主业务链已经形成：

```text
首页
→ 活动发现
→ 活动详情
→ Ticket
→ 登录
→ 预约
```

Phase 5C 要补齐第二条用户链：

```text
浏览活动体验
→ 登录
→ 点赞
→ 关注作者
→ 查看关注 Feed
→ 发布自己的活动体验
→ 个人中心查看自己的内容
```

---

## 二、本阶段定位

本阶段只做：

### Community
- 热门动态
- 关注 Feed
- BlogCard
- 点赞 / 取消点赞
- Follow / Unfollow
- 发布 Activity Blog

### Profile
- 用户基础资料
- 我的动态（若后端已有或允许最小补充）

Community 约占本阶段 70% 精力，Profile 约占 30%。

不要把 CityHub 做成复杂社交平台。

---

## 三、必须保留的现有技术路线

最大程度复用 Phase 4 已验证的：

```text
Blog
Blog.activityId
Redis ZSet Like
点赞用户查询
Follow
Redis Set
共同关注
Feed ZSet
滚动分页
```

禁止重写社区后端架构。

---

## 四、禁止事项

禁止：
- 私信
- 通知
- 话题
- 标签
- 粉丝排行榜
- 认证等级
- 复杂评论
- 复杂用户主页
- 社区推荐算法
- 社区搜索
- 图片上传系统
- OSS / MinIO
- Redis Stream
- Kafka / RabbitMQ
- AI 顾问迁移
- Nginx 正式部署
- README 重写

禁止修改：
- Redis 缓存
- Lua
- Redisson
- BlockingQueue
- RedisIdWorker
- 秒杀核心
- Activity / Ticket 核心架构

只有前端接入时发现真实阻断问题，才允许做最小后端修复。

---

## 五、Git 基线

开始前执行：

```bash
git status --short
git log --oneline -n 10
git branch --show-current
git remote -v
```

要求：
- main
- working tree clean
- main == origin/main

确认 Phase 5B 最新真实 commit hash。

如果报告和 Git 记录不一致，以 `git log --oneline` 为准。

---

# 六、执行顺序

按下面顺序完成，但仍然是一个阶段、一个最终 commit、一个报告：

```text
5C-1 社区 API / 权限审计
→ 5C-2 Community + BlogCard
→ 5C-3 Like + Follow + Feed + Publish
→ 5C-4 Profile
→ 5C-5 联调 + Responsive + Build
```

---

# 七、5C-1：审计真实社区接口

修改前完整检查：

```text
BlogController
BlogServiceImpl
FollowController
FollowServiceImpl
UserController
ReservationOrderController
MvcConfig
LoginInterceptor
RefreshTokenInterceptor
```

以及前端：

```text
web/src/api/blog.js
web/src/api/follow.js
web/src/stores/user.js
web/src/router/index.js
web/src/components/business/BlogCard.vue
web/src/views/CommunityView.vue
web/src/views/ProfileView.vue
```

必须确认真实路径，不要按提示词猜。

重点确认：

```text
GET  /blog/hot
GET  /blog/{id}
GET  /blog/of/activity
POST /blog
PUT  /blog/like/{id}
GET  /blog/likes/{id}

PUT  /follow/{id}/{isFollow}
GET  /follow/or/not/{id}
GET  /follow/follow/common/{id}

GET  /blog/of/follow?lastId=&offset=
```

---

# 八、Blog 浏览权限

当前 Phase 5B 已知 `/blog/of/activity` 未登录时无法读取。

Phase 5C 推荐调整成：

```text
未登录：
可以浏览 Blog

登录：
才能点赞 / 关注 / 发布 / Following Feed
```

优先考虑开放纯读接口：

```text
GET /blog/hot
GET /blog/{id}
GET /blog/of/activity
```

可选开放：

```text
GET /blog/likes/{id}
```

禁止开放写接口和 Following Feed。

---

# 九、开放读接口时检查 UserHolder

检查 Blog 查询逻辑是否直接调用：

```text
UserHolder.getUser().getId()
```

如果未登录可能 NPE，允许最小修复：

```text
UserDTO user = UserHolder.getUser();

if (user != null) {
    // 判断 isLike
} else {
    isLike = false;
}
```

不要重写 BlogService。

调整后，Phase 5B Activity Detail 的“参加过的人怎么说”应该允许未登录浏览。

---

# 十、5C-2：Community 正式页面

路由：

```text
/community
```

页面结构：

```text
城市动态

看看大家最近去了哪里。

[热门动态] [关注]                  [发布体验]

Blog Feed
```

继续沿用 CityHub：
- 暖白
- 深墨
- 墨绿
- 橙棕

不要模仿微博 / Instagram / 小红书。

---

# 十一、Community 布局

Desktop：

```text
Feed 主列宽约 760~820px
居中
单列
```

不要做 Pinterest 瀑布流。

Mobile：

```text
BlogCard 100%
```

---

# 十二、Tab 与 URL

推荐：

```text
/community?tab=hot
/community?tab=following
```

刷新后保留 Tab 状态。

不要只用本地 state。

---

# 十三、热门动态

调用真实：

```text
GET /blog/hot?current=1
```

未登录也应可浏览。

使用正式 BlogCard。

普通分页或“加载更多”均可，不要求 Infinite Scroll。

---

# 十四、关注 Feed

调用：

```text
GET /blog/of/follow?lastId=&offset=
```

必须尊重后端真实：

```text
minTime
offset
```

滚动分页。

前端用：
- 加载更多，或
- 简单 Infinite Scroll

不要伪装成 page number 分页。

---

# 十五、关注 Tab 未登录

未登录切到 Following：

显示：

> 登录后查看你关注的人最近分享的活动体验。

按钮：

```text
登录
```

跳转并携带：

```text
redirect=/community?tab=following
```

---

# 十六、BlogCard 正式实现

Phase 5A 的 BlogCard 骨架升级为正式组件。

至少展示：

```text
头像
昵称
关联 Activity
发布时间
标题
正文摘要
图片
点赞按钮
点赞数
Follow 按钮
```

作者区建议：

```text
头像  昵称
参加了 · 城市青年创意市集          +关注
```

当前用户自己的 Blog 不显示关注按钮。

---

# 十七、Blog 与 Activity 联动

Blog 已有 `activityId`。

BlogCard 上：

```text
参加了 · Activity Title
```

必须可点击：

```text
/activities/:activityId
```

如果 Blog 响应没有 Activity title：

优先复用当前 Activity 数据或轻量 Activity Map。

避免每条 Blog 发一个 Activity 请求造成明显 N+1。

如果会明显复杂，允许只显示“相关活动”，但保留跳转。

不要为了一个标题大改后端 DTO。

---

# 十八、Blog 图片规则

优先：

```text
Blog.images
```

如果 Blog.images 为空且有 activityId：

```text
使用关联 Activity Cover
```

如果都没有：

```text
不显示图片区
```

不要让所有无图 Blog 都显示同一 fallback。

图片统一：
- 16:10 或 16:9
- object-fit: cover

第一版不做复杂九宫格。

---

# 十九、Blog 正文

Feed 中显示 2~4 行摘要，使用 line-clamp。

本阶段不要求单独 Blog Detail 页面。

---

# 二十、5C-3：Like

真实接口：

```text
PUT /blog/like/{id}
```

未登录点击 Like：

```text
→ /login?redirect=<当前 community URL>
```

已登录请求成功后局部修改：

```text
isLike
liked
```

不要求复杂 optimistic update。

---

# 二十一、Like 视觉

未点赞：
- 深墨弱化 / 灰色

已点赞：
- 橙棕 `#C87B52`

不要突然使用高饱和大红色。

点赞用户 API 是可选增强项，不作为阻断项。

---

# 二十二、Follow / Unfollow

真实接口：

```text
PUT /follow/{id}/{isFollow}
GET /follow/or/not/{id}
```

按钮：

```text
+ 关注
已关注
```

当前用户本人不显示。

未登录点击 Follow：

```text
跳登录
```

成功后局部切换，不要全量刷新 Feed。

共同关注 UI 本阶段不要求。

---

# 二十三、发布活动体验

Community 顶部提供：

```text
发布体验
```

未登录：
- 跳登录

已登录：
- 打开 ElDialog 或 ElDrawer

二选一即可。

---

# 二十四、发布表单

最低字段：

```text
关联活动
标题
正文
```

Activity 必须选择真实 Activity ID。

加载当前活动列表供选择。

提交：

```text
activityId
title
content
```

---

# 二十五、图片上传

本阶段不做：
- OSS
- MinIO
- Multipart Upload
- 图片裁剪
- 多图上传

优先方案：

```text
Blog.images 有简单 URL 就按现状处理
否则允许无图发布
展示时无 Blog 图片则使用关联 Activity Cover
```

---

# 二十六、发布校验

至少：
- Activity 必选
- Title 非空
- Content 非空
- 合理长度限制

不要做富文本编辑器。

---

# 二十七、发布成功

```text
POST /blog
→ 成功
→ 关闭 Dialog
→ ElMessage.success('发布成功')
→ 刷新当前 Feed
```

---

# 二十八、Feed fan-out 必须真实验证

至少验证：

```text
User A 关注 User B
→ User B 发布 Activity Blog
→ User A Following Feed
→ 能看到 B 的新 Blog
```

这是 Phase 5C 最重要的真实业务验证之一。

---

# 二十九、5C-4：Profile

路由：

```text
/profile
```

继续 requiresAuth。

Profile 只做轻量版，不做后台 Dashboard。

建议：

```text
用户资料 Header

头像
昵称

我的动态
```

如已有低成本能力，可增加关注相关内容，但不强制。

---

# 三十、Profile 用户资料

使用：

```text
userStore.user
GET /user/me
```

只展示真实 UserDTO 字段。

不要假设 bio / city / birthday / gender 存在。

手机号如果返回则脱敏：

```text
138****1234
```

如果不返回就不显示。

---

# 三十一、不做编辑资料

除非后端已真实存在可用编辑接口。

如果没有：
- 只读展示

不要新增资料编辑系统。

---

# 三十二、“我的动态”接口审计

检查 BlogController 是否已经存在：

```text
GET /blog/of/me
```

或：

```text
GET /blog/of/user/{id}
```

如果已有：
- 直接接入

如果没有：
允许新增最小只读查询接口，优先：

```text
GET /blog/of/user/{userId}?current=1
```

实现仅：

```text
WHERE user_id = ?
ORDER BY create_time DESC
```

并复用现有 Blog 回填逻辑。

不要扩展复杂用户主页。

---

# 三十三、我的动态 UI

复用同一个 BlogCard。

当前用户自己的 Blog：
- 不显示 Follow 按钮

Empty：

> 还没有发布过活动体验

副文案：

> 去发现活动，记录你的城市生活。

按钮：
- 发现活动

---

# 三十四、我的预约

只检查 ReservationOrderController 是否已有当前用户订单查询。

如果已有：
- 可轻量接入

如果没有：
- Phase 5C 不新增

“我的预约”不是本阶段阻断项。

---

# 三十五、5C-5：真实联调

必须至少验证以下浏览器流程。

### 1. 未登录浏览 Hot Blog

```text
/community?tab=hot
```

应能看到热门 Blog。

### 2. 未登录 Like

点击 Like：
- 跳登录
- 登录后回 community

### 3. Like / Unlike

登录后：
- 点赞成功
- liked +1
- 再次取消
- liked -1

### 4. Follow / Unfollow

A 关注 B：
- UI 变已关注

再次取消：
- UI 恢复

### 5. Following Feed

```text
A Follow B
→ B 发布 Blog
→ A Following Feed
→ 看到新 Blog
```

至少验证一次 minTime / offset 的下一批加载。

### 6. 发布 Blog

选择 Activity、填写 Title/Content、POST 成功。

确认数据库：
- activity_id 正确

### 7. Activity Detail 联动

发布后进入对应 `/activities/:id`：

Phase 5B 的 Blog Preview 能看到新 Blog。

### 8. Profile

登录用户进入 `/profile`：
- 头像 / 昵称正常
- 我的动态正常（若实现）

---

# 三十六、Responsive

至少验证：
- 1440px
- 768px
- 390px

检查：
- Community Feed
- BlogCard
- Tabs
- 发布 Dialog / Drawer
- Profile
- 无横向溢出

---

# 三十七、状态组件

继续复用 Phase 5A：

```text
PageLoading
EmptyState
ErrorState
```

不要重复造第二套。

Hot Empty：

```text
暂时还没有活动动态
```

Following Empty：

```text
关注一些有趣的人，他们的新动态会出现在这里。
```

Error：

```text
动态加载失败
```

按钮：
- 重新加载

---

# 三十八、组件建议

允许新增：

```text
CommunityTabs.vue
BlogFeed.vue
PublishBlogDialog.vue
FollowButton.vue
ProfileHeader.vue
```

BlogCard 尽量在：
- Hot
- Following
- Profile My Blogs
- Activity Detail Preview

复用同一套实现。

Activity Detail 可以通过 `compact/readOnly` props 做轻量模式。

不要复制第二个 BlogCard。

---

# 三十九、后端验证

Phase 4 已有 CommunityFlowIntegrationTest。

如果本阶段只开放 GET 白名单或新增 `/blog/of/user`：

执行现有相关测试或最小必要验证即可。

不需要再新增大量 Java 测试。

---

# 四十、Maven

如果有后端修改：

```bash
cd backend
mvn clean compile -DskipTests
```

必须 PASS。

---

# 四十一、前端验证

```bash
cd web
npm run dev
npm run build
```

必须 PASS。

Element Plus chunk > 500KB 的 warning 仍不作为阻断项。

---

# 四十二、唯一报告

本阶段只生成：

```text
F:\JavaProject\YJSHZ-main\docs\PHASE5C_REPORT.md
```

禁止生成多个 audit / verification 文件。

禁止创建：
`docs/refactor/phase5c/`

---

# 四十三、PHASE5C_REPORT.md 必须包含

1. Phase 5C 是否通过
2. Phase 5B 真实 Git commit
3. Blog GET 权限调整
4. 是否修复未登录 UserHolder null
5. Community Hot
6. Community Following
7. BlogCard
8. Activity Link
9. Like / Unlike
10. Follow / Unfollow
11. Publish Blog
12. Feed minTime / offset
13. Feed fan-out 真实验证
14. Activity Detail Blog Preview 联动
15. Profile
16. My Blogs
17. 后端最小修改
18. 1440 / 768 / 390
19. npm run dev
20. npm run build
21. Maven（若后端修改）
22. 已知限制
23. Git commit / hash / branch / push

---

# 四十四、Git 提交

完成后：

```bash
git status --short
git diff --check
```

确认不提交：
- node_modules
- dist
- .env.local
- 真实 token
- 临时验证码
- 测试日志
- 临时数据库文件

然后：

```bash
git add .
git diff --cached --check
git diff --cached --stat
git commit -m "feat: build CityHub community experience"
git push
```

禁止 force push。

---

# 四十五、Phase 5C 验收标准

核心尽量满足：

```text
未登录 Hot Blog：PASS
未登录 Activity Blog：PASS
Community Hot：PASS
Community Following：PASS
BlogCard：PASS
Activity Link：PASS
Like：PASS
Unlike：PASS
Follow：PASS
Unfollow：PASS
Publish Blog：PASS
Following Feed：PASS
Feed Pagination：PASS
Activity Detail Preview：PASS
Profile：PASS
My Blogs：PASS 或合理 N/A
Desktop 1440：PASS
Tablet 768：PASS
Mobile 390：PASS
npm run dev：PASS
npm run build：PASS
后端有修改时 Maven：PASS
Git commit + push：PASS
```

---

# 四十六、不作为阻断项

当前不要求：
- 评论
- 共同关注 UI
- 粉丝列表
- 关注列表
- 用户详情页
- 我的预约
- 编辑资料
- 图片上传
- 社区搜索
- 话题
- 标签
- 私信
- 通知
- AI
- Nginx 正式部署

---

# 四十七、下一阶段

Phase 5C 通过后：

> **CityHub Core Web 前端基本完成。**

下一阶段进入：

> **Phase 6：CityHub AI 顾问迁移与业务适配**

主要：
- 将 consultant 现有静态 AI 页面迁移到 Vue 主站 `/assistant`
- 保留 Streaming / AbortController
- 统一 CityHub Design System
- 将 AI 内部 Shop/Voucher/VoucherOrder 语义适配为 Activity/Ticket/Reservation
- 接入主站导航与登录态

Phase 5C 不提前实现。

---

# 四十八、最终回复格式

完成后只输出：

```text
Phase 5C CityHub 活动社区与用户基础页面完成。

1. Phase 5B Git：
2. Blog 浏览权限：
3. Community Hot：
4. Community Following：
5. BlogCard：
6. Activity Link：
7. Like：
8. Unlike：
9. Follow：
10. Unfollow：
11. Publish Blog：
12. Feed：
13. Feed Pagination：
14. Activity Detail Preview：
15. Profile：
16. My Blogs：
17. Desktop：
18. Tablet：
19. Mobile：
20. npm run dev：
21. npm run build：
22. 后端最小修改：
23. Maven：
24. Git commit：
25. commit hash：
26. push：
27. 已知限制：
28. 下一阶段：

唯一报告：
F:\JavaProject\YJSHZ-main\docs\PHASE5C_REPORT.md
```

---

# 四十九、最终原则

Phase 5C 的目标不是打造复杂社交平台。

真正目标：

> **让 CityHub 用户可以浏览活动体验、点赞、关注、查看关注 Feed、发布自己的活动体验，并拥有一个轻量个人页面。**

重点是把 Phase 4 已经完成的：

```text
Redis ZSet Like
Redis Set Follow
Feed ZSet
Blog.activityId
```

真实展示到前端。

完成 Phase 5C 后，CityHub Core Web 前端应基本冻结，后续重点转向 AI 顾问和最终工程收尾。
