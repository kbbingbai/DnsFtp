package com.hxht.dnsftp.myfinal;

import com.hxht.dnsftp.entity.Test;
import com.hxht.dnsftp.myconfig.MyScheduledTask;
import com.hxht.dnsftp.rowmapper.TestIdFileNameRowMapper;
import com.hxht.dnsftp.rowmapper.TestPullTimeoutRowMapper;
import com.hxht.dnsftp.util.DateUtils;
import com.hxht.dnsftp.util.HdfsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 当PullFile拉取文件时，如果文件长时间拉取不下来，就放弃本次任务。
 * six
 */
@Component
public class PullTimeoutHandler {
    @Value("${ftp.pullfile.timeout}")
    public String timeout;

    @Value("${hdfs.hdfsUrl}")
    private String hdfsUrl;

    @Value("${hdfs.hdfsDir}")
    private String hdfsDir;

    @Value("${ftp.pullip}")
    private String pullip;
    @Autowired
    private MyScheduledTask myScheduledTask;

    private static final Logger log = LoggerFactory.getLogger(PullTimeoutHandler.class);

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void pullTimeoutHandler() {
        log.info("PullTimeoutHandler pullTimeoutHandler方法执行");

        Test data = getData();
        if (data != null) {
            String currday = DateUtils.getCurrDay(data.getStartpulltime());
            //删除hadoop的文件
            boolean flag = HdfsClient.deleteFile(hdfsUrl, hdfsDir, currday, data.getFilename());
            //修改该条数据的状态downflag pulltimeoutcount的状态
            chaStatus(data);
            //取消本次任务
            myScheduledTask.myCancelTask(PullFile.class);
        }
    }


    public Test getData() {
        Test test = null;
        try {
            String querySql = "select id,pulltimeoutcount,filename,downflag from (select floor((UNIX_TIMESTAMP(now()) - UNIX_TIMESTAMP(startpulltime))/60) difminute," +
                    "downflag,id,pulltimeoutcount,filename,startpulltime from test where downflag=? and pullip=?) temp where difminute>? order by id desc limit 1";
            test = jdbcTemplate.queryForObject(querySql, new TestPullTimeoutRowMapper(), Test.pullingFile, pullip, timeout);
        } catch (Exception e) {
            log.error("PullTimeoutHandler 方法名获取拉取超时文件，即没有超时文件或者查询失败");
        }
        return test;
    }

    /***
     * 把这些数据的downflag = -2 的状态改变成-3
     */
    public int chaStatus(Test test) {
        String sql = "update test set downflag = ?,pulltimeoutcount=? where id =?";
        String downflag = "0";
        String pulltimeoutcount = "0";
        if (test.getPulltimeoutcount().equals("0")) {
            pulltimeoutcount = "1";
        } else {
            pulltimeoutcount = "2";
            downflag = "3";
        }
        return jdbcTemplate.update(sql, downflag, pulltimeoutcount, test.getId());
    }

}
