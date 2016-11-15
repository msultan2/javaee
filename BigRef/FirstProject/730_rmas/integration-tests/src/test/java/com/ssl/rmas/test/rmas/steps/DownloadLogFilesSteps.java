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
package com.ssl.rmas.test.rmas.steps;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlDatePickerUtils;
import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DownloadLogFilesSteps {

    private static final String DOWNLOAD_LOGS_LINK_DOWNLOAD = "Download";
    private static final String DEVICE_OPERATIONS_LINK_DOWNLOAD_LOGS = "Download logs";
    private static final String START_DATE_LABEL = "Start date";
    private static final String END_DATE_LABEL = "End date";
    private static final LocalDate START_VALID_DATE_VALUE = LocalDate.of(2016,3,23);
    private static final LocalDate END_VALID_DATE_VALUE = LocalDate.of(2016,3,25);

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;
    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;
    @Autowired private HtmlButtonUtils htmlButtonUtils;
    @Autowired private HtmlDatePickerUtils htmlDatePickerUtils;

    @When("^I select the option to download logs$")
    public void downloadLogs() throws IOException {
        webDriverHelper.takeScreenshotIfPossible();
        htmlUtils.clickOnButton(DEVICE_OPERATIONS_LINK_DOWNLOAD_LOGS);
    }

    @When("^I select a valid date range of log files to download$")
    public void selectValidDateRange() {
        htmlDatePickerUtils.select(htmlModalUtils, START_DATE_LABEL, START_VALID_DATE_VALUE);
        htmlDatePickerUtils.select(htmlModalUtils, END_DATE_LABEL, END_VALID_DATE_VALUE);
    }

    @When("^I initiate log file download$")
    public void initiateDownload() throws Throwable {
         htmlButtonUtils.click(htmlModalUtils,DOWNLOAD_LOGS_LINK_DOWNLOAD);
    }

    @Then("^I should be provided with a link to access the zipped up log files$")
    public void checkIfDownloadLinkIsPresent() throws Throwable {
        htmlModalUtils.waitForStatus("SUCCESS");
        htmlModalUtils.assertLinkIsDisplayed("Download");
    }
}
