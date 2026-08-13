# Phase 5B CityHub 活动发现与预约前端报告

## 结论

**通过。** 本阶段完成 CityHub 的“发现活动 → 查看详情 → 预约”Web 闭环，并在独立的临时 MySQL、Redis、CityHub Core 和 Vite 环境完成真实联调。未修改 README、Blog/Follow 的既有业务实现、秒杀/Lua/Redisson/BlockingQueue 链路或 AI。

## Git 基线

- 分支：`main`
- Phase 5A 功能提交：`2cc2ee1 feat: scaffold CityHub web foundation`
- Phase 5A 最后文档提交：`37eb1a8 docs: record Phase5A push result`
- 开始时 `main` 与 `origin/main` 一致；开始时唯一未跟踪项是本阶段用户提供的提示词。

## 图片资产与映射

- 扫描到 14 个源 PNG：12 张活动插画、1 张 Hero、1 张 fallback；活动图均为 `1586 × 992`，Hero 为 `1672 × 941`。
- 保留原 PNG，并生成 14 个正式使用的 WebP（约 60–393 KB/张）；ActivityCard 与详情使用 lazy loading，Hero 不延迟加载。
- 在 `web/src/config/activityImages.js` 集中按 Activity ID 映射 1–12，独立导出 Hero 与 fallback；图片加载错误会回退至 fallback，不在页面散落 ID 判断。
- 映射主题：创意市集、爵士音乐会、摄影展、陶艺、文化讲座、夜跑、亲子自然、剧场、露天电影、旧城漫步、咖啡市集、花艺工作坊。

## 数据与后端最小改动

- `cityhub_schema.sql` 从 5 条扩充至 **12 条** Activity Seed，并为 12 条活动提供 Ticket/SeckillTicket Seed；没有引入 Venue、ActivitySession 或新业务字段。
- 新增最小只读接口：`GET /activity/page?current=1&size=5`。它仅执行 Activity 的 MySQL 分页查询，按 score/sold 排序；`size` 限制为 1–10，返回现有 `Result.data` 与 `Result.total`，用于“全部活动”分页和首页取满 12 条推荐数据。
- 临时开发库重新导入后实测：`tb_activity=12`、`tb_ticket=12`、`tb_seckill_ticket=12`。导入的中文数据经 utf8mb4 字节检查确认无损；PowerShell 显示问号是控制台编码显示，不是数据库数据损坏。

## 前端实现

- 首页：正式 Hero（搜索跳转）、真实分类、按 score/sold 取数的“本周热门”、不同活动的“城市精选”和“周末灵感”、轻量社区 CTA。
- `ActivityCard`：统一封面、分类、时间、地点、价格与可选热门标记；16:10 图片、hover 位移/缩放；1440px 三列、768px 两列、390px 一列。
- 活动列表：URL 驱动的关键词/分类筛选、全部活动分页、卡片 Loading、Empty、Error 状态；搜索/分类复用既有 API，全部分页使用新增最小 API。
- 活动详情：真实 Activity、分类和 Ticket 数据，Hero、Meta、活动信息、sticky Ticket 面板；无 Ticket 和无 Blog 使用统一 EmptyState。
- 预约：未登录跳 `/login?redirect=/activities/:id`；登录后回到原详情页；ElDialog 确认；成功后按钮变为“已预约”；重复预约与库存不足分别映射为面向用户的中文提示，且抑制重复全局错误弹窗。
- Blog Preview：登录用户读取 `/blog/of/activity` 后只读展示最多 3 条；未登录或无数据不会阻断详情页，展示社区引导。原因是现有 Blog API 受登录 Token 保护。
- Header：Desktop 主导航与 Mobile ElDrawer；页面标题和语言已从 Vite 默认值改为 CityHub。

## 真实联调与验证

环境：Docker `cityhub-phase3b-mysql`（宿主 3307）、Docker Redis（6379）、CityHub Core（8081）、Vite（5173，`/api` 代理）。

| 项目 | 结果 | 依据 |
| --- | --- | --- |
| ActivityCategory / Activity / Ticket | PASS | `/api/activity-category/list`、`/api/activity/page`、`/api/activity/1`、`/api/ticket/list/1` 返回成功 |
| Blog Preview | PASS | 登录后可调用 `/api/blog/of/activity`；未登录降级为空状态，不阻断详情 |
| 未登录预约 | PASS | 浏览器点击“预约参加”跳至 `/login?redirect=/activities/1` |
| 登录 redirect | PASS | 临时验证码登录后回到 `/activities/1`，并显示 Ticket |
| 正常预约 | PASS | 浏览器真实提交 `/reservation/seckill/1`，显示“预约成功”；异步落库后 ticket 1 MySQL/Redis 库存均从 100 变为 97（本轮及先前临时验证共 3 单） |
| 重复预约 | PASS | 刷新详情再次提交，显示“你已经预约过这个活动” |
| 库存不足 | PASS | 临时将 Redis `seckill:stock:12` 设为 0 后提交，显示“很遗憾，本场活动名额已满”；验证后恢复为 20 |
| 1440px | PASS | 首页卡片 3 列，无横向溢出 |
| 768px | PASS | 首页卡片 2 列，无横向溢出 |
| 390px | PASS | 首页卡片 1 列，无横向溢出；Drawer 显示首页、发现活动、活动社区、登录 |
| `npm run dev` | PASS | Vite 在 5173 运行，浏览器实际访问 |
| `npm run build` | PASS | Vite production build 成功 |
| `mvn clean compile -DskipTests` | PASS | backend 全 reactor（parent/core/AI）编译成功 |
| `mvn -pl core compile -DskipTests` | PASS | 分页接口改动后的 Core 再次编译成功 |

## 已知限制

1. 现有搜索和分类 API 不返回 total，因此筛选结果按后端返回的一页展示，不额外伪造分页；“全部活动”才有真实 total 和 Element Plus 分页。
2. 首页的三组内容是确定性 score/sold 排序切片，不宣称为个性化推荐。
3. 现有 Activity 没有 description，详情介绍仅由真实活动字段和通用参与说明组成。
4. 当前阶段不恢复刷新后的“已预约”状态、不实现完整社区、个人中心、支付或 AI。
5. `npm run build` 仅保留 Element Plus 主 chunk 大于 500 KB 的非阻断警告；未为本阶段引入复杂拆包策略。

## Git 交付

- 功能提交：`feat: build CityHub activity discovery experience`（提交 hash 与 push 结果在最终交付时以实际 Git 输出记录）。
- 将执行 `git diff --check`、暂存检查和 `git diff --cached --check`，仅提交源码、WebP、提示词与本报告；不会提交 node_modules、dist、.env.local、token 或测试脚本。
