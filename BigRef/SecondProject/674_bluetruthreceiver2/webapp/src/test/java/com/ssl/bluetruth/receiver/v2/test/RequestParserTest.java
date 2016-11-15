/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.naming.NamingException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ssl.bluetruth.receiver.v2.misc.RequestParserFactory;

/**
 *
 * @author Liban Abdulkadir
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringTestConfig.class})
public class RequestParserTest {

    @Autowired
    private RequestParserFactory requestParserFactory;
    
    public RequestParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws NamingException {

        DbCon.init();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void basic() {
    	Testing testing = getTestData();
        assertEquals(1234, testing.id);
    }

    @Test
    public void dateFromHex() {
    	Testing testing = getTestData();
    	assertEquals(new Date(1357020000000L), testing.datetime);
    }

    @Test
    public void intFromHex() {
    	Testing testing = getTestData();
    	assertEquals(1357020000, testing.hex_int);
    }

	private Testing getTestData() {
		return requestParserFactory.getRequestParser(Testing.class)
                .data("1234,50e27b60,50e27b60,1234").parse();
	}

    @Test
    public void namedVariables() {
        // 0x50e27b60 = 1357020000 = (2013, 1, 1, 6, 0)
        Testing r = requestParserFactory.getRequestParser(Testing.class).data("1234,50e27b60,50e27b60,foo=bar,bar=foo,1234").parse();
        assertEquals(1234, r.id);
        assertEquals(new Date(1357020000000L), r.datetime);

        assertTrue(r.varHas("foo"));
        assertEquals("bar", r.varGet("foo"));

        assertTrue(r.varHas("bar"));
        assertEquals("foo", r.varGet("bar"));
    }
    
    @Test
    public void realExample() {
        Testing r = requestParserFactory.getRequestParser(Testing.class)
                .data("1235,52f23532,52f23534,boot=0,fv=1.0,sn=1235,cv=337549508e6e18413d9c637f1fb44e4c,"
                + "sl=-255,of=0,seed=0,ssh=closed,up=0,0").parse();
        
        assertTrue(r.varHas("fv"));
    }

    @After
    public void tearDown() {
    }
}