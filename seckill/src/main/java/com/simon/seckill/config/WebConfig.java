package com.simon.seckill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC配置类，在进入Controller层前，先判断用户信息是否正确，类似于拦截器
 * WebMvcConfigurer接口默认拦截静态资源，需要重写addResourceHandlers方法
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    // 注入自定义用户参数
    @Autowired
    private UserArgumentResolver userArgumentResolver;

    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;
    /**
     * 自定义参数处理
     * @param resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }

    /**
     * 取消WebMvcConfigurer接口默认拦截静态资源，配置类大于配置，
     * 需要重写addResourceHandlers方法，然后删掉target，重新编译
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 如果url请求为/resource/templates/goodsList.html，就会去templates路径下找goodsList.html文件
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 在mvc配置类中添加拦截器,使其生效
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimitInterceptor);
    }
}
