package com.hxht.dnsftp.myfinal;

import com.hxht.dnsftp.entity.Test;
import com.hxht.dnsftp.rowmapper.TestRowMapper;
import com.hxht.dnsftp.util.DateUtils;
import com.hxht.dnsftp.util.HdfsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 系统坏了之后 进行系统重新启动时的程序 file
 * 只需处理downflag=-2，其它的 -3(钢存入的数据)  0（可拉取的状态），1（拉取一次失败的程序）2（拉取两次次失败的程序）1（拉取三次失败的程序）不需要处理
 * file
 * @Order(value=1)  five
 */

//@Component
public class BootResume implements CommandLineRunner {

    @Value("${hdfs.hdfsUrl}")
    private String hdfsUrl;
    @Value("${hdfs.hdfsDir}")
    private String hdfsDir;
    @Autowired
    public JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(BootResume.class);

    @Override
    public void run(String... args) throws Exception {
        log.info("BootResume run方法执行");
        //查询那些downflag=-2的数据
        List<Test> data = getData();
        for (int i = 0; i < data.size(); i++) {
            Test temp = data.get(i);
            String currday = DateUtils.getCurrDay(temp.getCreatetime());
            String yesterday = DateUtils.getYesterday(temp.getCreatetime());
            boolean flag = HdfsClient.deleteFile(hdfsUrl, hdfsDir, currday, temp.getFilename());
            if (!flag) {
                HdfsClient.deleteFile(hdfsUrl, hdfsDir, yesterday, temp.getFilename());
            }
        }
        if (data.size() != 0) {
            int num = chaStatus(data);
            log.info("改变的条数是" + num);
        }
    }

    /**
     * 查询符合条件的数据 downflag = -2
     */
    public List<Test> getData() {
        String sql = "select id,filename,downflag,createtime,deleteflag from test where downflag=?";
        List<Test> list = jdbcTemplate.query(sql, new TestRowMapper(), Test.pullingFile);
        return list;
    }


    /***
     * 把这些数据的downflag = -2 的状态改变成-3
     */
    public int chaStatus(List<Test> list) {
        String sql = "update test set downflag = ?,createtime=? where id in(?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, Test.pullInitState);
                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                ps.setString(3, list.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        return list.size();
    }

}
