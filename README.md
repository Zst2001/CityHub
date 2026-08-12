# 雅鉴生活志 - 本地生活服务平台

## 项目简介
雅鉴生活志是一个集本地生活服务、社交分享和AI智能咨询于一体的综合性平台。项目采用前后端分离架构，为用户提供优质的本地生活体验。

## 后端架构
- **核心模块** (core): Spring Boot 2.7.18 + JDK 8
- **AI咨询模块** (consultant): Spring Boot 2.7.18 + JDK 17

## 技术栈
- **后端框架**：SpringBoot + MybatisPlus
- **数据库**：MySQL + Redis
- **中间件**：Lua + Kafka
- **缓存系统**：Caffeine
- **AI集成**：LangChain4j + 阿里云百炼平台大模型
- **其他技术**：JWT + WebFlux


## 🔥 核心业务功能

### 1. 秒杀系统
- **功能特点**：
  - 高并发秒杀场景支持
  - 精确库存控制
  - 用户限购机制

- **技术实现**：
  ```java
  // 分布式锁实现
  public Result seckill(Long voucherId) {
      // 1. Lua脚本原子扣减库存
      String script = "if redis.call('get', KEYS[1]) >= ARGV[1] then " +
                     "return redis.call('decrby', KEYS[1], ARGV[1]) " +
                     "else return -1 end";
      // 2. Kafka异步创建订单
      kafkaTemplate.send("order-topic", orderDTO);
  }
  ```

### 2. 订单系统
- **功能特点**：
  - 订单状态全生命周期管理
  - 自动关单和库存回滚
  - 支付并发安全

- **技术实现**：
  ```java
  // 订单状态机示例
  public enum OrderStatus {
      UNPAID, PAID, CANCELLED, COMPLETED
  }
  
  // 乐观锁控制支付并发
  @Transactional
  public Result pay(Long orderId) {
      int updated = orderMapper.updateStatus(
          orderId, UNPAID, PAID); // 原子状态变更
      if (updated == 0) {
          throw new ConcurrentPayException();
      }
  }
  ```
  ```sql
  UPDATE orders 
  SET status = 'PAID' 
  WHERE id = ? AND status = 'UNPAID'
  ```

### 3. 缓存体系
- **多级缓存架构**：
  ```
  请求 → Caffeine → Redis → DB
  ```
- **热点Key解决方案**：
  - 逻辑过期时间
  - 空值缓存
  - 互斥锁

### 4. 限流系统
- **滑动窗口算法**：
  ```lua
  -- Redis Lua实现
  local count = redis.call('INCR', KEYS[1])
  if count == 1 then
      redis.call('EXPIRE', KEYS[1], ARGV[1])
  end
  return count
  ```
## 🤖 AI智能咨询功能

### 功能特点

- **流式对话**: 支持实时流式响应
- **记忆管理**: 基于memoryId的对话上下文管理

### 技术实现

- **LangChain4J**: AI服务框架集成
- **WebFlux**: 响应式流式处理
- **Redis**: 对话记忆存储
- **通义千问**: 大语言模型支持

### 智能客服功能演示

![智能客服](https://github.com/liangchaaaaa/YJSHZ/blob/main/docs/AiConsultant.gif)


