package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import com.zhupinzan.speaking.service.AuthUserService;
import com.zhupinzan.speaking.service.GrowthService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 用户成长记录REST控制器
 * <p>
 * 该控制器负责管理和提供用户学习成长相关的数据接口，主要用于：
 * - 展示用户的练习历史和进步轨迹
 * - 提供能力分析的可视化数据
 * - 支持用户查看详细的评估记录
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li><b>历史记录查询</b>：获取用户的评估历史列表，支持分页和筛选</li>
 *     <li><b>能力分析</b>：生成用于雷达图展示的各项能力统计数据</li>
 *     <li><b>详情查看</b>：获取单次评估的完整信息，包括AI反馈</li>
 *     <li><b>数据可视化</b>：为前端图表组件提供结构化的数据支持</li>
 * </ul>
 *
 * <h3>数据模型：</h3>
 * <ul>
 *     <li>AssessmentRecord：评估记录实体，包含完整的评估数据</li>
 *     <li>GrowthHistoryView：历史记录视图，优化查询性能</li>
 *     <li>RadarStatsDTO：雷达图统计，聚合用户能力数据</li>
 * </ul>
 *
 * <h3>性能优化：</h3>
 * <ul>
 *     <li>使用JPA Projection减少数据传输量</li>
 *     <li>限制查询结果数量防止性能问题</li>
 *     <li>使用数据库索引优化查询速度</li>
 * </ul>
 *
 * <h3>API端点：</h3>
 * <ul>
 *     <li>GET /api/growth/history - 获取评估历史列表</li>
 *     <li>GET /api/growth/analysis - 获取能力分析数据</li>
 *     <li>GET /api/growth/detail/{id} - 获取单次评估详情</li>
 * </ul>
 *
 * <h3>权限控制：</h3>
 * <ul>
 *     <li>所有端点都需要用户登录认证</li>
 *     <li>用户只能访问自己的评估数据</li>
 *     <li>通过设备ID进行数据隔离</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/growth")
public class GrowthController {

    /**
     * 评估记录数据仓库
     * <p>
     * 用于查询和管理用户的评估历史数据。该仓库提供了多个查询方法：
     * - findByDeviceIdAndPersona：按设备ID和画像筛选历史记录
     * - findRadarStats：计算用户能力统计数据
     * - findByIdAndDeviceId：查询特定评估记录
     *
     * <h3>性能优化特性：</h3>
     * <ul>
     *     <li>使用JPA Projection（GrowthHistoryView）只查询必要字段</li>
     *     <li>支持分页查询，避免加载过多数据</li>
     *     <li>使用索引优化查询性能</li>
     * </ul>
     */
    @Autowired
    private AssessmentRecordRepository repo;

    /**
     * 认证用户服务
     * <p>
     * 提供用户认证和设备管理功能：
     * - 验证用户登录状态
     * - 获取设备ID
     * - 确保数据访问权限
     */
    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private GrowthService growthService;

    /**
     * 获取用户评估历史列表API端点
     * <p>
     * 该接口用于获取用户的练习历史记录，主要用于在前端展示能力变化趋势。
     * 为了性能优化，使用了JPA Projection技术，只查询必要的字段，避免加载大文本内容。
     *
     * <h3>主要用途：</h3>
     * <ul>
     *     <li>绘制用户能力变化趋势图</li>
     *     <li>展示练习时间线和进度</li>
     *     <li>统计练习次数和频率</li>
     *     <li>为成长报告提供数据基础</li>
     * </ul>
     *
     * <h3>数据优化：</h3>
     * <ul>
     *     <li>使用GrowthHistoryView投影，只包含必要字段</li>
     *     <li>限制最大返回数量为200条记录</li>
     *     <li>按时间倒序排列，最新记录在前</li>
     *     <li>避免加载音频文件等大对象</li>
     * </ul>
     *
     * <h3>请求参数：</h3>
     * <ul>
     *     <li>persona: 用户画像，默认为EXAM_PREP（备考党）</li>
     *     <li>limit: 返回记录数量，默认50，最大200</li>
     * </ul>
     *
     * <h3>响应数据结构：</h3>
     * <pre>
     * [
     *     {
     *         "id": 12345,
     *         "createdAt": "2024-01-01T10:00:00Z",
     *         "overallScore": 85,
     *         "fluency": 80,
     *         "completeness": 75,
     *         "relevance": 90,
     *         "scene": "面试准备"
     *     },
     *     ...
     * ]
     * </pre>
     *
     * <h3>性能注意事项：</h3>
     * <ul>
     *     <li>单次查询最多返回200条记录</li>
     *     <li>使用数据库索引优化查询速度</li>
     *     <li>避免N+1查询问题</li>
     * </ul>
     *
     * @param user    当前登录用户信息（通过@CurrentUser注解注入）
     * @param persona 用户的画像类型，决定了筛选的标准
     *                - EXAM_PREP: 备考党相关记录
     *                - BUSINESS: 商务场景相关记录
     *                - ACADEMIC: 学术场景相关记录
     * @param limit    返回记录的数量限制
     *                - 默认值：50条记录
     *                - 最小值：1条记录
     *                - 最大值：200条记录（防止性能问题）
     * @return ResponseEntity 包含历史记录列表
     *         - 成功：200 OK，返回GrowthHistoryView对象列表
     *         - 未授权：401 Unauthorized（通过@CurrentUser自动处理）
     */
    @GetMapping("/history")
    public ResponseEntity<List<AssessmentRecordRepository.GrowthHistoryView>> getHistory(
            @CurrentUser CurrentUserInfo user,
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(value = "from", required = false) String from
    ) {
        // 步骤1：验证用户身份并获取设备ID
        var deviceId = authUserService.requireDeviceId(user);

        // 步骤2：对limit参数进行安全限制，防止客户端请求过大的数据量
        // 确保limit在1-200之间，避免数据库负载过重
        int safeLimit = Math.max(1, Math.min(limit, 200));

        // 步骤3：创建分页请求，按创建时间降序排序
        // PageRequest.of(0, safeLimit) 表示查询第一页，每页safeLimit条记录
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 步骤4：执行查询并返回结果
        // 通过设备ID和画像双重条件筛选，确保数据隔离和准确性
        if (from != null && !from.isBlank()) {
            var fromDate = parseOffsetDateTime(from);
            return ResponseEntity.ok(repo.findByDeviceIdAndPersonaAndCreatedAtAfter(deviceId, persona, fromDate, pageable));
        }
        return ResponseEntity.ok(repo.findByDeviceIdAndPersona(deviceId, persona, pageable));
    }

    /**
     * 获取用户能力雷达图分析数据API端点
     * <p>
     * 该接口通过数据库聚合查询计算用户在特定时间段内的能力平均分，
     * 为前端的雷达图可视化提供数据支持。这种设计可以减少前端的计算负担，
     * 并利用数据库的聚合能力提高性能。
     *
     * <h3>分析维度：</h3>
     * <ul>
     *     <li><b>流畅度 (Fluency)</b>：语速、停顿、自然程度等口语表达流畅性指标</li>
     *     <li><b>完整性 (Completeness)</b>：内容覆盖度、细节丰富度、信息完整性</li>
     *     <li><b>相关性 (Relevance)</b>：回答与题目的契合度、要点覆盖程度</li>
     *     <li><b>总体得分 (Overall)</b>：三个维度的加权平均分</li>
     * </ul>
     *
     * <h3>时间范围：</h3>
     * <ul>
     *     <li>默认分析最近90天的数据</li>
     *     <li>时间窗口可配置，反映用户近期表现</li>
     *     <li>排除早期的练习数据，聚焦当前水平</li>
     * </ul>
     *
     * <h3>数据计算方式：</h3>
     * <ol>
     *     <li>筛选指定时间段内的评估记录</li>
     *     <li>按能力维度分组计算平均值</li>
     *     <li>使用数据库聚合函数提高效率</li>
     *     <li>返回四舍五入后的整数分数</li>
     * </ol>
     *
     * <h3>响应数据结构：</h3>
     * <pre>
     * {
     *     "fluency": 82,        // 流畅度平均分
     *     "completeness": 78,   // 完整性平均分
     *     "relevance": 85,      // 相关性平均分
     *     "overall": 82         // 总体平均分
     * }
     * </pre>
     *
     * <h3>业务价值：</h3>
     * <ul>
     *     <li>直观展示用户的能力分布和强弱项</li>
     *     <li>帮助用户了解自己的进步情况</li>
     *     <li>为个性化学习建议提供数据支持</li>
     *     <li>激励用户持续练习和改进</li>
     * </ul>
     *
     * @param user    当前登录用户信息（通过@CurrentUser注解注入）
     * @param persona 用户的画像类型，影响筛选标准
     *                - EXAM_PREP: 备考党相关的能力分析
     *                - BUSINESS: 商务场景相关的能力分析
     *                - ACADEMIC: 学术场景相关的能力分析
     * @return ResponseEntity 包含能力统计数据
     *         - 成功：200 OK，返回RadarStatsDTO对象
     *         - 无数据：可能返回空对象或零值
     *         - 未授权：401 Unauthorized（通过@CurrentUser自动处理）
     */
    @GetMapping("/analysis")
    public ResponseEntity<AssessmentRecordRepository.RadarStatsDTO> getAnalysis(
            @CurrentUser CurrentUserInfo user,
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona,
            @RequestParam(value = "from", required = false) String from
    ) {
        // 步骤1：验证用户身份并获取设备ID
        var deviceId = authUserService.requireDeviceId(user);

        // 步骤2：定义时间范围 - 只统计最近90天的数据
        // 这个时间窗口既包含了足够的数据点，又反映了用户近期的真实水平
        var fromDate = (from == null || from.isBlank())
            ? OffsetDateTime.now().minusDays(90)
            : parseOffsetDateTime(from);

        // 步骤3：执行聚合查询并返回结果
        // findRadarStats方法会使用数据库的聚合功能高效计算各项能力的平均值
        return ResponseEntity.ok(growthService.getRadarStats(deviceId, persona, fromDate));
    }

    /**
     * 获取单次评估详细信息API端点
     * <p>
     * 当用户在历史记录列表中点击某一项时，客户端会调用此接口来获取该次评估的完整数据。
     * 这个接口返回的是评估的全部信息，包括AI的详细反馈、改进建议、音频链接等，
     * 让用户能够回顾和学习自己的表现。
     *
     * <h3>数据完整性：</h3>
     * <ul>
     *     <li>包含原始音频文件或链接</li>
     *     <li>语音识别的完整文本</li>
     *     <li>各项能力的详细评分</li>
     *     <li>AI生成的具体改进建议</li>
     *     <li>识别到的问题和替代方案</li>
     *     <li>评估的上下文信息（场景、提示等）</li>
     * </ul>
     *
     * <h3>安全特性：</h3>
     * <ul>
     *     <li>通过设备ID和评估ID双重条件查询</li>
     *     <li>确保用户只能访问自己的评估数据</li>
     *     <li>防止数据泄露和越权访问</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>用户回顾自己的练习表现</li>
     *     <li>查看AI的具体反馈和建议</li>
     *     <li>分析自己的弱项和改进方向</li>
     *     <li>重新听取录音，对比AI的建议</li>
     * </ul>
     *
     * <h3>响应数据结构：</h3>
     * <pre>
     * {
     *     "id": 12345,
     *     "deviceId": "device-123456789",
     *     "persona": "EXAM_PREP",
     *     "scene": "面试准备",
     *     "prompt": "描述你对未来的规划",
     *     "userText": "我希望成为一名工程师...",
     *     "audioUrl": "/api/audio/12345",
     *     "durationMs": 45000,
     *     "transcript": "我希望成为一名工程师...",
     *     "overallScore": 85,
     *     "fluency": 80,
     *     "completeness": 75,
     *     "relevance": 90,
     *     "feedback": "你的表达很清晰，逻辑性强...",
     *     "suggestions": ["注意语速变化", "增加具体例子"],
     *     "issues": [
     *         {
     *             "offset": 10,
     *             "length": 4,
     *             "message": "可以更具体",
     *             "replacements": ["详细规划"]
     *         }
     *     ],
     *     "createdAt": "2024-01-01T10:00:00Z"
     * }
     * </pre>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *     <li>404 Not Found - 评估记录不存在或无权访问</li>
     *     <li>401 Unauthorized - 用户未登录</li>
     *     <li>403 Forbidden - 用户无权访问该记录</li>
     * </ul>
     *
     * @param id       评估记录的唯一标识符（路径变量）
     *                 - 类型：Long
     *                 - 来源：前端从历史列表中获取
     *                 - 用途：定位特定的评估记录
     * @param user     当前登录用户信息（通过@CurrentUser注解注入）
     *                 - 用于权限验证
     *                 - 确保数据隔离
     * @return ResponseEntity 包含评估完整信息
     *         - 成功：200 OK，返回AssessmentRecord实体
     *         - 未找到：404 Not Found，记录不存在或无权访问
     *         - 未授权：401 Unauthorized（通过@CurrentUser自动处理）
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<AssessmentRecord> getDetail(
            @PathVariable Long id,
            @CurrentUser CurrentUserInfo user
    ) {
        // 步骤1：验证用户身份并获取设备ID
        var deviceId = authUserService.requireDeviceId(user);

        // 步骤2：通过id和deviceId双重条件查询，防止越权访问
        // 这种设计确保用户只能查看自己的评估数据，保护数据隐私和安全
        return repo.findByIdAndDeviceId(id, deviceId)
                .map(ResponseEntity::ok)          // 如果找到记录，返回200 OK和记录内容
                .orElse(ResponseEntity.notFound().build());  // 如果未找到，返回404 Not Found
    }

    /**
     * 获取单次评估轻量详情API端点（不含 transcript/metrics/feedback）。
     *
     * @param id   评估记录的唯一标识符（路径变量）
     * @param user 当前登录用户信息（通过@CurrentUser注解注入）
     * @return ResponseEntity 包含评估轻量信息
     *         - 成功：200 OK，返回GrowthDetailView
     *         - 未找到：404 Not Found
     */
    @GetMapping("/detail/{id}/lite")
    public ResponseEntity<AssessmentRecordRepository.GrowthDetailView> getDetailLite(
            @PathVariable Long id,
            @CurrentUser CurrentUserInfo user
    ) {
        var deviceId = authUserService.requireDeviceId(user);
        return repo.findDetailViewByIdAndDeviceId(id, deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private OffsetDateTime parseOffsetDateTime(String value) {
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "from 参数格式错误，需 ISO-8601，例如 2026-01-01T00:00:00Z"
            );
        }
    }
}
