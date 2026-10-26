# 旧书坊（JiuShuFang）微服务版 · 部署说明

> 从零在一台新机器上把 6 个模块跑起来：中间件（Docker）→ 数据库初始化 → 构建公共模块 → 按序启动 5 个服务 → 前端。
> 架构与路由说明见 [README.md](file:///d:/Systems/computer/JiuShuFang/README.md)。

---

## 1. 部署拓扑与端口

### 1.1 应用

| 组件 | 目录 | 端口 | 说明 |
|---|---|---|---|
| 前端 | `JiuShuFangFrontend/` | **3000** | Vue 3 + Vite，开发代理 `/api` → `:8080` |
| 网关 | `cloud-gateway/` | **8080** | **唯一对外入口**，前端只认这个端口 |
| 用户服务 | `cloud-user/` | 8081 | 用户 / 登录 / 验证码 |
| 商品服务 | `cloud-goods/` | 8082 | 商品 / 分类 / 购物车 / 评论 |
| 交易服务 | `cloud-trade/` | 8083 | 订单 / 支付 / 秒杀 / 地址 |
| AI 服务 | `cloud-ai/` | 8089 | AI 对话 / 向量检索 / 验证码 |
| 公共模块 | `cloud-common/` | — | 普通 jar，不单独启动 |

### 1.2 中间件

| 中间件 | 端口 | 用途 | 是否必装 |
|---|---|---|---|
| MySQL 8 | 3308（容器内 3306） | 业务库 `zhi-yue` | **必装** |
| Redis 7 | 6379 | 缓存 / 对话记忆 / 限流令牌桶 / 秒杀库存 | **必装** |
| Nacos 2.x | 8848（+9848 gRPC） | 注册中心 + 配置中心，**缺了服务无法互相发现** | **必装** |
| RabbitMQ 3 | 5672（管理台 15672） | 聊天落库、商品向量同步 | **必装** |
| MinIO | 9000（控制台 9001） | 图片存储，bucket `zhiyue` | **必装** |
| Milvus 2.4 | 19530 | AI 语义找书向量库 | **必装**（不装 `cloud-ai` 启动失败，见 FAQ 3） |
| Elasticsearch 8 | 9200 | 全文搜索 | **微服务版不需要**（仅单体使用，当前未启用） |

---

## 2. 环境要求

| 软件 | 版本 | 验证 |
|---|---|---|
| JDK | **17** | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| Node.js | 16+ | `node -v` |
| Docker Desktop | 最新 | `docker -v` |

> Windows 用户：Docker Desktop 需启动并等图标变绿（Running）。
> PowerShell 下 `&&` 不可用，多命令请用 `;` 分隔，或用 `if ($LASTEXITCODE -eq 0) { ... }`。

---

## 3. 中间件部署（Docker）

逐条执行，`--restart always` 保证开机自恢复。

### 3.1 MySQL 8（3308）

```bash
docker run -d --name mysql8 --restart always `
  -p 3308:3306 `
  -e MYSQL_ROOT_PASSWORD=123456 `
  -e TZ=Asia/Shanghai `
  mysql:8 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci
```

> 容器内 3306 → 宿主机 **3308**，与各服务 `jdbc:mysql://localhost:3308/zhi-yue` 对应。
> 若本机已有 MySQL 占用 3308，改 `-p` 左侧端口，并同步修改所有 `cloud-*/src/main/resources/application.yaml` 的 url。

### 3.2 Redis 7（6379，密码 123）

```bash
docker run -d --name redis7 --restart always `
  -p 6379:6379 `
  redis:7 redis-server --requirepass 123
```

> 网关用 Redis 做限流（`redis-rate-limiter`），业务服务用它做缓存与秒杀。**密码必须与配置一致**，否则网关限流直接报错。

### 3.3 Nacos 2.x（8848）

```bash
docker run -d --name nacos --restart always `
  -e MODE=standalone `
  -p 8848:8848 -p 9848:9848 `
  nacos/nacos-server:v2.4.3
```

- 控制台 http://localhost:8848/nacos （默认 `nacos` / `nacos`）；
- **必须同时映射 9848**（Nacos 2.x 的 gRPC 端口），只映射 8848 会出现「服务注册上了但网关拉不到实例」；
- 启动日志出现 `Started` 后再启动后端服务。

### 3.4 RabbitMQ（5672 / 15672）

```bash
docker run -d --name rabbitmq --restart always `
  -p 5672:5672 -p 15672:15672 `
  rabbitmq:3-management
```

账号 `guest` / `guest`，管理台 http://localhost:15672 。

### 3.5 MinIO（9000 / 9001）

```bash
docker run -d --name minio --restart always `
  -p 9000:9000 -p 9001:9001 `
  -e MINIO_ROOT_USER=minioadmin `
  -e MINIO_ROOT_PASSWORD=minioadmin `
  -v minio-data:/data `
  minio/minio server /data --console-address ":9001"
```

**装完必须手动建 bucket**（否则图片上传/显示失败）：

1. 打开 http://localhost:9001 ，用 `minioadmin` / `minioadmin` 登录；
2. Buckets → Create Bucket，名称必须是 **`zhiyue`**（代码里写死）；
3. 进入 `zhiyue` → Anonymous → Add Access Rule，添加 **Read** 权限（公共读，否则前端图片裂图）。

### 3.6 Milvus（19530）

单容器内嵌 etcd 模式：

```bash
docker run -d --name milvus --restart always `
  -p 19530:19530 -p 9091:9091 `
  -e ETCD_USE_EMBED=true `
  -e COMMON_STORAGETYPE=local `
  -v milvus-data:/var/lib/milvus `
  milvusdb/milvus:v2.4.13 milvus run standalone
```

> 该模式下 `cloud-ai` 会在首次启动时自动建 collection `zhiyue_embeddings`（`initialize-schema: true`）。
> 单容器启动失败时，改用官方 docker-compose（见 FAQ 5）。

### 3.7 确认就绪

```bash
docker ps
```

应看到 `mysql8`、`redis7`、`nacos`、`rabbitmq`、`minio`、`milvus` 六个容器 Up。

---

## 4. 初始化数据库

```bash
cd JiuShuFangSecondHand
docker exec -i mysql8 mysql -uroot -p123456 < src/main/resources/init.sql
```

或用 Navicat / DataGrip 连 `localhost:3308`（root / 123456）执行 [init.sql](file:///d:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/src/main/resources/init.sql)。

> 脚本会 `CREATE DATABASE IF NOT EXISTS zhi-yue` 并重建表 —— **重复执行会清空数据**，仅首次部署使用。
> 微服务与单体共用这个库，所以只需初始化一次。

---

## 5. 配置与密钥

### 5.1 已内置默认值（本地开发可直接跑）

各服务的 [application.yaml](file:///d:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/cloud-user/src/main/resources/application.yaml) 里的密码都写成了 `${ENV:默认值}` 形式，与上面 Docker 命令的密码一致，**本地开发零配置即可启动**。

### 5.2 生产必须用环境变量覆盖

| 变量 | 作用 | 默认值 |
|---|---|---|
| `JWT_SECRET` | JWT 签名密钥，**网关与所有服务必须完全一致** | 中文占位符（必须覆盖） |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 数据库账号 | `root` / `123456` |
| `REDIS_PASSWORD` | Redis 密码 | `123` |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | MQ 账号 | `guest` / `guest` |
| `NACOS_ADDR` | Nacos 地址 | `localhost:8848` |
| `NACOS_NS` | Nacos 命名空间 | `public` |
| `MINIO_ENDPOINT` / `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` / `MINIO_BUCKET` | 图片存储 | `http://localhost:9000` / `minioadmin` / `minioadmin` / `zhiyue` |
| `DASHSCOPE_API_KEY` | 阿里云百炼 Key（AI 助手），`sk-` 开头 | 占位符（必须覆盖） |
| `AUTH_WHITELIST` | 网关免登录白名单（逗号分隔，**必须带 `/api` 前缀**） | 见下 |
| `SERVER_EXTERNAL_URL` / `SERVER_VUE_URL` | 支付回调与前端跳转地址（trade） | `http://localhost:8083` / `http://localhost:5173` |

### 5.3 网关本地配置（已 git 忽略）

`cloud-gateway/src/main/resources/application-local.yaml` 用于本地放行与本地 JWT 密钥：

```yaml
auth:
  whitelist: /api/code/**,/api/capture/**,/api/user/login/**,/api/user/wx/**,/api/user/check,/api/goods/list,/api/category/**,/error,/actuator/**,/v3/api-docs/**,/doc.html,/webjars/**
jwt:
  secret: localDevSecretKeyForJiuShuFangGateway0123456789abcdef
```

> 白名单是逗号分隔字符串而不是 YAML 数组 —— `@Value` 注入 `List<String>` 时只认逗号分隔，写成 `- /api/xxx` 的数组形式会注入失败。

### 5.4 支付宝（可选）

`cloud-trade/src/main/resources/application.yaml` 中 `alipay` 段已被注释，当前支付为**测试模式**（直接成功落库）。需要真实支付时填入沙箱密钥并取消注释。

---

## 6. 构建

### 6.1 先装公共模块（关键，顺序不能颠倒）

```bash
cd JiuShuFangSecondHand
mvn clean install -DskipTests -pl cloud-common -am
```

> `cloud-common` 是其它模块的依赖，**不 install 到本地仓库**时，单独跑某个模块会报「找不到 `com:cloud-common`」。

### 6.2 全量编译 / 打包

```bash
# 仅编译校验
mvn clean compile

# 打包（每个服务生成可执行 jar）
mvn clean package -DskipTests
```

产物：`cloud-*/target/cloud-*.jar`（网关 `cloud-gateway-*.jar` 等）。

> 若编译报 `illegal character: '\ufeff'`，说明某文件带了 UTF-8 BOM。用无 BOM 方式重写该文件：
> ```powershell
> $p='文件路径.java'
> $c=[IO.File]::ReadAllText($p)
> [IO.File]::WriteAllText($p, $c.TrimStart([char]0xFEFF), [Text.UTF8Encoding]::new($false))
> ```

---

## 7. 启动顺序

**必须先启动中间件，再启动服务；服务之间无强依赖，但网关最后启动**（网关要能发现其它服务）。

```
MySQL / Redis / Nacos / RabbitMQ / MinIO / Milvus
        │
        ├─ cloud-user   :8081
        ├─ cloud-goods  :8082
        ├─ cloud-trade  :8083
        ├─ cloud-ai     :8089
        └─ cloud-gateway:8080   ← 最后
```

### 方式一：IDEA（开发推荐）

1. 用 IDEA 打开 `JiuShuFangSecondHand`（作为 Maven 多模块工程）；
2. 依次运行 `UserApplication`、`GoodsApplication`、`TradeApplication`、`AiApplication`、`GatewayApplication`；
3. 网关建议激活 `local` profile（Active profiles 填 `local`），以便加载 `application-local.yaml`。

### 方式二：Maven 命令行（每个服务一个终端）

```bash
cd JiuShuFangSecondHand

mvn spring-boot:run -pl cloud-user
mvn spring-boot:run -pl cloud-goods
mvn spring-boot:run -pl cloud-trade
mvn spring-boot:run -pl cloud-ai
mvn spring-boot:run -pl cloud-gateway -Dspring-boot.run.profiles=local
```

### 方式三：jar 运行（服务器）

```bash
mvn clean package -DskipTests

java -jar cloud-user/target/cloud-user-0.0.1-SNAPSHOT.jar
java -jar cloud-goods/target/cloud-goods-0.0.1-SNAPSHOT.jar
java -jar cloud-trade/target/cloud-trade-0.0.1-SNAPSHOT.jar
java -jar cloud-ai/target/cloud-ai-0.0.1-SNAPSHOT.jar
java -jar cloud-gateway/target/cloud-gateway-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

生产环境建议把密钥放进环境变量或外部配置文件，不要写进 jar：

```bash
export JWT_SECRET='一个足够长的随机字符串'
export DASHSCOPE_API_KEY='sk-xxx'
export MYSQL_PASSWORD='xxx'
```

### 启动成功标志

| 服务 | 日志关键字 |
|---|---|
| 各服务 | `Started XxxApplication in ...` |
| 各服务 | `nacos registry, DEFAULT_GROUP cloud-xxx ... register finished` |
| 网关 | 同上 + 无 `RouteDefinitionRouteLocator` 的 WARN |

---

## 8. 启动前端

### 8.1 开发模式

```bash
cd JiuShuFangFrontend
npm install      # 首次
npm run dev      # :3000
```

访问 http://localhost:3000 。

> [vite.config.js](file:///d:/Systems/computer/JiuShuFang/JiuShuFangFrontend/vite.config.js) 把 `/api` 代理到 `http://localhost:8080`（网关），因此**前端不需要改任何后端地址**，只要网关在跑。
> 登陆态：`request.js` 从 `localStorage.userInfo.token` 取值，以 `Authorization: Bearer <token>` 发送（网关只认这个格式）。

### 8.2 生产模式（Nginx）

```bash
cd JiuShuFangFrontend
npm run build          # 产物在 dist/
```

Nginx 配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/jiushufang/dist;
    index index.html;

    # history 路由回退
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 全部业务接口交给网关
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Authorization $http_authorization;
        proxy_read_timeout 120s;    # AI 对话较慢，需要放宽
    }
}
```

> 图片由 MinIO 直接提供（`MINIO_READ_PATH`），生产可给 MinIO 配独立域名或用 Nginx 反代 `:9000`。

---

## 9. 部署验证清单

按顺序打勾：

- [ ] `docker ps` 六个中间件容器全部 Up
- [ ] Nacos 控制台 http://localhost:8848/nacos 的服务列表中能看到 `cloud-user / cloud-goods / cloud-trade / cloud-ai / cloud-gateway`
- [ ] Navicat 能连 `localhost:3308`，`zhi-yue` 库有全部业务表
- [ ] MinIO 控制台存在**公共读**的 `zhiyue` bucket
- [ ] `curl http://localhost:8080/api/goods/list` → 200（白名单免登录）
- [ ] `curl http://localhost:8080/api/category/list` → 200
- [ ] `curl http://localhost:8080/api/capture/captcha?key=t1` → 200（返回图片）
- [ ] `curl http://localhost:8080/api/order/page` → 401 `{"code":401,"message":"请先登录"}`（这是正常表现）
- [ ] 浏览器打开 http://localhost:3000 ，首页有商品数据
- [ ] 注册/登录成功，登录后 `/api/order/page` 带 token 返回 200
- [ ] 上传商品图片能正常显示（验证 MinIO）
- [ ] AI 助手能对话（验证 `DASHSCOPE_API_KEY`）
- [ ] 秒杀下单成功（验证 Redis Lua + MQ）

---

## 10. 常见问题（FAQ）

**1）网关启动报 `spring.cloud.gateway.routes[...]` 的 migrator 警告**

Spring Cloud Gateway 4.3 起配置前缀变更为 `spring.cloud.gateway.server.webflux.*`。当前 [application.yaml](file:///d:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/cloud-gateway/src/main/resources/application.yaml) 已迁移，若你看到该警告说明用的是旧配置。

**2）某个服务单独启动报「找不到 com:cloud-common」**

未安装公共模块。执行 `mvn clean install -DskipTests -pl cloud-common -am` 后重试。

**3）cloud-ai 启动失败：`required a bean of type 'VectorStore' that could not be found`**

Milvus 没起或没装。先确认 `docker ps` 里 `milvus` 是 Up、`localhost:19530` 可连；
确实不想装 Milvus 时，可临时排除自动配置（会失去 RAG 语义找书能力）：

```java
@SpringBootApplication(exclude = MilvusVectorStoreAutoConfiguration.class)
```

并给 `ToolService` 等使用 `VectorStore` 的 Bean 改为可选注入（`@Autowired(required = false)`）。

**4）前端报 `GET /api/xxx 500 (Internal Server Error)`，浏览器 Network 里没有响应体**

这**不是**业务错误，而是 Vite 代理连不上 `:8080`（网关没启动）。Vite 在目标端口不可达时一律返回 500，控制台会打印 `http proxy error: ECONNREFUSED`。
→ 先确认 `netstat -ano | findstr :8080` 有监听，再刷新页面。

**5）Milvus 单容器模式启动失败**

改用官方 docker-compose（etcd + minio + milvus 三容器）：

```bash
mkdir milvus && cd milvus
curl -O https://github.com/milvus-io/milvus/releases/download/v2.4.13/milvus-standalone-docker-compose.yml
docker compose -f milvus-standalone-docker-compose.yml up -d
```

**6）接口一直 401，登录也进不去**

按顺序排查：
1. 请求头是否是 `Authorization: Bearer <token>`（不是自定义 `token` 头）；
2. 路径是否以 `/api` 开头，且命中网关路由表（见 README 第 4 节）；
3. 白名单里的路径是否也带 `/api` 前缀（`@Value` 注入 List 只认逗号分隔、必须写全路径）；
4. 网关与服务的 `jwt.secret` 是否完全一致。

**7）`/api/user/**` 返回 401 但已经登录**

token 过期（默认 86400000ms = 24h）或 claim 里的用户 id 键名不对。网关同时兼容 `userId` 与单体遗留的 `id` 两种 claim。

**8）网关报限流错误 / 启动即失败在 `RedisRateLimiter`**

网关限流依赖 Redis。确认 Redis 已启动且 `spring.data.redis.password` 正确。报 `Unable to connect to Redis` 时先修 Redis，别改限流参数。

**9）服务注册上了但网关 503「Unable to find instance」**

Nacos 2.x 缺少 gRPC 端口映射。重新创建容器时补上 `-p 9848:9848`。

**10）路径多了一层（例如 `/goods/goods/list`）**

说明某个服务又加了 `server.servlet.context-path`。微服务版**统一不加 context-path**，网关 `StripPrefix=1` 去掉 `/api` 后的路径直接等于 Controller 的 `@RequestMapping`。

**11）编译报 `javax.validation.constraints.NotNull` 找不到**

Spring Boot 3 用 `jakarta.validation`。把 `javax.validation.*` 全改为 `jakarta.validation.*`。

**12）秒杀压测接口 404**

压测接口默认关闭（刻意设计）。需要时加启动参数 `--seckill.loadtest.enabled=true`。

**13）端口被占用**

```powershell
netstat -ano | findstr :8082
taskkill /PID <PID> /F
```

---

## 附：一键命令速查

```bash
# ── 中间件（只需创建一次，之后开机自启）
docker ps

# ── 首次：建库 + 装公共模块
cd JiuShuFangSecondHand
docker exec -i mysql8 mysql -uroot -p123456 < src/main/resources/init.sql
mvn clean install -DskipTests -pl cloud-common -am

# ── 启动 5 个服务（各开一个终端）
mvn spring-boot:run -pl cloud-user
mvn spring-boot:run -pl cloud-goods
mvn spring-boot:run -pl cloud-trade
mvn spring-boot:run -pl cloud-ai
mvn spring-boot:run -pl cloud-gateway -Dspring-boot.run.profiles=local

# ── 前端
cd ../JiuShuFangFrontend
npm run dev

# ── 访问
# http://localhost:3000        前端
# http://localhost:8080        网关（后端唯一入口）
# http://localhost:8848/nacos  Nacos 控制台
# http://localhost:9001        MinIO 控制台
```
