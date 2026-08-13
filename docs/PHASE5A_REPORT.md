# Phase 5A CityHub Web 基础工程报告

## 阶段结论

Phase 5A 通过。仓库根目录新增独立的 Vue 3 / Vite 工程 `web/`，已建立 Router、Axios、Pinia、Element Plus、CityHub Design System、登录恢复与基础页面骨架。没有修改后端业务、缓存、秒杀、Lua、Redisson、BlockingQueue、Blog/Follow 或 AI 后端。

## 新前端工程

- 目录：`web/`，与既有 `frontend/` Nginx 运行目录明确分离。
- 本机环境：Node `v24.14.0`、npm `11.9.0`，使用 npm 生成并安装依赖。
- 依赖：Vue `3.5.41`、Vite `8.2.1`、Vue Router `4.6.4`、Pinia `4.0.3`、Axios `1.19.0`、Element Plus `2.14.4`、`@element-plus/icons-vue` `2.3.2`。
- 提交 `package.json` 与 `package-lock.json`。根 `.gitignore` 新增 `web/node_modules/`、`web/dist/`、`web/.env.local`；未纳入依赖目录、构建产物或本地密钥。
- `.env.development` 与 `.env.production` 均只包含非敏感配置 `VITE_API_BASE=/api`。

## Router

| 路由 | 页面 | 状态 |
| --- | --- | --- |
| `/` | `HomeView` | CityHub 品牌视觉骨架，未接入完整首页业务。 |
| `/login` | `LoginView` | 正式验证码登录页。 |
| `/activities` | `ActivityListView` | 高质量骨架页。 |
| `/activities/:id` | `ActivityDetailView` | 高质量骨架页。 |
| `/community` | `CommunityView` | 高质量骨架页。 |
| `/profile` | `ProfileView` | 需要登录，骨架页。 |
| `/:pathMatch(.*)*` | `NotFoundView` | 品牌化 404 页。 |

路由守卫通过 `meta.requiresAuth` 保护 `/profile`，无 Token 时跳转 `/login?redirect=...`。

## Axios、Token 与 Pinia

- `vite.config.js` 的开发代理将 `/api/*` 转发至 `http://127.0.0.1:8081/*`，正确去除 `/api` 前缀；与后续 Nginx 同源反代一致。
- Axios 使用 `VITE_API_BASE=/api`、10 秒超时、请求/响应拦截器，并严格写入真实后端 Header：`authorization: <token>`，没有 `Bearer`。
- 统一解包 Core 的 `{ success, errorMsg, data, total }`：成功返回 `data`，业务失败统一 `ElMessage.error` 后 reject。
- HTTP 401 会统一 `clearAuth()`；不在登录页时跳转 `/login` 并保留来源路径，避免重复跳转。
- `src/stores/user.js` 管理 `token`、`user`、`isLoggedIn`，并提供 `setToken()`、`clearAuth()`、`fetchCurrentUser()`、`restoreSession()`、`logout()`。
- Token 持久化 Key 是 `cityhub_token`。应用启动不信任本地 Token 字符串，必须经 `/user/me` 成功后才恢复用户。
- 已建立 `user`、`activity`、`ticket`、`reservation`、`blog`、`follow` API 模块。Ticket/预约/社区只封装，不提前开发完整页面。

## 真实登录与 API 验证

测试环境为独立 Docker MySQL/Redis 与 CityHub Core `8081`，Vite `5173`。本地验证时通过 Redis 临时写入验证码；验证码未写入前端源码、环境文件或 Git。

| 验证项 | 真实行为 | 结果 |
| --- | --- | --- |
| ActivityCategory Proxy | `GET /api/activity-category/list` | HTTP 200，返回 8 个分类。 |
| Activity Detail Proxy | `GET /api/activity/1` | HTTP 200，返回 Activity 1。 |
| 未登录鉴权 | `GET /api/user/me` 无 Header | HTTP 401。 |
| 验证码 UI | 浏览器输入手机号并点击“获取验证码” | `POST /api/user/code` 成功，按钮进入 60 秒禁用倒计时。 |
| 浏览器登录 | 提交手机号/验证码至 `/api/user/login` | 成功保存 `cityhub_token` 并回到首页。 |
| 登录恢复 | 自动注入 authorization 后请求 `/api/user/me` | HTTP 200，Pinia 恢复用户，Header 显示昵称。 |
| Token 失效 | 浏览器写入无效 Token 后刷新 | Core 401；前端清除 Token 并跳转 `/login?redirect=/`。 |

## Design System

设计方向是城市文化活动发现，不使用点评红色或后台蓝色：暖白底、深墨文字、墨绿行动色、橙棕活动标记。首页用 CSS 城市地图作为克制的签名元素，未下载或提交版权不明图片。

| 内容 | 实现 |
| --- | --- |
| 颜色 | `#F7F6F2` 暖白、`#20211F` 深墨、`#426B5A` 墨绿、`#C87B52` 橙棕及语义色。 |
| 字体 | `Inter, PingFang SC, Microsoft YaHei, system-ui, sans-serif`；大标题克制使用系统衬线回退。 |
| 圆角 | 8 / 12 / 16 / 20 px。 |
| 间距 | 4 / 8 / 12 / 16 / 24 / 32 / 48 / 64 px。 |
| 容器/断点 | 最大 1200 px；Mobile `<768`、Tablet `768–1023`、Desktop `≥1024`。 |
| Element Plus | 覆盖 Button、Input、Dialog、Message、Skeleton、Empty 主色与视觉令牌。 |

## 基础组件与页面骨架

- 通用：`AppHeader`、`AppFooter`、`PageContainer`、`PageLoading`、`EmptyState`、`ErrorState`、`SectionHeader`。
- 业务骨架：`ActivityCard`、`TicketCard`、`BlogCard`，均有统一 Props 和基础视觉，但不提前承载完整业务。
- 页面：Home 品牌 Hero、搜索视觉占位与三张 demo 活动卡；Activities、ActivityDetail、Community、Profile 使用一致说明与空状态；404 可返回首页。
- Login 是正式页面，具备手机号校验、发送验证码、倒计时、登录、用户恢复和 redirect。

## Dev、Build 与响应式检查

- `cd web && npm run dev -- --host 127.0.0.1`：PASS，监听 `5173`。
- `cd web && npm run build`：PASS，生成被 Git 忽略的 `web/dist/`。
- Build 唯一提示是 Element Plus 带来的单个 chunk 大于 500 kB；不阻断 Phase 5A，后续按页面业务扩展时可按需引入/拆分。
- Playwright 检查：1440px 首页、登录、活动骨架页无 console error；390px 首页、登录页 `scrollWidth === clientWidth`，Header、Hero、表单和按钮无横向溢出或重叠。
- 有效登录后 Header 显示用户；失效 Token 测试中预期出现 401 网络记录，随后成功清理并跳转登录页。

## 后端修改与已知问题

- 后端修改：无。Vite Proxy 已解决开发联调的跨域/路径问题。
- 未完成完整首页数据化、活动列表、活动详情、Ticket/预约 UI、社区 Feed、个人中心、AI 迁移或 Nginx 正式部署，均留给后续阶段。
- `frontend/conf/nginx.conf` 仍指向缺失的 `frontend/html/cityhub`；本阶段只建立 `web/` 源码，不复制 `dist`，正式部署留待后续收尾。

## Git

- 功能提交信息：`feat: scaffold CityHub web foundation`。
- 功能提交 hash：`2cc2ee1`。
- 分支：`main`。
- 提交前会检查 `git diff --check`、暂存内容与敏感文件；不会纳入 `node_modules`、`dist`、`.env.local` 或测试 Token。
- Push：首次两次尝试因 GitHub 443 连接超时未完成；第三次非强制重试成功，`14756a1..f2a6c9b main -> main`，本地 `main` 已与 `origin/main` 对齐。
