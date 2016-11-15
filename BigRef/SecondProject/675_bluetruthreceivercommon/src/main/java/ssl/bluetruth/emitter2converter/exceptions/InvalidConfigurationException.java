/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF TH
 * IS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 * 
 * Created on 6th May 2015
 */
package ssl.bluetruth.emitter2converter.exceptions;

import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector.UnconfiguredType;

/**
 * This Exception should be thrown when there is any problem loading a
 * configuration value, regardless the reason or where is looking for to found
 * it. This information should be included in the String msg
 *
 * @author josetrujillo-brenes
 */
public class InvalidConfigurationException extends Exception {

    public InvalidConfigurationException(String msg) {
        super(msg);
    }

    public InvalidConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public void insertInDatabaseIfUnconfiguredDetector(String detectorId, UnconfiguredType unconfiguredType) {
        AbstractUnconfiguredDetector unconfiguredDetector = new AbstractUnconfiguredDetector();
        if (!unconfiguredDetector.checkDetectorConfiguredInDatabase(detectorId)) {           
            unconfiguredDetector.insertUnconfiguredDetector(detectorId, unconfiguredType);
        }
    }
}
