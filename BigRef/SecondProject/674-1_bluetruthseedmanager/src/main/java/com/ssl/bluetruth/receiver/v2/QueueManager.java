/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2;

import fj.F;
import fj.F2;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import static fj.data.Array.array;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import org.apache.log4j.Logger;
import java.sql.Connection;
import javax.naming.NamingException;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 *
 * @author liban, svenkataramanappa
 */
public class QueueManager {

    private static Logger logger = Logger.getLogger(QueueManager.class);
    
    private static class Command {
        
        public String name;
        public String argument;
        public Date time;
        private String table = "command_queue";

        public void table(String table) {
            this.table = table;
        }

        public static Command fromString(String cmd) {
            Command c = new Command();
            c.time = new Date();
            String[] parts = cmd.split(":");
            c.name = parts[0];
            if (parts.length == 2) {
                c.argument = parts[1];
            }
            return c;
        }

        public static void truncate(String outstation_id) {
            Connection connection = null;
            PreparedStatement stmt = null;
            try {
                //create/obtain DataSource
                DatabaseManager dm = DatabaseManager.getInstance();
                connection = dm.getDatasource().getConnection();
                stmt = connection.prepareStatement("DELETE FROM command_queue WHERE detector_id = ?");
                stmt.setString(1, outstation_id);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                logger.info("SQL query could not execute", ex);
            } catch (NamingException ex) {
                logger.fatal("Naming Exception", ex);
            } catch (DatabaseManagerException ex) {
                logger.fatal("Could not get instance of DatabaseManager", ex);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                    }
                    stmt = null;
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
        
        public void save(String outstation_id) {
            Connection connection = null;
            PreparedStatement stmt = null;
            try {
                //create/obtain DataSource
                DatabaseManager dm = DatabaseManager.getInstance();
                connection = dm.getDatasource().getConnection();
                stmt = connection.prepareStatement(String.format("INSERT INTO %s(name,argument,time,detector_id) VALUES (?,?,?,?)", table));
                stmt.setString(1, name);
                stmt.setString(2, argument);
                stmt.setTimestamp(3, new Timestamp(time.getTime()));
                stmt.setString(4, outstation_id);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                logger.info("SQL query could not execute", ex);
            } catch (NamingException ex) {
                logger.fatal("Naming Exception", ex);
            } catch (DatabaseManagerException ex) {
                logger.fatal("Could not get instance of DatabaseManager", ex);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                    }
                    stmt = null;
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
    }
    
    private String outstation_id;
    private ArrayList<Command> queue;
    private static final String table = "command_queue";
    /* table: instation_queue(id,time,command,outstation_id,argument) */
    /* for tests */

    public QueueManager(String table_name, final String outstation_id) {
        this.outstation_id = outstation_id;
        update(table_name, outstation_id);
    }

    public QueueManager(final String outstation_id) {
        this.outstation_id = outstation_id;
        update(table, outstation_id);
    }

    private void update(String table_name, String outstation_id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(String.format("SELECT time,name,argument FROM %s "
                                                + "WHERE detector_id = ? ORDER BY time ASC", table_name));
            stmt.setString(1, outstation_id);
            rs = stmt.executeQuery();
            ArrayList<Command> tmp = new ArrayList<>();
            while (rs.next()) {
                Command c = new Command();
                c.name = rs.getString("name");
                c.time = new Date(rs.getTimestamp("time").getTime());
                c.argument = rs.getString("argument");
                tmp.add(c);
            }
            this.queue = tmp;
        } catch (SQLException ex) {
            logger.info("SQL query could not execute", ex);
        } catch (NamingException ex) {
            logger.fatal("Naming Exception", ex);
        } catch (DatabaseManagerException ex) {
            logger.fatal("Could not get instance of DatabaseManager", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
                rs = null;
            }
        }
    }

    public void enqueue(String cmd) {
        Command c = Command.fromString(cmd);
        this.queue.add(c);
        c.save(outstation_id);
    }

    public String assemble() {

        /* map : [Command] -> [String], reduce : [String] -> String */
        if (queue.size() == 0) {
            return "";
        }

        String s = array(queue.toArray(new Command[queue.size()])).map(new F<Command, String>() {
            @Override
            public String f(Command c) {
                return c.name + (c.argument != null ? ":" + c.argument : "");
            }
        }).foldLeft(new F2<String, String, String>() {
            public String f(String rest, String me) {
                return rest + "," + me;
            }
        }, "").substring(1);
        
        logger.info("Response to " + outstation_id + ": " + s);
        return s;
    }
    public String assembleAndFlush() {
        String s = assemble();
        flush();
        return s;
    }

    public void flush() {
        Command.truncate(outstation_id);
    }
}
