# Cloud Message Platform (云消息中台)

基于 Spring Boot WebFlux + R2DBC 构建的高性能、高并发云消息中台。提供统一的消息发送接口，支持多渠道（短信、邮件、Push等）路由、流量削峰、去重、敏感词过滤及动态负载均衡。

## 🚀 核心特性

*   **高性能架构**：全链路异步非阻塞设计 (WebFlux + Reactor + R2DBC)，单机吞吐量远超传统阻塞式架构。
*   **灵活接入**：
    *   支持单条/批量消息发送。
    *   **人群包 (Crowd Key)** 支持：高效处理百万级用户圈选发送，集成 RoaringBitmap 进行流式分批处理。
*   **责任链增强 (Enhance Layer)**：
    *   **模板引擎**：支持动态参数替换 (`{{variable}}`)。
    *   **去重机制**：基于 Redis 的滑动窗口去重，防止用户被骚扰。
    *   **敏感词过滤**：基于 Trie 树（前缀树）的高性能敏感词检测与拦截。
*   **智能发送 (Send Layer)**：
    *   **动态负载均衡**：实现 Nginx 平滑加权轮询算法 (Smooth Weighted Round Robin)，根据渠道权重和实时表现动态路由。
    *   **多渠道支持**：抽象 Channel 接口，易于扩展（已内置阿里云/腾讯云短信 Mock 实现）。
*   **可靠性保障**：
    *   **本地消息表**：确保消息不丢失，支持断点续传。
    *   **失败重试**：自动扫描失败消息进行指数退避重试。
    *   **死信队列**：多次重试失败后进入死信处理。
*   **全链路追踪**：集成 TraceId，贯穿接入、处理、发送、回调全流程。

## 🛠 技术栈

*   **核心框架**: Spring Boot 3.2, Spring WebFlux
*   **数据库**: MySQL 8.0 (R2DBC Driver)
*   **消息队列**: Apache Kafka (Reactor Kafka)
*   **缓存**: Redis (Reactive)
*   **工具**: RoaringBitmap (人群包处理), Hutool, FastJSON2, Lombok

## 📂 项目结构

```
com.lzk.cloudmsg
├── access          // 接入层：Controller, DTO
├── common          // 通用组件：Response, Trie树
├── config          // 配置类：Kafka等
├── domain          // 领域模型：MessageRecord, MessageTemplate
├── enhance         // 增强层：责任链模式实现 (去重, 敏感词, 模板)
├── infrastructure  // 基础设施：R2DBC Repository
├── send            // 发送层：Channel接口, 负载均衡, 消费者
└── task            // 定时任务：失败重试
```

## ⚡ 快速开始

### 前置要求
*   JDK 17+
*   MySQL 8.0+
*   Redis
*   Kafka

### 1. 数据库初始化
执行 `src/main/resources/schema.sql` 创建表结构。

### 2. 配置修改
修改 `src/main/resources/application.yml`，配置您的 DB、Redis 和 Kafka 连接信息。

### 3. 启动应用
```bash
mvn spring-boot:run
```

### 4. 接口测试

**发送批量消息**
```http
POST /api/v1/send/batch
Content-Type: application/json

{
  "templateId": 1,
  "receivers": ["13800138000", "13800138001"],
  "variables": {
    "code": "123456"
  }
}
```

**发送人群包消息**
```http
POST /api/v1/send/batch
Content-Type: application/json

{
  "templateId": 1,
  "crowdKey": "crowd_20231129_001",
  "variables": {
    "activity": "双11大促"
  }
}
```

## 📈 负载均衡策略

系统内置了 **平滑加权轮询 (Smooth Weighted Round Robin)** 算法。
*   默认配置：AliyunSMS (Weight: 5), TencentSMS (Weight: 3)。
*   系统会根据权重平滑分配流量，避免连续请求打到同一渠道。
*   支持通过回调接口 (`/api/v1/callback`) 动态调整渠道权重（逻辑已预留）。

## 🛡 敏感词过滤

系统启动时初始化 Trie 树。
*   默认敏感词：`bad`, `evil`, `gambling`。
*   检测到敏感词时，消息状态将被标记为 `2` (Fail)，FailReason 为 "Sensitive word detected"。

## 🔄 状态机

*   `0`: Pending (待发送/处理中)
*   `1`: Success (发送成功)
*   `2`: Fail (发送失败，可重试)
*   `-1`: Dead (死信，不再重试)
