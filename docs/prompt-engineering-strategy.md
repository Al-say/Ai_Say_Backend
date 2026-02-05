# DeepSeek Prompt 工程策略文档

## 📋 目标
确保 DeepSeek AI 稳定返回结构化 JSON，避免返回无关文本和格式错误。

---

## 🎯 核心设计原则

### 1. **强约束输出格式**
在 System Prompt 中明确声明"仅输出 JSON"，禁止任何额外说明。

```java
// ✅ 正确示例
"Output MUST be a single valid JSON object and NOTHING ELSE.
Do not wrap in markdown. Do not add code fences. Do not add explanations."

// ❌ 错误示例（容易导致 AI 返回解释性文本）
"Please provide a JSON response with the following structure..."
```

### 2. **JSON Schema 约束**
提供精确的 Schema 定义，包括：
- 所有必需字段
- 数据类型（int, string, array）
- 取值范围（如分数 0-100）
- 嵌套结构

### 3. **边界情况处理**
明确定义异常场景的处理规则：

| 场景 | status 字段 | 分数设置 | feedback.summary |
|-----|------------|---------|-----------------|
| 文本为空 | `no_speech` | 全部设为 0 | "未检测到语音输入" |
| 文本过短（<8词） | `invalid_input` | 全部设为 0 | "输入过短，无法评估" |
| 非英文文本 | `invalid_input` | 全部设为 0 | "检测到非英文内容" |
| AI 处理错误 | `error` | 全部设为 0 | 错误原因说明 |

---

## 🛠️ 当前 Prompt 分析

### System Prompt（已优化）
```java
public static String buildSystemPrompt() {
    return """
        You are a strict English speaking evaluator.
        
        Output MUST be a single valid JSON object and NOTHING ELSE.
        Do not wrap in markdown. Do not add code fences. Do not add explanations.
        All numeric scores must be integers from 0 to 100.
        If the transcript is empty, too short (< 8 words), or not English, set:
          status = "no_speech" or "invalid_input" and set all scores to 0.
        
        Your JSON must follow this schema:
        {
          "status": "ok|no_speech|invalid_input|error",
          "overallScore": int,
          "metrics": {
            "fluency": int,
            "completeness": int,
            "relevance": int,
            "pronunciation": int,
            "grammar": int,
            "vocabulary": int
          },
          "feedback": {
            "summary": string,
            "strengths": [string],
            "issues": [{"type": string, "evidence": string, "fix": string}],
            "suggestions": [string],
            "improvedVersion": string
          }
        }
        
        When status != "ok", feedback.summary should briefly explain why.
        """;
}
```

**✅ 优点：**
- 明确禁止 Markdown 包裹
- 提供完整 JSON Schema
- 定义边界情况处理
- 约束分数范围

**⚠️ 潜在改进：**
1. 添加"如果你不确定，给出保守分数"的指令
2. 强调"improvedVersion"必须是英文
3. 添加"不要重复原文"的约束

---

## 🔧 容错机制设计

### 1. 响应解析多层防护

```java
try {
    // 第一层：尝试直接解析
    result = om.readValue(jsonContent, DeepSeekEvalResult.class);
    
} catch (JsonProcessingException e) {
    // 第二层：尝试清理 Markdown 包裹
    String cleaned = jsonContent
        .replaceAll("```json", "")
        .replaceAll("```", "")
        .trim();
    result = om.readValue(cleaned, DeepSeekEvalResult.class);
    
} catch (Exception e) {
    // 第三层：返回降级结果
    return DeepSeekEvalResult.fallback("AI 响应解析失败");
}
```

### 2. Jackson 宽松配置

```java
ObjectMapper om = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
```

### 3. 降级策略

当 AI 完全无法返回有效结果时，使用预设响应：

```java
public static DeepSeekEvalResult fallback(String reason) {
    return new DeepSeekEvalResult(
        "error",
        0,
        Map.of("fluency", 0, "grammar", 0, ...),
        new Feedback(
            reason,
            List.of(),
            List.of(),
            List.of("请重新录音或检查网络"),
            ""
        )
    );
}
```

---

## 📊 测试场景清单

### ✅ 必测场景

1. **正常输入**
   - 长度：20-100 词
   - 语法正确的英文

2. **边界输入**
   - 空字符串
   - 单个单词
   - 7 个单词（临界值）
   - 非常长的文本（500+ 词）

3. **异常输入**
   - 纯数字
   - 中文/日文
   - HTML/代码片段
   - 特殊字符（emoji）

4. **并发测试**
   - 同时提交 10 个任务
   - 检查是否有线程安全问题

---

## 🚀 优化建议（未来版本）

### 1. Few-Shot Learning
在 Prompt 中提供 1-2 个示例输入输出，提高稳定性：

```
Example:
Input: "I like pizza very much."
Output: {"status": "ok", "overallScore": 65, ...}
```

### 2. Temperature 调整
```java
"temperature": 0.3  // 降低随机性，提高稳定性
```

### 3. 结构化输出（Function Calling）
使用 OpenAI 的 Function Calling 特性强制 JSON 格式（DeepSeek 是否支持待验证）。

### 4. 流式返回
对于长评估，使用 SSE 实时返回部分结果：
```
{progress: 30%} → {progress: 60%} → {final_result}
```

---

## ⚠️ 注意事项

1. **API 费用**：DeepSeek 按 Token 计费，避免过长的 Prompt
2. **延迟监控**：设置 30 秒超时，记录慢请求
3. **日志审计**：记录所有失败的 AI 响应用于分析
4. **A/B 测试**：对比不同 Prompt 版本的效果

---

## 📈 监控指标

| 指标 | 目标值 | 告警阈值 |
|-----|-------|---------|
| JSON 解析成功率 | > 99% | < 95% |
| 平均响应时间 | < 10s | > 20s |
| 降级触发率 | < 1% | > 5% |
| 异常状态占比 | < 2% | > 10% |

---

## 📝 版本历史

- **v1.0** (2026-02-05): 初始版本，基础 JSON 约束
- **v1.1** (计划): 添加 Few-Shot 示例
- **v2.0** (计划): 支持流式输出

---

**文档维护者**: System Architecture Team  
**最后更新**: 2026-02-05
