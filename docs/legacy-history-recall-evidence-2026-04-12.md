# legacy 历史数据召回率补证（2026-04-12）

## 1. 目标

验证异步任务历史查询在“owner 主键统一后”的兼容召回能力，重点补证：

1. `owner_user_id is null` 的 legacy 历史是否可被同一用户召回
2. 兼容查询是否引入跨用户串数据
3. 主查 + 兼容补查合并后是否存在重复记录

## 2. 样本口径（第一轮）

本轮为可重复的本地集成样本（H2 PostgreSQL mode），构造 7 条任务：

1. 新模型主数据（目标用户）
- `N1`: `owner_user_id=1001`, `user_identity=USER:1001`
- `M1`: `owner_user_id=1001`, `user_identity=legacy@example.com`（用于验证去重）

2. legacy 数据（目标用户）
- `L1`: `owner_user_id=null`, `user_identity=legacy@example.com`
- `L2`: `owner_user_id=null`, `user_identity=apple-sub-1`
- `L3`: `owner_user_id=null`, `user_identity=USER:1001`

3. 非目标用户数据（污染样本）
- `O1`: `owner_user_id=2002`, `user_identity=USER:2002`
- `O2`: `owner_user_id=null`, `user_identity=other@example.com`

目标用户上下文：
- `ownerUserId=1001`
- `legacyOwnerKeys={legacy@example.com, apple-sub-1}`

## 3. 查询口径

和线上逻辑保持一致：

1. 主查：`findByOwnerUserIdOrderByCreatedAtDesc(1001)`
2. 兼容补查：`findByUserIdentityInOrderByCreatedAtDesc([legacy@example.com, apple-sub-1, USER:1001])`
3. 合并去重：按 `taskId` 去重
4. 结果排序：`createdAt` 倒序

legacy 候选定义（用于召回率分母）：
- `owner_user_id is null` 且 `user_identity in compatibilityKeys`

legacy 召回定义（用于召回率分子）：
- 最终返回集中 `owner_user_id is null` 且 `user_identity in compatibilityKeys`

## 4. 首轮统计结果

执行用例：
- `LegacyHistoryRecallEvidenceTest#shouldRecallLegacyHistoryWithoutLeakage`

执行命令：
- `mvn -q -Dtest=LegacyHistoryRecallEvidenceTest test`

结果：通过

统计：

1. 样本总量：`7`
2. 目标用户应召回总量（去重后）：`5`（`N1/M1/L1/L2/L3`）
3. 主查命中：`2`
4. 兼容补查命中：`5`
5. 合并前记录数：`7`
6. 去重后记录数：`5`
7. legacy 候选数（分母）：`3`（`L1/L2/L3`）
8. legacy 实际召回数（分子）：`3`
9. legacy 召回率：`100%`（样本内）
10. 串数据数：`0`（`O1/O2` 未进入结果）

## 5. 结论

在第一轮样本内，当前实现满足：

1. legacy 历史可被完整召回
2. 不会串出其他用户任务
3. 合并去重有效

可将“owner 统一后的兼容召回链路”标记为：
- **样本级补证通过**

## 6. 局限与下一步

当前局限：

1. 本轮样本为构造数据，不代表线上全量历史分布
2. 未覆盖真实线上异常 identity 形态（空串、大小写漂移、前后空白、未知前缀）

下一步建议：

1. 在 PostgreSQL 真实库执行同口径统计 SQL，给出真实召回率
2. 单独补一组“异常 identity 形态”回归样本
3. 根据真实缺口决定是否进入数据迁移（而不是直接重构查询）
