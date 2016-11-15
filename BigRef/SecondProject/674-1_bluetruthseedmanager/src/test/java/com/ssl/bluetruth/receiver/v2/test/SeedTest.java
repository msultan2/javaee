package com.ssl.bluetruth.receiver.v2.test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.ssl.bluetruth.db.Db;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.ssl.bluetruth.receiver.v2.GenericParsable;
import com.ssl.bluetruth.receiver.v2.seed.Seed;
import com.ssl.bluetruth.db.DBHasResult;
import org.junit.Ignore;

/**
 *
 * @author liban
 */
@Ignore
public class SeedTest {

    public SeedTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        DbCon.init();
    }

    @AfterClass
    public static void tearDownClass() {
    }
    final int seed = 1234;

    private void clearDatabase() {
        new Db().sql(
                "DELETE FROM detector_seed;"
                + "DELETE FROM detector;").go().close();
    }

    private void setUpDetectors() {
        new Db().sql("INSERT INTO detector (detector_id, detector_name) VALUES ('A', 'Detector A')").go().close();
    }

    private void setUpSeed() {
        new Db().sql("INSERT INTO detector_seed (id,detector_id, seed) VALUES (1,'A', 1337)").go()
                .sql("UPDATE detector_confirmed_config seed_id = 1 WHERE detector_id = 'A'").go().close();

    }

    @Before
    public void setUp() {
        clearDatabase();
        setUpDetectors();
        //  setUpSeed();
    }

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void insertSeed() {
        Seed s = new Seed();
        s.setDetectorId("A");
        try {
            s.setSeed(1234);
        } catch (Exception e) {
            fail("Should be able to set seed value at this point");
        }
        s.save();
        int c = (Integer) (new Db().sql("SELECT COUNT(*) FROM detector_seed").go(new DBHasResult() {
            @Override
            public Integer done(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt(1);
            }
        }).close().response());
        assertEquals(1, c);
    }

    @Test
    public void loadSeed() {
        new Db().sql("INSERT INTO detector_seed(id,detector_id,seed,last) VALUES (1000,'A',1,1);").go().close();

        Seed s = new Seed().setId(1000);
        s.load();
        assertEquals(1000, s.getId());
        assertEquals(1, s.getSeed());
        assertEquals(1, s.getLast());
    }

    private static class TestParsable extends GenericParsable {

        public static int validatedCount = 0;

        public TestParsable(int rnd) {
            this.rnd = rnd;
        }

        public void onValidated() {
            validatedCount++;
        }
    }

    @Test
    public void sequence() {
        int[] seedOne = {48271, 22498, 3902, 2979, 13326, 23506, 33358, 4499, 50461, 26420};
        try {
            Seed s = new Seed().setSeed(1);
            TestParsable.validatedCount = 0;
            for (int v : seedOne) {
                TestParsable tp = new TestParsable(v);
                s.validate(tp);
            }
        } catch (Exception e) {
            fail();
        }
        assertEquals(seedOne.length, TestParsable.validatedCount);
    }

    @Test
    public void sequenceSpoofed() {
        int[] seedOne = {48271, 22498, 3902, 10000, 2979, 13326, 23506, 33358, 4499, 50461, 26420};
        try {
            Seed s = new Seed().setSeed(1);
            TestParsable.validatedCount = 0;
            for (int v : seedOne) {
                TestParsable tp = new TestParsable(v);
                s.validate(tp);
            }
            assertEquals(0, s.getMQSize());
        } catch (Exception e) {
            fail();
        }
        assertEquals(seedOne.length - 1, TestParsable.validatedCount);
    }

    @Test
    public void sequenceBrokenOrder() {
        int[] seedOne = {48271, 22498, 2979, 3902, 13326, 23506, 33358, 4499, 50461, 26420};
        try {
            Seed s = new Seed().setSeed(1);
            TestParsable.validatedCount = 0;
            for (int v : seedOne) {
                TestParsable tp = new TestParsable(v);
                s.validate(tp);
                if (v == 2979) { // at this point it should be added to mq
                    assertEquals(1, s.getMQSize());
                } else if (v == 3902) { // verify both clear mq
                    assertEquals(0, s.getMQSize());
                }
            }
        } catch (Exception e) {
            fail();
        }
        assertEquals(seedOne.length, TestParsable.validatedCount);
    }

    @Test
    public void sendSeedXmlToOutstation() {
        try {
            Seed s = new Seed().setId(1000).setSeed(1);
            File f = new File(s.getSeedLocation());
            if (f.exists()) {
                f.delete();
            }
            s.sendToOutstation();
            assertTrue(f.exists());
            if (f.exists()) {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(f);
                assertEquals(1,doc.getElementsByTagName("Seed").getLength());
                Node nodeId = doc.getElementsByTagName("Seed").item(0).getFirstChild();
                assertTrue(nodeId.getNodeName().equals("ID"));
                assertTrue(nodeId.getChildNodes().item(0).getNodeValue().equals("1000"));
                
                Node nodeValue = doc.getElementsByTagName("Seed").item(0).getLastChild();
                assertTrue(nodeValue.getNodeName().equals("Value"));
                assertTrue(nodeValue.getChildNodes().item(0).getNodeValue().equals("1"));
            }
        } catch (Exception e) {
            fail();
        }
    }
}