ARG NODE_IMAGE=node:16-alpine
ARG NGINX_IMAGE=nginx:alpine

# 构建阶段
FROM ${NODE_IMAGE} AS builder

WORKDIR /app
COPY pms-web/package*.json ./
RUN npm install --registry=https://registry.npmmirror.com --legacy-peer-deps
COPY pms-web/ .
RUN npm run build:prod

# 运行阶段
FROM ${NGINX_IMAGE}
COPY --from=builder /app/dist /usr/share/nginx/html
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
