package com.gxb.trend.controller;

import com.gxb.trend.config.IPConfiguration;
import com.gxb.trend.pojo.Index;
import com.gxb.trend.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IndexController {

    @Autowired
    private IndexService indexService;
    @Autowired
    private IPConfiguration ipConfiguration;

    @GetMapping("codes")
    public List<Index> codes() {
        System.out.println("current instance's port is " + ipConfiguration.getPort());
        return indexService.get();
    }
}
