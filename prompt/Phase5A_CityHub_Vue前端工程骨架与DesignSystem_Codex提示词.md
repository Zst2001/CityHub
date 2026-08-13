# Codex 执行任务：Phase 5A Vue 前端工程骨架 + CityHub Design System + 登录/API 基础设施

## 一、阶段背景

项目：

> **CityHub - 城市活动发现与预约平台**

当前后端核心已经完成并验证：

```text
用户登录
ActivityCategory / Activity / Ticket
Redis 缓存治理
限量活动预约 / 秒杀
Blog 活动体验社区
Follow / Feed
```

Phase 5 前端审计已经确认：

- 当前仓库没有 CityHub 核心业务前端源码；
- `frontend/` 实际是 Windows Nginx 发行包；
- `frontend/html/index.html` 是默认 Nginx 欢迎页；
- `frontend/html/cityhub` 当前不存在；
- `backend/consultant/.../static/index.html` 只是独立 AI 顾问单页；
- 当前不存在 Vue/React 工程、Router、Axios 请求层、Token 管理或主站 UI。

因此 Phase 5 采用：

> **新建独立、可构建的 CityHub Web 工程**

而不是修改 Nginx 默认 HTML。

---

# 二、本阶段定位

Phase 5A 只做：

> **前端工程骨架 + Design System + Router + Axios + Pinia + 登录基础设施 + API 基础能力**

本阶段不要正式开发完整首页、活动详情、社区页面。

目标是从：

```text
没有业务前端
```

推进到：

```text
Vue 工程可以启动
↓
Router 可工作
↓
Axios 能请求 CityHub Core
↓
Token 可持久化并自动注入
↓
401 能统一处理
↓
验证码登录可真实跑通
↓
GET /user/me 可恢复用户
↓
ActivityCategory / Activity 可做基础连通性验证
↓
CityHub Design System 已建立
↓
后续页面可直接复用基础组件
```

---

# 三、前端源码目录

仓库根目录：

```text
F:\JavaProject\YJSHZ-main
```

新建源码工程：

```text
F:\JavaProject\YJSHZ-main\web
```

不要把源码直接塞进：

```text
frontend/
```

因为当前 `frontend/` 是 Nginx 运行目录。

职责固定为：

```text
web/
= Vue 前端源码

frontend/
= Nginx / 后续静态部署目录
```

后续正式 build 再将 `web/dist` 对接：

```text
frontend/html/cityhub
```

Phase 5A 暂时不要做自动复制部署脚本，除非 build 验证确有必要。

---

# 四、技术栈

建立：

```text
Vue 3
Vite
Vue Router
Axios
Pinia
Element Plus
普通 CSS + CSS Variables
```

原则：

- Vue 3 使用 Composition API；
- 允许 `<script setup>`；
- 不引入 TypeScript，优先控制学习和维护成本；
- 不引入 Tailwind；
- 不引入 Sass/Less；
- 不引入大型动画框架；
- 不引入额外状态管理方案；
- Element Plus 只用于通用交互，不用于把整个项目做成后台管理风格。

---

# 五、依赖版本原则

不要随意锁定网上复制来的过时版本。

根据当前本机 Node/npm 环境创建兼容工程。

先执行：

```bash
node -v
npm -v
```

记录实际版本。

创建项目后必须保留：

```text
package.json
package-lock.json
```

使用 npm。

不要混用：

```text
yarn
pnpm
```

---

# 六、Phase 5A 禁止事项

本阶段禁止正式实现：

```text
完整首页
活动推荐区
完整 Activity 列表
Activity 详情大页面
Ticket 完整视觉
社区 Feed
BlogCard 正式页面
个人中心完整业务
AI 顾问迁移
复杂移动端适配
复杂动画
PWA
SSR
Nuxt
```

也禁止修改：

```text
后端缓存
秒杀
Lua
Redisson
BlockingQueue
Blog / Follow 逻辑
AI 后端
README
```

只有前端接入时发现真实 API 阻断，才允许对后端做最小修复，并必须在报告说明。

---

# 七、任务 0：Git 基线

开始前执行：

```bash
git status --short
git log --oneline -n 10
git branch --show-current
git remote -v
```

要求：

```text
main
working tree clean
main == origin/main
```

当前已知 Phase 4 最新提交应为：

```text
14756a1 feat: adapt CityHub activity community flow
```

如果存在用户未提交文件，不要覆盖，先识别。

---

# 八、任务 1：创建 Vue 工程

在仓库根目录创建：

```text
web/
```

要求最终至少存在：

```text
web/
├─ package.json
├─ package-lock.json
├─ vite.config.js
├─ index.html
├─ src/
│  ├─ main.js
│  ├─ App.vue
│  ├─ router/
│  ├─ stores/
│  ├─ api/
│  ├─ components/
│  ├─ layouts/
│  ├─ views/
│  ├─ styles/
│  └─ utils/
└─ public/
```

不要把无关 demo 代码长期保留。

---

# 九、任务 2：Vite 开发代理

当前 Core：

```text
http://127.0.0.1:8081
```

Vite 开发阶段统一请求：

```text
/api/...
```

在 `vite.config.js` 配置代理：

```text
/api
→ http://127.0.0.1:8081
```

并正确移除：

```text
/api
```

前缀，使：

```text
/api/activity/1
```

最终转发到：

```text
http://127.0.0.1:8081/activity/1
```

与后续 Nginx `/api` 反代思路保持一致。

---

# 十、环境变量

建立：

```text
web/.env.development
web/.env.production
```

但只放非敏感前端配置，例如：

```text
VITE_API_BASE=/api
```

不要写：

```text
数据库密码
Redis 密码
API Secret
```

如果 production 同样走 Nginx 同源 `/api`：

继续使用：

```text
VITE_API_BASE=/api
```

即可。

---

# 十一、任务 3：建立目录规范

建议：

```text
src/
├─ api/
│  ├─ request.js
│  ├─ user.js
│  ├─ activity.js
│  ├─ ticket.js
│  ├─ reservation.js
│  ├─ blog.js
│  └─ follow.js
│
├─ components/
│  ├─ common/
│  └─ business/
│
├─ layouts/
│  └─ DefaultLayout.vue
│
├─ router/
│  └─ index.js
│
├─ stores/
│  └─ user.js
│
├─ styles/
│  ├─ tokens.css
│  ├─ reset.css
│  ├─ global.css
│  └─ element-overrides.css
│
├─ utils/
│  └─ storage.js
│
└─ views/
   ├─ HomeView.vue
   ├─ LoginView.vue
   ├─ ActivityListView.vue
   ├─ ActivityDetailView.vue
   ├─ CommunityView.vue
   ├─ ProfileView.vue
   └─ NotFoundView.vue
```

Phase 5A 允许这些页面先作为：

> **高质量骨架页 / 占位页**

不要提前把所有业务做完。

---

# 十二、任务 4：建立 Router

路由先固定：

```text
/
→ HomeView

/login
→ LoginView

/activities
→ ActivityListView

/activities/:id
→ ActivityDetailView

/community
→ CommunityView

/profile
→ ProfileView

/:pathMatch(.*)*
→ NotFoundView
```

未来：

```text
/assistant
```

Phase 5A 不实现。

---

# 十三、路由 meta

需要登录的页面至少：

```text
/profile
```

使用：

```js
meta: {
  requiresAuth: true
}
```

Phase 5A 不强制 `/community` 整页需要登录，因为浏览热门 Blog 可以后续开放。

---

# 十四、任务 5：建立 Pinia 用户状态

创建：

```text
stores/user.js
```

管理：

```text
token
user
isLoggedIn
```

至少提供：

```text
setToken()
clearAuth()
fetchCurrentUser()
logout()
```

---

# 十五、Token 持久化

当前后端真实契约：

```text
Header:
authorization: <token>
```

不是：

```text
Bearer <token>
```

所以 Axios 必须按真实后端要求：

```text
authorization: token
```

不要擅自增加：

```text
Bearer
```

Token 可保存：

```text
localStorage
```

Key 使用明确 CityHub 名称，例如：

```text
cityhub_token
```

不要沿用：

```text
hmdp
yjshz
shop
```

---

# 十六、任务 6：Axios 请求层

建立：

```text
src/api/request.js
```

Axios 实例：

```text
baseURL = import.meta.env.VITE_API_BASE
```

统一：

```text
timeout
request interceptor
response interceptor
```

---

# 十七、请求拦截器

如果存在 token：

```text
config.headers.authorization = token
```

不要把 Token 写入 query 参数。

---

# 十八、后端统一 Result 解包

当前后端 Result 结构：

```text
success
errorMsg
data
total
```

响应拦截器必须：

## HTTP 正常 + success=true

返回：

```text
data
```

或返回一个项目统一约定的对象。

要求所有 API 文件风格一致。

---

## HTTP 正常 + success=false

统一：

```text
ElMessage.error(errorMsg)
```

然后：

```text
Promise.reject(...)
```

不要每个页面重复写。

---

# 十九、401 统一处理

当 HTTP：

```text
401
```

时：

```text
清理 token
清理 user
```

并跳转：

```text
/login
```

避免无限重定向。

如果当前就在 `/login`：

不要重复 push。

---

# 二十、任务 7：API 模块

本阶段建立接口封装。

## user.js

至少：

```text
sendCode(phone)
login(data)
getCurrentUser()
```

对应：

```text
POST /user/code
POST /user/login
GET /user/me
```

---

## activity.js

至少：

```text
getActivityCategories()
getActivityById(id)
getActivitiesByCategory(params)
searchActivitiesByName(params)
```

对应：

```text
GET /activity-category/list
GET /activity/{id}
GET /activity/of/category
GET /activity/of/name
```

---

## ticket.js

封装：

```text
getTicketsByActivity(activityId)
```

对应：

```text
GET /ticket/list/{activityId}
```

---

## reservation.js

封装：

```text
seckillTicket(ticketId)
```

对应：

```text
POST /reservation/seckill/{ticketId}
```

Phase 5A 只封装，不做完整预约 UI。

---

## blog.js / follow.js

先封装当前核心 API。

允许后续 Phase 5C 再真正使用。

不要在本阶段实现社区页面业务。

---

# 二十一、任务 8：路由守卫

全局 beforeEach：

如果：

```text
requiresAuth = true
```

且：

```text
无 token
```

跳：

```text
/login
```

并保留来源，例如：

```text
redirect
```

登录成功后可回原页面。

---

# 二十二、任务 9：App 启动时恢复登录状态

应用初始化：

如果 localStorage 有 token：

```text
fetchCurrentUser()
```

成功：

```text
恢复 user
```

失败：

```text
清除 token
```

不要仅因为 localStorage 有字符串就认为登录有效。

---

# 二十三、任务 10：登录页真实实现

虽然 Phase 5A 不做完整业务页面，但：

> **登录必须真实可用。**

`LoginView.vue` 做成正式页面，而不是纯占位。

---

# 二十四、登录页 UI

视觉定位：

```text
暖白背景
CityHub Logo / Wordmark
简洁表单卡片
城市活动氛围背景或低干扰装饰
```

不要使用：

```text
后台系统登录页
大面积蓝色渐变
科技感网格
黑红电竞风
```

---

# 二十五、登录表单

字段：

```text
手机号
验证码
```

按钮：

```text
获取验证码
登录 / 注册
```

---

# 二十六、验证码交互

至少实现：

```text
手机号基本格式校验
获取验证码按钮
60 秒倒计时
倒计时期间禁用
```

调用真实：

```text
POST /user/code
```

如果本地开发后端没有真实短信服务，而开发环境通过 Redis 测试验证码：

不要在前端硬编码验证码。

真实运行测试可由 Codex按现有后端测试方式准备 Redis 验证码。

---

# 二十七、登录成功

调用：

```text
POST /user/login
```

成功后：

```text
保存 token
GET /user/me
恢复 user
ElMessage.success
跳转 redirect 或 /
```

---

# 二十八、任务 11：DefaultLayout

建立：

```text
DefaultLayout.vue
```

包含：

```text
AppHeader
main
AppFooter
```

页面统一在：

```text
max-width
```

和全局 spacing 规范下工作。

---

# 二十九、任务 12：AppHeader

Phase 5A 就把 Header 做成正式组件。

桌面端：

```text
CityHub

发现活动
活动社区

右侧：
登录 / 用户头像+昵称
```

暂时不加入 AI 顾问入口，留到后续。

---

# 三十、Header 行为

导航：

```text
发现活动
→ /activities

活动社区
→ /community
```

Logo：

```text
→ /
```

未登录：

```text
登录
```

已登录：

```text
头像
昵称
```

点击可进入：

```text
/profile
```

---

# 三十一、任务 13：Footer

保持极简。

例如：

```text
CityHub
发现城市里的精彩活动
© 2026 CityHub
```

不要写虚假：

```text
ICP备案
合作品牌
真实公司信息
```

---

# 三十二、任务 14：CityHub Design System

建立：

```text
src/styles/tokens.css
```

统一设计变量。

---

# 三十三、颜色

建议使用：

```css
--color-bg: #F7F6F2;
--color-surface: #FFFFFF;

--color-text-primary: #20211F;
--color-text-secondary: #6F716B;
--color-text-muted: #999B95;

--color-border: #E8E6DF;

--color-primary: #426B5A;
--color-primary-hover: #365B4B;
--color-primary-soft: #E8F0EC;

--color-accent: #C87B52;
--color-accent-soft: #F5E9E1;

--color-success: #3F7A5A;
--color-warning: #B8793B;
--color-danger: #B95C52;
```

如果实际实现中有轻微调整可以接受，但整体必须保持：

> 暖白 + 深墨 + 墨绿 + 橙棕强调

不要变成默认 Element Plus 蓝色主题。

---

# 三十四、字体

不引入外部付费字体。

优先系统字体栈，例如：

```css
font-family:
  Inter,
  "PingFang SC",
  "Microsoft YaHei",
  system-ui,
  sans-serif;
```

不要下载或提交字体文件。

---

# 三十五、圆角

统一：

```css
--radius-sm: 8px;
--radius-md: 12px;
--radius-lg: 16px;
--radius-xl: 20px;
```

---

# 三十六、间距

统一 4/8 基础系统：

```text
4
8
12
16
24
32
48
64
```

建立变量。

---

# 三十七、阴影

使用克制轻阴影。

例如概念：

```text
shadow-sm
shadow-md
```

不要每个容器都有厚重阴影。

---

# 三十八、容器

主内容：

```text
max-width: 1200px
```

页面两侧 padding：

```text
桌面 24~32px
移动 16px
```

---

# 三十九、断点

Phase 5A 先建立三个明确层级：

```text
Mobile
< 768

Tablet
768 ~ 1023

Desktop
>= 1024
```

不用建立复杂响应式体系。

---

# 四十、任务 15：Element Plus 主题覆盖

建立：

```text
element-overrides.css
```

至少统一：

```text
Button
Input
Dialog
Message
Skeleton
Empty
```

主按钮改为 CityHub 墨绿色。

不要保留大量 Element Plus 默认蓝色视觉。

---

# 四十一、任务 16：基础通用组件

Phase 5A 至少建立：

```text
AppHeader.vue
AppFooter.vue
PageContainer.vue
PageLoading.vue
EmptyState.vue
ErrorState.vue
SectionHeader.vue
```

---

# 四十二、基础业务组件骨架

可以建立但不必完整：

```text
ActivityCard.vue
TicketCard.vue
BlogCard.vue
```

要求：

- 有基本 props 接口；
- 有统一视觉骨架；
- 暂时不需要完整业务交互。

为 Phase 5B/5C 准备。

---

# 四十三、ActivityCard 骨架风格

必须体现：

```text
封面
类别
标题
时间
地点
价格
```

但 Phase 5A 可以只使用 mock / props demo。

不要正式拉活动列表做完整首页。

---

# 四十四、Task 17：首页骨架

`HomeView.vue` 只做“可展示骨架”，不是正式首页。

可以有：

```text
Hero
标题：
发现城市，遇见有趣。

副标题：
展览、音乐、市集、讲座与周末体验，都在 CityHub。

搜索框视觉占位
分类区视觉占位
少量 ActivityCard skeleton/demo
```

重点：

> 用户打开 `/` 时已经看到一个像 CityHub 的页面，而不是 Vite 默认页。

---

# 四十五、首页 Phase 5A 边界

暂时不要：

```text
真实热门排序
复杂搜索
推荐算法
轮播
分页
完整 API 数据联动
```

只做设计骨架。

Phase 5B 再正式接活动数据。

---

# 四十六、ActivityList / ActivityDetail / Community / Profile

Phase 5A 做统一高质量空壳：

```text
页面 Header
标题
简短描述
EmptyState / ComingSoon
```

不要用：

```text
“TODO”
“开发中...”
粗糙纯文本
```

要保持视觉完整。

---

# 四十七、任务 18：404 页面

`NotFoundView.vue`：

```text
404
没有找到这个页面
返回首页
```

保持品牌视觉。

---

# 四十八、任务 19：真实 API 连通性

完成 Axios / Router 后，必须在真实后端环境中至少验证：

```text
GET /activity-category/list
GET /activity/1
```

前端通过：

```text
/api
```

代理成功。

---

# 四十九、真实登录验证

至少验证一次完整：

```text
获取验证码
↓
登录
↓
返回 token
↓
localStorage 保存
↓
authorization Header 自动注入
↓
GET /user/me
↓
Header 显示用户
```

如果短信无法真实发送：

可以按照后端既有开发测试方式在测试 Redis 写验证码。

不要把测试验证码写进前端代码。

---

# 五十、Token 失效验证

使用无效 token 或清理 Redis token 后：

```text
GET /user/me
→ 401
```

前端必须：

```text
clearAuth()
→ /login
```

---

# 五十一、任务 20：基础 Loading / Empty / Error

至少提供：

```text
PageLoading
EmptyState
ErrorState
```

首页骨架和 API 测试中可以实际展示。

不要让失败页面只显示：

```text
console.error
```

---

# 五十二、任务 21：构建验证

必须执行：

```bash
cd web
npm run build
```

要求：

```text
BUILD SUCCESS
```

记录：

```text
dist/
```

生成情况。

---

# 五十三、开发启动验证

执行：

```bash
npm run dev
```

记录：

```text
端口
启动结果
```

使用浏览器检查：

```text
/
 /login
 /activities
 /profile
```

至少无明显 console error。

---

# 五十四、任务 22：基本视觉检查

通过浏览器检查：

```text
1440px 桌面
390px 移动端
```

至少确保：

```text
Header 不溢出
登录页可用
首页骨架不横向滚动
按钮可点击
文本不重叠
```

Phase 5A 不要求像素级移动端优化。

---

# 五十五、不要使用公网 CDN

新 `web/` 工程所有核心依赖必须通过 npm 管理。

不要继续使用 AI 单页那种：

```text
Vue CDN
Tailwind CDN
FontAwesome CDN
```

主站不能依赖公网 CDN 才能运行。

---

# 五十六、静态资源

Phase 5A 不需要准备大量活动照片。

允许：

```text
CSS 渐变
简单本地 SVG
简洁占位图
```

但不要下载版权不明图片。

Phase 5B 再统一准备 Activity 图片资产。

---

# 五十七、Logo

Phase 5A 使用简单文字 Logo：

```text
CityHub
```

可以加非常简单的 CSS / SVG 图形标记。

不要投入时间设计复杂品牌 Logo。

不要使用旧 YJSHZ Logo。

---

# 五十八、后端修改边界

原则：

```text
后端冻结
```

如果前端联调发现：

```text
CORS
Result
API 路径
登录返回
```

存在真实阻断：

先判断 Vite `/api` proxy 能否解决。

优先不改后端。

只有确定后端本身有 bug 时才能最小修复，并写进报告。

---

# 五十九、Nginx 边界

Phase 5A 保留现有：

```text
frontend/conf/nginx.conf
```

反代思路。

暂时不要重写整个 Nginx。

允许检查：

```text
/api
root html/cityhub
```

与未来 build 部署是否兼容。

正式：

```text
web/dist
→ frontend/html/cityhub
```

部署动作可留到 Phase 5D / 收尾阶段。

---

# 六十、唯一报告

本阶段只生成一个报告：

```text
F:\JavaProject\YJSHZ-main\docs\PHASE5A_REPORT.md
```

禁止生成：

```text
多个 audit
多个 verification
docs/refactor/phase5a/
```

只允许一个阶段报告。

---

# 六十一、报告必须包含

## 1. 阶段结论

```text
Phase 5A 是否通过
```

## 2. 新前端工程

记录：

```text
目录
Vue/Vite
Node/npm
主要依赖
```

## 3. Router

列出路由。

## 4. Axios

说明：

```text
/api
authorization
Result 解包
401
```

## 5. Pinia

说明：

```text
token
user
登录恢复
logout
```

## 6. 登录

记录真实：

```text
sendCode
login
token
/user/me
```

验证结果。

## 7. Design System

记录：

```text
主色
背景
字体
圆角
spacing
断点
```

## 8. 基础组件

列出已完成组件。

## 9. 页面骨架

记录：

```text
Home
Login
Activities
ActivityDetail
Community
Profile
404
```

## 10. API 连通性

至少：

```text
ActivityCategory
Activity Detail
```

## 11. Build / Dev

记录：

```text
npm run dev
npm run build
```

## 12. 响应式检查

记录：

```text
1440px
390px
```

## 13. 后端是否修改

据实说明。

## 14. 已知问题

列出：

```text
Phase 5B 需要继续做什么
```

## 15. Git

记录：

```text
commit
hash
branch
push
```

---

# 六十二、Git 提交

完成代码与验证后：

```bash
git status --short
git diff --check
```

检查：

```text
node_modules
.env.local
真实 token
日志
dist（根据 .gitignore 策略）
```

---

# 六十三、.gitignore

确保：

```text
web/node_modules/
web/dist/
web/.env.local
```

不进入 Git。

允许提交：

```text
web/.env.development
web/.env.production
```

前提是只有：

```text
VITE_API_BASE=/api
```

等非敏感配置。

---

# 六十四、Git commit

执行：

```bash
git add .
git diff --cached --check
git diff --cached --stat
```

确认后：

```bash
git commit -m "feat: scaffold CityHub web foundation"
```

---

# 六十五、Git push

```bash
git push
```

禁止：

```text
force push
```

---

# 六十六、Phase 5A 验收标准

必须满足：

```text
web/ Vue 3 工程存在

npm run dev：
PASS

npm run build：
PASS

Router：
PASS

Axios：
PASS

/api Vite Proxy：
PASS

authorization Token：
PASS

Pinia User Store：
PASS

真实登录：
PASS

GET /user/me：
PASS

401 清理：
PASS

ActivityCategory API：
PASS

Activity Detail API：
PASS

CityHub Design Tokens：
存在

AppHeader / AppFooter：
存在

Loading / Empty / Error：
存在

首页：
已有 CityHub 品牌骨架

Login：
正式可用

桌面 / 移动基础布局：
无明显破坏

后端核心：
未被大改

Git：
commit + push
```

---

# 六十七、不作为 Phase 5A 阻断项

当前不要求：

```text
完整首页数据化
完整活动列表
完整活动详情
Ticket 预约 UI
Blog Feed
社区完整功能
个人中心完整功能
AI 顾问
Nginx 正式部署
复杂图片资产
完整移动端适配
```

这些后续处理。

---

# 六十八、下一阶段

Phase 5A 通过后进入：

> **Phase 5B：首页 + Activity 列表 + Activity 详情 + Ticket / 预约 UI**

Phase 5B 是主要视觉包装阶段。

Phase 5A 不提前做 Phase 5B 的完整业务。

---

# 六十九、最终回复格式

完成后只输出：

```text
Phase 5A Vue 前端工程骨架完成。

1. Web 目录：
2. Vue / Vite：
3. Node / npm：
4. Router：
5. Axios：
6. Pinia：
7. Token：
8. 登录：
9. /user/me：
10. API Proxy：
11. ActivityCategory API：
12. Activity Detail API：
13. Design System：
14. 基础组件：
15. 页面骨架：
16. Desktop：
17. Mobile：
18. npm run dev：
19. npm run build：
20. 后端修改：
21. Git commit：
22. commit hash：
23. push：
24. 下一阶段：

唯一报告：
F:\JavaProject\YJSHZ-main\docs\PHASE5A_REPORT.md
```

---

# 七十、最终原则

Phase 5A 的目标不是一次把整个前端做完。

目标是：

> **先搭一个真正可维护、可运行、有统一视觉语言的 CityHub Web 基础工程，让后续首页、活动详情、预约和社区页面不再从零开始。**

重点：

```text
工程规范
API 基础设施
登录
Design System
可复用组件
```

而不是堆页面数量。

完成 Phase 5A 后再集中精力做 Phase 5B 的首页和 Activity 视觉。
