# CityHub Login Robustness + Logout Completion

## Final Status

```text
LOGIN_LOGOUT_FIX: PASS
```

本轮只修改管理员登录回滚和通用 Logout 闭环；手机号验证码登录、Redis TTL、Admin 权限模型、Consultant、LangChain4j、Qwen、ActivityTool、run-local.ps1、Docker AI 配置均未修改。

## Login

### Admin Login

`web/src/views/LoginView.vue` 现在执行完整流程：

```text
POST /admin/login
→ setToken()
→ await /user/me
→ 校验 user.role === ADMIN
→ await router.replace(route.query.redirect || '/admin/activities')
```

任意一步失败都会执行 `userStore.clearAuth()`，不再保留 Token 已存在但 User 未恢复的半登录状态。

真实浏览器（`http://127.0.0.1:8088`）验证：

```text
POST /api/admin/login → 200
GET /api/user/me     → 200
最终 URL             → /admin/activities
cityhub_token        → 存在
```

### Login Navigation

正常管理员登录自动进入 `/admin/activities`，且 `router.replace` 已被 `await`。

### Login Redirect

访问 `/login?redirect=/activities/3` 后登录，真实浏览器最终进入：

```text
/activities/3
```

原有 redirect 逻辑保留，没有强制改成固定首页。

### `/user/me` Failure Rollback

通过 Playwright 仅将 `/api/user/me` 模拟为 502：

```text
/admin/login → 200
/user/me     → 502
最终 URL     → /login
cityhub_token → 不存在
页面提示     → 登录状态初始化失败，请重新登录
```

### Token Cleanup

管理员流程在 `setToken()` 后的 `/user/me`、角色校验或路由导航失败，均进入 catch 并调用 `clearAuth()`。`clearAuth()` 同时清空 Pinia `token`、Pinia `user` 和 `localStorage.cityhub_token`。

### Login Error Handling

- 无效管理员凭据：真实浏览器只显示一次“用户名或密码错误”；
- `/user/me` 初始化失败：显示“登录状态初始化失败，请重新登录”，并抑制该请求的全局重复网络提示；
- 真实无响应网络错误：保留 Axios 全局“网络连接失败，请稍后重试”；
- 普通手机号验证码登录流程未修改。

## Logout

### Backend Logout API

新增真实实现：

```text
POST /user/logout
Header: authorization: <token>
```

Controller 将 Header 交给 `IUserService.logout`，Service 使用现有 `RedisConstants.LOGIN_USER_KEY` 删除登录 Session。空 Token、已删除 Token 和重复退出均返回成功，保持幂等。

为保证幂等请求能够到达 Controller，Logout 从登录拦截器排除；`RefreshTokenInterceptor` 对 `/user/logout` 直接放行，不改变其他请求的 Token 刷新逻辑。

### Redis Session Deletion

删除 Key 复用现有常量：

```text
login:token:{token}
```

真实 E2E 在退出前确认该 Key 存在，退出后确认不存在；没有新增第二套 Session Key，也没有修改任何 TTL。

### Frontend Logout API

`web/src/api/user.js` 新增 `logout()`，调用 `POST /user/logout`。现有 Axios 请求拦截器自动携带 `authorization: <token>`，不添加 Bearer 前缀。

### userStore Logout

`web/src/stores/user.js` 的 `logout()` 现在为异步闭环：

```javascript
try {
  await logoutApi()
} finally {
  clearAuth()
}
```

无论后端 Logout 成功、失败或网络异常，浏览器本地认证状态都会清理。

### Profile Logout Button

`ProfileView.vue` 个人信息卡新增轻量“退出登录”按钮：

- Desktop：卡片右上区域；
- Mobile：卡片内容下方，占满可用宽度；
- 使用暖白、细边框和轻量 hover danger 色，不使用大型红色危险按钮。

点击后显示 loading，等待 Store Logout，最后 `router.replace('/')`。

### LocalStorage Cleanup

真实 E2E：退出后 `localStorage.cityhub_token` 不存在。

### Pinia Cleanup

代码路径确认 `clearAuth()` 将 Pinia `token` 置空、`user` 置空；退出后 Header 不再渲染管理员入口，路由守卫也按未登录状态工作。

### Header Cleanup

真实退出后返回首页，Header 不再显示“活动管理”。

### Route Guard

退出后真实直接访问：

```text
/profile          → /login?redirect=/profile
/admin/activities → /login?redirect=/admin/activities
```

既有 `requiresAuth` 保护保留并得到验证。

### Logout API Failure Fallback

通过 Playwright 将 `POST /api/user/logout` 模拟为 502，真实结果：

```text
最终 URL              → /
cityhub_token         → 不存在
```

说明后端 Logout 失败时，本地浏览器仍然完成退出。

### Re-login

完成正常退出后再次使用管理员登录，真实浏览器重新进入 `/admin/activities`，证明退出没有破坏后续登录。

## Consultant

```text
CONSULTANT_CHANGE: NO CHANGE
DOCKER_CONSULTANT_REGRESSION: PASS
```

本轮没有修改 `backend/consultant/**`、LangChain4j、Qwen、ActivityTool、Streaming、Redis Chat Memory、`run-local.ps1` 或 Docker AI 环境配置。最终 Docker Consultant 仍为 running。

## Build

| Check | Result |
|---|---|
| `mvn -f backend/pom.xml clean compile -DskipTests` | PASS |
| `mvn -f backend/pom.xml package -DskipTests` | PASS |
| `npm --prefix web run build` | PASS |
| `docker compose config --quiet` | PASS |
| `docker compose build` / Web rebuild | PASS |
| Docker 五服务状态 | PASS，MySQL/Redis healthy，Core/Consultant/Web running |
| `git diff --check` | PASS |
| `.env` ignore check | PASS，`.gitignore` 命中 `.env` |
| Secret scan | PASS，未提交真实 API Key、数据库密码或 Redis 密码 |

## Git

```text
COMMIT: fix: complete CityHub login and logout flow
HASH: ce9fdea (implementation commit; final amended commit also contains this report)
PUSH: origin/main final push follows this report finalization
```

最终 commit 使用：

```text
fix: complete CityHub login and logout flow
```
