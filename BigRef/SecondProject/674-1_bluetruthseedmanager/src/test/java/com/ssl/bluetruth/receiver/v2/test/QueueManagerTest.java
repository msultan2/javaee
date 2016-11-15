package com.ssl.bluetruth.receiver.v2.test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.naming.NamingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.ssl.bluetruth.receiver.v2.QueueManager;
import com.ssl.bluetruth.db.Db;
import static org.junit.Assert.*;
import com.ssl.bluetruth.db.DBSetsParams;
import org.junit.Ignore;

/**
 *
 * @author liban
 */
@Ignore
public class QueueManagerTest {

    private static final String table = "command_queue";

    public QueueManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws NamingException {
        DbCon.init();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        clearDatabase();
        setUpDetectors();
    }

    private void clearDatabase() {
        new Db().sql(
                "DELETE FROM command_queue;"
                + "DELETE FROM detector;").go().close();
    }

    private void setUpDetectors() {
        new Db().sql("INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A');").go().close();
    }

    @Test
    public void assembleOne() {

        new Db().sql("INSERT INTO command_queue (name,argument,detector_id,time) VALUES (?,?,?,?),(?,?,?,?)")
                .set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                Calendar c = Calendar.getInstance();
                c.set(2012, 5, 2, 13, 0, 0);

                ps.setString(1, "cmdOne");
                ps.setString(2, "1234");
                ps.setString(3, "A");
                ps.setTimestamp(4, new Timestamp(c.getTime().getTime()));
                c.add(Calendar.SECOND, 5);

                ps.setString(5, "cmdTwo");
                ps.setObject(6, null);
                ps.setString(7, "A");
                ps.setTimestamp(8, new Timestamp(c.getTime().getTime()));
            }
        }).go().close();
        QueueManager qm = new QueueManager("A");
        assertEquals("cmdOne:1234&cmdTwo", qm.assemble());
    }

    @Test
    public void assembleTwo() {
        new Db().sql("INSERT INTO command_queue (name,argument,detector_id,time) VALUES (?,?,?,?),(?,?,?,?),(?,?,?,?)")
                .set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                Calendar c = Calendar.getInstance();
                c.set(2012, 5, 2, 13, 0, 0);

                ps.setString(1, "cmdOne");
                ps.setString(2, "1234");
                ps.setString(3, "A");
                ps.setTimestamp(4, new Timestamp(c.getTime().getTime()));
                c.add(Calendar.SECOND, 5);

                ps.setString(5, "cmdTwo");
                ps.setObject(6, null);
                ps.setString(7, "A");
                ps.setTimestamp(8, new Timestamp(c.getTime().getTime()));

                c.add(Calendar.SECOND, 5);

                ps.setString(9, "cmdThree");
                ps.setObject(10, null);
                ps.setString(11, "A");
                ps.setTimestamp(12, new Timestamp(c.getTime().getTime()));
            }
        }).go().close();

        QueueManager qm = new QueueManager("A");
        assertEquals("cmdOne:1234&cmdTwo&cmdThree", qm.assemble());
    }

    @Test
    public void assembleEmpty() {
        QueueManager qm = new QueueManager("A");
        assertEquals("", qm.assemble());
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}