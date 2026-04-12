# Skill 文档一致性校对报告（upbeat-hypatia）

日期：2026-04-12  
范围：`.claude/worktrees/upbeat-hypatia/Skill/` 变更文件（14 改 + 1 新）  
基线：`upbeat-hypatia` 工作树当前代码与配置（controller/service/model/schema/application.yml）

---

## 1. 总体结论

- 这批文档不是噪声，属于真实重写改动，结构质量整体提升明显。
- 目前不建议直接提交主线；需先修正 A 类硬冲突。
- 建议定位为：`需修订后采纳`。

分级统计：

- 一致：7
- 部分不一致：6
- 明显不一致：2

---

## 2. 逐文件分级

| 文件 | 结论 | 说明 |
|---|---|---|
| `Skill/00_概念与规范.md` | 部分不一致 | 鉴权口径与真实白名单冲突（`/api/eval/audio/full`） |
| `Skill/02_API/01_API总览.md` | 明显不一致 | 认证分区表述冲突 + 缺少 `/api/v1/evaluate` 核心接口 |
| `Skill/02_API/02_首页模块.md` | 部分不一致 | 返回字段名写成 `topicDate`，实际 DTO 字段为 `date` |
| `Skill/02_API/03_探索模块.md` | 一致 | 与控制器与鉴权口径一致 |
| `Skill/02_API/04_评估模块.md` | 部分不一致 | `audio/full` 标记为匿名可访问，与当前安全配置冲突 |
| `Skill/02_API/05_成长模块.md` | 一致 | 接口、参数、分页、鉴权与代码一致 |
| `Skill/02_API/06_个人模块.md` | 部分不一致 | “密码修改通过 /api/auth”超出当前实际接口能力 |
| `Skill/03_数据与存储/01_数据库设计.md` | 部分不一致 | 设备列名同时出现 `device_id/current_device_id`，关系图口径混用 |
| `Skill/03_数据与存储/02_表结构与索引.md` | 明显不一致 | `evaluation_tasks.status` 枚举值写错；`login_type` 值域写宽 |
| `Skill/03_数据与存储/03_画像数据模型.md` | 部分不一致 | “所有接口 persona 默认 EXAM_PREP”不成立（`audio/full` 必填） |
| `Skill/04_AI与评测/01_评测链路.md` | 部分不一致 | fallback `status` 仅写 `error`，与运行态多状态不完全一致 |
| `Skill/04_AI与评测/02_评分维度与解释.md` | 一致（带注） | 与 Prompt/Service 逻辑基本一致，可补充 `service_unavailable/parse_error` |
| `Skill/04_AI与评测/03_提示词与结构化输出.md` | 部分不一致 | 文档阈值 `<8词` 与服务前置校验 `<5词` 不一致 |
| `Skill/04_AI与评测/04_音频规范与处理.md` | 一致 | 转码、ASR、配置口径与实现一致 |
| `Skill/README.md` | 一致 | 新导航链接有效，结构清晰 |

---

## 3. A 类问题（必须修）

### A-1 `audio/full` 鉴权口径错误

- 文档写法：
  - `Skill/00_概念与规范.md`：匿名音频评估无需 token
  - `Skill/02_API/01_API总览.md`：同上
  - `Skill/02_API/04_评估模块.md`：`POST /api/eval/audio/full` 需要认证：否
- 代码事实：
  - 默认 `anyRequest().authenticated()`
  - 白名单仅含 `/api/auth/**`、`/api/home/daily`、`/actuator/health`、`/api/v1/evaluate/health` 等
  - 未对白名单放开 `/api/eval/audio/full`
- 证据：
  - `/Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend/.claude/worktrees/upbeat-hypatia/src/main/java/com/zhupinzan/speaking/config/SecurityConfig.java`
- 修订建议：
  - 将 `audio/full` 改为“当前安全配置下需认证访问（除非后续将该路径加入白名单）”。

### A-2 API 总览缺少异步评估主接口

- 文档缺失：`/api/v1/evaluate` 的提交、状态、历史接口未在总览中出现。
- 代码事实：
  - `POST /api/v1/evaluate`
  - `GET /api/v1/evaluate/{taskId}`
  - `GET /api/v1/evaluate/history`
- 证据：
  - `/Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend/.claude/worktrees/upbeat-hypatia/src/main/java/com/zhupinzan/speaking/controller/EvaluationController.java`
- 修订建议：
  - 在 `02_API/01_API总览.md` 增加“异步评估模块（/api/v1/evaluate）”。

### A-3 `daily` 返回字段名写错

- 文档写法：`topicDate`
- 代码事实：`DailyTopicDTO` 字段名为 `date`（Jackson 输出字段即 `date`）
- 证据：
  - `/Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend/src/main/java/com/zhupinzan/speaking/controller/DailyTopicDTO.java`
- 修订建议：
  - 将 `02_API/02_首页模块.md` 中 `topicDate` 全部改为 `date`。

### A-4 `evaluation_tasks.status` 枚举口径错误

- 文档写法：`PENDING/PROCESSING/DONE/FAILED`
- 代码事实：
  - 持久化枚举：`PENDING/COMPLETED/FAILED`
  - `PROCESSING` 仅存在于响应 DTO 枚举，当前映射逻辑不会落库该值
- 证据：
  - `/Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend/.claude/worktrees/upbeat-hypatia/src/main/java/com/zhupinzan/speaking/model/TaskStatus.java`
  - `/Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend/.claude/worktrees/upbeat-hypatia/src/main/java/com/zhupinzan/speaking/service/AsyncEvaluationService.java`
- 修订建议：
  - 表结构文档改为“数据库状态：`PENDING/COMPLETED/FAILED`；接口响应可见 `PROCESSING` 语义”。

### A-5 `login_type` 值域写宽

- 文档写法：`PASSWORD / APPLE`
- 代码事实：当前枚举只有 `PASSWORD`
- 证据：
  - `/Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend/.claude/worktrees/upbeat-hypatia/src/main/java/com/zhupinzan/speaking/model/LoginType.java`
- 修订建议：
  - 文档改为“当前实现：`PASSWORD`（预留扩展）”。

---

## 4. B 类问题（建议修）

### B-1 认证模块标题与子项描述自冲突

- 位置：`02_API/01_API总览.md`
- 问题：标题写“认证模块无需认证”，但表格又写 `/api/auth/me`、`/api/auth/bind-device` 需认证。
- 建议：改为“认证模块（注册/登录公开，其他需认证）”。

### B-2 persona 默认值描述过度泛化

- 位置：`03_数据与存储/03_画像数据模型.md`
- 问题：写“所有接口默认 EXAM_PREP”；但 `POST /api/eval/audio/full` 的 `persona` 为必填。
- 建议：改为“多数 GET/查询接口默认 EXAM_PREP，`/api/eval/audio/full` 必填”。

### B-3 DeepSeek 前置校验词数阈值不一致

- 位置：`04_AI与评测/03_提示词与结构化输出.md`
- 问题：文档用 `<8词`，服务前置校验是 `<5词`。
- 建议：明确区分：
  - Prompt 约束建议阈值（8）
  - 服务前置兜底阈值（5）

### B-4 fallback 状态值文档过窄

- 位置：`04_AI与评测/01_评测链路.md`、`04_AI与评测/02_评分维度与解释.md`
- 问题：仅强调 `error`，实际服务可能返回 `service_unavailable`、`parse_error`。
- 建议：补充“status 为字符串，常见值包括 `ok/no_speech/invalid_input/error/service_unavailable/parse_error`”。

### B-5 设备列名口径混用

- 位置：`03_数据与存储/01_数据库设计.md`、`03_数据与存储/02_表结构与索引.md`
- 问题：`device_id` 与 `current_device_id` 并存说明不够清晰。
- 建议：在文档顶部增加“环境差异说明（初始化 schema vs Hibernate update）”，并统一示例。

### B-6 个人模块范围声明超出实际能力

- 位置：`02_API/06_个人模块.md`
- 问题：“密码修改通过 /api/auth 模块处理”目前无对应端点。
- 建议：改为“暂不支持密码修改接口（当前版本）”。

---

## 5. C 类问题（可后置）

- 示例响应与真实响应字段顺序、示例值并非严格一致（不影响契约本身）。
- 部分“排障”段落偏策略性，建议后续统一风格（事实/推断分层）。

---

## 6. 是否可提交

当前结论：**不可直接提交**（A 类未修完）。  
建议流程：

1. 先修 A 类问题
2. 顺带修 B 类中的 B-1/B-2/B-3（低成本高收益）
3. 再发起“文档治理 PR（证据型）”

建议 PR 定位：

- 类型：`docs consistency`
- 范围：仅 `Skill/` 文档，不夹带业务代码
- 证明材料：本报告 + 对照代码引用

