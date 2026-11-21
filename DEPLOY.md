# 电影评分系统免费部署指南

目标架构（全部免费，¥0/月）：

```
用户浏览器
   │  访问 https://<你的项目>.pages.dev
   ▼
Cloudflare Pages（前端静态文件）
   │  调用 https://<你的服务>.onrender.com/api/...
   ▼
Render 免费实例（Spring Boot 后端，Docker 部署）
   │  JDBC + TLS
   ▼
TiDB Cloud Serverless（MySQL 兼容云数据库，5GB 免费）

Render 免费实例 ──(可选) 内部 redis://──▶ Render Free Key Value（Valkey 8 / 25MB，热点缓存）
```

- **UptimeRobot**（免费）每 5 分钟 ping 一次后端，避免 Render 免费实例 15 分钟无访问后休眠（休眠后冷启动约 1 分钟）。
- 本地开发不受影响：不设置环境变量时，后端照连本地 MySQL，前端照走 vite 代理。

---

## 第 0 步：把代码推到 GitHub

Render 和 Cloudflare Pages 都通过 GitHub 仓库自动拉取构建，所以先把本次的改动推上去：

```bash
git add Movie_Rating_Backend/Dockerfile Movie_Rating_Backend/src/main/resources/application.yml \
        movie-rating-frontend/src/api/request.js deploy/ DEPLOY.md
git commit -m "chore: 添加云端部署配置（Dockerfile、环境变量化数据库连接、数据库导出）"
git push origin main
```

> 提示：`deploy/init.sql` 同时充当数据库备份，保留在仓库里即可。

## 第 1 步：TiDB Cloud 建库并导入数据

1. 打开 <https://tidbcloud.com>，用邮箱或 Google/GitHub 注册（免费，不用信用卡）。
2. 创建 **Serverless Cluster**：选择离你近的区域（如 Singapore / Tokyo），其余默认。免费额度 5GB 存储 + 每月 5000 万请求单位，对本项目绰绰有余。
3. 集群创建后，进入 **Cluster → Connect**，记下连接信息：
   - Host：`<你的 Host>`（形如 `gateway01.<region>.prod.aws.tidbcloud.com`）
   - Port：`4000`
   - Username：`<你的用户名>`（形如 `<集群前缀>.root`）
   - 密码：在 TiDB Cloud 控制台自行设置/重置——**只保存在 TiDB Cloud 与 Render 环境变量中，不要写入任何文档或仓库**
4. 导入数据（两种方式任选）：

   **方式 A（推荐，一条命令）**：本机就有 mysql 客户端，直接把完整的 `deploy/init.sql` 导入 TiDB（必须走 TLS）。注意用 **8.4.8 客户端**（9.6 客户端缺 TiDB 需要的认证插件，会报 2059 错误）：
   ```bash
   "D:\Download\MySQL848\bin\mysql.exe" --host <Host> --port 4000 --user <Username> -p \
     --ssl-mode=VERIFY_IDENTITY --get-server-public-key \
     < deploy/init.sql
   ```
   dump 里自带 `CREATE DATABASE movie_info1707`、8 张表、全部数据和视图，跑完即完成。若证书校验报错，把 `--ssl-mode=VERIFY_IDENTITY` 换成 `--ssl-mode=REQUIRED`。

   **方式 B（网页控制台，只支持 CSV）**：
   1. 先建表结构：集群页面打开 **SQL Editor**，把 `deploy/schema.sql` 的内容（纯建表语句，含 `CREATE DATABASE`）整段粘贴执行。
   2. 再逐表导数据：集群页面 **Import → Import from local file**，依次上传 `deploy/csv/` 下的 8 个 CSV：
      - 每个文件的目标库选 `movie_info1707`、目标表选同名表（如 `movie1707.csv` → 表 `movie1707`）
      - 分隔符选逗号，勾选 **第一行是表头/跳过首行**（如有该选项）
      - 空字段按 NULL 处理（如有该选项）
   3. 8 个文件顺序无所谓（都是纯数据行）。视图 `v_hot_movies` 不在 CSV 里，若需要可在 SQL Editor 单独执行 `init.sql` 末尾的 `CREATE VIEW v_hot_movies` 语句。

5. 验证：在 TiDB Cloud 网页的 **SQL Editor** 里执行 `SELECT COUNT(*) FROM movie_info1707.movie1707;`，能看到数据即成功。

## 第 2 步：Render 部署后端

1. 打开 <https://render.com>，用 GitHub 账号登录并授权仓库访问。
2. **New → Web Service**，选择 GitHub 仓库 `DBProject`。
3. 配置：
   - **Root Directory**：`Movie_Rating_Backend`（重要，Dockerfile 在这个子目录里）
   - **Runtime**：Docker（Render 会自动识别 Dockerfile）
   - **Instance Type**：Free
4. 展开 **Environment Variables**，添加 4 个变量：

   | Key | Value |
   |-----|-------|
   | `DB_URL` | `jdbc:mysql://<Host>:4000/movie_info1707?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
   | `DB_USERNAME` | `<Username>` |
   | `DB_PASSWORD` | `<你设置的 TiDB SQL 密码>` |
   | `JWT_SECRET` | `<强随机密钥，至少 32 字符，仅保存在 Render 环境变量>` |

   > 注意 `DB_URL` 里把 `<Host>` 换成你的实际 Host；`sslMode=VERIFY_IDENTITY` 如果构建后连接报证书错误，可降级为 `REQUIRED`。
   > 不需要设置 `PORT`，Render 会自动注入，后端已配置读取 `${PORT:8088}`。
   > **`JWT_SECRET` 为必填**：缺失时后端启动即失败（fail-fast），可用 `openssl rand -hex 32` 生成强随机值。

5. 点 **Create Web Service**，等第一次构建（Maven 打包约 5~10 分钟）。部署成功后会得到后端地址，形如：
   `https://<你的服务名>.onrender.com`

6. 验证后端活了：浏览器访问 `https://<你的服务名>.onrender.com/api/movie/page?pageNum=1&pageSize=1`（或任意一个你确定存在的 GET 接口），能返回 JSON 即成功。

## 第 3 步：Cloudflare Pages 部署前端

1. 打开 <https://dash.cloudflare.com>，注册/登录后进入 **Workers & Pages → Create → Pages → Connect to Git**，选择同一个 GitHub 仓库。
2. 配置：
   - **Project name**：随意（会决定你的访问域名 `<项目名>.pages.dev`）
   - **Root Directory**（在 Settings → Build & deployments → Build configuration 里改）：`movie-rating-frontend`
   - **Framework preset**：Vue
   - **Build command**：`npm run build`
   - **Build output directory**：`dist`
3. **Environment variables**（生产环境）添加：
   - `VITE_API_BASE_URL` = `https://<你的服务名>.onrender.com/api`（第 2 步的后端地址 + `/api`）
   - `NODE_VERSION` = `20`（Vite 5 需要 Node 18+，明确指定避免构建机用旧版本）
4. 点 **Save and Deploy**，约 2 分钟构建完成，得到 `https://<项目名>.pages.dev`。

5. 用浏览器打开这个域名，注册账号、搜电影、评分走一遍，全链路验证。

## 第 4 步：UptimeRobot 保活（可选但建议）

1. 打开 <https://uptimerobot.com> 免费注册（50 个监控，5 分钟间隔）。
2. **Add New Monitor**：
   - Monitor Type：HTTP(s)
   - URL：`https://<你的服务名>.onrender.com/`（或上面第 2.6 步的接口地址）
3. 保存。此后 Render 实例基本不会休眠。

## 第 5 步：热点缓存（可选，Render Free Key Value）

> 缓存默认关闭（`CACHE_ENABLED=false`），不影响现有部署；以下步骤全部使用免费额度。

1. 在 Render 控制台 **New → Key Value**，选择 **Free** 实例；**Region 必须与后端 Web Service 相同**（内部 URL 只在同区域私有网络可用）。
2. 创建后复制 **Internal Redis URL**（形如 `redis://red-xxxx:6379`，默认无密码）。
3. 打开后端 Web Service → **Environment**，新增：

   | Key | Value |
   |-----|-------|
   | `REDIS_URL` | `<Internal Redis URL>` |
   | `CACHE_ENABLED` | `true` |

4. 在 Key Value 设置里把驱逐策略设为 **`allkeys-lru`**（所有缓存 key 都带 TTL，双保险）。
5. 保存并等待重新部署。连续访问两次 `/api/movie/hot`，在 Key Value 指标页应能看到命令数增长；`/api/movie/list`、`/api/movie/search`、`/api/movie/{id}`、`/api/report/genre-summary` 同理。
6. **限制**：Free Key Value 为 25MB / 50 连接 / 无持久化（重启丢数据，缓存可接受）；仅作缓存，不要存业务唯一数据。
7. **回滚**：把 `CACHE_ENABLED` 改回 `false` 并重新部署即可，无需回滚代码。
8. **备选 Upstash Free**：若 Render 当前区域没有 Free Key Value，可在 <https://upstash.com> 创建免费 Redis（250MB、50 万命令/月），选择离 Render 最近的区域；环境变量 `REDIS_URL=rediss://default:<TOKEN>@<HOST>:<PORT>`（`rediss://` 会自动启用 TLS，也可显式设置 `REDIS_SSL=true`）。免费档仅适合低流量，JMeter 压测请打本地 Docker Valkey，不要打云实例。

## 日常更新

改完代码正常 `git push`，Render 和 Cloudflare Pages 会**自动重新部署**，无需手动操作。

## 常见问题

- **TiDB 不支持触发器和存储过程**：本地库的 3 个评分触发器和 2 个存储过程无法导入 TiDB。已做等效迁移：
  - 触发器"评分后重算电影平均分"→ 改为 Java 服务层逻辑（`RatingServiceImpl.recalcMovieRating`，评分增/改/删后自动重算）
  - 存储过程 `sp_query_movies_by_actor` / `sp_query_comments_by_movie` → mapper 已改为等价的普通 SQL（本地库的存储过程保留，课设演示不受影响）
  - 因此 `deploy/init.sql` 用 `--skip-triggers` 导出，不含触发器和存储过程；后端功能在 MySQL 和 TiDB 上完全一致
- **MySQL 9.x 客户端连 TiDB 报 `Authentication plugin 'mysql_native_password' cannot be loaded`**：9.x 客户端删除了该插件，改用 8.4.8 客户端（`D:\Download\MySQL848\bin\mysql.exe`）
- **Render 构建失败 / 内存超限**：免费实例构建内存有限。本仓库 Dockerfile 已用多阶段构建；如仍失败，重试一次（Render 页面 Manual Deploy）。
- **后端能启动但接口报数据库错误**：检查 `DB_URL/DB_USERNAME/DB_PASSWORD` 是否与 TiDB Cloud Connect 页面完全一致，重点确认 Host、端口 4000、用户名带 `.root` 后缀。
- **前端能打开但请求全失败**：检查 Cloudflare Pages 的 `VITE_API_BASE_URL` 是否以后端地址 + `/api` 结尾（后端接口路径本身就带 `/api` 前缀，不要再拼一层）。
- **TiDB 免费额度**：Serverless 每月 5000 万 request units，个人课设用量远用不完；超限时升级付费前会有邮件提醒。
- **开启缓存后接口报 Redis 连接失败**：先确认 Key Value 与 Web Service 同区域、`REDIS_URL` 用的是 Internal URL；即使 Redis 不可用，后端也会熔断并直查数据库（30 秒后重试），不会中断接口。需要立即恢复可临时设 `CACHE_ENABLED=false`。
- **Free Key Value 重启后缓存为空**：免费实例无持久化，属于预期行为；缓存会自动按需重建。
- **安全提示**：2026-11-16 已完成凭据轮换——git 历史中曾包含的本地 MySQL 密码与 TiDB 密码均已作废，历史旧值一律视为失效（仓库历史未重写）；JWT 密钥已改为仅通过环境变量注入，源码不再包含任何默认密钥；本地开发凭据存放于不入库的 `application-local.yml`（模板见 `application-local.yml.example`）。

## 本地 Docker Desktop 全栈部署（可选，与云部署互不影响）

不想装/占用本地 MySQL，或想在本地完整复刻"前端 + 后端 + 数据库 + 缓存"时使用：

```bash
# 1. 启动 Docker Desktop（应用装在 %LOCALAPPDATA%\Programs\DockerDesktop，等待引擎就绪）
# 2. 生成密钥并启动
cd DatabaseProject
cp .env.example .env      # 填 MYSQL_ROOT_PASSWORD / JWT_SECRET（.env 不入库）
docker compose up -d --build

# 3. 访问 http://localhost:5173（Nginx 入口，/api 自动反代后端）
```

| 服务 | 镜像 | 宿主机端口 | 说明 |
|---|---|---|---|
| frontend | node:20 构建产物 + nginx:alpine | **5173** | 访问入口，SPA 路由回退 |
| backend | 多阶段 Maven 构建 → eclipse-temurin:11-jre | 8088 | `/api` 直连调试 |
| mysql | **mysql:8.4 LTS** | **3307** | 首次启动自动执行 `deploy/init.sql`（8 表 + 种子 + 视图）；不碰本机 MySQL96(3306) / MySQL84(3305) |
| redis | valkey/valkey:8-alpine | 6379 | 热点缓存（`.env` 里 `CACHE_ENABLED`，默认 true） |

常用命令：

```bash
docker compose logs -f backend      # 后端日志
docker compose ps                   # 健康状态
docker compose down                 # 停止（保留数据卷）
docker compose down -v              # 连 MySQL 数据卷一起清空，下次启动重新导入种子数据
```

> **MySQL 9.6 与 8.4 的关系**：两条并行的发布线——8.4 是 LTS（长期支持版），9.x（含本机正在运行的 9.6.0）是 Innovation 创新版（约一年生命周期），互为并行而非升级替代；本项目 SQL 两者都兼容。差异主要在客户端：9.x 的 mysql 客户端移除了 `mysql_native_password` 插件，连 TiDB 会报 2059（见上文"常见问题"），需改用 8.4.8 客户端。Docker 容器统一选 8.4 LTS 求稳。

## 本地部署不受影响的说明

- `application.yml` 中数据库地址/用户名仍是 `${环境变量:本地默认值}` 形式；数据库密码与 JWT 密钥**没有默认值**：复制 `application-local.yml.example` 为 `application-local.yml`（已被 .gitignore 忽略）填入本地值即可，不创建该文件且未设环境变量时后端会因数据库/密钥缺失而启动失败。
- 环境变量（`DB_PASSWORD` / `JWT_SECRET`）优先级高于 `application-local.yml`。
- 缓存开关 `CACHE_ENABLED` 默认 `false`；Docker 全栈部署在 `.env` 里控制（默认 true），本地裸机开发则在 `application-local.yml` 中开启并先 `docker compose up -d redis` 启动 Valkey。
- 前端 `request.js` 的 `baseURL` 未设 `VITE_API_BASE_URL` 时回退为 `/api`，vite 开发代理照常工作。
- Dockerfile 只在 Docker 构建时使用，与 `mvnw spring-boot:run` 本地启动方式互不影响。
