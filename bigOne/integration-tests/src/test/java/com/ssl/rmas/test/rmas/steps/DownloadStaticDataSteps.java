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
package com.ssl.rmas.test.rmas.steps;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlModalUtils;
import com.ssl.rmas.test.shared.utils.HtmlTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.Arrays;

public class DownloadStaticDataSteps {

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlModalUtils htmlModalUtils;
    @Autowired
    private HtmlTableUtils htmlTableUtils;
    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;

    private static final String DEVICES_LINK = "Devices";
    private static final String DEVICES_OPERATION_PAGE_TITLE= "Device Operation";
    private static final String IP_ADDRESS_HEADER = "IP address";
    private static final String DEVICE_UNDER_TEST = "10.163.49.68";
    @When("^I refresh the device details$")
    public void downloadStaticData() throws IOException, InterruptedException {
       
        webDriverHelper.takeScreenshotIfPossible();
        if(!webDriverHelper.getTitle().contains(DEVICES_OPERATION_PAGE_TITLE)) {
        htmlUtils.clickOnLink(DEVICES_LINK);
        htmlUtils.clickOnLink(htmlTableUtils, DEVICE_UNDER_TEST, IP_ADDRESS_HEADER, DEVICE_UNDER_TEST);
        }
        htmlUtils.clickOnButton(HtmlUtils.DOWNLOAD_STATIC_DATA_BUTTON);
    }

    @Then("^I will receive notification that the device details have been updated$")
    public void checkFileDownload() throws InterruptedException, IOException {
        htmlModalUtils.waitForStatus(Arrays.asList(HtmlModalUtils.STATUS_UPDATED, HtmlModalUtils.STATUS_SUCCESS));
        htmlModalUtils.clickFooterButton(HtmlButtonUtils.OK_BUTTON);
    }

}
