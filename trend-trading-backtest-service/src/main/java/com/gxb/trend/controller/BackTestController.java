package com.gxb.trend.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.gxb.trend.pojo.AnnualProfit;
import com.gxb.trend.pojo.IndexData;
import com.gxb.trend.pojo.Profit;
import com.gxb.trend.pojo.Trade;
import com.gxb.trend.service.BackTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
public class BackTestController {

    @Autowired
    private BackTestService backTestService;

    @GetMapping("/simulate/{code}/{ma}/{buyThreshold}/{sellThreshold}/{serviceCharge}/{startDate}/{endDate}")
    @CrossOrigin
    public Map<String,Object> backTest(
            @PathVariable("code") String code,
            @PathVariable("ma") Integer ma,
            @PathVariable("buyThreshold") float buyThreshold,
            @PathVariable("sellThreshold") float sellThreshold,
            @PathVariable("serviceCharge") float serviceCharge,
            @PathVariable("startDate") String strStartDate,
            @PathVariable("endDate") String strEndDate){
        List<IndexData> allIndexDatas = backTestService.listIndexData(code);
        String indexStartDate = allIndexDatas.get(0).getDate();
        String indexEndDate = allIndexDatas.get(allIndexDatas.size() - 1).getDate();
        allIndexDatas = filterByDateRange(allIndexDatas, strStartDate, strEndDate);

        float sellRate = sellThreshold;
        float buyRate = buyThreshold;
        Map<String, Object> simulate = backTestService.simulate(ma, sellRate, buyRate, serviceCharge, allIndexDatas);
        List<Profit> profits = (List<Profit>) simulate.get("profits");
        List<Trade> trades = (List<Trade>) simulate.get("trades");
        float years = backTestService.getYear(allIndexDatas);
        float indexIncomeTotal = (allIndexDatas.get(allIndexDatas.size() - 1).getClosePoint() - allIndexDatas.get(0).getClosePoint()) /  allIndexDatas.get(0).getClosePoint();
        float indexIncomeAnnual = (float) (Math.pow(1 + indexIncomeTotal, 1/years) - 1);
        float trendIncomeTotal = (profits.get(profits.size() - 1).getValue() - profits.get(0).getValue()) / profits.get(0).getValue();
        float trendIncomeAnnual = (float) (Math.pow(1 + trendIncomeTotal, 1 / years) - 1);
        List<AnnualProfit> annualProfits = (List<AnnualProfit>) simulate.get("annualProfits");

        Map<String,Object> result = new HashMap<>();
        result.put("indexData", allIndexDatas);
        result.put("indexStartDate", indexStartDate);
        result.put("indexEndDate", indexEndDate);
        result.put("profits", profits);
        result.put("trades", trades);
        result.put("winCount", simulate.get("winCount"));
        result.put("lossCount", simulate.get("lossCount"));
        result.put("avgWinRate", simulate.get("avgWinRate"));
        result.put("avgLossRate",simulate.get("avgLossRate"));
        result.put("years", years);
        result.put("indexIncomeTotal", indexIncomeTotal);
        result.put("indexIncomeAnnual", indexIncomeAnnual);
        result.put("trendIncomeTotal",trendIncomeTotal);
        result.put("trendIncomeAnnual",trendIncomeAnnual);
        result.put("annualProfits", annualProfits);
        return result;
    }

    private List<IndexData> filterByDateRange(List<IndexData> allIndexDatas, String strStartDate, String strEndDate) {
        if(StrUtil.isBlankOrUndefined(strStartDate) || StrUtil.isBlankOrUndefined(strEndDate)) {
            return allIndexDatas;
        }
        List<IndexData> result = new ArrayList<>();
        Date startDate = DateUtil.parse(strStartDate);
        Date endDate = DateUtil.parse(strEndDate);
        allIndexDatas.stream().forEach(indexData -> {
            Date date = DateUtil.parse(indexData.getDate());
            if(date.getTime() >= startDate.getTime() && date.getTime() <= endDate.getTime()) {
                result.add(indexData);
            }
        });
        return result;
    }


}
