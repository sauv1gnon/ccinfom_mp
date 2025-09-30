package com.ccinfoms17grp2.dao.impl;

import com.ccinfoms17grp2.dao.DaoException;
import com.ccinfoms17grp2.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

abstract class AbstractJdbcDao {

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    protected DaoException translateException(String message, SQLException ex) {
        return new DaoException(message, ex);
    }

    protected void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {
            }
        }
    }

    protected void closeQuietly(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
