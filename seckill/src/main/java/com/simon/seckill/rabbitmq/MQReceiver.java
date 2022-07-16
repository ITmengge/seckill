package com.simon.seckill.rabbitmq;

import com.simon.seckill.pojo.SeckillMessage;
import com.simon.seckill.pojo.SeckillOrder;
import com.simon.seckill.pojo.User;
import com.simon.seckill.service.IGoodsService;
import com.simon.seckill.service.IOrderService;
import com.simon.seckill.utils.JsonUtil;
import com.simon.seckill.vo.GoodsVo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消息消费者
 */
@Service
@Slf4j
public class MQReceiver {

//    // 从队列queue中接收消息
//    @RabbitListener(queues = "queue")
//    public void receive(Object msg) {
//        log.info("接收消息：" + msg);
//    }
//
//    // 通过广播模式从队列1中接收消息
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg){
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    // 通过广播模式从队列2中接收消息
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg){
//        log.info("QUEUE02接收消息：" + msg);
//    }
//
//    // 通过指定队列从Direct模式的交换机接收消息
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg) {
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    // 通过指定队列从Direct模式的交换机接收消息
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg) {
//        log.info("QUEUE02接收消息：" + msg);
//    }
//
//    // 从Topic模糊匹配的交换机接收消息
//    @RabbitListener(queues = "queue_topic01")
//    public void receive05(Object msg) {
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    // 从Topic模糊匹配的交换机接收消息
//    @RabbitListener(queues = "queue_topic02")
//    public void receive06(Object msg) {
//        log.info("QUEUE02接收消息：" + msg);
//    }
//
//    // Headers通过键值对匹配
//    @RabbitListener(queues = "queue_header01")
//    public void receive01(Message message) {
//        log.info("QUEUE01接受Message对象：" + message);
//        log.info("QUEUE01接受消息：" + new String(message.getBody()));
//    }
//
//    //Headers通过键值对匹配
//    @RabbitListener(queues = "queue_header02")
//    public void receive02(Message message) {
//        log.info("QUEUE02接受Message对象：" + message);
//        log.info("QUEUE02接受消息：" + new String(message.getBody()));
//    }


    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receiveSeckillMessage(String message) {
        log.info("接收的秒杀信息：" + message);
        // 将接收到的秒杀信息json转为SeckillMessage对象
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        // 从SeckillMessage对象获得user和goodsId
        User user = seckillMessage.getUser();
        Long goodsId = seckillMessage.getGoodsId();

        // 1、判断库存
        // 通过goodsId获取商品库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            return;
        }

        // 2、判断是否重复抢购
        // 优化：从redis中获取秒杀信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return;
        }

        // 3、秒杀
        orderService.seckill(user, goodsVo);
    }
}
