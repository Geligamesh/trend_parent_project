package com.gxb.trend.controller;

import com.gxb.trend.config.IPConfiguration;
import com.gxb.trend.pojo.IndexData;
import com.gxb.trend.service.IndexDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexController {

    @Autowired
    private IndexDataService indexDataService;
    @Autowired
    private IPConfiguration ipConfiguration;

    @GetMapping("data/{code}")
    public List<IndexData> get(@PathVariable("code") String code) {
        System.out.println("current instance's port is " + ipConfiguration.getPort());
        return indexDataService.get(code);
    }
}
