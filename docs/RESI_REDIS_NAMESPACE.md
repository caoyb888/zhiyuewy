# 住宅收费模块 Redis 命名空间规范

**文档版本**：V1.0  
**编制日期**：2026-05-10  
**适用范围**：`zhaoxinwy-pms`（`com.zhaoxinms.resi` 包）+ `zhaoxinwy-wxmp`

---

## 1. 设计目的

住宅收费模块（resi）与现有商业收费体系并行运行，为避免 Redis Key 冲突、便于运维排查，所有住宅收费相关的 Redis Key 必须统一使用 `resi:` 前缀。

---

## 2. Key 命名规范

### 2.1 通用格式

```
resi:{模块}:{项目ID}:{业务标识}
```

| 段位 | 说明 | 示例 |
|---|---|---|
| `resi` | 固定前缀，标识住宅收费模块 | — |
| `{模块}` | 功能模块简称 | `dashboard`, `pay`, `receipt`, `meter`, `notice` |
| `{项目ID}` | project_id（可选，全局类 key 可省略） | `5`, `12` |
| `{业务标识}` | 具体业务唯一标识 | `overview`, `notify:ORDER202605100001` |

### 2.2 各模块 Key 示例

```
# 看板缓存（TTL 5 分钟）
resi:dashboard:5:overview
resi:dashboard:5:collection-trend
resi:dashboard:5:payment-method-dist
resi:dashboard:5:arrears-top10
resi:dashboard:5:fee-type-dist

# 支付回调幂等锁（TTL 60 秒）
resi:pay:notify:ORDER202605100001

# 收据号自增（TTL 至当日结束）
resi:receipt:no:5:20260510

# 抄表导入临时数据（TTL 30 分钟）
resi:meter:import:{batchId}

# C端短信验证码（TTL 5 分钟）
resi:sms:code:13800138000

# C端 JWT refreshToken（TTL 30 天）
resi:wxmp:refresh:{openid}
```

---

## 3. TTL 规范

| 数据类型 | 推荐 TTL | 说明 |
|---|---|---|
| 看板聚合数据 | 300 秒（5 分钟） | 收款成功后主动清除 |
| 支付回调幂等锁 | 60 秒 | 防止重复处理同一笔回调 |
| 收据号日序计数器 | 至当日 23:59:59 | 每日重置 |
| 抄表导入预览数据 | 1800 秒（30 分钟） | 导入确认后主动删除 |
| 短信验证码 | 300 秒（5 分钟） | 超期失效 |
| refreshToken | 2592000 秒（30 天） | C端自动登录续期 |

---

## 4. 编码约束

### 4.1 必须遵守

- ✅ 所有住宅收费模块产生的 Redis Key 必须以 `resi:` 开头
- ✅ 禁止使用 `keys("*")` 做全局扫描，使用 `scan` 或精确 Key 操作
- ✅ 禁止将金额、账单等核心业务数据只存 Redis 不落库
- ✅ 涉及多表写操作的缓存清除，在 Service 事务提交后执行

### 4.2 示例代码

```java
// 看板缓存写入
String key = "resi:dashboard:" + projectId + ":overview";
redisTemplate.opsForValue().set(key, jsonData, 300, TimeUnit.SECONDS);

// 收款成功后清除看板缓存
Set<String> keys = redisTemplate.keys("resi:dashboard:" + projectId + ":*");
if (keys != null) redisTemplate.delete(keys);

// 支付回调幂等锁
String lockKey = "resi:pay:notify:" + orderId;
Boolean locked = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, "1", 60, TimeUnit.SECONDS);
if (!locked) {
    return; // 已处理，直接返回成功（幂等）
}
```

---

## 5. 环境配置检查项

| 环境 | 检查项 | 验证方式 |
|---|---|---|
| Docker | `docker/wxmp/application-docker.yml` 已包含命名空间注释 | 文件审查 |
| 开发 | 开发人员本地 Redis 未与生产混用 | `redis-cli INFO keyspace` |
| 测试 | `resi:*` 前缀 Key 无异常增长 | `redis-cli --big-keys` |
| 上线 | 监控告警：Redis 内存使用率 > 80% | Prometheus/Grafana |

---

## 6. 相关文档

- [CLAUDE.md](../CLAUDE.md) — 第 5.4 节 Redis 使用规范
- [物业收费系统-新增收费模块技术方案.md](物业收费系统-新增收费模块技术方案.md)
