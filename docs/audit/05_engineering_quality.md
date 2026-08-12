# 05 工程质量审计

## 分层与接口

core 具有 Controller → Service → Mapper 基本分层，使用 `Result` 统一响应，并有 `WebExceptionAdvice` 捕获 `RuntimeException`。但大量接口直接接收/返回 Entity（如 `ShopController.saveShop/updateShop`、`BlogController.saveBlog`、`VoucherController`），缺少请求 DTO、响应 VO 和 Bean Validation；全局异常只覆盖 RuntimeException。

接口封装也不一致：`ShopServiceImpl.queryById()` 已返回 `Result`，而 `ShopController.queryShopById()` 又执行 `Result.ok(shopService.queryById(id))`，会产生嵌套的 Result 响应；其他 Controller 多数直接返回 Service Result。

consultant 重复定义 `Shop`、`Voucher`、`VoucherOrder` POJO，直接连接同一库，而不是复用 core 契约或稳定 API。这与 backend README 的“模块通过 REST API 通信”不符。

## 安全与配置

严重工程风险：两个 YAML 将 MySQL root 密码明文提交：`core/.../application.yaml` 和 `consultant/.../application.yml`。Redis、Redisson 地址也硬编码 127.0.0.1。DashScope API key 使用环境变量，未发现明文 API Key；但 `log-requests: true`、`log-responses: true` 与 `dev.langchain4j: debug` 可能记录用户内容和模型交互，生产环境有隐私风险。

`UploadController` 把文件写入相对工作目录推导的前端目录；删除接口是 GET 方法。权限层面 `/voucher/**`、`/upload/**` 被排除登录拦截，新增券/上传接口无角色鉴权。

## 并发、事务与一致性

- `VoucherOrderServiceImpl` 用 JVM 内存队列和静态单线程执行器；无生命周期管理、消息持久化、重试或可观测性。
- `proxy` 是 Service 单例可变字段，多个并发秒杀请求会互相覆盖；消费者可能在 `proxy` 赋值前取到订单，存在空指针竞态。
- Redisson `tryLock()` 失败后不 return，finally 又无条件 `unlock()`；同一订单流程错误处理后继续落库。
- Redis Lua 预扣与 MySQL 扣减/下单没有可靠补偿，JVM 队列丢失会永久不一致。
- `ShopServiceImpl.update()` 的 DB 后删缓存是常规 cache-aside 方向，但无消息/重试保障。
- `SimpleRedisLock.tryLock(timeoutSec)` 将参数以 `TimeUnit.MINUTES` 使用，命名为秒；此外它目前没有业务调用。`trylock.lua` 未被加载。

## 测试、构建与交付

没有 `src/test` 代码。根编译在本环境因无用 CORBA import 失败。Docker compose 仅提供 Redis Stack；不含 MySQL、core、consultant、frontend。`frontend` 含 Nginx 可执行文件、pid 和 access/error logs，缺少可复现的前端源构建工程；其 Nginx 配置还指定 `root html/yjshz`，但该目录未随当前仓库提供。已提交日志暴露历史请求路径。`.git` 不在当前工作副本，无法审计提交历史。

工程质量结论：适合作为学习代码底座，不满足直接投递的工程质量要求；尤其应先处理编译、密钥、数据模型和可靠性问题。
