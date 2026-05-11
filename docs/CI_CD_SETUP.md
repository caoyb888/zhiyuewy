# 住宅物业收费模块 · CI/CD 流水线配置文档

**文档版本**：V1.0  
**编制日期**：2026-05-10  
**负责人**：BE-B  
**适用范围**：肇新智慧物业 · 住宅物业收费模块（resi）

---

## 目录

1. [架构概览](#1-架构概览)
2. [增量编译策略](#2-增量编译策略)
3. [流水线工作流](#3-流水线工作流)
4. [环境配置](#4-环境配置)
5. [自动部署流程](#5-自动部署流程)
6. [代码规范检查](#6-代码规范检查)
7. [验收标准](#7-验收标准)

---

## 1. 架构概览

```
GitHub Push/PR
       │
       ▼
┌─────────────────────────────────────────────┐
│  GitHub Actions (ci-cd.yml)                  │
│  ┌─────────────┐                            │
│  │ 变更检测     │ ──→ 判断影响模块范围        │
│  └──────┬──────┘                            │
│         │                                   │
│  ┌──────▼──────┐  ┌──────────────┐         │
│  │ 代码规范检查 │  │ Maven 编译检查 │         │
│  │ code-check  │  │ frontend lint │         │
│  └──────┬──────┘  └──────────────┘         │
│         │                                   │
│  ┌──────▼──────────┐  ┌──────────────┐     │
│  │ Build Backend    │  │ Build Wxmp   │     │
│  │ (zhaoxinwy-admin)│  │ (zhaoxinwy-  │     │
│  └──────┬───────────┘  │  wxmp)       │     │
│         │              └──────┬───────┘     │
│  ┌──────▼──────┐  ┌───────────▼──────┐      │
│  │ Build Front │  │ Docker 镜像构建   │      │
│  │ (pms-web)   │  │ & Push to GHCR   │      │
│  └──────┬──────┘  └───────────┬──────┘      │
│         │                     │             │
│  ┌──────▼─────────────────────▼──────┐      │
│  │      Deploy to Test Server        │      │
│  │      (SSH + Docker Compose)       │      │
│  └───────────────────────────────────┘      │
└─────────────────────────────────────────────┘
```

### 涉及文件

| 文件 | 说明 |
|---|---|
| `.github/workflows/ci-cd.yml` | 主 CI/CD 工作流 |
| `.github/workflows/code-check.yml` | 代码规范检查工作流（独立触发） |
| `scripts/code-check.sh` | CLAUDE.md 规范检查脚本 |
| `scripts/deploy-test.sh` | 测试环境自动部署脚本 |
| `docker-compose.yml` | Docker Compose 配置（已更新 image 字段） |

---

## 2. 增量编译策略

### 模块依赖关系

```
zhaoxinwy-admin (B端入口, 8080)
  ├── zhaoxinwy-framework
  ├── zhaoxinwy-system
  ├── zhaoxinwy-common
  ├── zhaoxinwy-quartz
  ├── zhaoxinwy-generator
  ├── zhaoxinwy-workflow
  └── zhaoxinwy-tenant

zhaoxinwy-wxmp (C端入口, 8081)
  ├── zhaoxinwy-common
  ├── zhaoxinwy-framework
  ├── zhaoxinwy-system
  └── zhaoxinwy-pms (住宅收费核心模块)

pms-web (Vue 前端)
  └── 依赖 B端 /resi/ 和 C端 /wxmp/ API
```

### 增量编译规则

| 变更路径 | 触发构建 |
|---|---|
| `zhaoxinwy-pms/**` | backend + wxmp |
| `zhaoxinwy-wxmp/**` | wxmp |
| `zhaoxinwy-admin/**` | backend |
| `zhaoxinwy-framework/**` | backend + wxmp |
| `zhaoxinwy-system/**` | backend + wxmp |
| `zhaoxinwy-common/**` | backend + wxmp |
| `zhaoxinwy-quartz/**` | backend |
| `zhaoxinwy-generator/**` | backend |
| `zhaoxinwy-workflow/**` | backend |
| `zhaoxinwy-tenant/**` | backend |
| `pms-web/**` | frontend |
| `sql/**` | backend + wxmp |
| `docker/**` | backend + wxmp + frontend |
| `pom.xml` / `**/pom.xml` | backend + wxmp + frontend |
| `docs/**`, `README.md` | 不触发构建 |

### 实现方式

使用 `dorny/paths-filter@v3` Action 检测变更文件路径，通过 `detect-changes` Job 输出构建决策，后续 Job 根据 `if` 条件决定是否执行。

---

## 3. 流水线工作流

### 3.1 触发条件

```yaml
on:
  push:
    branches: [develop, main, release/*, feature/resi-*]
  pull_request:
    branches: [develop, main]
```

### 3.2 Job 列表

| Job | 说明 | 依赖 | 触发条件 |
|---|---|---|---|
| `detect-changes` | 变更检测与构建决策 | - | 总是执行 |
| `code-check` | CLAUDE.md 规范检查 | detect-changes | PR 或 resi 代码变更 |
| `maven-compile-check` | Maven 全模块编译检查 | - | PR 时执行 |
| `frontend-lint` | ESLint 前端检查 | - | PR 时执行 |
| `build-backend` | 构建 B 端 Docker 镜像 | detect-changes, code-check | backend 变更时 |
| `build-wxmp` | 构建 C 端 Docker 镜像 | detect-changes, code-check | wxmp 变更时 |
| `build-frontend` | 构建前端 Docker 镜像 | detect-changes | frontend 变更时 |
| `deploy-test` | 部署到测试环境 | 以上全部 | develop/main/release/feature/resi-* push |

### 3.3 镜像管理

- **镜像仓库**：GitHub Container Registry (`ghcr.io`)
- **镜像名**：
  - `ghcr.io/caoyb888/zhiyuewy/backend`
  - `ghcr.io/caoyb888/zhiyuewy/wxmp`
  - `ghcr.io/caoyb888/zhiyuewy/frontend`
- **标签策略**：
  - `:{github.sha}` — 唯一版本标签
  - `:latest` — 最新标签
- **缓存**：使用 `gha` (GitHub Actions Cache) 加速 Docker 构建

---

## 4. 环境配置

### 4.1 GitHub Secrets 配置

在 GitHub 仓库 Settings → Secrets and variables → Actions 中配置：

| Secret | 说明 | 示例 |
|---|---|---|
| `TEST_SERVER_HOST` | 测试服务器 IP/域名 | `192.168.1.100` 或 `test.zhaoxinms.com` |
| `TEST_SERVER_USER` | SSH 用户名 | `deploy` |
| `TEST_SERVER_SSH_KEY` | SSH 私钥（免密登录） | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `TEST_SERVER_PORT` | SSH 端口（可选） | `22` |
| `GITHUB_TOKEN` | 自动提供，用于 GHCR 登录 | - |

### 4.2 测试服务器准备

```bash
# 1. 在测试服务器上创建工作目录
sudo mkdir -p /opt/zhaoxinpms
sudo chown deploy:deploy /opt/zhaoxinpms

# 2. 克隆代码仓库
cd /opt/zhaoxinpms
git clone https://github.com/caoyb888/zhiyuewy.git .

# 3. 配置 Docker 登录（用于拉取 GHCR 镜像）
echo "YOUR_GITHUB_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USER --password-stdin

# 4. 确保部署脚本可执行
chmod +x scripts/deploy-test.sh

# 5. 首次启动基础服务（MySQL + Redis）
docker compose up -d mysql redis
```

### 4.3 SSH 密钥配置

```bash
# 在测试服务器上生成部署专用密钥对
ssh-keygen -t ed25519 -C "deploy@zhaoxinms" -f ~/.ssh/deploy_key

# 将公钥添加到 authorized_keys
cat ~/.ssh/deploy_key.pub >> ~/.ssh/authorized_keys

# 将私钥内容添加到 GitHub Secrets: TEST_SERVER_SSH_KEY
cat ~/.ssh/deploy_key
```

---

## 5. 自动部署流程

### 5.1 部署触发条件

- `develop` 分支 push → 自动部署到测试环境
- `main` / `release/*` 分支 push → 自动部署到测试环境（可扩展为预发布）
- `feature/resi-*` 分支 push → 自动部署到测试环境（便于功能联调）
- PR **不触发**自动部署（仅构建镜像，不部署）

### 5.2 部署步骤

1. **SSH 连接到测试服务器**
2. **拉取最新 Docker 镜像**（仅更新有变更的服务）
3. **执行数据库迁移**（自动执行 `sql/0.11.0/*.sql`）
4. **更新并启动服务**（`docker compose up -d`）
5. **健康检查**：
   - Backend: `http://localhost:8080/actuator/health`
   - Wxmp: `http://localhost:8081/actuator/health`
   - Frontend: `http://localhost` (HTTP 200)
6. **冒烟测试**：
   - Swagger 文档可访问
   - `/resi/` API 路径可访问
   - `/wxmp/` API 路径可访问
   - 数据库 `resi_` 表存在
7. **清理旧镜像**

### 5.3 部署日志查看

```bash
# 在测试服务器上查看
cd /opt/zhaoxinpms
docker compose logs -f backend   # B端日志
docker compose logs -f wxmp      # C端日志
docker compose logs -f frontend  # 前端日志
```

---

## 6. 代码规范检查

### 6.1 检查项清单

基于 `CLAUDE.md` 禁止行为清单和检查项：

| # | 检查项 | 级别 | 说明 |
|---|---|---|---|
| 1 | 禁止修改非 `resi_` 前缀表 | ❌ 阻塞 | 检查 SQL 和 Java 代码中对 `payment_`/`config_`/`sys_` 等表的引用 |
| 2 | 禁止修改现有模块文件 | ❌ 阻塞 | 检查是否修改了 `payment`/`baseconfig`/`system` 等现有包 |
| 3 | 新接口路径规范 | ❌ 阻塞 | 新增接口必须以 `/resi/` 或 `/wxmp/` 开头 |
| 4 | 禁止 `double`/`float` 金额 | ❌ 阻塞 | 检查金额字段类型 |
| 5 | 禁止 `System.out.println` | ❌ 阻塞 | 检查 Java 代码 |
| 6 | 禁止硬编码密钥/密码 | ❌ 阻塞 | 检查密码/AppSecret/APIKey 硬编码 |
| 7 | 禁止 `${}` SQL 拼接 | ❌ 阻塞 | 检查 MyBatis XML 中的 `${}` |
| 8 | 前端禁止直接 `axios` | ❌ 阻塞 | 检查 Vue/JS 组件中是否直接调用 axios |
| 9 | 新增表前缀检查 | ❌ 阻塞 | 新增表必须以 `resi_` 开头 |
| 10 | 禁止 Lombok | ❌ 阻塞 | 检查 Lombok 注解使用 |
| 11 | Controller `@Log` 注解 | ⚠️ 警告 | 检查 Controller 是否包含操作日志注解 |

### 6.2 检查脚本使用

```bash
# 本地执行代码检查（对比 develop 分支）
chmod +x scripts/code-check.sh
scripts/code-check.sh origin/develop

# 对比指定分支
scripts/code-check.sh origin/main
```

### 6.3 PR 检查流程

1. 开发者提交 PR 到 `develop`
2. GitHub Actions 自动触发：
   - `code-check.yml` → CLAUDE.md 规范检查
   - `code-check.yml` → Maven 全模块编译
   - `code-check.yml` → 前端 ESLint
3. 任何检查失败 → PR 禁止合并
4. 全部通过 → 代码审查后可合并

---

## 7. 验收标准

### S0-04 验收标准

- [x] **配置 `zhaoxinwy-pms` 模块增量编译触发条件**
  - `zhaoxinwy-pms/**` 路径变更时，自动触发 backend 和 wxmp 构建
  - 实现方式：`dorny/paths-filter` + Job `if` 条件控制

- [x] **配置 `zhaoxinwy-wxmp` 新模块构建流程**
  - 独立 `build-wxmp` Job，构建并推送 Docker 镜像
  - 端口 8081，独立 Dockerfile 已配置
  - 健康检查端点：`/actuator/health`

- [x] **配置自动部署到测试环境**
  - `develop` 分支 push → 5 分钟内完成构建+部署
  - SSH + Docker Compose 增量更新
  - 部署后自动健康检查和冒烟测试

- [x] **配置代码检查（CLAUDE.md 规范检查项）**
  - `scripts/code-check.sh` 覆盖 11 项规范检查
  - PR 自动触发代码检查
  - 检查失败阻断 PR 合并

### Sprint 0 验收标准

- [ ] CI/CD：提交代码到 develop 分支，5 分钟内自动部署到测试环境
  - **实现**：GitHub Actions Workflow `ci-cd.yml`，`build-backend` + `build-wxmp` + `deploy-test` 流水线
- [ ] 访问 `http://test-server:8081/actuator/health` 返回 `{"status":"UP"}`（wxmp 服务启动）
  - **实现**：`zhaoxinwy-wxmp` 模块需添加 `spring-boot-starter-actuator` 依赖并配置

---

## 附录：Maven 增量编译命令参考

```bash
# 仅构建 backend 及其依赖模块
mvn clean package -pl zhaoxinwy-admin -am -DskipTests

# 仅构建 wxmp 及其依赖模块
mvn clean package -pl zhaoxinwy-wxmp -am -DskipTests

# 全量构建
mvn clean package -DskipTests

# 仅编译不打包（快速检查）
mvn clean compile -DskipTests
```

---

*本文档随 CI/CD 演进持续更新。*  
*最后更新：2026-05-10*
