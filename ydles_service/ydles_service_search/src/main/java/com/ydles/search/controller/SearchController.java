package com.ydles.search.controller;

import com.ydles.entity.Page;
import com.ydles.search.pojo.SkuInfo;
import com.ydles.search.service.SearchService;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;
    @GetMapping
    @ResponseBody
    public Map search(@RequestParam Map<String, String> searchMap) {
        handleSearchMap(searchMap);
        Map map = searchService.search(searchMap);
        return  map;
    }

    private void handleSearchMap(Map<String, String> searchMap) {
        Set<Map.Entry<String, String>> entries = searchMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().startsWith("spec_")){
               String replace =  entry.getValue().replace("+", "%2B");
               searchMap.put(entry.getKey(), replace);
            }
        }
    }

    //承接搜索页面的请求
    @GetMapping("/list")
    public String list(@RequestParam Map<String,String> searchMap, Model model){
        handleSearchMap(searchMap);
        //搜索到的结果

        Map resultMap = searchService.search(searchMap);
        model.addAttribute("resultMap", resultMap);
        //用户搜索的条件
        model.addAttribute("searchMap", searchMap);

        //拼接url
        StringBuilder url = new StringBuilder("/search/list");
        // 如果有搜索条件
        if (searchMap != null && searchMap.size() > 0) {
            url.append("?");
            //拼接
            for (String paramKey : searchMap.keySet()) {
                //排除特殊情况
                if(!"sortRule".equals(paramKey)&&!"sortField".equals(paramKey)&&!"pageNo".equals(paramKey)&&!"pageSize".equals(paramKey)){
                    url.append(paramKey).append("=").append(searchMap.get(paramKey)).append("&");
                }
            }

            String urlString = url.toString();
            //除去最后的&
            urlString=urlString.substring(0,urlString.length()-1);
            model.addAttribute("url", urlString);
        }else {
            model.addAttribute("url", url);
        }

        //分页
        Page<SkuInfo> page = new Page(Long.parseLong(String.valueOf(resultMap.get("total"))), Integer.parseInt(String.valueOf(resultMap.get("pageNo"))),  Integer.parseInt(String.valueOf(resultMap.get("pageSize"))));
        model.addAttribute("page",page);

        return "search";
    }


}
