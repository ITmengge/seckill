package com.simon.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simon.seckill.pojo.Goods;
import com.simon.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 通过goodsId获取商品详情
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
