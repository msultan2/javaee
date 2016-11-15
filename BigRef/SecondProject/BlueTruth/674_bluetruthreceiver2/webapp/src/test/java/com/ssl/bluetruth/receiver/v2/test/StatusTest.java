/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.NamingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.entities.StatusReport;

/**
 *
 * @author liban
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringTestConfig.class})
public class StatusTest {

    @Autowired
    private ApplicationContext applicationContext;

    public StatusTest() {
    }

    private void clearDatabase() {
        new Db().sql(
                "DELETE FROM detector_status;"
                + "DELETE FROM detector;").go().close();
    }

    private void setUpDetectors() {
        new Db().sql("INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A');").go().close();
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
        clearDatabase();
        setUpDetectors();
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void receivedFirst() {
        final StatusReport sr = new StatusReport();
        String u = "52597d86";
        long ms = ((long) Integer.valueOf(u, 16)) * 1000;
        sr.timestamp = new Date(ms);
        sr.vars.put("fv", "a");
        sr.vars.put("sn", "b");
        sr.vars.put("cv", "c");
//        sr.vars.put("sl", "1");
        sr.vars.put("of", "2");
        sr.vars.put("ssh", "d");
        sr.vars.put("seed", "3");
        sr.vars.put("up", "4");
        sr.id = "A";
        sr.update();
        new Db().sql("SELECT * FROM detector_status").go(new DBHasResult() {
            @Override
            public Object done(ResultSet rs) throws SQLException {
                assertTrue(rs.next());
                assertTrue(sr.id.equals(rs.getString("detector_id")));
                assertTrue(sr.varGet("fv").equals(rs.getString("fv")));
                assertTrue(sr.varGet("sn").equals(rs.getString("sn")));
                assertTrue(sr.varGet("cv").equals(rs.getString("cv")));

//                assertTrue(sr.varGet("sl").equals(rs.getInt("sl") + ""));
                assertTrue(sr.varGet("of").equals(rs.getInt("of") + ""));

                assertTrue(sr.varGet("ssh").equals(rs.getString("ssh")));
                assertTrue(sr.varGet("seed").equals(rs.getInt("seed") + ""));
                assertTrue(sr.varGet("up").equals(rs.getInt("up") + ""));

                assertEquals(sr.timestamp.getTime(), rs.getTimestamp("timestamp").getTime());
                return null;
            }
        }).close();

        sr.vars.put("fv", "UPDATED!");

        sr.update();
        new Db().sql("SELECT * FROM detector_status").go(new DBHasResult() {
            @Override
            public Object done(ResultSet rs) throws SQLException {
                assertTrue(rs.next());
                assertTrue(sr.id.equals(rs.getString("detector_id")));
                assertTrue(sr.varGet("fv").equals(rs.getString("fv")));
                assertTrue(sr.varGet("sn").equals(rs.getString("sn")));
                assertTrue(sr.varGet("cv").equals(rs.getString("cv")));

//                assertTrue(sr.varGet("sl").equals(rs.getInt("sl") + ""));
                assertTrue(sr.varGet("of").equals(rs.getInt("of") + ""));

                assertTrue(sr.varGet("ssh").equals(rs.getString("ssh")));
                assertTrue(sr.varGet("seed").equals(rs.getInt("seed") + ""));
                assertTrue(sr.varGet("up").equals(rs.getInt("up") + ""));

                assertEquals(sr.timestamp.getTime(), rs.getTimestamp("timestamp").getTime());
                return null;
            }
        }).close();
    }
}