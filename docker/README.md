# Docker Compose 本地运行指南

本项目提供了一套完整的 Docker Compose 配置，可一键启动以下服务：

- **MySQL 8.0**（数据库，自动导入初始化脚本）
- **Redis 6**（缓存）
- **Backend**（Java 后端 B端，基于 Spring Boot，端口 8080）
- **Wxmp**（Java 后端 C端微信小程序，基于 Spring Boot，端口 8081）
- **Frontend**（Vue 前端，Nginx 托管）

---

## 前置要求

- Docker >= 20.10
- Docker Compose >= 2.0
- 至少 **4GB 可用内存**（首次构建 Maven 和 npm 需要较多资源）
- 至少 **5GB 可用磁盘空间**

---

## 快速开始

### 1. 启动所有服务

```bash
cd zhaoxinpms
docker compose up -d
```

> 首次构建会下载大量依赖，耗时约 **10~20 分钟**，请耐心等待。

### 2. 查看构建/运行日志

```bash
# 所有服务日志
docker compose logs -f

# 单独查看后端日志
docker compose logs -f backend

# 单独查看 wxmp 日志
docker compose logs -f wxmp

# 单独查看前端日志
docker compose logs -f frontend
```

### 3. 访问系统

- **前端地址**：http://localhost
- **B端 Swagger**：http://localhost:8080/swagger-ui/index.html
- **C端 Wxmp 健康检查**：http://localhost:8081/actuator/health
- **Druid 监控**：http://localhost:8080/druid/index.html（账号/密码：zhaoxinms / 123456）

默认登录账号：**admin / admin123**

> **住宅收费模块路由说明**：
> - `/resi/*` → Backend (8080)，住宅收费 B 端 API
> - `/wxmp/*` → Wxmp (8081)，微信小程序 C 端 API

---

## 服务端口说明

| 服务 | 容器内端口 | 映射到宿主机端口 | 说明 |
|------|-----------|----------------|------|
| Frontend (Nginx) | 80 | 80 | 前端入口，含 /resi/ /wxmp/ 路由转发 |
| Backend (Spring Boot) | 8080 | 8080 | B端 API（/resi/ 住宅收费接口） |
| Wxmp (Spring Boot) | 8081 | 8081 | C端 API（/wxmp/ 微信小程序接口） |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存（住宅收费 Key 前缀 `resi:`） |

---

## 国内网络加速（推荐）

如果无法直接访问 Docker Hub，可使用 DaoCloud 等国内镜像源。

**方式一：临时指定镜像前缀（当前已配置）**

本项目 `docker-compose.yml` 已默认使用 `docker.m.daocloud.io` 前缀拉取镜像。若你所在网络可直接访问 Docker Hub，可去掉此前缀：

```bash
# 编辑 docker-compose.yml，将以下镜像改为标准名称
# docker.m.daocloud.io/library/mysql:8.0  -> mysql:8.0
# docker.m.daocloud.io/library/redis:6-buster -> redis:6-buster
```

**方式二：配置 Docker 全局镜像加速**

编辑 `/etc/docker/daemon.json`：

```json
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://registry.docker-cn.com"
  ]
}
```

然后重启 Docker：

```bash
sudo systemctl restart docker
```

---

## 常见问题

### Q1: 首次构建后端失败（Maven 编译错误）

后端构建需要 **JDK 8** 环境。本项目 Dockerfile 已使用 `maven:3.8-openjdk-8` 镜像，容器内会自动处理。

若宿主机直接运行 `mvn clean package` 失败，请检查宿主机 JDK 版本是否为 1.8（高版本 JDK 如 17/21 无法编译本项目）。

### Q2: MySQL 初始化报错（DEFINER 权限）

若 `workflow_view.sql` 中的视图创建失败，可在 MySQL 容器启动后手动执行：

```bash
docker compose exec mysql mysql -uroot -proot pms -e "
DROP VIEW IF EXISTS act_id_group, act_id_membership, act_id_user;
"
```

然后重新导入 `sql/workflow_view.sql`。

### Q3: 后端启动报数据库连接失败

`docker-compose.yml` 已配置 `depends_on` 健康检查，确保 MySQL 完全就绪后才会启动后端。若仍报错，请检查：

```bash
docker compose ps
docker compose logs backend
```

### Q4: 文件上传后无法访问

容器内文件存储在 `/uploads` 目录，已挂载到 Docker Volume `uploads_data`。如需持久化到宿主机，可修改 `docker-compose.yml`：

```yaml
volumes:
  - ./uploads:/uploads
```

---

## 已知限制

1. **未开源模块已移除**：`zhaoxinwy-owner`（业主端）、`zhaoxinwy-pay`（支付）、`zhaoxinwy-park`（停车）等模块在源码中不存在，Docker 构建前已在 `zhaoxinwy-admin/pom.xml` 中注释相关依赖。
2. **住宅收费模块（resi）**：`zhaoxinwy-wxmp` 为新增 C 端模块，`zhaoxinwy-pms` 内新增 `com.zhaoxinms.resi` 包，两者均独立运行，与现有商业收费体系互不干扰。
3. **报表服务（jmreport）未包含**：该模块为独立 JAR，源码中未提供，因此 Docker Compose 中未包含报表服务。如需使用，请手动配置。
4. **Redis 命名空间**：住宅收费模块所有 Redis Key 必须以 `resi:` 为前缀，详见 `docs/RESI_REDIS_NAMESPACE.md`。

---

## 停止与清理

```bash
# 停止服务
docker compose down

# 停止并删除数据卷（会清空数据库！）
docker compose down -v

# 重新构建（代码变更后）
docker compose up -d --build
```
