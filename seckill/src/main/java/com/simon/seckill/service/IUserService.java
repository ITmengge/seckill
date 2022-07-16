package com.simon.seckill.service;

import com.simon.seckill.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simon.seckill.vo.LoginVo;
import com.simon.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Simon
 * @since 2022-07-04
 */
public interface IUserService extends IService<User> {

    /**
     * 登录，将登录信息存入redis中
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie从redis中获取用户信息
     */
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);

    /**
     * 更新密码
     */
    RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response);
}
