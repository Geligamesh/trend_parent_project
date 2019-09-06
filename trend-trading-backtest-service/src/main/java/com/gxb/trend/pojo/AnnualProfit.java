package com.gxb.trend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnualProfit {

    private int year;
    private float indexIncome;
    private float trendIncome;
}
