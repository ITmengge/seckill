package com.simon.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 公共返回对象枚举，注意枚举的每个值是用逗号分隔，不是分号
 */
@ToString
@Getter
@AllArgsConstructor
public enum  RespBeanEnum {
    // 通用模块
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),

    // 登录模块5002xx
    LOGIN_ERROR(500210,"用户名或者密码错误"),
    MOBILE_ERROR(500211,"手机号码格式错误"),
    BIND_ERROR(500212,"参数校验异常"),
    MOBILE_NOT_EXIST(500213,"用户名不存在"),
    PASSWORD_UPDATE_FAIL(500214,"密码更新失败"),
    SESSION_ERROR(500215,"用户不存在"),

    // 秒杀模块5005xx
    EMPTY_STOCK(500500,"库存不足"),
    REPEATE_ERROR(500501,"该商品每人限购一件"),
    REQUEST_ILLEGAL(500502,"非法请求，请重新尝试"),
    ERROR_CAPTCHA(500503,"验证码错误,请重新输入"),
    ACCESS_LIMIT_REACHED(500504,"频繁访问,请稍后再试"),

    // 订单模块5003xx
    ORDER_NOT_EXIST(500300,"订单信息不存在"),
    ;
    private final Integer code;
    private final String message;
}
