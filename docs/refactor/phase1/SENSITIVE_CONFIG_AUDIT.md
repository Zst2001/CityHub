# 敏感配置审计

## 扫描范围

扫描了 backend 的 YAML、Properties、POM、Java、Shell/Batch、Docker、Redis 配置和 SQL 中的 `password`、`api-key`、`secret`、`token`、`dashscope`、`mysql`、`redis`、`root` 等候选项；构建产物 `target/` 不作为源配置审计对象。

## 已发现并完成治理的配置

| 原配置文件 | 敏感字段类型 | 新环境变量 | 示例配置位置 |
|---|---|---|---|
| `backend/core/src/main/resources/application.yaml` | MySQL 密码 | `DB_PASSWORD` | `.env.example` |
| `backend/core/src/main/resources/application.yaml` | MySQL 用户名 | `DB_USERNAME` | `.env.example` |
| `backend/core/src/main/resources/application.yaml` | Redis 主机/端口/密码 | `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` | `.env.example` |
| `backend/consultant/src/main/resources/application.yml` | MySQL 密码 | `DB_PASSWORD` | `.env.example` |
| `backend/consultant/src/main/resources/application.yml` | MySQL 用户名 | `DB_USERNAME` | `.env.example` |
| `backend/consultant/src/main/resources/application.yml` | Redis 主机/端口/密码 | `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` | `.env.example` |
| `backend/consultant/src/main/resources/application.yml` | DashScope 模型密钥 | 既有 `ALIYUNCS_API_KEY` | `.env.example` |
| `backend/core/src/main/java/com/yjshz/config/RedissonConfig.java` | 硬编码 Redis 地址 | 读取 `spring.redis.*`，由上述环境变量提供 | `.env.example` |

数据库 URL 同时支持 `DB_URL` 覆盖；未设置时保留原有本地连接地址作为非敏感默认值。密码和 API Key 没有默认真实值。

## 当前扫描结论

- 当前工作区源 YAML 中不再包含本次发现的明文数据库密码。
- 未发现提交的 DashScope API Key；模型密钥原本已使用环境变量占位。
- `User.password`、`LoginFormDTO.password` 和 SQL 字段定义是业务字段/模式定义，不等同于仓库凭证，未修改。
- AI 请求/响应 debug 日志设置仍可能在运行时记录敏感用户内容；此为运行配置风险，保留供后续安全治理评估，本阶段未调整 AI 业务模块行为。

## Git 历史风险与凭证轮换

当前工作副本不包含 `.git` 目录，`git rev-parse` 返回非 Git 仓库，无法核查提交历史、tracked 文件或历史泄露情况。由于本次发现过明文数据库凭证，建议立即在对应数据库轮换该凭证，并在拥有完整 Git 仓库时单独审查历史；本阶段未重写 Git 历史、未执行强制推送。
