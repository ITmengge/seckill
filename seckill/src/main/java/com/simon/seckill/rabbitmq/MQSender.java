package com.simon.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消息生产者
 */
@Service        // 这里是跟service层一样的功能
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;  // 类似与redisTemplate

//    public void send(Object msg){
//        log.info("发送消息：" + msg);
//        // 往自定义的队列queue发送消息
////        rabbitTemplate.convertAndSend("queue", msg);
//
//        // 将消息发送到交换机（不用指定路由器，但还是得放着）
//        rabbitTemplate.convertAndSend("fanoutExchange","", msg);
//    }
//
//    // DirectExchange
//    public void send01(Object msg){
//        log.info("发送red消息：" + msg);
//        rabbitTemplate.convertAndSend("directExchange","queue.red", msg);
//    }
//
//    // DirectExchange
//    public void send02(Object msg){
//        log.info("发送green消息：" + msg);
//        rabbitTemplate.convertAndSend("directExchange","queue.green", msg);
//    }
//
//    // TopicExchange
//    public void send03(Object msg){
//        log.info("发送消息（QUEUE01接收）：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange","queue.red.message", msg);
//    }
//
//    // TopicExchange
//    public void send04(Object msg){
//        log.info("发送消息（QUEUE01和QUEUE02接收）：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange","message.queue.green.message", msg);
//    }
//
//    // HeadersExchange
//    public void send05(String msg) {
//        log.info("发送消息(被两个queue接收)：" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "fast");
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headersExchange", "", message);
//    }
//
//    // HeadersExchange
//    public void send06(String msg) {
//        log.info("发送消息(被01队列接受)：" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "normal");    // 第二个键值对不满足
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headersExchange", "", message);
//    }

    // 发送秒杀信息
    public void sendSeckillMessage(String message) {
        log.info("发送秒杀信息：" + message);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message);
    }
}
