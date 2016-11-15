/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import static java.time.ZoneOffset.UTC;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.commons.io.FilenameUtils;

public class LogFile {

    private static final String LOG_FILE_NAME_FORMAT = "yyyyMMdd";
    private static final String LOG_FILE_EXTENSION = "log";
    private final Path file;

    public LogFile(final Path file) {
        this.file = file;
    }

    public LocalDate getCreationDate() throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
        Instant creationTime = attrs.creationTime().toInstant();
        ZonedDateTime creationZonedTime = creationTime.atZone(UTC);
        return creationZonedTime.toLocalDate();
    }

    public LocalDate getDateFromFileName() {
        String filenameWithoutExtension = removeExtension(file);
        return LocalDate.parse(filenameWithoutExtension, DateTimeFormatter.ofPattern(LOG_FILE_NAME_FORMAT));
    }

    private String removeExtension(final Path file) {
        return FilenameUtils.removeExtension(file.getFileName().toString());
    }

    public boolean isValid() {
        return hasValidExtension() && isNotStaticDataFile() && hasValidFormatName();
    }

    private boolean hasValidExtension() {
        String fileExtension = FilenameUtils.getExtension(file.toString());
        return fileExtension.equals(LOG_FILE_EXTENSION);
    }

    private boolean isNotStaticDataFile() {
        return !file.getFileName().toString().equals("staticdata.log");
    }

    private boolean hasValidFormatName() {
        String fileNameWithoutExtension = removeExtension(file);
        try {
            LocalDate.parse(fileNameWithoutExtension, DateTimeFormatter.ofPattern(LOG_FILE_NAME_FORMAT));
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public boolean hasBeenSavedOnFileDate() throws IOException {
        LogFile logFile = new LogFile(file);
        LocalDate fileCreationDateOnRmas = logFile.getCreationDate();
        LocalDate fileDate = logFile.getDateFromFileName();
        return fileCreationDateOnRmas.isEqual(fileDate);
    }

}
