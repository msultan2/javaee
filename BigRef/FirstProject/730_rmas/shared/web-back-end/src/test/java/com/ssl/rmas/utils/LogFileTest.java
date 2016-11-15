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
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import static java.time.Month.*;
import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class LogFileTest {

    private Path file1 = Paths.get("src", "test", "resources", "20160323.log");
    private Path file2 = Paths.get("src", "test", "resources", "20161109.log");
    private Path file3 = Paths.get("src", "test", "resources", "staticdata.log");

    @Before
    public void setUp() throws IOException {
        changeFilesCreationTime(LocalDate.of(2016, NOVEMBER, 9));
    }

    private void changeFilesCreationTime(final LocalDate date) throws IOException {
        FileTime time = FileTime.from(date.atStartOfDay(UTC).toInstant());
        changeFileCreationTime(file1, time);
        changeFileCreationTime(file2, time);
        changeFileCreationTime(file3, time);
    }

    private void changeFileCreationTime(final Path file, final FileTime createTime) throws IOException {
        BasicFileAttributeView attrsView = Files.getFileAttributeView(file, BasicFileAttributeView.class);
        attrsView.setTimes(createTime, null, createTime);
    }

    @Test
    public void testGetCreationDate() throws Exception {
        LogFile logFile = new LogFile(file1);

        assertEquals(LocalDate.of(2016, NOVEMBER, 9), logFile.getCreationDate());
    }

    @Test
    public void testGetDateFromFileName() {
        LogFile logFile = new LogFile(file1);

        assertEquals(LocalDate.of(2016, MARCH, 23), logFile.getDateFromFileName());
    }

    @Test
    public void testIsValid() {
        assertTrue(new LogFile(file1).isValid());
    }

    @Test
    public void testIsNotValid() {
        assertFalse(new LogFile(file3).isValid());
    }

    @Test
    public void testHasBeenSavedOnFileDate() throws Exception {
        LogFile logFile = new LogFile(file2);

        assertTrue(logFile.hasBeenSavedOnFileDate());
    }

    @Test
    public void testHasNotBeenSavedOnFileDate() throws Exception {
        LogFile logFile = new LogFile(file1);

        assertFalse(logFile.hasBeenSavedOnFileDate());
    }

}
