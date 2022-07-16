package com.simon.seckill.controller;


import com.simon.seckill.pojo.User;
import com.simon.seckill.rabbitmq.MQSender;
import com.simon.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Simon
 * @since 2022-07-04
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;

    /**
     * 用户信息（压测用的）
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }
//
//    /**
//     * 测试发送RabbitMQ消息
//     */
//    @RequestMapping("/mq")
//    @ResponseBody
//    public void mq(){
//        mqSender.send("hello");
//    }
//
//    /**
//     * Fanout模式
//     */
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public void fanout(){
//        mqSender.send("hello");
//    }
//
//    /**
//     * Direct模式
//     */
//    @RequestMapping("/mq/direct01")
//    @ResponseBody
//    public void direct01(){
//        mqSender.send01("Hello,Red!");
//    }
//
//    /**
//     * Direct模式
//     */
//    @RequestMapping("/mq/direct02")
//    @ResponseBody
//    public void direct02(){
//        mqSender.send02("Hello,Green!");
//    }
//
//    /**
//     * Topic模式
//     */
//    @RequestMapping("/mq/topic01")
//    @ResponseBody
//    public void topic01(){
//        mqSender.send03("Hello,Red!");
//    }
//
//    /**
//     * Topic模式
//     */
//    @RequestMapping("/mq/topic02")
//    @ResponseBody
//    public void topic02(){
//        mqSender.send04("Hello,Green!");
//    }
//
//    /**
//     * Headers模式
//     */
//    @RequestMapping("/mq/header01")
//    @ResponseBody
//    public void header01() {
//        mqSender.send05("Hello,header01");
//    }
//
//    /**
//     * Headers模式
//     */
//    @RequestMapping("/mq/header02")
//    @ResponseBody
//    public void header02() {
//        mqSender.send06("Hello,header02");
//    }
}
