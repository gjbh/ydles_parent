package com.ydles.search.service.impl;


import com.alibaba.fastjson.JSON;

import com.ydles.goods.feign.SkuFeign;
import com.ydles.goods.pojo.Sku;
import com.ydles.search.dao.ESManagerMapper;
import com.ydles.search.pojo.SkuInfo;
import com.ydles.search.service.EsManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EsManagerServiceImpl implements EsManagerService {
    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    SkuFeign skuFeign;
    @Autowired
    ESManagerMapper esManagerMapper;
    //创建索引库 创建映射
    @Override
    public void createIndexAndMapping() {
        //创建索引库
        elasticsearchTemplate.createIndex(SkuInfo.class);
        //创建映射
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importData() {
        //1 查到现有所有sku
        List<Sku> skuList = skuFeign.findSkuListBySpuId("all");
        System.out.println("查询出的skulist多少："+skuList.size());

        //2导入es
        List<SkuInfo> skuinfoList = new ArrayList<>();
        for (Sku sku : skuList) {
            String skuStr = JSON.toJSONString(sku); //{id:12,name:asda}
            SkuInfo skuInfo = JSON.parseObject(skuStr, SkuInfo.class);

            //需求 Sku.spec string ------>SkuInfo.specMap Map
            Map map = JSON.parseObject(sku.getSpec(), Map.class);
            skuInfo.setSpecMap(map);

            skuinfoList.add(skuInfo);
        }
        esManagerMapper.saveAll(skuinfoList);
    }

    @Override
    public void importDataBySpuId(String spuId) {
        //1 查到spuId对应的sku
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        System.out.println("查询出的skulist多少："+skuList.size());

        //2导入es
        List<SkuInfo> skuinfoList = new ArrayList<>();
        for (Sku sku : skuList) {
            String skuStr = JSON.toJSONString(sku); //{id:12,name:asda}
            SkuInfo skuInfo = JSON.parseObject(skuStr, SkuInfo.class);

            //需求 Sku.spec string ------>SkuInfo.specMap Map
            Map map = JSON.parseObject(sku.getSpec(), Map.class);
            skuInfo.setSpecMap(map);

            skuinfoList.add(skuInfo);
        }
        esManagerMapper.saveAll(skuinfoList);
    }

    @Override
    public void delDataBySpuId(String spuId) {
        //查询spuid对应的sku
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        //根据id删除
        for (Sku sku : skuList) {
            System.out.println("要删除的sku"+sku.toString());
            esManagerMapper.deleteById(Long.parseLong(sku.getId()));
        }
    }

}
