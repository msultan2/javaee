package com.ssl.bluetruth.receiver.v2.test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.ssl.bluetruth.receiver.v2.seed.SSH;
import org.junit.Ignore;

/**
 *
 * @author liban 
 * Note: current user must be able to ssh into localhost as
 * "outstation_user" WITHOUT a password. Please, copy the public key to the
 * "outstation_user" before running this test suite
 */
@Ignore
public class SSHTest {

    private SSH ssh;
    private String outstation_user = "bluetruth", instation_user = "liban";

    public SSHTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    File localdir;
    File remotedir;
    File userdir;

    @Before
    public void setUp() {
        localdir = new File("/tmp/sshtest/local");
        remotedir = new File("/tmp/sshtest/remote");
        remotedir.setReadable(true, false);
        remotedir.setWritable(true, false);
        userdir = new File("/tmp/sshtest/userdir");

        localdir.mkdirs();
        remotedir.mkdirs();
        userdir.mkdirs();
        new File(userdir, ".ssh").mkdirs();

        ssh = new SSH().user(outstation_user)
                .instationPrivateKey("/home/" + instation_user + "/.ssh/id_rsa")
                .localDir(localdir.getAbsolutePath() + "/")
                .remoteDir(remotedir.getAbsolutePath() + "/")
                .userDir(userdir.getAbsolutePath() + "/");
        ssh.newkeys();
    }

    private byte[] sha1(File f) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            InputStream fis = new FileInputStream(f);
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void privateKeysSame() {
        File private_key_local = new File(localdir, "/" + ssh.priv_file);
        File private_key_remote = new File(remotedir, "/" + ssh.priv_file);

        assertArrayEquals(sha1(private_key_local), sha1(private_key_remote));
    }

    @Test
    public void publicKeysSame() {
        File public_key_local = new File(localdir, "/" + ssh.pub_file);
        File public_key_remote = new File(localdir, "/" + ssh.pub_file);

        assertArrayEquals(sha1(public_key_local), sha1(public_key_remote));
    }

    @Test
    public void authorizedKeysChanged() {
        File public_key_local = new File(localdir, "/" + ssh.pub_file);
        File authorized_keys = new File(userdir, ".ssh/authorized_keys");

        assertArrayEquals(sha1(public_key_local), sha1(authorized_keys));
    }

    @Test
    public void sendFile() {
        File src_file = new File("/tmp/sendFile_test");
        File dest_file = new File("/tmp/sendFile_test_received");
        if (src_file.exists()) {
            src_file.delete();
        }
        if (dest_file.exists()) {
            dest_file.delete();
        }
        
        try {
            src_file.createNewFile();
            try (PrintWriter pw = new PrintWriter(src_file)) {
                pw.write("test");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        ssh.sendFile(src_file.getAbsolutePath(), dest_file.getAbsolutePath());

        assertTrue(dest_file.exists());
        assertArrayEquals(sha1(src_file), sha1(dest_file));
    }

    @After
    public void tearDown() {
    }
}