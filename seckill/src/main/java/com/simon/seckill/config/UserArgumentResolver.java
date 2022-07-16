package com.simon.seckill.config;

import com.simon.seckill.pojo.User;
import com.simon.seckill.service.IUserService;
import com.simon.seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义用户参数
 */
@Component          // 需要将这个类添加到WebMvcConfigurer，所以要添加@Component注解
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    // 注入userService
    @Autowired
    private IUserService userService;

    /**
     * 条件判断，判断前端传过来的参数是否为User类型
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;
    }

    /**
     * 当上面的方法返回true，即传过来的参数是User类型，才会执行下面这个方法，进行用户信息的比对
     * @param parameter
     * @param mavContainer
     * @param webRequest
     * @param binderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//        // 通过WebRequest获取HttpServletRequest和HttpServletResponse
//        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
//        // 通过cookie获得用户信息
//        String ticket = CookieUtil.getCookieValue(request, "userTicket");
//        if (StringUtils.isEmpty(ticket)) {
//            return null;
//        }
//        return userService.getUserByCookie(ticket, request, response);

        // 通过UserContext本地线程获得线程对应的用户信息
        return UserContext.getUser();
    }
}
