package com.simon.seckill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.seckill.pojo.User;
import com.simon.seckill.service.IUserService;
import com.simon.seckill.utils.CookieUtil;
import com.simon.seckill.vo.RespBean;
import com.simon.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 自定义注解的拦截器
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 通过本地线程LocalThread获取线程对应的用户登录信息(多线程情况下不会紊乱)
            User user = getUser(request, response);
            // 将用户放入本地线程中
            UserContext.setUser(user);
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 让自定义注解AccessLimit生效
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            int second = accessLimit.second();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin) {
                if (null == user) {
                    // 返回错误信息respBeanEnum(转为json对象)
                    render(response, RespBeanEnum.SESSION_ERROR);
                    // 被拦截掉了,返回false
                    return false;
                }
                // 拼接key(加上用户id)
                key += ":" + user.getId();
            }
            // 将key存入redis中
            ValueOperations valueOperations = redisTemplate.opsForValue();
            // 限制访问次数,5秒内访问5次(使用计数器实现)
            Integer count = (Integer) valueOperations.get(key);
            if (null == count) {
                valueOperations.set(key, 1, second, TimeUnit.SECONDS);
            } else if (count < maxCount) {
                valueOperations.increment(key);
            } else {
                render(response, RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    /**
     * 返回错误信息respBeanEnum(转为json对象)
     *
     * @param response
     * @param respBeanEnum
     */
    private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter printWriter = response.getWriter();
        RespBean respBean = RespBean.error(respBeanEnum);
        printWriter.write(new ObjectMapper().writeValueAsString(respBean));
        printWriter.flush();
        printWriter.close();
    }

    /**
     * 从本地线程LocalThread中获取当前登录用户信息
     *
     * @param request
     * @param response
     * @return
     */
    private User getUser(HttpServletRequest request, HttpServletResponse response) {
        // 通过cookie获得用户信息
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (StringUtils.isEmpty(ticket)) {
            return null;
        }
        return userService.getUserByCookie(ticket, request, response);
    }
}
