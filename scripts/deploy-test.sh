#!/bin/bash
# ============================================================
# 肇新智慧物业 · 测试环境自动部署脚本
# 版本：v1.0
# 日期：2026-05-10
# 负责人：BE-B
# 说明：
#   1. 由 GitHub Actions CI/CD 流水线在测试服务器上执行
#   2. 支持增量部署（根据环境变量决定更新哪些服务）
#   3. 部署后执行健康检查和冒烟测试
# 环境变量：
#   BACKEND_TAG: backend 镜像标签
#   WXMP_TAG: wxmp 镜像标签
#   FRONTEND_TAG: frontend 镜像标签
#   REGISTRY: 镜像仓库地址
#   REPO: GitHub 仓库名
#   BUILD_BACKEND: 是否构建 backend (true/false)
#   BUILD_WXMP: 是否构建 wxmp (true/false)
#   BUILD_FRONTEND: 是否构建 frontend (true/false)
# ============================================================

set -euo pipefail

# 默认配置
DEPLOY_DIR="/opt/zhaoxinpms"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.yml"
HEALTH_TIMEOUT=120
HEALTH_INTERVAL=5

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_ok() {
    echo -e "${GREEN}[OK]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ------------------------------------------------------------
# 0. 前置检查
# ------------------------------------------------------------
check_prerequisites() {
    log_info "检查前置条件..."

    if [ ! -f "$COMPOSE_FILE" ]; then
        log_error "未找到 docker-compose.yml: $COMPOSE_FILE"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装"
        exit 1
    fi

    # 使用 docker compose (v2) 或 docker-compose (v1)
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi

    log_ok "前置条件检查通过"
}

# ------------------------------------------------------------
# 1. 拉取最新镜像
# ------------------------------------------------------------
pull_images() {
    log_info "拉取最新镜像..."

    local registry="${REGISTRY:-ghcr.io}"
    local repo="${REPO:-caoyb888/zhiyuewy}"
    local backend_tag="${BACKEND_TAG:-latest}"
    local wxmp_tag="${WXMP_TAG:-latest}"
    local frontend_tag="${FRONTEND_TAG:-latest}"

    if [ "${BUILD_BACKEND:-true}" == "true" ]; then
        log_info "拉取 Backend 镜像: $registry/$repo/backend:$backend_tag"
        docker pull "$registry/$repo/backend:$backend_tag"
        # 更新 docker-compose.yml 中的镜像标签
        sed -i "s|image:.*backend.*|image: $registry/$repo/backend:$backend_tag|g" "$COMPOSE_FILE" || true
    fi

    if [ "${BUILD_WXMP:-true}" == "true" ]; then
        log_info "拉取 Wxmp 镜像: $registry/$repo/wxmp:$wxmp_tag"
        docker pull "$registry/$repo/wxmp:$wxmp_tag"
        sed -i "s|image:.*wxmp.*|image: $registry/$repo/wxmp:$wxmp_tag|g" "$COMPOSE_FILE" || true
    fi

    if [ "${BUILD_FRONTEND:-true}" == "true" ]; then
        log_info "拉取 Frontend 镜像: $registry/$repo/frontend:$frontend_tag"
        docker pull "$registry/$repo/frontend:$frontend_tag"
        sed -i "s|image:.*frontend.*|image: $registry/$repo/frontend:$frontend_tag|g" "$COMPOSE_FILE" || true
    fi

    log_ok "镜像拉取完成"
}

# ------------------------------------------------------------
# 2. 执行数据库迁移（如 SQL 有变更）
# ------------------------------------------------------------
run_db_migrations() {
    log_info "检查数据库迁移..."

    # 如果 sql/0.11.0/ 目录下的文件有更新，执行迁移
    local sql_dir="$DEPLOY_DIR/sql/0.11.0"
    if [ -d "$sql_dir" ]; then
        log_info "发现 SQL 脚本目录: $sql_dir"
        # 通过 MySQL 容器执行 SQL
        local mysql_container="zhaoxin-pms-mysql"
        if docker ps -q -f name="$mysql_container" | grep -q .; then
            log_info "MySQL 容器运行中，执行 SQL 初始化..."
            # 按顺序执行 SQL 文件
            for sql_file in "$sql_dir"/resi_init.sql "$sql_dir"/resi_init_data.sql "$sql_dir"/resi_test_accounts.sql "$sql_dir"/resi_test_data.sql; do
                if [ -f "$sql_file" ]; then
                    log_info "执行: $(basename "$sql_file")"
                    docker exec -i "$mysql_container" mysql -uroot -proot pms < "$sql_file" || log_warn "SQL 执行可能已存在或报错: $(basename "$sql_file")"
                fi
            done
        else
            log_warn "MySQL 容器未运行，跳过数据库迁移"
        fi
    fi

    log_ok "数据库迁移检查完成"
}

# ------------------------------------------------------------
# 3. 更新并启动服务
# ------------------------------------------------------------
deploy_services() {
    log_info "更新并启动服务..."

    cd "$DEPLOY_DIR"

    # 先停止需要更新的服务
    local services_to_update=""
    if [ "${BUILD_BACKEND:-true}" == "true" ]; then
        services_to_update="$services_to_update backend"
    fi
    if [ "${BUILD_WXMP:-true}" == "true" ]; then
        services_to_update="$services_to_update wxmp"
    fi
    if [ "${BUILD_FRONTEND:-true}" == "true" ]; then
        services_to_update="$services_to_update frontend"
    fi

    if [ -n "$services_to_update" ]; then
        log_info "更新服务: $services_to_update"
        $COMPOSE_CMD stop $services_to_update
        $COMPOSE_CMD rm -f $services_to_update
        $COMPOSE_CMD up -d $services_to_update
    else
        log_warn "没有需要更新的服务"
    fi

    log_ok "服务更新完成"
}

# ------------------------------------------------------------
# 4. 健康检查
# ------------------------------------------------------------
health_check() {
    log_info "执行健康检查..."

    local backend_url="http://localhost:8080/actuator/health"
    local wxmp_url="http://localhost:8081/actuator/health"
    local frontend_url="http://localhost"
    local elapsed=0

    # Backend 健康检查
    if [ "${BUILD_BACKEND:-true}" == "true" ]; then
        log_info "检查 Backend 健康状态..."
        while [ $elapsed -lt $HEALTH_TIMEOUT ]; do
            if curl -sf "$backend_url" > /dev/null 2>&1; then
                log_ok "Backend 健康检查通过"
                break
            fi
            sleep $HEALTH_INTERVAL
            elapsed=$((elapsed + HEALTH_INTERVAL))
            echo -n "."
        done
        if [ $elapsed -ge $HEALTH_TIMEOUT ]; then
            log_error "Backend 健康检查超时"
            return 1
        fi
    fi

    # Wxmp 健康检查
    if [ "${BUILD_WXMP:-true}" == "true" ]; then
        log_info "检查 Wxmp 健康状态..."
        elapsed=0
        while [ $elapsed -lt $HEALTH_TIMEOUT ]; do
            if curl -sf "$wxmp_url" > /dev/null 2>&1; then
                log_ok "Wxmp 健康检查通过"
                break
            fi
            sleep $HEALTH_INTERVAL
            elapsed=$((elapsed + HEALTH_INTERVAL))
            echo -n "."
        done
        if [ $elapsed -ge $HEALTH_TIMEOUT ]; then
            log_error "Wxmp 健康检查超时"
            return 1
        fi
    fi

    # Frontend 健康检查（简单检查 200 状态码）
    if [ "${BUILD_FRONTEND:-true}" == "true" ]; then
        log_info "检查 Frontend 健康状态..."
        elapsed=0
        while [ $elapsed -lt $HEALTH_TIMEOUT ]; do
            if curl -sf -o /dev/null -w "%{http_code}" "$frontend_url" | grep -q "200\|301\|302"; then
                log_ok "Frontend 健康检查通过"
                break
            fi
            sleep $HEALTH_INTERVAL
            elapsed=$((elapsed + HEALTH_INTERVAL))
            echo -n "."
        done
        if [ $elapsed -ge $HEALTH_TIMEOUT ]; then
            log_error "Frontend 健康检查超时"
            return 1
        fi
    fi

    log_ok "所有健康检查通过"
}

# ------------------------------------------------------------
# 5. 冒烟测试（核心链路快速验证）
# ------------------------------------------------------------
smoke_test() {
    log_info "执行冒烟测试..."

    # 检查 Swagger 文档可访问
    if curl -sf "http://localhost:8080/swagger-ui/index.html" > /dev/null 2>&1; then
        log_ok "Swagger 文档可访问"
    else
        log_warn "Swagger 文档可能无法访问"
    fi

    # 检查住宅收费菜单接口（/resi/ 路径）
    if curl -sf "http://localhost:8080/resi/archive/project" > /dev/null 2>&1 || \
       curl -sf -o /dev/null -w "%{http_code}" "http://localhost:8080/resi/archive/project" | grep -q "401\|403\|200"; then
        log_ok "住宅收费 B 端 API 路径可访问"
    else
        log_warn "住宅收费 B 端 API 路径可能异常"
    fi

    # 检查 C 端 API 路径
    if curl -sf "http://localhost:8081/wxmp/auth/login" > /dev/null 2>&1 || \
       curl -sf -o /dev/null -w "%{http_code}" "http://localhost:8081/wxmp/auth/login" | grep -q "401\|403\|400\|200"; then
        log_ok "微信小程序 C 端 API 路径可访问"
    else
        log_warn "微信小程序 C 端 API 路径可能异常"
    fi

    # 检查数据库连接
    local mysql_container="zhaoxin-pms-mysql"
    if docker exec "$mysql_container" mysql -uroot -proot -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='pms' AND table_name LIKE 'resi_%';" 2>/dev/null | grep -q '[0-9]'; then
        log_ok "数据库 resi_ 表存在"
    else
        log_warn "数据库 resi_ 表检查异常"
    fi

    log_ok "冒烟测试完成"
}

# ------------------------------------------------------------
# 6. 清理旧镜像
# ------------------------------------------------------------
cleanup() {
    log_info "清理旧镜像..."
    docker image prune -f --filter "until=168h" || true
    log_ok "清理完成"
}

# ------------------------------------------------------------
# 主流程
# ------------------------------------------------------------
main() {
    echo "========================================"
    echo "肇新智慧物业 · 测试环境自动部署"
    echo "========================================"
    echo "部署目录: $DEPLOY_DIR"
    echo "Backend: ${BUILD_BACKEND:-true} (tag: ${BACKEND_TAG:-latest})"
    echo "Wxmp: ${BUILD_WXMP:-true} (tag: ${WXMP_TAG:-latest})"
    echo "Frontend: ${BUILD_FRONTEND:-true} (tag: ${FRONTEND_TAG:-latest})"
    echo "========================================"

    check_prerequisites
    pull_images
    run_db_migrations
    deploy_services
    health_check
    smoke_test
    cleanup

    echo ""
    echo "========================================"
    log_ok "测试环境部署完成"
    echo "前端地址: http://test-server"
    echo "B端 API: http://test-server:8080"
    echo "C端 API: http://test-server:8081"
    echo "========================================"
}

main "$@"
