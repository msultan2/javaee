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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ssl.bluetruth.receiver.v2.QueueManager;
import com.ssl.bluetruth.receiver.v2.entities.Detector;
import com.ssl.bluetruth.receiver.v2.entities.StatusReport;
import com.ssl.bluetruth.receiver.v2.misc.RequestParserFactory;

/**
 *
 * @author Liban Abdulkadir, svenkataramanappa
 */
@Controller
public class StatusReportController {

    @Autowired
    private RequestParserFactory requestParserFactory;

    @RequestMapping(value = "/Status", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<String> incoming(HttpServletRequest request) {
        
        LogManager.getLogger(StatusReportController.class.getName()).debug("Receiving new status report...");
        StatusReport sr = requestParserFactory.getRequestParser(StatusReport.class).request(request).parse();
        sr.seedManage();
        Detector.getInstance(sr.id).getSeed().setDetectorId(sr.id).validate(sr);
        String r = new QueueManager(sr.id).assembleAndFlush();
        // This part is used for sending seed file automatically
        if (!sr.commands.isEmpty()) {
            QueueManager qm = new QueueManager(sr.id);
            for (String cmd : sr.commands) {
                qm.enqueue(cmd);
            }
        }
        return new ResponseEntity<>(r, HttpStatus.OK);
    }
}
