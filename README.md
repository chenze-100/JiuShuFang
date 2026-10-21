# 旧书坊（JiuShuFangSecondHand）

> 一个集成 AI 助手的二手书交易全栈项目：Spring Boot 3 + Vue 3 + Spring AI 多 Agent 架构

二手书交易平台，覆盖「浏览选购 → 购物车 → 下单支付 → 物流评价」完整交易闭环，并内置基于大模型的 **AI 购书助手**（多 Agent 路由 + RAG 向量检索 + 对话记忆），支持自然语言找书、智能下单；同时提供用户间 **WebSocket 即时聊天**、Elasticsearch 书籍搜索、个性化推荐等能力。

## 技术栈

**后端**

| 技术 | 用途 |
|---|---|
| Spring Boot 3 / Java 17 | 基础框架 |
| MyBatis-Plus | 数据持久化（乐观锁、逻辑删除） |
| Spring AI + 阿里云百炼（通义千问） | AI 对话 / 文本向量化 |
| Redis | AI 对话记忆、热点数据缓存 |
| RabbitMQ | 聊天记录异步落库、商品向量化 |
| Elasticsearch（IK 分词） | 书籍全文搜索 |
| Milvus | 商品向量存储与相似度检索（RAG） |
| MinIO | 图片对象存储 |
| JWT + ThreadLocal | 鉴权与用户上下文传递 |

**前端**

Vue 3（`<script setup>` 组合式 API） · Element Plus · Vue Router · 原生 fetch 封装统一请求层 · WebSocket 即时通讯 · GSAP 动效

## 核心功能

- **交易闭环**：商品发布/审核、分类浏览、购物车、订单、模拟支付、物流、评价、收货地址管理
- **AI 购书助手**：
  - 多 Agent 路由架构（RouterAgent 意图识别 → BuyAgent / SellAgent / NormalAgent）
  - 对话记忆：Redis 滑动窗口（最近 20 条） + RabbitMQ 异步持久化到 MySQL
  - RAG：Milvus 向量相似度检索书籍，支持自然语言描述找书
  - 智能下单：AI 对话中直接创建订单，聊天记录关联订单 ID
- **即时聊天**：WebSocket 长连接，全局断线自动重连 + 消息通知
- **书籍搜索**：Elasticsearch IK 中文分词
- **其他**：店铺管理、收藏/点赞/评论/关注、浏览历史、个性化推荐、微信扫码登录

## 快速启动

### 环境要求

JDK 17 · Maven 3.6+ · Node 16+ · Docker（中间件）

### 依赖中间件

| 服务 | 端口 | 说明 |
|---|---|---|
| MySQL 8 | 3308 | 业务数据 |
| Redis | 6379 | 缓存 / 对话记忆 |
| RabbitMQ | 5672 | 消息队列 |
| Elasticsearch 8 | 9200 | 书籍搜索 |
| Milvus | 19530 | 向量数据库 |
| MinIO | 9000 | 图片存储 |

### 启动步骤

```bash
# 1. 初始化数据库（16 张表 + 书籍分类基础数据）
mysql -uroot -p < src/main/resources/init.sql

# 2. 配置密钥
#    复制 application.yaml 同目录下新建 application-local.yaml，
#    填入你的阿里云百炼 API Key（https://bailian.console.aliyun.com/）：
#
#    spring:
#      ai:
#        dashscope:
#          api-key: sk-你的Key

# 3. 启动后端（端口 89）
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. 启动前端（端口 3000）
cd ../JiuShuFangFrontend
npm install
npm run dev
```

访问 `http://localhost:3000`，前端通过 Vite 代理将 `/api` 转发至后端。

> 支付模块当前为**测试模式**：点击支付直接成功并正常落库（订单/支付单/流水三表一致），不涉及真实扣款。支付宝沙箱回调代码已保留，配置密钥后即可切换真实支付。

## 架构概览

```
浏览器 (Vue3, :3000)
   │  /api 前缀（Vite 代理）
   ▼
Spring Boot 后端 (:89)
   ├─ 交易链路：Goods / Cart / Order / Pay / Evaluate
   ├─ AI 链路：RouterAgent ─┬─ BuyAgent  → queryBook / buyBook 工具 → MySQL
   │                        ├─ SellAgent → 商品上架引导
   │                        └─ NormalAgent → 陪伴对话
   │     记忆层：Redis（滑动窗口 20 条）→ MQ → MySQL（全量持久化）
   │     检索层：queryBook（MySQL 精确）+ searchByRag（Milvus 语义）
   ├─ 聊天链路：WebSocket 长连接 + 全局重连
   └─ 搜索链路：Elasticsearch（IK 分词）
```

## 项目结构

```
JiuShuFangSecondHand/
├── src/main/java/com/JiuShuFangSecondHand/
│   ├── agent/            # AI 多 Agent（Router/Buy/Sell/Normal）
│   ├── config/           # ChatClient、拦截器、MQ 等配置
│   ├── controller/       # REST 接口（商品/订单/支付/聊天/AI...）
│   ├── listener/         # MQ 消费者（聊天落库、商品向量化）
│   ├── repository/       # 自定义 Redis 对话记忆仓库
│   ├── service/          # 业务层
│   └── tool/             # Spring AI Function Calling 工具
└── src/main/resources/
    ├── application.yaml          # 配置模板（占位符，可上传）
    ├── application-local.yaml    # 真实密钥（.gitignore 已忽略）
    └── init.sql                  # 数据库初始化脚本
```

## License

MIT
