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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.controllers;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.ResultsManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(method = RequestMethod.GET)
public class ArchiveLogsController {

    private final Logger logger = LoggerFactory.getLogger(ArchiveLogsController.class);
    private final String zipFileName = "logs.zip";

    @Value("${rmas.downloadLogs.subPath}")
    private String archivedLogsFolder;

    @Value("${rmas.downloadLogs.localPath}")
    private String rmasDataFolder;

    @Autowired
    private ResultsManager resultsManager;

    @RequestMapping(value = "activity/{activityId}/logArchive", produces= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> sendFileResponse(@PathVariable(value = "activityId") String activityId) {
        UUID uuid = UUID.fromString(activityId);
        Optional<OperationResult> operationResult = resultsManager.getData(SecurityContextHolder.getContext().getAuthentication(), uuid);
        ResponseEntity<FileSystemResource> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (operationResult.isPresent()) {                        
            Path zipFilePath = Paths.get(System.getProperty("user.home"), rmasDataFolder, archivedLogsFolder, activityId, zipFileName);            
            FileSystemResource fileSystemResource = new FileSystemResource(zipFilePath.toString());
            if(fileSystemResource.exists()){
                responseEntity = new ResponseEntity<>(fileSystemResource, HttpStatus.OK);
            }            
        } else {            
            logger.debug("No result found for activity {}", activityId);
        }
        return responseEntity;
    }
}
