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

import com.ssl.rmas.entities.HeaderKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.http.multipart.UploadedMultipartFile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.support.MessageBuilder;
import static com.ssl.rmas.utils.ErrorMessage.FAILED_TO_PROCESS_FILES;
import java.util.HashSet;
import java.util.Set;
import static com.ssl.rmas.utils.ErrorMessage.THERE_ARE_NO_FILES_TO_UPLOAD;

public class TemporaryFileStorageTransformer {
    private final Logger logger = LoggerFactory.getLogger(TemporaryFileStorageTransformer.class);
    private final String HTTP_STATUS_CODE = "http_statusCode";
    private final String HTTP_CONTENT_TYPE = "Content-Type";
    private final String localPath;
    private final String tempDirectory;

    public TemporaryFileStorageTransformer(String localPath, String tempDir){
        this.localPath = localPath;
        this.tempDirectory = tempDir;
    }

    public Message<?> saveFiles(Message<?> message, @Headers MessageHeaders headers, LinkedMultiValueMap<String, UploadedMultipartFile> multipartRequest) throws IOException {
        logger.debug("headers: {}", headers);
        logger.debug("multipartRequest: {}", multipartRequest);

        Path outputDir = getOuputDir();
        List<Optional<Path>> savedFiles = multipartRequest.values().stream()
            .flatMap(value -> value.stream())
            .map(file -> saveFile(outputDir).savefile(file))
            .collect(Collectors.toList());

        Message<?> returnMessage;       
        Set<String> errors = new HashSet<>();
        if(savedFiles.isEmpty()){            
            errors.add(THERE_ARE_NO_FILES_TO_UPLOAD.toString());
            returnMessage = MessageBuilder
                    .withPayload(errors)
                    .copyHeaders(headers)
                    .setHeader(HTTP_CONTENT_TYPE, "application/json")
                    .setHeader(HTTP_STATUS_CODE, HttpStatus.BAD_REQUEST.value())
                    .build();
        }else if(savedFiles.stream().anyMatch(savedFile -> !savedFile.isPresent())) {
            errors.add(FAILED_TO_PROCESS_FILES.toString());
            returnMessage = MessageBuilder
                    .withPayload(errors)
                    .copyHeaders(headers)
                    .setHeader(HTTP_CONTENT_TYPE, "application/json")
                    .setHeader(HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        } else {
            returnMessage = createSuccessMessage(savedFiles.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()), message);
        }
        logger.debug("Message returned is: {}", message);
        return returnMessage;
    }

    private Path getOuputDir() throws IOException {
        final Path dir = Paths.get(System.getProperty("user.home"), localPath, tempDirectory);
        Files.createDirectories(dir);
        return dir;
    }

    private Message<?> createSuccessMessage(List<Path> savedFiles, Message<?> message) {
        return MessageBuilder.fromMessage(message).setHeader(HeaderKeys.FILE_PATHS.toString(), savedFiles).build();
    }

    private SaveFile saveFile(Path outputDir) {
        return (file) -> {
            String fileName = file.getOriginalFilename();
            Path outputFile = outputDir.resolve(fileName);
            try {
                file.transferTo(outputFile.toFile());
            } catch (IOException e) {
                logger.info("Failed to save file from upload", e);
                return Optional.empty();
            }
            return Optional.of(outputFile);
        };
    }

    @FunctionalInterface
    private interface SaveFile {
        Optional<Path> savefile(final UploadedMultipartFile file);
    }
}
