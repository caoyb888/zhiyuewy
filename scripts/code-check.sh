#!/bin/bash
# ============================================================
# 肇新智慧物业 · 住宅物业收费模块代码规范检查脚本
# 版本：v1.0
# 日期：2026-05-10
# 负责人：BE-B
# 说明：基于 CLAUDE.md 禁止行为清单和检查项进行代码合规检查
# 用法：scripts/code-check.sh [base_branch]
#   base_branch: 对比的基准分支，默认 origin/develop
# ============================================================

set -euo pipefail

BASE_BRANCH="${1:-origin/develop}"
EXIT_CODE=0

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "CLAUDE.md 代码规范检查"
echo "基准分支: $BASE_BRANCH"
echo "当前分支: $(git rev-parse --abbrev-ref HEAD)"
echo "========================================"

# 获取变更文件列表
CHANGED_FILES=$(git diff --name-only "$BASE_BRANCH"...HEAD || true)
if [ -z "$CHANGED_FILES" ]; then
    echo -e "${YELLOW}警告: 未检测到变更文件，跳过检查${NC}"
    exit 0
fi

echo ""
echo "变更文件列表:"
echo "$CHANGED_FILES"
echo ""

# ------------------------------------------------------------
# 检查项 1: 禁止修改非 resi_ 前缀的表
# ------------------------------------------------------------
check_forbidden_tables() {
    echo "【检查项 1】禁止修改非 resi_ 前缀的表..."
    local violations=""

    # 检查 SQL 文件中是否有 ALTER/DROP/CREATE 非 resi_ 表
    local sql_files=$(echo "$CHANGED_FILES" | grep -E '\.(sql|xml)$' || true)
    if [ -n "$sql_files" ]; then
        violations=$(echo "$sql_files" | while read -r file; do
            if [ -f "$file" ]; then
                # 检查是否包含对禁止表的修改
                grep -iEn '(ALTER|DROP|CREATE|INSERT|UPDATE|DELETE).*\b(payment_|config_|sys_|owner_|park_|ACT_|qrtz_)' "$file" 2>/dev/null || true
            fi
        done)
    fi

    # 检查 Java 代码中是否引用了禁止的表名
    local java_files=$(echo "$CHANGED_FILES" | grep -E '\.(java|xml)$' || true)
    if [ -n "$java_files" ]; then
        violations="$violations$(echo "$java_files" | while read -r file; do
            if [ -f "$file" ]; then
                grep -iEn '\b(payment_|config_|sys_|owner_|park_|ACT_|qrtz_)[a-z_]+' "$file" 2>/dev/null | grep -v '//.*\b(payment_|config_|sys_)' | grep -v '\*.*\b(payment_|config_|sys_)' || true
            fi
        done)"
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到对非 resi_ 前缀表的引用或修改${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到对非 resi_ 前缀表的修改${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 2: 禁止修改现有 Controller / Service / Mapper
# ------------------------------------------------------------
check_existing_files() {
    echo ""
    echo "【检查项 2】禁止修改现有 Controller/Service/Mapper/Entity..."
    local violations=""

    # 检查是否修改了非 resi 命名空间的现有文件
    local modified_existing=$(echo "$CHANGED_FILES" | grep -E '(zhaoxinwy-pms/src/main/java/com/zhaoxinms/(payment|baseconfig)/.*|zhaoxinwy-system/src/main/java/com/zhaoxinms/.*|zhaoxinwy-framework/src/main/java/com/zhaoxinms/.*)' || true)

    if [ -n "$modified_existing" ]; then
        echo -e "${RED}❌ 失败: 检测到对现有模块文件的修改${NC}"
        echo "$modified_existing"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到对现有模块文件的修改${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 3: 新接口路径规范（必须以 /resi/ 或 /wxmp/ 开头）
# ------------------------------------------------------------
check_api_paths() {
    echo ""
    echo "【检查项 3】新接口路径规范检查..."
    local java_files=$(echo "$CHANGED_FILES" | grep '\.java$' || true)
    local violations=""

    if [ -n "$java_files" ]; then
        violations=$(echo "$java_files" | while read -r file; do
            if [ -f "$file" ] && echo "$file" | grep -q 'resi'; then
                # 检查 @RequestMapping / @GetMapping / @PostMapping 等
                grep -n '@\(RequestMapping\|GetMapping\|PostMapping\|PutMapping\|DeleteMapping\)' "$file" 2>/dev/null | grep -v '/resi/' | grep -v '/wxmp/' | grep -v 'value.*=' || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到非规范接口路径（必须以 /resi/ 或 /wxmp/ 开头）${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 新增接口路径符合规范${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 4: 禁止使用 double / float 存储金额
# ------------------------------------------------------------
check_float_usage() {
    echo ""
    echo "【检查项 4】金额字段类型检查（禁止 double/float）..."
    local java_files=$(echo "$CHANGED_FILES" | grep '\.java$' || true)
    local violations=""

    if [ -n "$java_files" ]; then
        violations=$(echo "$java_files" | while read -r file; do
            if [ -f "$file" ]; then
                # 检查金额相关字段是否使用了 double/float
                grep -iEn '(double|float)\s+.*(amount|money|price|fee|total|receivable|paid|balance|deposit)' "$file" 2>/dev/null || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到使用 double/float 存储金额${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到 double/float 金额字段${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 5: 禁止 System.out.println
# ------------------------------------------------------------
check_system_out() {
    echo ""
    echo "【检查项 5】禁止 System.out.println..."
    local java_files=$(echo "$CHANGED_FILES" | grep '\.java$' || true)
    local violations=""

    if [ -n "$java_files" ]; then
        violations=$(echo "$java_files" | while read -r file; do
            if [ -f "$file" ]; then
                grep -n 'System\.out\.println' "$file" 2>/dev/null || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到 System.out.println${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到 System.out.println${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 6: 禁止硬编码密钥/密码/AppSecret
# ------------------------------------------------------------
check_hardcoded_secrets() {
    echo ""
    echo "【检查项 6】禁止硬编码密钥/密码/AppSecret..."
    local violations=""

    # 检查 Java/JS/Vue/XML/Properties/YML 文件
    local code_files=$(echo "$CHANGED_FILES" | grep -E '\.(java|js|vue|xml|properties|yml|yaml)$' || true)

    if [ -n "$code_files" ]; then
        violations=$(echo "$code_files" | while read -r file; do
            if [ -f "$file" ]; then
                # 检测常见的密钥硬编码模式（排除注释行和配置文件中的占位符）
                grep -iEn '(password|secret|appsecret|appid|apikey|token)\s*[:=]\s*["'\'''][^"'\''"]+$' "$file" 2>/dev/null | \
                    grep -vi 'password.*:\s*\${' | \
                    grep -vi 'password.*=\s*\${' | \
                    grep -vi 'password.*placeholder' | \
                    grep -vi '//.*password' | \
                    grep -vi '/\*.*password' || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到可能的硬编码密钥/密码${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到硬编码密钥/密码${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 7: 禁止 ${} SQL 拼接（MyBatis XML）
# ------------------------------------------------------------
check_sql_injection() {
    echo ""
    echo "【检查项 7】MyBatis SQL 注入检查（禁止 \${} 拼接）..."
    local xml_files=$(echo "$CHANGED_FILES" | grep 'Mapper\.xml$' || true)
    local violations=""

    if [ -n "$xml_files" ]; then
        violations=$(echo "$xml_files" | while read -r file; do
            if [ -f "$file" ]; then
                grep -n '\${' "$file" 2>/dev/null || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到 MyBatis \${} SQL 拼接${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到 \${} SQL 拼接${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 8: 前端禁止直接调用 axios
# ------------------------------------------------------------
check_frontend_api() {
    echo ""
    echo "【检查项 8】前端禁止直接调用 axios..."
    local vue_files=$(echo "$CHANGED_FILES" | grep -E '\.(vue|js)$' || true)
    local violations=""

    if [ -n "$vue_files" ]; then
        violations=$(echo "$vue_files" | while read -r file; do
            if [ -f "$file" ] && echo "$file" | grep -q 'pms-web'; then
                # 排除 api/ 目录下的文件（允许在 api 目录中使用 axios）
                if ! echo "$file" | grep -q 'src/api'; then
                    grep -n "import axios" "$file" 2>/dev/null || true
                    grep -n "axios\." "$file" 2>/dev/null || true
                fi
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到前端组件中直接调用 axios${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 前端未直接调用 axios${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 9: 新增表必须以 resi_ 前缀
# ------------------------------------------------------------
check_new_table_prefix() {
    echo ""
    echo "【检查项 9】新增表前缀检查（必须以 resi_ 开头）..."
    local sql_files=$(echo "$CHANGED_FILES" | grep '\.sql$' || true)
    local violations=""

    if [ -n "$sql_files" ]; then
        violations=$(echo "$sql_files" | while read -r file; do
            if [ -f "$file" ]; then
                # 查找 CREATE TABLE 语句，检查是否以 resi_ 开头
                grep -iEn 'CREATE TABLE \`[^\`]+\`' "$file" 2>/dev/null | grep -vi 'CREATE TABLE \`resi_' | grep -vi 'CREATE TABLE \`base_billrule\`' | grep -vi 'CREATE TABLE \`sys_' || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到新增表未使用 resi_ 前缀${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 新增表前缀符合规范${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 10: 禁止引入 Lombok
# ------------------------------------------------------------
check_lombok() {
    echo ""
    echo "【检查项 10】禁止引入 Lombok..."
    local java_files=$(echo "$CHANGED_FILES" | grep '\.java$' || true)
    local violations=""

    if [ -n "$java_files" ]; then
        violations=$(echo "$java_files" | while read -r file; do
            if [ -f "$file" ]; then
                grep -n '@\(Data\|Slf4j\|Getter\|Setter\|NoArgsConstructor\|AllArgsConstructor\|Builder\)' "$file" 2>/dev/null || true
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${RED}❌ 失败: 检测到 Lombok 注解使用${NC}"
        echo "$violations"
        EXIT_CODE=1
    else
        echo -e "${GREEN}✅ 通过: 未检测到 Lombok 注解${NC}"
    fi
}

# ------------------------------------------------------------
# 检查项 11: Controller 必须有 @Log 注解
# ------------------------------------------------------------
check_controller_log() {
    echo ""
    echo "【检查项 11】Controller 操作日志注解检查..."
    local controller_files=$(echo "$CHANGED_FILES" | grep 'Controller\.java$' || true)
    local violations=""

    if [ -n "$controller_files" ]; then
        violations=$(echo "$controller_files" | while read -r file; do
            if [ -f "$file" ] && echo "$file" | grep -q 'resi'; then
                # 检查每个 @PostMapping / @PutMapping / @DeleteMapping 方法是否有 @Log
                # 这是一个简化检查，仅检查文件是否包含 @Log
                if ! grep -q '@Log' "$file"; then
                    echo "$file: 缺少 @Log 注解"
                fi
            fi
        done)
    fi

    if [ -n "$violations" ]; then
        echo -e "${YELLOW}⚠️ 警告: 以下 Controller 可能缺少 @Log 注解${NC}"
        echo "$violations"
        # 警告级别，不阻塞构建
    else
        echo -e "${GREEN}✅ 通过: Controller 日志注解检查通过${NC}"
    fi
}

# ------------------------------------------------------------
# 主执行流程
# ------------------------------------------------------------

check_forbidden_tables
check_existing_files
check_api_paths
check_float_usage
check_system_out
check_hardcoded_secrets
check_sql_injection
check_frontend_api
check_new_table_prefix
check_lombok
check_controller_log

echo ""
echo "========================================"
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}所有强制检查项通过 ✅${NC}"
else
    echo -e "${RED}存在检查失败项，请修复后重新提交 ❌${NC}"
fi
echo "========================================"

exit $EXIT_CODE
