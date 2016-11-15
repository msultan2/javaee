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
 * Java version: JDK 1.8
 *
 * Created By: Estelle Edwards
 *
 * Product: 674 - BlueTruthReceiver2
 */
package com.ssl.bluetruth.receiver.v2;

import com.ssl.bluetruth.receiver.v2.datalogger.DataLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @author Estelle Edwards
 */
@Component
public class ApplicationListener implements
        org.springframework.context.ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LogManager.getLogger(ApplicationListener.class);
    private DataLogger dataLogger;

    public ApplicationListener() {
        if (logger.isTraceEnabled()) {
            logger.trace("ApplicationListener (" + this + ") created");
        }
    }

    public DataLogger getDataLogger() {
        return dataLogger;
    }

    @Autowired
    public void setDataLogger(DataLogger dataLogger) {
        this.dataLogger = dataLogger;

        if (logger.isTraceEnabled()) {
            logger.trace("setDataLogger( " + dataLogger + ") called");
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (logger.isTraceEnabled()) {
            logger.trace("onApplicationEvent (" + event + ") called for: " + this);
            logger.trace("using dataLogger instance: " + dataLogger);
        }

        dataLogger.closeMongoConnections();
    }
}