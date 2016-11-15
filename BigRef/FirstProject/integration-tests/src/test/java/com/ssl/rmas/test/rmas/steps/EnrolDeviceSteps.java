/**
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
package com.ssl.rmas.test.rmas.steps;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.UserUtils;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlTableUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrolDeviceSteps {

    private static final String NEW_DEVICE = "1.1.1.8";
    private static final String EXISTING_DEVICE = "1.1.1.1";
    private static final List<String> EXPECTED_DEVICES = Arrays.asList("10.163.49.68", "1.1.1.68", NEW_DEVICE);
    private static final String IP_ADDRESS_LABEL = "IP address";
    private static final String RCC_LABEL = "RCC";
    private static final String BANDWIDTH_LIMIT_LABEL = "Bandwidth limit (Kbps)";
    
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private HtmlTableUtils htmlTableUtils;

    @Given("^I am on the device enrolment page$")
    public void navigateToRegistrationPage() {
        userUtils.goToEnrolmentPage();
    }

    @When("^I submit the details of the new device$")
    public void deviceDetails() {
        htmlFormUtils.set(IP_ADDRESS_LABEL, NEW_DEVICE);
        htmlFormUtils.set(BANDWIDTH_LIMIT_LABEL, "256");
        htmlFormUtils.setSelect(RCC_LABEL, "South West");
        htmlUtils.clickOnButton("Submit");
    }

    @When("^I enter the details of a device with an IP address which already exists in RMAS$")
    public void deviceWhichAlreadyInRmas() {
        htmlFormUtils.set(IP_ADDRESS_LABEL, EXISTING_DEVICE);
        htmlFormUtils.set(BANDWIDTH_LIMIT_LABEL, "256");
        htmlFormUtils.setSelect(RCC_LABEL, "South East");
        htmlUtils.clickOnButton("Submit");
    }

    @Then("^I am informed that the enrolment was successful$")
    public void successDeviceEnrolment() {
        htmlUtils.assertAlertText(HtmlUtils.SUCCESS_DEVICE_ENROLMENT_MESSAGE);
    }

    @Then("^I am informed that the enrolment was unsuccessful$")
    public void unsuccessDeviceEnrolment() {
        htmlUtils.assertAlertText(HtmlUtils.UNSUCCESS_DEVICE_ENROLMENT_MESSAGE);
    }

    @Then("^I see the list of devices including the new device$")
    public void deviceListUpdatedSuccessfully() throws TestTimeoutException {
        htmlTableUtils.waitforNumberOfRows(EXPECTED_DEVICES.size(), 5);
        htmlTableUtils.assertRowsPresent(EXPECTED_DEVICES);
    }

}
