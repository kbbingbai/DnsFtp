package com.hxht.dnsftp.rowmapper;
import com.hxht.dnsftp.entity.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * you yong
 */
public class TestRowMapper implements RowMapper<Test> {
    public Test mapRow(ResultSet resultSet, int i) throws SQLException {
        String id = resultSet.getString("id");
        String filename = resultSet.getString("filename");
        String downflag = resultSet.getString("downflag");
        Date createtime = resultSet.getDate("createtime");
        String deleteflag = resultSet.getString("deleteflag");
        Test test = new Test();
        test.setId(id);
        test.setFilename(filename);
        test.setDownflag(downflag);
        test.setCreatetime(createtime);
        test.setDeleteflag(deleteflag);
        return test;
    }
}
