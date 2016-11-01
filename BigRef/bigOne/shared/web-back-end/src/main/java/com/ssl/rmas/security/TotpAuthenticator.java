/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */

package com.ssl.rmas.security;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.google.common.io.BaseEncoding;

/**
 * Implementation based on
 * http://blog.shinetech.com/2015/05/01/securing-your-spring-app-using-2fa/
 */
@Component
@PropertySource("classpath:config/application.properties")
public class TotpAuthenticator {

    @Value("${security.totp.variance:2}")
    private int variance;
    private Clock clock;

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public boolean verifyCode(String secret, int code)
            throws InvalidKeyException, NoSuchAlgorithmException {
        long timeIndex = clock.millis() / 1000 / 30;
        byte[] secretBytes = BaseEncoding.base32().decode(secret);
        for (int i = 0; i <= variance; i++) {
            long calculatedCode = getCode(secretBytes, timeIndex + i);
            if (calculatedCode == code) {
                return true;
            }
            if (i != 0) {
                calculatedCode = getCode(secretBytes, timeIndex - i);
                if (calculatedCode == code) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getCode(byte[] secret, long timeIndex) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signKey = new SecretKeySpec(secret, "HmacSHA1");
        // We put the timeIndex in a bytes array
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timeIndex);
        byte[] timeBytes = buffer.array();

        // Calculate the SHA1
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(timeBytes);

        // Calculate the offset we will use to extract our pin
        int offset = hash[19] & 0xf;
        // Clear the signed bits
        long truncatedHash = hash[offset] & 0x7f;
        // Use bits shift operations to copy the remaining 3 bytes from the
        // array
        // and construct our number
        for (int i = 1; i < 4; i++) {
            truncatedHash <<= 8;
            truncatedHash |= hash[offset + i] & 0xff;
        }
        // Truncate to 6 digits
        return truncatedHash % 1000000;
    }
}
