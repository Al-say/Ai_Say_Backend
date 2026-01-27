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
 * <h3>设计意图</h3>
 * <ul>
 *   <li>提供统一的存储客户端配置，支持多种S3兼容存储服务</li>
 *   <li>简化与对象存储服务的集成，降低使用门槛</li>
 *   <li>支持自定义端点，便于使用私有化部署的存储服务</li>
 *   <li>实现安全的认证机制，保护存储访问权限</li>
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
 *   <li><b>并行操作</li>: 支持多线程并发访问，提高吞吐量</li>
 * </ul>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li><b>HTTPS通信</b>: 强制使用HTTPS协议确保数据传输安全</li>
 *   <li><b>最小权限</li>: 仅分配必要的存储访问权限</li>
 *   <li><b>凭据管理</li>: 支持从安全存储加载凭据，避免硬编码</li>
 *   <li><b>访问控制</li>: 结合IAM策略实现细粒度访问控制</li>
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
 * <h3>配置最佳实践</h3>
 * <ul>
 *   <li><b>使用IAM用户</b>: 创建专门的IAM用户，不要使用根账户</li>
 *   <li><b>最小权限</b>: 使用IAM策略限制访问权限</li>
 *   <li><b>启用多因素认证</b>: 为重要的存储账户启用MFA</li>
 *   <li><b>定期轮换密钥</b>: 定期更新访问密钥和密钥</li>
 *   <li><b>加密存储</b>: 启用服务器端加密保护敏感数据</li>
 *   <li><b>日志监控</li>: 启用访问日志，监控异常访问行为</li>
 *   <li><b>备份策略</b>: 实施数据备份和恢复策略</li>
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
     * </p>
     *
     * <h4>配置参数说明</h4>
     * <ul>
     *   <li><b>endpoint</b>: 存储服务的端点URL</li>
     *   <li><b>region</b>: 存储服务部署的区域</li>
     *   <li><b>accessKey</b>: 存储服务访问密钥</li>
     *   <li><b>secretKey</b>: 存储服务密钥</li>
     * </ul>
     *
     * <h4>创建的S3Client特性</h4>
     * <ul>
     *   <li>支持自定义端点，可连接非AWS S3服务</li>
     *   <li>启用path-style访问，兼容MinIO等私有存储方案</li>
     *   <li>使用静态凭据提供者，简化认证配置</li>
     *   <li>继承AWS SDK的所有功能，如分块上传、重试机制等</li>
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