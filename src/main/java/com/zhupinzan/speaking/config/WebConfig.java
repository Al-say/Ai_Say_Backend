package com.zhupinzan.speaking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Spring Web MVC 的自定义配置。
 * <p>
 * 这个类实现了 {@link WebMvcConfigurer} 接口，允许我们对Spring MVC的默认行为进行扩展和定制。
 * 在这里，我们主要做了两件事：
 * 1.  配置静态资源处理器，以允许外部直接访问上传的文件。
 * 2.  注册自定义的参数解析器。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${api.rate-limit.max-requests:100}")
    private int maxRequests;

    @Value("${api.rate-limit.time-window-seconds:60}")
    private long timeWindowSeconds;

    /**
     * 添加自定义的方法参数解析器。
     * <p>
     * 这个方法将我们的 {@link CurrentUserArgumentResolver} 注册到Spring MVC中。
     * 一旦注册，Spring在处理Controller方法时就能够识别并正确处理
     * 被 {@link com.zhupinzan.speaking.util.CurrentUser} 注解的参数。
     *
     * @param resolvers Spring MVC中已有的参数解析器列表
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }

    /**
     * 添加自定义的拦截器。
     * <p>
     * 这个方法将我们的 {@link RateLimitingInterceptor} 注册到Spring MVC中，
     * 从而在请求到达Controller之前对API请求进行速率限制。
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor(maxRequests, timeWindowSeconds))
                .addPathPatterns("/api/**"); // 对所有 /api/** 路径下的请求进行速率限制
    }
}
