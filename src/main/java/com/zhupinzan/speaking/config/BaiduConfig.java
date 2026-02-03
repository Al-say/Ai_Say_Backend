package com.zhupinzan.speaking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 百度AI服务配置类
 * <p>
 * 该配置类用于管理百度AI服务的相关配置信息，主要包括百度AI平台的三个核心参数：
 * 应用ID（AppID）、API密钥（API Key）和Secret Key。这些配置主要用于接入百度AI提供的
 * 各种服务，如语音识别、自然语言处理、图像识别等。
 * </p>
 *
 * <h3>整体作用和设计意图</h3>
 * <p>
 * 本配置类是系统AI服务层的重要组成部分，为应用提供与百度AI平台无缝对接的能力。
 * 通过集中管理配置参数，实现了配置与业务逻辑的分离，提高了系统的可维护性和可测试性。
 * 该设计遵循了Spring Boot的自动配置原则，简化了第三方服务的集成过程。
 * </p>
 * <ul>
 *   <li><b>配置集中化</b>: 集中管理百度AI服务的配置参数，避免在代码中硬编码敏感信息</li>
 *   <li><b>多源支持</b>: 通过Spring Boot的配置属性绑定机制，支持多种配置源（application.yml、环境变量等）</li>
 *   <li><b>类型安全</b>: 提供类型安全的配置对象，便于其他服务注入使用</li>
 *   <li><b>前缀绑定</b>: 支持配置的前缀绑定，提高配置的可维护性和避免命名冲突</li>
 *   <li><b>扩展性</b>: 便于扩展新的配置项，支持未来功能升级</li>
 * </ul>
 *
 * <h3>主要配置项</h3>
 * <ul>
 *   <li><b>appId</b>: 百度AI应用ID，用于标识应用程序的唯一标识符</li>
 *   <li><b>apiKey</b>: 百度AI API密钥，用于身份认证和请求授权</li>
 *   <li><b>secretKey</b>: 百度AI Secret密钥，用于签名验证和敏感操作</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * # application.yml
 * baidu:
 *   appId: your_app_id_here
 *   apiKey: your_api_key_here
 *   secretKey: your_secret_key_here
 * }</pre>
 *
 * <h3>技术实现要点</h3>
 * <ul>
 *   <li>使用@ConfigurationProperties注解实现配置属性自动绑定</li>
 *   <li>通过prefix="baidu"指定配置前缀，避免命名冲突</li>
 *   <li>使用@Data注解自动生成getter/setter方法，简化JavaBean开发</li>
 *   <li>使用@Component注解将该配置类注册为Spring Bean</li>
 * </ul>
 *
 * <h3>支持的百度AI服务</h3>
 * <p>
 * 该配置类主要支持百度AI平台以下几类服务：
 * </p>
 * <ul>
 *   <li><b>语音技术</b>: 语音识别（ASR）、语音合成（TTS）、语音唤醒</li>
 *   <li><b>自然语言处理</b>: 文本分析、情感分析、实体识别、机器翻译</li>
 *   <li><b>图像技术</b>: 图像识别、图像分类、人脸检测、OCR文字识别</li>
 *   <li><b>知识图谱</b>: 实体检索、关系抽取、语义搜索</li>
 * </ul>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li><b>敏感信息保护</b>: apiKey和secretKey属于敏感信息，应通过环境变量或配置中心管理</li>
 *   <li><b>访问控制</b>: 定期轮换API密钥，避免长期使用同一密钥</li>
 *   <li><b>权限最小化</li>: 只申请必要的API权限，遵循最小权限原则</li>
 *   <li><b>监控审计</li>: 记录API调用日志，监控异常使用行为</li>
 * </ul>
 *
 * <h3>与外部服务的集成</h3>
 * <p>
 * 百度AI服务集成采用标准的REST API调用方式：
 * </p>
 * <ul>
 *   <li>使用HTTP/HTTPS协议进行通信</li>
 *   <li>通过API Key进行身份验证</li>
 *   <li>部分服务需要使用Secret Key进行签名计算</li>
 *   <li>返回JSON格式的响应数据</li>
 * </ul>
 * <p>
 * 集成流程通常包括：
 * </p>
 * <ol>
 *   <li>配置百度AI开放平台账号并创建应用</li>
 *   <li>获取AppID、API Key和Secret Key</li>
 *   <li>根据不同服务的API文档构造请求参数</li>
 *   <li>发送HTTP请求到百度AI服务端点</li>
 *   <li>解析并处理响应结果</li>
 * </ol>
 *
 * <h3>配置最佳实践</h3>
 * <ul>
 *   <li><b>配置隔离</b>: 不同环境（开发、测试、生产）使用不同的配置</li>
 *   <li><b>敏感信息管理</b>: 使用配置中心或密钥管理服务存储敏感信息</li>
 *   <li><b>配置验证</b>: 在应用启动时验证必要配置项是否正确</li>
 *   <li><b>配置更新</li>: 实现配置热更新机制，无需重启应用即可生效</li>
 *   <li><b>文档管理</li>: 为配置项添加详细的文档说明和示例</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Autowired
 * private BaiduConfig baiduConfig;
 *
 * // 使用配置访问百度AI服务
 * public void callBaiduAIService() {
 *     String appId = baiduConfig.getAppId();
 *     String apiKey = baiduConfig.getApiKey();
 *     String secretKey = baiduConfig.getSecretKey();
 *
 *     // 调用百度AI服务...
 * }
 * }</pre>
 */
@Configuration
@ConfigurationProperties(prefix = "baidu")
@Component
public class BaiduConfig {
    /**
     * 百度AI应用ID
     * <p>
     * 应用ID是百度AI平台分配给每个应用的唯一标识符，用于：
     * <ul>
     *   <li>识别不同的应用</li>
     *   <li>统计应用使用量</li>
     *   <li>管理应用权限</li>
     * </ul>
     */
    private String appId;

    /**
     * 百度AI API密钥
     * <p>
     * API密钥用于调用百度AI的公共API，主要功能包括：
     * <ul>
     *   <li>HTTP请求的身份认证</li>
     *   <li>访问权限控制</li>
     *   <li>使用量统计</li>
     * </ul>
     * <p>
     * 注意：API密钥可用于调用大部分公开API，但对于敏感操作可能还需要Secret Key。
     */
    private String apiKey;

    /**
     * 百度AI Secret密钥
     * <p>
     * Secret密钥是百度AI平台提供的另一个密钥，主要用于：
     * <ul>
     *   <li>请求签名验证</li>
     *   <li>安全相关的API调用</li>
     *   <li>防止请求被篡改</li>
     * </ul>
     * <p>
     * 注意：Secret密钥比API Key更加敏感，应严格保密。
     */
    private String secretKey;

    public String getAppId() {
        return appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}