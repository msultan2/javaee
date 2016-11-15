/*
 * 
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ''AS IS'' AND
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
 * Copyright 2015(C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created By: Estelle Edwards, Simulation Systems Ltd
 *
 * Product: 674
 */
package com.ssl.bluetruth.receiver.v2.test.mocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import ssl.bluetruth.emitter2converter.configuration.ConfigurationManager;

import com.ssl.bluetruth.receiver.v2.entities.StatisticsReport;

/**
 *
 * @author Estelle Edwards
 */
@ContextConfiguration(locations = "classpath:testContext.xml")
public class MockStatisticsReport extends StatisticsReport {

    @Autowired
    private ConfigurationManager configurationManager;
    private static final Logger logger = LogManager.getLogger(MockStatisticsReport.class.getName());

    public MockStatisticsReport() {
        super();
    }

    @Override
    public int getTimestampTolerance(final String detectorId) {
        int timestampTolerance = 5000;

        if (detectorId.equalsIgnoreCase("")) {
            timestampTolerance = 10000;

        }

        return timestampTolerance;
    }
}