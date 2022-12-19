package com.my.conferences.dao.implementation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private JdbcTemplate() {}

    public static <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... parameters) throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            insertParameters(stmt, parameters);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
            }
        }

        return results;
    }

    public static PreparedStatement update(Connection connection, String sql, Object... parameters) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        insertParameters(stmt, parameters);
        stmt.executeUpdate();
        return stmt;
    }

    private static void insertParameters(PreparedStatement stmt, Object... parameters) throws SQLException {
        int k = 0;
        for (Object parameter : parameters) {
            if (parameter.getClass() == Integer.class) {
                stmt.setInt(++k, (Integer) parameter);
            } else if (parameter.getClass() == String.class) {
                stmt.setString(++k, (String) parameter);
            } else if (parameter.getClass() == Boolean.class) {
                stmt.setBoolean(++k, (Boolean) parameter);
            } else if (parameter.getClass() == Timestamp.class) {
                stmt.setTimestamp(++k, (Timestamp) parameter);
            } else {
                throw new SQLException("Unknown parameter type");
            }
        }
    }
}
