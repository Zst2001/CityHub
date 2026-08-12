# YJSHZ 融合项目

本项目成功将consultant项目融合进backend项目中，采用模块化架构解决JDK版本差异问题。

## 项目结构

```
backend/
├── pom.xml                    # 父POM，管理多模块
├── core/                      # 核心模块 (JDK 8)
│   ├── pom.xml
│   ├── src/main/java/com/yjshz/ (业务代码)
│   └── src/main/resources/   (配置文件、数据库脚本等)
└── consultant/               # AI咨询模块 (JDK 17)
    ├── pom.xml
    ├── src/main/java/com/yjshz/consultant/ (AI功能代码)
    └── src/main/resources/   (AI相关配置)
```

## 技术栈差异

| 模块 | JDK版本 | Spring Boot | 主要功能 |
|------|---------|-------------|----------|
| core | JDK 8 | 2.7.18 | 基础业务功能、用户管理、商铺管理 |
| consultant | JDK 17 | 2.7.18 | AI对话服务、LangChain4J集成 |

## 运行方式

### 1. 启动AI服务（consultant模块）
```bash
cd backend/consultant
mvn spring-boot:run
```
AI服务将在端口8084启动

### 2. 启动核心服务（core模块）
```bash
cd backend/core
mvn spring-boot:run
```
核心服务将在端口8081启动

### 3. 测试AI服务调用
启动两个服务后，可以通过以下方式测试AI功能：

**检查AI服务状态：**
```
GET http://localhost:8081/ai/status
```

**与AI助手对话：**
```
GET http://localhost:8081/ai/chat?message=你好
```

### 4. 编译整个项目
```bash
cd backend
mvn clean compile
```

## 关键修改

1. **包名统一**: 将consultant的包名从`com.yjshz.consultant`改为`com.yjshz.consultant`
2. **模块依赖**: consultant模块依赖core模块，共享基础功能
3. **JDK配置**: consultant模块配置为使用JDK 17编译

## 端口配置

- core模块默认端口: 8080
- consultant模块默认端口: 8081（可在application.yml中修改）

## 数据库配置

两个模块共享相同的数据库配置，确保数据一致性。

## 注意事项

1. 需要安装JDK 8和JDK 17两个版本
2. 运行时需要根据模块选择对应的JDK版本
3. 两个模块可以独立部署，也可以一起部署
4. 模块间通过REST API进行通信（如果需要）

## 服务调用机制

core模块通过HTTP REST API调用consultant模块的AI服务：

1. **AI服务接口**: `IAiConsultantService`
2. **HTTP客户端**: 使用WebClient进行服务调用
3. **健康检查**: 通过Actuator端点监控AI服务状态
4. **错误处理**: 服务不可用时提供友好的错误提示

## 优势

- **解耦**: 核心业务和AI功能分离，便于维护
- **兼容**: 解决JDK 8和JDK 17的版本差异问题
- **灵活**: 可以独立部署或集成部署
- **可扩展**: 为未来添加更多模块奠定基础
- **服务化**: 通过HTTP API实现模块间通信，支持分布式部署