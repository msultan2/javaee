package com.ssl.bluetruth.receiver.v2.seed;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;

/**
 *
 * @author Liban Abdulkadir, svenkataramanappa
 */
public class SSH {

    private static Logger logger = Logger.getLogger(SSH.class);
    private String user = "remote", 
                   userdir, 
                   instation_privkey;
    
    // Keys Default paths
    private String localdir = "/tmp/", 
                   remotedir = "/var/cache/bt/";
    
    public String priv_file = "outstation_to_instation_key",            // outstation private key
                  pub_file = "outstation_to_instation_key.pub";         // outstation public key to be saved in the Instation authorised file
    
    private int port = 50000;
    private String host = "localhost";
    private final String authDir = "/home/bt/.ssh/";
    private String detectorId;
    private final String CHECK_DETECTOR_KEY_EXISTS = "SELECT EXISTS(SELECT 1 FROM detector_keys WHERE detector_id = ?);";
    private final String INSERT_DETECTOR_KEY = "INSERT INTO detector_keys VALUES (?,?); ";
    private final String UPDATE_DETECTOR_KEY = "UPDATE detector_keys SET outstation_public = ? WHERE detector_id = ?;";
    private final String GET_DETECTOR_KEYS = "SELECT outstation_public FROM detector_keys;";
    
    public SSH() {
    }

    public SSH port(int port) {
        this.port = port;
        return this;
    }

    public SSH host(String host) {
        this.host = host;
        return this;
    }

    public SSH user(String username) {
        this.user = username;
        return this;
    }

    public SSH localDir(String localdir) {
        this.localdir = localdir;
        return this;
    }

    public String instationPrivateKey() {
        return instation_privkey == null ? userDir() + ".ssh/id_rsa" : instation_privkey;
    }

    public SSH instationPrivateKey(String path) {
        instation_privkey = path;
        return this;
    }

    public SSH remoteDir(String remotedir) {
        this.remotedir = remotedir;
        return this;
    }

    public String userDir() {
        return userdir == null ? "/home/" + user + "/" : userdir;
    }

    public SSH userDir(String userdir) {
        this.userdir = userdir;
        return this;
    }

    public String getDetectorId() {
        return detectorId;
    }

    public SSH setDetectorId(String detectorId) {
        this.detectorId = detectorId;
        return this;
    }
    
    public void newkeys() {
        try {
            int type = KeyPair.RSA;
            JSch jsch = new JSch();

            KeyPair kpair = KeyPair.genKeyPair(jsch, type);

            File f = new File(localdir + "outstation_to_instation_key");
            if (f.exists()) {
                f.delete();     // delete outstation private key if exists
            }
            
            Path priv = Paths.get(localdir, priv_file);
            kpair.writePrivateKey(priv.toString());

            HashSet<PosixFilePermission> hs = new HashSet<>();
            hs.add(PosixFilePermission.OWNER_READ);
            Files.setPosixFilePermissions(priv, hs);

            Path pub = Paths.get(localdir, pub_file);
            kpair.writePublicKey(pub.toString(), "");           // No passphrase 
            
            sendFile(priv_file);                                // outstation private key
            
            String pubK = keyContents(pub_file);
            insertOrUpdateKey(pubK);
            regenerateInstationAuthorizedFile();
            
        } catch (JSchException ex) {
            logger.fatal("JSch Exception occured generating new keys");
        } catch (IOException ex) {
            logger.fatal("IO Exception occured generating new keys");
        }
    }

    public void regenerateInstationAuthorizedFile() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        FileWriter fw = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(GET_DETECTOR_KEYS);
            rs = stmt.executeQuery();
            deleteAuthorizedFile();     // delete authorized file
            fw = new FileWriter(authDir + "authorized_keys", true);
            while (rs.next()) {
                fw.write(rs.getString(1) + "\n");
            }
            logger.info("Authorized file created successfully");
        } catch (IOException ex) {
            logger.warn("Authorized file missing in directory : " + authDir);
        }
        catch (SQLException ex) {
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
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                }
            }
            fw = null;
        }
    }
    
    public void deleteAuthorizedFile() {
        try {
            File f = new File(authDir + "authorized_keys");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
        } catch (IOException ex) {
            logger.warn("Failed to create Instation authorized file");
        }
    }
    
    public void addOutstationToInstationKey() {
        FileWriter fw = null;
        try {
            String key = keyContents("generic_outstation_to_instation_key.pub");
            fw = new FileWriter(authDir + "authorized_keys", true);
            fw.write(key+"\n");
        } catch (FileNotFoundException ex) {
            logger.warn("Failed to find Generic Outstation to Instation public key");
        } catch (IOException ex) {
            logger.warn("Failed to find authorized keys file");
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                }
                fw = null;
            }
        }
    }
    
    private void insertOrUpdateKey(String pubK) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //create/obtain DataSource
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(CHECK_DETECTOR_KEY_EXISTS);
            stmt.setString(1, detectorId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean(1)) {
                    updateKey(connection, pubK);    // update
                } else {
                    insertKey(connection, pubK);    // insert
                }
            }
            logger.info(detectorId + " Insert or Update key successful");
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
    
    private void updateKey(Connection connection, String pubK) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(UPDATE_DETECTOR_KEY);
            ps.setString(1, pubK);
            ps.setString(2, detectorId);
            ps.executeUpdate();
            logger.info("Key updated successfully");
        } catch (SQLException ex) {
            logger.info("SQL query could not execute", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
                ps = null;
            }
        }
    }

    private void insertKey(Connection connection, String pubK) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(INSERT_DETECTOR_KEY);
            ps.setString(1, detectorId);
            ps.setString(2, pubK);
            ps.executeUpdate();
            logger.info("Key inserted successfully");
        } catch (SQLException ex) {
            logger.info("SQL query could not execute", ex);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
                ps = null;
            }
        }
    }
    
    private String keyContents(String key) throws FileNotFoundException {
        if (key.equals("generic_outstation_to_instation_key.pub")) {
            return new Scanner(new File(authDir + key)).useDelimiter("\\Z").next();
        }
        return new Scanner(new File(localdir + key)).useDelimiter("\\Z").next();
    }
    
    private void sendFile(String filename) {
        sendFile(localdir + filename, remotedir + filename);
    }

    // TODO: Refactor in the future
    // private void sendFile(String filename) {
    public boolean sendFile(String src, String dest) {
        boolean sent = false;
        String filename = new File(src).getName();
        JSch jsch = new JSch();
        // outstation must have the instation's current public key
        FileInputStream fis = null;
        Session session = null;
        try {
            logger.info("Send File:- User: " + user + "; Host: " + host + "; Port: " + port + "; Instation private key: " + instationPrivateKey());
            
            jsch.addIdentity(instationPrivateKey(),"");         // No passphrase
            session = jsch.getSession(user, host, port);
                                
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            String command = "scp -t " + dest;
            Channel chan = session.openChannel("exec");
            ((ChannelExec) chan).setCommand(command);
            OutputStream out = chan.getOutputStream();
            InputStream in = chan.getInputStream();
            chan.connect();
            boolean ptimestamp = true;
            if (checkAck(in) != 0) {
                // fail
            }
            File _lfile = new File(src);
            if (ptimestamp) {
                command = "T " + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    // fail
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (filename.lastIndexOf('/') > 0) {
                command += filename.substring(filename.lastIndexOf('/') + 1);
            } else {
                command += filename;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                // fail
            }

            // send a content of lfile
            fis = new FileInputStream(src);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                // fail
            }
            out.close();

            chan.disconnect();
            sent = true;
        } catch (Exception e) {
            logger.fatal("Exception occured while sending file ");
            sent = false;
        } finally {
            if (session != null) {
                session.disconnect();
            }
            return sent;
        }
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    } 
}
