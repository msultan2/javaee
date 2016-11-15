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
 */
package com.ssl.rmas.managers;

import static com.ssl.rmas.entities.OperationResult.Status.FAILURE;
import static com.ssl.rmas.utils.ErrorMessage.*;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.JSchException;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.entities.OperationResult.Status;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.LogFile;
import com.ssl.rmas.utils.ssh.SshManager;
import java.nio.file.Files;
import java.time.Clock;

@Component
@PropertySource("classpath:config/application.properties")
public class DownloadLogsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadLogsManager.class);
    private static final String LOG_FILE_NAME_FORMAT = "yyyyMMdd";
    private static final String LOG_FILE_EXTENSION = "log";
    static final Path LOG_FILE_DIRECTORY_ON_DEVICE = Paths.get("logs");

    @Autowired
    private SshManager sshManager;

    @Value("${rmas.downloadLogs.cache:true}")
    private String cacheLogs;
    
    private Clock clock;
    
    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    OperationResult downloadLogFiles(final String outputDir, final Authentication currentUser, OperationResult opResult, ConnectionParams connectionParams, LocalDate fromDate, LocalDate toDate) {
        List<Path> logFiles = new ArrayList<>();
        LOGGER.debug("Downloading data...");
        Path downloadDestinationDir = Paths.get(System.getProperty("user.home"), outputDir, connectionParams.getIpAddress());
        boolean createdOutputDir = downloadDestinationDir.toFile().mkdirs();

        if (createdOutputDir) {
            LOGGER.debug("Created output directory {}", downloadDestinationDir);
        }
        try {
            logFiles = getLogFilePaths(currentUser, opResult.getActivityId(), connectionParams, fromDate, toDate);
        } catch (JSchException ex) {
            LOGGER.debug(FILES_NOT_FOUND_ON_ROADSIDE_DEVICE + " Exception thrown is: {}", ex.getMessage());
            opResult.setStatus(FAILURE);
            opResult.addErrorMessage(sshManager.getErrorMessage(ex));
        } catch (IOException ex) {
            opResult.setStatus(FAILURE);
            opResult.addErrorMessage(FAILED_TO_READ_DIRECTORY_ON_DEVICE);
        }
        if (!FAILURE.equals(opResult.getStatus())) {
            if (!logFiles.isEmpty()) {
                if (Boolean.TRUE.equals(Boolean.valueOf(cacheLogs))) {
                    logFiles = logFiles.stream()
                        .filter(file -> shouldBeDownloaded(file, downloadDestinationDir))
                        .collect(toList());
                }
                try {
                    opResult = sshManager.downloadFiles(currentUser, opResult, logFiles, connectionParams, downloadDestinationDir);
                    opResult.setResult(downloadDestinationDir.toString());
                    LOGGER.debug("Download Log Files, operation result: {}", opResult);
                    opResult = areLogsBetween2DatesInFolder(fromDate, toDate, opResult, downloadDestinationDir);
                } catch (IOException ex) {
                    LOGGER.debug("Download Log Files failed for activity id {}", opResult.getActivityId(), ex);
                    opResult.setStatus(FAILURE);
                    opResult.addErrorMessage(FAILED_TO_DOWNLOAD_ONE_OR_MORE_FILES);
                }
            } else {
                opResult.setStatus(FAILURE);
                opResult.addErrorMessage(FILES_NOT_FOUND_ON_ROADSIDE_DEVICE);
            }
        }
        return opResult;
    }

    private List<Path> getLogFilePaths(final Authentication currentUser, UUID activityId, final ConnectionParams connectionParams, final LocalDate dateFrom, final LocalDate dateTo) throws IOException, JSchException {
        List<Path> files = sshManager.getFilePaths(currentUser, activityId, LOG_FILE_DIRECTORY_ON_DEVICE, connectionParams);
        return files.stream()
            .filter(file -> new LogFile(file).isValid())
            .filter(file -> isDateWithinRange(file, dateFrom, dateTo))
            .collect(toList());
    }

    private OperationResult areLogsBetween2DatesInFolder(LocalDate fromDate, LocalDate toDate, OperationResult result, Path downloadDestinationDir) {
        LocalDate currentDate = fromDate;
        boolean atLeastOneSuccess = false;
        boolean atLeastOneFailure = false;
        while (isBeforeInclusive(currentDate, toDate)) {
            Path buildLogFileName = this.getPathFrom(currentDate, downloadDestinationDir);
            if (buildLogFileName.toFile().exists()) {
                atLeastOneSuccess = true;
            } else {
                result.addErrorMessage(FAILED_TO_DOWNLOAD_ONE_OR_MORE_FILES);
                atLeastOneFailure = true;
            }
            currentDate = currentDate.plusDays(1);
        }
        Status status = Status.getStatus(atLeastOneSuccess, atLeastOneFailure);
        result.setStatus(status);
        return result;
    }

    private boolean shouldBeDownloaded(final Path file, final Path downloadDestinationDir) {
        return hasNotBeenPreviouslyDownloaded(file, downloadDestinationDir) || isTodaysFile(file)
            || hasBeenDownloadedPartially(file);
    }

    private boolean hasNotBeenPreviouslyDownloaded(final Path file, final Path downloadDestinationDir) {
        Path outputFile = downloadDestinationDir.resolve(file.getFileName());
        return Files.notExists(outputFile);
    }

    private boolean isTodaysFile(final Path file) {
        return LocalDate.now(clock).equals(new LogFile(file).getDateFromFileName());
    }

    private boolean hasBeenDownloadedPartially(final Path file) {
        try {
            return new LogFile(file).hasBeenSavedOnFileDate();
        } catch (IOException ex) {
            LOGGER.warn("Failed to determine whether {} has been downloaded partially: {}", file, ex);
            return true;
        }
    }

    private boolean isDateWithinRange(final Path file, final LocalDate dateFrom, final LocalDate dateTo) {
        LocalDate fileDate = new LogFile(file).getDateFromFileName();
        return isAfterInclusive(fileDate, dateFrom) && isBeforeInclusive(fileDate, dateTo);
    }

    private boolean isAfterInclusive(final LocalDate fileDate, final LocalDate dateFrom) {
        return fileDate.isAfter(dateFrom) || fileDate.equals(dateFrom);
    }

    private boolean isBeforeInclusive(final LocalDate fileDate, final LocalDate dateTo) {
        return fileDate.isBefore(dateTo) || fileDate.equals(dateTo);
    }

    private Path getPathFrom(final LocalDate date, final Path downloadDestinationDir) {
        Path path = Paths.get(downloadDestinationDir.toString(), date.format(DateTimeFormatter.ofPattern(LOG_FILE_NAME_FORMAT)) + "." + LOG_FILE_EXTENSION);
        return path;
    }
}
