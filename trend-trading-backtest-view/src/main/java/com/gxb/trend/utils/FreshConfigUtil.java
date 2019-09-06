package com.gxb.trend.utils;

import cn.hutool.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class FreshConfigUtil {

    public static void main(String[] args) {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        String result = HttpUtil.createPost("http://localhost:8041/actuator/bus-refresh").addHeaders(headers).execute().body();
        System.out.println("result:" + result);
        System.out.println("refresh完成");
    }
}
