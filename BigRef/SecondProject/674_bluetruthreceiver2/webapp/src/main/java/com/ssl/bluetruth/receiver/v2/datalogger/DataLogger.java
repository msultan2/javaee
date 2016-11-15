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
package com.ssl.bluetruth.receiver.v2.datalogger;

import com.ssl.bluetruth.receiver.v2.entities.StatisticsReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Estelle Edwards
 */
@Component
@Scope("singleton")
public class DataLogger {

    private static final Logger logger
            = LogManager.getLogger(DataLogger.class.getName());
    private MongoDbManager mongoDbManager;

    public DataLogger() {
        if (logger.isInfoEnabled()) {
            logger.info("DataLogger instance (" + this + ") created");
        }
    }

    public MongoDbManager getMongoDbManager() {
        return mongoDbManager;
    }

    @Autowired
    public void setMongoDbManager(MongoDbManager mongoDbManager) {
        this.mongoDbManager = mongoDbManager;

        if (logger.isInfoEnabled()) {
            logger.info("setMongoDbManager( " + mongoDbManager + ") called");
        }
    }

    public void logStatisticsReport(StatisticsReport sr) {
        if (logger.isInfoEnabled()) {
            logger.info("Logging new statistics report to MongoDB with ID: "
                    + sr.id);
        }
        
        mongoDbManager.insertStatsReport(sr);
    }

    public void closeMongoConnections() {
        logger.info("About to close down Mongo connections...");

        mongoDbManager.closeConnections();

        logger.info("MongoDbManager successfully destroyed.");
    }
}
