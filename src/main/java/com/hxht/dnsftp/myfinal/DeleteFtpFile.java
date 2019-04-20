package com.hxht.dnsftp.myfinal;
import com.hxht.dnsftp.entity.Test;
import com.hxht.dnsftp.rowmapper.TestIdFileNameRowMapper;
import com.hxht.dnsftp.util.FtpUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 删除别人服务器上的文件并且这个文件已经拉取成功，二天执行一回 four
 */
//@Component
public class DeleteFtpFile {

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Autowired
    public FtpUtil ftpUtil;

    @Value("${ftp.remoteDirectory}")
    public String remoteDirectory;

    private static final Logger log = LoggerFactory.getLogger(DeleteFtpFile.class);

    /**
     * 每隔两天执行一回  @Scheduled(cron = "0 0 4 0/2 * ?")
     */
    @Scheduled(cron = "30 0/3 * * * ?")
    public void deleteFtpFile() {
        log.info("deleteFtpFile，deleteFtpFile方法执行");
        List<Test> data = getData();
        List<Test> succDelFile = new ArrayList<Test>();

        FTPClient ftpClient = null;
        if (data.size() > 0) {
            ftpClient = ftpUtil.getFtpClient();
        }

        try {
            for (Test temp : data) {
                String filename = temp.getFilename();
                String[] splitArr = filename.split(".");
                String name = filename.substring(0, 9);
                String numStr = String.valueOf(Integer.parseInt(filename.substring(9, filename.indexOf("."))) + 1);

                boolean flag = ftpClient.rename(new String(("/" + filename).getBytes("UTF-8"), "ISO-8859-1"), new String(("/" + name + numStr + ".csv").getBytes("UTF-8"), "ISO-8859-1"));
                if (flag) {
                    temp.setDeleteflag(Test.deleteflagSucc);
                    succDelFile.add(temp);
                }
            }
        } catch (IOException e) {
            log.error("DeleteFtpFile ftpClient.deleteFile发生异常deleRemoteFile方法，异常e：{}", e);
            e.printStackTrace();
        } finally {
            updateData(succDelFile);
            if (ftpClient != null) {
                try {
                    ftpClient.logout();
                } catch (IOException e) {
                    log.error("DeleteFtpFile ftpClient关闭失败，异常e：{}", e);
                }
            }
        }
    }

    /**
     * 查询符合删除条件的数据
     */
    public List<Test> getData() {
        String querySql = "select id,filename,downflag from test where downflag=?";// 查询成功拉取的数据
        List<Test> data = jdbcTemplate.query(querySql, new TestIdFileNameRowMapper(), Test.pullSucc);
        return data;
    }

    //修改数据
    public void updateData(List<Test> list) {
        if (list.size() > 0) {
            String updateSql = "update test set deleteflag=? where id=?";// 查询成功拉取的数据
            jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, list.get(i).getDeleteflag());
                    ps.setString(2, list.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });
        }
    }

}
