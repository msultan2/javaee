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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.HtmlTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DeviceListSteps {
    public static final String WELCOME_TO_RMAS_TITLE = "Welcome to RMAS";
    private static final String DEVICES_LINK = "Devices";
    private static final String DEVICES_TITLE = "Device List";
    static final String DEVICE_TITLE = "Device Operation";
    private static final String DEVICE_UNDER_TEST = "10.163.49.68";
    private static final String SECOND_DEVICE_UNDER_TEST = "1.1.1.68";
    static final String EXPECTED_LABEL = "Device Details";
    private static final List<String> EXPECTED_DEVICES = Arrays.asList(DEVICE_UNDER_TEST, SECOND_DEVICE_UNDER_TEST);
    private static final String IP_ADDRESS_HEADER = "IP address";
    private static final String STATUS_HEADER = "Status";
    private static final String DEVICE_LIST_TYPE_HEADER = "Device list";
    private static final List<String> MIN_EXPECTED_HEADERS = Arrays.asList(IP_ADDRESS_HEADER, STATUS_HEADER, DEVICE_LIST_TYPE_HEADER);
    static final String TITLE_SIZE_1 = "1";
    public final String userNameValue = "sergio@ssl.com";
    public final String passwordValue = "ssl1324";

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlTableUtils htmlTableUtils;
    @Autowired
    private LoginUtils loginUtils;

    @When("^I go to the device list page$")
    public void goToDeviceListPage() throws TestTimeoutException{
        loginUtils.logIn(userNameValue,passwordValue);
        htmlUtils.assertTitleText(WELCOME_TO_RMAS_TITLE);
        htmlUtils.clickOnLink(DEVICES_LINK);
    }

    @When("^I select a device$")
    public void goToDevicePage(){
        htmlUtils.clickOnLink(htmlTableUtils, DEVICE_UNDER_TEST, IP_ADDRESS_HEADER, DEVICE_UNDER_TEST);
    }

    @Then("^I see the page displaying the list of devices available to me$")
    public void checkDevices() throws TestTimeoutException{
        htmlUtils.assertTitleText(DEVICES_TITLE, TITLE_SIZE_1);
        htmlTableUtils.assertHeadersPresent(MIN_EXPECTED_HEADERS);
        htmlTableUtils.waitforNumberOfRows(EXPECTED_DEVICES.size(), 5);
        htmlTableUtils.assertRowsPresent(EXPECTED_DEVICES);
    }

    @Then("^I see the page displaying the device$")
    public void checkDevicePage(){
        htmlUtils.assertTitleText(DEVICE_TITLE, TITLE_SIZE_1);
        htmlUtils.assertPanelHeading(EXPECTED_LABEL);
    }
}
