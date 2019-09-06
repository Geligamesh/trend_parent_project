package com.gxb.trend.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.gxb.trend.pojo.IndexData;
import com.gxb.trend.util.SpringContextUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "index_data")
public class IndexDataService {

    private Map<String, List<IndexData>> indexDataMap = new HashMap<>();
    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "third_part_not_connected")
    public List<IndexData> fresh(String code) {
        List<IndexData> indexDataList =fetch_indexes_from_third_part(code);

        indexDataMap.put(code, indexDataList);

        System.out.println("code:"+code);
        System.out.println("indexDataList:"+indexDataMap.get(code).size());

        IndexDataService indexDataService = SpringContextUtil.getBean(IndexDataService.class);
        indexDataService.remove(code);
        return indexDataService.store(code);
    }

    @CacheEvict(key = "'indexData-code-' + #p0")
    public void remove(String code) {

    }

    @CachePut(key = "'indexData-code-' + #p0")
    public List<IndexData> store(String code) {
        return indexDataMap.get(code);
    }

    @Cacheable(key = "'indexData-code-' + #p0")
    public List<IndexData> get(String code) {
        return CollUtil.toList();
    }

    public List<IndexData> fetch_indexes_from_third_part(String code) {
        List<Map> maps = restTemplate.getForObject("http://127.0.0.1:8090/indexes/"+code+".json",List.class);
        return map2IndexData(maps);
    }

    private List<IndexData> map2IndexData(List<Map> maps) {
        List<IndexData> indexDataList = new ArrayList<>();
        maps.stream().forEach(map -> {
            String date = map.get("date").toString();
            float closePoint = Convert.toFloat(map.get("closePoint"));
            IndexData indexData = new IndexData();
            indexData.setDate(date);
            indexData.setClosePoint(closePoint);
            indexDataList.add(indexData);
        });
        return indexDataList;
    }

    public List<IndexData> third_part_not_connected(String code) {
        System.out.println("third_part_not_connected");
        IndexData indexData = new IndexData();
        indexData.setDate("n/a");
        indexData.setClosePoint(0);
        return CollectionUtil.toList(indexData);
    }

}
