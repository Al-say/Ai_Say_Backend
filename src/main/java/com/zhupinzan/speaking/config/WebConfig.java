package com.zhupinzan.speaking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
}
