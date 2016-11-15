/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;


/**
 *
 * @author liban
 */
public class Db {

    DatabaseManager dm;
    Connection connection = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Object response_;
    public Db() {
        try {
            dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
        } catch (DatabaseManagerException | NamingException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Db sql(String query) {
        try {
            this.ps = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Db set(DBSetsParams ics) {
        try {
            ics.set(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Db go() {
        try {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Db go(DBHasResult hasres) {
        try {
            rs = ps.executeQuery();
            response_ = hasres.done(rs);  
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Db close() {
        cleanQueryObjects(rs, ps, connection);
        return this;
    }
    
    public Object response() {
        return response_;
    }
    
    private static void cleanQueryObjects(ResultSet rs, PreparedStatement ps, Connection connection) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
            rs = null;
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
            }
            ps = null;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
            connection = null;
        }
    }
}
