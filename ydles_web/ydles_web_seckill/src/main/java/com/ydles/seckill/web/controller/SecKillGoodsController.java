package com.ydles.seckill.web.controller;

import com.ydles.entity.Result;
import com.ydles.seckill.feign.SecKillFeign;
import com.ydles.seckill.pojo.SeckillGoods;
import com.ydles.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/wseckillgoods")
public class SecKillGoodsController {


    @Autowired
    SecKillFeign secKillFeign;
    //跳转秒杀首页
    @RequestMapping("/toIndex")
    public String toIndex(){
        return "seckill-index";
    }

    //获取秒杀时间段集合信息
    @RequestMapping("/timeMenus")
    @ResponseBody
    public List<String> dateMenus(){

        //获取当前时间段相关的信息集合
        List<Date> dateMenus = DateUtil.getDateMenus(); //5个
        //返回值
        List<String> result = new ArrayList<>();

        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //格式化时间段
        for (Date dateMenu : dateMenus) {
            String format = simpleDateFormat.format(dateMenu);
            result.add(format);
        }
        return  result;
    }
    @GetMapping("/list")
    @ResponseBody
    public Result<List<SeckillGoods>> list(String time){
        String timeStr = DateUtil.formatStr(time);
        System.out.println(timeStr);
        return secKillFeign.list(timeStr);
    }
}