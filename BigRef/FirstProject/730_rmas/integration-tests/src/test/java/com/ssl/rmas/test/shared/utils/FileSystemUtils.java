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
 * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 */
package com.ssl.rmas.test.shared.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.springframework.stereotype.Component;


@Component
public class FileSystemUtils {

    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter CONTENT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FunctionalInterface
    public interface LocalDateOperation {
        public void execute(LocalDate date) throws IOException;
    }

    public LocalDateOperation assertLogFileNotPresentIn(Path basePath) {
        return (localDate) -> {
            Path completePath = getLogFile(basePath, localDate);
            Assert.assertFalse(completePath.toFile().exists());
        };
    }
    
    public LocalDateOperation deleteLogIfExists(Path basePath) {
        return (localDate) -> {
            Files.deleteIfExists(getLogFile(basePath, localDate));
        };
    }
    
    public LocalDateOperation assertLogContent(Path basePath) {
        return (localDate) -> {
            Path log = getLogFile(basePath, localDate);
            if (log.toFile().exists()) {
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(log.toFile()))) {
                    Assert.assertThat(bufferedReader.readLine(), is(equalTo("SSL XXXX")));
                    String dateString = localDate.format(CONTENT_FORMAT);
                    Assert.assertTrue(bufferedReader.readLine().startsWith(dateString));
                }
            } else {
                fail(log.toAbsolutePath() + " doesn't exist");
            }
        };
    }
    
    public void doBetween(LocalDateOperation operation, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        LocalDate nextDate = dateFrom;
        while (!nextDate.isAfter(dateTo)) {
            operation.execute(nextDate);
            nextDate = nextDate.plusDays(1L);
        }
    }

    private Path getLogFile(Path basePath, LocalDate localDate) {
        return Paths.get(basePath.toString(), localDate.format(FILE_NAME_FORMAT) + ".log");
    }
}
