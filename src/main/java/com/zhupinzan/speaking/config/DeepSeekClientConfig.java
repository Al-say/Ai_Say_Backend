package com.zhupinzan.speaking.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * DeepSeek API客户端配置类
 * <p>
 * 该配置类负责创建和配置用于与DeepSeek AI服务进行HTTP通信的WebClient实例。
 * DeepSeek是一个大型语言模型服务提供商，通过REST API提供自然语言处理能力。
 * 本配置类优化了网络连接和认证配置，确保与DeepSeek API的高效、安全通信。
 * </p>
 *
 * <h3>设计意图</h3>
 * <ul>
 *   <li>提供统一的HTTP客户端配置，确保所有与DeepSeek API的请求使用相同的连接参数</li>
 *   <li>实现自动认证管理，通过Bearer Token进行API访问控制</li>
 *   <li>优化网络性能，包括超时设置和连接池管理</li>
 *   <li>支持环境变量配置，提高部署灵活性</li>
 * </ul>
 *
 * <h3>主要配置项</h3>
 * <ul>
 *   <li><b>deepseek.base-url</b>: DeepSeek API的基础URL，默认为"https://api.deepseek.com"</li>
 *   <li><b>deepseek.api-key</b>: 访问DeepSeek API所需的认证密钥</li>
 * </ul>
 *
 * <h3>技术实现要点</h3>
 * <ul>
 *   <li>使用Spring WebClient作为响应式HTTP客户端，支持非阻塞IO</li>
 *   <li>配置Netty HttpClient以实现高性能网络通信</li>
 *   <li>实现多层次超时控制：连接超时、读取超时、写入超时</li>
 *   <li>自动处理API密钥认证，无需手动在每个请求中添加Authorization头</li>
 * </ul>
 *
 * <h3>性能考虑</h3>
 * <ul>
 *   <li><b>连接超时</b>: 5秒，避免长时间等待连接建立</li>
 *   <li><b>响应超时</b>: 30秒，给予足够的处理时间但不会过长</li>
 *   <li><b>读写超时</b>: 30秒，确保活跃连接不会长时间挂起</li>
 *   <li><b>连接复用</b>: WebClient会自动管理连接池，提高性能</li>
 * </ul>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li>使用HTTPS协议确保通信安全</li>
 *   <li>通过Bearer Token进行认证，符合OAuth 2.0规范</li>
 *   <li>API密钥通过环境变量配置，避免硬编码</li>
 *   <li>如果未提供API密钥，请求将不包含认证头，确保服务降级</li>
 * </ul>
 *
 * <h3>与外部服务的集成</h3>
 * <p>
 * 该配置类与DeepSeek AI服务紧密集成，主要支持以下功能：
 * <ul>
 *   <li>文本生成：创建、编辑和迭代文本内容</li>
 *   <li>对话系统：构建智能对话助手</li>
 *   <li>代码生成：辅助编程和技术问答</li>
 *   <li>多语言支持：提供多种语言的AI能力</li>
 * </ul>
 * </p>
 *
 * <h3>配置最佳实践</h3>
 * <ul>
 *   <li>在生产环境中，应将API密钥存储在安全的位置（如AWS Secrets Manager或Vault）</li>
 *   <li>根据API响应时间调整超时设置，避免因网络波动导致请求失败</li>
 *   <li>实现重试机制，提高与外部API通信的可靠性</li>
 *   <li>考虑使用连接池和请求限流，避免对API服务器造成过大压力</li>
 *   <li>定期监控API使用量和响应时间，优化配置参数</li>
 * </ul>
 */
@Configuration
public class DeepSeekClientConfig {

        /**
         * 创建DeepSeek API的WebClient Bean
         * <p>
         * 该Bean提供了一个预配置的WebClient实例，用于与DeepSeek API进行HTTP通信。
         * WebClient是一个响应式HTTP客户端，支持非阻塞IO，适合高并发场景。
         * </p>
         *
         * <h4>配置参数说明</h4>
         * <ul>
         *   <li><b>baseUrl</b>: DeepSeek API的基础URL，支持环境变量配置</li>
         *   <li><b>apiKey</b>: API访问密钥，支持环境变量配置，可选参数</li>
         * </ul>
         *
         * <h4>网络配置说明</h4>
         * <ul>
         *   <li>连接超时：5秒，确保快速失败</li>
         *   <li>响应超时：30秒，给予足够的处理时间</li>
         *   <li>读写超时：30秒，确保活跃连接不会挂起</li>
         *   <li>使用Netty HttpClient作为底层实现，提供高性能网络通信</li>
         * </ul>
         *
         * @param baseUrl DeepSeek API的基础URL
         * @param apiKey DeepSeek API访问密钥
         * @return 配置好的WebClient实例
         */
        @Bean
        public WebClient deepSeekWebClient(
                        @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
                        @Value("${deepseek.api-key:}") String apiKey) {
                // 配置Netty HttpClient，实现高性能HTTP连接
                HttpClient httpClient = HttpClient.create()
                                // 连接超时设置，避免长时间等待连接建立
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                // 响应超时设置，限制整个请求处理时间
                                .responseTimeout(Duration.ofSeconds(60))
                                // 连接建立后的配置：添加读写超时处理器
                                .doOnConnected(conn -> conn
                                                // 读取超时：60秒，避免读取响应时长时间阻塞
                                                .addHandlerLast(new ReadTimeoutHandler(60))
                                                // 写入超时：60秒，避免发送请求时长时间阻塞
                                                .addHandlerLast(new WriteTimeoutHandler(60)));

                // 构建WebClient，提供流畅的API调用体验
                WebClient.Builder b = WebClient.builder()
                                // 设置API基础URL
                                .baseUrl(baseUrl)
                                // 使用自定义的HttpClient配置
                                .clientConnector(new ReactorClientHttpConnector(httpClient))
                                // 设置默认内容类型为JSON
                                .defaultHeader("Content-Type", "application/json");

                // 如果提供了API密钥，则自动添加认证头
                if (apiKey != null && !apiKey.isEmpty()) {
                        // 使用Bearer Token认证，符合OAuth 2.0规范
                        b.defaultHeader("Authorization", "Bearer " + apiKey);
                }

                // 构建并返回WebClient实例
                return b.build();
        }
}
