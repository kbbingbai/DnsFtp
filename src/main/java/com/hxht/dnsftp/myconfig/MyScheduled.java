package com.hxht.dnsftp.myconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认情况下poolSize=1,所以要设置poolsize,如果不设置这个poolsize，那么所有的定时任务都是一个线程完成的，
 * 这样就会一个任务完不成，别的任务就会等着（不执行）
 */
@Configuration
public class MyScheduled {
    @Bean(name = "myScheduledTask")
    public MyScheduledTask taskScheduler() {
        MyScheduledTask taskScheduler = new MyScheduledTask();
        taskScheduler.setPoolSize(6);
        return taskScheduler;
    }
}
