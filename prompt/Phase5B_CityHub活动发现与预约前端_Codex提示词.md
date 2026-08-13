# Codex 执行任务：Phase 5B CityHub 首页 + Activity 列表 + Activity 详情 + Ticket / 预约 UI

## 一、阶段背景
项目：**CityHub - 城市活动发现与预约平台**

当前已完成：
- Phase 1：工程治理
- Phase 2：CityHub 工程身份规范化
- Phase 3A-R：核心领域轻量迁移
- Phase 3B：Activity + Redis 缓存真实验证
- Phase 3C：限量预约 / 秒杀链路真实验证
- Phase 4：活动体验社区轻量适配
- Phase 5A：Vue 3 Web 工程骨架、Router、Axios、Pinia、登录、Design System

当前前端源码：
`F:\JavaProject\YJSHZ-main\web`

当前图片目录：
`F:\JavaProject\YJSHZ-main\web\src\assets\images`

用户已经准备一套统一风格的城市文化活动插画，主题包括：
- 城市青年创意市集
- 夏日爵士音乐会
- 当代摄影艺术展
- 周末陶艺体验课
- 城市文化讲座
- 城市夜跑活动
- 亲子自然工作坊
- 独立剧场演出
- 露天电影夜
- 旧城建筑漫步
- 周末咖啡文化市集
- 花艺工作坊

另有可能存在 `hero` 与 `fallback` 图片。

**必须扫描实际目录，以真实文件为准，不要凭提示词假设文件一定存在或文件名完全一致。**

---

## 二、本阶段定位
Phase 5B 是 CityHub 第一轮正式业务前端与视觉包装阶段。

目标形成完整用户闭环：

```text
进入首页
→ Hero + 活动分类 + 活动推荐
→ 搜索 / 分类筛选
→ 活动列表
→ Activity Detail
→ Ticket
→ 预约
→ 预约成功 / 重复预约 / 名额已满
```

Phase 5B 完成后，即使暂时不进入社区和 AI，也应能完成一个完整的“发现活动 → 查看活动 → 预约活动”体验。

---

## 三、视觉方向
继续沿用 Phase 5A Design System：

- 暖白背景 `#F7F6F2`
- 深墨文字 `#20211F`
- 墨绿主操作 `#426B5A`
- 橙棕强调 `#C87B52`
- 低饱和城市文化活动插画
- 克制圆角
- 轻阴影
- 较高留白

禁止改成：
- 点评红
- 商城红黄
- Element Plus 默认蓝
- 后台管理系统风格
- 科技蓝紫渐变
- 赛博朋克

---

## 四、视觉投入优先级
建议：
- 首页 / ActivityCard：35%
- Activity Detail / Ticket / 预约：35%
- Activity List：20%
- 状态 / Responsive：10%

首页和 Activity Detail 是最重要的两个页面。

---

## 五、本阶段禁止事项
禁止：
- 完整社区 Feed 开发
- Blog 点赞/关注完整 UI
- 复杂个人中心
- AI 顾问迁移
- Redis Stream
- MQ
- 后端缓存重构
- 秒杀重构
- 订单状态机
- 支付 / 退款 / 核销
- 推荐算法
- ElasticSearch
- 复杂搜索联想
- 复杂动画
- PWA / SSR / Nuxt
- README 重写

只有当前前端真实联调出现后端阻断时，才允许最小后端修复。

---

## 六、Git 基线
开始前执行：

```bash
git status --short
git log --oneline -n 10
git branch --show-current
git remote -v
```

要求：
- `main`
- working tree clean
- `main == origin/main`

先确认 Phase 5A 最新真实 commit hash。若报告中的 hash 与 `git log` 不一致，以真实 `git log --oneline` 为准。

---

## 七、Phase 5B 内部执行顺序
本阶段内部按四步执行：

```text
5B-1 图片与数据层
→ 5B-2 首页
→ 5B-3 Activity List
→ 5B-4 Activity Detail + Ticket + Reservation
```

这是同一个阶段，不要拆成多个阶段报告或多个 Git 分支。

---

## 八、5B-1：扫描图片资产
先扫描：
`web/src/assets/images`

记录所有：
- png
- jpg/jpeg
- webp
- svg

确认：
- 实际文件名
- 尺寸
- 比例
- 文件大小
- 12 个 Activity 封面是否齐全
- hero 是否存在
- fallback 是否存在

若有明显拼写错误，例如 `concet`，允许安全重命名为 `concert` 并同步所有引用。

---

## 九、图片命名规范
如当前命名不统一，可整理为类似：
- `creative-market`
- `concert`
- `art-exhibition`
- `pottery-class`
- `cultural-lecture`
- `night-running`
- `nature-workshop`
- `theatrical-performance`
- `movie-night`
- `strolling-through`
- `coffee-market`
- `floral-workshop`
- `hero`
- `fallback`

不要使用随机长文件名或历史 ChatGPT 导出文件名。

---

## 十、图片转换 WebP
如果当前主要是 PNG，允许在本阶段将 Web 实际使用图片转换为 WebP。

建议：
- Activity：保持原比例，目标约 `1600×1000`，quality 82~88
- Hero：建议 `1920×1080` 或保留足够清晰的原始尺寸，quality 约 85

尽量控制：
- Activity 单图 < 400 KB
- Hero < 700 KB

不要为了极限压缩明显损伤画质。

若无法确认原 PNG 是否已被用户另存备份，不要冒险删除唯一原始资产。Web 正式引用优先使用 WebP。

---

## 十一、建立统一图片映射
禁止在多个 Vue 页面散落 `if activity.id === ...`。

建立一个统一文件，例如：
`src/config/activityImages.js`

提供：
`getActivityImage(activity)`

优先按 `activity.id` 映射，不优先按 title。

任何 Activity 找不到图片时返回 fallback。图片加载失败也必须降级到 fallback，不能出现 broken image icon。

Hero 与 fallback 独立管理，不进入 Activity ID 映射。

---

## 十二、审计真实 Activity API
正式实现首页前，读取当前 Core 的 Activity Controller。

重点确认是否存在“获取全部 Activity 分页”的真实接口。

已知已有：
- `GET /activity-category/list`
- `GET /activity/{id}`
- `GET /activity/of/category`
- `GET /activity/of/name`

若还有 `/activity/page`、`/activity/list` 等接口，以真实代码为准。

若没有“全部活动分页”能力，优先判断 `/activity/of/category` 是否可在 categoryId 为空时查询全部。

如无法满足前端“全部活动”，允许新增一个最小 Core 查询 API，例如：
`GET /activity/page?current=1`

只做 MySQL Activity 分页查询，禁止因此修改缓存架构或领域模型。报告必须说明为什么新增。

---

## 十三、补充 Seed Activity
若数据库实际仍只有 5 个 Activity，允许在：
`backend/core/src/main/resources/db/cityhub_schema.sql`

补充到 10~12 条 Seed Activity，与图片主题对应：

1. 城市青年创意市集
2. 夏日爵士音乐会
3. 当代摄影艺术展
4. 周末陶艺体验课
5. 城市文化讲座
6. 城市夜跑活动
7. 亲子自然工作坊
8. 独立剧场演出
9. 露天电影夜
10. 旧城建筑漫步
11. 周末咖啡文化市集
12. 花艺工作坊

必须结合当前真实字段填写，不新增 `startTime/endTime/venueId/sessionId`。

沿用当前真实字段，例如：
- categoryId
- title
- images
- area
- address
- x/y
- avgPrice
- sold
- comments
- score
- openHours

不要把 Windows 本地路径或 `src/assets` 物理路径写入数据库。

---

## 十四、Ticket Seed
不要求 12 个 Activity 全部配置限量 Ticket。

至少保证用于详情页和预约演示的几条 Activity，`GET /ticket/list/{activityId}` 能返回真实数据。

---

# 十五、5B-2：首页正式开发
路由：
`/`

从 Phase 5A 骨架升级为正式首页。

结构：

```text
AppHeader
→ Hero
→ Activity Category
→ 本周热门
→ 城市精选
→ 周末灵感
→ Community CTA
→ AppFooter
```

---

## 十六、Hero
使用当前 hero 图片。

禁止轮播图、Swiper、自动 Banner。

Desktop 建议：
- 左侧约 55%：标题、描述、搜索
- 右侧约 45%：Hero 插画
- 大圆角容器
- 暖白 / 极浅米背景

文案：

主标题：
**发现城市，遇见有趣。**

副标题：
**展览、音乐、市集、讲座与周末体验，都在 CityHub。**

搜索 placeholder：
`搜索活动、展览、演出、市集...`

搜索真实工作：
输入 keyword 后 Enter/点击搜索，跳转：
`/activities?keyword=xxx`

不要在首页内实现复杂搜索结果。

---

## 十七、Activity Category
真实调用：
`GET /activity-category/list`

展示真实分类，例如：
`全部 / 展览 / 音乐 / 市集 / 演出 / 讲座 / 手作 / 体育 / 亲子`

实际数量以后端返回为准。

视觉：
- 未激活：白底、浅边框、深墨文字
- 激活：墨绿背景、暖白文字

不要 8 种随机高饱和颜色。

---

## 十八、本周热门
展示约 4 个 ActivityCard。

优先使用真实字段中的 `sold/score/comments` 选择一个简单排序依据；若数据不足，可基于 Seed 做简单排序，但不要包装成个性化推荐。

报告据实说明。

---

## 十九、城市精选
展示约 4 个与本周热门不同的 Activity。

不要重复同一条 Activity 伪装多个活动。

---

## 二十、周末灵感
展示剩余 3~4 个活动，用于充分使用扩展活动图片。

文案用“周末灵感”，禁止写“猜你喜欢 / 为你推荐 / 个性化推荐”，因为当前无推荐算法。

---

## 二十一、首页跳转
每个 ActivityCard 点击：
`/activities/:id`

每个 Section Header 提供：
`查看全部活动 →`
跳：
`/activities`

首页底部可增加轻量 Community CTA：
- 和同好一起发现城市
- 分享你的活动体验
- 按钮：去社区

本阶段不加载真实 Feed。

---

# 二十二、ActivityCard 正式组件
Phase 5A 骨架升级为全站正式组件。

核心 props：
`activity`

内部统一处理：
- image
- category
- title
- openHours
- area/address
- avgPrice

只展示：
- 封面
- Category Tag
- 标题
- 活动时间
- 区域 / 简短地点
- 参考价格
- 可选“热门/限量”Tag

不要展示一堆 score/comments/sold 等统计，避免回到点评卡片风格。

图片统一：
```css
aspect-ratio: 16 / 10;
object-fit: cover;
```

Hover：
- card translateY(-3px)
- image scale(1.03)
- transition 200~250ms

Desktop 3 列、Tablet 2 列、Mobile 1 列。即使大屏也不强制 4 列。

---

# 二十三、5B-3：Activity List
路由：
`/activities`

正式结构：
- 标题
- 副标题
- 搜索框
- Category Filter
- 结果统计
- Activity Grid
- Pagination
- Loading / Empty / Error

标题：
**发现活动**

副标题：
**在城市里找到你的下一站。**

---

## 二十四、Activity List 搜索
读取：
`route.query.keyword`

如果 keyword 存在，调用真实 `GET /activity/of/name` 或等价搜索 API。

---

## 二十五、Category Filter
读取：
`route.query.categoryId`

选择分类后更新 URL query，再重新加载列表。

不要只保存在组件本地 state，确保刷新后状态可恢复。

“全部”调用真实全部活动能力。

---

## 二十六、分页
第一版使用 Element Plus Pagination，与后端 `current` 参数对齐。

不做 Infinite Scroll。

---

## 二十七、Loading / Empty / Error
Loading：
使用 3~6 个 ActivityCard Skeleton，不要整页只放大 spinner。

Empty：
- 没找到相关活动
- 换个关键词，或者看看其他分类。
- 按钮：查看全部活动

Error：
- 活动加载失败
- 按钮：重新加载

不要展示 AxiosError / Stack Trace。

---

# 二十八、5B-4：Activity Detail
路由：
`/activities/:id`

必须真实调用：
- `GET /activity/{id}`
- `GET /ticket/list/{activityId}`

并使用 `getActivityImage(activity)`。

Desktop 结构建议：

```text
Breadcrumb
→ Hero Image
→ Category Tag
→ Activity Title
→ Basic Meta
→ 2-column content

左 8/12：
- 活动介绍
- 活动信息
- 参加过的人怎么说

右 4/12：
- Sticky Ticket Panel
```

---

## 二十九、Detail Hero
使用同一 Activity 封面图。

建议：
```css
width: 100%;
aspect-ratio: 16 / 7;
object-fit: cover;
border-radius: 20px;
```

不要把巨大标题压在图上。

---

## 三十、详情信息
展示：
- Category Tag
- Activity Title
- 时间
- 地点
- 参考价格

时间优先使用当前真实 `openHours`。

不要假设不存在的 `startTime/endTime`。

地点优先 `area + address`，避免出现 undefined/null。

价格使用 `avgPrice`；对于 0/null 结合实际语义显示“免费”或合理降级。

---

## 三十一、活动介绍
若当前 Activity 没有完整 description 字段，不要为页面大改后端 Content 系统。

允许用现有真实字段组织“活动信息”区域。

不要硬造不存在的长篇详情数据模型。

---

# 三十二、TicketCard 正式组件
展示：
- 标题
- 副标题 / 规则摘要
- 价格
- 类型
- 库存（若真实返回）
- 限量标签
- 预约按钮

视觉：
- 白底
- 浅边框
- 圆角
- 限量 Tag 用橙棕
- CTA 用墨绿

禁止做成红黄优惠券齿孔电商风。

若一个 Activity 有多个 Ticket，按列表展示。

没有 Ticket：
`当前活动暂无可预约名额`

---

# 三十三、预约交互
点击预约前检查 `userStore.isLoggedIn`。

未登录：
跳转到：
`/login?redirect=<当前详情页>`

不要只弹“请登录”。

已登录：
使用 ElDialog：

标题：
`确认预约`

内容：
- Activity Title
- Ticket Title
- 价格

按钮：
- 取消
- 确认预约

调用真实：
`POST /reservation/seckill/{ticketId}`

---

## 三十四、预约反馈
成功：
`ElMessage.success('预约成功')`

若接口返回 orderId，可简要展示。

当前 session 中将按钮置为：
`已预约`
并 disabled。

不要求刷新后恢复已预约状态。

重复预约错误映射为：
`你已经预约过这个活动`

库存不足映射为：
`很遗憾，本场活动名额已满`

不要向用户展示 Lua 返回码或生硬内部错误。

---

# 三十五、Activity Blog Preview
Phase 4 已实现：
`GET /blog/of/activity`

详情页底部增加：
**参加过的人怎么说**

只读展示 2~3 条 Blog。

只展示：
- 头像
- 用户
- 标题
- 简短内容
- 图片（若有）
- liked 数

本阶段不要在详情页完整实现点赞、关注、Feed、评论。

无数据：
- 还没有人分享体验
- 成为第一个分享活动体验的人。
- 可提供“去社区”按钮

---

# 三十六、Responsive Header
Phase 5A AppHeader 在本阶段正式完善。

Desktop：
- CityHub
- 发现活动
- 活动社区
- 登录 / 用户

Mobile：
- CityHub
- Menu Icon

点击使用 ElDrawer：
- 发现活动
- 活动社区
- 个人中心 / 登录

Phase 5B 暂不加入 AI 顾问导航。

---

# 三十七、响应式要求
至少真实检查：
- 1440px Desktop
- 390px Mobile
- Tablet 768~1023 基本正常

Desktop：
- Hero 左右布局
- Activity 3 列
- Detail 8:4
- Ticket sticky

Mobile：
- Hero 上下布局
- Activity 1 列
- Category 可横向滚动或换行
- Detail 单列
- Ticket 不 sticky
- 无横向溢出
- Header Drawer 可用

Tablet：
- Activity 2 列
- Detail 可转单列

---

# 三十八、性能
ActivityCard 图片：
`loading="lazy"`

Hero：
不要 lazy。

Element Plus chunk > 500k 的 warning 不作为阻断项。低风险可优化，若会增加大量复杂度则暂不处理。

---

# 三十九、真实联调
必须在真实 Core 环境至少验证：
- `GET /api/activity-category/list`
- `GET /api/activity/{id}`
- Activity List / Search
- `GET /api/ticket/list/{activityId}`
- `POST /api/reservation/seckill/{ticketId}`
- `GET /api/blog/of/activity`

至少验证一次完整预约链：

```text
未登录进入详情
→ 点击预约
→ 跳登录
→ 登录成功
→ redirect 回详情
→ 再次预约
→ 成功
```

还要验证：
- 重复预约友好提示
- 库存为 0 时“名额已满”
- 不存在 Activity 显示 Error/NotFound
- 无图片映射时显示 fallback

---

# 四十、测试数据安全
如果补充 `cityhub_schema.sql` Seed，必须在独立开发数据库重新导入和验证。

禁止覆盖用户无法确认安全的本机数据库。

优先继续使用此前已验证的临时 MySQL / Redis 环境。

---

# 四十一、复用 Phase 5A 组件
必须实际复用：
- PageLoading
- EmptyState
- ErrorState
- SectionHeader
- AppHeader
- AppFooter

不要为同一种状态再造第二套组件。

页面不要变成一个 800 行 HomeView.vue。

建议适度拆：
- HomeHero
- CategoryFilter
- ActivitySection
- ActivityCard
- TicketCard
- ActivityBlogPreview

但不要过度组件化。

---

# 四十二、CSS
继续使用：
- tokens.css
- global.css
- component scoped CSS

禁止引入：
- Tailwind
- Sass/Less
- CSS-in-JS

---

# 四十三、唯一报告
本阶段只生成一个报告：

`F:\JavaProject\YJSHZ-main\docs\PHASE5B_REPORT.md`

禁止生成多个 audit / verification 文件，也不要创建 `docs/refactor/phase5b/`。

---

# 四十四、报告必须包含
1. Phase 5B 是否通过
2. Git 基线与真实 Phase 5A commit
3. 图片资产数量、命名、尺寸、是否转 WebP
4. hero / fallback / Activity 图片映射
5. 是否补充 Activity Seed，最终 Activity 数量
6. 是否新增全部 Activity 查询 API
7. 首页 Hero / Category / 本周热门 / 城市精选 / 周末灵感
8. ActivityCard 实现
9. Activity List 搜索 / 分类 / 分页 / Loading / Empty / Error
10. Activity Detail Hero / Meta / Ticket / Blog Preview
11. 未登录预约跳转
12. 登录 redirect
13. 正常预约
14. 重复预约
15. 库存不足
16. 1440px / 390px / Tablet 验证
17. 真实 API 联调结果
18. `npm run dev`
19. `npm run build`
20. 后端最小修改
21. 已知限制
22. Git commit / hash / branch / push

---

# 四十五、Git 检查
完成后：

```bash
git status --short
git diff --check
```

检查不要提交：
- node_modules
- dist
- .env.local
- 真实 token
- 临时测试文件
- logs

然后：

```bash
git add .
git diff --cached --check
git diff --cached --stat
```

通过后提交：

```bash
git commit -m "feat: build CityHub activity discovery experience"
```

然后：

```bash
git push
```

禁止 force push。

---

# 四十六、Phase 5B 最终验收
必须尽量满足：

- Hero：PASS
- 12 张 Activity 图片映射：PASS
- fallback：PASS
- ActivityCategory：PASS
- 首页真实 Activity：PASS
- ActivityCard：PASS
- Search：PASS
- Category Filter：PASS
- Pagination：PASS
- Activity Detail：PASS
- Ticket：PASS
- 未登录预约跳登录：PASS
- 登录 redirect：PASS
- 正常预约：PASS
- 重复预约：PASS
- 库存不足：PASS
- Blog Preview：PASS
- Loading：PASS
- Empty：PASS
- Error：PASS
- Desktop 1440：PASS
- Mobile 390：PASS
- npm run dev：PASS
- npm run build：PASS
- Git commit + push：PASS

---

# 四十七、不作为 Phase 5B 阻断项
当前不要求：
- 完整社区 Feed
- Blog 点赞/关注 UI
- 完整个人中心
- 我的预约列表
- 刷新后恢复“已预约”按钮
- AI 顾问
- Nginx 正式部署
- 推荐算法
- 搜索联想
- 地图 / GEO 前端
- 支付
- 订单详情

---

# 四十八、下一阶段
Phase 5B 通过后建议进入：

**Phase 5C：活动社区 + 用户基础页面**

主要：
- Community
- Hot Blog
- Follow Feed
- Like
- Follow
- Activity Blog
- Profile 基础资料
- 我的动态

Phase 5B 不提前完整实现。

---

# 四十九、最终回复格式
完成后只输出：

```text
Phase 5B CityHub 活动发现与预约前端完成。

1. Phase 5A Git：
2. 图片资产：
3. WebP：
4. Hero：
5. fallback：
6. Activity Seed：
7. Activity All API：
8. 首页：
9. Activity Category：
10. ActivityCard：
11. Activity List：
12. Search：
13. Pagination：
14. Activity Detail：
15. Ticket：
16. Blog Preview：
17. 未登录预约：
18. 登录 redirect：
19. 正常预约：
20. 重复预约：
21. 库存不足：
22. Desktop：
23. Mobile：
24. npm run dev：
25. npm run build：
26. 后端最小修改：
27. Git commit：
28. commit hash：
29. push：
30. 下一阶段：

唯一报告：
F:\JavaProject\YJSHZ-main\docs\PHASE5B_REPORT.md
```

---

# 五十、最终原则
Phase 5B 的目标不是把所有页面都做完。

真正目标：

> **利用已经准备好的统一城市文化活动插画，把 CityHub 最核心的“发现活动 → 查看活动 → 预约活动”做成一个真正有产品完成度的 Web 体验。**

优先：首页视觉、ActivityCard、Activity Detail、Ticket / Reservation。

不要继续堆后端技术，不要过度扩展页面。
