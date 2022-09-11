package com.ydles.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydles.goods.dao.*;
import com.ydles.goods.pojo.*;
import com.ydles.goods.service.SpuService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ydles.util.IdWorker;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.beans.Transient;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryBrandMapper categoryBrandMapper;
    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param goods
     */
    @Transactional
    @Override
    public void add(Goods goods){
        Spu spu = goods.getSpu();
        long spuId = idWorker.nextId();
        spu.setId(String.valueOf(spuId));
        spu.setIsDelete("0");
        spu.setIsMarketable("0");
        spu.setStatus("0");
        spuMapper.insertSelective(spu);

        //保存sku集合数据到数据库
        saveSkuList(goods);
    }

    @Autowired
    SkuMapper skuMapper;
    //插入sku集合数据到数据库
    private void saveSkuList(Goods goods) {
        List<Sku> skuList = goods.getSkuList();
        Spu spu = goods.getSpu();
        String skuName = spu.getName();
        String spuId = spu.getId();
        Integer category3Id = spu.getCategory3Id();
        Category category = categoryMapper.selectByPrimaryKey(category3Id);
        Integer brandId = spu.getBrandId();
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        for (Sku sku : skuList) {
            long skuId = idWorker.nextId();
            sku.setId(skuId + "");
            String speStr = sku.getSpec();
            //判断speStr是否为空
            if (StringUtil.isNullOrEmpty(speStr)) {
                speStr = "{}";
            }
            Map sepMap = JSON.parseObject(speStr, Map.class);
            if (sepMap != null && sepMap.size() > 0) {
                for (Object value : sepMap.values()) {
                    skuName += " " + value;
                }
                sku.setName(skuName);

                sku.setName(skuName);//名称
                sku.setSpuId(spuId);//设置spu的ID
                sku.setCreateTime(new Date());//创建日期
                sku.setUpdateTime(new Date());//修改日期
                sku.setCategoryId(category.getId());//商品分类ID
                sku.setCategoryName(category.getName());//商品分类名称
                sku.setBrandName(brand.getName());//品牌名称
                skuMapper.insertSelective(sku);//插入sku表数据
            }
        }
        //品牌的分类的关系表 插入数据
        //查询有没有关联
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrand_id(brandId);
        categoryBrand.setCategory_id(category3Id);
        int i = categoryBrandMapper.selectCount(categoryBrand);
        //如果没有关联插入关联
        if (i <= 0) {
            categoryBrandMapper.insertSelective(categoryBrand);

        }
    }


    /**
     * 修改
     * @param goods
     */
    @Override
    public void update(Goods goods){
        //取出spu部分
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKey(spu);
        //删除原sku列表
        Example example=new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",spu.getId());
        skuMapper.deleteByExample(example);

        saveSkuList(goods);//保存sku列表
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        //逻辑删除
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //判断是否处于下架
        if(!spu.getIsMarketable().equals("0")){
            throw new RuntimeException("下架状态才能删除商品");
        }

        //是下架状态 经行软删除
        spu.setIsDelete("1");
        spu.setStatus("0");

        //存入数据库
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    @Override
    public Goods findGoodsById(String spuId) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //查询SKU 列表
        Example example=new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",spuId);
        List<Sku> skuList = skuMapper.selectByExample(example);

        //封装，返回
        Goods goods=new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    @Override
    public void audit(String id) {
        //查询spu对象
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new RuntimeException("当前商品不存在");
        }
        //判断当前spu是否处于删除状态
        if ("1".equals(spu.getIsDelete())){
            throw new RuntimeException("当前商品处于删除状态");
        }
        //不处于删除状态,修改审核状态为1,上下架状态为1
        spu.setStatus("1");
        spu.setIsMarketable("1");
        //执行修改操作
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void pull(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new RuntimeException("当前商品不存在");
        }
        //判断当前商品是否处于删除状态
        if ("1".equals(spu.getIsDelete())){
            throw new RuntimeException("当前商品处于删除状态");
        }
        //商品处于未删除状态的话,则修改上下架状态为已下架(0)
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }
    //审核上架
    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!spu.getStatus().equals("1")){
            throw new RuntimeException("未通过审核的商品不能上架！");
        }
        spu.setIsMarketable("1");//上架状态
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void restore(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //检查是否删除的商品
        if(!spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品未删除！");
        }
        spu.setIsDelete("0");//未删除
        spu.setStatus("0");//未审核
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void realDelete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //检查是否删除的商品
        if(spu==null){
            throw new RuntimeException("当前商品不存在");
        }
        if(!"1".equals(spu.getIsDelete())){
            throw new RuntimeException("当前商品没有逻辑删除,不能物理删除");

        }
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
