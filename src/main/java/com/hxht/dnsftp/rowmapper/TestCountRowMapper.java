package com.hxht.dnsftp.rowmapper;

import com.hxht.dnsftp.entity.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * you yong
 */
public class TestCountRowMapper implements RowMapper<Test> {
    public Test mapRow(ResultSet resultSet, int i) throws SQLException {
        String count = resultSet.getString("count(*)");
        Test test = new Test();
        return test;
    }
}
