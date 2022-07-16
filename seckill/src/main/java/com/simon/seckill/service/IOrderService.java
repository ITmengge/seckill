package com.simon.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simon.seckill.pojo.Order;
import com.simon.seckill.pojo.User;
import com.simon.seckill.vo.GoodsVo;
import com.simon.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
public interface IOrderService extends IService<Order> {

    /**
     * 秒杀
     */
    Order seckill(User user, GoodsVo goodsVo);

    /**
     * 订单详情
     */
    OrderDetailVo detail(Long orderId);

    /**
     * 获取秒杀地址
     */
    String createPath(User user, Long goodsId);

    /**
     * 校验秒杀地址
     */
    boolean checkPath(User user, Long goodsId, String path);

    /**
     * 校验验证码
     */
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
