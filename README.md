# 旧书坊（JiuShuFang）· Spring Cloud 微服务版

> 技术底座：Spring Boot 3.5.5 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0 + Spring AI 1.1.2 + Vue 3。

---

## 1. 项目定位

覆盖「浏览选购 → 购物车 → 下单支付 → 评价」完整交易闭环，并内置基于大模型的 **AI 购书助手**（多 Agent 路由 + RAG 向量检索 + 对话记忆）。

本仓库当前处于 **绞杀者（Strangler Fig）迁移模式**：

- 单体代码（`JiuShuFangSecondHand/src/main/java/com/JiuShuFangSecondHand/`）**保留不动**，作为对照与回滚基线；
- 微服务代码在 `JiuShuFangSecondHand/cloud-*` 下**并行存在**，与单体共用同一个 MySQL 库 `zhi-yue`；
- 前端只改请求头鉴权方式，其余路径不变，通过网关统一入口访问。

---

## 2. 架构总览

```
                    ┌─────────────────────────────┐
   浏览器 :3000 ───▶ │  Vue 3 + Vite  dev server    │
                    │  代理 /api ──▶ :8080         │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │   cloud-gateway  :8080      │
                    │   路由 / 跨域 / JWT 鉴权     │
                    │   限流(Redis) / 熔断(Resilience4j) │
                    └──────────────┬──────────────┘
                                   │ lb://  (Nacos 服务发现)
        ┌──────────────┬───────────┼───────────┬──────────────┐
        ▼              ▼           ▼           ▼              ▼
   cloud-user     cloud-goods  cloud-trade  cloud-ai     cloud-common
     :8081          :8082        :8083       :8089      （公共 jar）
   用户/验证码      商品域       交易域        AI 域
        │              │           │           │
        └──────────────┴───────────┴───────────┘
                       │
        MySQL(3308) Redis(6379) RabbitMQ(5672) MinIO(9000) Milvus(19530)
```

---

## 3. 模块与端口

| 模块 | 端口 | 职责 | 关键 Controller 前缀 |
|---|---|---|---|
| `cloud-common` | — | 公共 jar：`Result`/`Ret`/`BusinessException`/`GlobalExceptionHandler`/`PageDTO`/`UserContext`/`JwtUtil`/`IRedisUtils`/`MyBatisConfig`/`GoodsApi`(Feign) | — |
| `cloud-gateway` | 8080 | 统一入口：路由、跨域、JWT 校验、限流、熔断降级 | `/fallback/trade` |
| `cloud-user` | 8081 | 用户、登录、短信/图形验证码、微信扫码 | `/user`、`/code` |
| `cloud-goods` | 8082 | 商品、分类、收藏、评论、浏览历史、店铺、购物车、图片 | `/goods`、`/category`、`/collect`、`/comment`、`/history`、`/shop`、`/shopping-cart`、`/image` |
| `cloud-trade` | 8083 | 订单、订单项、地址、支付、支付单/流水/退款、秒杀 | `/order`、`/order-item`、`/address`、`/pay`、`/pay-order`、`/pay-record`、`/pay-refund`、`/seckill` |
| `cloud-ai` | 8089 | AI 对话、会话、消息、对话记忆、验证码、向量化 | `/chat`、`/chat/session`、`/chat/message`、`/chat-list`、`/chat-memory`、`/capture`、`/embedding` |

> 所有业务服务**不设 `context-path`**（早期版本设过 `/goods`、`/trade`，会让路径多一层，已移除）。

---

## 4. 网关路由表（前端路径 → 后端）

前端 [request.js](file:///d:/Systems/computer/JiuShuFang/JiuShuFangFrontend/request.js) 会给所有路径自动补 `/api` 前缀。网关按**业务前缀**分流，`StripPrefix=1` 去掉 `/api`，剩下的路径正好等于 Controller 的 `@RequestMapping`。

| 前端请求 | 网关路由 id | 转发到 | 去掉 `/api` 后 |
|---|---|---|---|
| `/api/user/login/**`、`/api/code/**` | `cloud-user-login` | `lb://cloud-user` | `/user/login/**`、`/code/**` |
| `/api/user/**` | `cloud-user` | `lb://cloud-user` | `/user/**` |
| `/api/goods/**`、`/api/category/**`、`/api/collect/**`、`/api/comment/**`、`/api/history/**`、`/api/image/**`、`/api/shop/**`、`/api/shopping-cart/**` | `cloud-goods` | `lb://cloud-goods` | 同名路径 |
| `/api/order/**`、`/api/order-item/**`、`/api/address/**`、`/api/pay/**`、`/api/pay-order/**`、`/api/pay-record/**`、`/api/pay-refund/**`、`/api/seckill/**` | `cloud-trade` | `lb://cloud-trade` | 同名路径 |
| `/api/ai/**`、`/api/capture/**`、`/api/chat/**`、`/api/chat-list/**`、`/api/chat-memory/**`、`/api/embedding/**` | `cloud-ai` | `lb://cloud-ai` | 同名路径 |

过滤器：

- 全局：`AddResponseHeader=X-Served-By, JiuShuFang-Gateway`、全局 CORS；
- `cloud-user-login`：按 IP 限流（`ipKeyResolver`，2 QPS / 突发 5）——防撞库；
- `cloud-user`：按用户限流（`userKeyResolver`，20 / 40）；
- `cloud-ai`：按用户限流（5 / 10）；
- `cloud-trade`：挂 Resilience4j 熔断器 `tradeCB`，失败率 50% 打开 10s，降级到 `/fallback/trade`；
- 另开启 `discovery.locator`，服务名也可直接作为路径前缀（如 `/cloud-ai/**`）。

> Spring Cloud Gateway 4.3 起配置前缀已迁移为 `spring.cloud.gateway.server.webflux.*`，旧的 `spring.cloud.gateway.*` 虽仍兼容但启动会打印 migrator 警告。

---

## 5. 鉴权与用户上下文

```
前端 localStorage.userInfo.token
   │  Authorization: Bearer <token>
   ▼
cloud-gateway  JwtAuthGlobalFilter (order = -100)
   │  白名单直接放行；否则验签
   │  解出 claim（兼容 "userId" 与单体遗留的 "id"）
   ▼
  请求头注入 X-User-Id
   ▼
各业务服务  UserContext(ThreadLocal) 读取 X-User-Id → 业务层拿当前用户
```

- **白名单**（[application.yaml](file:///d:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/cloud-gateway/src/main/resources/application.yaml#L139-L140)）：`/api/code/**`、`/api/capture/**`、`/api/user/login/**`、`/api/user/wx/**`、`/api/user/check`、`/api/goods/list`、`/api/category/**`、`/error`；
- 完整白名单（含 `/actuator/**`、`/v3/api-docs/**`、`/doc.html`、`/webjars/**`）放在被 git 忽略的 `application-local.yaml`；
- 网关与各服务的 `jwt.secret` **必须一致**，否则验签失败。

---

## 6. 技术栈

**后端**

| 技术 | 用途 |
|---|---|
| Spring Boot 3.5.5 / Java 17 | 基础框架（注意：`jakarta.validation`，非 `javax`） |
| Spring Cloud 2025.0.0 | Gateway、LoadBalancer、OpenFeign、Resilience4j |
| Spring Cloud Alibaba 2025.0.0.0 | Nacos 注册中心 + 配置中心 |
| MyBatis-Plus 3.5.5 | 持久化（乐观锁、逻辑删除） |
| Spring AI 1.1.2 + 阿里云百炼 | 多 Agent、Function Calling、向量化 |
| Milvus 2.4 | 商品向量库（RAG 语义找书） |
| Redis 7 | 缓存、对话记忆、限流令牌桶、秒杀 Lua 扣减 |
| RabbitMQ 3 | 聊天落库、商品向量增删同步 |
| MinIO | 图片对象存储（bucket `zhiyue`） |
| Knife4j 4.5 | 接口文档 |

**前端**：Vue 3（`<script setup>`）· Element Plus · Vue Router · 原生 fetch 封装 · WebSocket · GSAP

---

## 7. 目录结构

```
JiuShuFang/
├── README.md                      # 本文档
├── DEPLOY.md                      # 部署说明
├── JiuShuFangFrontend/            # Vue 3 前端（:3000）
│   ├── request.js                 # 统一请求层（自动加 /api、带 Bearer token、401 跳登录）
│   └── vite.config.js             # 开发代理 → :8080
└── JiuShuFangSecondHand/
    ├── pom.xml                    # 父 POM（JiuShuFangCloud）
    ├── src/                       # 单体（绞杀者基线，保留不动）
    ├── cloud-common/              # 公共模块（jar）
    ├── cloud-gateway/             # 网关 :8080
    │   └── src/main/java/com/JiuShuFang/gateway/
    │       ├── filter/JwtAuthGlobalFilter.java
    │       ├── config/            # KeyResolver、CORS、限流
    │       └── controller/FallbackController.java
    ├── cloud-user/                # :8081  com.JiuShuFang.user
    ├── cloud-goods/               # :8082  com.JiuShuFang.goods
    ├── cloud-trade/               # :8083  com.JiuShuFang.trade
    └── cloud-ai/                  # :8089  com.JiuShuFang.ai
```

包命名规则：`com.JiuShuFangSecondHand` → `com.JiuShuFang.<service>`；公共类统一来自 `com.JiuShuFang.common.*`。

---

## 8. 本地快速启动

完整步骤见 [DEPLOY.md](file:///d:/Systems/computer/JiuShuFang/DEPLOY.md)。最短路径：

```bash
# 0. 前提：MySQL(3308) / Redis(6379) / RabbitMQ(5672) / Nacos(8848) / MinIO(9000) / Milvus(19530) 已启动
#    （不需要 Elastiscearch，微服务版未启用 ES）

# 1. 初始化数据库（仅首次）
cd JiuShuFangSecondHand
docker exec -i mysql8 mysql -uroot -p123456 < src/main/resources/init.sql

# 2. 安装公共模块（必须先装，否则子模块找不到 cloud-common）
mvn clean install -DskipTests -pl cloud-common -am

# 3. 按顺序启动（每个开一个终端 / IDEA 各跑一次）
mvn spring-boot:run -pl cloud-user       # :8081
mvn spring-boot:run -pl cloud-goods      # :8082
mvn spring-boot:run -pl cloud-trade      # :8083
mvn spring-boot:run -pl cloud-ai         # :8089
mvn spring-boot:run -pl cloud-gateway    # :8080（最后启动，需先发现到上面 4 个服务）

# 4. 前端
cd ../JiuShuFangFrontend
npm install && npm run dev               # :3000
```

访问 http://localhost:3000 。

---

## 9. 关键设计说明

### 9.1 跨域数据访问（共享库只读）

绞杀者阶段各服务共用 `zhi-yue` 库。为减少服务间耦合，部分**跨域只读数据**直接复制 POJO + Mapper 到调用方：

- `cloud-goods`：内置 `Order`/`OrderItem`/`User` 的只读 Mapper（商品详情要展示订单数、卖家信息）；
- `cloud-trade`：内置 `Goods`/`Category`/`ShoppingCart`/`ChatMemory`/`User` 的只读 Mapper。

写操作永远只在自己的域内进行。后续可替换为 OpenFeign（`cloud-common` 已提供 `GoodsApi` 示例）。

### 9.2 消息与异步

- 商品上/下架 → 发 MQ → `cloud-ai` 的 `GoodsToVectorStoreListener` 消费 → 写/删 Milvus 向量；
- 聊天消息 → 发 MQ → 落库 `chat_memory`；
- 订单创建 → `OrderListener`；秒杀订单 → `SeckillOrderListener` + `SeckillReconcileSchedule` 对账。

### 9.3 秒杀

`cloud-trade` 用 Redis Lua（[seckill_deduct.lua](file:///d:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/cloud-trade/src/main/resources/scripts/seckill_deduct.lua)）原子扣库存，`RedisScriptConfig` 注册脚本 Bean。并发阈值 `seckill.limit.per-second` 默认 20000。

### 9.4 支付

当前为**测试模式**：点击支付直接成功并三表一致落库（订单 / 支付单 / 流水）。支付宝沙箱配置在 [application.yaml](file:///d:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/cloud-trade/src/main/resources/application.yaml#L52-L59) 中已注释，填充密钥并取消注释即可切换真实支付。

---

## 10. 已知限制

| 项 | 说明 |
|---|---|
| Elasticsearch 搜索 | 微服务版 `cloud-goods` 未启用 ES（无 `GoodsDocRepository`），全文搜索能力仅保留在单体。相关代码与依赖仍留在单体中 |
| AI 语义找书（RAG） | 依赖 Milvus。Milvus 未启动时 `cloud-ai` 会因缺少 `VectorStore` Bean 启动失败 |
| WebSocket 聊天 | 前端 `vite.config.js` 里 `/chat` 走 ws 代理到 `:8080/chat`，但网关只有 `/api/chat/**` 路由，**当前不通**。需改为 `ws://localhost:8080/api/chat/**` 或在网关补 `/chat/**` 路由 |
| `/image`、`/user`、`/comment` 等旧代理 | `vite.config.js` 中这些无 `/api` 前缀的代理指向 `:8080`，网关没有对应路由；图片实际由 MinIO `:9000/zhiyue` 直接提供，通常不触发 |
| 秒杀压测接口 | 默认关闭（`seckill.loadtest.enabled=false`），需要时用启动参数打开 |
| 单一数据库 | 仍是共享库，未做分库；跨服务事务需靠本地消息表/最终一致，当前未实现分布式事务 |

---

## 11. License

MIT
