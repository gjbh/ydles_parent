package com.ydles.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydles.goods.feign.SkuFeign;
import com.ydles.goods.feign.SpuFeign;
import com.ydles.goods.pojo.Sku;
import com.ydles.goods.pojo.Spu;
import com.ydles.order.pojo.OrderItem;
import com.ydles.order.service.CartService;


import com.ydles.util.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    //购物车redis key 头
    private static final String CART = "cart_";
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    SpuFeign spuFeign;
    @Autowired
    SkuFeign skuFeign;

    //添加购物车 从购物车减一个，num=-1
    @Override
    public void addCart(String skuId, Integer num, String username) {

        String orderItemStr = (String) stringRedisTemplate.boundHashOps(CART + username).get(skuId);
        Sku sku = skuFeign.findById(skuId).getData();
        Spu spu = spuFeign.findSpuById(sku.getSpuId()).getData();
        //判断  当前用户购物车里有没有添加的sku
        OrderItem orderItem;
        if (StringUtils.isNotEmpty(orderItemStr)) {
            orderItem = JSON.parseObject(orderItemStr, OrderItem.class);
            //购物车里有  更新redis数据
            orderItem.setNum(orderItem.getNum() + num);
            if (orderItem.getNum() < 1) {
                //总数小于1 redis删除
                stringRedisTemplate.boundHashOps(CART + username).delete(skuId);
            }
            orderItem.setMoney(orderItem.getPrice() * orderItem.getNum());
            orderItem.setPayMoney(orderItem.getPrice() * orderItem.getNum());//满减
            orderItem.setWeight(sku.getWeight() * num);
        } else {
            //新加入购物车的sku
            orderItem = sku2OrderItem(spu, sku, num);
        }
        //放入redis
        stringRedisTemplate.boundHashOps(CART + username).put(skuId, JSON.toJSONString(orderItem));
    }
    //查询购物车
    @Override
    public Map list(String name) {
        Map map=new HashMap();
        //商品的总数量与总价格
        Integer totalNum = 0;
        Integer totalMoney = 0;

        List<Object> values = stringRedisTemplate.boundHashOps(CART + name).values();
        List<OrderItem> orderItemList =new ArrayList<>();
        for(Object value:values){
            OrderItem orderItem = JSON.parseObject(value.toString(), OrderItem.class);
            totalNum+=orderItem.getNum();
            totalMoney+=orderItem.getMoney();
            orderItemList.add(orderItem);
        }
        map.put("orderItemList",orderItemList);
        //总数量
        map.put("totalNum",totalNum);
        //总金额
        map.put("totalMoney",totalMoney);


        return null;
    }

    @Autowired
    IdWorker idWorker;

    //拼接 OrderItem
    private OrderItem sku2OrderItem(Spu spu, Sku sku, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(idWorker.nextId() + "");
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(sku.getPrice() * num);
        orderItem.setPayMoney(sku.getPrice() * num);
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight() * num);

        return orderItem;
    }
}