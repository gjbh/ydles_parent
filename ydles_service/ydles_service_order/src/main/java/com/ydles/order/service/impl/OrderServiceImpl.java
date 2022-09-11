package com.ydles.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydles.goods.feign.SkuFeign;
import com.ydles.order.config.RabbitMQConfig;
import com.ydles.order.dao.*;
import com.ydles.order.pojo.*;
import com.ydles.order.service.CartService;
import com.ydles.order.service.OrderService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ydles.pay.feign.PayFeign;
import com.ydles.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private  static  final String CART = "cart:";
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    CartService cartService;
    @Autowired
    IdWorker idWorker;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    SkuFeign skuFeign;
    @Autowired
    StringRedisTemplate stringredisTemplate;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    PayFeign payFeign;
    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 下单
     * @param order
     */
    @Override
    @Transactional //事务 只能控制本地事务
    public String add(Order order){
        //1 获取 购物车信息
        Map cartMap = cartService.list(order.getUsername());
        //map.put("orderItemList",orderItemList);
        Integer totalNum = (Integer) cartMap.get("totalNum");
        Integer totalMoney = (Integer) cartMap.get("totalMoney");

        //2 order表里存数据
        String orderId = idWorker.nextId()+"";
        order.setId(orderId);
        order.setTotalNum(totalNum);
        order.setTotalMoney(totalMoney);
        //优惠金额 怎么算      本店满50-5 跨店 300-50
        //邮费金额 怎么算
        order.setPayMoney(totalMoney);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        //买家留言
        order.setBuyerRate("0");
        order.setSourceType("1");
        order.setOrderStatus("0");
        order.setPayStatus("0");
        order.setConsignStatus("0");
        order.setIsDelete("0");

        orderMapper.insertSelective(order);

        //3 orderItem表里存数据
        List<OrderItem> orderItemList = (List<OrderItem>) cartMap.get("orderItemList");
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(orderId);
            orderItem.setPostFee(0);
            orderItem.setIsReturn("0");
            orderItemMapper.insertSelective(orderItem);
        }
        //远程调用减库存
        skuFeign.decrCount(order.getUsername());

        //添加积分
        System.out.println("添加积分");
        Task task = new Task();
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setMqExchange(RabbitMQConfig.EX_BUYING_ADDPOINTUSER);
        task.setMqRoutingkey(RabbitMQConfig.CG_BUYING_ADDPOINT_KEY);
        //RequestBody 消息数据 需要order_id user_id point
        Map map = new HashMap();
        map.put("order_id",orderId);
        map.put("user_id",order.getUsername());
        map.put("point",totalMoney);//消费多少加多少积分
        task.setRequestBody(JSON.toJSONString(map));

        taskMapper.insertSelective(task);

        //往延迟队列发消息
        rabbitTemplate.convertAndSend("","queue.ordercreate",orderId);
        //4 删除购物车信息
        stringredisTemplate.delete(CART+order.getUsername());
        return orderId;
    }
    /**
     * 修改
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Order>)orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Order>)orderMapper.selectByExample(example);
    }

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Override
    public void updatePayStatus(String orderId, String transactionId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order!=null  && "0".equals(order.getPayStatus())){  //存在订单且状态为0
            //查询订单是否存在 并且状态未支付
            order.setPayStatus("1");
            order.setOrderStatus("1");
            order.setUpdateTime(new Date());
            order.setPayTime(new Date());
            order.setTransactionId(transactionId);//微信返回的交易流水号
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单变动日志
            OrderLog orderLog=new OrderLog();
            orderLog.setId( idWorker.nextId()+"" );
            orderLog.setOperater("system");// 系统
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("支付流水号"+transactionId);
            orderLog.setOrderId(order.getId());
            orderLogMapper.insertSelective(orderLog);
        }
    }

    //关闭订单
    @Override
    public void closeOrder(String orderId) {
        System.out.println("关闭订单开启了:"+orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order==null){
            throw new RuntimeException("这笔订单不存在！");
        }
        if(!order.getOrderStatus().equals("0")){
            System.out.println("这笔订单不用关闭");
            return;
        }
        System.out.println("关闭订单逻辑通过校验："+orderId);

        //1支付服务 微信查询订单
        Map<String, String> wxQueryMap = payFeign.queryOrder(orderId).getData();

        //2.1支付了 order表修改
        if(wxQueryMap.get("trade_state").equals("SUCCESS")){
            updatePayStatus(orderId,wxQueryMap.get("transaction_id"));
            System.out.println("已支付"+orderId);
        }

        //2.2未支付 关闭订单微信 内部回滚库存 订单状态关闭
        if(wxQueryMap.get("trade_state").equals("NOTPAY")){
            //1关闭订单微信
            payFeign.closeOrder(orderId);

            //2订单状态关闭
            System.out.println("本项目关闭订单了");
            order.setOrderStatus("9");//订单状态 0下单 1支付 2发货 3收货 4退货 9关闭
            order.setCloseTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);

            //orderLog表 新增数据
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(orderId);
            orderLog.setOrderStatus("9");
            orderLog.setPayStatus("0");
            orderLog.setConsignStatus("0");
            orderLog.setRemarks("超时未支付！");
            orderLogMapper.insertSelective(orderLog);
            //3内部回滚库存
            //查出来这笔订单的所有购物项
            OrderItem orderItem=new OrderItem();
            orderItem.setOrderId(orderId);
            List<OrderItem> orderItemList = orderItemMapper.select(orderItem);

            for (OrderItem orderItem1 : orderItemList) {
                skuFeign.resumeStock(orderItem1.getSkuId(),orderItem1.getNum());
            }

        }
    }

    //批量发货
    @Transactional
    public void batchSend(List<Order> orderList) {
        //循环1 物流公司名称和物流单号 不能为空
        for (Order order : orderList) {
            if(order.getId()==null){
                throw new RuntimeException("订单号为空！");
            }
            if(order.getShippingName()==null||order.getShippingCode()==null){
                throw new RuntimeException("物流公司名称或单号为空！");
            }
        }
        //循环2 查询订单状态 校验
        for (Order order : orderList) {
            Order queryOrder = orderMapper.selectByPrimaryKey(order.getId());
            if(!queryOrder.getOrderStatus().equals("1")||!queryOrder.getConsignStatus().equals("0")){
                throw new RuntimeException("订单状态不对，不能发货！");
            }
        }
        //循环3 发货
        for (Order order : orderList) {
            order.setOrderStatus("2");
            order.setConsignStatus("1");
            order.setUpdateTime(new Date());
            order.setConsignTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);

            //orderLog表 新增数据
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");
            orderLog.setOperater("店小二");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(order.getId());
            orderLog.setOrderStatus("2");
            orderLog.setPayStatus("1");
            orderLog.setConsignStatus("1");
            orderLog.setRemarks("批量发货");
            orderLogMapper.insertSelective(orderLog);
        }

    }

    //确定收货
    @Override
    public void confirmTask(String orderId, String operator) {
        Order order = orderMapper.selectByPrimaryKey( orderId );
        if(order==null){
            throw new RuntimeException( "订单不存在" );
        }
        if( !"1".equals( order.getConsignStatus() )){
            throw new RuntimeException( "订单未发货" );
        }
        order.setConsignStatus("2"); //已送达
        order.setOrderStatus( "3" );//已完成
        order.setUpdateTime( new Date() );
        order.setEndTime( new Date() );//交易结束
        orderMapper.updateByPrimaryKeySelective( order );
        //记录订单变动日志
        OrderLog orderLog=new OrderLog();
        orderLog.setId( idWorker.nextId()+"" );
        orderLog.setOperateTime(new Date());//当前日期
        orderLog.setOperater( operator );//系统？管理员？用户？
        orderLog.setOrderStatus("3");
        orderLog.setOrderId(order.getId());
        orderLogMapper.insertSelective(orderLog);
    }

    @Autowired
    OrderConfigMapper orderConfigMapper;
    @Override
    public void autoTack() {
        //1 从配置表中获取15天值
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey("1");
        Integer takeTimeout = orderConfig.getTakeTimeout();//15
        //2 推算拿几号之前发货的订单
        LocalDate now=LocalDate.now();//当前
        LocalDate date = now.plusDays(-takeTimeout);
        System.out.println(date);
        //3 查询发货超过15天的订单
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderStatus","2");
        criteria.andLessThan("consignTime", date);
        List<Order> orderList = orderMapper.selectByExample(example);
        //4 循环 把这些订单 收货
        for (Order order : orderList) {
            confirmTask
                    (order.getId(),"system");
        }
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andEqualTo("payType",searchMap.get("payType"));
           	}
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
           	}
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
           	}
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
           	}
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
           	}
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
           	}
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
           	}
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
           	}
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
           	}
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andEqualTo("sourceType",searchMap.get("sourceType"));
           	}
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
           	}
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andEqualTo("orderStatus",searchMap.get("orderStatus"));
           	}
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andEqualTo("payStatus",searchMap.get("payStatus"));
           	}
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andEqualTo("consignStatus",searchMap.get("consignStatus"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
