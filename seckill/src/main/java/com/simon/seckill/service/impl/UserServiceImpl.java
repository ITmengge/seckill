package com.simon.seckill.service.impl;

import com.simon.seckill.exception.GlobalException;
import com.simon.seckill.pojo.User;
import com.simon.seckill.mapper.UserMapper;
import com.simon.seckill.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simon.seckill.utils.CookieUtil;
import com.simon.seckill.utils.MD5Util;
import com.simon.seckill.utils.UUIDUtil;
import com.simon.seckill.vo.LoginVo;
import com.simon.seckill.vo.RespBean;
import com.simon.seckill.vo.RespBeanEnum;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Simon
 * @since 2022-07-04
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    // 注意在使用service前需要注入这个对象
    @Autowired
    private UserMapper userMapper;

    // 注入redisTemplate序列化对象
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录，将登录信息存入redis中
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if (null == user) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            // 抛出自定义的异常
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        // 判断密码是否正确
        if (!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPassword())) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        // 生成cookie
        String ticket = UUIDUtil.uuid();

        // 将登录成功后的用户信息存入redis中（opsForValue是对字符串进行操作）
        redisTemplate.opsForValue().set("user:" + ticket,user);

        request.getSession().setAttribute(ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        // 将登录成功后的ticket返回
        return RespBean.success(ticket);
    }

    /**
     * 根据cookie从redis中获取用户信息
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if (user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    /**
     * 更新密码，当更新数据库时，数据库中有这条记录，删除redis缓存，保证一致性
     */
    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
        // 从Cookie中获取用户信息
        User user = getUserByCookie(userTicket, request, response);
        if (null == user) {
            // 抛出手机号不存在
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password, user.getSalt()));
        // 更新数据库
        int result = userMapper.updateById(user);
        if (1 == result){
            // 如果数据库中有这条记录，删除Redis缓存
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        // 抛出密码更新失败
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
