package com.ydles.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydles.goods.feign.CategoryFeign;
import com.ydles.goods.feign.SkuFeign;
import com.ydles.goods.feign.SpuFeign;
import com.ydles.goods.pojo.Category;
import com.ydles.goods.pojo.Sku;
import com.ydles.goods.pojo.Spu;
import com.ydles.page.service.PageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class PageServiceImpl implements PageService {
    @Autowired
    TemplateEngine templateEngine;
    @Value("${pagepath}")
    String pagepath;
    //生成静态化页面
    @Override
    public void generateHtml(String spuId) {
        //上下文
        Context context = new Context();
        Map<String,Object> dataMap = getData(spuId);
        context.setVariables(dataMap);
        //文件 模板
        File dir = new File(pagepath);//文件夹
        //判断文件夹是否存在 不存在就创建
        if (!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(pagepath+ "/" +spuId + ".html");//文件
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            templateEngine.process("item",context,writer);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Autowired
    SpuFeign spuFeign;
    @Autowired
    SkuFeign skuFeign;
    @Autowired
    CategoryFeign categoryFeign;
    //获取数据
    public Map<String,Object> getData(String spuId){
        Map<String,Object> resultMap = new HashMap<>();
        //获取数据
        //获取spu
        Spu spu = spuFeign.findSpuById(spuId).getData();
        resultMap.put("spu",spu);
        //获取图片
        String images = spu.getImages();
        if (StringUtils.isNotEmpty(images)){
            String[] imagesList = images.split(",");
            resultMap.put("imagesList",imagesList);
        }
        //获取specificationList
        String specItems = spu.getSpecItems();
        if(StringUtils.isNotEmpty(specItems)){
            Map specMap = JSON.parseObject(specItems, Map.class);
            resultMap.put("specificationList",specMap);
        }
        //获取sku
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        resultMap.put("skuList",skuList);
        //获取category
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category1",category1);
        resultMap.put("category2",category2);
        resultMap.put("category3",category3);
        return resultMap;
    }
}
