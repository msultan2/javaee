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
package com.ssl.rmas.test.rait.steps;

import com.ssl.rmas.test.shared.utils.FileSystemUtils;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlDatePickerUtils;
import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import static com.ssl.rmas.utils.ErrorMessage.*;
import cucumber.api.java.Before;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

public class DownloadLogFilesSteps {

    private static final String DEVICE_OPERATIONS_LINK_DOWNLOAD_LOGS = "Download logs";
    private static final String DOWNLOAD_LOGS_LINK_DOWNLOAD = "Download";
    private static final String START_DATE_LABEL = "Start date";
    private static final String END_DATE_LABEL = "End date";
    private static final LocalDate START_VALID_DATE_VALUE = LocalDate.of(2016,3,23);
    private static final LocalDate END_VALID_DATE_VALUE = LocalDate.of(2016,3,25);
    private static final LocalDate START_INVALID_DATE_VALUE = LocalDate.of(2016,3,26);
    private static final LocalDate END_INVALID_DATE_VALUE = LocalDate.of(2016,3,27);

    private final Path baseLogDir = Paths.get(System.getProperty("user.home"), "rait", "10.163.49.68");

    @Autowired private HtmlDatePickerUtils htmlDatePickerUtils;
    @Autowired private HtmlModalUtils htmlModalUtils;
    @Autowired private HtmlUtils htmlUtils;
    @Autowired private HtmlButtonUtils htmlButtonUtils;
    @Autowired private FileSystemUtils fileSystemUtils;

    @Before("@RMAS-300")
    public void deleteLogIfExists() throws IOException {
        fileSystemUtils.doBetween(fileSystemUtils.deleteLogIfExists(baseLogDir), START_VALID_DATE_VALUE, END_VALID_DATE_VALUE);
    }

    @When("^I select a valid date range of log files to download$")
    public void selectValidDateRange() {
        htmlUtils.clickOnButton(DEVICE_OPERATIONS_LINK_DOWNLOAD_LOGS);
        htmlDatePickerUtils.select(htmlModalUtils, START_DATE_LABEL, START_VALID_DATE_VALUE);
        htmlDatePickerUtils.select(htmlModalUtils, END_DATE_LABEL, END_VALID_DATE_VALUE);
    }

    @When("^I select a invalid date range of log files to download$")
    public void selectInvalidDateRange() {
        htmlUtils.clickOnButton(DEVICE_OPERATIONS_LINK_DOWNLOAD_LOGS);
        htmlDatePickerUtils.select(htmlModalUtils, START_DATE_LABEL, START_INVALID_DATE_VALUE);
        htmlDatePickerUtils.select(htmlModalUtils, END_DATE_LABEL, END_INVALID_DATE_VALUE);
    }
    
    @When("^I select a partial valid date range of log files to download$")
    public void selectPartialValidDateRange() {
        htmlUtils.clickOnButton(DEVICE_OPERATIONS_LINK_DOWNLOAD_LOGS);
        htmlDatePickerUtils.select(htmlModalUtils, START_DATE_LABEL, START_VALID_DATE_VALUE);
        htmlDatePickerUtils.select(htmlModalUtils, END_DATE_LABEL, END_INVALID_DATE_VALUE);
    }

    @When("^I initiate log file download$")
    public void initiateDownload() {
        htmlButtonUtils.click(htmlModalUtils,DOWNLOAD_LOGS_LINK_DOWNLOAD);
    }

    @Then("^I should be able to view the downloaded log files")
    public void checkDownloadedLogFilesPresent() throws IOException, InterruptedException {
        htmlModalUtils.waitForStatus(HtmlModalUtils.STATUS_SUCCESS);
        htmlModalUtils.assertTextIsDisplayed(baseLogDir.toString());
        fileSystemUtils.doBetween(fileSystemUtils.assertLogContent(baseLogDir), START_VALID_DATE_VALUE, END_VALID_DATE_VALUE);
    }

    @Then("^I should not be able to view the downloaded log files")
    public void checkDownloadedLogFilesNotPresent() throws IOException, InterruptedException {
        htmlModalUtils.waitForStatus(HtmlModalUtils.STATUS_FAILURE);
        htmlModalUtils.assertTextIsDisplayed(FILES_NOT_FOUND_ON_ROADSIDE_DEVICE.toString());
        fileSystemUtils.doBetween(fileSystemUtils.assertLogFileNotPresentIn(baseLogDir), START_INVALID_DATE_VALUE, END_INVALID_DATE_VALUE);
    }
    
    @Then("^I should be able to view one or more the downloaded log files")
    public void checkDownloadedLogFilesPresentAndNot() throws IOException, InterruptedException {
        htmlModalUtils.waitForStatus(HtmlModalUtils.STATUS_PARTIAL_SUCCESS);
        htmlModalUtils.assertTextIsDisplayed(FAILED_TO_DOWNLOAD_ONE_OR_MORE_FILES.toString());
        htmlModalUtils.assertTextIsDisplayed(baseLogDir.toString());
        fileSystemUtils.doBetween(fileSystemUtils.assertLogContent(baseLogDir), START_VALID_DATE_VALUE, END_VALID_DATE_VALUE);
        fileSystemUtils.doBetween(fileSystemUtils.assertLogFileNotPresentIn(baseLogDir), START_INVALID_DATE_VALUE, END_INVALID_DATE_VALUE);
    }
}
