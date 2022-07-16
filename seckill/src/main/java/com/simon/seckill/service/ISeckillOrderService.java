package com.simon.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simon.seckill.pojo.SeckillOrder;
import com.simon.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    /**
     * 通过goodsId和userId查询订单
     */
    SeckillOrder findOrderByGoodsIdAndUserId(Long goodsId,Long userId);

    /**
     * 获取秒杀结果
     * @return orderId：成功   -1：秒杀失败     0：排队中
     */
    Long getResult(User user, Long goodsId);
}
