/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.NamingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.entities.CongestionReport;

/**
 *
 * @author liban
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringTestConfig.class})
public class CongestionTest {

    public CongestionTest() {
    }

    private void clearDatabase() {
        new Db().sql(
                "DELETE FROM occupancy;"
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
    public void insert() {
        final CongestionReport o = new CongestionReport();
        o.detector_id = "A";
        o.freeFlowCount = 1;
        o.queuePresent = 0;
        o.moderateFlowCount = 2;
        o.slowFlowCount = 3;
        o.verySlowFlowCount = 4;
        o.staticFlowCount = 5;
        String u = "52597d86";
        o.inquiryStart = new Date(((long) Integer.valueOf(u, 16)) * 1000);
        o.insert();

        assertTrue((Boolean) (new Db().sql("SELECT * FROM occupancy").go(new DBHasResult() {
            @Override
            public Boolean done(ResultSet rs) throws SQLException {
                rs.next();
                assertEquals(rs.getString("detector_id"), o.detector_id);
                assertEquals(rs.getInt("free"), o.freeFlowCount);
                assertEquals(rs.getInt("moderate"), o.moderateFlowCount);
                assertEquals(rs.getInt("slow"), o.slowFlowCount);
                assertEquals(rs.getInt("very_slow"), o.verySlowFlowCount);
                assertEquals(rs.getInt("stationary"), o.staticFlowCount);
                assertEquals(rs.getInt("queue_present"), o.queuePresent);
                assertEquals(rs.getTimestamp("reported_timestamp"), new Timestamp(o.inquiryStart.getTime()));
                return true;
            }
        }).close().response()));
    }
}
