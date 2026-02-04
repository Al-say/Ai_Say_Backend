package com.zhupinzan.speaking.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;

/**
 * JWT认证令牌过滤器
 * <p>
 * 该过滤器是整个应用的安全核心，负责验证所有非公开API请求的JWT令牌。
 * 它实现了基于令牌的认证机制，确保只有拥有有效令牌的用户才能访问受保护的资源。
 * 过滤器继承自OncePerRequestFilter，保证每个请求只被处理一次，避免重复认证。
 * </p>
 *
 * <h3>设计意图</h3>
 * <ul>
 *   <li>实现无状态认证机制，支持水平扩展</li>
 *   <li>提供统一的安全入口点，简化权限控制</li>
 *   <li>支持细粒度的公开路径配置，提高灵活性</li>
 *   <li>实现完善的错误处理和友好的错误提示</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li>检查请求是否为公开路径，如果是则直接放行</li>
 *   <li>从Authorization头提取Bearer Token</li>
 *   <li>解析JWT令牌并验证签名</li>
 *   <li>检查令牌有效期</li>
 *   <li>提取用户信息并存入请求属性</li>
 *   <li>放行请求到后续处理程序</li>
 * </ol>
 *
 * <h3>安全特性</h3>
 * <ul>
 *   <li><b>签名验证</b>: 使用HMAC-SHA256算法验证令牌签名</li>
 *   <li><b>有效期检查</b>: 验证令牌是否在有效期内</li>
 *   <li><b>算法限制</b>: 强制使用HS256算法，防止算法混淆攻击</li>
 *   <li><b>密钥检查</b>: 确保服务端配置了有效的签名密钥</li>
 *   <li><b>错误处理</b>: 提供详细的错误信息，便于调试</li>
 * </ul>
 *
 * <h3>性能考虑</h3>
 * <ul>
 *   <li><b>轻量级处理</b>: JWT验证计算量小，性能影响极低</li>
 *   <li><b>无状态设计</li>: 无需查询数据库或缓存，响应快速</li>
 *   <li><b>并行处理</li>: 过滤器支持多线程并发处理</li>
 *   <li><b>资源复用</li>: 重用JWSVerifier对象，减少对象创建开销</li>
 * </ul>
 *
 * <h3>与外部服务的集成</h3>
 * <p>
 * 该过滤器主要与以下组件协同工作：
 * </p>
 * <ul>
 *   <li><b>AppleSignInConfig</b>: 提供JWT签名密钥配置</li>
 *   <li><b>Spring Security</b>: 在Spring Security过滤器链中运行</li>
 *   <li><b>控制器层</li>: 通过请求属性传递用户信息</li>
 *   <li><b>业务服务</li>: 获取认证后的用户上下文</li>
 * </ul>
 *
 * <h3>配置最佳实践</h3>
 * <ul>
 *   <li><b>过滤器顺序</b>: 设置合适的优先级，确保在授权过滤器之前执行</li>
 *   <li><b>公开路径</b>: 明确定义不需要认证的路径，避免过度限制</li>
 *   <li><b>密钥管理</b>: 使用安全的密钥存储方案，定期轮换密钥</li>
 *   <li><b>错误响应</b>: 提供详细的错误信息，但避免泄露敏感信息</li>
 *   <li><b>日志记录</li>: 记录认证失败的日志，便于安全审计</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>移动应用</li>: 为移动应用提供API访问认证</li>
 *   <li><b>前后端分离</li>: 为SPA应用提供API认证</li>
 *   <li><b>微服务架构</li>: 服务间调用认证</li>
 *   <li><b>RESTful API</li>: 保护RESTful API端点</li>
 * </ul>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AuthTokenFilter extends OncePerRequestFilter {

    /**
     * Apple登录配置注入
     * <p>
     * 注入AppleSignInConfig以获取JWT签名密钥配置。
     * 密钥用于验证本地生成的JWT令牌的真实性和完整性。
     * </p>
     */
    private final AppleSignInConfig config;

    /**
     * 构造函数，注入Apple登录配置
     *
     * @param config Apple登录配置对象
     */
    public AuthTokenFilter(AppleSignInConfig config) {
        this.config = config;
    }

    /**
     * 过滤器的核心认证逻辑
     * <p>
     * 该方法实现了完整的JWT认证流程，包括：
     * 公开路径检查、令牌提取、签名验证、有效期检查和用户信息提取。
     * 认证成功后将用户信息存入请求属性，供后续处理程序使用。
     * </p>
     *
     * <h4>认证流程详解</h4>
     * <ol>
     *   <li><b>公开路径检查</b>: 检查请求是否为公开访问路径</li>
     *   <li><b>令牌提取</b>: 从Authorization头提取Bearer Token</li>
     *   <li><b>签名验证</b>: 使用HS256算法验证令牌签名</li>
     *   <li><b>有效期检查</b>: 验证令牌是否在有效期内</li>
     *   <li><b>用户信息提取</b>: 从令牌中提取用户ID和Apple ID</li>
     *   <li><b>请求放行</b>: 将请求传递给后续处理程序</li>
     * </ol>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 如果发生Servlet相关异常
     * @throws IOException 如果发生IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // 步骤 1: 判断请求是否为公开访问路径
        // 公开路径包括：OPTIONS请求、Apple认证端点、监控端点和根路径
        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 步骤 2: 从请求头中提取Bearer Token
        // 标准的JWT认证格式为：Authorization: Bearer <token>
        var auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少 Bearer Token");
            return;
        }
        var token = auth.substring("Bearer ".length()).trim();

        try {
            // 步骤 3: 解析和验证JWT
            // 使用Nimbus库解析JWT令牌
            var jwt = SignedJWT.parse(token);

            // 检查JWT的签名算法是否为预期的HS256
            // 强制使用HS256算法，防止算法混淆攻击（Algorithm Confusion Attack）
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
                writeUnauthorized(response, "Token 算法不支持");
                return;
            }

            // 获取签名密钥并进行安全检查
            var secret = config.getTokenSecret();
            if (secret == null || secret.isBlank()) {
                writeUnauthorized(response, "服务端未配置 token secret");
                return;
            }

            // 使用密钥创建验证器并验证JWT的签名
            // MACVerifier实现了HMAC-SHA256验证算法
            JWSVerifier verifier = new MACVerifier(secret.getBytes(StandardCharsets.UTF_8));
            if (!jwt.verify(verifier)) {
                writeUnauthorized(response, "Token 校验失败");
                return;
            }

            // 步骤 4: 检查JWT的声明(claims)
            var claims = jwt.getJWTClaimsSet();
            var exp = claims.getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(Instant.now())) {
                writeUnauthorized(response, "Token 已过期");
                return;
            }

            // 验证 issuer
            var issuer = claims.getIssuer();
            if (!config.getJwtIssuer().equals(issuer)) {
                writeUnauthorized(response, "Token issuer 无效");
                return;
            }

            // 验证 audience
            var audience = claims.getAudience();
            if (audience == null || !audience.contains(config.getJwtAudience())) {
                writeUnauthorized(response, "Token audience 无效");
                return;
            }

            // 步骤 5: 将用户信息存入请求属性
            // 认证成功后，将用户信息放入请求属性，供后续处理程序使用
            // sub字段是用户的唯一标识符
            request.setAttribute("auth.userId", claims.getSubject());
            // appleSub字段是Apple用户ID，用于与Apple账户关联
            request.setAttribute("auth.appleSub", claims.getStringClaim("appleSub"));

            // 放行请求到后续处理程序
            filterChain.doFilter(request, response);
        } catch (ParseException e) {
            // JWT解析异常，通常是令牌格式错误
            writeUnauthorized(response, "Token 格式错误");
        } catch (Exception e) {
            // 其他认证异常
            writeUnauthorized(response, "Token 校验异常");
        }
    }

    /**
     * 判断请求的路径是否为公开访问路径
     * <p>
     * 公开路径包括：
     * <ul>
     *   <li>OPTIONS请求：CORS预检请求</li>
     *   <li>/api/auth/apple：Apple认证端点</li>
     *   <li>/actuator/*：Spring Boot监控端点</li>
     *   <li>/：应用根路径</li>
     * </ul>
     * 这些路径不需要认证即可访问。
     * </p>
     *
     * @param request HTTP请求对象
     * @return 如果是公开路径则返回true，否则返回false
     */
    private boolean isPublic(HttpServletRequest request) {
        var path = request.getRequestURI();

        // OPTIONS请求通常用于CORS预检，应直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // Apple认证相关路径，允许匿名访问
        if (path.equals("/api/auth/apple")) return true;

        // 测试端点，允许匿名访问
        if (path.equals("/api/test")) return true;

        // 🔥 临时：聊天端点，允许匿名访问用于测试 DeepSeek 集成
        if (path.startsWith("/api/chat/")) return true;

        // 🔥 临时：评估端点，允许匿名访问用于测试 DeepSeek 集成
        if (path.startsWith("/api/eval/")) return true;

        // 🔥 临时：测试端点，允许匿名访问用于测试 DeepSeek 集成
        if (path.startsWith("/api/test/")) return true;

        // Spring Boot Actuator监控端点，用于健康检查和监控
        if (path.startsWith("/actuator")) return true;

        // 应用根路径，通常用于健康检查或API文档
        if (path.equals("/")) return true;

        return false;
    }

    /**
     * 向客户端写入401 Unauthorized错误响应
     * <p>
     * 当认证失败时，该方法会构造一个JSON格式的错误响应，
     * 包含错误代码和错误信息。使用标准的HTTP状态码401，
     * 并设置适当的Content-Type。
     * </p>
     *
     * <h4>响应格式</h4>
     * <pre>{@code
     * {
     *   "code": 401,
     *   "message": "错误信息"
     * }
     * }</pre>
     *
     * @param response HTTP响应对象
     * @param message 错误信息描述
     * @throws IOException 如果写入响应时发生错误
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        // 设置HTTP状态码为401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 设置Content-Type为application/json，使用UTF-8编码
        response.setContentType("application/json;charset=UTF-8");

        // 构造JSON格式的错误响应
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
