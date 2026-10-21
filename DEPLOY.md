# 旧书坊（JiuShuFangSecondHand）部署教程

> 从零开始，在一台新机器上把「旧书坊」跑起来：中间件（Docker）→ 数据库初始化 → 后端（:89）→ 前端（:3000）。

---

## 1. 项目组成与端口总览

本项目是一个全栈应用，分为三部分：

| 部分 | 目录 | 说明 |
|---|---|---|
| 后端 | `JiuShuFangSecondHand/` | Spring Boot 3 + Java 17，端口 **89** |
| 前端 | `JiuShuFangFrontend/` | Vue 3 + Vite，端口 **3000** |
| 中间件 | Docker 容器 | MySQL / Redis / RabbitMQ / Elasticsearch / Milvus / MinIO |

后端依赖的中间件及默认端口：

| 中间件 | 端口 | 用途 | 是否必装 |
|---|---|---|---|
| MySQL 8 | 3308 | 业务数据（库名 `zhi-yue`） | 必装 |
| Redis 7 | 6379 | 缓存、AI 对话记忆、秒杀库存 | 必装 |
| RabbitMQ 3 | 5672（管理台 15672） | 聊天落库、商品向量化等异步消息 | 必装 |
| Elasticsearch 8 | 9200 | 书籍全文搜索（IK 分词） | 必装 |
| MinIO | 9000（控制台 9001） | 图片对象存储（bucket：`zhiyue`） | 必装 |
| Milvus | 19530 | AI 语义找书的向量数据库 | 建议装（不装需改代码，见 FAQ） |

---

## 2. 环境准备

先装好以下软件，再开始部署：

| 软件 | 版本要求 | 验证命令 |
|---|---|---|
| JDK | 17 | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| Node.js | 16+ | `node -v` |
| Docker Desktop | 最新版即可 | `docker -v` |

> Windows 用户：Docker Desktop 安装后需要启动并等待左下角图标变绿（Running）。

---

## 3. 中间件部署（Docker 一键启动）

打开终端（PowerShell），逐条执行以下命令。每条命令创建并后台运行一个容器，重启电脑后会自动恢复。

### 3.1 MySQL 8（端口 3308）

```bash
docker run -d --name mysql8 --restart always `
  -p 3308:3306 `
  -e MYSQL_ROOT_PASSWORD=123456 `
  -e TZ=Asia/Shanghai `
  mysql:8 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci
```

> 注意：容器内 MySQL 是 3306 端口，映射到宿主机 **3308**，与后端配置 `jdbc:mysql://localhost:3308/zhi-yue` 对应。
> 如果你本机已经装了 MySQL 且占用了 3308，改 `-p` 左边的端口，并同步修改 `application.yaml` 里的数据库 url。

### 3.2 Redis 7（端口 6379，密码 123）

```bash
docker run -d --name redis7 --restart always `
  -p 6379:6379 `
  redis:7 redis-server --requirepass 123
```

### 3.3 RabbitMQ（端口 5672，管理台 15672）

```bash
docker run -d --name rabbitmq --restart always `
  -p 5672:5672 -p 15672:15672 `
  rabbitmq:3-management
```

默认账号/密码：`guest` / `guest`。
管理台：http://localhost:15672 （可以看到队列堆积情况，调试用）。

### 3.4 Elasticsearch 8（端口 9200）

本地开发推荐**关闭安全认证**的单节点模式，省去自签名证书的麻烦：

```bash
docker run -d --name es8 --restart always `
  -p 9200:9200 `
  -e "discovery.type=single-node" `
  -e "xpack.security.enabled=false" `
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" `
  docker.elastic.co/elasticsearch/elasticsearch:8.14.3
```

因为关闭了认证，需要把后端 [application.yaml](file:///D:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/src/main/resources/application.yaml) 中的 ES 配置改为：

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200   # https 改为 http
    # username 和 password 两行删掉或注释掉
```

验证：浏览器访问 http://localhost:9200 ，能看到一段 JSON（含 `"tagline" : "You Know, for Search"`）即成功。

> **中文搜索需要 IK 分词器**（书籍搜索功能依赖它），安装命令：
>
> ```bash
> docker exec -it es8 bin/elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/8.14.3
> docker restart es8
> ```

### 3.5 Milvus（端口 19530）

使用内嵌 etcd 的单容器模式（最省事）：

```bash
docker run -d --name milvus --restart always `
  -p 19530:19530 -p 9091:9091 `
  -e ETCD_USE_EMBED=true `
  -e COMMON_STORAGETYPE=local `
  -v milvus-data:/var/lib/milvus `
  milvusdb/milvus:v2.4.13 milvus run standalone
```

> 如果该模式在你的机器上启动失败，改用官方 docker-compose 方式（见 FAQ 第 5 条）。
> 暂时不想装 Milvus 也能启动项目，见 FAQ 第 4 条。

### 3.6 MinIO（端口 9000，控制台 9001）

```bash
docker run -d --name minio --restart always `
  -p 9000:9000 -p 9001:9001 `
  -e MINIO_ROOT_USER=minioadmin `
  -e MINIO_ROOT_PASSWORD=minioadmin `
  -v minio-data:/data `
  minio/minio server /data --console-address ":9001"
```

**装完必须手动建 bucket**（否则图片上传/显示失败）：

1. 浏览器打开控制台 http://localhost:9001 ，用 `minioadmin` / `minioadmin` 登录
2. 左侧 Buckets → Create Bucket，名称填 **`zhiyue`**（必须叫这个，前后端代码里写死了）
3. 建好后点进 `zhiyue` → Anonymous → Add Access Rule，添加一条 **Read** 权限（即设为公共读，否则前端图片显示不出来）

### 3.7 确认中间件全部就绪

```bash
docker ps
```

应看到 6 个容器：`mysql8`、`redis7`、`rabbitmq`、`es8`、`milvus`、`minio`，状态都是 Up。

---

## 4. 初始化数据库

项目自带初始化脚本 [init.sql](file:///D:/Systems/computer/JiuShuFang/JiuShuFangSecondHand/src/main/resources/init.sql)，包含**建库 `zhi-yue`、全部 16 张表、书籍分类基础数据**。

方式一（命令行，推荐）：

```bash
cd JiuShuFangSecondHand
docker exec -i mysql8 mysql -uroot -p123456 < src/main/resources/init.sql
```

方式二（图形化）：用 Navicat / DataGrip 连接 `localhost:3308`（root / 123456），打开 `init.sql` 全选执行。

> 脚本开头有 `CREATE DATABASE IF NOT EXISTS`，重复执行会先 DROP 再重建表——**会清空数据**，只在新环境第一次部署时执行。

---

## 5. 配置后端密钥

后端的真实密钥不放在 Git 仓库里，需要你在本地手动创建配置文件。

在 `JiuShuFangSecondHand/src/main/resources/` 目录下新建文件 **`application-local.yaml`**，内容如下：

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-填入你的阿里云百炼API-Key    # 必填，AI 助手靠它

  datasource:
    password: 123456        # MySQL root 密码，与 3.1 一致

  data:
    redis:
      password: "123"       # Redis 密码，与 3.2 一致

  rabbitmq:
    password: guest         # RabbitMQ 密码，与 3.3 一致

minio:
  secret-key: minioadmin    # MinIO 密码，与 3.6 一致

# JWT 签名密钥（自定义一个足够长的随机字符串）
jwt:
  secret: mySecretKey123456789012345678901234
```

**各项密钥从哪来：**

| 配置项 | 获取方式 |
|---|---|
| `dashscope.api-key` | 阿里云百炼平台 https://bailian.console.aliyun.com/ 注册后免费创建，AI 助手/语义找书必须 |
| 数据库/Redis/RabbitMQ/MinIO 密码 | 就是第 3 步 docker 命令里设置的密码，保持一致即可 |

> 如果你按 3.4 关掉了 ES 安全认证，local 文件里**不要**配置 `spring.elasticsearch.password`。
> 微信扫码登录（`wx-appid` / `wx-secret`）和支付宝沙箱是可选功能，不配置不影响主流程，需要时再加。

---

## 6. 启动后端（端口 89）

三种方式任选其一：

**方式一：IDEA（开发推荐）**

1. 用 IDEA 打开 `JiuShuFangSecondHand` 项目
2. 右上角运行配置 → Active profiles 填 `local`
3. 运行 `JiuShuFangSecondHandApplication`

**方式二：Maven 命令行**

```bash
cd JiuShuFangSecondHand
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**方式三：打包成 jar（服务器部署推荐）**

```bash
cd JiuShuFangSecondHand
mvn clean package -DskipTests
java -jar target/JiuShuFangSecondHand-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

**验证**：看到日志 `Started JiuShuFangSecondHandApplication` 后，访问接口文档 http://localhost:89/doc.html ，能打开即成功。

---

## 7. 启动前端（端口 3000）

```bash
cd JiuShuFangFrontend
npm install     # 第一次部署才需要，安装依赖
npm run dev
```

访问 http://localhost:3000 ，能看到旧书坊首页即成功。

> 前端通过 Vite 代理把 `/api`、`/image`、`/chat` 等请求转发到 `localhost:89`（见 `vite.config.js`），所以**前端不需要改任何地址**，只要后端在 89 端口跑着即可。
>
> 服务器正式部署时改用 `npm run build`，把生成的 `dist/` 交给 Nginx 托管，并把代理规则改成 Nginx 的 `location /api { proxy_pass http://后端:89; }`。

---

## 8. 部署验证清单

按顺序过一遍，全部打勾即部署成功：

- [ ] `docker ps` 里 6 个中间件容器全部 Up
- [ ] Navicat 能连上 `localhost:3308`，且 `zhi-yue` 库里有 16 张表
- [ ] MinIO 控制台（:9001）存在公共读的 `zhiyue` bucket
- [ ] http://localhost:89/doc.html 能打开接口文档
- [ ] http://localhost:3000 能打开首页，书籍列表有数据
- [ ] 注册/登录一个账号，上传商品时图片能正常显示
- [ ] 搜索框输入书名能搜到结果（验证 ES + IK 分词）
- [ ] AI 助手能正常对话（验证 dashscope api-key）

---

## 9. 常见问题（FAQ）

**1）后端启动报错：连不上 Elasticsearch（SSL / 证书相关错误）**

ES8 默认开启 HTTPS + 自签名证书，Java 客户端不信任就会报错。按本教程 3.4 的方式关闭安全认证（`xpack.security.enabled=false`）并把 yaml 改成 `http://localhost:9200` 即可。生产环境请改为配置受信任证书，而不是关认证。

**2）前端图片全部显示不出来**

90% 是 MinIO 的 `zhiyue` bucket 没建或没设为公共读，回到 3.6 检查。其次检查浏览器里图片地址能否直接打开（形如 `http://localhost:9000/zhiyue/2026/xx/xx.jpg`）。

**3）AI 助手不回复 / 报 401**

`application-local.yaml` 里的 `dashscope.api-key` 没配或填错，去百炼平台重新复制，注意以 `sk-` 开头、不要带空格。

**4）暂时不想装 Milvus，怎么让项目先跑起来？**

目前主类 `JiuShuFangSecondHandApplication` 的注释写着「已排除 Milvus 自动配置」，但实际注解上并没有排除参数（注释与代码不一致）。不装 Milvus 时需要手动改注解：

```java
@SpringBootApplication(exclude = MilvusVectorStoreAutoConfiguration.class)
```

同时在 `application.yaml` 中把 `initialize-schema: true` 改为 `false`。代价：AI「语义找书（RAG）」功能不可用，其余功能不受影响。建议有条件还是装上 Milvus。

**5）Milvus 单容器模式启动失败**

改用官方 docker-compose（三个容器：etcd + minio + milvus）：

```bash
mkdir milvus && cd milvus
curl -O https://github.com/milvus-io/milvus/releases/download/v2.4.13/milvus-standalone-docker-compose.yml
docker compose -f milvus-standalone-docker-compose.yml up -d
```

**6）端口被占用（Port xxx was already in use）**

先查谁占了端口（PowerShell）：`netstat -ano | findstr :端口号`，杀掉对应进程或改配置里的端口。注意 MySQL 映射的是 3308 而非常规的 3306，就是为了避开本机已装的 MySQL。

**7）数据库连不上 / 报时区错误**

`application.yaml` 的 url 里已带 `serverTimezone=GMT%2B8` 等参数，正常不会报。如果用的是自己本机安装的 MySQL 而非 Docker，确认账号密码与 yaml 一致、且允许 root 远程（localhost）登录。

**8）秒杀压测接口 404**

压测接口默认关闭，属于刻意设计。需要压测时加启动参数 `--seckill.loadtest.enabled=true`。

---

## 附：全部启动命令速查

```bash
# 1. 中间件（只需创建一次，之后开机自动启动）
docker ps    # 确认 6 个容器 Up

# 2. 后端
cd JiuShuFangSecondHand
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. 前端（新开一个终端）
cd JiuShuFangFrontend
npm run dev

# 访问 http://localhost:3000
```
