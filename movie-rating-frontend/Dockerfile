# ---- 构建阶段：Vite 打包 ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# 容器内由 Nginx 把 /api 反代到 backend 容器，前端无需知道后端具体地址
ENV VITE_API_BASE_URL=/api
RUN npm run build

# ---- 运行阶段：Nginx 托管静态文件并反代 API ----
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
