# Phase 5 CityHub 前端现状与 UI 审计报告

## 1. 审计结论

当前仓库**不存在 CityHub 核心业务前端源码或可构建前端工程**。`frontend/` 是 Windows Nginx 发行包，唯一可渲染页面为 Nginx 默认欢迎页；其 `/api` 反向代理配置存在，但静态根目录指向仓库中不存在的 `html/cityhub`。另有 `backend/consultant` 内嵌的 AI 顾问单页，能单独渲染，却不承载活动发现、预约、社区、登录或个人中心。

因此，后续 Phase 5 不应在“现有业务前端”上做视觉修补；应保留有价值的 Nginx 反代思路和 AI 单页交互参考，另行建立一个小型、可构建的 CityHub Web 前端，并逐步对齐现有 Core API。

本报告只读审计：未改动前端/后端代码、配置、依赖或 README；未执行 `git add`、`git commit`、`git push`。

## 2. 前端目录与真实技术栈

| 位置 | 真实内容 | 技术栈/状态 |
| --- | --- | --- |
| `frontend/` | Windows Nginx 二进制、默认配置、默认 `50x.html` 和默认欢迎页 | 不是 npm/Vue/React 项目；无 `package.json`、lockfile、`src/`、构建工具配置或业务静态资源。 |
| `frontend/html/index.html` | Nginx 默认 Welcome 页面 | 原生静态 HTML，612 B，无 CityHub UI。 |
| `frontend/html/yjshz/` | 空目录 | 历史工程目录残留；无可部署业务页面。 |
| `backend/consultant/src/main/resources/static/index.html` | AI 顾问独立单页 | CDN Vue 3.2.31 全局构建、Tailwind CSS 2.2.19、Font Awesome 6 beta、内联 JS/CSS、浏览器原生 `fetch`。没有 package 管理、router、store 或组件目录。 |

未发现 Vue SFC、React JSX/TSX、Vite、Webpack、Vue CLI、Axios、Vue Router、Pinia/Vuex、Tailwind 配置、Sass/Less 或任一前端构建脚本。故无 Node 版本、npm/yarn/pnpm 命令可审计，也不存在可执行的 `dev`、`build`、`lint` 脚本。

## 3. 运行与构建核验

| 项目 | 执行/证据 | 结果 |
| --- | --- | --- |
| Nginx 静态页渲染 | Playwright 直接渲染 `frontend/html/index.html` | 成功渲染默认 “Welcome to nginx!”；2 个外链、0 个按钮、0 个输入框，不是 CityHub 页面。 |
| Nginx 配置语法 | `frontend/nginx.exe -t -p frontend -c conf/nginx.conf` | 指令语法正确；配置测试最终失败，因为发行包缺失 `frontend/temp/client_body_temp` 运行目录。 |
| Nginx 业务静态根 | `Test-Path frontend/html/cityhub` | `false`。配置的 `root html/cityhub` 不存在；故 8083 无法提供 CityHub 业务 UI。 |
| AI 单页渲染 | Playwright 直接渲染 consultant 的 `static/index.html` | 成功，标题为“CityHub AI 顾问”；3 个按钮、1 个 textarea；无页面脚本异常。CDN 依赖可用时可展示。 |
| 核心前端 build/dev | 仓库无 package manifest 或构建配置 | 不适用；未安装依赖，也无构建命令可运行。 |

Nginx `frontend/conf/nginx.conf` 监听 `8083`，`/api/*` 会去掉 `/api` 前缀并代理到 `127.0.0.1:8081`；`/api/ai` 以 302 跳往 `localhost:8084`。该代理是可保留的部署思路，但当前不能构成可运行前端站点。

## 4. 路由、页面与组件清单

没有客户端 Router，因而没有 SPA 路由、首页、活动列表/详情、预约、社区、登录或个人中心组件。

| 预期页面/功能 | 现有文件或路由 | 状态 | 说明 |
| --- | --- | --- | --- |
| 首页 | 无 | 缺失 | 静态入口只是 Nginx 默认页。 |
| 活动分类/列表 | 无 | 缺失 | 无组件、无 API 调用。 |
| 活动详情 | 无 | 缺失 | 无详情页、Ticket 区域或活动动态区。 |
| Ticket 展示 | 无 | 缺失 | 无卡片或请求。 |
| 限量预约 | 无 | 缺失 | 无预约按钮、登录前置或订单反馈。 |
| 社区/Blog | 无 | 缺失 | 无动态流、点赞、关注、Feed 页面。 |
| 登录 | 无 | 缺失 | 无登录表单、验证码流程或未登录跳转。 |
| 个人中心 | 无 | 缺失 | 无资料、预约、我的动态界面。 |
| AI 顾问 | `backend/consultant/.../static/index.html` 的 `/` | 部分 | 单一对话界面，调用相对路径 `GET /chat?message=&memoryId=`。 |

可见“组件”只有 AI 单页内的头部、消息气泡、输入区与主题切换逻辑，均写在一个 HTML 文件里；未发现 `ActivityCard`、`BlogCard`、`Header/NavBar`、`UserAvatar`、`TicketCard`、`Pagination`、`Loading` 或 `Empty State` 的可复用实现。

## 5. API 层、登录态与后端匹配

### 5.1 现有请求层

- Core 前端请求层：不存在。无 Axios/Fetch 封装、`baseURL`、统一响应处理、错误处理、Token 注入或路由守卫。
- AI 单页：只使用 `fetch('/chat?message=...&memoryId=...')` 流式读取响应；对应 AI 模块的 `ChatController` `/chat`，端口配置为 `8084`。没有请求 CityHub Core。
- AI 单页仅在 `localStorage` 保存 `darkMode`，不保存登录 token；没有 `Authorization` Header、Cookie 登录态或注销逻辑。
- Core 实际需要在请求 Header 传 `authorization: {token}`，由 `RefreshTokenInterceptor` 读取 Redis Token。AI 单页未实现该契约。

### 5.2 前后端接口匹配矩阵

| 前端功能 | 前端接口 | 后端接口 | 当前状态 |
| --- | --- | --- | --- |
| 登录 | 无 | `POST /user/code`、`POST /user/login`、`GET /user/me` | MISSING |
| 活动分类 | 无 | `GET /activity-category/list` | MISSING |
| 活动列表/搜索 | 无 | `GET /activity/of/category`、`GET /activity/of/name` | MISSING |
| 活动详情 | 无 | `GET /activity/{id}` | MISSING |
| Ticket | 无 | `GET /ticket/list/{activityId}` | MISSING |
| 限量预约 | 无 | `POST /reservation/seckill/{ticketId}` | MISSING |
| Blog | 无 | `POST /blog`、`GET /blog/{id}`、`GET /blog/hot`、`GET /blog/of/activity` | MISSING |
| Like | 无 | `PUT /blog/like/{id}`、`GET /blog/likes/{id}` | MISSING |
| Follow | 无 | `PUT /follow/{id}/{isFollow}`、`GET /follow/or/not/{id}` | MISSING |
| Feed | 无 | `GET /blog/of/follow?lastId=&offset=` | MISSING |
| AI 顾问 | `GET /chat` | AI 模块 `GET /chat` | MATCH（仅 AI 单页）；Nginx 的 `/api/ai` 302 并未与该相对请求自然结合。 |

## 6. CityHub / 旧业务残留

- 用户可见核心前端：没有 CityHub 业务文案，也没有 Shop、Voucher、点评、探店、商户、店铺、优惠券、团购、秒杀券、YJSHZ 或雅鉴生活志文案；原因是核心业务前端缺失，并非已完成清理。
- 用户可见 Nginx 残留：标题与正文均为 “Welcome to nginx!”；这是最高优先级的品牌失败。
- 部署配置残留：`frontend/html/yjshz/` 空目录仍在；`nginx.conf` 已指向 `html/cityhub`，但该目录未随仓库提交，形成工程身份迁移不完整。
- AI 页面：用户可见名称已是 “CityHub AI 顾问”，但 AI 后端源码仍有 `Shop`、`Voucher`、`VoucherOrder` POJO、Mapper、Service、Tool 等旧业务内部命名。这些不在主 UI 中展示，但应在未来 AI 单独迁移时处理。
- 历史审计文件中仍有旧项目记录，属于文档归档，不属于当前前端运行路径。

## 7. UI 现状与工程质量

### 核心站点

无法进行首页、活动详情、预约、社区或个人中心的视觉评分，因为它们不存在。默认页没有品牌色、导航、搜索、卡片、图片、空状态、Loading、响应式布局或移动端设计；仅是 Nginx 原始默认窄栏页面。

### AI 顾问单页

| 维度 | 现状 |
| --- | --- |
| 布局 | 有完整高度的聊天布局、顶部栏、滚动消息区、输入区；移动端 meta 存在。 |
| 配色与层级 | Tailwind 蓝/绿/灰色默认配色，基础浅/深色切换；视觉可用但通用、与城市文化活动品牌无关联。 |
| 交互 | 支持会话清空、请求中止、流式读取、textarea 自适应和消息逐字显示。 |
| 卡片/图片 | 仅消息气泡；无活动卡、票券卡、图片处理、封面比例或 `object-fit` 规则。 |
| 响应式 | 主要依赖 Tailwind 默认 flex 和 `max-w-*`，未见针对断点的业务布局设计。 |
| 稳定性 | 依赖 Vue/Tailwind/Font Awesome 三个公网 CDN；离线或 CDN 失败时页面不可完整工作。`console.log/error` 保留；请求错误可显示文案，但没有重试、网络状态或统一错误页。 |
| 工程性 | 400 余行单文件，展示、状态、网络与动画耦合；无类型、测试、组件拆分或构建流程。 |

## 8. 页面完整度评分（5 分制）

| 页面 | 功能完整度 | 视觉完成度 | CityHub 业务匹配度 | 原因 |
| --- | ---: | ---: | ---: | --- |
| 首页 | 0 | 0 | 0 | 只有 Nginx 默认页。 |
| 活动列表 | 0 | 0 | 0 | 无页面、路由、请求。 |
| 活动详情 | 0 | 0 | 0 | 无页面、Ticket/体验动态关联区。 |
| 预约 | 0 | 0 | 0 | 未接入 Reservation API 或登录态。 |
| 社区 | 0 | 0 | 0 | 未接入 Blog/Like/Follow/Feed。 |
| 登录 | 0 | 0 | 0 | 无验证码 UI、Token 保存或鉴权恢复。 |
| 个人中心 | 0 | 0 | 0 | 无资料、预约、我的动态页面。 |
| AI 顾问（非核心评分项） | 2 | 3 | 1 | 聊天界面可渲染，但未与活动领域、登录用户或核心导航融合。 |

## 9. 问题优先级

| 优先级 | 问题 | 影响 |
| --- | --- | --- |
| P0 | CityHub 业务前端源码与构建工程缺失；Nginx 根目录 `html/cityhub` 不存在。 | 用户无法访问任何活动/预约/社区功能。 |
| P0 | 无登录、Token 注入、API 请求层或 Core API 页面集成。 | 预约、发动态、点赞、关注、Feed 均无法使用。 |
| P1 | Nginx 静态入口保留默认 Welcome 页面；发行包还缺少 `temp` 运行目录。 | 8083 即使启动也不构成 CityHub 品牌或稳定部署。 |
| P1 | 现有 AI 单页与 Core 无统一导航、登录态或业务入口，且 CDN 依赖外网。 | 无法作为平台完整前端的一部分展示。 |
| P2 | 缺少设计语言：品牌色、排版、图片裁切、卡片规范、Loading/空状态、桌面/移动断点。 | 后续新增页面易形成碎片化 UI。 |
| P2 | AI 单页全部逻辑堆在静态 HTML，存在 console 输出、无组件化和统一错误处理。 | 后续维护成本高。 |
| P3 | AI 模块内部仍有 Shop/Voucher 旧命名。 | 不阻断 Phase 5 Web 首发，适合在 AI 独立迁移阶段处理。 |

## 10. 值得保留的内容

1. `nginx.conf` 的同源 `/api` → Core `8081` 转发思路，适合新前端继续使用（须修正静态根目录和运行目录）。
2. 后端已稳定的活动、Ticket、预约、Blog、Follow、Feed API，可直接作为新 UI 数据源。
3. AI 单页的流式 `fetch`、`AbortController`、聊天滚动、输入框自适应、深色模式持久化，可在未来用组件化方式迁移到“AI 顾问”独立页面。
4. `Result` 的统一 `{success,errorMsg,data,total}` 响应形态，适合在新请求层统一解包和错误提示。

## 11. Phase 5 正式改造建议（仅建议，未实施）

### 11.1 方案选择

建议**新建轻量、可构建的独立 Web 工程**，而不是试图修补默认 Nginx HTML。优先选择 Vue 3 + Vite + Vue Router + Axios：AI 单页已经使用 Vue 3，团队认知迁移最小；使用当前稳定版本，不在此审计阶段安装或升级任何依赖。Nginx 保留为最终静态托管和 `/api` 反代层。

不要把 AI 单页直接扩展成全站：它没有工程化、路由或 Token 契约。可在主站完成后把它抽成 `/assistant` 页面，复用其流式聊天体验。

### 11.2 分步范围

1. 建立工程骨架：路由、Axios 实例、`authorization` Token 注入、`Result` 解包、401 清理与登录跳转、全局 Loading/Empty/Error 组件。
2. 先实现可展示闭环：首页（分类 + 热门/搜索活动）、活动列表、活动详情（Ticket 列表 + 预约入口）。
3. 实现账号闭环：验证码登录、Token 持久化、个人中心基础资料；预约成功/失败反馈对齐 `POST /reservation/seckill/{ticketId}`。
4. 实现社区闭环：活动体验动态、活动关联动态、点赞、关注与 Feed 的滚动加载。
5. 最后接入 AI 顾问并统一导航/视觉；再补移动端、可访问性、空态、Loading、错误态和静态资源规范。

### 11.3 设计方向

采用“城市文化活动发现”而非点评红色风格：暖白/深墨基础色配以低饱和文化色，强调活动封面大图、时间/地点/票价信息层级与克制的圆角卡片。首页、活动详情和社区应共享导航、栅格、图片比例与按钮层级；先做桌面和移动两套明确断点，再增加装饰性视觉。

## 12. 审计边界与 Git

- 初始 Git 状态仅有本阶段提示词文件未跟踪；未触碰其内容，也未暂存任何文件。
- 本报告是本阶段唯一新文件；除此之外没有代码、配置、依赖、构建产物或 Git 历史变更。
- 未启动或修改任何后端业务服务；浏览器只读渲染两个本地 HTML 文件。
