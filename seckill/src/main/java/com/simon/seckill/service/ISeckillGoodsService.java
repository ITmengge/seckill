package com.simon.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simon.seckill.pojo.SeckillGoods;
import com.simon.seckill.pojo.SeckillOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
public interface ISeckillGoodsService extends IService<SeckillGoods> {

    /**
     * 通过goodsId查询秒杀商品
     */
    SeckillGoods findGoodsByGoodsId(Long goodsId);
}
