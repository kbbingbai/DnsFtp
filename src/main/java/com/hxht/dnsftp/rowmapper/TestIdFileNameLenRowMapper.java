package com.hxht.dnsftp.rowmapper;

import com.hxht.dnsftp.entity.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * you yong
 */
public class TestIdFileNameLenRowMapper implements RowMapper<Test> {
    public Test mapRow(ResultSet resultSet, int i) throws SQLException {
        Test test = new Test();
        String filename = resultSet.getString("filename");
        String id = resultSet.getString("id");
        long filelen = resultSet.getLong("filelen");
        String downflag = resultSet.getString("downflag");

        test.setId(id);
        test.setFilename(filename);
        test.setFilelen(filelen);
        test.setDownflag(downflag);

        return test;
    }
}
