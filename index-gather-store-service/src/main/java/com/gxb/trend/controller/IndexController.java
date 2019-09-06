package com.gxb.trend.controller;

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

    //  http://127.0.0.1:80v
    //  http://127.0.0.1:8001/getCodes
    //  http://127.0.0.1:8001/removeCodes

    @GetMapping("getCodes")
    public List<Index> get() {
        return indexService.get();
    }

    @GetMapping("removeCodes")
    public String removeCodes(){
        indexService.remove();
        return "remove codes successfully";
    }

    @GetMapping("freshCodes")
    public List<Index> fresh() {
        return indexService.fresh();
    }


    public static void main(String[] args) {
        System.out.println(1.0f / 0 >= 1);
    }
}
