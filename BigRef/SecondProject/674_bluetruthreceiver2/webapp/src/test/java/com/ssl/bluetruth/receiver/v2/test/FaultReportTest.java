/*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.7
 *
 * Product: 674 - BlueTruthReceiver2
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.entities.Fault;
import com.ssl.bluetruth.receiver.v2.entities.FaultReport;
import com.ssl.bluetruth.receiver.v2.misc.RequestParserFactory;

/**
 *
 * @author liban
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringTestConfig.class})
public class FaultReportTest {

    @Autowired
    private RequestParserFactory requestParserFactory;
    
    public FaultReportTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws NamingException {
        DbCon.init();
    }

    @AfterClass
    public static void tearDownClass() {
    }
    Fault f1, f2;

    private void clearDatabase() {
        
        new Db().sql(
          "DELETE FROM fault_report;"
        + "DELETE FROM public.fault_message;"
        + "DELETE FROM public.occupancy;"
        + "DELETE FROM public.device_detection;"
        + "DELETE FROM public.detector;").go().close();
    }

    private void setUpDetectors() {
        new Db().sql("INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A');").go().close();
    }


    @Before
    public void setUp() {
        clearDatabase();
        setUpDetectors();
        f1 = new Fault();
        f1.fn = 100;
        f1.status = 1;
        f1.time = new Date(1383825193000L);

        f2 = new Fault();
        f2.fn = 200;
        f2.status = 0;
        f2.time = new Date(1383825199000L);
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void deviceDeserialiser() {
        assertEquals(f1.fn + ":" + Integer.toHexString(
                (int) (f1.time.getTime() / 1000)) + ":" + f1.status, f1.toString());
        assertEquals(Fault.deserialise(f1.toString()).toString(), f1.toString());
    }

    @Test
    public void batchDeserialiser() {
        FaultReport fr = requestParserFactory.getRequestParser(FaultReport.class)
                .data("A,ffff," + f1.toString() + "," + f2.toString() + ",1234")
                .parse();


        assertEquals(f1.toString(), fr.faults.get(0).toString());
        assertEquals(f2.toString(), fr.faults.get(1).toString());
    }

    @Test
    public void insertDevices() {
        FaultReport fr = requestParserFactory.getRequestParser(FaultReport.class)
                .data("A,ffff," + f1.toString() + "," + f2.toString() + ",1234")
                .parse();
        fr.insertFaults(1);

        Boolean status = (Boolean) (new Db().sql("SELECT code,"
                + "status,time FROM fault_message")
                .go(new DBHasResult<Boolean>() {
            @Override
            public Boolean done(ResultSet rs) throws SQLException {
                // Assuming the same order
                rs.next();
                assertEquals(f1.fn, rs.getInt("code"));
                assertEquals(f1.status, rs.getInt("status"));
                assertEquals(f1.time, rs.getTimestamp("time"));

                rs.next();
                assertEquals(f2.fn, rs.getInt("code"));
                assertEquals(f2.status, rs.getInt("status"));
                assertEquals(f2.time, rs.getTimestamp("time"));
                return true;
            }
        }).close().response());
        assertTrue("No exceptions", status);
    }

    @Test
    public void insertReport() {
        FaultReport fr = requestParserFactory.getRequestParser(FaultReport.class)
                .data("A,ffff," + f1.toString() + "," + f2.toString() + ",1234")
                .parse();

        final int report_id = fr.insertReport();
        new Db().sql("SELECT report_id,time,detector_id FROM fault_report").go(new DBHasResult() {
            @Override
            public Object done(ResultSet rs) throws SQLException {
                rs.next();
                assertEquals(report_id, rs.getInt("report_id"));
                assertEquals(new Timestamp(Integer.valueOf("ffff", 16) * 1000), rs.getTimestamp("time"));
                return null;
            }
        }).close();
    }
}