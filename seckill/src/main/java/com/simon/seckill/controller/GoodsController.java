package com.simon.seckill.controller;

import com.simon.seckill.pojo.User;
import com.simon.seckill.service.IGoodsService;
import com.simon.seckill.service.IUserService;
import com.simon.seckill.vo.DetailVo;
import com.simon.seckill.vo.GoodsVo;
import com.simon.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     *  1、优化后的用户登录
     *  跳转到商品列表页面时前需判断用户是否登录，即是否有cookie值
     *
     *  2、Windows优化前的QPS：330.1
     *             缓存QPS：56.8
     *  Linux优化前的QPS：581.3（这里不做优化）
     *
     *  3、页面缓存：将商品列表页存入redis中，设置失效时间为60秒
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        // 从Redis中获取页面html
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        // 如果页面不为空，则直接返回页面
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
        // 如果页面为空，手动渲染，存入Redis并返回（model.asMap()将model转为map类型）
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)) {
            // 需要对页面设置超时时间60秒，放入Redis中
            valueOperations.set("goodList", html, 60, TimeUnit.SECONDS);
        }
        return html;
        // return "goodsList";
    }

    /**
     * 通过goodsId获取商品详情，进行页面缓存
     */
    @RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        // 从Redis中获取页面html（需要添加goodsId获取不同的商品）
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        // 如果页面不为空，则直接返回页面
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        // 设置秒杀状态（0秒杀还未开始，1秒杀进行中，2秒杀已经结束）
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            // 秒杀还未开始（开始时间减去当前时间）
            remainSeconds = (int) (startDate.getTime() - nowDate.getTime()) / 1000;
        } else if (nowDate.after(endDate)) {
            // 秒杀已经结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("secKillStatus",secKillStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("goods",goodsVo);

        // 如果页面为空，手动渲染，存入Redis并返回（model.asMap()将model转为map类型）
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
        // return "goodsDetail";
    }

    /**
     * 通过goodsId获取商品详情，进行页面缓存
     */
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId) {
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        // 设置秒杀状态（0秒杀还未开始，1秒杀进行中，2秒杀已经结束）
        int secKillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            // 秒杀还未开始（开始时间减去当前时间）
            remainSeconds = (int) (startDate.getTime() - nowDate.getTime()) / 1000;
        } else if (nowDate.after(endDate)) {
            // 秒杀已经结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        // 商品详情返回对象
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return RespBean.success(detailVo);
    }
}











