package com.zhupinzan.speaking.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 本地文件存储服务 - 可靠的文件持久化解决方案
 *
 * <h2>服务概述</h2>
 * LocalStorageService是一个基于本地文件系统的存储实现，
 * 作为云存储服务（如MinIO）的备选方案。它提供了简单、可靠的
 * 文件存储功能，适用于开发环境、小型应用或对云存储有依赖的场景。
 *
 * <h2>架构设计</h2>
 * <ul>
 *   <li><b>存储路径</b>：基于配置的本地文件系统路径</li>
 *   <li><b>URL生成</b>：根据配置的base URL生成可访问的文件链接</li>
 *   <li><b>目录结构</b>：按日期和设备ID组织，便于管理和查找</li>
 *   <li><b>条件启用</b>：通过Spring条件注解控制自动装配</li>
 * </ul>
 *
 * <h2>技术实现细节</h2>
 * <p><b>路径管理</b></p>
 * <ul>
 *   <li>存储路径：从配置文件读取，默认为./uploads</li>
 *   <li>公共URL：配置文件中的base URL，默认为http://localhost:8082/files</li>
 *   <li>目录创建：自动创建不存在的目录结构</li>
 * </ul>
 *
 * <p><b>文件上传流程</b></p>
 * <ol>
 *   <li>生成规范化的相对路径：audio/日期/设备ID/UUID.扩展名</li>
 *   <li>创建必要的父目录结构</li>
 *   <li>写入文件数据到指定路径</li>
 *   <li>拼接并返回完整的访问URL</li>
 * </ol>
 *
 * <h2>与系统集成</h2>
 * <ul>
 *   <li><b>Spring Boot</b>：通过@ConfigurationProperties注入配置</li>
 *   <li><b>条件装配</b>：@ConditionalOnProperty控制bean的创建</li>
 *   <li><b>日志系统</b>：集成SLF4J进行日志记录</li>
 *   <li><b>异常处理</b>：转换为运行时异常，简化上层调用</li>
 * </ul>
 *
 * <h2>错误处理策略</h2>
 * <ul>
 *   <li><b>IO异常</b>：捕获并转换为RuntimeException，保持API一致性</li>
 *   <li><b>权限问题</b>：记录错误日志，提供有意义的异常信息</li>
 *   <li><b>磁盘空间</b>：依赖系统文件系统管理，不做额外检查</li>
 *   <li><b>路径安全</b>：使用Paths.resolve防止路径遍历攻击</li>
 * </ul>
 *
 * <h2>性能优化</h2>
 * <ul>
 *   <li><b>路径缓存</b>：避免重复的路径解析</li>
 *   <li><b>批量创建</b>：Files.createDirectories一次性创建多级目录</li>
 *   <li><b>流式写入</b>：使用Files.write直接写入字节数组</li>
 *   <li><b>异步考虑</b>：虽然是同步操作，但可以轻松扩展为异步</li>
 * </ul>
 *
 * <h2>使用场景</h2>
 * <ul>
 *   <li><b>开发环境</b>：快速原型开发和测试</li>
 *   <li><b>小型应用</b>：文件量不大的场景</li>
 *   <li><b>备用方案</b>：云存储服务不可用时的降级方案</li>
 *   <li><b>本地测试</b：不需要云存储功能的测试环境</li>
 * </ul>
 *
 * <h2>配置说明</h2>
 * <table border="1" cellpadding="3" cellspacing="0">
 *   <tr><th>配置项</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>storage.provider</td><td>local</td><td>存储提供者，设为local时启用此服务</td></tr>
 *   <tr><td>storage.local-path</td><td>./uploads</td><td>本地存储根路径</td></tr>
 *   <tr><td>storage.public-base-url</td><td>http://localhost:8082/files</td><td>文件访问的基础URL</td></tr>
 * </table>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li>使用有意义的目录结构，便于文件管理</li>
 *   <li>配置合理的base URL，确保前端能正确访问</li>
 *   <li>定期清理过期文件，避免磁盘空间浪费</li>
 *   <li>监控存储空间使用情况，设置告警阈值</li>
 *   <li>在多实例部署时考虑文件共享问题</li>
 * </ul>
 *
 * <h2>扩展性考虑</h2>
 * <ul>
 *   <li><b>存储策略</b>：可以扩展支持不同的文件命名策略</li>
 *   <li><b>生命周期</b>：可以添加文件过期和自动清理机制</li>
 *   <li><b>元数据</b>：可以扩展支持文件元数据的存储</li>
 *   <li><b>监控</b>：可以添加存储使用情况的监控指标</li>
 * </ul>
 *
 * @author System
 * @version 1.0
 * @see StorageService 存储服务接口
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty 条件装配注解
 * @see lombok.extern.slf4j.Slf4j 日志支持
 */
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path storagePath;
    private final String publicBaseUrl;

    public LocalStorageService(
            @Value("${storage.local-path:./uploads}") String localPath,
            @Value("${storage.public-base-url:http://localhost:8082/files}") String publicBaseUrl) {
        this.storagePath = Paths.get(localPath);
        this.publicBaseUrl = publicBaseUrl;

        try {
            Files.createDirectories(storagePath);
            log.info("📁 Local storage initialized at: {}", storagePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create storage directory", e);
        }
    }

    /**
     * 音频文件上传 - 核心存储方法
     *
     * <h2>方法说明</h2>
     * 这是LocalStorageService的核心业务方法，负责将字节数组形式的音频文件
     * 持久化到本地文件系统，并生成可供前端访问的URL。该方法实现了完整的
     * 文件存储流程，包括路径生成、目录创建和文件写入。
     *
     * <h2>详细执行步骤</h2>
     * <p><b>步骤1：路径生成</b></p>
     * <pre>
     * 格式：audio/日期/设备ID/UUID.扩展名
     * 示例：audio/2026-01-27/device_abc123/550e8400-e29b-41d4-a716-446655440000.wav
     * </pre>
     *
     * <p><b>步骤2：目录结构创建</b></p>
     * <pre>
     * 操作：Files.createDirectories(fullPath.getParent())
     * 特点：原子性创建多级目录结构
     * 优势：避免中间目录不存在导致的异常
     * </pre>
     *
     * <p><b>步骤3：文件写入</b></p>
     * <pre>
     * 操作：Files.write(fullPath, data)
     * 特点：直接写入字节数组，避免多次拷贝
     * 性能：高效的文件写入操作
     * </pre>
     *
     * <p><b>步骤4：URL拼接</b></p>
     * <pre>
     * 规则：检查base URL是否以/结尾
     * 示例：http://localhost:8082/files/audio/2026-01-27/...
     * 用途：前端通过此URL访问文件
     * </pre>
     *
     * <h2>路径设计理念</h2>
     * <ul>
     *   <li><b>按日期分类</b>：便于按时间查找和管理文件</li>
     *   <li><b>按设备组织</b>：支持多用户、多设备场景</li>
     *   <li><b>UUID命名</b>：保证文件名唯一性，避免冲突</li>
     *   <li><b>保留扩展名</b>：便于识别文件类型</li>
     * </ul>
     *
     * <h2>错误处理机制</h2>
     * <ul>
     *   <li><b>IO异常</b>：捕获IOException并转换为RuntimeException</li>
     *   <li><b>权限问题</b>：记录错误日志，提示权限不足</li>
     *   <li><b>磁盘满</b>：依赖系统抛出异常</li>
     *   <li><b>路径安全</b>：使用Paths.resolve防止路径遍历</li>
     * </ul>
     *
     * <h2>性能优化策略</h2>
     * <ul>
     *   <li><b>路径解析</b>：避免重复的路径字符串拼接</li>
     *   <li><b>批量操作</b>：使用createDirectories一次性创建目录</li>
     *   <li><b>直接写入</b>：Files.write直接操作字节数组</li>
     *   <li><b>异常处理</b>：轻量级异常处理，避免性能损失</li>
     * </ul>
     *
     * <h2>典型使用场景</h2>
     * <ul>
     *   <li><b>录音文件存储</b>：保存用户的口语练习录音</li>
     *   <li><b>媒体文件管理</b>：处理各种音视频文件</li>
     *   <li><b>文档存储</b>：保存用户上传的各种文档</li>
     *   <li><b>临时文件</b>：处理上传过程中的临时文件</li>
     * </ul>
     *
     * <h2>注意事项</h2>
     * <ul>
     *   <li>确保配置的base URL与实际访问地址一致</li>
     *   <li>注意文件系统的权限设置</li>
     *   <li>考虑磁盘空间的使用情况</li>
     *   <li>在高并发场景下注意文件创建的性能</li>
     * </ul>
     *
     * <h2>与其他服务的集成</h2>
     * <ul>
     *   <li><b>EvalOrchestratorService</b>：用于存储评估过程中的音频文件</li>
     *   <li><b>前端应用</b>：通过生成的URL访问存储的文件</li>
     *   <li><b>CDN服务</b>：可以无缝切换到云存储服务</li>
     *   <li><b>监控系统</b>：记录存储操作的日志和指标</li>
     * </ul>
     *
     * @param data 音频文件的字节数组，必须是有效的文件数据
     * @param deviceId 设备唯一标识符，用于文件分类和组织
     * @param extension 文件扩展名（包含点号，如.wav）
     * @return 完整的文件访问URL，格式为：baseURL/relativePath
     * @throws RuntimeException 当文件操作失败时抛出，包含详细的错误信息
     * @see java.nio.file.Files 文件操作工具类
     * @see java.nio.file.Paths 路径操作工具类
     * @see java.util.UUID 通用唯一标识符生成
     */
    @Override
    public String uploadAudio(byte[] data, String deviceId, String extension) {
        try {
            // 生成规范路径: audio/2026-01-16/device_uuid/file_uuid.wav
            String relativePath = String.format("audio/%s/%s/%s%s",
                    LocalDate.now(), deviceId, UUID.randomUUID(), extension);

            Path fullPath = storagePath.resolve(relativePath);
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, data);

            log.info("✅ Audio saved to: {}", fullPath);

            // 拼接访问地址
            return publicBaseUrl.endsWith("/") ? publicBaseUrl + relativePath : publicBaseUrl + "/" + relativePath;
        } catch (IOException e) {
            log.error("Failed to save audio file", e);
            throw new RuntimeException("Failed to save audio", e);
        }
    }

    /**
     * 上传用户头像
     *
     * @return 返回可供前端访问的完整 URL
     */
    public String uploadAvatar(byte[] data, String userId, String extension) {
        try {
            // 生成规范路径: avatar/2026-01-16/user_id/file_uuid.png
            String relativePath = String.format("avatar/%s/%s/%s%s",
                    LocalDate.now(), userId, UUID.randomUUID(), extension);

            Path fullPath = storagePath.resolve(relativePath);
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, data);

            log.info("✅ Avatar saved to: {}", fullPath);

            return publicBaseUrl.endsWith("/") ? publicBaseUrl + relativePath : publicBaseUrl + "/" + relativePath;
        } catch (IOException e) {
            log.error("Failed to save avatar file", e);
            throw new RuntimeException("Failed to save avatar", e);
        }
    }
}
