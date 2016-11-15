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
 * Created By: Estelle Edwards
 *
 * Product: 674 - BlueTruthReceiver2
 */
package com.ssl.bluetruth.receiver.v2.test.mocks;

import java.util.Map;
import ssl.bluetruth.emitter2converter.configuration.OutStationsConfiguration;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;

/**
 *
 * @author Estelle Edwards
 */
public class MockOutStationsConfiguration implements OutStationsConfiguration {
    
    Map<String, String> outstationsMap;

    public MockOutStationsConfiguration(Map<String, String> outStationsMap) {
        this.outstationsMap = outStationsMap;
    }

    public void setOutstationsMap(Map<String, String> outstationsMap) {
        this.outstationsMap = outstationsMap;
    }

    @Override
    public Map<String, String> getMap(String idOutStation) throws InvalidConfigurationException {
        return outstationsMap;
    }
    
}
