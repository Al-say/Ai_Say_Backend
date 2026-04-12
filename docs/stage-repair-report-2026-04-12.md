# 阶段修复报告（2026-04-12）

## 1. 结论

本轮“最小可用修复”已阶段完成，项目状态从“工程基线失稳”推进到“可继续联调与验证”。

已完成主线：
1. 可编译基线恢复
2. 安全与身份链路收口
3. 异步任务 owner 统一
4. DeepSeek 配置键收敛

## 2. 范围与边界

本轮覆盖：
- 编译可用性、认证基线、任务归属一致性、DeepSeek 配置治理

本轮不覆盖：
- DeepSeek 业务调用链重构
- 多轮对话与弱网优化
- 全量集成测试和生产监控完善

## 3. 已完成项（含代码落点）

### 3.1 可编译基线恢复

- 通过编译配置排除冲突副本文件（`*_冲突文件_*`）参与编译。
- 结果：`mvn -DskipTests clean compile` 可通过。

主要文件：
- `pom.xml`

### 3.2 安全与身份链路收口

- 默认策略改为默认鉴权（非白名单接口需认证）。
- 移除 `@CurrentUser` 伪用户注入与 dev 自动用户旁路。
- JWT 过滤器要求关键声明并写入 `auth.userId/auth.email/auth.appleSub`。

主要文件：
- `src/main/java/com/zhupinzan/speaking/config/SecurityConfig.java`
- `src/main/java/com/zhupinzan/speaking/config/CurrentUserArgumentResolver.java`
- `src/main/java/com/zhupinzan/speaking/filter/JwtAuthenticationFilter.java`
- `src/main/java/com/zhupinzan/speaking/util/JwtUtil.java`
- `src/main/java/com/zhupinzan/speaking/service/AuthUserService.java`

### 3.3 异步任务 owner 统一

- 归属主键统一为 `ownerUserId`。
- 提交/状态/历史三条入口统一使用当前登录用户 `userId`。
- 兼容期保留 legacy key（`email/appleSub`）召回并在服务层去重。
- 状态查询增加 owner 约束，阻断 taskId 越权读取。

主要文件：
- `src/main/java/com/zhupinzan/speaking/model/entity/EvaluationTask.java`
- `src/main/java/com/zhupinzan/speaking/repository/EvaluationTaskRepository.java`
- `src/main/java/com/zhupinzan/speaking/service/AsyncEvaluationService.java`
- `src/main/java/com/zhupinzan/speaking/controller/EvaluationController.java`
- `schema.sql`

### 3.4 DeepSeek 配置键收敛

- 建立统一配置入口：`deepseek.api.*`（主配置）。
- 旧键仅作 fallback：`deepseek.base-url`、`deepseek.api-key`、`deepseek.api.key`、`deepseek.api.url`。
- DeepSeek 消费点统一读取同一配置对象（不再多处散落 `@Value`）。
- 启动日志输出配置来源与是否使用 deprecated fallback（不打印密钥明文）。

主要文件：
- `src/main/java/com/zhupinzan/speaking/config/DeepSeekApiProperties.java`
- `src/main/java/com/zhupinzan/speaking/config/DeepSeekClientConfig.java`
- `src/main/java/com/zhupinzan/speaking/service/DeepSeekEvalService.java`
- `src/main/java/com/zhupinzan/speaking/service/DeepSeekService.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-template.properties`

## 4. 已执行验证

执行时间：2026-04-12

1. 编译验证
- 命令：`mvn -DskipTests clean compile -q`
- 结果：通过

2. DeepSeek 配置优先级单测
- 新增测试：`src/test/java/com/zhupinzan/speaking/config/DeepSeekApiPropertiesTest.java`
- 覆盖场景：
  - 新键 only
  - 旧键 only（fallback）
  - 新旧同时存在（新键优先）
- 命令：`mvn -q -Dtest=DeepSeekApiPropertiesTest test`
- 结果：通过

## 5. 待补证项（联调后）

1. 异步历史旧数据召回完整性
- 需预置 legacy 样本（`owner_user_id is null` + `email/appleSub`）验证召回完整率

2. 白名单业务可用性
- `GET /api/home/daily` 已修复并补测试（见 6.4），待在 PostgreSQL 环境做一次等价复核

3. 生产环境等价验证
- 本次为本地 H2 联调，仍需在目标环境（PostgreSQL + 真实配置）复核一次

## 6. 联调验证结果（第 1 包）

执行时间：2026-04-12
验证环境：本地 Spring Boot + H2（端口 2581/2582/2583）

### 6.1 权限矩阵验证

1. 白名单接口
- `GET /actuator/health`（未登录）→ `200`，通过
- `POST /api/auth/login`（未登录 + 错误密码）→ `401`，接口可达且未被鉴权层拦截，通过

2. 受保护接口
- `GET /api/auth/me`（未登录）→ `401`，通过
- `GET /api/v1/evaluate/history`（未登录）→ `403`，符合“未认证拒绝（401/403）”预期，通过
- `GET /api/auth/me`（有效 token）→ `200`，通过

3. 缺少 `userId` claim 的 token
- 使用自签名 JWT（仅 `sub`，无 `userId`）访问 `GET /api/auth/me` → `401`，通过
- 日志证据：`JWT 缺少必要声明 ... userId=null`

补充观察：
- `GET /api/home/daily`（白名单）已完成单独修复，见 6.4。

### 6.2 异步任务闭环验证

1. 新数据闭环
- 用户 A 提交 `POST /api/v1/evaluate` → `202`（返回 taskId）
- 用户 A 查询 `GET /api/v1/evaluate/{taskId}` → `200`
- 用户 A 查询 `GET /api/v1/evaluate/history` → `200`
- 历史记录包含该 taskId，且计数 `1`（无重复）

2. 越权校验
- 用户 B 查询用户 A 的 taskId → `403`（无权访问），通过

3. 兼容路径触发情况
- 日志中出现 `user_identity in (?, ?)` 查询，说明兼容查询路径被执行。
- 说明：本次未预置“历史 legacy 数据样本”（`owner_user_id is null` + `email/appleSub`），因此“旧数据召回完整性”仍需补证。

### 6.3 DeepSeek 三组配置实测

1. 新键 only（`deepseek.api.base-url/api-key`）
- 启动日志：`baseUrl=deepseek.api.base-url, apiKey=deepseek.api.api-key`
- 结论：通过

2. 旧键 only（`deepseek.base-url/api-key` + 新键置空）
- 启动日志：`(deprecated fallback in use)`
- 来源：`baseUrl=deepseek.base-url (deprecated), apiKey=deepseek.api.key (deprecated)`
- 结论：通过（fallback 生效）

3. 新旧同时存在且值冲突
- 启动日志仍显示新键来源：`deepseek.api.base-url / deepseek.api.api-key`
- `effective baseUrl` 命中新键值（未命中 legacy 值）
- 结论：通过（新键优先）

安全性检查：
- 日志仅输出 `apiKeyPresent=true` 与配置来源，未打印密钥明文，通过

### 6.4 `/api/home/daily` 500 修复验证

问题现象（修复前）：
- 第一次调用 `GET /api/home/daily` 返回 `200`，第二次返回 `500`
- 关键异常：`UnexpectedRollbackException`

根因：
1. `DailyTopic.payload` 同时使用 `JsonBinaryType` 与 `@JdbcTypeCode(SqlTypes.JSON)`，触发 JSON 双重编码，读缓存时报错：
   `The given string value: "{\"source\":\"static_fallback\"}" cannot be transformed to Json object`
2. `DailyChallengeService.getOrCreate` 处于单事务内，虽然捕获了读异常，但事务已被标记 rollback-only，最终提交时抛 `UnexpectedRollbackException`
3. 清理坏数据使用派生删除，触发实体态删除异常（detached instance）

修复动作：
- `DailyTopic` 去掉 `@Type(JsonBinaryType.class)`，仅保留 `@JdbcTypeCode(SqlTypes.JSON)`
- `DailyTopicRepository.deleteByTopicDateAndPersona` 改为原生 SQL `DELETE`
- `DailyChallengeService.getOrCreate` 移除方法级事务，避免“吞异常后提交回滚”
- 新增集成测试：`HomeControllerDailyIntegrationTest`（连续两次调用 `/api/home/daily` 均应 `200`）

验证结果：
- 命令：`mvn -q -Dtest=DailyChallengeServiceTest,HomeControllerDailyIntegrationTest test`
- 结果：通过
- 观察：`HomeControllerDailyIntegrationTest` 中两次 `/api/home/daily` 均返回 `200`

## 7. 残余风险

1. 冲突副本文件目前是“编译排除”，尚未“物理清理”。
2. owner 兼容查询属于过渡策略，需后续数据迁移后收口。
3. DeepSeek 调用链虽已统一配置入口，但异常语义和 fail-fast 仍可继续强化。

## 8. 下一阶段建议优先级

1. 接口级联调与回归验证（先证）
2. 冲突文件物理清理（仓库治理）
3. DeepSeek 调用链异常与 fail-fast 加固
4. 补齐关键单测/集成测试
5. 进入多轮对话与弱网体验优化
