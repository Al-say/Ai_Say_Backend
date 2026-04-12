package com.zhupinzan.speaking.service.business;

import com.zhupinzan.speaking.model.UserPersona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 场景服务 - 管理用户练习场景和推荐提示词
 *
 * <h3>服务定位与功能概述</h3>
 * <p>
 * SceneService 是一个轻量级的服务，负责根据不同的用户画像提供相应的练习场景和提示词。
 * 该服务通过配置化的方式管理不同场景的内容，实现了内容与业务逻辑的分离，便于后期维护和扩展。
 * </p>
 *
 * <h3>核心业务流程和算法逻辑</h3>
 * <p>
 * <strong>场景匹配逻辑</strong>：
 * - 根据用户画像类型（UserPersona.EXAM_PREP 或 UserPersona.CAREER_GROWTH）选择对应的提示词列表
 * - 使用策略模式，通过条件判断选择不同的提示词集合
 * - 无复杂的算法逻辑，主要关注数据管理和场景划分
 * </p>
 *
 * <strong>提示词管理</strong>：
 * - 将配置文件中的字符串分割为提示词列表
 * - 支持分号分隔的批量配置，便于批量管理提示词
 * - 每个提示词对应一个具体的练习场景或话题
 * </p>
 *
 * <h3>与其他服务的协作关系</h3>
 * <p>
 * - 依赖 Spring 配置系统，通过 @Value 注入配置参数
 * - 作为数据提供方，为前端或其他服务提供场景提示词
 * - 与用户画像系统紧密配合，实现个性化的场景推荐
 * </p>
 *
 * <h3>数据处理和转换逻辑</h3>
 * <p>
 * - 使用 Arrays.asList() 将字符串配置转换为 List<String> 结构
 * - 通过字符串分割（split(";")）实现配置解析
 * - 保持数据的原始结构，不进行复杂的转换处理
 * </p>
 *
 * <h3>缓存策略和性能优化</h3>
 * <p>
 * - 采用静态缓存模式，在服务启动时加载所有配置数据
 * - 无运行时动态加载，避免频繁IO操作
 * - 使用内存存储，访问性能为 O(1)
 * - 无缓存失效策略，因为配置数据相对稳定
 * </p>
 *
 * <h3>错误处理和降级机制</h3>
 * <p>
 * - 当前实现较为简单，主要依赖配置系统的健壮性
 * - 如果配置缺失或格式错误，可能导致提示词列表为空
 * - 建议后续增加配置验证和默认值机制
 * </p>
 *
 * <h3>配置参数和使用场景</h3>
 * <p>
 * - scene.prompts.exam-prep：考试准备场景的提示词，以分号分隔
 * - scene.prompts.career-growth：职场成长场景的提示词，以分号分隔
 * - 使用示例：用户选择考试准备模式时，返回与学业、考试相关的提示词
 * - 适用场景：口语练习、面试准备、英语学习等多种练习场景
 * </p>
 *
 * <h3>扩展性和维护性考虑</h3>
 * <p>
 * <strong>扩展性设计</strong>：
 * - 轻量级架构，易于添加新的用户画像类型
 * - 配置外部化，无需修改代码即可更新提示词内容
 * - 清晰的接口设计，便于集成到其他模块
 * </p>
 *
 * <strong>维护性考虑</strong>：
 * - 代码结构简单，维护成本低
 * - 职责单一，修改影响范围小
 * - 配置与代码分离，降低部署风险
 * - 建议后续添加配置验证和日志记录功能
 * </p>
 */
@Service
public class SceneService {

    // 考试准备场景的提示词列表，通过分号分隔的字符串转换而来
    private final List<String> examPrepPrompts;
    // 职场成长场景的提示词列表，通过分号分隔的字符串转换而来
    private final List<String> careerGrowthPrompts;

    /**
     * 构造函数 - 初始化场景服务的提示词配置
     * <p>
     * 该构造函数通过 Spring 的依赖注入获取配置文件中的提示词数据，
     * 并将其解析为可用的列表格式。提示词数据以分号分隔，便于批量管理。
     * </p>
     *
     * <h3>配置参数说明</h3>
     * <dl>
     *   <dt><strong>scene.prompts.exam-prep</strong></dt>
     *   <dd>考试准备场景的提示词字符串，多个提示词用分号（;）分隔。
     *       示例："Describe your favorite book;Talk about your last exam experience"
     *       这些提示词主要用于帮助用户进行考试相关的口语练习。</dd>
     *
     *   <dt><strong>scene.prompts.career-growth</strong></dt>
     *   <dd>职场成长场景的提示词字符串，多个提示词用分号（;）分隔。
     *       示例："Describe your work environment;Talk about a challenge at work"
     *       这些提示词主要用于帮助用户进行职场相关的口语练习。</dd>
     * </dl>
     *
     * <h3>数据解析逻辑</h3>
     * <p>
     * - 使用 String.split(";") 方法将配置字符串分割为多个提示词
     * - 通过 Arrays.asList() 转换为不可变列表，确保线程安全
     * - 空字符串会被忽略，但不会产生空列表
     * </p>
     *
     * <h3>配置示例</h3>
     * <pre>{@code
     * scene:
     *   prompts:
     *     exam-prep: "Describe your favorite book;Talk about your last exam experience"
     *     career-growth: "Describe your work environment;Talk about a challenge at work"
     * }</pre>
     *
     * @param examPrepPromptsStr 考试准备场景的提示词字符串
     * @param careerGrowthPromptsStr 职场成长场景的提示词字符串
     */
    public SceneService(
            @Value("${scene.prompts.exam-prep}") String examPrepPromptsStr,
            @Value("${scene.prompts.career-growth}") String careerGrowthPromptsStr) {
        this.examPrepPrompts = Arrays.asList(examPrepPromptsStr.split(";"));
        this.careerGrowthPrompts = Arrays.asList(careerGrowthPromptsStr.split(";"));
    }

    public List<String> getRecommendedPrompts(UserPersona persona) {
        if (persona == UserPersona.EXAM_PREP) {
            return examPrepPrompts;
        } else {
            return careerGrowthPrompts;
        }
    }
}