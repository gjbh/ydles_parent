package com.ydles.controller;


import com.ydles.entity.Result;
import com.ydles.order.feign.CartFeign;
import com.ydles.order.feign.OrderFeign;


import com.ydles.order.pojo.Order;
import com.ydles.order.pojo.OrderItem;
import com.ydles.user.feign.AddressFeign;
import com.ydles.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/worder")
public class OrderController {
    @Autowired
    private OrderFeign orderFeign;
    @Autowired
    AddressFeign addressFeign;
    @Autowired
    CartFeign cartFeign;
    @RequestMapping("/ready/order")
    public String readyOrder(Model model) {
        //收件人
        List<Address> addressList = addressFeign.list();
        model.addAttribute("addressList", addressList);
        //默认收件人信息
        for (Address address : addressList) {
            if ("1".equals(address.getIsDefault())) {
                //默认收件人
                model.addAttribute("deAddr", address);
                break;
            }
        }
        //购物车信息
        Map map = cartFeign.list();
        //总购物项数
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        model.addAttribute("carts", orderItemList);
        //总价格
        Integer totalMoney = (Integer) map.get("totalMoney");
        model.addAttribute("totalMoney", totalMoney);
        //总价数
        Integer totalNum = (Integer) map.get("totalNum");
        model.addAttribute("totalNum", totalNum);

        return "order";
    }

    @PostMapping("/add")
    @ResponseBody
    public Result add(@RequestBody Order order) {
        Result result = orderFeign.add(order);
        return result;
    }
    @GetMapping("/toPayPage")
    public String toPayPage(String orderId,Model model){
        //获取到订单的相关信息
        Order order = orderFeign.findById(orderId).getData();
        model.addAttribute("orderId",orderId);
        model.addAttribute("payMoney",order.getPayMoney());
        return "pay";
    }
}
