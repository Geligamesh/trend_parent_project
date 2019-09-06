package com.gxb.trend.client;

import cn.hutool.core.collection.CollectionUtil;
import com.gxb.trend.pojo.IndexData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndexDataClientFeignHystrix implements IndexDataClient{

    @Override
    public List<IndexData> getIndexData(String code) {
        IndexData indexData = new IndexData();
        indexData.setDate("0000-00-00");
        indexData.setClosePoint(0);
        return CollectionUtil.toList(indexData);
    }
}
