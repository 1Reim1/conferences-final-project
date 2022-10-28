package com.my.conferences.util;

import com.my.conferences.service.DBException;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Util class for comfortable work with connections
 */
public class ConnectionUtil {

    private final static Logger logger = Logger.getLogger(ConnectionUtil.class);
    private static DataSource dataSource;

    private ConnectionUtil() {}

    static {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/ConferencesDB");
        } catch (NamingException e) {
            logger.error("Data source was not created", e);
        }
    }

    /**
     * Returns a connection from dataSource
     *
     * @return Connection
     */
    public static Connection getConnection() throws DBException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DBException("The connection to the database is lost", e);
        }
    }

    public static Connection getConnectionForTransaction() throws DBException {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return connection;
        }   catch (SQLException e) {
            throw new DBException("The connection to the database is lost", e);
        }
    }

    /**
     * Closes a connection
     */
    public static void closeConnection(Connection connection) throws DBException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DBException("Connection was not closed", e);
        }
    }

    public static void rollbackConnection(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.error("SQLException in connection.rollback()", e);
        }
    }
}
