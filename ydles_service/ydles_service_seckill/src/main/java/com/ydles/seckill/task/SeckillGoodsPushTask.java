package com.ydles.seckill.task;

import com.ydles.seckill.dao.SeckillGoodsMapper;
import com.ydles.seckill.pojo.SeckillGoods;
import com.ydles.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoodsPushTask {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //redis key开头
    public static final String SECKILL_GOODS_KEY = "seckill_goods_";
    //秒杀商品库存头
    public static final String SECKILL_GOODS_STOCK_COUNT_KEY="seckill_goods_stock_count_";

    /**
     * 定时将秒杀商品存入redis
     * 暂定为30秒一次，正常业务为每天凌晨触发
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void loadSecKillGoodsToRedis() {
        /**
         * 1.查询所有符合条件的秒杀商品
         * 	1) 获取时间段集合并循环遍历出每一个时间段
         * 	2) 获取每一个时间段名称,用于后续redis中key的设置
         * 	3) 状态必须为审核通过 status=1
         * 	4) 商品库存个数>0
         * 	5) 秒杀商品开始时间>=当前时间段
         * 	6) 秒杀商品结束<当前时间段+2小时
         * 	7) 排除之前已经加载到Redis缓存中的商品数据
         * 	8) 执行查询获取对应的结果集
         * 2.将秒杀商品存入缓存
         */
        //1) 获取时间段集合并循环遍历出每一个时间段
        List<Date> dateMenus = DateUtil.getDateMenus(); // 5个

        for (Date dateMenu : dateMenus) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 2) 获取每一个时间段名称,用于后续redis中key的设置
            String redisExtName = DateUtil.date2Str(dateMenu);

            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            // 3) 状态必须为审核通过 status=1
            criteria.andEqualTo("status", "1");
            // 4) 商品库存个数>0 gt
            criteria.andGreaterThan("stockCount", 0);
            // 5) 秒杀商品开始时间>=当前时间段 gte
            criteria.andGreaterThanOrEqualTo("startTime", simpleDateFormat.format(dateMenu));
            // 6) 秒杀商品结束<当前时间段+2小时 lt
            criteria.andLessThan("endTime", simpleDateFormat.format(DateUtil.addDateHour(dateMenu, 2)));

            // 7) 排除之前已经加载到Redis缓存中的商品数据
            Set keys = redisTemplate.boundHashOps(SECKILL_GOODS_KEY + redisExtName).keys();//key field value
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }

            // 8) 执行查询获取对应的结果集
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

            //2.将秒杀商品存入缓存
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //放入商品信息
                redisTemplate.opsForHash().put(SECKILL_GOODS_KEY + redisExtName, seckillGoods.getId(), seckillGoods);
                //放入商品库存
                redisTemplate.opsForValue().set(SECKILL_GOODS_STOCK_COUNT_KEY + seckillGoods.getId(), seckillGoods.getStockCount());
            }
        }

    }
}
