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
 * Created By: Liban Abdulkadir
 *
 * Product: 674 - BlueTruthReceiver2
 */
package com.ssl.bluetruth.receiver.v2.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ssl.bluetruth.receiver.v2.QueueManager;
import com.ssl.bluetruth.receiver.v2.datalogger.DataLogger;
import com.ssl.bluetruth.receiver.v2.entities.Detector;
import com.ssl.bluetruth.receiver.v2.entities.StatisticsReport;
import com.ssl.bluetruth.receiver.v2.misc.RequestParserFactory;

/**
 *
 * @author Liban Abdulkadir
 */
@Controller
public class StatisticsReportController {

    private static final Logger logger = LogManager.getLogger(StatisticsReportController.class);

    @Autowired
    private RequestParserFactory requestParserFactory;
    private DataLogger dataLogger;

    public DataLogger getDataLogger() {
        return dataLogger;
    }

    @Autowired
    public void setDataLogger(DataLogger dataLogger) {
        this.dataLogger = dataLogger;

        if (logger.isInfoEnabled()) {
            logger.info("setDataLogger() called with instance: " + dataLogger);
        }
    }

    public StatisticsReportController() {
        if (logger.isInfoEnabled()) {
            logger.info("StatisticsReportController instance: (" + this + ") created");
        }
    }

    @RequestMapping(value = "/Statistics", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<String> incoming(HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("incoming(HttpServletRequest request) called with "
                    + "DataLogger instance: " + dataLogger);
            logger.debug(
                    "Receiving new statistics report...");
        }

        StatisticsReport sr = requestParserFactory.getRequestParser(StatisticsReport.class).request(request).parse();
        boolean isValid = Detector.getInstance(sr.id).getSeed().setDetectorId(sr.id).validate(sr);

        if (isValid) {
            dataLogger.logStatisticsReport(sr);

        } else {
            LogManager.getLogger(StatisticsReportController.class.getName()).warn(
                    "Spoofed data detected for statistics report with ID: " + sr.id
                    + " and data: " + sr.data);
        }

        return new ResponseEntity<>(new QueueManager(sr.id).assembleAndFlush(),
                HttpStatus.OK);
    }
}
