package com.simon.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simon.seckill.exception.GlobalException;
import com.simon.seckill.mapper.OrderMapper;
import com.simon.seckill.pojo.Order;
import com.simon.seckill.pojo.SeckillGoods;
import com.simon.seckill.pojo.SeckillOrder;
import com.simon.seckill.pojo.User;
import com.simon.seckill.service.IGoodsService;
import com.simon.seckill.service.IOrderService;
import com.simon.seckill.service.ISeckillGoodsService;
import com.simon.seckill.service.ISeckillOrderService;
import com.simon.seckill.utils.MD5Util;
import com.simon.seckill.utils.UUIDUtil;
import com.simon.seckill.vo.GoodsVo;
import com.simon.seckill.vo.OrderDetailVo;
import com.simon.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Simon
 * @since 2022-07-06
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 秒杀
     * @param user
     * @param goodsVo
     * @return
     */
    @Transactional      // 添加事务注解
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 1、通过goodsId查询秒杀商品
        SeckillGoods seckillGoods = seckillGoodsService.findGoodsByGoodsId(goodsVo.getId());
        // 2、库存-1
//        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        // 3、更新库存（mybatis-plus自带的方法）
//        seckillGoodsService.updateById(seckillGoods);
        // 解决库存超卖：通过goodsId查询秒杀商品，如果库存大于0则更新库存
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count - 1").
                eq("goods_id", goodsVo.getId()).gt("stock_count", 0));
//        if (!result) {
//            // 更新不成功
//            return null;
//        }
        if (seckillGoods.getStockCount() <= 0) {
            // 判断是否还有库存
            valueOperations.set("isStockEmpty:" + goodsVo.getId(), "0");
            return null;
        }

        // 4、生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setGoodsChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        // 5、生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrderService.save(seckillOrder);

        // 将秒杀信息存入redis（秒杀订单是以用户id和商品id共同作为唯一标识的）
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId(), seckillOrder);
        return order;
    }

    /**
     * 订单详情
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        if (null == orderId) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrder(order);
        orderDetailVo.setGoodsVo(goodsVo);
        return orderDetailVo;
    }

    /**
     * 获取秒杀地址
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        // 将随机生成的秒杀接口地址存入redis，设置过期时间为60s
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (null == user || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        // 从redis中获取秒杀地址，并比对秒杀地址
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    /**
     * 校验验证码
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (StringUtils.isEmpty(captcha) || user == null || goodsId < 0){
            return false;
        }
        // 从redis中获取验证码
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
