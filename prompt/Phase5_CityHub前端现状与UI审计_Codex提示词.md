# Codex 执行任务：Phase 5 前端现状与 UI 审计

## 一、任务背景
项目：CityHub - 城市活动发现与预约平台。

当前后端核心已经完成：
- 用户登录
- ActivityCategory / Activity / Ticket
- Redis 缓存治理
- 限量活动预约 / 秒杀
- Blog 活动体验社区
- Follow / Feed

现在进入前端阶段。用户说明当前前端较简陋，后续需要包装和 UI 优化。

本阶段只做“前端现状审计”，不要修改任何前端或后端代码。

## 二、审计目标
必须回答：
1. 当前前端代码在哪里？
2. 真实技术栈是什么？
3. 是否可运行？
4. 有哪些页面和路由？
5. 页面分别调用哪些后端 API？
6. 哪些页面真实可用，哪些只是静态壳子？
7. 登录态如何管理？
8. UI/CSS 的主要问题是什么？
9. 是否存在 Shop/Voucher/点评/YJSHZ/雅鉴生活志等旧业务残留？
10. 是否适配 Activity/Ticket/Reservation/Blog？
11. 当前前后端有哪些明显不匹配？
12. 后续最适合如何进行 UI 包装？

## 三、严格禁止
本阶段禁止：
- 修改前端代码
- 修改后端代码
- 修改 package.json
- 安装或升级 UI 框架
- 重构 Router / API
- 改 CSS
- 新增或删除页面
- 修改 README
- git add / commit / push

只允许读取、运行、检查、记录。

## 四、定位所有前端内容
从仓库根目录：
`F:\JavaProject\YJSHZ-main`

搜索：
- package.json
- vite.config.*
- vue.config.*
- webpack.config.*
- src/
- public/
- dist/
- index.html
- frontend/
- web/
- client/
- ui/
- nginx/html/
- *.vue / *.jsx / *.tsx / *.js / *.ts / *.css / *.scss / *.less

判断：
- 是否存在完整前端源码
- 是否只有静态 HTML
- 是否只有 build 后产物
- 是否存在 Nginx 静态资源
- 是否前端位于非标准目录

## 五、确认真实技术栈
如果存在 package.json，读取 dependencies、devDependencies、scripts，确认：
- Vue 2 / Vue 3 / React / 其他
- Vite / Webpack / Vue CLI
- Axios / Fetch
- Vue Router / React Router
- Pinia / Vuex / Redux
- Element UI / Element Plus / Ant Design / Vant / 其他
- Sass / Less / Tailwind / 普通 CSS
- Node 版本要求
- npm / yarn / pnpm
- dev / build 命令

禁止根据 README 推测。

## 六、尝试运行与构建
如果前端源码完整，优先使用项目真实命令，例如：
`npm run dev`

如 package-lock.json 存在、node_modules 缺失，可评估是否安全执行 `npm ci`。如果可能修改依赖状态或存在风险，不要安装，只记录原因。

记录：
- 启动命令
- 端口
- 是否编译成功
- warning / runtime error
- build 是否成功

不要为了通过审计修改代码。

## 七、页面与路由审计
读取 Router 配置，列出所有真实路由。

对每个页面记录：
- 路由
- 组件文件
- 页面用途
- 完整 / 部分 / 静态 / 缺失
- 是否调用后端

重点关注：
- 登录
- 首页
- 活动分类/列表
- 活动详情
- Ticket/预约
- 社区
- 个人中心

## 八、API 请求层审计
搜索 axios / request / fetch / baseURL。

确认：
- API 封装文件
- baseURL
- Token 注入
- 响应拦截
- 错误处理

检查是否使用当前接口：
- /activity-category
- /activity
- /ticket
- /reservation
- /blog
- /follow
- /user

搜索旧接口：
- /shop
- /shop-type
- /voucher
- /voucher-order

记录文件路径和影响，不修改。

## 九、登录态审计
检查：
- Token 保存位置
- localStorage / sessionStorage / Cookie
- Authorization/Header 写法
- Router Guard
- 登录跳转
- 未登录处理

判断是否与当前 CityHub Core 登录接口兼容。

## 十、核心业务覆盖矩阵
逐项检查：
- 活动分类
- 活动列表
- 活动详情
- Ticket 展示
- 限量预约
- 登录
- 个人信息
- 活动动态
- 点赞
- Follow
- Feed

状态统一写：
`完整 / 部分 / 仅静态 / 缺失`

## 十一、UI 视觉审计
从源码和实际运行效果（若可运行）评估：
- 视觉层级
- 字体
- 配色
- 圆角
- 阴影
- 卡片
- 间距
- 图片比例
- object-fit
- 空状态
- Loading
- 响应式
- 桌面端/移动端适配
- 是否仍保留明显点评类红色风格
- 是否有统一品牌感

## 十二、CityHub 品牌残留
搜索用户可见文案：
- 黑马点评
- 点评
- 探店
- 商户
- 店铺
- 优惠券
- 团购
- 秒杀券
- Shop
- Voucher
- YJSHZ
- 雅鉴生活志

区分：
- 用户可见残留
- 仅代码内部命名
- 仅历史注释

重点记录用户可见问题。

## 十三、组件复用与静态资源
检查是否已有：
- ActivityCard
- BlogCard
- Header/NavBar
- UserAvatar
- TicketCard
- Pagination
- Loading
- Empty State

判断哪些可保留，哪些重复严重。

检查：
- logo
- icon
- banner
- activity images
- avatar
- background

记录是否低质量、旧项目残留或缺失。

## 十四、工程质量
检查：
- console.log
- TODO/FIXME
- 硬编码 URL
- 硬编码 Token/userId
- 重复请求代码
- 明显死代码
- 未处理 Promise

如果 package scripts 提供 build/lint，可执行只读验证，但不要修。

## 十五、前后端接口匹配矩阵
报告必须包含：

| 前端功能 | 前端接口 | 后端接口 | 当前状态 |
|---|---|---|---|
| 登录 | ... | ... | MATCH/MISMATCH/MISSING |
| 活动分类 | ... | ... | ... |
| 活动详情 | ... | ... | ... |
| Ticket | ... | ... | ... |
| 限量预约 | ... | ... | ... |
| Blog | ... | ... | ... |
| Like | ... | ... | ... |
| Follow | ... | ... | ... |
| Feed | ... | ... | ... |

## 十六、页面完整度评分
对以下页面按 5 分制评分：
- 首页
- 活动列表
- 活动详情
- 预约
- 社区
- 登录
- 个人中心

三个维度：
- 功能完整度
- 视觉完成度
- CityHub 业务匹配度

必须给简短理由。

## 十七、问题优先级
按：
- P0：阻断运行 / 核心业务
- P1：Phase 5 必须修改
- P2：UI 优化
- P3：以后再做

分类。

## 十八、给出正式改造方向，但不要实现
基于真实审计结果分析：
1. 是否保留现有前端技术栈
2. 是否需要重搭前端
3. 是否适合继续使用现有 UI 组件库
4. 哪些页面必须重做
5. 哪些页面只需轻量美化
6. 哪些 API 要重新对齐
7. 哪些组件值得抽取
8. Phase 5 应分几步执行

仅分析，不编码。

## 十九、参考页面结构
只用于判断，不允许本阶段实现：

首页：
- 顶部导航
- 搜索
- 活动分类
- 热门活动
- 推荐活动

活动详情：
- 大图
- 标题
- 时间
- 地址
- 价格
- 介绍
- Ticket
- 预约按钮
- 相关体验动态

社区：
- 动态卡片
- 点赞
- 关注
- Feed

个人中心：
- 用户资料
- 我的预约
- 我的动态

## 二十、唯一报告要求
本阶段只生成一个报告：

`F:\JavaProject\YJSHZ-main\docs\PHASE5_FRONTEND_AUDIT_REPORT.md`

不要创建其他 audit / verification / refactor 子报告。

报告必须包含：
1. 审计结论
2. 前端目录与技术栈
3. 启动与构建
4. 路由与页面清单
5. API 层
6. 前后端接口匹配矩阵
7. UI 现状
8. CityHub 旧业务残留
9. 页面评分
10. P0/P1/P2/P3 问题清单
11. 可复用内容
12. Phase 5 正式改造建议

## 二十一、Git
本阶段是只读审计。

禁止：
`git add`
`git commit`
`git push`

如果运行命令产生 package-lock、缓存、日志或 build 文件变化，报告中说明，不提交。

## 二十二、最终回复格式
只输出：

Phase 5 前端现状审计完成。

1. 前端位置：
2. 技术栈：
3. 是否可运行：
4. 当前主要页面：
5. 当前 API 对齐情况：
6. UI 完成度：
7. 最大 P0/P1 问题：
8. 是否建议基于现有前端继续改：
9. 推荐下一步：

唯一报告：
F:\JavaProject\YJSHZ-main\docs\PHASE5_FRONTEND_AUDIT_REPORT.md

本阶段未修改任何业务代码，未提交 Git。

## 二十三、最终原则
本阶段只做一件事：

> 把当前 CityHub 前端真实状态看清楚。

不要急着换框架，不要急着重做，不要在审计阶段写 CSS。

先确认现有代码能保留多少、哪些页面最值得重做、哪些接口可以直接复用，再设计正式的 Phase 5 UI 包装方案。
