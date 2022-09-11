package com.ydles.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ydles.search.pojo.SkuInfo;
import com.ydles.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map search(Map<String, String> searchMap) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (searchMap != null) {
            //多个搜索条件
            if (StringUtils.isNotEmpty(searchMap.get("keyword"))) {
              MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keyword"));
              boolQueryBuilder.must(matchQueryBuilder);
            }
            //品牌查询
            if (StringUtils.isNotEmpty(searchMap.get("brand"))) {
                //brandName keyword term查询
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
                //term查询一般放到filter中
                boolQueryBuilder.must(termQueryBuilder);
            }
            //规格查询
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    String value = searchMap.get(key).replace("%2B","+");
                    TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("specMap." + key.substring(5), value);
                    boolQueryBuilder.filter(termQueryBuilder);
                }
            }
            //价格区间查询
            if (StringUtils.isNotEmpty(searchMap.get("price"))) {
                String price = searchMap.get("price");
                String[] split = price.split("-");
                //优化后查询 codeReview
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(split[0]);
                if(split.length== 2){
                    rangeQueryBuilder.lte(split[1]);
                }
                    boolQueryBuilder.filter(rangeQueryBuilder);
                //优化前查询
//                if(split.length== 2){
//                    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(split[0]).lte(split[1]);
//                    boolQueryBuilder.filter(rangeQueryBuilder);
//                }else {
//                    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(split[0]);
//                    boolQueryBuilder.filter(rangeQueryBuilder);
//                }
            }
        }

        //搜索
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        //聚合 品牌
        String skuBrand = "skuBrand";
        TermsAggregationBuilder brandTerms = AggregationBuilders.terms(skuBrand).field("brandName");
        nativeSearchQueryBuilder.addAggregation(brandTerms);
        //聚合 规格
        String skuSpec = "skuSpec";
        TermsAggregationBuilder specTerms = AggregationBuilders.terms(skuSpec).field("spec.keyword");
        nativeSearchQueryBuilder.addAggregation(specTerms);
        //分页
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        if(StringUtils.isEmpty(pageNo)){
            pageNo = "1";
        }
        if(StringUtils.isEmpty(pageSize)){
            pageSize = "30";
        }
        PageRequest pageRequest = PageRequest.of(Integer.parseInt(pageNo)-1,Integer.parseInt(pageSize));
        nativeSearchQueryBuilder.withPageable(pageRequest);
        //排序
        if (StringUtils.isNotEmpty(searchMap.get("sortField")) && StringUtils.isNotEmpty(searchMap.get("sortRule"))) {
            if (searchMap.get("sortRule").equals("ASC")) {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
            }else {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
            }
        }
        //设置高亮
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        //设置高亮前缀
        field.preTags("<span style='color:red'>");
        //设置高亮后缀
        field.postTags("</span>");
        nativeSearchQueryBuilder.withHighlightFields(field);


        //构建查询
        /**
         * 1.nativeSearchQuery 承接条件
         * 2.SkuInfo.class 实体类类型
         * 3.SearchResultMapper 结果集映射器
         *
         * skuinfos 查询出来的结果集
         */
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
            //搜索结果和对象如何映射起来
           @Override
           public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
               List<T> list = new ArrayList<>();
               SearchHits hits = searchResponse.getHits();
               long totalHits = hits.getTotalHits();
               SearchHit[] hits1 = hits.getHits();
               for (SearchHit hit : hits1) {
                  String sourceAsString = hit.getSourceAsString();
                  SkuInfo skuInfo = JSON.parseObject(sourceAsString, SkuInfo.class);
                  //获取高亮 设置到name属性中
                  Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                  HighlightField highlightField = highlightFields.get("name");
                  Text[] fragments = highlightField.getFragments();
                  String realName = "";
                  for (Text fragment : fragments) {
                      realName += fragment;
                  }
                  skuInfo.setName(realName);

                  list.add((T) skuInfo);
               }
               return new AggregatedPageImpl<>(list,pageable, totalHits, searchResponse.getAggregations());
           }
       });
        //封装结果
        Map resultMap = new HashMap();
        //总记录数
        resultMap.put("total",skuInfos.getTotalElements());
        //总页面
        resultMap.put("totalpages",skuInfos.getTotalPages());
        //数据集合
        resultMap.put("rows",skuInfos.getContent());
        //把聚合结果传给前端
        StringTerms brandStringTerms = (StringTerms) skuInfos.getAggregation(skuBrand);
        List<String> brandList = brandStringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brandList",brandList);

        //把聚合结果传给前端
        StringTerms specStringTerms = (StringTerms) skuInfos.getAggregation(skuSpec);
        List<String> specList = specStringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("specList",formatSpec(specList));
        //当前页 每页多少
        resultMap.put("pageNo",pageNo);//当前第几页
        resultMap.put("pageSize",pageSize);//页大小
        return resultMap;
    }

    //规格转换
    public Map<String , Set<String>> formatSpec(List<String> specList){
        Map<String ,Set<String>> resultMap = new HashMap<>();
        //遍历list
        for (String spec : specList) {
            //解析规格
            Map<String,String> specMap=JSON.parseObject(spec,Map.class);
            //遍历map
            for (String key:specMap.keySet()){
                //获取规格值
                Set<String> valueSet = resultMap.get(key);
                if (valueSet==null){
                    valueSet = new HashSet<>();
                }
                valueSet.add(specMap.get(key));
                resultMap.put(key,valueSet);
            }
        }
        return resultMap;
    }
}

