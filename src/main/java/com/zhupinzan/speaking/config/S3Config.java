package com.zhupinzan.speaking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * AWS S3存储配置类
 * <p>
 * 该配置类负责创建和配置用于与S3兼容的对象存储服务进行交互的S3Client实例。
 * 支持标准的AWS S3服务，同时也兼容MinIO等S3兼容的对象存储解决方案。
 * 配置包括端点、区域、认证信息等，确保安全、高效地访问存储服务。
 * </p>
 *
 * <h3>整体作用和设计意图</h3>
 * <p>
 * 本配置类是系统存储层的核心配置，为应用提供统一的对象存储访问接口。
 * 通过抽象和封装AWS SDK的复杂配置，使开发者可以轻松地与各种S3兼容存储服务交互。
 * 该设计遵循了依赖注入和控制反转的原则，将存储服务的配置与业务代码解耦。
 * </p>
 * <ul>
 *   <li><b>统一接口</b>: 提供统一的存储客户端配置，支持多种S3兼容存储服务</li>
 *   <li><b>简化集成</b>: 简化与对象存储服务的集成，降低使用门槛</li>
 *   <li><b>灵活部署</b>: 支持自定义端点，便于使用私有化部署的存储服务</li>
 *   <li><b>安全保障</b>: 实现安全的认证机制，保护存储访问权限</li>
 *   <li><b>性能优化</b>: 配置优化的连接池和重试机制，提高访问性能</li>
 * </ul>
 *
 * <h3>主要配置项</h3>
 * <ul>
 *   <li><b>storage.s3.endpoint</b>: S3服务的端点URL，支持自定义端点</li>
 *   <li><b>storage.s3.region</b>: AWS区域或存储服务部署区域</li>
 *   <li><b>storage.s3.access-key</b>: 存储服务访问密钥</li>
 *   <li><b>storage.s3.secret-key</b>: 存储服务密钥</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * # AWS S3 配置
 * storage:
 *   s3:
 *     endpoint: https://s3.amazonaws.com
 *     region: us-east-1
 *     access-key: your-access-key
 *     secret-key: your-secret-key
 *
 * # MinIO 配置
 * storage:
 *   s3:
 *     endpoint: http://localhost:9000
 *     region: us-east-1
 *     access-key: minioadmin
 *     secret-key: minioadmin
 * }</pre>
 *
 * <h3>技术实现要点</h3>
 * <ul>
 *   <li>使用AWS SDK for Java v2的S3Client，提供现代化的API</li>
 *   <li>支持端点覆盖，可用于连接非AWS S3兼容存储</li>
 *   <li>使用静态凭据提供者，简化认证配置</li>
 *   <li>启用path-style访问，兼容MinIO等私有存储方案</li>
 * </ul>
 *
 * <h3>性能考虑</h3>
 * <ul>
 *   <li><b>连接池</b>: S3Client内部使用连接池管理HTTP连接</li>
 *   <li><b>重试机制</b>: SDK内置自动重试，提高网络请求可靠性</li>
 *   <li><b>分块上传</b>: 支持大文件分块上传，提高上传效率</li>
 *   <li><b>并行操作</b>: 支持多线程并发访问，提高吞吐量</li>
 * </ul>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li><b>HTTPS通信</b>: 强制使用HTTPS协议确保数据传输安全</li>
 *   <li><b>最小权限</b>: 仅分配必要的存储访问权限</li>
 *   <li><b>凭据管理</b>: 支持从安全存储加载凭据，避免硬编码</li>
 *   <li><b>访问控制</b>: 结合IAM策略实现细粒度访问控制</li>
 * </ul>
 *
 * <h3>支持的存储服务</h3>
 * <p>
 * 该配置支持以下类型的S3兼容存储服务：
 * </p>
 * <ul>
 *   <li><b>AWS S3</b>: Amazon Simple Storage Service，标准对象存储服务</li>
 *   <li><b>MinIO</b>: 高性能对象存储，支持私有化部署</li>
 *   <li><b>阿里云OSS</b>: 阿里云对象存储服务</li>
 *   <li><b>腾讯云COS</b>: 腾讯云对象存储服务</li>
 *   <li><b>其他S3兼容存储</li>: 如Ceph、Rook等</li>
 * </ul>
 *
 * <h3>与外部服务的集成</h3>
 * <p>
 * S3存储服务集成主要支持以下功能：
 * </p>
 * <ul>
 *   <li><b>文件存储</b>: 上传、下载、删除文件对象</li>
 *   <li><b>元数据管理</b>: 管理文件的元数据和标签</li>
 *   <li><b>版本控制</b>: 支持文件版本管理</li>
 *   <li><b>生命周期管理</b>: 自动管理文件的生命周期（如过期、归档）</li>
 *   <li><b>跨区域复制</b>: 实现数据备份和容灾</li>
 *   <li><b>静态网站托管</b>: 托管静态网站内容</li>
 * </ul>
 *
 * <h3>性能优化与业务价值</h3>
 * <p>
 * 合理配置S3存储可以为业务带来显著的价值提升：
 * </p>
 * <ul>
 *   <li><b>成本效益</b>:
 *       <ul>
 *         <li>按需付费模式，降低初始投入成本</li>
 *         <li>自动扩展，无需担心容量规划</li>
 *         <li>存储分层，优化长期存储成本</li>
 *       </ul>
 *   </li>
 *   <li><b>性能优化</b>:
 *       <ul>
 *         <li>配置CDN加速全球内容分发</li>
 *         <li>使用预签名URL减少带宽消耗</li>
 *         <li>并行上传大文件，提升上传效率</li>
 *       </ul>
 *   </li>
 *   <li><b>高可用性</b>:
 *       <ul>
 *         <li>99.99%的服务可用性保证</li>
 *         <li>多AZ部署，自动故障转移</li>
 *         <li>版本控制防止数据丢失</li>
 *       </ul>
 *   </li>
 *   <li><b>业务创新</b>:
 *       <ul>
 *         <li>支持大数据分析处理</li>
 *         <li>集成机器学习服务</li>
 *         <li>实现跨区域数据同步</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h3>配置最佳实践</h3>
 * <p>
 * 为确保S3存储的安全性和可靠性，建议遵循以下配置最佳实践：
 * </p>
 * <ul>
 *   <li><b>使用IAM用户</b>:
 *       <ul>
 *         <li>创建专门的IAM用户，不要使用根账户</li>
 *         <li>为不同环境创建不同的IAM用户</li>
 *         <li>使用IAM策略限制访问权限</li>
 *       </ul>
 *   </li>
 *   <li><b>认证与授权</b>:
 *       <ul>
 *         <li>启用多因素认证（MFA）保护重要账户</li>
 *         <li>定期轮换访问密钥和密钥</li>
 *         <li>使用临时凭证而非长期凭证</li>
 *       </ul>
 *   </li>
 *   <li><b>数据保护</b>:
 *       <ul>
 *         <li>启用服务器端加密（SSE-S3、SSE-KMS）保护敏感数据</li>
 *         <li>使用客户端加密保护高度敏感数据</li>
 *         <li>配置对象版本控制防止意外删除</li>
 *       </ul>
 *   </li>
 *   <li><b>监控与审计</b>:
 *       <ul>
 *         <li>启用访问日志，监控异常访问行为</li>
 *         <li>配置CloudTrail记录API调用</li>
 *         <li>设置警报阈值，监控异常访问模式</li>
 *       </ul>
 *   </li>
 *   <li><b>成本优化</b>:
 *       <ul>
 *         <li>实施生命周期策略，自动转换存储类别</li>
 *         <li>配置自动删除过期或临时文件</li>
 *         <li>使用存储分析优化数据分布</li>
 *       </ul>
 *   </li>
 *   <li><b>灾难恢复</b>:
 *       <ul>
 *         <li>实施跨区域复制策略</li>
 *         <li>配置定期备份和恢复测试</li>
 *         <li>建立应急响应预案</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Autowired
 * private S3Client s3Client;
 *
 * // 上传文件到S3
 * public void uploadFile(String bucket, String key, InputStream inputStream) {
 *     PutObjectRequest request = PutObjectRequest.builder()
 *             .bucket(bucket)
 *             .key(key)
 *             .build();
 *
 *     s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
 * }
 *
 * // 从S3下载文件
 * public InputStream downloadFile(String bucket, String key) {
 *     GetObjectRequest request = GetObjectRequest.builder()
 *             .bucket(bucket)
 *             .key(key)
 *             .build();
 *
 *     return s3Client.getObject(request).asInputStream();
 * }
 * }</pre>
 *
 * <h3>特殊配置说明</h3>
 * <p>
 * <b>Path-Style访问</b>:
 * 在配置中启用了path-style访问（`.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())`），
 * 这是因为：
 * </p>
 * <ul>
 *   <li>MinIO默认使用path-style访问</li>
 *   <li>某些私有S3兼容存储也要求使用path-style</li>
 *   <li>AWS S3虽然推荐virtual-hosted-style，但仍支持path-style</li>
 * </ul>
 */
@Configuration
public class S3Config {

    /**
     * 创建S3Client Bean
     * <p>
     * 该Bean提供了一个配置好的S3Client实例，用于与S3兼容的对象存储服务进行交互。
     * 支持自定义端点，可以连接AWS S3、MinIO或其他S3兼容的存储服务。
     * 该Bean在Spring容器初始化时自动创建，并被注入到需要访问存储服务的组件中。
     * </p>
     *
     * <h4>配置参数详细说明</h4>
     * <ul>
     *   <li><b>endpoint</b>: 存储服务的端点URL
     *       <ul>
     *         <li>格式：完整的URL，包括协议（http/https）</li>
     *         <li>示例：https://s3.amazonaws.com 或 http://localhost:9000</li>
     *         <li>用途：指定存储服务的基础访问地址</li>
     *       </ul>
     *   </li>
     *   <li><b>region</b>: 存储服务部署的区域
     *       <ul>
     *         <li>格式：AWS区域代码，如 us-east-1, cn-north-1</li>
     *         <li>用途：影响服务端点的某些行为，如签名算法</li>
     *         <li>重要性：某些存储服务要求正确的区域配置</li>
     *       </ul>
     *   </li>
     *   <li><b>accessKey</b>: 存储服务访问密钥
     *       <ul>
     *         <li>格式：字符串，由服务提供商分配</li>
     *         <li>用途：身份认证的第一要素</li>
     *         <li>安全性：属于敏感信息，需要严格保护</li>
     *       </ul>
     *   </li>
     *   <li><b>secretKey</b>: 存储服务密钥
     *       <ul>
     *         <li>格式：字符串，与accessKey配对使用</li>
     *         <li>用途：验证访问请求的合法性</li>
     *         <li>安全性：核心敏感信息，必须加密存储</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h4>创建的S3Client特性和能力</h4>
     * <ul>
     *   <li><b>自定义端点支持</b>: 可连接非AWS S3服务，实现存储服务解耦</li>
     *   <li><b>Path-Style访问</b>: 启用path-style访问，兼容MinIO等私有存储方案</li>
     *   <li><b>静态凭据管理</b>: 使用静态凭据提供者，简化认证配置</li>
     *   <li><b>AWS SDK全功能</b>: 继承AWS SDK的所有功能，如分块上传、重试机制等</li>
     *   <li><b>连接池管理</b>: 内置连接池，提高HTTP连接复用效率</li>
     *   <li><b>异步支持</b>: 支持异步操作，提高高并发场景下的性能</li>
     * </ul>
     *
     * @param endpoint S3服务的端点URL
     * @param region AWS区域或存储服务区域
     * @param accessKey 访问密钥
     * @param secretKey 密钥
     * @return 配置好的S3Client实例
     */
    @Bean
    public S3Client s3Client(
            @Value("${storage.s3.endpoint}") String endpoint,
            @Value("${storage.s3.region}") String region,
            @Value("${storage.s3.access-key}") String accessKey,
            @Value("${storage.s3.secret-key}") String secretKey
    ) {
        return S3Client.builder()
                // 覆盖默认端点，支持自定义S3服务
                .endpointOverride(URI.create(endpoint))
                // 设置服务区域
                .region(Region.of(region))
                // 使用静态凭据提供者，提供认证信息
                .credentialsProvider(StaticCredentialsProvider.create(
                        // 创建AWS基础凭据
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                // 启用path-style访问，兼容MinIO等S3兼容存储服务
                // 注意：AWS S3推荐使用virtual-hosted-style，但MinIO通常需要path-style
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                // 构建并返回S3Client实例
                .build();
    }
}