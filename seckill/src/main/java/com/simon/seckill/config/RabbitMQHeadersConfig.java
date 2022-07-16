//package com.simon.seckill.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * RabbitMQ配置类：典型的生产者（发送消息）和消费者（接收消息）模型，
// *                准备队列（存储消息的缓冲区）
// */
//@Configuration
//public class RabbitMQHeadersConfig {
//
//    private static final String QUEUE01 = "queue_header01";
//    private static final String QUEUE02 = "queue_header02";
//    private static final String EXCHANGE = "headersExchange";
//
//    @Bean
//    public Queue queue01(){
//        return new Queue(QUEUE01);
//    }
//
//    @Bean
//    public Queue queue02(){
//        return new Queue(QUEUE02);
//    }
//
//    @Bean
//    public HeadersExchange headersExchange(){
//        return new HeadersExchange(EXCHANGE);
//    }
//
//    /**
//     * Headers：any满足一个键值对就行
//     */
//    @Bean
//    public Binding binding01(){
//        Map<String,Object> map = new HashMap<>();
//        map.put("color","red");
//        map.put("speed","low");
//        return BindingBuilder.bind(queue01()).to(headersExchange()).whereAny(map).match();
//    }
//
//    /**
//     * Headers：all满足所有的键值对
//     */
//    @Bean
//    public Binding binding02(){
//        Map<String,Object> map = new HashMap<>();
//        map.put("color","red");
//        map.put("speed","fast");
//        return BindingBuilder.bind(queue02()).to(headersExchange()).whereAll(map).match();
//    }
//}
