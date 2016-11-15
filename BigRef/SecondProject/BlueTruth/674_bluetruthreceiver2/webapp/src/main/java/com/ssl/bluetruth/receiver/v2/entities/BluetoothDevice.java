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
 */
package com.ssl.bluetruth.receiver.v2.entities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Liban Abdulkadir
 */
public class BluetoothDevice {

    public BluetoothDevice() {
    }

    private String id;
    private int cod;
    private Instant firstSeen;
    private Instant referencePoint;
    private Instant lastSeen;
    private Integer refTime;
    private Integer endTime;
    private static final String TIMESTAMP_LIMIT = "FFFFFFFE";
    private static final Logger logger = LogManager.getLogger(BluetoothDevice.class.getName());
    private static final String MD5 = "MD5";
    private static final String BASE64_PADDING = "=";

    public static BluetoothDevice deserialise(String a) {
        String[] parts = a.split(":");
        BluetoothDevice bd = new BluetoothDevice();
        bd.setId(parts[0]);
        bd.setCod(Integer.valueOf(parts[1], 16));
        bd.setFirstSeen(Instant.ofEpochSecond(Long.valueOf(parts[2], 16)));
        bd.setReferencePoint(Instant.ofEpochSecond(bd.getFirstSeen().getEpochSecond() + Long.valueOf(parts[3], 16)));
        bd.setRefTime(Integer.valueOf(parts[3], 16));

        long lastDeltams = Long.valueOf(parts[4], 16);
        if (lastDeltams > 0) {
            bd.setLastSeen(Instant.ofEpochSecond(bd.getFirstSeen().getEpochSecond() + lastDeltams));
        }

        bd.setEndTime(Integer.valueOf(parts[4], 16));
        return bd;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(Instant firstSeen) {
        this.firstSeen = firstSeen;
    }

    public Instant getReferencePoint() {
        return referencePoint;
    }

    public void setReferencePoint(Instant referencePoint) {
        this.referencePoint = referencePoint;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = obfuscateId(id);
    }

    /**
     * This method is used to obfuscate the device ID (MAC address) of a v4
     * detection so that this matches the obfuscation that would take place for
     * a v3 detection. This is done to ensure that if the same device ID is
     * detected by a mix of V3 and V4 detectors it will be recognised as so.
     *
     * The v3 detectors obfuscate the device ID by performing an MD5 hash on the
     * device MAC address which is then Base64 encoded. This value is then
     * appended with an underscore and the 'class of device' - a value retrieved
     * via a lookup table at the V3 outstation. The v4 detectors perform no
     * conversions to the device MAC address.
     *
     * @param toObfuscate
     * @return obfuscatedId
     */
    private String obfuscateId(String toObfuscate) {
        String obfuscatedId = null;

        if (logger.isDebugEnabled()) {
            logger.debug("obfuscateId(): Incoming device ID = " + toObfuscate);
        }

        //check if a v4 device ID
        if (toObfuscate.trim().length() == 12) {
            if (logger.isDebugEnabled()) {
                logger.debug("obfuscateId(): Device ID " + toObfuscate
                        + " is from a v4 outstation");
            }

            //match algorithm in C# outstation code
            byte[] asBytes = toObfuscate.toUpperCase().getBytes(StandardCharsets.US_ASCII);

            try {
                //hash the ID
                MessageDigest md = MessageDigest.getInstance(MD5);
                byte[] md5 = md.digest(asBytes);

                //Base64 encode the hahsed byte array
                String temp = Base64.getEncoder().encodeToString((md5));

                if (logger.isDebugEnabled()) {
                    logger.debug("obfuscateId(): Base64 encoded ID = " + temp);
                }

                //remove any padded '=' characters from end of string
                while (temp.endsWith(BASE64_PADDING)) {
                    temp = temp.substring(0, temp.length()-1);
                }
                
                obfuscatedId = temp;

            } catch (NoSuchAlgorithmException nsae) {
                logger.error("Could not create MD5 digest", nsae);

            } catch (Exception ex) {
                logger.error("Could not create MD5 digester", ex);
            }

        } else {
            //this is a v3 device ID - it will be > 12 characters long
            if (logger.isDebugEnabled()) {
                logger.debug("obfuscateId(): Device ID " + toObfuscate
                        + " is from a v3 outstation");
            }

            //substitute white space with '+' characters
            toObfuscate = toObfuscate.replace(" ", "+");

            //remove any appended values i.e. underscore and end substring
            if (toObfuscate.contains("_")) {
                obfuscatedId = toObfuscate.substring(0, (toObfuscate.indexOf("_")));

            } else {
                obfuscatedId = toObfuscate;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("obfuscateId(): obfuscated device ID = " + obfuscatedId);
        }

        return obfuscatedId;
    }

    @Override
    public String toString() {
        return StringUtils.join(new Object[]{id, Integer.toHexString(cod),
            //handle null 'firstSeen'
            (firstSeen != null) ? Integer.toHexString((int) (firstSeen.getEpochSecond())) : firstSeen,
            //handle null 'firsSeen or referencePoint'
            (firstSeen != null && referencePoint != null) ? Integer.toHexString((int) (referencePoint.getEpochSecond() - firstSeen.getEpochSecond())) : null,
            //handle a null 'lastSeen' value
            (lastSeen != null) ? Integer.toHexString((int) (lastSeen.getEpochSecond() - firstSeen.getEpochSecond())) : lastSeen
        }, ':');
    }

    public static boolean isLastSeenValid(String a) {
        boolean isLastSeenValid = true;
        String[] parts = a.split(":");
        String id = parts[0];
        String firstSeenString = parts[2];
        String lastDeltamsString = parts[4];
        try {
            long lastDeltams = Long.valueOf(parts[4], 16);
            long limit = Long.valueOf(TIMESTAMP_LIMIT, 16);

            if (lastDeltams >= limit) {
                isLastSeenValid = false;
                logger.warn("LastSeen is invalid for device ID: " + id
                        + ". Value is: " + firstSeenString + " + " + lastDeltamsString);
            }
        } catch (NumberFormatException nfex) {
            logger.warn("Last seen of " + a + " caused a NumberFormatException");
            return false;
        }
        return isLastSeenValid;
    }

    /**
     * @return the refTime
     */
    public Integer getRefTime() {
        return refTime;
    }

    /**
     * @param refTime the refTime to set
     */
    public void setRefTime(Integer refTime) {
        this.refTime = refTime;
    }

    /**
     * @return the endTime
     */
    public Integer getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }
}
