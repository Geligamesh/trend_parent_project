package com.gxb.trend.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.gxb.trend.client.IndexDataClient;
import com.gxb.trend.pojo.AnnualProfit;
import com.gxb.trend.pojo.IndexData;
import com.gxb.trend.pojo.Profit;
import com.gxb.trend.pojo.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BackTestService {

    @Autowired
    private IndexDataClient indexDataClient;

    public List<IndexData> listIndexData(String code){
        List<IndexData> result = indexDataClient.getIndexData(code);
        Collections.reverse(result);
        for (IndexData indexData : result) {
            System.out.println(indexData.getDate());
        }
        return result;
    }

    public Map<String,Object> simulate(int ma, float sellRate, float buyRate, float serviceCharge, List<IndexData> indexDataList)  {

        List<Profit> profits =new ArrayList<>();
        List<Trade> trades = new ArrayList<>();
        float initCash = 1000;
        float cash = initCash;
        float share = 0;
        float value;

        float winCount = 0;
        float totalWinRate = 0;
        float avgWinRate;
        float lossCount = 0;
        float totalLossRate = 0;
        float avgLossRate;

        float init =0;
        if(!indexDataList.isEmpty())
            init = indexDataList.get(0).getClosePoint();

        for (int i = 0; i<indexDataList.size() ; i++) {
            IndexData indexData = indexDataList.get(i);
            float closePoint = indexData.getClosePoint();
            float avg = getMA(i,ma,indexDataList);
            float max = getMax(i,ma,indexDataList);

            float increase_rate = closePoint/avg;
            float decrease_rate = closePoint/max;

            if(avg!=0) {
                //buy 超过了均线
                if(increase_rate>buyRate  ) {
                    //如果没买
                    if(0 == share) {
                        share = cash / closePoint;
                        cash = 0;

                        Trade trade = new Trade();
                        trade.setBuyDate(indexData.getDate());
                        trade.setBuyClosePoint(indexData.getClosePoint());
                        trade.setSellClosePoint(0);
                        trade.setSellDate("n/a");
                        trades.add(trade);
                    }
                }
                //sell 低于了卖点
                else if(decrease_rate<sellRate ) {
                    //如果没卖
                    if(0!= share){
                        cash = closePoint * share * (1-serviceCharge);
                        share = 0;

                        Trade trade = trades.get(trades.size() - 1);
                        trade.setSellClosePoint(indexData.getClosePoint());
                        trade.setSellDate(indexData.getDate());
                        float rate = cash / initCash;
                        trade.setRate(rate);

                        if(trade.getSellClosePoint() - trade.getBuyClosePoint() > 0) {
                            totalWinRate += ((trade.getSellClosePoint() - trade.getBuyClosePoint()) / trade.getBuyClosePoint());
                            winCount++;
                        }else {
                            totalLossRate += ((trade.getSellClosePoint() - trade.getBuyClosePoint()) / trade.getBuyClosePoint());
                            lossCount++;
                        }
                    }
                }
                //do nothing
                else{

                }
            }

            if(share!=0) {
                value = closePoint * share;
            }
            else {
                value = cash;
            }
            float rate = value/initCash;

            Profit profit = new Profit();
            profit.setDate(indexData.getDate());
            profit.setValue(rate*init);

            System.out.println("profit.value:" + profit.getValue());
            profits.add(profit);

        }
        avgWinRate = totalWinRate / winCount;
        avgLossRate = totalLossRate / lossCount;
        List<AnnualProfit> annualProfits = calculateAnnualProfits(indexDataList, profits);

        Map<String,Object> map = new HashMap<>();
        map.put("profits", profits);
        map.put("trades", trades);
        map.put("winCount", winCount);
        map.put("lossCount", lossCount);
        map.put("avgWinRate", avgWinRate);
        map.put("avgLossRate", avgLossRate);
        map.put("annualProfits",annualProfits);
        return map;
    }

    private static float getMax(int i, int day, List<IndexData> list) {
        int start = i-1-day;
        if(start<0){
            start = 0;
        }
        int now = i-1;
        float max = 0;
        for (int j = start; j < now; j++) {
            IndexData bean =list.get(j);
            if(bean.getClosePoint()>max) {
                max = bean.getClosePoint();
            }
        }
        return max;
    }

    private static float getMA(int i, int ma, List<IndexData> list) {
        int start = i-1-ma;
        int now = i-1;

        if(start<0)
            return 0;

        float sum = 0;
        float avg;
        for (int j = start; j < now; j++) {
            IndexData bean =list.get(j);
            sum += bean.getClosePoint();
        }
        avg = sum / (now - start);
        return avg;
    }

    public float getYear(List<IndexData> indexDataList) {
        float years;
        String strStartDate = indexDataList.get(0).getDate();
        String strEndDate = indexDataList.get(indexDataList.size() - 1).getDate();
        DateTime startDate = DateUtil.parse(strStartDate);
        DateTime endDate = DateUtil.parse(strEndDate);
        long days = DateUtil.between(startDate, endDate, DateUnit.DAY);
        years = days / 365f;
        return years;
    }

    //获取某个日期如 2019-05-21 里的年份
    private int getYear(String date) {
        String strYear = StrUtil.subBefore(date, "-", false);
        return Convert.toInt(strYear);
    }

    //计算某一年的指数收益
    private float getIndexIncome(int year,List<IndexData> indexDataList) {
        IndexData first = null;
        IndexData last = null;

        for (IndexData indexData : indexDataList) {
            String strDate = indexData.getDate();
            int currentYear = getYear(strDate);
            if (currentYear == year) {
                if (first == null) {
                    first = indexData;
                }
                last = indexData;
            }
        }
        return (last.getClosePoint() - first.getClosePoint()) / first.getClosePoint();
    }

    //计算某一年的趋势收益
    private float getTrendIncome(int year,List<Profit> profits) {
        Profit first = null;
        Profit last = null;
        for (Profit profit : profits) {
            int currentYear = getYear(profit.getDate());
            if (currentYear == year) {
                if (first == null) {
                    first = profit;
                }
                last = profit;
            }
        }
        return (last.getValue() - first.getValue()) / first.getValue();
    }

    // 计算完整时间范围内，每一年的指数投资收益和趋势投资收益
    private List<AnnualProfit> calculateAnnualProfits(List<IndexData> indexDataList,List<Profit> profits) {

        List<AnnualProfit> annualProfits = new ArrayList<>();
        String strStartDate = indexDataList.get(0).getDate();
        String strEndDate = indexDataList.get(indexDataList.size() - 1).getDate();
        int startYear = DateUtil.year(DateUtil.parse(strStartDate));
        int endYear = DateUtil.year(DateUtil.parse(strEndDate));

        for (int year = startYear; year <= endYear; year++) {
            AnnualProfit annualProfit = new AnnualProfit();
            annualProfit.setYear(year);
            float indexIncome = getIndexIncome(year, indexDataList);
            float trendIncome = getTrendIncome(year, profits);
            annualProfit.setIndexIncome(indexIncome);
            annualProfit.setTrendIncome(trendIncome);
            annualProfits.add(annualProfit);
        }
        return annualProfits;
    }
}
