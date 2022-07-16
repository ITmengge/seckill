package com.simon.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simon.seckill.mapper.SeckillGoodsMapper;
import com.simon.seckill.pojo.SeckillGoods;
import com.simon.seckill.pojo.SeckillOrder;
import com.simon.seckill.service.ISeckillGoodsService;
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
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements ISeckillGoodsService {

    /**
     * 通过goodsId查询秒杀商品
     */
    @Override
    public SeckillGoods findGoodsByGoodsId(Long goodsId) {
        SeckillGoods seckillGoods = getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsId));
        return seckillGoods;
    }
}
