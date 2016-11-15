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
 * Created By: Liban Abdulkadir
 *
 * Product: 674 - BlueTruthReceiver2
 */
package com.ssl.bluetruth.receiver.v2.seed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import com.ssl.bluetruth.receiver.v2.GenericParsable;
import com.ssl.bluetruth.receiver.v2.QueueManager;
import java.sql.Connection;
import javax.naming.NamingException;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 *
 * @author Liban Abdulkadir, svenkataramanappa, Estelle Edwards
 */
public class Seed {

    private static final Logger logger = Logger.getLogger(Seed.class);
    private final String seed_location = "/var/cache/bt/seed.xml";
    private final String INSTATION_PRIVATE_KEY = "/home/bt/.ssh/to_outstation_key";

    public String getSeedLocation() {
        return seed_location;
    }
    private int id = -1;
    private long value = 0, last = -1;
    private String detector_id;
    private static LCG lcg = new LCG(48271, 2147483647);
    private ArrayList<GenericParsable> messageQueue = new ArrayList<>();
    private int port;
    private int retries;

    public Seed() {
        last = value = (int) (Math.random() * Integer.MAX_VALUE / 2);
    }

    public int getId() {
        return id;
    }

    public Seed setId(int id) {
        this.id = id;
        return this;
    }

    public Seed load() {
        if (id != -1) {
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                //create/obtain DataSource
                DatabaseManager dm = DatabaseManager.getInstance();
                connection = dm.getDatasource().getConnection();
                stmt = connection.prepareStatement("SELECT seed, last, retries FROM detector_seed WHERE id = ?");
                stmt.setInt(1, id);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    value = rs.getInt(1);
                    last = rs.getInt(2);
                    retries = rs.getInt(3);
                    logger.info(detector_id + ": Seed value: " + value + " and last value: " + last + " No of retries: " + retries);
                }
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
            inserted = true;
        }
        return this;
    }

    public long getLast() {
        return last;
    }

    public long getSeed() {
        return value;
    }

    public Seed setSeed(int value) throws Exception {
        if (inserted) {
            throw new Exception("Cannot change an existing seed. Create a new seed instead.");
        }
        this.value = this.last = value;
        return this;
    }

    public String getDetectorId() {
        return detector_id;
    }

    public Seed setDetectorId(String detector_id) {
        this.detector_id = detector_id;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Seed setPort(int port) {
        this.port = port;
        return this;
    }

    private void saveToXMLFile(String location) {
        File f = new File(location);
        if (f.exists()) {
            f.delete();
        }
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("Seed");

        root.addElement("ID").addText(id + "");
        root.addElement("Value").addText(value + "");

        try {
            XMLWriter writer = new XMLWriter(new FileWriter(location));
            writer.write(doc);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendToOutstation() {
        // Write seed.xml and send it to the outstation
        String tmp_seed = "/tmp/seed_" + detector_id + ".xml";
        saveToXMLFile(tmp_seed);
        SSH ssh = new SSH()
                .user("remote")
                .host("localhost")
                .port(port)
                .instationPrivateKey(INSTATION_PRIVATE_KEY);
        return ssh.sendFile(tmp_seed, seed_location);
    }

    private boolean inserted = false;

    public int getMQSize() {
        return messageQueue.size();
    }

    public boolean validate(GenericParsable rp) {
        boolean isValid = true;

        if (value == 0 || !getSignReports()) {
            rp.onValidated();

        } else {
            load();     // Keep Seed up to date
            long received = rp.rnd;
            long computed = lcg.next(last);
            long expected = lcg.mask(computed);
            logger.info(String.format(detector_id + ": (%d) Expected %s - "
                    + "Received %s", value, Long.toHexString(expected), 
                    Long.toHexString(received)));

            if (received == expected) {
                last = computed;
                save();     // update last and retries value
                rp.onValidated();

                for (GenericParsable gp : messageQueue) {
                    if (gp.rnd == (LCG.mask(lcg.next(computed)))) {
                        validate(gp);
                        messageQueue.remove(gp);
                        logger.info("Resolved order conflict");
                        break;
                    }
                }

            } else if (received == (LCG.mask(lcg.next(computed)))) { // one ahead
                messageQueue.add(rp);

            } else {
                logger.warn("Spoofed message detected");
                if (retries < 3) {
                    QueueManager qm = new QueueManager(detector_id);
                    qm.enqueue("changeSeed");
                    retries++;
                    updateSeed();   // reset seed (i.e. last=seed) and retries
                }

                isValid = false;
            }
        }

        return isValid;
    }

    private void updateSeed() {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement("UPDATE detector_seed SET last=seed, retries = ? WHERE id = ?");
            stmt.setLong(1, retries);
            stmt.setInt(2, id);
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

    private boolean getSignReports() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean ret = false;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement("SELECT \"signReports\" FROM detector_configuration WHERE detector_id = ?");
            stmt.setString(1, detector_id);
            rs = stmt.executeQuery();
            ret = rs.next() && rs.getBoolean(1);
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
        return ret;
    }

    public void save() {
        if (inserted) {
            // update
            Connection connection = null;
            PreparedStatement stmt = null;
            try {
                //create/obtain DataSource
                DatabaseManager dm = DatabaseManager.getInstance();
                connection = dm.getDatasource().getConnection();
                stmt = connection.prepareStatement("UPDATE detector_seed SET last = ?, retries=0 WHERE id = ?");
                stmt.setLong(1, last);
                stmt.setInt(2, id);
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
        } else {
            // insert
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                //create/obtain DataSource
                DatabaseManager dm = DatabaseManager.getInstance();
                connection = dm.getDatasource().getConnection();
                stmt = connection.prepareStatement("INSERT INTO detector_seed(id,detector_id,seed,last) VALUES(DEFAULT,?,?,?) RETURNING id");
                stmt.setString(1, detector_id);
                stmt.setLong(2, value);
                stmt.setLong(3, last);
                rs = stmt.executeQuery();
                if (!rs.next() && id != -1 && rs.getInt(1) != id) {
                    // error
                } else {
                    id = rs.getInt(1);
                    inserted = true;
                }
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
    }
}
