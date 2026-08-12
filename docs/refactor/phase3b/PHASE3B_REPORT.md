# Phase 3B：CityHub 活动发现与查询链路报告

## 1. 阶段结论

CityHub 新领域的只读发现链路已完成：活动分类、场馆列表/详情、活动联表分页、活动联表详情和活动场次查询均已实现。新增实现严格停留在 Controller → Service → MySQL 查询层；没有预约写入、名额扣减、缓存或异步链路。

本阶段按用户要求只生成本文一份报告。

## 2. 新增 Controller

| 文件 | 接口 | 职责 |
| --- | --- | --- |
| `ActivityCategoryController` | `GET /activity-categories` | 返回有效活动分类，按 `sort ASC, id ASC` 排序 |
| `VenueController` | `GET /venues` | 按可选 `city` 返回有效场馆 |
| `VenueController` | `GET /venues/{id}` | 返回单个有效场馆；不存在或禁用时返回业务失败结果 |
| `ActivityController` | `GET /activities` | 以分页返回已发布活动，支持分类、场馆、城市筛选 |
| `ActivityController` | `GET /activities/{id}` | 返回已发布活动、分类和场馆组成的详情 VO |
| `ActivityController` | `GET /activities/{id}/sessions` | 返回活动的可展示未来场次 |

所有接口均复用既有 `com.cityhub.dto.Result`，没有直接向客户端返回 Entity。

## 3. 新增 Service

| 接口 | 实现 | 职责 |
| --- | --- | --- |
| `IActivityCategoryService` | `ActivityCategoryServiceImpl` | 查询有效分类并映射分类 VO |
| `IVenueService` | `VenueServiceImpl` | 查询有效场馆列表和详情并映射场馆 VO |
| `IActivityService` | `ActivityServiceImpl` | 调用 Mapper 的已发布活动联表分页/详情查询 |
| `IActivitySessionService` | `ActivitySessionServiceImpl` | 查询活动下可展示的未来场次并映射场次 VO |

以上服务均未注入 `StringRedisTemplate`、缓存客户端或消息组件，也没有事务、写入和预约相关方法。

## 4. DTO / VO

| 对象 | 用途 |
| --- | --- |
| `ActivityPageQuery` | 接收 `page`、`size`、`categoryId`、`venueId`、`city`；空/非法页码归一为 1，空/非法大小归一为 10，最大大小截断为 50 |
| `ActivityCategoryVO` | 分类列表仅输出 `id`、`name`、`icon`、`sort` |
| `VenueVO` | 场馆列表/详情输出公开场馆信息 |
| `ActivityListVO` | 列表输出活动概览、分类名称和场馆概览，不含描述全文或内部状态字段 |
| `ActivityDetailVO` | 输出活动基础信息及嵌套的分类、场馆信息，不包含场次 |
| `ActivitySessionVO` | 输出场次 ID、活动 ID、时间窗口、场次名额和状态 |

## 5. Activity 查询设计

`ActivityMapper.selectPublishedPage` 通过一条参数化联表 SQL 查询：

```text
activity a
INNER JOIN venue v ON v.id = a.venue_id AND v.status = 1
INNER JOIN activity_category c ON c.id = a.category_id AND c.status = 1
WHERE a.status = 1
```

- 分页：复用现有 MyBatis-Plus `PaginationInnerInterceptor`，由 `Page<ActivityListVO>` 执行分页和计数。
- 筛选：可选 `categoryId`、`venueId`、`city` 均使用 `#{...}` 参数绑定，不拼接用户输入 SQL。
- 排序：固定为 `a.publish_time DESC, a.id DESC`，不向客户端开放排序字段。
- N+1：不存在。列表的活动、场馆、分类数据均由上述一次联表查询取得；没有在循环中调用 `selectById`。

## 6. Activity Detail

`ActivityMapper.selectPublishedDetailById` 同样以一条 `activity + venue + activity_category` 的 `INNER JOIN` 查询已发布活动，并同时要求关联场馆和分类均为有效状态。查询结果映射为 `ActivityDetailVO`：

- `category`：`id`、`name`；
- `venue`：`id`、`name`、`city`、`district`、`address`。

详情接口没有内嵌场次列表，场次由专用子资源接口读取。

## 7. Session 查询

场次使用 MyBatis-Plus 参数化条件查询，规则如下：

- 仅查询指定 `activity_id`；
- 仅展示 `start_time >= 当前应用时间` 的未来场次；
- 排除 `status IN (3 cancelled, 4 finished)`；
- 按 `start_time ASC, id ASC` 排序；
- 不判断当前用户资格、是否已预约，也不读取/扣减库存。

`GET /activities/{id}/sessions` 会先确认活动是可公开浏览的已发布活动，之后才返回其场次。

## 8. 公开访问配置

`MvcConfig` 的既有 `LoginInterceptor` 新增以下排除路径，使六个发现接口可被未登录用户访问：

```text
/activity-categories
/venues
/venues/*
/activities
/activities/*
/activities/*/sessions
```

项目现有拦截器仅按路径匹配，无法在该配置点区分 HTTP 方法。因此这些路径在当前仅含 GET 发现接口的阶段被排除；未来新增活动写接口前，必须收紧为按方法/权限控制，不能把写操作置于这些公开路径下。刷新令牌拦截器仍覆盖全部路径，仅在有令牌时填充用户上下文。

## 9. Seed 数据

新增 `backend/core/src/main/resources/db/cityhub_domain_seed.sql`：

| 数据 | 数量 |
| --- | ---: |
| ActivityCategory | 5 |
| Venue | 3 |
| 已发布 Activity | 9 |
| 未来 ActivitySession | 18 |
| User / ReservationOrder | 0 |

脚本使用固定的专用 ID 和 `INSERT IGNORE`，便于在已存在相同 ID 的开发库中重复执行；不包含旧表清理、用户插入或预约订单插入。

## 10. 数据库执行结果

**未验证。** 本次未实际连接或执行数据库，原因是仓库配置中没有可确认安全写入的目标 MySQL 实例，不能虚报 DDL/Seed 已落地。

需要由使用者在目标开发数据库中按以下顺序手工执行：

```text
backend/core/src/main/resources/db/cityhub_domain_v1.sql
backend/core/src/main/resources/db/cityhub_domain_seed.sql
```

执行完成后，再启动 Core 服务进行查询接口验证。执行前应确认连接目标是开发库，而不是生产库。

## 11. API Smoke Test

**未验证。** 未启动应用或调用 HTTP 接口；原因同上，CityHub 查询表和 Seed 未实际部署到可确认安全的数据库，且应用启动还依赖既有 Redis 刷新令牌拦截器环境。

| API | 是否验证 | 结果 |
| --- | --- | --- |
| `GET /activity-categories` | 未验证 | 等待 DDL/Seed 落库后验证 |
| `GET /venues?city=上海` | 未验证 | 等待 DDL/Seed 落库后验证 |
| `GET /venues/200001` | 未验证 | 等待 DDL/Seed 落库后验证 |
| `GET /activities?page=1&size=10` | 未验证 | 等待 DDL/Seed 落库后验证 |
| `GET /activities?categoryId=100001&venueId=200001&city=上海` | 未验证 | 等待 DDL/Seed 落库后验证 |
| `GET /activities/300001` | 未验证 | 等待 DDL/Seed 落库后验证 |
| `GET /activities/300001/sessions` | 未验证 | 等待 DDL/Seed 落库后验证 |
| 不存在的场馆/活动 ID、非法页码与大小 | 未验证 | 等待运行环境后验证 |

已完成的静态检查：六个 GET 映射存在；活动列表/详情 SQL 含关联场馆和分类的 `INNER JOIN`；活动筛选均为 MyBatis 参数绑定；新增查询代码中未发现 Redis、Kafka、Lua、预约订单或 POST/PUT/DELETE 映射。

## 12. Maven Compile

| 命令 | 结果 |
| --- | --- |
| `cd backend && mvn clean compile` | `BUILD SUCCESS`；CityHub、CityHub Core、CityHub AI 全部 SUCCESS |

编译仍报告既有 `ShopTypeServiceImpl` 的未检查操作警告，本阶段未修改该旧业务。

## 13. 旧业务影响

| 项目 | 是否修改 |
| --- | --- |
| Shop | 否 |
| Voucher | 否 |
| 旧秒杀 | 否 |
| Blog / Follow | 否 |
| AI | 否 |
| README | 否 |

唯一跨新旧边界的修改是既有 `MvcConfig` 的公开路径白名单；影响范围仅为当前 CityHub 六个 GET 查询路径的登录校验排除，详见“公开访问配置”。

## 14. 当前遗留问题

- DDL、Seed 和 API 尚未在真实运行环境中验证，不能证明数据库方言、连接信息或 HTTP 行为已通过。
- 项目全局 Redis 刷新令牌拦截器仍是服务启动/全链路运行的外部依赖；本阶段未改动它。
- 现有 `ShopTypeServiceImpl` 存在编译器未检查操作警告，属于旧业务遗留，未在本阶段处理。
- 当前公开路径策略只能按路径匹配；在新增 CityHub 写接口之前，需要改为更细粒度的鉴权策略。
- 当前仓库没有 `.git` 元数据，无法执行 `git diff` 或提交级别的变更核验。

## 15. 下一阶段建议

仅建议进入 **Phase 3C：MySQL 基线预约链路**，在此基础上实现创建预约、我的预约、取消/重新预约、场次名额扣减与恢复、事务边界及数据库唯一约束冲突处理。不要在 Phase 3C 之前提前引入 Redis、Lua、MQ 或支付能力。
