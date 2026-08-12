# Codex 执行任务：Phase 4 CityHub 社区业务轻量适配与运行验证

## 一、任务背景

项目：

> **CityHub - 城市活动发现与预约平台**

当前已经完成：

- Phase 1：基础工程治理；
- Phase 2：CityHub 工程身份规范化；
- Phase 3A-R：核心领域轻量迁移；
- Phase 3B：Activity 查询与 Redis 缓存真实验证；
- Phase 3C：限量活动预约 / 秒杀链路真实验证。

当前 CityHub Core 已经形成两条核心技术主线：

```text
Activity
↓
Redis Cache
├─ 缓存穿透
├─ 缓存击穿
├─ 缓存雪崩
└─ 缓存一致性
```

以及：

```text
Ticket
↓
SeckillTicket
↓
Lua
↓
一人一单
↓
RedisIdWorker
↓
ArrayBlockingQueue
↓
Redisson
↓
ReservationOrder
```

Phase 4 不再扩展缓存和秒杀。

本阶段目标：

> **将现有 Blog / Follow / Feed 轻量适配为 CityHub 的“活动体验社区”，保留现有 Redis ZSet / Set / Feed 技术实现，补充 Blog 与 Activity 的轻量关联，并完成真实运行验证。**

---

# 二、本阶段总体原则

## 1. 轻量适配，不大规模重命名

本阶段默认继续保留：

```text
Blog
BlogComments
Follow

BlogController
BlogService
BlogServiceImpl
FollowService
FollowServiceImpl
```

不要为了业务名称更漂亮而统一改成：

```text
Post
PostComment
PostService
PostController
```

原因：

- 当前目标是优先完成项目；
- Blog 本身不影响面试讲解；
- 强行重命名会牵涉 Controller / Service / Mapper / Redis Key / 前端 / AI；
- 收益低、风险高。

---

## 2. 业务语义改成“活动体验动态”

原：

```text
达人探店
Blog
```

现在统一解释为：

```text
活动体验动态
活动笔记
活动攻略
观展 / 演出 / 市集体验分享
```

代码类名可以继续叫 Blog。

---

## 3. 保留原有社区技术实现

以下方案原则上不改：

```text
Blog 点赞：
Redis ZSet

点赞用户排行：
ZRANGE / 等价 SortedSet 查询

关注：
tb_follow

共同关注：
Redis Set / 交集（若现有代码真实存在）

Feed：
Redis ZSet
推模式
时间戳 score
滚动分页
```

本阶段以验证和最小适配为主。

---

# 三、本阶段禁止事项

禁止：

```text
Blog -> Post 全量重命名
BlogComments -> PostComment 全量重命名

重新设计 Feed
重新设计点赞
重新设计 Follow

推荐算法
ElasticSearch
Kafka
RabbitMQ
Redis Stream

复杂评论系统
标签系统
话题系统
私信
消息通知

前端视觉重构
首页 UI 重做
活动详情 UI 重做

AI Tool 深度迁移
RAG
MCP

README 重写
Docker 扩展

缓存 / 秒杀核心逻辑修改
Lua
Redisson
BlockingQueue
RedisIdWorker
```

---

# 四、Git 基线检查

GitHub：

```text
https://github.com/Zst2001/CityHub
```

当前最新已知 Phase 3C：

```text
93f3f49 test: verify CityHub seckill reservation flow
```

开始前执行：

```bash
git status --short
git log --oneline -n 10
git branch --show-current
git remote -v
```

要求：

```text
working tree clean
main == origin/main
```

如果有未提交内容：

不要覆盖。

先分析来源。

如果不是本阶段内容，停止并报告。

---

# 五、任务 1：审计当前社区真实实现

修改前先完整阅读：

```text
Blog
BlogComments
Follow

BlogController
BlogService / IBlogService
BlogServiceImpl

FollowController
FollowService / IFollowService
FollowServiceImpl

BlogMapper
BlogCommentsMapper
FollowMapper

User
UserDTO
UserHolder
```

同时检查：

```text
RedisConstants
tb_blog
tb_blog_comments
tb_follow
```

以及：

```text
frontend 当前社区接口调用
```

---

# 六、生成社区审计文档

创建：

```text
docs/refactor/phase4/COMMUNITY_AUDIT.md
```

必须基于真实源码回答：

```text
1. Blog 发布 API 是什么？
2. Blog 详情 API 是什么？
3. Blog 热门查询 API 是什么？
4. Blog 点赞 / 取消点赞 API 是什么？
5. 点赞 Top 用户 API 是什么？
6. Follow / Unfollow API 是什么？
7. 共同关注是否真实存在？
8. Feed API 是什么？
9. Feed 当前是推模式还是拉模式？
10. Feed 使用什么 Redis 数据结构？
11. Feed score 是什么？
12. 是否支持滚动分页？
13. BlogComments 当前是否真正实现？
14. Blog 当前有哪些字段？
15. Blog 与 Activity 当前是否有关联？
16. 当前 Redis Blog / Feed Key 是什么？
```

不要凭黑马点评教程推测。

只写当前代码真实存在的功能。

---

# 七、任务 2：为 Blog 增加 activityId 轻量关联

本阶段唯一建议新增的社区业务字段：

```text
activityId
```

目标：

```text
Blog
↓
关联
Activity
```

表示：

> 这条活动体验动态属于哪个 Activity。

---

# 八、Blog Entity 修改

如果当前 Blog 中不存在：

```java
Long activityId;
```

则新增：

```java
private Long activityId;
```

命名风格与当前代码保持一致。

不要新增：

```text
activityTitle
activityCover
activityCategory
activitySnapshot
```

这些可查询 Activity 获取，不在 Blog 里冗余。

---

# 九、数据库修改

更新：

```text
backend/core/src/main/resources/db/cityhub_schema.sql
```

在：

```text
tb_blog
```

中增加：

```text
activity_id
```

第一版：

```text
BIGINT
```

建议允许 NULL。

原因：

- 兼容当前旧 Blog Seed；
- 不要求所有动态必须绑定活动；
- 降低迁移成本。

不要增加复杂外键约束。

---

# 十、索引

如果当前 Blog 后续需要按 activityId 查询，可以增加普通索引：

```text
idx_activity_id(activity_id)
```

但如果当前数据量很小，也可以不加。

不要做复杂索引优化。

---

# 十一、任务 3：发布 Blog 时支持 activityId

当前发布 Blog API 尽量保持不变。

如果客户端提交：

```json
{
  "title": "...",
  "content": "...",
  "images": "...",
  "activityId": 1
}
```

则保存。

如果 activityId 不为空：

建议检查：

```text
Activity 是否存在
```

不存在则返回业务失败。

不要实现：

```text
用户是否真的预约过活动
用户是否已参加活动
活动结束后才可发布
```

---

# 十二、任务 4：Activity 相关体验查询

建议增加简单查询：

```text
GET /blog/of/activity?activityId={id}&current={page}
```

或者根据当前 Controller 风格设计等价路由。

目标：

> 查询某个 Activity 下的活动体验 Blog。

使用：

```text
activity_id
```

过滤。

分页方式沿用现有 Blog 分页。

不要新建复杂 DTO 体系。

如果当前已经有可以复用的通用查询能力，不要重复造 API。

---

# 十三、任务 5：初始化数据改成活动社区语义

将 `cityhub_schema.sql` 中少量 Blog Seed 改成：

```text
城市青年创意市集体验分享
夏日爵士音乐会现场记录
当代摄影艺术展观展笔记
周末陶艺体验课体验
```

至少部分 Blog：

```text
activity_id
```

指向现有 Seed Activity。

不要造大量社区数据。

---

# 十四、任务 6：点赞逻辑保持原实现

不要重新设计。

先确认当前真实逻辑。

如果使用：

```text
Redis ZSet
```

保持：

```text
Blog liked
+
BLOG_LIKED_KEY
+
userId
+
timestamp score
```

验证：

```text
User A 点赞 Blog
↓
MySQL liked +1
Redis ZSet 出现 User A

User A 再次点赞
↓
取消点赞
MySQL liked -1
Redis ZSet 移除 User A
```

---

# 十五、点赞 Top 用户验证

如果当前存在：

```text
queryBlogLikes
```

或等价接口：

验证：

```text
多个用户点赞
↓
Top N 用户返回
```

顺序以当前实现为准。

不要重新设计排行榜。

---

# 十六、任务 7：Follow / Unfollow 保持

当前：

```text
tb_follow
```

继续使用。

验证：

```text
User A follow User B
↓
tb_follow +1

User A unfollow User B
↓
关系删除
```

---

# 十七、任务 8：共同关注

先检查当前代码是否真实实现：

```text
Redis Set
+
intersection
```

如果存在且可用：

验证：

```text
User A
User B
共同关注 User C
↓
共同关注接口返回 User C
```

如果不存在或当前实现明显不完整：

报告：

```text
N/A
```

不要为了本阶段新增复杂逻辑。

共同关注不是阻断项。

---

# 十八、任务 9：Feed 推送验证

这是 Phase 4 的重点之一。

当前如果是：

```text
推模式
+
Redis ZSet
```

则保持。

核心：

```text
User A 关注 User B

User B 发布 Blog
↓
查询 B 的粉丝
↓
将 Blog ID 推入 A 的 Feed ZSet
↓
score = timestamp

User A 查询 Feed
↓
能看到 B 的新 Blog
```

禁止：

```text
改成拉模式
改成推拉结合
Kafka Feed
推荐 Feed
复杂 fanout
```

---

# 十九、Feed 滚动分页

如果当前代码已经支持：

```text
max
offset
minTime
```

等滚动分页参数：

保留并验证一次。

不要求复杂边界测试。

---

# 二十、任务 10：BlogComments 处理策略

先审计真实实现。

如果评论已经完整：

可以做 Smoke Test：

```text
发表评论
查询评论
```

如果只是实体 / 表存在，业务不完整：

报告：

```text
当前 BlogComments 未形成完整业务链，Phase 4 未补充。
```

不作为阻断项。

---

# 二十一、任务 11：真实运行环境

优先复用 Phase 3B / 3C 的独立开发环境。

重新导入更新后的：

```text
cityhub_schema.sql
```

启动 CityHub Core。

确认：

```text
MySQL
Redis
Redisson
异步 Consumer
```

没有因为 Blog.activityId 新增而出现：

```text
字段不存在
Mapper 错误
JSON 错误
```

---

# 二十二、任务 12：真实社区集成测试

推荐新增：

```text
CommunityFlowIntegrationTest.java
```

核心依赖尽量使用真实：

```text
Spring Boot
MySQL
Redis
HTTP
登录 Token
```

不要全 Mock。

推荐测试链：

```text
User A
User B
Activity 1

1. User A 关注 User B

2. User B 发布 Blog：
   activityId = 1

3. 检查：
   tb_blog.activity_id = 1

4. User A 查询 Feed

5. 确认：
   Feed 中存在 User B 新发布 Blog

6. User A 点赞该 Blog

7. 检查：
   liked +1
   Redis ZSet 存在 User A

8. 查询点赞 Top 用户

9. User A 取消点赞

10. 检查：
    liked 恢复
    Redis ZSet 移除 User A

11. User A 取消关注 User B
```

一条完整链即可。

---

# 二十三、Activity 关联查询验证

如果新增：

```text
/blog/of/activity
```

验证：

```text
Activity 1
↓
Blog A / Blog B
```

只返回：

```text
activity_id = 1
```

的动态。

---

# 二十四、本阶段最低验证矩阵

创建：

```text
docs/refactor/phase4/COMMUNITY_VERIFICATION.md
```

记录：

| 场景 | 预期 | 实际 | 状态 |
|---|---|---|---|
| 发布活动动态 | 保存 activityId | ... | PASS/FAIL |
| Blog 详情 | 正常返回 | ... | PASS/FAIL |
| Activity 相关动态 | 正确过滤 | ... | PASS/FAIL |
| 点赞 | liked +1 / ZSet | ... | PASS/FAIL |
| 取消点赞 | liked -1 / ZSet 删除 | ... | PASS/FAIL |
| 点赞 Top 用户 | 正常 | ... | PASS/FAIL |
| Follow | 新增关系 | ... | PASS/FAIL |
| Unfollow | 删除关系 | ... | PASS/FAIL |
| Feed | 粉丝可看到新动态 | ... | PASS/FAIL |
| 共同关注 | ... | ... | PASS/FAIL/N/A |
| 评论 | ... | ... | PASS/FAIL/N/A |

---

# 二十五、本阶段允许的最小修复

允许：

```text
Blog.activityId
tb_blog.activity_id
Activity 关联查询 API
Blog Seed 数据修改

Blog / Follow / Feed 当前真实 Bug
Redis Key 迁移遗漏
Mapper / SQL 小错误
Feed / Like 运行阻断问题
测试辅助
少量必要日志
```

---

# 二十六、本阶段禁止的修改

禁止：

```text
Blog 全量重命名 Post
Follow 架构重写
Feed 架构重写
点赞算法重写

秒杀
缓存
RedisIdWorker
Lua
BlockingQueue
Redisson

Activity / Ticket 大改

AI

前端视觉

README
```

---

# 二十七、前端处理边界

Phase 4 只允许：

> 因后端接口变化而做的最小 API 兼容。

例如新增：

```text
activityId
```

请求字段。

不要开始：

```text
社区页面重画
CSS 美化
导航重构
首页重构
```

这些统一放 Phase 5。

---

# 二十八、AI 处理边界

CityHub AI 当前如果仍有：

```text
Shop
Voucher
```

旧语义：

继续冻结。

不要在 Phase 4 修改。

---

# 二十九、Maven 验证

完成后：

```bash
cd backend
mvn clean compile
```

必须：

```text
CityHub
CityHub Core
CityHub AI
BUILD SUCCESS
```

---

# 三十、集成测试

执行：

```bash
mvn -pl core test -Dtest=CommunityFlowIntegrationTest
```

如果环境需要之前已验证的 Java 17 参数：

继续使用：

```text
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED
```

不要升级依赖。

---

# 三十一、Spring 最终验证

最终再次启动 Core。

确认：

```text
Activity
Blog
Follow
Feed
Redis
MySQL
```

没有启动错误。

---

# 三十二、Git 检查

完成后：

```bash
git status --short
git diff --check
```

确认没有：

```text
.env
真实 DB 密码
Redis 密码
API Key
测试 Token
target
logs
临时 DB 文件
```

---

# 三十三、Git commit

执行：

```bash
git add .
git diff --cached --check
git diff --cached --stat
```

通过后：

```bash
git commit -m "feat: adapt CityHub activity community flow"
```

---

# 三十四、Git push

确认：

```bash
git branch --show-current
git remote -v
```

然后：

```bash
git push
```

禁止 force push。

---

# 三十五、本阶段交付文件

创建：

```text
docs/refactor/phase4/
```

生成：

```text
PHASE4_REPORT.md
COMMUNITY_AUDIT.md
COMMUNITY_VERIFICATION.md
```

如新增：

```text
CommunityFlowIntegrationTest.java
```

---

# 三十六、PHASE4_REPORT.md 必须包含

## 1. 阶段结论

```text
Phase 4 是否通过
```

## 2. Blog 当前业务定位

说明：

```text
Blog 类名是否保留
业务是否改为活动体验动态
```

## 3. Activity 关联

说明：

```text
activityId
数据库字段
API
```

## 4. 发布动态

真实验证结果。

## 5. 点赞

说明：

```text
Redis 数据结构
点赞
取消点赞
Top 用户
```

## 6. Follow

说明：

```text
Follow
Unfollow
```

## 7. 共同关注

```text
PASS / N/A
```

据实填写。

## 8. Feed

说明：

```text
推模式
Redis ZSet
score
滚动分页
真实验证
```

## 9. BlogComments

说明：

```text
完整 / 部分 / 未实现
```

不要虚报。

## 10. 工程验证

```text
mvn clean compile
CommunityFlowIntegrationTest
Spring startup
```

## 11. Git

记录：

```text
Phase 4 commit
commit hash
branch
push
```

## 12. 未修改

明确：

```text
秒杀
缓存
AI
前端视觉
README
```

---

# 三十七、Phase 4 验收标准

核心必须满足：

```text
Blog 保持原类名或当前实现，不做大规模重命名

Blog.activityId：
真实保存

Activity 相关动态查询：
可用

点赞：
真实可用

取消点赞：
真实可用

点赞 Top 用户：
现有能力可用

Follow：
可用

Unfollow：
可用

Feed：
关注用户能看到被关注者新发布的 Activity Blog

Redis Feed / Like：
真实使用

Maven：
BUILD SUCCESS

Integration Test：
PASS

Spring：
启动成功

Git：
commit + push
```

---

# 三十八、不作为阻断项

以下当前不要求：

```text
共同关注（若现有代码不完整）
复杂评论
推荐 Feed
搜索社区
话题
标签
消息通知
私信
Post 全量重命名
前端美化
AI
```

---

# 三十九、下一阶段

Phase 4 完成后进入：

> **Phase 5：CityHub 前端重构与 UI 美化**

重点页面：

```text
首页
活动详情
限量预约
活动社区
个人中心
```

Phase 4 不提前实施 Phase 5。

---

# 四十、最终回复格式

完成后输出：

```text
Phase 4 CityHub 社区业务轻量适配完成。

【Git 基线】
1. Phase 3C commit：
2. main / origin：

【社区审计】
3. Blog：
4. BlogComments：
5. Follow：
6. Feed：
7. 共同关注：

【Activity 关联】
8. activityId：
9. Activity 相关动态 API：

【运行验证】
10. 发布动态：
11. Blog 详情：
12. Activity 相关动态：
13. 点赞：
14. 取消点赞：
15. 点赞 Top 用户：
16. Follow：
17. Unfollow：
18. Feed：
19. 共同关注：
20. 评论：

【工程】
21. Maven：
22. Integration Test：
23. Spring：

【Git Phase 4】
24. commit：
25. commit hash：
26. branch：
27. push：

【未修改】
28. Redis 缓存：
29. 秒杀：
30. AI：
31. 前端视觉：
32. README：

详细报告：
docs/refactor/phase4/PHASE4_REPORT.md
```

---

# 四十一、最终原则

Phase 4 的目标不是把 CityHub 变成复杂社区平台。

目标是：

> **最大程度复用现有 Blog + Follow + Redis ZSet / Set + Feed 实现，将其自然解释成“活动体验社区”，并通过 activityId 与 Activity 建立最小但真实的业务联系。**

优先完成项目。

不要为了代码名字更漂亮制造新的重构工作。
