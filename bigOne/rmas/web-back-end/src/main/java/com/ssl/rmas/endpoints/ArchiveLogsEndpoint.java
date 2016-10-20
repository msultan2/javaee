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
package com.ssl.rmas.endpoints;

import com.ssl.rmas.entities.DownloadLogDates;
import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.utils.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.springframework.messaging.Message;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;

public class ArchiveLogsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveLogsEndpoint.class);

    private static final String LOG_FILE_NAME_FORMAT = "yyyyMMdd";
    private final String zipFileName = "logs.zip";
    private static final String LOG_FILE_EXTENSION = ".log";

    @Value("${rmas.downloadLogs.subPath}")
    private String archivedLogsFolder;

    @Value("${rmas.downloadLogs.localPath}")
    private String rmasDataFolder;

    @Value("${rmas.downloadLogs.maxFolderSizeMB}")
    private String maxArchivedLogsFolderSizeMB;

    public Message<DownloadLogDates> archiveLogs(Message<DownloadLogDates> message) throws IOException {
        
        LocalDate startDate = message.getPayload().getStartDate();
        LocalDate endDate = message.getPayload().getEndDate();
        OperationResult opResult = message.getHeaders().get(HeaderKeys.OPERATION_RESULT.toString(), OperationResult.class);
        String zipName = opResult.getActivityId().toString();
        Path archiveLogsDir = Paths.get(System.getProperty("user.home"), rmasDataFolder, archivedLogsFolder, zipName);        
        List<Path> logFileNamesList = getFileNamesList(startDate, endDate, opResult);
        Path zipFilePath = Paths.get(archiveLogsDir.toString(), zipFileName);
        
        if (!opResult.getStatus().equals(OperationResult.Status.FAILURE)){
            if(totalSizeOfLogFilesIsBelowLimit(logFileNamesList)){
                Files.createDirectories(archiveLogsDir);
                archiveLogs(zipFilePath, logFileNamesList);
                LOGGER.debug("Operation result after zipping the files: {}", opResult);
            }else{                
                opResult.setStatus(OperationResult.Status.FAILURE);
                opResult.setErrorMessage(ErrorMessage.ARCHIVE_TOO_BIG);
                LOGGER.debug("Operation result when size of log file zip exceeds the maximum limit: {}", opResult);
            }
            opResult.setResult("");
        }else{
            LOGGER.debug("Archive was not created as status of operation result was failure");
        }
        
        return MessageBuilder.fromMessage(message)
                .setHeader(HeaderKeys.OPERATION_RESULT.toString(), opResult)
                .build();
    }

    private List<Path> getFileNamesList(final LocalDate startDate, final LocalDate endDate, final OperationResult opResult) throws IOException {
        List<Path> logFiles = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Path logFile = getLogFile(opResult, currentDate);
            if (Files.exists(logFile)){
                logFiles.add(logFile);
            }
            currentDate = currentDate.plusDays(1);
        }
        return logFiles;
    }

    private Path getLogFile(final OperationResult opResult, final LocalDate currentDate) {
        String datePartOfFileName = currentDate.format(DateTimeFormatter.ofPattern(LOG_FILE_NAME_FORMAT));
        return Paths.get(opResult.getResult(), datePartOfFileName + LOG_FILE_EXTENSION);
    }

    private void archiveLogs(final Path zipFilePath, final List<Path> fileList) throws IOException {
        Map<String, String> env = Collections.singletonMap("create", "true");
        URI zipFileUri = URI.create("jar:".concat(zipFilePath.toUri().toString()));
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFileUri, env)) {
            for (Path file : fileList) {
                addLogToArchive(zipFileSystem, file);
            }
        } catch (IOException ie) {
            LOGGER.debug("Error occured while zipping the files: {} ", ie.getLocalizedMessage());
            Files.deleteIfExists(zipFilePath);
            throw ie;
        }
    }

    private void addLogToArchive(final FileSystem zipFileSystem, final Path file) throws IOException {
        String fileName = file.getFileName().toString();
        Path pathInZipfile = zipFileSystem.getPath("/".concat(fileName));
        Files.copy(file, pathInZipfile);
    }

    private boolean totalSizeOfLogFilesIsBelowLimit(List<Path> logFiles) throws IOException {
        long totalSizeInBytes = 0;
        for(Path logFile : logFiles){
            totalSizeInBytes += Files.size(logFile);
        }
        return totalSizeInBytes <= Long.parseLong(maxArchivedLogsFolderSizeMB) * 1024 * 1024;
    }
}
