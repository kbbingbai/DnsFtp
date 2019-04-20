package com.hxht.dnsftp.myfinal;
import com.hxht.dnsftp.entity.Test;
import com.hxht.dnsftp.rowmapper.TestIdFileNameLenRowMapper;
import com.hxht.dnsftp.util.FtpUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现从服务器上读取文件，并存储到mysql数据库中 One
 */
//@Component
public class FtpToSql {

    @Value("${ftp.remoteDirectory}")
    public String remoteDirectory;

    @Value("${ftp.pullFailFilesDir}")
    public String pullFailFilesDir;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Autowired
    public FtpUtil ftpUtil;

    private FTPClient ftpClient;

    private static final Logger log = LoggerFactory.getLogger(FtpToSql.class);

    //每隔2分钟一次
    @Scheduled(cron = "0 0/2 * * * ?")
    public void ftpToSql() {
        log.info("FtpToSql  保存文件名到数据库，ftpToSql方法执行");
        List<Test> requiredTestData = requiredTestData();
        Map<String, List<Test>> map = pullData(remoteDirectory, new ArrayList<Test>(), new ArrayList<Test>(), requiredTestData);
        insertData(map);
    }

    /**
     * ftp远程文件夹名称
     *
     * @param remoteDirectory  远程文件夹的名称
     * @param pullAddList      需要向数据添加的数据
     * @param pullUpList       需要向数据库修改的数据（把downflag的状态标志位，改为0，表明这个文件已经稳定，可以进行拉取）
     * @param requiredTestData
     * @return
     */
    public Map<String, List<Test>> pullData(String remoteDirectory, List<Test> pullAddList, List<Test> pullUpList, List<Test> requiredTestData) {
        // 拉取的数据
        FTPFile[] allFile = null;
        try {
            if (this.remoteDirectory.equals(remoteDirectory)) {
                ftpClient = ftpUtil.getFtpClient();
            }
            allFile = ftpClient.listFiles(remoteDirectory);
        } catch (IOException e) {
            log.error("FtpToSql发生异常，pullData方法--listFiles()方法错误，异常e：{}", e);
        }

        if (allFile != null) {
            for (FTPFile ftpFile : allFile) {
                if (ftpFile.isFile()) {// 判断是一个文件
                    try {
                        String temp = new String(remoteDirectory.getBytes("ISO-8859-1"), "UTF-8") + "/"
                                + new String(ftpFile.getName().getBytes("ISO-8859-1"), "UTF-8");

                        temp = temp.substring(1, temp.length());
                        Test isExist = isExist(requiredTestData, temp);//该文件存在于数据库就返回这个对象，否则返回null
                        if (isExist == null) {//说明这个文件还没有保存到数据库
                            pullAddList.add(new Test(temp, ftpFile.getSize()));
                        } else {//说明这个文件已经保存到数据库当中，看这个文件的大小是否有变化，如果没有变化，则认为这个文件已经稳定，可以设置downflag=0,如果文件的大小有变化则把文件最新的大小保存到数据库
                            if (isSizeCha(ftpFile, isExist.getFilelen())) {//该时刻服务器上的文件的大小与数据库一样，说明这个文件稳定
                                if (Test.pullInitState.equals(isExist.getDownflag())) {
                                    isExist.setDownflag(Test.pullEnable);//只有downflag=-3的才可以变成downflag=0，其它的状态不可以
                                }
                            } else {//该文件还没有保持稳定
                                isExist.setFilelen(ftpFile.getSize());
                            }
                            pullUpList.add(isExist);
                        }
                    } catch (UnsupportedEncodingException e) {
                        log.error("FtpToSql发生异常，pullData方法--UnsupportedEncodingException，异常e：{}", e);
                    }
                } else {// 它是一个文件夹
                    if (!pullFailFilesDir.equals(ftpFile.getName())) {
                        String remotePath = remoteDirectory + "/" + ftpFile.getName();
                        pullData(remotePath, pullAddList, pullUpList, requiredTestData);
                    }
                }
            }
        }

        if (this.remoteDirectory.equals(remoteDirectory)) {// 完成了一次任务，就让它存储数据库
            Map<String, List<Test>> map = new HashMap<String, List<Test>>();
            map.put("pullAddList", pullAddList);
            map.put("pullUpList", pullUpList);
            try {
                if (ftpClient != null) {
                    ftpClient.logout();
                }
            } catch (IOException e) {
                log.error("FtpToSql发生异常，关闭ftpClient失败：{}", e);
            }

            return map;
        } else {
            return null;
        }
    }

    // 向数据库中插入数据
    public void insertData(Map<String, List<Test>> map) {
        List<Test> pullAddList = map.get("pullAddList");//向数据库要新添加的数据
        List<Test> pullUpList = map.get("pullUpList");//向数据库要修改的数据
        if (pullAddList.size() > 0) {
            //String inserSql = "insert into test(filename,filelen) select ?,? from dual where not exists(select 1 from test where (downflag = ? or downflag=?) and filename=?)";
            String inserSql = "insert into test(filename,filelen) values(?,?)";
            jdbcTemplate.batchUpdate(inserSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setString(1, pullAddList.get(i).getFilename());
                    ps.setLong(2, pullAddList.get(i).getFilelen());
                }

                @Override
                public int getBatchSize() {
                    return pullAddList.size();
                }
            });
        }

        if (pullUpList.size() > 0) {
            String updateSql = "update test set filelen=?,downflag=? where id=?";
            jdbcTemplate.batchUpdate(updateSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, pullUpList.get(i).getFilelen());
                    ps.setString(2, pullUpList.get(i).getDownflag());
                    ps.setString(3, pullUpList.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return pullUpList.size();
                }
            });
        }
    }

    //得到数据库的数据
    public List<Test> requiredTestData() {
        String sql = "select id,filename,filelen,downflag from test where deleteflag!=?";
        List<Test> list = jdbcTemplate.query(sql,new TestIdFileNameLenRowMapper(), Test.deleteflagSucc);
        return list;
    }

    /**
     * 判断该文件是否已经存在于数据库中，如果存在数据库就返回这个对象，如果不存在就返回null
     */
    public Test isExist(List<Test> test, String filename) {
        Test flag = null;
        for (Test temp : test) {
            if (temp.getFilename().equals(filename)) {
                flag = temp;
            }
        }
        return flag;
    }

    /**
     * 查看文件的大小是否有变化
     * @param ftpFile 该时刻服务器上的文件的状态
     * @param size    原文件的大小
     * @return
     */
    public boolean isSizeCha(FTPFile ftpFile, long size) {
        return ftpFile.getSize() == size ? true : false;
    }
}
