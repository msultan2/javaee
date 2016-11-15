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
package com.ssl.bluetruth.receiver.v2.entities.test;

import com.ssl.bluetruth.receiver.v2.entities.BluetoothDevice;
import java.time.Instant;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test class for com.ssl.bluetruth.receiver.v2.entities.BluetoothDevice. Tests
 * the obfuscation of a device ID.
 */
public class BluetoothDeviceTest {

    @Test
    public void testObfuscateIdV4Detection() {
        String v4MacAddress = "A826D953D2FB";
        BluetoothDevice btd1 = getBluetoothDevice();
        btd1.setId(v4MacAddress);
        Assert.assertFalse(v4MacAddress.equalsIgnoreCase(btd1.getId()));
    }

    @Test
    public void testObfuscateIdV3Detection() {
        String v3MacAddress = "0123456789abcdef";
        BluetoothDevice btd2 = getBluetoothDevice();
        btd2.setId(v3MacAddress);
        assertTrue(btd2.getId().equalsIgnoreCase(v3MacAddress));
    }

    @Test
    public void testObfuscateIdAlreadyObfuscateddWithCod() {
        String obfuscatedWithCod = "0123456789abcdef_CodeOfDevice";
        String obfuscatedNoCod = "0123456789abcdef";
        BluetoothDevice btd3 = getBluetoothDevice();
        btd3.setId(obfuscatedWithCod);
        assertTrue(btd3.getId().equalsIgnoreCase(obfuscatedNoCod));
    }

    @Test
    public void testObfuscateIdWithWhiteSpace() {
        String obfuscatedWithWhiteSpace = "k0rmaVJxig86KqWz3oNC g";
        String obfuscatedNoWhiteSpace = "k0rmaVJxig86KqWz3oNC+g";
        BluetoothDevice btd4 = getBluetoothDevice();
        btd4.setId(obfuscatedWithWhiteSpace);
        assertTrue(btd4.getId().equalsIgnoreCase(obfuscatedNoWhiteSpace));
    }

    @Test
    public void testObfuscateIdRealDetection1() {
        String macAddress2 = "0015831215b6";
        BluetoothDevice btd5 = getBluetoothDevice();
        btd5.setId(macAddress2);
        assertTrue(btd5.getId().equalsIgnoreCase("syTyqED2Lk38kXVlY+9EBA"));
    }

    @Test
    public void testObfuscateIdRealDetection2() {
        String macAddress3 = "00f46f5a84ad";
        BluetoothDevice btd6 = getBluetoothDevice();
        btd6.setId(macAddress3);
        assertTrue(btd6.getId().equalsIgnoreCase("/P7UQlCs2/76MPVJ6mOgrg"));
    }

    private BluetoothDevice getBluetoothDevice() {
        BluetoothDevice btd = new BluetoothDevice();
        btd.setCod(Integer.valueOf("527b7f29", 16));
        btd.setFirstSeen(Instant.ofEpochMilli(1383825193000L)); // 527b7f29 (seconds)
        btd.setReferencePoint(Instant.ofEpochMilli(1383825193000L).plusSeconds(5)); // 5 (seconds)
        btd.setLastSeen(Instant.ofEpochMilli(1383825193000L).plusSeconds(10)); // a (seconds)

        return btd;
    }
}