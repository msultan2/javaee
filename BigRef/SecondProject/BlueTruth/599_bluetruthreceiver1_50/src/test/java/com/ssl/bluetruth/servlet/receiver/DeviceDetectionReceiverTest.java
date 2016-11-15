/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.servlet.receiver;

import com.ssl.bluetruth.servlet.receiver.DeviceDetectionReceiver;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nthompson
 */
public class DeviceDetectionReceiverTest {
    
    public DeviceDetectionReceiverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of insertDeviceDetections method, of class DeviceDetectionReceiver.
     */
    @Test
    public void testParseDateString() throws Exception {
        
        String TIMEZONE_LONDON = "Europe/London";
        String DATE_FORMAT_ISO_8601 = "yyyy-MM-dd HH:mm:ss";
        String DATE_FORMAT_US = "yyyy-dd-MM HH:mm:ss";
        
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_ISO_8601);
        Long controlTime = dateFormat.parse("2012-08-25 12:13:14").getTime();
        
        Long parseDateString1 = DeviceDetectionReceiver.convertTimestampToMillis("2012-08-25 12:13:14", DATE_FORMAT_US, TIMEZONE_LONDON);
        assertNotSame(parseDateString1, controlTime);
        
        Long parseDateString2 = DeviceDetectionReceiver.convertTimestampToMillis("2012-25-8 12:13:14", DATE_FORMAT_US, TIMEZONE_LONDON);
        assertEquals(parseDateString2, controlTime);
        
        Long parseDateString3 = DeviceDetectionReceiver.convertTimestampToMillis("2012-08-25 12:13:14", DATE_FORMAT_ISO_8601, TIMEZONE_LONDON);
        assertEquals(parseDateString3, controlTime);
    }
}
