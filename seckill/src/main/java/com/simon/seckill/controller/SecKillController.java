package com.simon.seckill.controller;

import com.simon.seckill.config.AccessLimit;
import com.simon.seckill.exception.GlobalException;
import com.simon.seckill.pojo.Order;
import com.simon.seckill.pojo.SeckillMessage;
import com.simon.seckill.pojo.SeckillOrder;
import com.simon.seckill.pojo.User;
import com.simon.seckill.rabbitmq.MQSender;
import com.simon.seckill.service.IGoodsService;
import com.simon.seckill.service.IOrderService;
import com.simon.seckill.service.ISeckillGoodsService;
import com.simon.seckill.service.ISeckillOrderService;
import com.simon.seckill.utils.JsonUtil;
import com.simon.seckill.vo.GoodsVo;
import com.simon.seckill.vo.RespBean;
import com.simon.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    // 内存标记用的map，减少对redis的访问
    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

//    @Autowired
//    private DefaultRedisScript<Long> redisScript;

    /**
     * 秒杀
     * Windows优化前QPS：134.2
     * Linux优化前QPS：211.2
     */
    @RequestMapping("/doSeckill2")
    public String doSecKill2(Model model, User user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        // 有用户登录信息则放到model传到前端去
        model.addAttribute("user",user);
        // 1、判断库存
        // 通过goodsId获取商品库存
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0) {
            // 库存不足时，返回错误信息
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        // 2、判断是否重复抢购
        SeckillOrder seckillOrder = seckillOrderService.findOrderByGoodsIdAndUserId(goodsId, user.getId());
        if (seckillOrder != null) {
            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        // 3、秒杀
        Order order = orderService.seckill(user ,goodsVo);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }

    /**
     * 秒杀，秒杀订单静态化，秒杀接口优化
     * Windows优化前QPS：134.2
     *        缓存后QPS：648.2
     *        优化后QPS：835.5
     * Linux优化前QPS：211.2
     */
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill (@PathVariable String path, User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        // 校验秒杀接口地址
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // 内存标记，减少对redis的访问
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 从redis中获取缓存操作器
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 1、判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        // 2、预减库存
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        // 使用分布式锁+Lua脚本优化预减库存（未解决超卖问题）
//        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0){
            // 标记内存不足
            EmptyStockMap.put(goodsId, true);
            // 当预减库存后，库存小于0，需要加回去变成0
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 3、发送秒杀信息
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        // 转为json发送
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        // 请求入队，立即返回0：排队中
        return RespBean.success(0);

//        if (user == null) {
//            return RespBean.error(RespBeanEnum.SESSION_ERROR);
//        }
//        // 1、判断库存
//        // 通过goodsId获取商品库存
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        if (goodsVo.getStockCount() <= 0) {
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//
//        // 2、判断是否重复抢购
//        // 优化：从redis中获取秒杀信息
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
//
//        if (seckillOrder != null) {
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        // 3、秒杀
//        Order order = orderService.seckill(user ,goodsVo);
//        return RespBean.success(order);
    }


    /**
     * 获取秒杀结果
     * @return orderId：成功   -1：秒杀失败     0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 获取秒杀地址
     */
    @AccessLimit(second=5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if (null == user){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        // 限制访问次数,5秒内访问5次(使用计数器实现)
//        String uri = request.getRequestURI();
//        //方便测试
//        captcha = "0";
//        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
//        if (null == count) {
//            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
//        } else if (count < 5){
//            valueOperations.increment(uri + ":" + user.getId());
//        } else {
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
//        }

        // 校验验证码
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        // 生成秒杀地址
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * 生成验证码
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if (null == user || goodsId < 0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置响应头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam","No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 生成验证码,将其存入redis中
        // 设置长宽高
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130,32,3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);

        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            // 使用log打印日志,需先添加@Slf4j
            log.error("验证码生成失败", e.getMessage());
        }
    }

    /**
     * 实现InitializingBean接口，重写方法
     * 系统初始化，把商品库存数量加载到Redis
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            // 初始内存标记设置为false，还有内存
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
