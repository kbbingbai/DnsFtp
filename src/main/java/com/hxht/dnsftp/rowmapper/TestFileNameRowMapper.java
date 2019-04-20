package com.hxht.dnsftp.rowmapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * you yong
 */
public class TestFileNameRowMapper implements RowMapper<String> {
    public String mapRow(ResultSet resultSet, int i) throws SQLException {
        String filename = resultSet.getString("filename");
        return filename;
    }
}
