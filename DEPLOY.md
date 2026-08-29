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
```

- **UptimeRobot**（免费）每 5 分钟 ping 一次后端，避免 Render 免费实例 15 分钟无访问后休眠（休眠后冷启动约 1 分钟）。
- 本地开发不受影响：不设置环境变量时，后端照连本地 MySQL，前端照走 vite 代理。

---

## 第 0 步：把代码推到 GitHub

Render 和 Cloudflare Pages 都通过 GitHub 仓库自动拉取构建，所以先把本次的改动推上去：

```bash
git add Movie_Rating_Backend/Dockerfile Movie_Rating_Backend/src/main/resources/application.yml \
        movie-rating-frontend/src/api/request.js deploy/init.sql DEPLOY.md
git commit -m "chore: 添加云端部署配置（Dockerfile、环境变量化数据库连接、数据库导出）"
git push origin main
```

> 提示：`deploy/init.sql` 同时充当数据库备份，保留在仓库里即可。

## 第 1 步：TiDB Cloud 建库并导入数据

1. 打开 <https://tidbcloud.com>，用邮箱或 Google/GitHub 注册（免费，不用信用卡）。
2. 创建 **Serverless Cluster**：选择离你近的区域（如 Singapore / Tokyo），其余默认。免费额度 5GB 存储 + 每月 5000 万请求单位，对本项目绰绰有余。
3. 集群创建后，进入 **Cluster → Connect**，记下连接信息：
   - Host（形如 `gateway01.xxx.prod.aws.tidbcloud.com`）
   - Port（一般是 `4000`）
   - Username（形如 `<随机串>.root`）
   - 密码：第一次连接前需要在此页面设置一个 SQL 密码
4. 导入数据（两种方式任选）：

   **方式 A（推荐，网页操作）**：集群页面左侧 **Import** → **Import from local file** → 上传本仓库的 `deploy/init.sql` → 按提示完成。dump 里自带 `CREATE DATABASE movie_info1707`，导入即建库。

   **方式 B（命令行）**：在本机（需有 mysql 客户端，连接 TiDB 必须走 TLS）：
   ```bash
   mysql --host <Host> --port 4000 --user <Username> -p \
     --ssl-mode=VERIFY_IDENTITY \
     --get-server-public-key \
     < deploy/init.sql
   ```
   （若提示证书校验问题，可去掉 `--ssl-mode=VERIFY_IDENTITY` 改用 `--ssl-mode=REQUIRED`。）

5. 验证：在 TiDB Cloud 网页的 **SQL Editor** 里执行 `SELECT COUNT(*) FROM movie_info1707.movie1707;`，能看到数据即成功。

## 第 2 步：Render 部署后端

1. 打开 <https://render.com>，用 GitHub 账号登录并授权仓库访问。
2. **New → Web Service**，选择 GitHub 仓库 `DBProject`。
3. 配置：
   - **Root Directory**：`Movie_Rating_Backend`（重要，Dockerfile 在这个子目录里）
   - **Runtime**：Docker（Render 会自动识别 Dockerfile）
   - **Instance Type**：Free
4. 展开 **Environment Variables**，添加 3 个变量（对应第 1 步记下的信息）：

   | Key | Value |
   |-----|-------|
   | `DB_URL` | `jdbc:mysql://<Host>:4000/movie_info1707?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3&useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
   | `DB_USERNAME` | `<Username>` |
   | `DB_PASSWORD` | `<你设置的 TiDB SQL 密码>` |

   > 注意 `DB_URL` 里把 `<Host>` 换成你的实际 Host；`sslMode=VERIFY_IDENTITY` 如果构建后连接报证书错误，可降级为 `REQUIRED`。
   > 不需要设置 `PORT`，Render 会自动注入，后端已配置读取 `${PORT:8088}`。

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

## 日常更新

改完代码正常 `git push`，Render 和 Cloudflare Pages 会**自动重新部署**，无需手动操作。

## 常见问题

- **Render 构建失败 / 内存超限**：免费实例构建内存有限。本仓库 Dockerfile 已用多阶段构建；如仍失败，重试一次（Render 页面 Manual Deploy）。
- **后端能启动但接口报数据库错误**：检查 `DB_URL/DB_USERNAME/DB_PASSWORD` 是否与 TiDB Cloud Connect 页面完全一致，重点确认 Host、端口 4000、用户名带 `.root` 后缀。
- **前端能打开但请求全失败**：检查 Cloudflare Pages 的 `VITE_API_BASE_URL` 是否以后端地址 + `/api` 结尾（后端接口路径本身就带 `/api` 前缀，不要再拼一层）。
- **TiDB 免费额度**：Serverless 每月 5000 万 request units，个人课设用量远用不完；超限时升级付费前会有邮件提醒。
- **安全提示**：仓库历史提交中曾包含本地数据库明文密码（现默认值仍在 `application.yml` 中）。该仓库若是公开的，建议顺手改掉本地 MySQL 的 root 密码，或把仓库设为私有。

## 本地部署不受影响的说明

- `application.yml` 所有新配置都是 `${环境变量:本地默认值}` 形式，不设环境变量即用默认值。
- 前端 `request.js` 的 `baseURL` 未设 `VITE_API_BASE_URL` 时回退为 `/api`，vite 开发代理照常工作。
- Dockerfile 只在 Docker 构建时使用，与 `mvnw spring-boot:run` 本地启动方式互不影响。
