package com.simon.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simon.seckill.pojo.Goods;
import com.simon.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
public interface IGoodsService extends IService<Goods> {

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
