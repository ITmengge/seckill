package com.simon.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simon.seckill.mapper.SeckillOrderMapper;
import com.simon.seckill.pojo.SeckillOrder;
import com.simon.seckill.pojo.User;
import com.simon.seckill.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 通过goodsId和userId查询订单
     */
    @Override
    public SeckillOrder findOrderByGoodsIdAndUserId(Long goodsId,Long userId) {
        SeckillOrder seckillOrder = getOne(new QueryWrapper<SeckillOrder>().eq("user_id", userId).eq("goods_id", goodsId));
        return seckillOrder;
    }

    /**
     * 获取秒杀结果
     * @return orderId：成功   -1：秒杀失败     0：排队中
     */
    @Override
    public Long getResult(User user, Long goodsId) {

        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if (seckillOrder != null) {
            // 秒杀成功
            return seckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)){
            // 秒杀失败
            return -1L;
        } else{
            // 排队中
            return 0L;
        }
    }
}
