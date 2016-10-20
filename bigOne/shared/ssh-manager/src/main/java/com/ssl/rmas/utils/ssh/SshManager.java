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
package com.ssl.rmas.utils.ssh;

import static com.ssl.rmas.entities.OperationResult.Status.FAILURE;
import static com.ssl.rmas.utils.ErrorMessage.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.entities.OperationResult.Status;
import com.ssl.rmas.security.RMASUserDetails;
import com.ssl.rmas.ssh.manager.ConnectionParams;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.Collection;
import static java.lang.System.lineSeparator;
import org.springframework.util.StringUtils;

/**
 * Handles SSH command execution.
 *
 * Uses JSch - Java Secure Channel
 * http://www.jcraft.com/jsch/
 */
@PropertySource("classpath:config/application.properties")
@Component
public class SshManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshManager.class);
    private static final int BUFFER_DIVISOR = 2;
    private final Logger auditLogger = LoggerFactory.getLogger("Audit");
    private ExpiredKeyProvider expiredKeyProvider;

    @Value("${rmas.tempFileExtention:tmp}")
    private String fileDownloadingExtention;

    @Autowired
    private Clock clock;

    @Autowired
    public void setKeyProvider(ExpiredKeyProvider expiredKeyProvider) {
        this.expiredKeyProvider = expiredKeyProvider;
    }

    public OperationResult executeCommand(final Authentication currentUser, final OperationResult opResult, final String command, final ConnectionParams connectionParams)
            throws IOException {
        return executeCommand(currentUser, opResult, command, connectionParams, null);
    }

    public OperationResult executeCommand(final Authentication currentUser, final OperationResult opResult, final String command, final ConnectionParams connectionParams, final String pewNumber)
            throws IOException {
        Session session = null;
        setupMDC(currentUser, opResult.getActivityId(), connectionParams);
        try {
            session = startSessionRetryingWithOldKeys(connectionParams);
            String result = executeCommand(session, command, pewNumber);
            opResult.setResult(result);
        } catch (JSchException ex) {
            opResult.setStatus(FAILURE);
            opResult.addErrorMessage(getErrorMessage(ex));
            LOGGER.info("Failed to execute command: \"{}\"", command, ex);
        } finally {
            close(session);
        }
        return opResult;
    }

    private String pewNumberString(final String pewNumber) {
        if (StringUtils.isEmpty(pewNumber)) {
            return "";
        } else {
            return ", with PEW number: \"" + pewNumber + "\"";
        }
    }

    private String executeCommand(Session session, final String command, final String pewNumber) throws JSchException, IOException {
        ChannelExec channel = null;
        try {
            auditLogger.info("Executing command \"{}\"{}", command, pewNumberString(pewNumber));
            channel = startChannelExec(session, command);
            InputStream in = channel.getInputStream();
            String result = readResult(in);
            LOGGER.info("Executed command: \"{}\"{}, Result: \"{}\"", command, pewNumberString(pewNumber), result);
            return result;
        } finally {
            close(channel);
        }
    }

    private Session startSessionRetryingWithOldKeys(final ConnectionParams connectionParams) throws JSchException, IOException {
        try {
            return startSession(connectionParams);
        } catch(JSchException ex) {
            LOGGER.debug("Handling JSCH exception while starting session");            
            if(isInvalidPrivateKeyException(ex)){
                Session session = startSessionWithMultipleKeys(connectionParams);
                updatePublicKeyOnDevice(session);
                session = startSession(connectionParams);
                removeOldKeysOnDevice(session);
                return session;
            }
            throw ex;
        }
    }
    
    private Session startSession(final ConnectionParams connectionParams) throws JSchException, IOException {
        JSch jsch = getJsch(connectionParams);
        auditLogger.info("Connecting to device {}", connectionParams.getIpAddress());
        return configureSessionAndConnect(jsch, connectionParams);
    }
   
    private void updatePublicKeyOnDevice(Session session) throws JSchException, IOException {
        String newPublicKey = expiredKeyProvider.getPublicKey();
        String command = "updatekey " + newPublicKey;
        String successResponse = "ok";
        String pewNumber = null;
        String result = executeCommand(session, command, pewNumber);
        if (!successResponse.equals(result)) {
            String errMessage = "Failed to update new public key on the device after connecting with expired keys. Response from the device: \"" + result + "\", when the expected was \""+successResponse+"\"";
            LOGGER.info(errMessage);
            throw new JSchException(errMessage);
        }
    }

    private void removeOldKeysOnDevice(Session session) throws JSchException, IOException {
        String command = "deleteoldkey";
        String successResponse = "";
        String pewNumber = null;
        String result = executeCommand(session, command, pewNumber);
        if (!successResponse.equals(result)) {
            String errMessage = "Failed to remove all keys on the device after connecting with expired keys. Response from the device: \"" + result + "\", when the expected was \""+successResponse+"\"";
            LOGGER.info(errMessage);
            throw new JSchException(errMessage);
        }
    }

    private Session configureSessionAndConnect(JSch jsch, final ConnectionParams connectionParams) throws JSchException {
        Session session = jsch.getSession(connectionParams.getUserName(), connectionParams.getIpAddress(),
                connectionParams.getPort());
        session.setServerAliveInterval(5000);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }
    
    private Session startSessionWithMultipleKeys(final ConnectionParams connectionParams) throws JSchException{
        Collection<String> expiredPrivateKeys = expiredKeyProvider.getExpiredPrivateKeys();
        if(expiredPrivateKeys.isEmpty()){
            throw new JSchException("No expired keys found afer failing to connect with an invalid privatekey");
        }
        JSch jsch = getJschForMultipleIdentities(connectionParams, expiredPrivateKeys);
        return configureSessionAndConnect(jsch, connectionParams);
    }
    
    private JSch getJschForMultipleIdentities(final ConnectionParams connectionParams, final Collection<String> expiredPrivateKeys){
        JSch jsch = new JSch();       
        expiredPrivateKeys.stream().forEach(expiredPrivateKey -> {
            try {
                jsch.addIdentity(connectionParams.getUserName(), expiredPrivateKey.getBytes(UTF_8), null, null);
            } catch (JSchException ex) {
                LOGGER.debug("Exception thrown while adding identities to JSCH {}", ex.getMessage());
            }
        });
        
        return jsch;
    }

    private JSch getJsch(final ConnectionParams connectionParams) throws JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(connectionParams.getUserName(), connectionParams.getPrivateKey().getBytes(UTF_8), null, null);
        return jsch;
    }

    private ChannelExec startChannelExec(final Session session, final String command) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();
        return channel;
    }

    private ChannelSftp startChannelSftp(final Session session) throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }

    private String readResult(final InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            return buffer.lines().collect(Collectors.joining(lineSeparator()));
        }
    }

    private void close(final Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private void close(final Channel channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }

    /**
     * Warning: destinationFolder must exist before calling this method, otherwise IOException is thrown.
     */
    public OperationResult downloadFiles(final Authentication currentUser, final OperationResult opResult, final List<Path> files, final ConnectionParams connectionParams, final Path destinationFolder) throws IOException {
        Session session = null;
        ChannelSftp channelToBeClosed = null;
        Boolean atLeastOneFileFailed = false;
        Boolean atLeastOneFileSucceed = false;
        setupMDC(currentUser, opResult.getActivityId(), connectionParams);
        auditLogger.info("Downloading files...");
        try {
            session = startSessionRetryingWithOldKeys(connectionParams);
            ChannelSftp channel = startChannelSftp(session);
            channelToBeClosed = channel;
            int bandwidthLimit = connectionParams.getBandwidthLimit();
            LOGGER.info("Downloading files: {}", files);
            for (Path file : files) {
                try {
                    String currentRemoteDirectory = channel.pwd();
                    changeCurrentRemoteDirectory(file.getParent(), channel);
                    Path outputFile = destinationFolder.resolve(file.getFileName());
                    downloadFile(file.getFileName().toString(), outputFile, channel, bandwidthLimit);
                    changeCurrentRemoteDirectoryBack(currentRemoteDirectory, channel);
                    atLeastOneFileSucceed = true;
                } catch (IOException | SftpException ex) {
                    atLeastOneFileFailed = true;
                    LOGGER.info("Failed to download file {} during activity {} because of exception: {} ", file.getFileName(), opResult.getActivityId(), ex.getMessage(), ex);
                    opResult.addErrorMessage(FAILED_TO_DOWNLOAD_ONE_OR_MORE_FILES);
                }
            }
        } catch (JSchException ex) {
            LOGGER.info("Failed to download files from device {} during activity {} because of exception: {}", connectionParams, opResult.getActivityId(), ex.getMessage(), ex);
            opResult.addErrorMessage(getErrorMessage(ex));
        } finally {
            disconnectFromDevice(session, channelToBeClosed);
        }
        opResult.setStatus(Status.getStatus(atLeastOneFileSucceed, atLeastOneFileFailed));
        return opResult;
    }

    private void downloadFile(final String inputFileName, final Path outputFile, final ChannelSftp channel, final int bandwidthLimitKbps) throws IOException, SftpException {
        Path outputFileTemp = Paths.get(outputFile.toString() + "." + fileDownloadingExtention);
        auditLogger.info("Downloading file \"{}\"", inputFileName);
        try (BufferedInputStream input = new BufferedInputStream(channel.get(inputFileName));
                BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(outputFileTemp))) {
            copyWithBandwidthLimit(input, output, bandwidthLimitKbps);
            Files.move(outputFileTemp, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            FileUtils.deleteQuietly(outputFileTemp.toFile());
        }
    }

    private void copyWithBandwidthLimit(BufferedInputStream input, BufferedOutputStream output, final int bandwidthLimitKbps) throws IOException {
        int bytesPerSecond = bandwidthLimitKbps * (1024 / 8);
        int bufferSize = Math.min(bytesPerSecond / BUFFER_DIVISOR, 32 * 1024);
        byte[] buffer = new byte[bufferSize];
        LOGGER.debug("SFTP Buffer set to {} bytes based on a max bandwidth of {} Kbps", bufferSize, bandwidthLimitKbps);
        Instant startTime = Instant.now(clock);
        int bytesRead;
        long totalBytesRead = 0;
        while ((bytesRead = input.read(buffer)) > 0) {
            LOGGER.debug("Read {} bytes", bytesRead);
            output.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            sleep(startTime, totalBytesRead, bytesPerSecond);
        }
    }

    private void sleep(final Instant startTime, final long totalBytesRead, final int bandWidthInBytesPerSecond) {
        Duration timeElapsed = Duration.between(startTime, Instant.now(clock));
        Duration timeToSleep = getTimeToSleep(totalBytesRead, timeElapsed, bandWidthInBytesPerSecond);
        if (!timeToSleep.isNegative() && !timeToSleep.isZero()) {
            LOGGER.debug("Took {}ms to read {} bytes, sleeping for {}ms", timeElapsed, totalBytesRead, timeToSleep);
            try {
                Thread.sleep(timeToSleep.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Duration getTimeToSleep(final long bytesRead, final Duration timeTaken, final long bandWidthInBytesPerSecond) {
        Duration timeShouldHaveTaken = Duration.ofSeconds(bytesRead / bandWidthInBytesPerSecond);
        return timeShouldHaveTaken.minus(timeTaken);
    }

    public List<Path> getFilePaths(final Authentication currentUser, final UUID activityId, final Path relativeRemoteDirectory, final ConnectionParams connectionParams) throws IOException, JSchException {
        Session session = null;
        ChannelSftp channelToBeClosed = null;
        setupMDC(currentUser, activityId, connectionParams);
        try {
            session = startSessionRetryingWithOldKeys(connectionParams);
            ChannelSftp channel = startChannelSftp(session);
            channelToBeClosed = channel;
            List<LsEntry> result = listContentsOfRemoteDirectory(channel, relativeRemoteDirectory.toString());

            List<Path> results = result.stream()
                    .map(LsEntry::getFilename)
                    .filter(fileName -> !".".equals(fileName))
                    .filter(fileName -> !"..".equals(fileName))
                    .map(relativeRemoteDirectory::resolve)
                    .collect(Collectors.toList());
            return results;
        } catch (SftpException ex) {
            LOGGER.error("Failed to get file paths in: {}", relativeRemoteDirectory, ex);
            throw new IOException("Failed to get file paths in " + relativeRemoteDirectory, ex);
        } finally {
            disconnectFromDevice(session, channelToBeClosed);
        }
    }

    @SuppressWarnings("unchecked")
    private List<LsEntry> listContentsOfRemoteDirectory(final ChannelSftp channel, final String dir) throws SftpException {
        auditLogger.info("Getting directory listing \"{}\"", dir);
        return channel.ls(dir);
    }

    /**
     * Fixes the problem of File separator by extracting each directory from the given path and changing the current
     * remote directory to the corresponding directory
     */
    private void changeCurrentRemoteDirectory(final Path path, final ChannelSftp channel) throws SftpException {
        auditLogger.info("CDing to \"{}\"", path);
        for (Path directory : path) {
            channel.cd(directory.toString());
        }
    }

    private void changeCurrentRemoteDirectoryBack(final String path, ChannelSftp channel) throws SftpException {
        channel.cd(path);
    }

    private ErrorMessage getErrorMessageForUploadFile(Exception ex) {
        ErrorMessage message;

        if (ex instanceof JSchException) {
            message = getErrorMessage((JSchException)ex);
        } else if (ex instanceof SftpException) {
            message = getErrorMessageForUploadFile((SftpException) ex);
        }else if(ex instanceof IOException){
            message = getErrorMessageForUploadFile((IOException) ex);
        }else{
            message = UNKNOWN;
        }

        return message;
    }

    private ErrorMessage getErrorMessageForUploadFile(SftpException sftpEx) {
        if (sftpEx.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
            return DEVICE_DIRECTORY_NOT_FOUND;
        } else {
            return ERROR_WHILE_UPLOADING_FILE;
        }
    }

    private ErrorMessage getErrorMessageForUploadFile(IOException ioEx) {
        if (ioEx instanceof FileNotFoundException) {
            return TEMPORARY_FILE_NOT_FOUND;
        } else {
            return ERROR_WHILE_UPLOADING_FILE;
        }
    }

    public boolean uploadFile(final Authentication currentUser, final UUID activityId, final Path remoteFileName,
            final Path localPath, final ConnectionParams connectionParams,
            final ChannelSftp channel) throws SftpException {

        setupMDC(currentUser, activityId, connectionParams);
        auditLogger.info("Uploading file \"{}\"", remoteFileName);
        LOGGER.debug("Uploading firmware file: {} during activity {}", localPath.toString(), activityId);
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(localPath.toFile()));
                BufferedOutputStream output = new BufferedOutputStream(channel.put(remoteFileName.getFileName().toString()))) {
            copyWithBandwidthLimit(input, output, connectionParams.getBandwidthLimit());
            LOGGER.debug("Firmware file uploaded successfully during activity {}", activityId);
            return true;
        }catch(IOException ex){
            LOGGER.debug("Failed to upload file {} during activity {} because of  {}", remoteFileName.getFileName(), activityId, ex.getLocalizedMessage(), ex);
            return false;
        }
    }

    public OperationResult uploadFiles(final Authentication currentUser, OperationResult operationResult, final Path UPLOAD_FIRMWARE_REMOTE_PATH, final List<Path> filePathList, final ConnectionParams connectionParams) throws IOException {
        Session session = null;
        ChannelSftp channelToBeClosed = null;
        boolean atLeastOneSuccess = false;
        boolean atLeastOneFailure = false;
        boolean status = false;
        setupMDC(currentUser, operationResult.getActivityId(), connectionParams);
        try {
            session = startSessionRetryingWithOldKeys(connectionParams);
            ChannelSftp channel = startChannelSftp(session);
            channelToBeClosed = channel;
            channel.cd(UPLOAD_FIRMWARE_REMOTE_PATH.toString());
            for (Path path : filePathList) {
                status = uploadFile(currentUser, operationResult.getActivityId(), path.getFileName(), path, connectionParams, channel);
                if (status) {
                    atLeastOneSuccess = true;
                } else {
                    atLeastOneFailure = true;
                }
            }
            operationResult.setStatus(Status.getStatus(atLeastOneSuccess, atLeastOneFailure));
        } catch (JSchException | SftpException ex) {
            operationResult.setStatus(FAILURE);
            operationResult.addErrorMessage(getErrorMessageForUploadFile(ex));
            LOGGER.debug("Failed to upload firmware to the device {} during activity {} because of exception: {}", connectionParams, operationResult.getActivityId(), ex.getMessage(), ex);
        } finally {
            disconnectFromDevice(session, channelToBeClosed);
        }
        return operationResult;
    }

    private void disconnectFromDevice(final Session session, final Channel channel) {
        close(channel);
        close(session);
    }

    public ErrorMessage getErrorMessage(JSchException ex) {        
        if (isInvalidPrivateKeyException(ex)) {
            return PRIVATE_KEY_USED_TO_CONNECT_TO_DEVICE_WAS_INCORRECT;
        } else {
            return FAILED_TO_CONNECT_TO_DEVICE;
        }
    }

    private String getUserIdFromAuth(Authentication currentUser) {
        String userId = null;
        if(currentUser!=null && currentUser.getPrincipal()!=null && User.class.isAssignableFrom(currentUser.getPrincipal().getClass())) {
            userId = ((RMASUserDetails)currentUser.getPrincipal()).getUserId();
        }
        return userId;
    }

    private void setupMDC(final Authentication currentUser, final UUID activityId,
            final ConnectionParams connectionParams) {
        MDC.put("userId", getUserIdFromAuth(currentUser));
        MDC.put("activityId", activityId==null?"null":activityId.toString());
        MDC.put("device", connectionParams==null?"null":connectionParams.getIpAddress());
    }
    
    private boolean isInvalidPrivateKeyException(JSchException exception){
        String rawMsg = exception.getMessage();
        return rawMsg != null && (rawMsg.contains("invalid privatekey") || rawMsg.contains("Auth fail"));
    }
}
