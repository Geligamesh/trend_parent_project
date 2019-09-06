package com.gxb.trend.job;

import cn.hutool.core.date.DateUtil;
import com.gxb.trend.pojo.Index;
import com.gxb.trend.service.IndexDataService;
import com.gxb.trend.service.IndexService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

public class IndexDataSyncJob extends QuartzJobBean {

    @Autowired
    private IndexDataService indexDataService;
    @Autowired
    private IndexService indexService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("定时启动:" + DateUtil.now());
        List<Index> indexes = indexService.fresh();
        for (Index index : indexes) {
            indexDataService.fresh(index.getCode());
        }
        System.out.println("定时结束:" + DateUtil.now());
    }
}
