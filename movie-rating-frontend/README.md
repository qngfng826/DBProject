# MovieRating 前端

电影评分系统的用户端 + 管理后台。Vue 3 + Vite + Element Plus + Pinia + ECharts。

## 运行

```bash
npm install
npm run dev      # http://localhost:5173，/api 代理到 http://localhost:8088（见 vite.config.js）
npm run build    # 生产构建到 dist/
npm run preview  # 本地预览构建产物
```

## 目录结构

```
src/
├── api/          # Axios 封装（统一拦截器、GET 缓存、401 处理）与各模块接口
├── components/   # 电影详情页组件（MovieHeader / MovieCommentsSection）
├── router/       # 路由与守卫（登录 / 管理员权限）
├── stores/       # Pinia：用户会话
└── views/        # 用户端页面（首页/列表/详情/评分/评论/报表…）与 admin/ 管理后台
```

## 构建环境变量

- `VITE_API_BASE_URL`：生产环境后端 API 前缀（如 `https://your-backend.example.com/api`），配置于 `.env.production` 或 Cloudflare Pages 环境变量；不设置时回退 `/api`，走开发代理。

完整项目说明见仓库根 [README](../README.md)。